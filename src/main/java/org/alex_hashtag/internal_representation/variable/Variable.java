package org.alex_hashtag.internal_representation.variable;

import org.alex_hashtag.internal_representation.types.Type;

import java.util.HashMap;

public class Variable
{
    private String name;
    private Type type;
    /**
     * A map defining the properties and their corresponding values for this variable.
     * These properties dictate the behavior and characteristics of the variable.
     *
     * <p><b>Default Properties:</b></p>
     * <ul>
     *   <li><b>local_mutability:</b> Determines if the variable is mutable or immutable in its local scope.
     *       Possible values: {@code "mutable"}, {@code "immutable"}.</li>
     *   <li><b>global_mutability:</b> Indicates the variable's mutability in a global context.
     *       Possible values: {@code "mutable"}, {@code "immutable"}, {@code "const"}.</li>
     * </ul>
     *
     * @see HashMap
     * @see org.alex_hashtag.internal_representation.types.Type
     */
    private HashMap<String, String> properties;

    public Variable(String name, Type type)
    {
        this.name = name;
        this.type = type;
    }
}
