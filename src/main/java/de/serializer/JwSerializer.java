package de.serializer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class JwSerializer<T> {

    private static final String HEX_FORMAT_STRING = "%02X";

    public void serialize(T value) throws IOException, IllegalAccessException {
        int magicNumber = MagicNumberProvider.getNextNumber();

        Map<String, Object> fields = new PrimitiveFieldsLoader(value).load();

        List<byte[]> byteArray = getAllByteArrays(fields);
    }

    private List<byte[]> getAllByteArrays(Map<String, Object> fields) {
        StringBuilder binaryString = new StringBuilder();
        for (String name : fields.keySet()) {
            byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
            String nameBinary = toByteString(nameBytes);
            byte length = (byte) nameBinary.length();
            String nameLengthBinary = toBinaryString(length, (byte) 32);


            binaryString.append(nameLengthBinary).append(nameBinary);
        }

        return null;
    }

    private String toByteString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte it : bytes) {
            sb.append(toBinaryString(it, (byte) 8));
        }
        return sb.toString();
    }

    private String toBinaryString(byte it, byte length) {
        return String.format("%" + length + "s", Integer.toBinaryString(it & 0xFF)).replace(' ', '0');
    }

    private byte[] toByta(Object object) {
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
