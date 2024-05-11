package org.nanomodeller.Tools.DataAccessTools;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class FileOperationHelper {

    public static void runFile(String path) {
        File file = new File(path);
        Desktop desktop = Desktop.getDesktop();
        if (file.exists()) try {
            desktop.open(file);
        } catch (IOException exception) {

        }
    }
    public static boolean fileExists(String path){
        return (new File(path).exists());
    }
}
