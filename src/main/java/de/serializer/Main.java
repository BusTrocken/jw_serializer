package de.serializer;

import java.io.IOException;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException, IllegalAccessException {
 /*       Street street = new Street("straße", 2.4f);
        Address address = new Address(street, "stadt");
        Customer customer = new Customer("Jonas", "Würzner", 22, address);
        JwSerializer serializer = new JwSerializer();
        serializer.serialize(customer); */

    //    new JwSerializer().serialize(new SimpleCustomer("Jonas", 22));
        new JwDeserializer(Customer.class).deserialize("22.bin");
    }

    private record SimpleCustomer(String name, int age) {}
}