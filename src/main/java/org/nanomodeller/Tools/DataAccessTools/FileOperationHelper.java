package org.nanomodeller.Tools.DataAccessTools;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Scanner;

public class FileOperationHelper {

    public static void runFile(String path) {
        File file = new File(path);
        Desktop desktop = Desktop.getDesktop();
        if (file.exists()) try {
            desktop.open(file);
        } catch (IOException exception) {

        }
    }



    public static double findValueByEnergy(double[][] data, double energy, int column) {
        if (column < 1 || column > 5) {
            throw new IllegalArgumentException("Column index must be between 1 and 5 (excluding energy column).");
        }

        double number = new BigDecimal(energy)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();

        int pos = (int)((number - data[0][0])/0.01);
        return data[pos][column];

//        int left = 0;
//        int right = data.length - 1;
//
//        // Perform binary search to find the closest index
//        while (left < right - 1) { // Ensure we always have two points to compare
//            int mid = (left + right) / 2;
//            if (data[mid][0] == energy) {
//                return data[mid][column]; // Exact match found
//            } else if (data[mid][0] < energy) {
//                left = mid;
//            } else {
//                right = mid;
//            }
//        }
//
//        // Find which of left or right is closer to the target energy
//        if (Math.abs(data[left][0] - energy) <= Math.abs(data[right][0] - energy)) {
//            return data[left][column];
//        } else {
//            return data[right][column];
//        }
    }
    public static double[][] readDoubleDataFromFile(String filePath) throws IOException {
        // First pass: Count number of rows
        int numRows = countLines(filePath);
        if (numRows == 0) {
            throw new IOException("File is empty or unreadable.");
        }

        // Initialize fixed-size 2D array
        double[][] data = new double[numRows][6];

        // Second pass: Read data
        try (Scanner scanner = new Scanner(new File(filePath))) {
            int row = 0;
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                String[] tokens = line.split("\\s+"); // Split by whitespace

                if (tokens.length != 6) {
                    throw new IllegalArgumentException("Invalid data format: each line must have exactly 6 columns.");
                }

                for (int col = 0; col < 6; col++) {
                    data[row][col] = Double.parseDouble(tokens[col]);
                }
                row++;
            }
        }
        return data;
    }

    private static int countLines(String filePath) throws IOException {
        int count = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            while (reader.readLine() != null) {
                count++;
            }
        }
        return count;
    }
    public static boolean fileExists(String path){
        return (new File(path).exists());
    }
}
