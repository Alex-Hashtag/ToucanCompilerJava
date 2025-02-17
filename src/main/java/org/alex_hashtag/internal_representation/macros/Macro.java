package org.alex_hashtag.internal_representation.macros;

import lombok.Getter;
import org.alex_hashtag.internal_representation.utils.Locatable;
import org.alex_hashtag.tokenizationOLD.CoordinatesOLD;
import org.alex_hashtag.tokenizationOLD.TokenStream;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class Macro implements Locatable
{
    private final String name;
    private final ArrayList<Arm> arms;
    private final boolean pub;
    @Getter
    private CoordinatesOLD location;

    /**
     * Constructor accepting the 'pub' flag.
     *
     * @param location Coordinates where the macro is defined.
     * @param name     Name of the macro.
     * @param pub      Indicates if the macro is public.
     */
    public Macro(CoordinatesOLD location, String name, boolean pub)
    {
        this.location = location;
        this.name = name;
        this.arms = new ArrayList<>();
        this.pub = pub;
    }

    public void addArm(Pattern pattern, TokenStream codeSnippets)
    {
        this.arms.add(new Arm(pattern, codeSnippets));
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("  Macro {\n")
                .append("    Name: '").append(name).append("',\n")
                .append("    Location: ").append(location).append(",\n")
                .append("    Public: ").append(pub).append(",\n")
                .append("    Arms: [\n");


        for (int i = 0; i < arms.size(); i++)
        {
            sb.append(arms.get(i).toString());
            if (i < arms.size() - 1)
            {
                sb.append(",");
            }
            sb.append("\n");
        }

        sb.append("    ]\n")
                .append("  }");
        return sb.toString();
    }

    /**
     * Represents an arm of the macro, consisting of a pattern and corresponding code snippets.
     */
    public record Arm(Pattern pattern, TokenStream codeSnippets)
    {
        @Override
        public String toString()
        {
            return "    Arm {\n" +
                    "      Pattern: " + pattern + ",\n" +
                    "      CodeSnippets: " + codeSnippets + "\n" +
                    "    }";
        }
    }

    /**
     * Represents a pattern used in the macro, consisting of multiple pattern elements.
     */
    public static class Pattern
    {
        private final List<PatternElement> elements;

        public Pattern(List<PatternElement> elements)
        {
            this.elements = elements;
        }

        public List<PatternElement> getElements()
        {
            return elements;
        }

        @Override
        public String toString()
        {
            String elementsStr = elements.stream()
                    .map(PatternElement::toString)
                    .collect(Collectors.joining(", "));
            return "Pattern [ " + elementsStr + " ]";
        }

        public enum RepetitionKind
        {
            ZERO_OR_MORE, // *
            ONE_OR_MORE,  // +
            ZERO_OR_ONE   // ?
        }

        public enum MacroVarType
        {
            EXPRESSION,
            TYPE,
            IDENTIFIER
        }

        /**
         * Abstract base class for different types of pattern elements.
         */
        public static abstract class PatternElement
        {
            @Override
            public abstract String toString();
        }

        /**
         * Represents a literal element in the pattern.
         */
        public static class LiteralElement extends PatternElement
        {
            private final String token;

            public LiteralElement(String token)
            {
                this.token = token;
            }

            public String getToken()
            {
                return token;
            }

            @Override
            public String toString()
            {
                return "Literal('" + token + "')";
            }
        }

        /**
         * Represents a variable element in the pattern.
         */
        public static class VariableElement extends PatternElement
        {
            private final String name;
            private final MacroVarType type;

            public VariableElement(String name, MacroVarType type)
            {
                this.name = name;
                this.type = type;
            }

            public String getName()
            {
                return name;
            }

            public MacroVarType getType()
            {
                return type;
            }

            @Override
            public String toString()
            {
                return "Variable(name='" + name + "', type=" + type + ")";
            }
        }

        /**
         * Represents a repetition element in the pattern.
         */
        @Getter
        public static class RepetitionElement extends PatternElement
        {
            private final Pattern subPattern;
            private final RepetitionKind repetition;
            private final String separator;

            public RepetitionElement(Pattern subPattern, RepetitionKind repetition, String separator)
            {
                this.subPattern = subPattern;
                this.repetition = repetition;
                this.separator = separator;
            }

            @Override
            public String toString()
            {
                String sep = separator != null ? ", separator='" + separator + "'" : "";
                return "Repetition(" + repetition + sep + ", SubPattern=" + subPattern + ")";
            }
        }
    }
}
