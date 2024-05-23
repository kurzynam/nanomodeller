package org.nanomodeller;

import org.nanomodeller.GUI.NanoModeler;
import org.nanomodeller.Tools.DataAccessTools.MyFileWriter;
import org.jscience.mathematics.number.Complex;

import java.awt.*;
import java.util.ArrayList;
import java.util.Locale;

public class Main {

    public static void main(String[] args) throws Exception {
        System.setProperty("org.xml.sax.driver", "com.sun.org.apache.xerces.internal.parsers.SAXParser");
        System.setProperty("javax.xml.parsers.DocumentBuilderFactory","com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl");
        System.setProperty("javax.xml.parsers.SAXParserFactory","com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl");
//        String[] H = new String[4];
//        H[0] = AiS+aiS;// i-th atom energy
//        H[1] = AiS+aiS+Ais+ais;// Coulomb interaction
//        H[2] = AkS+akS;// electrode energy
//        H[3] = AkS+aiS;// hybrydization energy
//        H[4] = AiS+akS;// hybrydization energy h.c.
//        H[5] = AjS+aiS;// atom-atom coupling
//        H[6] = AkS + aiS;// atom-surface coupling
//        H[7] = AiS + akS;// atom-surface coupling
//        String operator = AlS+Als+aqS;//+Als+als+Als+als+Als+als;
//        runQWS();
//        Expression.run(H, operator);

//mandel();


        ArrayList<Color> palette1 = new ArrayList<>();
//
//
//        palette1.add((new Color(128, 128, 255)));
//        palette1.add((new Color(0, 0, 0)));
//        palette1.add((new Color(255,128,128)));
//
//        ArrayList<Color> palette2 = new ArrayList<>();
//
//        palette2.add((new Color(0, 0, 255)));
//        palette2.add((new Color(75, 75, 75)));
//        palette2.add((new Color(255,0,0)));


        ArrayList<Color> palette3 = new ArrayList<>();

        palette3.add((new Color(0, 22, 255)));
        palette3.add((new Color(90, 255, 231)));
        palette3.add((new Color(101, 255, 63)));
        palette3.add((new Color(255, 255, 0)));
        palette3.add((new Color(220,30,0)));

        ArrayList<Color> palette4 = new ArrayList<>();

        palette4.add((new Color(0, 107, 255)));
        palette4.add((new Color(90, 255, 218)));
        palette4.add((new Color(144, 220, 71)));
        palette4.add((new Color(200, 200, 0)));
        palette4.add((new Color(200, 50, 5)));

//
//        palette1.add((new Color(0, 3, 255)));
//        palette1.add((new Color(0, 137, 255)));
//        palette1.add((new Color(255, 255, 255)));
//        palette1.add((new Color(14, 255, 242)));
//        palette1.add((new Color(0, 17, 255)));
//
//        ArrayList<Color> palette2 = new ArrayList<>();
//
////        palette2.add((new Color(0, 2, 197)));
////        palette2.add((new Color(0, 97, 192)));
//        palette2.add((new Color(200, 200, 200)));
//        palette2.add((new Color(0, 140, 196)));
//        palette2.add((new Color(0, 70, 192)));
//
//
//        palette3.add((new Color(0, 23,255)));
//        palette3.add((new Color(89,255, 247)));
//        palette3.add((new Color(197, 197, 197)));
//        palette3.add((new Color(255, 128,0)));
//        palette3.add((new Color(255,0,0)));
//        palette3.add((new Color(255,0,0)));
//
//        ArrayList<Color> palette4 = new ArrayList<>();
//
//        palette4.add((new Color(0, 132, 255)));
//        palette4.add((new Color(129, 225, 222)));
//        palette4.add((new Color(226, 226, 226)));
//        palette4.add((new Color(186, 155, 106)));
//        palette4.add((new Color(255, 0, 147)));

//        ArrayList<Color> pal1 = createTemporaryPalette(199, palette1);
//        ArrayList<Color> pal2 = createTemporaryPalette(199, palette2);
//        ArrayList<Color> pal3 = createTemporaryPalette(5, palette3);
//        ArrayList<Color> pal4 = createTemporaryPalette(5, palette4);
//
//
//        ArrayList<ArrayList<Color>> palettes = new ArrayList<>();
        Locale.setDefault(Locale.US);
            NanoModeler.getInstance().initNanoModeller();



//        XMLEditorForm xmlEditorForm = new XMLEditorForm();



//        JavaPlot p = new JavaPlot("gnuplot\\bin\\wgnuplot.exe");
//
//        p.addPlot("sin(x)");

//        amethod();

//        p.plot();
//        readData(2,2);
//        palettes.add(pal1);
//        palettes.add(pal2);
//        palettes.add(pal3);
//        palettes.add(pal4);
//        ArrayList<Color> mix = mixPalettes(palettes, 1, palettes.get(0).size());
//
//        createGnuPlotPalette2(mix);

      //  new CircleMove();

    }


//public static void amethod(){
//
//    JEditorPane editor = new JEditorPane();
//    editor.setBounds(114, 65, 262, 186);
//    JFrame frame = new JFrame();
//    frame.setSize(300,400);
//    frame.getContentPane().add(editor);
//    editor.setContentType( "html" );
//    File file=new File("parameters.xml");
//    try {
//        editor.setPage(file.toURI().toURL());
//    } catch (IOException e) {
//        throw new RuntimeException(e);
//    }
//    try (InputStream xml = getClass().getResourceAsStream("parameters.xml")) {
//        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
//        Document doc = db.parse(xml);
//
//        StringWriter output = new StringWriter();
//        TransformerFactory tf = TransformerFactory.newInstance();
//
//        String html = output.toString();
//
//        // JEditorPane doesn't like the META tag...
//        html = html.replace("<META http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">", "");
//        editor.setContentType("text/html; charset=UTF-8");
//
//        editor.setText(html);
//    } catch (IOException | ParserConfigurationException | SAXException  e) {
//        editor.setText("Unable to format document due to:\n\t" + e);
//    }
//    frame.show();
//    editor.setCaretPosition(0);
//}



    public static int mand(Complex z0, int max) {
        Complex z = z0;
        for (int t = 0; t < max; t++) {
            if (z.magnitude() > 2.0) return t;
            z = z.times(z).plus(z0);
        }
        return max;
    }

    public static void mandel()  {

        double d = 0.01;
        double xc   = -d;
        double yc   = d;
        double size = 1;

        int n   = 200;   // create n-by-n image
        int max = 100;   // maximum number of iterations
        MyFileWriter mw = new MyFileWriter("C:\\Users\\lenovo\\Desktop\\mandel.txt");
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                double x0 = xc - size/2 + size*i/n;
                double y0 = yc - size/2 + size*j/n;
                Complex z0 = Complex.valueOf(x0, y0);
                int gray = max - mand(z0, max);
                double y = n-1-j;
                mw.println(i+" "+ y +" "+ gray);
            }
            mw.println();
        }
        mw.close();
    }
}



