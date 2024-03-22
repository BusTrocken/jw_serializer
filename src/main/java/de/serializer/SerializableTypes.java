package de.serializer;

import lombok.Value;
import org.jetbrains.annotations.NotNull;

public enum SerializableTypes {
    BOOLEAN(new PrimitiveWithWrapper(boolean.class, Boolean.class), 1),
    BYTE(new PrimitiveWithWrapper(byte.class, Byte.class), 1),
    SHORT(new PrimitiveWithWrapper(short.class, Short.class), 2),
    CHARACTER(new PrimitiveWithWrapper(char.class, Character.class), 1),
    INTEGER(new PrimitiveWithWrapper(int.class, Integer.class), 4),
    LONG(new PrimitiveWithWrapper(long.class, Long.class), 8),
    FLOAT(new PrimitiveWithWrapper(float.class, Float.class), 4),
    DOUBLE(new PrimitiveWithWrapper(double.class, Double.class), 8),
    STRING(new PrimitiveWithWrapper(String.class, String.class), 0);

    private final PrimitiveWithWrapper clazz;
    private final int size;

    SerializableTypes(PrimitiveWithWrapper clazz, int size) {
        this.clazz = clazz;
        this.size = size;
    }

    public static boolean isSerializableType(@NotNull Class<?> clazz) {
        for (SerializableTypes type : SerializableTypes.values()) {
            if (type.clazz.primitive == clazz || type.clazz.wrapper == clazz) {
                return true;
            }
        }
        return false;
    }

    public static SerializableTypes fromOrderNumber(int orderNumber) {
        if (orderNumber >= 0 && orderNumber < SerializableTypes.values().length) {
            return SerializableTypes.values()[orderNumber];
        }

        throw new IllegalArgumentException("No enum found for order number: " + orderNumber);
    }

    public static int getSizeByClass(Class<?> clazz) {
        for (SerializableTypes type : SerializableTypes.values()) {
            if (type.clazz.primitive == clazz || type.clazz.wrapper == clazz) {
                return type.size;
            }
        }
        throw new IllegalArgumentException("No enum found for order number: " + clazz.toString());
    }

    public static Class<?> getClassByOrderNumber(int orderNumber) {
        if (orderNumber >= 0 && orderNumber < SerializableTypes.values().length) {
            return SerializableTypes.values()[orderNumber].clazz.wrapper;
        }

        throw new IllegalArgumentException("No enum found for order number: " + orderNumber);
    }

    public static SerializableTypes fromClass(Class<?> clazz) {
        for (SerializableTypes type : SerializableTypes.values()) {
            if (type.clazz.primitive == clazz || type.clazz.wrapper == clazz) {
                return type;
            }
        }
        throw new IllegalArgumentException("No enum found for class: " + clazz);
    }

    private record PrimitiveWithWrapper(Class<?> primitive, Class<?> wrapper) {}
}