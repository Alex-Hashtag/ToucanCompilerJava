package org.alex_hashtag;

import com.electronwill.nightconfig.core.file.FileConfig;
import org.alex_hashtag.tokenization.TokenStream;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Scanner;


public class Main
{
    public static void main(String[] args) {
        // Example Toucan code snippet
        String toucanCode = """
            annotation Getter(String field_name) {
                public void apply(mutable Class cl) {
                    Variable variable = switch (cl.getFields().stream().find(x -> x.getVariable().getName().equals(field_name))) {
                        NONE -> ErrorManager.panic("There is no field with the name " + field_name + " in class " + cl.getName());
                        SOME(a) -> a;
                    };
                    Method method = Method.new("get" + field_name.toPascalCase(), variable.getType(), List.empty());
                    Method.addStatement(Statement.parse("return this." + field_name + ";"));
                    cl.addMethod(method);
                }
            }

            @Getter("example")
            public class Example {
                private int example;
            }

            macro sum {
                (expression $x) -> {
                    yield $x;
                };
                (expression $x, $(expression $rest),+) -> {
                    $x + sum!($($rest),+);
                };
            }

            void main() {
                let result = sum!(1, 2, 3, 4, 5); // Output: 15
                println("The sum is: " + result);
            }
        """;

        // Initialize the TokenStream with the Toucan code
        TokenStream tokenStream = new TokenStream(toucanCode);

        // Print all tokens to verify correct tokenization
        System.out.println("Tokenized output:");
        tokenStream.printTokens();
    }
}