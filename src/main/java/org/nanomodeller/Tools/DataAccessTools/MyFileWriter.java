package org.nanomodeller.Tools.DataAccessTools;

import org.nanomodeller.Tools.StringUtils;
import org.nanomodeller.XMLMappingFiles.GlobalProperties;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.nanomodeller.Globals.XML_FILE_PATH;


public class MyFileWriter {
    PrintWriter pw = null;
    public MyFileWriter(String filePath){
        try {
            createFileIfNotExists(filePath);
            pw = new PrintWriter(new BufferedWriter(new FileWriter(filePath)));
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void printf(String format, Object... args){
        pw.write(String.format(format, args));
    }
    public void printcsv(String... args){
        String res = "";
        for (int i = 0; i < args.length ; i++){
            res += args[i];
            if (i == args.length -1)
                res += ",";
        }
        printf(res);
    }
    public void println(String text){
        pw.write(text + "\n");
    }
    public void println(){
        println("");
    }
    public void close(){
        pw.close();
    }

    public static void createFileIfNotExists(String path){

        try {
            File f = new File(path);
            if (!f.exists()) {
                f.getParentFile().mkdirs();
                f.createNewFile();
            }
        }
        catch (Exception e){
            Log.appendLine(e, MyFileWriter.class.getName(), "createFileIfNotExists");
        }
    }

    public static int blankLineCount(String path)
    {
        File f1=new File(path);
        int linecount = 0;
        FileReader fr = null;
        try {
            fr = new FileReader(f1);
            BufferedReader br = new BufferedReader(fr);
            String line;
            while((line = br.readLine())!=null)
            {
                if (StringUtils.isEmpty(line))
                    linecount++;
            }
            fr.close();
        } catch (Exception e) {
            Log.appendLine(Log.dateNow() + e.getMessage() + Log.EXCEPTION + MyFileWriter.class.getName() + " blankLineCount");
        }
        return linecount;
    }

    public static void sumFiles(File[] files, String resultPATH)
    {
        MyFileWriter myFileWriter = new MyFileWriter(resultPATH);
        int length = files.length;
        FileReader[] fr = new FileReader[length];
        BufferedReader[] br = new BufferedReader[length];
        String[] lines = new String[length];
        try {

            for(int i = 0; i < length; i++) {
                fr[i] = new FileReader(files[i]);
                br[i] = new BufferedReader(fr[i]);
            }
            double summedValue;
            boolean condition = true;
            while (condition) {
                summedValue = 0;
                String[] split = null;
                for (int i = 0; i < length; i++) {
                    lines[i] = br[i].readLine();
                    if (lines[i] == null) {
                        condition = false;
                        break;
                    }
                    split = lines[i].split(" ");
                    if (split.length > 1) {
                        summedValue += Double.parseDouble(split[2]);
                    }
                }
                if (split != null) {
                    if (split.length > 1) {
                        myFileWriter.println(split[0] + " " + split[1] + " " + summedValue / length);
                    }
                    else {
                        myFileWriter.println();
                    }
                }
            }
            for (FileReader reader : fr) {
                reader.close();
            }
            myFileWriter.close();
        } catch (Exception e) {
            Log.appendLine(Log.dateNow() + e.getMessage() + Log.EXCEPTION + MyFileWriter.class.getName() + " sumFiles");
        }
    }

    public static int blockSize(String path)
    {
        File f1=new File(path);
        int linecount = 0;
        FileReader fr = null;
        try {
            fr = new FileReader(f1);
            BufferedReader br = new BufferedReader(fr);
            String line;
            while((line = br.readLine())!=null)
            {
                linecount++;
                if (StringUtils.isEmpty(line))
                {
                    break;
                }
            }
            fr.close();
        } catch (Exception e) {
            Log.appendLine(Log.dateNow() + e.getMessage() + Log.EXCEPTION + MyFileWriter.class.getName() + " blankLineCount");
        }
        return linecount;
    }

    public static void saveBlockGivenT(String t, String currentStepPath){
        GlobalProperties gp = GlobalProperties.getInstance();
        String pathToRead = gp.getDynamicPATH() + "/" + currentStepPath + "/" + "LDOS.csv";
        String lasTpattern = "LDOS_t";
        String writePath =  gp.getDynamicPATH() + "/" + currentStepPath + "/" + lasTpattern + ".csv";
        MyFileWriter mw = new MyFileWriter(writePath);
        try {
            File f1=new File(pathToRead);
            FileReader fr = null;
            boolean flag = false;
            double differenceToRemember = 0;

            try {
                fr = new FileReader(f1);
                BufferedReader br = new BufferedReader(fr);
                String line;
                line = br.readLine();
                while((line = br.readLine())!=null)
                {
                    String[] lines = line.split(",");
                    if (lines.length > 1) {
                        double difference = Double.parseDouble(lines[1]) - Double.parseDouble(t);
                        if (difference < 0) {
                            continue;
                        } else if (flag) {
                            if (differenceToRemember - difference == 0)
                                mw.println(line);
                            else
                                break;
                        } else {
                            differenceToRemember = difference;
                            flag = true;
                        }
                    }
                    else{
                        continue;
                    }
                }
                fr.close();
                mw.close();
            } catch (Exception e) {
                Log.appendLine(Log.dateNow() + e.getMessage() + Log.EXCEPTION + MyFileWriter.class.getName() + " saveBlockGivenT");
            }
        }
        catch (Exception e){

        }

    }
    public static void overwriteLine(String fileName, int lineNumber, String line) {
        Path path = Paths.get(fileName);
        try {
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            lines.set(lineNumber - 1, line);
            Files.write(path, lines, StandardCharsets.UTF_8);
        }
        catch (Exception e){

        }
    }
    public static boolean isFileOpen(String fileName){
        File file = new File(fileName);
        File sameFileName = new File(fileName);

        if(file.renameTo(sameFileName)){
            return false;
        }else{
            return true;
        }
    }

}
