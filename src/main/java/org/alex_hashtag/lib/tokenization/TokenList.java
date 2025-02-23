package org.alex_hashtag.lib.tokenization;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Represents a list of tokens generated from an input string, given a set of TokenRules
 * and a TokenPostProcessor. Provides iteration, look-ahead, invalid-token retrieval, and
 * a formatted table output of all tokens.
 */
public class TokenList implements Iterable<Token>
{

    private final List<Token> tokens;

    private TokenList(List<Token> tokens)
    {
        this.tokens = Collections.unmodifiableList(tokens);
    }

    /**
     * Factory method:
     * 1) Tokenizes the input using the given rules.
     * 2) Applies the post-processor to each token.
     * 3) Returns a new TokenList.
     */
    public static TokenList create(String input, TokenRules rules, TokenPostProcessor postProcessor)
    {
        List<Token> rawTokens = tokenize(input, rules);
        List<Token> processedTokens = applyPostProcessing(rawTokens, postProcessor);
        return new TokenList(processedTokens);
    }

    // ==================================================
    // =============== TOKENIZATION LOGIC ===============
    // ==================================================

    private static List<Token> tokenize(String input, TokenRules rules)
    {
        // 1) Normalize line endings
        input = input.replace("\r\n", "\n").replace("\r", "\n");

        // 2) We'll store everything (except Start/End/NewLine) in a single list
        //    so we can do "longest match" among all prototypes
        List<InternalProto> protoList = new ArrayList<>();
        // We'll keep track of whether we have Start/End in the prototypes
        boolean hasStart = false;
        boolean hasEnd = false;

        for (TokenPrototype proto : rules.tokenPrototypes)
        {
            switch (proto)
            {
                case TokenPrototype.Keyword kw ->
                {
                    protoList.add(InternalProto.keyword(kw.value(), rules.caseSensitive));
                }
                case TokenPrototype.Delimeter d ->
                {
                    protoList.add(InternalProto.delimiter(d.value()));
                }
                case TokenPrototype.Operator op ->
                {
                    protoList.add(InternalProto.operator(op.value()));
                }
                case TokenPrototype.Comment c ->
                {
                    protoList.add(InternalProto.comment(c.regex()));
                }
                case TokenPrototype.Literal lit ->
                {
                    protoList.add(InternalProto.literal(lit.type(), lit.regex()));
                }
                case TokenPrototype.Identifier id ->
                {
                    protoList.add(InternalProto.identifier(id.type(), id.regex()));
                }
                case TokenPrototype.Start s ->
                {
                    hasStart = true;
                }
                case TokenPrototype.End e ->
                {
                    hasEnd = true;
                }
                case TokenPrototype.NewLine n ->
                {
                    // We'll produce NewLine tokens ourselves on each line break
                }
            }
        }

        // 3) If rules.longestMatchFirst, we want prototypes that match longer strings first.
        //    We'll apply a custom comparator:
        if (rules.longestMatchFirst)
        {
            protoList.sort((a, b) ->
            {
                // If both are fixed strings, compare lengths desc
                if (a.fixedString != null && b.fixedString != null)
                {
                    int diff = b.fixedString.length() - a.fixedString.length();
                    if (diff != 0) return diff;
                    // Tie-break: if the strings are the same, and one is delimiter vs operator
                    if (a.type == InternalProtoType.DELIMITER && b.type == InternalProtoType.OPERATOR
                            && a.fixedString.equals(b.fixedString))
                    {
                        return -1; // a (DELIMITER) has priority
                    }
                    if (b.type == InternalProtoType.DELIMITER && a.type == InternalProtoType.OPERATOR
                            && b.fixedString.equals(a.fixedString))
                    {
                        return 1;
                    }
                    // else fallback to the order of type
                    return a.type.ordinal() - b.type.ordinal();
                }
                // If one is a fixed string and the other is a regex, keep them in stable order
                return a.type.ordinal() - b.type.ordinal();
            });
        }

        // 4) We'll produce tokens in a result list
        List<Token> result = new ArrayList<>();
        Coordinates startCoord = new Coordinates(0, 0);
        if (hasStart)
        {
            result.add(new Token.Start(startCoord));
        }

        int line = 1;
        int col = 1;
        int index = 0;
        int length = input.length();

        while (index < length)
        {
            char c = input.charAt(index);

            // Handle newlines explicitly
            if (c == '\n')
            {
                // If SIGNIFICANT or INDENTATION, produce a NewLine token
                if (rules.whitespaceMode != WhitespaceMode.IGNORE)
                {
                    result.add(new Token.NewLine(new Coordinates(line, col)));
                }
                line++;
                col = 1;
                index++;
                continue;
            }

            // Skip whitespace if IGNORE
            if (rules.whitespaceMode == WhitespaceMode.IGNORE && Character.isWhitespace(c))
            {
                col++;
                index++;
                continue;
            }

            // For SIGNIFICANT or INDENTATION, you might want to produce tokens for spaces,
            // but let's just skip them for simplicity:
            if ((rules.whitespaceMode == WhitespaceMode.SIGNIFICANT
                    || rules.whitespaceMode == WhitespaceMode.INDENTATION)
                    && Character.isWhitespace(c))
            {
                col++;
                index++;
                continue;
            }

            // Now we do the "longest match among all prototypes" approach.
            // We'll collect all matches, pick the best.
            BestMatch best = new BestMatch(-1, null, null);

            for (InternalProto p : protoList)
            {
                MatchResult mr = p.match(input, index, line, col, rules.caseSensitive);
                if (mr != null && mr.length > best.length)
                {
                    best = new BestMatch(mr.length, mr.token, p.type);
                }
                else if (mr != null && mr.length == best.length)
                {
                    // Tie-break rules:
                    // Delimiter over Operator if same length and same text
                    if (p.type == InternalProtoType.DELIMITER && best.type == InternalProtoType.OPERATOR)
                    {
                        if (mr.token instanceof Token.Delimiter delT
                                && best.token instanceof Token.Operator opT
                                && delT.value().equals(opT.value()))
                        {
                            best = new BestMatch(mr.length, mr.token, p.type);
                        }
                    }
                    // else keep the first match
                }
            }

            if (best.token != null)
            {
                // We got a match
                result.add(best.token);
                index += best.length;
                col += best.length;
            }
            else
            {
                // No matches => invalid
                result.add(new Token.Invalid(new Coordinates(line, col), String.valueOf(c)));
                index++;
                col++;
            }
        }

        Coordinates endCoord = new Coordinates(line, col);
        if (hasEnd)
        {
            result.add(new Token.End(endCoord));
        }

        return result;
    }

