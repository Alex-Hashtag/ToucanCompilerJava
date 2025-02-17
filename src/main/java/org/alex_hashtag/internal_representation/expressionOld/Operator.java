package org.alex_hashtag.internal_representation.expressionOld;

import org.alex_hashtag.tokenizationOLD.Token;


public enum Operator
{
    PLUS("+"),
    MINUS("-"),
    MULTIPLY("*"),
    DIVIDE("/"),
    MODULO("%"),
    BITWISE_AND("&"),
    BITWISE_OR("|"),
    BITWISE_XOR("^"),
    BITWISE_NOT("~"),
    BIT_SHIFT_LEFT("<<"),
    BIT_SHIFT_RIGHT(">>"),
    BIT_SHIFT_RIGHT_UNSIGNED(">>>"),
    LOGICAL_AND("and"),
    LOGICAL_OR("or"),
    LOGICAL_NOT("!"),
    GREATER_THAN(">"),
    LESS_THAN("<"),
    GREATER_EQUAL(">="),
    LESS_EQUAL("<="),
    EQUAL_TO("=="),
    NOT_EQUAL_TO("!=");

    private final String operator;

    Operator(String c)
    {
        this.operator = c;
    }

    public String getOperator()
    {
        return operator;
    }

    public static Operator fromToken(Token token) {
        if (token == null) return null;

        return switch (token.type) {
            case ADDITION -> PLUS;
            case SUBTRACTION -> MINUS;
            case MULTIPLICATION -> MULTIPLY;
            case DIVISION -> DIVIDE;
            case MODULO -> MODULO;

            // Bitwise operators
            case BITWISE_AND -> BITWISE_AND;
            case BITWISE_OR -> BITWISE_OR;
            case BITWISE_XOR -> BITWISE_XOR;
            case BITWISE_NOT -> BITWISE_NOT;
            case BIT_SHIFT_LEFT -> BIT_SHIFT_LEFT;
            case BIT_SHIFT_RIGHT -> BIT_SHIFT_RIGHT;
            case BIT_SHIFT_RIGHT_UNSIGNED -> BIT_SHIFT_RIGHT_UNSIGNED;

            // Logical operators
            case LOGICAL_AND -> LOGICAL_AND;
            case LOGICAL_OR -> LOGICAL_OR;
            case LOGICAL_NOT -> LOGICAL_NOT;

            // Comparison operators
            case GREATER_THAN -> GREATER_THAN;
            case LESS_THAN -> LESS_THAN;
            case GREATER_THAN_OR_EQUAL -> GREATER_EQUAL;
            case LESS_THAN_OR_EQUAL -> LESS_EQUAL;
            case EQUALITY -> EQUAL_TO;
            case INEQUALITY -> NOT_EQUAL_TO;

            default -> null; // Return null if the token is not an operator
        };
    }

}
