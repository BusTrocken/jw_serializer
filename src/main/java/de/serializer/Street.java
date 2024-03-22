package de.serializer;

import lombok.Getter;

@Getter
public class Street {
    private String name;
    private int houseNumber;

    public Street(String name, int houseNumber) {
        this.name = name;
        this.houseNumber = houseNumber;
    }

    public Street() {}
}