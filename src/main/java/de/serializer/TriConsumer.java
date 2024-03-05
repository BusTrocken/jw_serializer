package de.serializer;

public interface TriConsumer<A, B, C> {
    void accept(A a, B b, C c);
}
