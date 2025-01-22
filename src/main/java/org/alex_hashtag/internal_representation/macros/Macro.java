package org.alex_hashtag.internal_representation.macros;

import lombok.Getter;
import org.alex_hashtag.internal_representation.expression.Expression;
import org.alex_hashtag.internal_representation.util.Locatable;
import org.alex_hashtag.tokenization.Coordinates;

import java.util.List;


public class Macro implements Locatable
{
    @Getter
    Coordinates location;
    String name;
    List<Arm> arms;
    static class Arm
    {
        Pattern pattern;
        Expression expansion;
    }
    public static class Pattern {
        private final List<PatternElement> elements;

        public Pattern(List<PatternElement> elements)
        {
            this.elements = elements;
        }


        static abstract class PatternElement {}
        static class LiteralElement extends PatternElement
        {
            private final String token;

            public LiteralElement(String token) {
                this.token = token;
            }

            public String getToken() {
                return token;
            }
        }
        static class VariableElement extends PatternElement {
            private final String name;
            private final MacroVarType type;

            public VariableElement(String name, MacroVarType type) {
                this.name = name;
                this.type = type;
            }

            public String getName() {
                return name;
            }

            public MacroVarType getType() {
                return type;
            }
        }
        @Getter
        static class RepetitionElement extends PatternElement {
            private final Pattern subPattern;        // The pattern inside $( ... )
            private final RepetitionKind repetition; // ZERO_OR_MORE (*), ONE_OR_MORE (+), or ZERO_OR_ONE (?)
            private final String separator;          // e.g. "," or ";" or null if none

            public RepetitionElement(Pattern subPattern, RepetitionKind repetition, String separator) {
                this.subPattern = subPattern;
                this.repetition = repetition;
                this.separator = separator;
            }

        }

        /**
         * An enum to represent the repetition type:
         *  - ZERO_OR_MORE => `*`
         *  - ONE_OR_MORE  => `+`
         *  - ZERO_OR_ONE  => `?`
         */
        enum RepetitionKind {
            ZERO_OR_MORE,
            ONE_OR_MORE,
            ZERO_OR_ONE
        }

        enum MacroVarType {
            EXPRESSION,
            TYPE,
            IDENTIFIER
        }
    }

}