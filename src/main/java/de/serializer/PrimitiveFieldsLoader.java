package de.serializer;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.BiConsumer;

public class PrimitiveFieldsLoader {

    private static final List<Class<?>> SERIALIZABLE_TYPES = Arrays.asList(byte.class,
            Byte.class,
            short.class,
            Short.class,
            int.class,
            Integer.class,
            long.class,
            Long.class,
            float.class,
            Float.class,
            double.class,
            Double.class,
            boolean.class,
            Boolean.class,
            char.class,
            Character.class,
            String.class);

    private final Map<String, Object> values;

    public PrimitiveFieldsLoader() {
        values = new HashMap<>();
    }

    public Map<String, Object> load(Object o) throws IllegalAccessException {
        getFlatFields(o, "", (name, fieldValue, field) -> {
            try {
                values.put(name, field.get(fieldValue));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
        return values;
    }

    private void getFlatFields(Object value, String parentName, TriConsumer<String, Object, Field> function) throws IllegalAccessException {
        Field[] firstLevelFields = value.getClass().getDeclaredFields();
        for (Field field : firstLevelFields) {
            forAllFields(value, field, parentName, function);
        }
    }

    private void forAllFields(Object value, Field field, String parentName, TriConsumer<String, Object, Field> function) throws IllegalAccessException {
        String fieldName = parentName.isEmpty() ? field.getName() : parentName + "." + field.getName();
        if (Collection.class.isAssignableFrom(field.getType())) {
            throw new IllegalArgumentException(fieldName + ", " + field.getType() + ", " + value);
        }
        field.setAccessible(true);
        if (!SERIALIZABLE_TYPES.contains(field.getType())) {
            getFlatFields(field.get(value), fieldName, function);
        } else {
            function.accept(fieldName, value, field);
        }
    }
}
