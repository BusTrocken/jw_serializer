package de.serializer;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class JwDeserializer<T> {

    private static final Map<Class<?>, Function<byte[], ?>> converterMap = ByteConverter.getConverterMap();

    private final Class<T> clazz;
    private int index = 4;

    public JwDeserializer(@NotNull Class<T> clazz) {
        this.clazz = clazz;
    }

    public T deserialize(@NotNull String fileName) {
        File file = new File(fileName);
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] bytes = fis.readAllBytes();
            return readObject(bytes);
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
            return createInstance(fieldsAndValues);
        } catch (IllegalAccessException e) {
            throw new RuntimeException();
        } catch (NoSuchFieldException | NoSuchMethodException | InvocationTargetException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    private T createInstance(Map<String, Object> fieldsAndValues) throws IllegalAccessException, NoSuchFieldException, NoSuchMethodException, InvocationTargetException, InstantiationException {
        T instance = createEmptyInstance();

        for (Field field : clazz.getDeclaredFields()) {
            setField(fieldsAndValues, field, instance, "");
        }

        return instance;
    }

    private Object setField(Map<String, Object> fieldsAndValues, Field field, Object instance, String parentFieldName) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException {
        field.setAccessible(true);
        String currentFieldName = (!Objects.equals(parentFieldName, "")) ? parentFieldName + "." + field.getName() : field.getName();
        if (SerializableTypes.isSerializableType(field.getType())) {
            Object value = fieldsAndValues.get(currentFieldName);
            field.set(instance, value);
        } else {
            Constructor<?> constructor = field.getType().getDeclaredConstructor();
            constructor.setAccessible(true);
            Object nestedInstance = constructor.newInstance();
            for (Field nestedField : field.getType().getDeclaredFields()) {
                field.set(instance,
                        setField(fieldsAndValues,
                                nestedField,
                                nestedInstance,
                                currentFieldName));
            }
            return instance;
        }
        return instance;
    }

    private T createEmptyInstance() {
        try {
            Constructor<T> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new RuntimeException();
        }
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
}
