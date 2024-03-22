package de.serializer.jk

import de.serializer.Address
import de.serializer.Customer
import de.serializer.Street
import java.io.File

fun main() {
    val customer = Customer(
        "peddar",
        "lümmeln",
        23,
        Address(
            Street("puff-aleé", 20),
            "Canne"
        )
    )

    val file = File(System.getProperty("user.home"), "/Desktop/test.data")

    JkSerializer().serialize(file, customer)

    val readCustomer = JkDeserializer().deserialize<Customer>(file, Customer::class.java)

    println(readCustomer)
}
