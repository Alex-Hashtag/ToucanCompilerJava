package org.alex_hashtag.internal_representation.utils;

record Err<T, E>(E error) implements Result<T, E> {}