    /**
     * Applies the TokenPostProcessor transformations to the raw tokens.
     */
    private static List<Token> applyPostProcessing(List<Token> rawTokens, TokenPostProcessor postProcessor)
    {
        List<Token> output = new ArrayList<>(rawTokens.size());
        for (Token t : rawTokens)
        {
            Token finalToken = t;

            if (t instanceof Token.Literal lit)
            {
                // Use lit.type() as the key
                for (Function<Token, Token> func : postProcessor.getProcessors(lit.type()))
                {
                    finalToken = func.apply(finalToken);
                }
            }
            else if (t instanceof Token.Identifier id)
            {
                for (Function<Token, Token> func : postProcessor.getProcessors(id.type()))
                {
                    finalToken = func.apply(finalToken);
                }
            }
            else if (t instanceof Token.Keyword kw)
            {
                // We'll call the key "keyword"
                for (Function<Token, Token> func : postProcessor.getProcessors("keyword"))
                {
                    finalToken = func.apply(finalToken);
                }
            }
            else if (t instanceof Token.Operator op)
            {
                for (Function<Token, Token> func : postProcessor.getProcessors("operator"))
                {
                    finalToken = func.apply(finalToken);
                }
            }
            else if (t instanceof Token.Delimiter d)
            {
                for (Function<Token, Token> func : postProcessor.getProcessors("delimiter"))
                {
                    finalToken = func.apply(finalToken);
                }
            }
            else if (t instanceof Token.Comment c)
            {
                // We'll call the key "comment"
                for (Function<Token, Token> func : postProcessor.getProcessors("comment"))
                {
                    finalToken = func.apply(finalToken);
                }
            }

            output.add(finalToken);
        }
        return output;
    }

    // ==================================================
    // ================== COLLECTION API =================
    // ==================================================

    @Override
    public @NotNull Iterator<Token> iterator()
    {
        return new LookAheadIterator(tokens);
    }

    /**
     * Returns a list of all Invalid tokens in this TokenList.
     */
    public List<Token.Invalid> getInvalid()
    {
        List<Token.Invalid> invalids = new ArrayList<>();
        for (Token t : tokens)
        {
            if (t instanceof Token.Invalid inv)
            {
                invalids.add(inv);
            }
        }
        return invalids;
    }

