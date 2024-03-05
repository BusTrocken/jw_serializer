package de.serializer;

public enum SerializableTypes {
    BOOLEAN(Boolean.class, 1),
    BYTE(Byte.class, 1),
    SHORT(Short.class, 2),
    CHARACTER(Character.class, 1),
    INTEGER(Integer.class, 4),
    LONG(Long.class, 8),
    FLOAT(Float.class, 4),
    DOUBLE(Double.class, 8),
    STRING(String.class, 0);

    private final Class<?> clazz;
    private final int size;

    SerializableTypes(Class<?> clazz, int size) {
        this.clazz = clazz;
        this.size = size;
    }

    public static SerializableTypes fromOrderNumber(int orderNumber) {
        if (orderNumber >= 0 && orderNumber < SerializableTypes.values().length) {
            return SerializableTypes.values()[orderNumber];
        }

        throw new IllegalArgumentException("No enum found for order number: " + orderNumber);
    }

    public static int getSizeByClass(Class<?> clazz) {
        for (SerializableTypes type : SerializableTypes.values()) {
            if (type.clazz == clazz) {
                return type.size;
            }
        }
        throw new IllegalArgumentException("No enum found for order number: " + clazz.toString());
    }

    public static Class<?> getClassByOrderNumber(int orderNumber) {
        if (orderNumber >= 0 && orderNumber < SerializableTypes.values().length) {
            return SerializableTypes.values()[orderNumber].clazz;
        }

        throw new IllegalArgumentException("No enum found for order number: " + orderNumber);
    }

    public static SerializableTypes fromClass(Class<?> clazz) {
        for (SerializableTypes type : SerializableTypes.values()) {
            if (type.clazz.equals(clazz)) {
                return type;
            }
        }
        throw new IllegalArgumentException("No enum found for class: " + clazz);
    }
}