package org.nanomodeller;

import org.nanomodeller.Tools.DataAccessTools.MyFileWriter;

import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

import static org.openjdk.nashorn.internal.objects.Global.println;


public class MixedPalette {

    static ArrayList<Color> mixPalettes(
            ArrayList<ArrayList<Color>> palletes,//list of palettes
            int w, int N){
        //step 4: mixing palettes
        ArrayList<Color> mixedPalette = new ArrayList<>();
        // mixedPalette - output palette
        int M = palletes.size();
        for (int i = 0; i < N; i += w) {
            // 0 <= i < N - colors counter
            for (int j = 0; j < M; j++) {
                // 0 <= j < M - palettes counter
                for(int k = 0; k < w; k++){
                    // 0 <= k < w - weaving number counter
                    mixedPalette.add(palletes.get(j).get(i + k));
                }
            }
        }
        return mixedPalette;
    }

    static ArrayList<ArrayList<Color>> readData(int N, int M)
            throws java.io.IOException{
        ArrayList<ArrayList<Color>> outputArray = new ArrayList<>();
        BufferedReader reader =
            new BufferedReader(new InputStreamReader(System.in));
            for (int j = 0; j < M; j++) {
                // 0 <= j < M - palettes counter
                ArrayList<Color> palette = new ArrayList<>();
                println("palette_"+j);
                for (int i = 0; i < N; i ++) {
                    // 0 <= i < N - colors counter
                    int r, g, b;
                    println("Insert color_"+i+" RGB components");
                    r = Integer.parseInt(reader.readLine());
                    g = Integer.parseInt(reader.readLine());
                    b = Integer.parseInt(reader.readLine());
                    palette.add(new Color(r,g,b));
                }
                outputArray.add(palette);
            }
        return outputArray;
    }

    static ArrayList<Color> createTemporaryPalette
            (int density, ArrayList<Color> pallete){

        ArrayList<Color> result = new ArrayList<>();
        int N = pallete.size();
        // N - nuber of colors in array
        int [][] differences = new int[N -1][3];
        // differences is an array storing $\delta$ elements

        //step 2: finding differences
        for (int i = 0; i < N - 1; i++){
            // 0 <= i < N - color counter
            differences[i][0] =
                    ((pallete.get(i + 1) .getRed()) -
                            (pallete.get(i) .getRed()))/density;
            differences[i][1] =
                    ((pallete.get(i + 1) .getGreen()) -
                            (pallete.get(i) .getGreen()))/density;
            differences[i][2] =
                    ((pallete.get(i + 1) .getBlue()) -
                            (pallete.get(i) .getBlue()))/density;
        }

        //step 3: adding $\delta$ elements to colors
        for (int n = 0; n < differences.length; n++)
        {
            for(int i = 0; i <= density; i++){
                int r = (pallete.get(n) .getRed()
                        + differences[n][0]*i);
                int g = (pallete.get(n) .getGreen()
                        + differences[n][1]*i);
                int b = (pallete.get(n) .getBlue()
                        + differences[n][2]*i);
                result.add(new Color(r,g,b));
            }
        }
        return result;
    }

    public static void createGnuPlotPalette(ArrayList<Color> pallete){
        MyFileWriter mw = new MyFileWriter("C:\\Users\\lenovo\\Desktop\\palette.plt");
        mw.printf("set palette defined ( ");
        int counter = 0;
        int index = 0;
        for (Color c : pallete){

            double r = c.getRed() / 255.0;
            double g = c.getGreen() / 255.0;
            double b = c.getBlue() / 255.0;
            mw.printf("%d %f %f %f,", counter++, r, g, b);
//            if (index % 2 == 0){
//                counter++;
//                index = 0;
//            }
//            index++;
        }
        mw.println("\nset samples 1000\n" +
                "set isosamples 1000\n" +
                "set xrange [0:16]\n" +
                "set yrange [0:16]\n" +
//                "set zrange [0:1]\n" +
//                "set cbrange [0:1]\n"+
                "set view map\n" +
                "f(x,y) = 2.71**(-(x**2+y**2))\n" +
                "F(x,y) = 0.01*2.71**(-((x-3)**2+(y-1)**2))\n" +
                "g(x,y) = 0.05*(sin(x)*cos(y)+sin(20*y)*cos(10*x)+cos(4*x*y)/2)**2\n" +
                "l(x,y) = 0.01*(sin(x)*cos(y)+sin(2*y)*cos(x)+cos(x*y/2))**2\n" +
                "h(x,y) = f(x,y)+g(x,y)+F(x,y)\n" +
                "u(x,y) = sin(x*y)/(x*y)\n"+
                "e=2.71\n" +
                "A=1\n" +
                "m=0.5\n" +
                "k=150\n" +
                "W(y)=sqrt(k/m - y*y/(4*m*m))\n" +
                "H(x,y)=A*e**(-y*x/(2*m))*cos(W(y)*x)\n"+
                "m(x,y)=e**(-0.5*x*x-0.5*y*y)*sin(10*(x +y))\n"+
                "geek(x,y) = (1 - x / 2 + x**5 + y**6) * e**(-1.57*(x**2 + y**2))\n"+
                "splot abs(H(x,y)) title '' with pm3d");
        mw.close();
    }






    public static void createGnuPlotPalette2(ArrayList<Color> pallete){
        MyFileWriter mw = new MyFileWriter("C:\\Users\\lenovo\\Desktop\\palette.plt");
        mw.printf("set palette defined ( ");
        int counter = 0;
        int index = 0;
        for (Color c : pallete){

            double r = c.getRed() / 255.0;
            double g = c.getGreen() / 255.0;
            double b = c.getBlue() / 255.0;
            mw.printf("%d %f %f %f,", counter++, r, g, b);
//            if (index % 2 == 0){
//                counter++;
//                index = 0;
//            }
//            index++;
        }
        mw.println("\nset samples 1000\n" +
                "set isosamples 1000\n" +
                "set xrange [-6:6]\n" +
                "set yrange [-6:6]\n" +
                "set xlabel 'x'\n" +
                "set ylabel 'y'\n" +
//                "set zrange [0:1]\n" +
//                "set cbrange [0:1]\n"+
                "set view map\n" +
                "f(x,y) = 2.71**(-(x**2+y**2))\n" +
                "F(x,y) = 0.01*2.71**(-((x-3)**2+(y-1)**2))\n" +
                "g(x,y) = 0.05*(sin(x)*cos(y)+sin(20*y)*cos(10*x)+cos(4*x*y)/2)**2\n" +
                "l(x,y) = 0.01*(sin(x)*cos(y)+sin(2*y)*cos(x)+cos(x*y/2))**2\n" +
                "h(x,y) = f(x,y)+g(x,y)+F(x,y)\n" +
                "u(x,y) = sin(x*y)/(x*y)\n"+
                "e=2.71\n" +
                "A=1\n" +
                "m=0.5\n" +
                "k=150\n" +
                "W(y)=sqrt(k/m - y*y/(4*m*m))\n" +
                "H(x,y)=A*e**(-y*x/(2*m))*cos(W(y)*x)\n"+
                "H(x,y)=cos(1.3*x)*sin(.9*y)+sin(.8*x)*cos(1.9*y)+sin(y*.2*x)\n"+
                "m(x,y)=e**(-0.5*x*x-0.5*y*y)*sin(10*(x +y))\n"+
                "geek(x,y) = (1 - x / 2 + x**5 + y**6) * e**(-1.57*(x**2 + y**2))\n"+
                "splot H(x,y) title '' with pm3d");
        mw.close();
    }


}
