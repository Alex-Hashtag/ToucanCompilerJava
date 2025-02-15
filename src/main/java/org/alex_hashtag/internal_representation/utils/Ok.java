package org.alex_hashtag.internal_representation.utils;

record Ok<T, E>(T value) implements Result<T, E> {}
