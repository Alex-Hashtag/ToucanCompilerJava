package org.alex_hashtag.internal_representation.literals;

import lombok.Getter;
import lombok.Setter;
import org.alex_hashtag.internal_representation.types.Type;
import org.alex_hashtag.internal_representation.types.TypeRegistry;
import org.alex_hashtag.tokenizationOLD.CoordinatesOLD;

import java.util.Optional;

@Getter
@Setter
public class FloatLiteral implements Literal {
    private CoordinatesOLD location;
    private byte sizeInBytes;
    private String internal;

    @Override
    public Optional<Type> getType() {
        return TypeRegistry.searchByName("float" + (sizeInBytes * 8));
    }
}
