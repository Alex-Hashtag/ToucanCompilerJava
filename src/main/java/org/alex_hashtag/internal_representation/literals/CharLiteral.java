package org.alex_hashtag.internal_representation.literals;

import lombok.Getter;
import lombok.Setter;
import org.alex_hashtag.internal_representation.types.Type;
import org.alex_hashtag.internal_representation.types.TypeRegistry;
import org.alex_hashtag.tokenization.Coordinates;

import java.util.Optional;

@Getter
@Setter
public class CharLiteral implements Literal {
    private Coordinates location;
    private String value;

    @Override
    public Optional<Type> getType() {
        return TypeRegistry.searchByName("char");
    }
}
