package de.serializer;

import java.io.*;

public class MagicNumberProvider {
    private static final String filePath = "src/main/resources/magic_numbers.txt";

    public static int getNextNumber() {
        int nextNumber;
        try {
            nextNumber = getLastNumber() + 1;
            writeToMagicNumbers(nextNumber);
        } catch (IOException e) {
            throw new RuntimeException("unable to read magic_number file");
        }
        return nextNumber;
    }

    private static int getLastNumber() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line = reader.readLine();
            String lastLine = "";
            while (line != null) {
                lastLine = line;
                line = reader.readLine();
            }
            if (lastLine.isEmpty()) {
                return 0;
            }
            return Integer.parseInt(lastLine.trim());
        }
    }

    private static void writeToMagicNumbers(int nextNumber) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
            writer.newLine();
            writer.write(Integer.toString(nextNumber));
        }
    }
}