    /**
     * A formatted string of all tokens as a neat table:
     * Columns: #, Type, Position, Value
     */
    @Override
    public String toString()
    {
        final String[] headers = {"#", "Type", "Position", "Value"};
        int[] widths = {2, 4, 10, 5};

        List<String[]> rows = new ArrayList<>();
        int i = 0;
        for (Token t : tokens)
        {
            i++;
            String idx = String.valueOf(i);
            String type = t.getClass().getSimpleName(); // e.g. "Keyword", "Operator"...
            Coordinates pos = switch (t)
            {
                case Token.Keyword k -> k.position();
                case Token.Delimiter d -> d.position();
                case Token.Operator o -> o.position();
                case Token.Literal l -> l.position();
                case Token.Identifier id -> id.position();
                case Token.Comment c -> c.position();
                case Token.Start st -> st.position();
                case Token.End en -> en.position();
                case Token.NewLine nl -> nl.position();
                case Token.Invalid iv -> iv.position();
            };
            String position = "(" + pos.line() + "," + pos.column() + ")";
            String val = switch (t)
            {
                case Token.Keyword k -> k.value();
                case Token.Delimiter d -> d.value();
                case Token.Operator o -> o.value();
                case Token.Literal l -> l.type() + ":" + l.value();
                case Token.Identifier id -> id.type() + ":" + id.value();
                case Token.Comment c -> c.value();
                case Token.Start st -> "START";
                case Token.End en -> "END";
                case Token.NewLine nl -> "NEWLINE";
                case Token.Invalid iv -> "INVALID:" + iv.value();
            };

            String[] row = {idx, type, position, val};
            rows.add(row);

            widths[0] = Math.max(widths[0], idx.length());
            widths[1] = Math.max(widths[1], type.length());
            widths[2] = Math.max(widths[2], position.length());
            widths[3] = Math.max(widths[3], val.length());
        }

        String fmt = String.format("%%-%ds | %%-%ds | %%-%ds | %%-%ds\n",
                widths[0], widths[1], widths[2], widths[3]);

        StringBuilder sb = new StringBuilder();
        sb.append(String.format(fmt, headers[0], headers[1], headers[2], headers[3]));
        sb.append(buildSeparator(widths));
        for (String[] row : rows)
        {
            sb.append(String.format(fmt, row[0], row[1], row[2], row[3]));
        }

        return sb.toString();
    }

    private String buildSeparator(int[] widths)
    {
        // e.g. "---+------+--------+-------"
        StringBuilder sep = new StringBuilder();
        for (int i = 0; i < widths.length; i++)
        {
            if (i > 0) sep.append("-+-");
            sep.append("-".repeat(widths[i]));
        }
        sep.append("\n");
        return sep.toString();
    }

    private enum InternalProtoType
    {
        KEYWORD,
        DELIMITER,
        OPERATOR,
        COMMENT,
        LITERAL,
        IDENTIFIER
    }

    // ==================================================
    // ================ HELPER STRUCTS ==================
    // ==================================================

    /**
     * An internal record that holds the best match info.
     */
    private record BestMatch(int length, Token token, InternalProtoType type)
    {
    }

    /**
     * A container for token-prototype logic in one place (keyword, operator, etc.).
     * 'type' helps with tie-break logic or priority. 'fixedString' is for direct matching,
     * 'literalType' is for LITERAL or IDENTIFIER. 'pattern' is for regex-based matching.
     */
    private static class InternalProto
    {
        final InternalProtoType type;
        final String fixedString;   // for Keyword, Operator, Delimiter
        final String literalType;   // for Literal/Identifier
        final Pattern pattern;      // for Comment, Literal, Identifier

        private InternalProto(InternalProtoType type, String fixedString, String literalType, Pattern pattern)
        {
            this.type = type;
            this.fixedString = fixedString;
            this.literalType = literalType;
            this.pattern = pattern;
        }

        static InternalProto keyword(String word, boolean caseSensitive)
        {
            // We'll store as a fixed string for direct match
            String val = caseSensitive ? word : word.toLowerCase(Locale.ROOT);
            return new InternalProto(InternalProtoType.KEYWORD, val, null, null);
        }

        static InternalProto delimiter(String val)
        {
            return new InternalProto(InternalProtoType.DELIMITER, val, null, null);
        }

        static InternalProto operator(String val)
        {
            return new InternalProto(InternalProtoType.OPERATOR, val, null, null);
        }

        static InternalProto comment(String userRegex)
        {
            // We'll remove leading ^ and trailing $ if present,
            // then wrap in "^( ... )" so it matches from the start
            String cleaned = cleanAnchors(userRegex);
            Pattern p = Pattern.compile("^(?:" + cleaned + ")");
            return new InternalProto(InternalProtoType.COMMENT, null, null, p);
        }

        static InternalProto literal(String type, String userRegex)
        {
            String cleaned = cleanAnchors(userRegex);
            Pattern p = Pattern.compile("^(?:" + cleaned + ")");
            return new InternalProto(InternalProtoType.LITERAL, null, type, p);
        }

        static InternalProto identifier(String type, String userRegex)
        {
            String cleaned = cleanAnchors(userRegex);
            Pattern p = Pattern.compile("^(?:" + cleaned + ")");
            return new InternalProto(InternalProtoType.IDENTIFIER, null, type, p);
        }

