package org.nanomodeller;

import java.io.*;

public class RunPythonScript {
    public static void main(String[] args) {
        try {
            // Ścieżka do interpretera Pythona i skryptu Pythona
            String pythonPath = "C:\\Users\\mkurzyna\\PycharmProjects\\pythonProject\\.venv\\Scripts/python.exe";
            String scriptPath = "C:\\Users\\mkurzyna\\PycharmProjects\\pythonProject\\plot.py";

            // Uruchomienie skryptu Pythona
            ProcessBuilder pb = new ProcessBuilder(pythonPath, scriptPath);
            Process p = pb.start();
            System.out.println("KKK");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
