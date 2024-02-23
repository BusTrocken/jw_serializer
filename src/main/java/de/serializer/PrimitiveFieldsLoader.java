package de.serializer;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.*;

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

    public PrimitiveFieldsLoader(Object o) throws IllegalAccessException {
        values = new HashMap<>();
        getFlatFields(o, "");
    }

    public Map<String, Object> load() {
        return values;
    }


    private void getFlatFields(Object value, String parentName) throws IllegalAccessException {
        Field[] firstLevelFields = value.getClass().getDeclaredFields();
        for (Field field : firstLevelFields) {
            fillMap(value, field, parentName);
        }
    }

    private void fillMap(Object value, Field field, String parentName) throws IllegalAccessException {
        String fieldName = parentName.isEmpty() ? field.getName() : parentName + "." + field.getName();
        if (Collection.class.isAssignableFrom(field.getType())) {
            throw new IllegalArgumentException(fieldName + ", " + field.getType() + ", " + value);
        }
        field.setAccessible(true);
        if (!SERIALIZABLE_TYPES.contains(field.getType())) {
            getFlatFields(field.get(value), fieldName);
        } else {
            values.put(fieldName, field.get(value));
        }
    }

    private Object[] getAsArray(Field field, Object value) throws IllegalAccessException {
        if (List.class.isAssignableFrom(field.getType())) {
            field.setAccessible(true);
            return ((List<?>)field.get(value)).toArray();
        }
        return null;
    }
}
