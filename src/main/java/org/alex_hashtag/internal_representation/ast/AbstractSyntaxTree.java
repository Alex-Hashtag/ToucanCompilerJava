package org.alex_hashtag.internal_representation.ast;

import org.alex_hashtag.internal_representation.function.Function;
import org.alex_hashtag.internal_representation.macros.Annotation;
import org.alex_hashtag.internal_representation.macros.Macro;
import org.alex_hashtag.internal_representation.macros.MacroParser;
import org.alex_hashtag.internal_representation.types.Field;
import org.alex_hashtag.internal_representation.types.Type;
import org.alex_hashtag.tokenizationOLD.TokenStream;

import java.util.ArrayList;
import java.util.List;


public class AbstractSyntaxTree
{

    private final List<Macro> macros = new ArrayList<>();
    private final List<Annotation> annotations = new ArrayList<>();
    private final List<Type> types = new ArrayList<>();
    private final List<Function> functions = new ArrayList<>();
    private final List<Field> fields = new ArrayList<>();

    public AbstractSyntaxTree(List<TokenStream> tokenStreams)
    {
        // Use the MacroParser to parse macros
        MacroParser macroParser = new MacroParser();
        this.macros.addAll(macroParser.parseAllMacros(tokenStreams));


        // initAnnotations(tokenStreams);
        // initTypes(tokenStreams);
        // initFunctions(tokenStreams);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("AbstractSyntaxTree {\n");

        // Macros
        sb.append("  Macros:\n");
        for (Macro macro : macros)
            sb.append("    ").append(macro).append("\n");

        // Annotations
        sb.append("  Annotations:\n");
        for (Annotation annotation : annotations)
            sb.append("    ").append(annotation).append("\n");

        // Types
        sb.append("  Types:\n");
        for (Type type : types)
            sb.append("    ").append(type).append("\n");

        // Functions
        sb.append("  Functions:\n");
        for (Function function : functions)
            sb.append("    ").append(function).append("\n");

        sb.append("}");
        return sb.toString();
    }
}
