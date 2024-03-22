package de.serializer;

import lombok.Getter;

@Getter
public class Address {
    private Street street;
    private String city;

    public Address(Street street, String city) {
        this.street = street;
        this.city = city;
    }

    private Address() {}
}