package de.serializer;

import lombok.Getter;

@Getter
public class Street {
    private String name;
    private float houseNumber;

    public Street(String name, float houseNumber) {
        this.name = name;
        this.houseNumber = houseNumber;
    }

    public Street() {}
}