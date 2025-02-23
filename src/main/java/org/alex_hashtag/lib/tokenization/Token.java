package org.alex_hashtag.lib.tokenization;

public sealed interface Token
        permits Token.Keyword, Token.Delimiter, Token.Operator, Token.Literal, Token.Identifier, Token.Comment, Token.Start, Token.End, Token.NewLine, Token.Invalid
{
    record Keyword(Coordinates position, String value) implements Token
    {
        @Override
        public String toString() {
            return "Keyword: " + value;
        }

        public String getValue() {
            return value;
        }
    }

    record Delimiter(Coordinates position, String value) implements Token
    {
        @Override
        public String toString() {
            return "Delimiter: " + value;
        }

        public String getValue() {
            return value;
        }
    }

    record Operator(Coordinates position, String value) implements Token
    {
        @Override
        public String toString() {
            return "Operator: " + value;
        }

        public String getValue() {
            return value;
        }
    }

    record Literal(Coordinates position, String type, String value) implements Token
    {
        @Override
        public String toString() {
            return "Literal (" + type + "): " + value;
        }

        public String getValue() {
            return value;
        }
    }

    record Identifier(Coordinates position, String type, String value) implements Token
    {
        @Override
        public String toString() {
            return "Identifier (" + type + "): " + value;
        }

        public String getValue() {
            return value;
        }
    }

    record Comment(Coordinates position, String value) implements Token
    {
        @Override
        public String toString() {
            return "Comment: " + value;
        }

        public String getValue() {
            return value;
        }
    }

    record Start(Coordinates position) implements Token
    {
        @Override
        public String toString() {
            return "Start";
        }

        public String getValue() {
            return "Start";
        }
    }

    record End(Coordinates position) implements Token
    {
        @Override
        public String toString() {
            return "End";
        }

        public String getValue() {
            return "End";
        }
    }

    record NewLine(Coordinates position) implements Token
    {
        @Override
        public String toString() {
            return "NewLine";
        }

        public String getValue() {
            return "NewLine";
        }
    }

    record Invalid(Coordinates position, String value) implements Token
    {
        @Override
        public String toString() {
            return "Invalid: " + value;
        }

        public String getValue() {
            return value;
        }
    }

    default Coordinates getPosition()
    {
        return switch (this)
        {
            case Keyword keyword -> keyword.position();
            case Delimiter delimiter -> delimiter.position();
            case Operator operator -> operator.position();
            case Literal literal -> literal.position();
            case Identifier identifier -> identifier.position();
            case Comment comment -> comment.position();
            case Start start -> start.position();
            case End end -> end.position();
            case NewLine newLine -> newLine.position();
            case Invalid invalid -> invalid.position();
        };
    }

    String getValue();
}
