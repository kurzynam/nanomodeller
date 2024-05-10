package org.nanomodeller;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.TimeUnit;

public class CircleMove extends JFrame {
    Body p = new Body(300, 0, 0);
    double x = 290;
    double y = 290;
    double vx = -40;
    double vy = 0;
    double aR = 30;
    double aX;
    double aY;
    double theta = 0;
    double R = 560;
    int dt = 10;
    int centerX = 400;
    int centerY = 400;


    public CircleMove(){
        setContentPane(new MyPanel());
        setSize(900,900);
        show();
        double dt = 0.01;

        x = centerX;
        y = 30;
        for(int i =0; ; i++){
            theta = Math.atan2((y+centerY),(x+centerY+0.001));
            aX = Math.cos(theta)*aR;
            aY = Math.sin(theta)*aR;

            double a = Math.sqrt(aX*aX + aY*aY);
            vx += dt*aX;
            vy += dt*aY;
            x += dt*vx;
            y += dt*vy;
            getContentPane().repaint();
            try {
                TimeUnit.MILLISECONDS.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    public class MyPanel extends JPanel {
        public MyPanel() {
            setPreferredSize(new Dimension(400, 400));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            int eradius = 50;
            g2d.drawOval((int)(x - eradius/2), (int)(y - eradius/2), eradius, eradius);
            g2d.fillOval((int)(x - eradius/2), (int)(y - eradius/2), eradius, eradius);
            int radius = 100;
            g2d.setPaint(Color.orange);
            g2d.drawOval(centerX - radius/2, centerY - radius/2, radius, radius);
            g2d.fillOval(centerX - radius/2, centerY - radius/2, radius, radius);

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
