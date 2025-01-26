package org.alex_hashtag.internal_representation.macros;

import lombok.Getter;
import org.alex_hashtag.internal_representation.util.Locatable;
import org.alex_hashtag.tokenization.Coordinates;
import org.alex_hashtag.tokenization.Token;
import org.alex_hashtag.tokenization.TokenStream;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;



public class Macro implements Locatable
{
    @Getter
    Coordinates location;
    String name;
    List<Arm> arms;
    boolean pub;

    public Macro(Coordinates location, String name)
    {
        this.location = location;
        this.name = name;
        this.arms = new ArrayList<>();
    }

    public void addArm(Pattern pattern, TokenStream codeSnippets)
    {
        this.arms.add(new Arm(pattern, codeSnippets));
    }

    public record Arm
            (
        Pattern pattern,
        TokenStream codeSnippets
            ){}

    public static class Pattern
    {
        private final List<PatternElement> elements;

        public Pattern(List<PatternElement> elements)
        {
            this.elements = elements;
        }

        /**
         * An enum to represent the repetition type:
         * - ZERO_OR_MORE => `*`
         * - ONE_OR_MORE  => `+`
         * - ZERO_OR_ONE  => `?`
         */
        public enum RepetitionKind
        {
            ZERO_OR_MORE,
            ONE_OR_MORE,
            ZERO_OR_ONE
        }

        public enum MacroVarType
        {
            EXPRESSION,
            TYPE,
            IDENTIFIER
        }

        public static abstract class PatternElement
        {
        }

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
        }

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
        }

        @Getter
        public static class RepetitionElement extends PatternElement
        {
            private final Pattern subPattern;        // The pattern inside $( ... )
            private final RepetitionKind repetition; // ZERO_OR_MORE (*), ONE_OR_MORE (+), or ZERO_OR_ONE (?)
            private final String separator;          // e.g. "," or ";" or null if none

            public RepetitionElement(Pattern subPattern, RepetitionKind repetition, String separator)
            {
                this.subPattern = subPattern;
                this.repetition = repetition;
                this.separator = separator;
            }

        }
    }

}
