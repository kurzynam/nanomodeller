package org.nanomodeller.Tools.DataAccessTools;

import org.nanomodeller.Tools.DataAccessTools.MyFileWriter;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class FilesMathOperation {

    public static void substractDataFromFiles(int beginColumn, String firstPath, String secondPath, String outcomePath){

        BufferedReader br1 = null;
        BufferedReader br2 = null;
        try {
            br1 = new BufferedReader(new FileReader(firstPath));
            br2 = new BufferedReader(new FileReader(secondPath));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        MyFileWriter myFileWriter = new MyFileWriter(outcomePath);
        try {
            br1.readLine();
            br2.readLine();
            while (true) {
                String partOne = br1.readLine();
                String partTwo = br2.readLine();
                if (partOne == null || partTwo == null) {
                    break;
                } else {
                    String[] numbers1 = partOne.split(",");
                    String[] numbers2 = partTwo.split(",");
                    String output = "";
                    int length = numbers1.length;
                    if (partOne.length() > 0) {
                        for (int i = 0; i < beginColumn; i++) {
                            try
                            {
                                output += numbers1[i] + ",";
                            }
                            catch (Exception e){
                                System.out.println();
                            }

                        }
                        for (int i = 0; i < length - beginColumn; i++) {
                            double val1 = Double.parseDouble(numbers2[beginColumn + i]);
                            double val2 = Double.parseDouble(numbers1[beginColumn + i]);
                            double result = val1 - val2;
                            output += result;
                            if (i < length - 1) {
                                output += ",";
                            }
                        }
                    }
                    myFileWriter.println(output);
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        myFileWriter.close();
    }
}


