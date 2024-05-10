package org.nanomodeller.Tools.DataAccessTools;

import org.nanomodeller.Globals;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Log {

    public static final String EXCEPTION = "An exception has occured in ";
    public static final SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    public static String dateNow(){
        return formatter.format(new Date()) + " ";
    }
    public static void appendLine(String line){
        Path path = Paths.get(Globals.LOG_PATH);
        try {
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            lines.add(line);
            Files.write(path, lines, StandardCharsets.UTF_8);
        }
        catch (Exception e){}
    }
    public static void appendLine(Exception e, String className, String methodName){
        Log.appendLine(Log.dateNow() + e.getMessage() + Log.EXCEPTION + className + methodName);
    }
}
