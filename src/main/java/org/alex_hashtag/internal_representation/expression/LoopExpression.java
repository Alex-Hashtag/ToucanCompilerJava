package org.alex_hashtag.internal_representation.expression;

import lombok.Getter;
import org.alex_hashtag.internal_representation.types.Type;
import org.alex_hashtag.internal_representation.types.TypeRegistry;
import org.alex_hashtag.tokenizationOLD.CoordinatesOLD;

import java.util.List;
import java.util.Optional;

public class LoopExpression implements Expression {
    @Getter
    private final CoordinatesOLD location;

    // If present, we have a loop with an iteration count (i.e. loop(x) { ... }).
    // If empty, it is simply a loop (i.e. loop { ... }).
    private final Optional<Expression> numberOfIterations;

    private final List<Expression> statements;

    @Getter
    private final boolean brackets;

    /**
     * Constructor for a loop without an iteration count (e.g. "loop { ... }").
     *
     * @param location   The source code location.
     * @param statements The loop’s body.
     * @param brackets   Whether the curly brackets were explicitly written.
     */
    public LoopExpression(CoordinatesOLD location, List<Expression> statements, boolean brackets) {
        this.location = location;
        this.statements = statements;
        this.brackets = brackets;
        this.numberOfIterations = Optional.empty();
    }

    /**
     * Constructor for a loop with an iteration count (e.g. "loop(x) { ... }").
     *
     * @param numberOfIterations The expression for the number of iterations.
     * @param statements         The loop’s body.
     * @param location           The source code location.
     * @param brackets           Whether the curly brackets were explicitly written.
     */
    public LoopExpression(Expression numberOfIterations, List<Expression> statements, CoordinatesOLD location, boolean brackets) {
        this.location = location;
        this.statements = statements;
        this.brackets = brackets;
        this.numberOfIterations = Optional.of(numberOfIterations);
    }

    @Override
    public Optional<Type> getType() {
        return TypeRegistry.searchByName("void");
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("loop");

        // If we have an iteration count, print it.
        numberOfIterations.ifPresent(expr -> sb.append("(")
                .append(expr.toString())
                .append(")"));

        // Now print the statements.
        if (!statements.isEmpty()) {
            if (brackets) {
                sb.append(" {\n");
                for (Expression statement : statements) {
                    sb.append(statement.toString()).append(";\n");
                }
                sb.append("}");
            } else {
                // If no brackets, we assume a single (or inline) statement.
                sb.append(" ");
                for (Expression statement : statements) {
                    sb.append(statement.toString()).append("; ");
                }
            }
        }
        return sb.toString();
    }
}
