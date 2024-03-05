package de.serializer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class JwSerializer {

    private BufferedOutputStream writer;

    public void serialize(Object value) throws IOException, IllegalAccessException {
        int magicNumber = MagicNumberProvider.getNextNumber();

        writer = new BufferedOutputStream(new FileOutputStream(magicNumber + ".bin"));

        byte[] magicNumberBytes = toByta(magicNumber);

        writer.write(magicNumberBytes);
        Map<String, Object> fields = new PrimitiveFieldsLoader().load(value);
        writeFieldBytes(fields);

        writer.close();
    }

    private void writeFieldBytes(Map<String, Object> fields) throws IOException {
        for (String name : fields.keySet()) {
            writeStringAndItsLength(name);

            Object value = fields.get(name);
            byte typeByte = getTypeByte(value);
            writer.write(typeByte);

            if (value.getClass() == String.class) {
                writeStringAndItsLength((String) value);
            } else {
                writer.write(toByta(value));
            }
        }
    }

    private void writeStringAndItsLength(String name) throws IOException {
        byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
        byte[] length = toByta(nameBytes.length);
        writer.write(length);
        writer.write(nameBytes);
    }

    private byte getTypeByte(Object o) {
        return (byte) SerializableTypes.fromClass(o.getClass()).ordinal();
    }

    public static byte[] toByta(Object object) {
        if (object.getClass() == Byte.class) {
            return ByteConverter.toByta((Byte) object);
        }
        if (object.getClass() == byte[].class) {
            return ByteConverter.toByta((byte[]) object);
        }
        if (object.getClass() == Short.class) {
            return ByteConverter.toByta((Short) object);
        }
        if (object.getClass() == short[].class) {
            return ByteConverter.toByta((short[]) object);
        }
        if (object.getClass() == Character.class) {
            return ByteConverter.toByta((Character) object);
        }
        if (object.getClass() == char[].class) {
            return ByteConverter.toByta((char[]) object);
        }
        if (object.getClass() == Integer.class) {
            return ByteConverter.toByta((Integer) object);
        }
        if (object.getClass() == int[].class) {
            return ByteConverter.toByta((int[]) object);
        }
        if (object.getClass() == Long.class) {
            return ByteConverter.toByta((Long) object);
        }
        if (object.getClass() == long[].class) {
            return ByteConverter.toByta((long[]) object);
        }
        if (object.getClass() == Float.class) {
            return ByteConverter.toByta((Float) object);
        }
        if (object.getClass() == float[].class) {
            return ByteConverter.toByta((float[]) object);
        }
        if (object.getClass() == Double.class) {
            return ByteConverter.toByta((Double) object);
        }
        if (object.getClass() == double[].class) {
            return ByteConverter.toByta((double[]) object);
        }
        if (object.getClass() == Boolean.class) {
            return ByteConverter.toByta((Boolean) object);
        }
        if (object.getClass() == boolean[].class) {
            return ByteConverter.toByta((boolean[]) object);
        }
        if (object.getClass() == String.class) {
            return ByteConverter.toByta((String) object);
        }
        if (object.getClass() == String[].class) {
            return ByteConverter.toByta((String[]) object);
        }
        throw new IllegalArgumentException(object.toString());
    }
}
