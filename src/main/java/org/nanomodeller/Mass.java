package org.nanomodeller;



import org.apache.commons.math3.complex.Complex;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.TimeUnit;

import javax.swing.JPanel;

public class Mass extends JFrame {
    Body p = new Body(300, 0, 0);
    Body m = new Body(400, 560, 0);
    Body m1 = new Body(400, 560, 0);
    Body m2 = new Body(400, 560, 0);
    Body m3 = new Body(400, 560, 0);
    Body m4 = new Body(400, 560, 0);
    double R = 560;
    int dt = 10;


    public static double countB(double a, double b, double A){
        return Math.asin((Math.sin(A)/Math.sin(a))*Math.sin(b));
    }
    public Mass(){
        setContentPane(new MyPanel());
        setSize(900,900);
        show();
        double theta0 = Math.PI/6;
        for(int i =0; ; i++){

            R = 560;

            double k = 10;
            double mass = 1;
            double t = i*dt/1000.0;
            double lambda = 0.02;
            double damping = Math.exp(-lambda*t);
            double omega = Math.sqrt(k/mass);
            double prevPy = p.y;
            double theta = theta0*Math.cos(omega*t);
            p.y = 300 + (int)(300*(Math.sin(omega*t))*damping);

            double amp = 20;

            omega = amp*Math.sqrt(10/R);
            theta = theta0*Math.cos(omega*t);
            m.y = (int)(R*Math.cos(theta));
            m.x = 300+ (int)(R*Math.sin(theta));
            R -= 30;

            omega = amp*Math.sqrt(10/R);
            theta = theta0*Math.cos(omega*t);
            m1.y = (int)(R*Math.cos(theta));
            m1.x = 300+ (int)(R*Math.sin(theta));
            R -= 30;

            omega = amp*Math.sqrt(10/R);
            theta = theta0*Math.cos(omega*t);
            m2.y = (int)(R*Math.cos(theta));
            m2.x = 300+ (int)(R*Math.sin(theta));
            R -= 30;

            omega = amp*Math.sqrt(10/R);
            theta = theta0*Math.cos(omega*t);
            m3.y = (int)(R*Math.cos(theta));
            m3.x = 300+ (int)(R*Math.sin(theta));
            R -= 30;

            omega = amp*Math.sqrt(10/R);
            theta = theta0*Math.cos(omega*t);
            m4.y = (int)(R*Math.cos(theta));
            m4.x = 300+ (int)(R*Math.sin(theta));
            R -= 30;

            if (i%4==0)
                p.velocity = (prevPy - p.y)/dt;
            getContentPane().repaint();
            try {
                TimeUnit.MILLISECONDS.sleep(dt);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


//    public class MyPanel extends JPanel {
//        public MyPanel() {
//            setPreferredSize(new Dimension(400, 400));
//        }
//
//        @Override
//        protected void paintComponent(Graphics g) {
//            super.paintComponent(g);
//            Graphics2D g2d = (Graphics2D) g;
//
////            g2d.drawRect(p.x, p.y, 120, 120);
////            g2d.fillRect(p.x, p.y, 120, 120);
////            g2d.drawLine(360,0,360,p.y);
//
//
//            g2d.setColor(Color.red);
//            g2d.drawOval(m.x, m.y, 120, 120);
//            g2d.fillOval(m.x, m.y, 120, 120);
//            g2d.drawLine(360,0,m.x +60,m.y +60);
//
//            g2d.setColor(Color.green);
//            g2d.drawOval(m1.x, m1.y, 120, 120);
//            g2d.fillOval(m1.x, m1.y, 120, 120);
//            g2d.drawLine(360,0,m1.x +60,m1.y +60);
//
//            g2d.setColor(Color.blue);
//            g2d.drawOval(m2.x, m2.y, 120, 120);
//            g2d.fillOval(m2.x, m2.y, 120, 120);
//            g2d.drawLine(360,0,m2.x +60,m2.y +60);
//
//            g2d.setColor(Color.black);
//            g2d.drawOval(m3.x, m3.y, 120, 120);
//            g2d.fillOval(m3.x, m3.y, 120, 120);
//            g2d.drawLine(360,0,m3.x +60,m3.y +60);
//
//            g2d.setColor(Color.orange);
//            g2d.drawOval(m4.x, m4.y, 120, 120);
//            g2d.fillOval(m4.x, m4.y, 120, 120);
//            g2d.drawLine(360,0,m4.x +60,m4.y +60);
//
//
//
//            g2d.setColor(Color.white);
//            g2d.setFont(new Font("Consolas", Font.BOLD, 22));
////            g2d.drawString(String.format("y=-%.2f", p.y/600.0),p.x+35,p.y+60);
////            g2d.drawString(String.format("v=%.2f", Math.abs(p.velocity)),p.x+35,p.y+90);
//        }
//    }

    public class MyPanel extends JPanel {
        public MyPanel() {
            setPreferredSize(new Dimension(400, 400));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            int i = 1;//this.getWidth()/500;
            Complex c;
            double maxr = 0;
            double maxim = 0;
            double minr = 0;
            double mini = 0;
            int gridsize= 1000;
            double stp = 0.01;
            for (int j=0;j<gridsize;j++){
                for (int k=0; k<gridsize; k++){
                    c = Complex.valueOf((j-gridsize/2)*stp,(k-gridsize/2)*stp);
                    Complex d = c.cos();
                    double re = d.getReal();
                    double im = d.getImaginary();
                    if (re > maxr){
                        maxr = re;
                    }
                    if (im > maxim){
                        maxim = im;
                    }
                    if (re < minr){
                        minr = re;
                    }
                    if (im < mini){
                        mini = im;
                    }
                }


            }
            for (int j=0;j<gridsize;j++){
                for (int k=0; k<gridsize; k++){
                    c = Complex.valueOf((j-gridsize/2)*stp,(k-gridsize/2)*stp);
                    Complex d = c.cos();
                    double re = d.getReal();
                    double im = d.getImaginary();
                    try{

                            int val = (int)(255*re);
                            if (val > 255)
                                val = 255;
                            else if (val <-255){
                                val = -255;
                            }
                            int val1 = val/2;
                            val= val1+128;
                            g2d.setColor(new Color(val,0,255-val,122));//255-val,(Math.abs(val1*2))));
//
//                        g2d.drawRect(j*i,k*i , 1, 1);
//                        g2d.fillRect(j*i,k*i , 1, 1);

                            val = (int)((255*im));
                            if (val > 255)
                                val = 255;
                            else if (val < -255){
                                val = -255;
                            }
                            val1 = val/2;
                            val= val1+128;
                            g2d.setColor(new Color(0,val, 255-val,122));//, (Math.abs(val1*2))));

                        g2d.drawOval(j*i,k*i , 1, 1);
                        g2d.fillOval(j*i,k*i , 1, 1);

                    }catch (Exception e){
                        g2d.setColor(Color.black);
                    }

                }


            }

        }
    }

    public class Body extends Point{
        double velocity = 0;
        public Body(int x, int y){
            super(x,y);
        }
        public Body(int x, int y, double v){
            super(x,y);
            velocity = v;
        }
    }
}