        /**
         * Removes leading ^ and trailing $ if present, so partial substring matches can succeed.
         */
        private static String cleanAnchors(String regex)
        {
            String out = regex;
            // If user included ^ at the start, remove it
            if (out.startsWith("^"))
            {
                out = out.substring(1);
            }
            // If user included $ at the end, remove it
            if (out.endsWith("$"))
            {
                out = out.substring(0, out.length() - 1);
            }
            return out;
        }

        // Utility checks
        private static boolean isAlpha(String s)
        {
            for (char c : s.toCharArray())
            {
                if (!Character.isLetter(c)) return false;
            }
            return true;
        }

        private static boolean isIdentifierChar(char c)
        {
            return Character.isLetterOrDigit(c) || c == '_';
        }

        /**
         * Tries to match from 'index' in 'input'.
         * If match succeeds, returns MatchResult with matched length and the resulting Token.
         */
        MatchResult match(String input, int index, int line, int col, boolean caseSensitive)
        {
            if (index >= input.length()) return null;

            return switch (type)
            {
                case KEYWORD -> matchKeyword(input, index, line, col, caseSensitive);
                case DELIMITER, OPERATOR -> matchFixedString(input, index, line, col);
                case COMMENT, LITERAL, IDENTIFIER -> matchRegex(input, index, line, col);
            };
        }

        // --------------------------------------
        // KEYWORD
        // --------------------------------------
        private MatchResult matchKeyword(String input, int index, int line, int col, boolean caseSensitive)
        {
            String kw = this.fixedString;
            int len = kw.length();

            if (index + len > input.length()) return null;
            String chunk = input.substring(index, index + len);
            if (!caseSensitive) chunk = chunk.toLowerCase(Locale.ROOT);
            if (!chunk.equals(kw)) return null;

            // Boundary check if purely alphabetical
            if (isAlpha(kw))
            {
                if (index + len < input.length())
                {
                    char next = input.charAt(index + len);
                    if (isIdentifierChar(next))
                    {
                        // e.g. "or" next char is 'g' => "org"
                        return null;
                    }
                }
            }
            // success
            return new MatchResult(len,
                    new Token.Keyword(new Coordinates(line, col), input.substring(index, index + len)));
        }

        // --------------------------------------
        // DELIMITER / OPERATOR
        // --------------------------------------
        private MatchResult matchFixedString(String input, int index, int line, int col)
        {
            String val = this.fixedString;
            int len = val.length();

            if (index + len > input.length()) return null;
            String chunk = input.substring(index, index + len);
            if (!chunk.equals(val)) return null;

            // If it's alphabetical (like "and"), boundary check
            if (isAlpha(val))
            {
                if (index + len < input.length())
                {
                    char next = input.charAt(index + len);
                    if (isIdentifierChar(next))
                    {
                        return null; // partial overlap with an identifier
                    }
                }
            }
            return switch (type)
            {
                case DELIMITER -> new MatchResult(len,
                        new Token.Delimiter(new Coordinates(line, col), val));
                case OPERATOR -> new MatchResult(len,
                        new Token.Operator(new Coordinates(line, col), val));
                default -> null;
            };
        }

        // --------------------------------------
        // COMMENT / LITERAL / IDENTIFIER (Regex)
        // --------------------------------------
        private MatchResult matchRegex(String input, int index, int line, int col)
        {
            Matcher m = pattern.matcher(input.substring(index));
            if (!m.find()) return null;
            if (m.start() != 0) return null; // must start at index 0 of substring

            String matchedText = m.group();
            return switch (type)
            {
                case COMMENT -> new MatchResult(matchedText.length(),
                        new Token.Comment(new Coordinates(line, col), matchedText));
                case LITERAL -> new MatchResult(matchedText.length(),
                        new Token.Literal(new Coordinates(line, col), literalType, matchedText));
                case IDENTIFIER -> new MatchResult(matchedText.length(),
                        new Token.Identifier(new Coordinates(line, col), literalType, matchedText));
                default -> null;
            };
        }
    }

    private record MatchResult(int length, Token token)
    {
    }

    /**
     * Special iterator that supports lookAhead without consuming tokens.
     */
    public static class LookAheadIterator implements Iterator<Token>
    {
        private int currentIndex = 0;
        private final List<Token> tokens;

        public LookAheadIterator(List<Token> tokens) {
            this.tokens = tokens;
        }

        @Override
        public boolean hasNext() {
            return currentIndex < tokens.size();
        }

        @Override
        public Token next() {
            if (!hasNext()) throw new NoSuchElementException();
            return tokens.get(currentIndex++);
        }

        public Token lookAhead(int steps) {
            int idx = currentIndex + steps;
            if (idx < 0 || idx >= tokens.size()) {
                return null;
            }
            return tokens.get(idx);
        }

        public void remove() {
            if (currentIndex == 0) {
                throw new IllegalStateException("No token to remove.");
            }
            tokens.remove(--currentIndex);
        }
    }
    // ================ END TOKENIZATION ================
}
