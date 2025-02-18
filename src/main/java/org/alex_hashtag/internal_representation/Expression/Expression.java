package org.alex_hashtag.internal_representation.Expression;

import org.alex_hashtag.internal_representation.types.TypeHolder;
import org.alex_hashtag.lib.results.Option;
import org.alex_hashtag.lib.tokenization.Coordinates;
import org.alex_hashtag.lib.tokenization.Token;

import java.util.List;


public sealed interface Expression
{
    record Empty(Coordinates coordinates) implements Expression {}
    record Binary(Coordinates coordinates, Expression left, Expression right, Token.Operator operator) implements Expression {}
    record If(Coordinates coordinates, Expression condition, List<Expression> statements, Option<If> elseExpression) implements Expression {}
    record Loop(Coordinates coordinates, Option<Expression> numberOfIteration, List<Expression> statements) implements Expression {}
    record While(Coordinates coordinated, Expression condition, List<Expression> statements) implements Expression {}
    record doWhile(Coordinates coordinated, List<Expression> statements, Expression condition) implements Expression {}
    record For(Coordinates coordinates, ArgsList init, ArgsList conditions, ArgsList updates) implements Expression {}
    record ForEach(Coordinates coordinates, Expression item, Expression list) implements Expression {}

    record Break(Coordinates coordinates) implements Expression {}
    record Continue(Coordinates coordinates) implements Expression {}

    record Return(Coordinates coordinates, Expression expr) implements Expression {}
    record Yield(Coordinates coordinates, Expression expr) implements Expression {}

    record Sizeof(Coordinates coordinates, Expression expr) implements Expression {}
    record Typeof(Coordinates coordinates, Expression expr) implements Expression {}

    record Scope(Coordinates coordinates, List<Expression> statements) implements Expression {}
    record Unsafe(Coordinates coordinates, List<Expression> statements) implements Expression {}

    record InstanceAccessStruct(Coordinates coordinates, Expression variable, String index) implements Expression {}
    record InstanceAccessEnum(Coordinates coordinates, Expression variable, int index) implements Expression {}
    record InstanceAccessArray(Coordinates coordinates, Expression variable, int index) implements Expression {}
    record StaticAccessLambda(Coordinates coordinates, Expression variable, String index) implements Expression {}

    record FunctionInvocation(Coordinates coordinates, Expression expression, ArgsList arguments) implements Expression {}
    record AnonFunctionLamnda(Coordinates coordinates, ArgsList arguments, List<Expression> statements) implements Expression {}

    record IdentifierVar(Coordinates coordinates, String name) implements Expression {}
    record IdentifierType(Coordinates coordinates, String name) implements Expression {}
    record IdentifierFunc(Coordinates coordinates, String name) implements Expression {}

    record VariableDeclaration(Coordinates coordinates, IdentifierType type, String name) implements Expression {}
    record VariableAssigment(Coordinates coordinates, String name, Expression assignTo) implements Expression {}
    record VariableDeclarationAssigment(Coordinates coordinates, IdentifierType type, String name, Expression assignTo) implements Expression {}

    sealed interface Literal extends Expression
    {
        record Int(Coordinates coordinates, java.lang.String value) implements Literal {}
        record Float(Coordinates coordinates, java.lang.String value) implements Literal {}
        record Char(Coordinates coordinates, java.lang.String value) implements Literal {}
        record Rune(Coordinates coordinates, java.lang.String value) implements Literal {}
        record String(Coordinates coordinates, java.lang.String value) implements Literal {}
        record Array(Coordinates coordinates, IdentifierType type, Expression size, List<Expression> elements) implements Literal {}
        record Struct(Coordinates coordinates, IdentifierType type, List<VariableAssigment> assigments) implements Literal {}
        record Enum(Coordinates coordinates, IdentifierType type, java.lang.String variant, ArgsList arguments) implements Literal {}

        //! PROPER RETURN TYPE EVALUATION WILL BE SAVED FOR SEMATIC ANALYSIS PHASE
        default TypeHolder getType()
        {
            return switch (this)
            {
                case Int(Coordinates coordinates, _) -> new TypeHolder.Resolved(coordinates, "int64");
                case Float(Coordinates coordinates, _) -> new TypeHolder.Resolved(coordinates, "float64");
                case Char(Coordinates coordinates, _) -> new TypeHolder.Resolved(coordinates, "char");
                case Rune(Coordinates coordinates, _) -> new TypeHolder.Resolved(coordinates, "rune");
                case String(Coordinates coordinates, _) -> new TypeHolder.Resolved(coordinates, "string");
                case Literal ignored -> new TypeHolder() {};
            };
        }
    }


    record ArgsList(Coordinates coordinates, List<Expression> arguments) implements Expression {}



    //! PROPER RETURN TYPE EVALUATION WILL BE SAVED FOR SEMATIC ANALYSIS PHASE
    default TypeHolder getType()
    {
        return switch (this)
        {
            case Empty(Coordinates coordinates) -> new TypeHolder.Resolved(coordinates, "void");
            case Binary(Coordinates coordinates, Expression left, Expression right, Token.Operator operator) -> {
                TypeHolder leftType = left.getType();
                TypeHolder rightType = right.getType();
                if (!(leftType instanceof TypeHolder.Resolved))
                    yield  leftType;
                if (!(rightType instanceof TypeHolder.Resolved))
                    yield  rightType;

                if (((TypeHolder.Resolved) leftType).name() != ((TypeHolder.Resolved) rightType).name())
                    yield new TypeHolder.IncompatibleTypesInBinaryExpression(coordinates, ((TypeHolder.Resolved) leftType).name(), ((TypeHolder.Resolved) rightType).name());

                throw new IllegalStateException("Unexpected value: " + this);
                
            }
            default -> throw new IllegalStateException("Unexpected value: " + this);
        };
    }
}
