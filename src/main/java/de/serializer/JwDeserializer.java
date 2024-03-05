package de.serializer;

import org.jetbrains.annotations.NotNull;
import sun.reflect.ReflectionFactory;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class JwDeserializer<T> {

    private static final Map<Class<?>, Function<byte[], ?>> converterMap = ByteConverter.getConverterMap();

    private final Class<T> type;
    private int index = 4;

    public JwDeserializer(@NotNull Class<T> type) {
        this.type = type;
    }

    public void deserialize(@NotNull String fileName) {
        File file = new File(fileName);
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] bytes = new byte[(int) file.length()];
            fis.read(bytes);
            T object = readObject(bytes);
        } catch (IOException e) {
            throw new IllegalArgumentException(String.format("could not find file " + fileName));
        }
    }

    private T readObject(byte[] bytes) {
        Map<String, Object> fieldsAndValues = new HashMap<>();
        while (index < bytes.length) {
            int fieldNameLength = fourBytesToInt(bytes);
            String fieldName = bytesToString(bytes, fieldNameLength);
            Class<?> clazz = SerializableTypes.getClassByOrderNumber(getNBytes(bytes, 1)[0]);
            if (clazz == String.class) {
                int stringLength = fourBytesToInt(bytes);
                fieldsAndValues.put(fieldName, bytesToString(bytes, stringLength));
            } else {
                fieldsAndValues.put(fieldName, readValue(bytes, clazz));
            }
        }
        try {
            T instance = createInstance(fieldsAndValues);
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                 IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private T createInstance(Map<String, Object> fieldsAndValues) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        String[][] fields = new String[fieldsAndValues.size()][];
        for (int i = 0; i < fieldsAndValues.size(); i++) {
            fields[i] = getNameByIndex(fieldsAndValues, i).split("\\.");
        }


        ReflectionFactory rf = ReflectionFactory.getReflectionFactory();
        Constructor objDef = type.getDeclaredConstructor();
        Constructor intConstr = rf.newConstructorForSerialization(type, objDef);
        type.cast(intConstr.newInstance());
        return null;
    }

    private Object readValue(byte[] bytes, Class<?> clazz) {
        byte[] valueBytes = getNBytes(bytes, SerializableTypes.getSizeByClass(clazz));
        return converterMap.get(clazz).apply(valueBytes);
    }

    private String bytesToString(byte[] bytes, int fieldNameLength) {
        byte[] stringBytes = getNBytes(bytes, fieldNameLength);
        return new String(stringBytes, StandardCharsets.UTF_8);
    }

    private int fourBytesToInt(byte[] bytes) {
        byte[] fourBytes = getNBytes(bytes, 4);
        return ByteConverter.toInt(fourBytes);
    }

    private @NotNull byte[] getNBytes(byte[] bytes, int byteCount) {
        byte[] slicedBytes = new byte[byteCount];
        for (int i = 0; i < slicedBytes.length; i++) {
            slicedBytes[i] = bytes[index++];
        }
        return slicedBytes;
    }

    private @NotNull String getNameByIndex(@NotNull Map<String, Object> fieldsAndValues, int index) {
        return fieldsAndValues.keySet().toArray(new String[0])[index];
    }
}
