package de.serializer;

import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException, IllegalAccessException {
        Street street = new Street("straße", 2);
        Address address = new Address(street, 3);
        Customer customer = new Customer("Jonas", "Würzner", 22, address);
        JwSerializer<Customer> serializer = new JwSerializer<>();
        serializer.serialize(customer);
    }
}