package de.serializer;

import java.util.List;

public record Customer(String firstName, String lastName, int age, Address address){}