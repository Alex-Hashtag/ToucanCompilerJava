package org.alex_hashtag.internal_representation.expression;

import lombok.Getter;
import org.alex_hashtag.internal_representation.types.Type;
import org.alex_hashtag.internal_representation.types.TypeRegistry;
import org.alex_hashtag.tokenization.Coordinates;

import java.util.Optional;


/**
 * Represents an arithmetic expression in the form of a binary operation
 * involving a left operand, an operator, and a right operand. This class
 * implements the {@code Expression} interface, enabling retrieval of the
 * type information for the expression.
 */
public class BinaryExpression implements Expression

{
    //try me bitch, 28 was here
    @Getter
    Coordinates location;
    String type;
    Expression left;
    Expression right;
    Operator operator;

    @Override
    public Optional<Type> getType()
    {
        return TypeRegistry.searchByName(type);
    }

    @Override
    public String toString()
    {
        return "(" + left.toString() + " " + operator.getOperator() + " " + right.toString() + ")";
    }
}