package org.alex_hashtag.internal_representation.ast;


import org.alex_hashtag.internal_representation.function.Function;
import org.alex_hashtag.internal_representation.macros.Annotation;
import org.alex_hashtag.internal_representation.macros.Macro;
import org.alex_hashtag.internal_representation.types.Type;
import org.alex_hashtag.tokenization.TokenStream;

import java.util.List;


public class AbstractSyntaxTree
{
    List<Macro> macros;
    List<Annotation> annotations;
    List<Type> types;
    List<Function> functions;


    /**
     * First pass: Go through the token streams (of different files) and first get all the macros
     * Second pass: Populate annotations, types and functions (Macros will be processed accordingly)
     * Execute annotations on different types and functions
     * Populate the TypeRegistry
     * Check type constraints
     */
    public AbstractSyntaxTree(List<TokenStream> tokenStreams)
    {
    }
}
