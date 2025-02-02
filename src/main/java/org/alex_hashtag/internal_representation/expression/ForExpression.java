package org.alex_hashtag.internal_representation.expression;

import lombok.Getter;
import org.alex_hashtag.internal_representation.types.Type;
import org.alex_hashtag.internal_representation.types.TypeRegistry;
import org.alex_hashtag.tokenization.Coordinates;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ForExpression implements Expression {

    public enum LoopType {
        STANDARD,
        FOREACH
    }

    @Getter
    private Coordinates location;

    private final LoopType type;

    // Fields for standard for-loop
    @Getter
    private List<Expression> initialisations;
    @Getter
    private Expression condition;
    @Getter
    private List<Expression> updates;

    // Fields for foreach loop
    @Getter
    private VariableDeclarationExpression element;
    @Getter
    private Expression list;

    @Getter
    private List<Expression> statements = new ArrayList<>();

    private final boolean brackets;

    // Constructor for standard for-loop
    public ForExpression(
            List<Expression> initialisations,
            Expression condition,
            List<Expression> updates,
            Coordinates location,
            boolean hasBrackets
    ) {
        this.type = LoopType.STANDARD;
        this.initialisations = initialisations;
        this.condition = condition;
        this.updates = updates;
        this.location = location;
        this.brackets = hasBrackets;
    }

    // Constructor for foreach loop
    public ForExpression(
            VariableDeclarationExpression element,
            Expression list,
            Coordinates location,
            boolean hasBrackets
    ) {
        this.type = LoopType.FOREACH;
        this.element = element;
        this.list = list;
        this.location = location;
        this.brackets = hasBrackets;
    }

    @Override
    public Optional<Type> getType() {
        return TypeRegistry.searchByName("void");
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (type == LoopType.STANDARD) {
            sb.append("for (");
            for (int i = 0; i < initialisations.size(); i++) {
                sb.append(initialisations.get(i));
                if (i < initialisations.size() - 1)
                    sb.append(", ");
            }

            sb.append("; ");

            sb.append(condition);

            sb.append("; ");

            for (int i = 0; i < updates.size(); i++) {
                sb.append(updates.get(i));
                if (i < updates.size() - 1)
                    sb.append(", ");
            }
        } else if (type == LoopType.FOREACH) {
            sb.append("for (").append(element).append(" : ").append(list).append(")");
        }

        if (brackets)
            sb.append(" {\n");
        else
            sb.append(" ");

        for (Expression statement : statements) {
            if (type == LoopType.FOREACH) {
                sb.append("    ");
            }
            sb.append(statement.toString());
            sb.append(";"); // Add a semicolon to the end of each statement
            sb.append("\n");
        }
        if (brackets)
            sb.append("}\n");
        return sb.toString();
    }

    // Getters for all fields (if needed)
    public LoopType getTypeOfLoop() {
        return type;
    }

    public boolean hasBrackets() {
        return brackets;
    }
}
