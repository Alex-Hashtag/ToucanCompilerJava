package org.alex_hashtag.internal_representation.literals;

import lombok.Getter;
import lombok.Setter;
import org.alex_hashtag.internal_representation.types.Type;
import org.alex_hashtag.internal_representation.types.TypeRegistry;
import org.alex_hashtag.tokenizationOLD.CoordinatesOLD;

import java.util.Optional;

@Getter
@Setter
public class IntegerLiteral implements Literal {
    private CoordinatesOLD location;
    private byte sizeInBytes;
    private boolean unsigned;
    private String internal;

    @Override
    public Optional<Type> getType() {
        StringBuilder typeName = new StringBuilder("int");
        if (unsigned) {
            typeName.insert(0, "u");
        }
        typeName.append(sizeInBytes * 8);
        return TypeRegistry.searchByName(typeName.toString());
    }
}
