package org.alex_hashtag.internal_representation.function;

import org.alex_hashtag.internal_representation.expression.Expression;
import org.alex_hashtag.internal_representation.expression.VariableDeclarationExpression;
import org.alex_hashtag.internal_representation.macros.Annotatable;
import org.alex_hashtag.internal_representation.macros.Annotation;
import org.alex_hashtag.internal_representation.types.Generic;
import org.alex_hashtag.internal_representation.types.Type;
import org.alex_hashtag.internal_representation.types.TypeRegistry;

import java.util.*;


/**
 * Represents a function definition, which includes information about annotations, properties,
 * generic arguments, type, arguments, and the function body.
 * This class implements the interfaces {@link Generic} and {@link Annotatable}, adhering to their
 * contract methods for managing generic arguments and annotations respectively.
 */
public class Function implements Generic, Annotatable
{
    /**
     * A set of annotations applied to the function, representing metadata or additional
     * information about the function's behavior or properties.
     * This set is managed through the {@link Annotatable} interface, which provides access
     * to annotation data associated with the function.
     */
    Set<Annotation> annotations = new HashSet<>();
    /**
     * A map of properties associated with a function definition.
     * The keys are property names, and the values are the corresponding property values.
     * Some default properties may include:
     * - "inline": indicates whether the function should be inlined (e.g., "true" or "false").
     * - "visibility": defines the function's visibility level (e.g., "public", "private", "protected").
     * - "mutability": specifies the mutability of the function (e.g., "mutable", "immutable", "const", "static", "abstract").
     * - "constructor": indicates if the function is a constructor (e.g., "true" or "false").
     * - "overrides": specifies if the function overrides another (e.g., "true" or "false").
     * - "unsafe": marks the function as unsafe (e.g., "true" or "false").
     *
     * These properties are used to provide metadata about the function and its behavior.
     */
    Map<String, String> properties = new HashMap<>();
    /**
     * A list containing the generic arguments associated with the definition of a function or type.
     * Each generic argument is represented by a {@link VariableDeclarationExpression}, which contains
     * details such as the identifier, type, and mutability of the argument.
     *
     * The generic arguments define type parameters that can be used within the context of the function
     * or type where this list is maintained. If no generic arguments are provided, the list remains empty.
     */
    List<VariableDeclarationExpression> genericArguments = new ArrayList<>();
    /**
     * Represents the return type of the function as a string.
     * The value may correspond to a primitive type, custom type, or a reference to
     * a type retrieved from the {@link TypeRegistry}.
     * This property can be used to define or infer the function's output or behavior in context.
     */
    String type;
    /**
     * Holds a list of arguments for the function, represented as instances of
     * {@link VariableDeclarationExpression}. Each argument typically represents
     * a variable declaration with associated type, name, and mutability.
     */
    List<VariableDeclarationExpression> arguments = new ArrayList<>();
    /**
     * Represents the body of the function, consisting of a list of expressions.
     * Each expression in the body encapsulates an executable or evaluatable construct,
     * such as variable declarations, function calls, or control flow statements.
     * This list defines the sequential operations that constitute the logic of the function.
     */
    List<Expression> body;

    @Override
    public Map<String, String> getProperties()
    {
        return properties;
    }

    /**
     * Provides a map of default settings where the key represents a specific property and the value
     * is a list of possible default options for that property.
     *
     * @return a map containing property names as keys and lists of corresponding default values as values.
     */
    @Override
    public Map<String, List<String>> getDefaults()
    {
        return Map.of("inline", List.of("true", "false"),
                "visibility", List.of("public", "private", "protected"),
                "mutability", List.of("mutable", "immutable", "const", "static", "abstract"),
                "constructor", List.of("true", "false"),
                "overrides", List.of("true", "false"),
                "unsafe", List.of("true", "false"));
    }

    @Override
    public Optional<Set<Annotation>> getAnnotations()
    {
        return annotations.isEmpty() ? Optional.empty() : Optional.of(annotations);
    }

    @Override
    public Optional<List<VariableDeclarationExpression>> getGenericArguments()
    {
        return genericArguments.isEmpty() ? Optional.empty() : Optional.of(genericArguments);
    }

    public Optional<Type> getType()
    {
        return TypeRegistry.searchByName(type);
    }
}
