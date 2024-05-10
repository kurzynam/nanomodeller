package org.nanomodeller;//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//


import com.panayotis.gnuplot.dataset.GenericDataSet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class MyFileDataSet extends GenericDataSet {
    public MyFileDataSet(File datafile) throws IOException, NumberFormatException, ArrayIndexOutOfBoundsException {
        super(true);
        BufferedReader in = new BufferedReader(new FileReader(datafile));

        String line;
        while((line = in.readLine()) != null && !line.equals("")) {
            line = line.trim();
            if (!line.startsWith("#")) {
                ArrayList<String> data = new ArrayList();
                StringTokenizer tk = new StringTokenizer(line);

                while(tk.hasMoreTokens()) {
                    data.add(tk.nextToken());
                }

                this.add(data);
            }
        }

    }
}
