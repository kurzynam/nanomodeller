package org.nanomodeller.GUI;

import org.nanomodeller.GUI.Shapes.AtomBond;
import org.nanomodeller.GUI.Shapes.AtomShape;
import org.nanomodeller.GUI.Shapes.ElectrodeShape;
import org.nanomodeller.Globals;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;

class PaintSurface extends Component {

    private static final long serialVersionUID = 1L;

    private final NanoModeler nanoModeler;

    public PaintSurface(NanoModeler nanoModeler) {
        this.nanoModeler = nanoModeler;
    }

    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        int screenHeight = PaintSurface.this.getSize().height;
        int screenWidth = PaintSurface.this.getSize().width;
        g2.setColor(nanoModeler.getMenu().colorBox.colors.get(nanoModeler.getbColor()));
        g2.fillRect(0, 0, screenWidth, screenHeight);
        Stroke thindashed = new BasicStroke(4.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
                1.0f, new float[]{8.0f, 3.0f, 2.0f, 3.0f}, 0.0f);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (Globals.DARK_GRAY.equals(nanoModeler.getbColor())) {
            g2.setPaint(Color.LIGHT_GRAY);
        } else {
            g2.setPaint(Color.DARK_GRAY);
        }

        if (nanoModeler.isShowGrid()) {
            for (int i = 0; i < screenWidth; i += nanoModeler.getGridSize())
                g2.draw(new Line2D.Float(i, 0, i, screenHeight));
            for (int i = 0; i < screenHeight; i += nanoModeler.getGridSize())
                g2.draw(new Line2D.Float(0, i, screenWidth, i));
        }
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(4));
        for (AtomBond bound : nanoModeler.getAtomBounds()) {

            if (bound.getFirstAtom() != null && bound != nanoModeler.getHighlightedBound()) {
                g2.setColor(nanoModeler.getMenu().colorBox.colors.get(bound.getColor()));
                g2.draw(bound.getLine());
            }
        }
        g2.setColor(Color.BLACK);
        for (ElectrodeShape e : nanoModeler.getElectrodes()) {

            if (e.getLine() != null) {
                if (Globals.BLACK.equals(nanoModeler.getbColor())) {
                    g2.setColor(Color.WHITE);
                } else {
                    g2.setColor(Color.BLACK);
                }
                g2.setStroke(thindashed);
                g2.draw(e.getLine());
                g2.setStroke(new BasicStroke(4));
            }

            String color = e.getColor();
            Color innerColor = Color.BLACK;
            Color selectedColor = Color.GREEN;
            if ("BLACK".equals(color) || "DARK GRAY".equals(color)) {
                innerColor = Color.WHITE;
            } else if ("GREEN".equals(color) || "DARK GREEN".equals(color)) {
                selectedColor = Color.RED;
            }
            int outerRadius = (int) (e.getRectangle().getWidth() / 1.76);
            Rectangle2D rect = e.getRectangle();
            g2.setColor(nanoModeler.getMenu().colorBox.colors.get(e.getColor()));
            Hexagon hexagon = new Hexagon(new Point((int) rect.getX() + (int) rect.getWidth() / 2, (int) rect.getY() + (int) rect.getWidth() / 2), outerRadius);
            Hexagon innerHexagon = new Hexagon(new Point((int) rect.getX() + (int) rect.getWidth() / 2, (int) rect.getY() + (int) rect.getWidth() / 2), outerRadius / 2);
            g2.setPaint(new RadialGradientPaint(
                    new Point((int) rect.getX() + (int) rect.getWidth() / 2, (int) rect.getY() + (int) rect.getWidth() / 2), (float) outerRadius, new float[]{0, 1},
                    new Color[]{Color.darkGray, nanoModeler.getMenu().colorBox.colors.get(e.getColor())}));
            g2.drawPolygon(hexagon.getHexagon());
            g2.fillPolygon(hexagon.getHexagon());
            if (e != nanoModeler.getHighlightedElectrode() && !nanoModeler.getSelectedElectrodes().contains(e)) {
                g2.setPaint(new RadialGradientPaint(
                        new Point((int) rect.getX() + (int) rect.getWidth() / 2, (int) rect.getY() + (int) rect.getWidth() / 2), (float) outerRadius, new float[]{0, 1},
                        new Color[]{innerColor, nanoModeler.getMenu().colorBox.colors.get(e.getColor())}));
            } else {
                g2.setPaint(new RadialGradientPaint(
                        new Point((int) rect.getX() + (int) rect.getWidth() / 2, (int) rect.getY() + (int) rect.getWidth() / 2), (float) outerRadius, new float[]{0, 1},
                        new Color[]{selectedColor, nanoModeler.getMenu().colorBox.colors.get(e.getColor())}));
            }
            g2.drawPolygon(innerHexagon.getHexagon());
            g2.fillPolygon(innerHexagon.getHexagon());


        }
        for (AtomShape s : nanoModeler.getShapes()) {
            String color = s.getColor();
            Color innerColor = Color.BLACK;
            Color selectedColor = Color.orange;
            if (Globals.BLACK.equals(color) || Globals.DARK_GRAY.equals(color)) {
                innerColor = Color.WHITE;
            } else if (Globals.YELLOW.equals(color) || Globals.DARK_YELLOW.equals(color) || Globals.ORANGE.equals(color)) {
                selectedColor = Color.RED;
            }
            double centerX = s.getShape().getCenterX();
            double centerY = s.getShape().getCenterY();
            double outerRadius = s.getShape().getWidth() / 2.5;
            double innerRadius = 0.55 * s.getShape().getWidth();
            g2.setPaint(new RadialGradientPaint(
                    (float) centerX, (float) centerY, (float) outerRadius, new float[]{0, 1},
                    new Color[]{Color.darkGray, nanoModeler.getMenu().colorBox.colors.get(s.getColor())}));

            g2.fill(createStar(centerX, centerY,
                    innerRadius,
                    outerRadius, 13, 2));
            if (s != nanoModeler.getHighlightedShape() && !nanoModeler.getSelectedAtoms().contains(s)) {
                g2.setPaint(new RadialGradientPaint(
                        (float) centerX, (float) centerY, (float) outerRadius, new float[]{0, 1},
                        new Color[]{innerColor, nanoModeler.getMenu().colorBox.colors.get(s.getColor())}));

            } else {
                g2.setPaint(new RadialGradientPaint(
                        (float) centerX, (float) centerY, (float) outerRadius, new float[]{0, 1},
                        new Color[]{selectedColor, nanoModeler.getMenu().colorBox.colors.get(s.getColor())}));
            }

            g2.fill(createStar(centerX, centerY,
                    innerRadius / 2,
                    outerRadius / 2, 13, 2));
        }


        g2.setColor(Color.ORANGE);
        for (AtomBond s : nanoModeler.getSelectedBounds())
            if (nanoModeler.getAtomBounds().contains(s))
                g2.draw(s.getLine());
        if (nanoModeler.getHighlightedBound() != null) {
            g2.setColor(Color.GREEN);
            g2.draw(nanoModeler.getHighlightedBound().getLine());
        }
        g2.setColor(Color.GRAY);
        if (nanoModeler.getSelection() != null)
            g2.draw(nanoModeler.getSelection());
    }

    private Shape createDefaultStar(double radius, double centerX,
                                    double centerY) {
        return createStar(centerX, centerY, radius, radius * 2.63, 5,
                Math.toRadians(-18));
    }

    private Shape createStar(double centerX, double centerY,
                             double innerRadius, double outerRadius, int numRays,
                             double startAngleRad) {
        Path2D path = new Path2D.Double();
        double deltaAngleRad = Math.PI / numRays;
        for (int i = 0; i < numRays * 2; i++) {
            double angleRad = startAngleRad + i * deltaAngleRad;
            double ca = Math.cos(angleRad);
            double sa = Math.sin(angleRad);
            double relX = ca;
            double relY = sa;
            if ((i & 1) == 0) {
                relX *= outerRadius;
                relY *= outerRadius;
            } else {
                relX *= innerRadius;
                relY *= innerRadius;
            }
            if (i == 0) {
                path.moveTo(centerX + relX, centerY + relY);
            } else {
                path.lineTo(centerX + relX, centerY + relY);
            }
        }
        path.closePath();
        return path;
    }

    public class Hexagon {
        private final int radius;

        private final Point center;

        private final Polygon hexagon;

        public Hexagon(Point center, int radius) {
            this.center = center;
            this.radius = radius;
            this.hexagon = createHexagon();
        }

        private Polygon createHexagon() {
            Polygon polygon = new Polygon();

            for (int i = 0; i < 6; i++) {
                int xval = (int) (center.x + radius
                        * Math.cos(i * 2 * Math.PI / 6D));
                int yval = (int) (center.y + radius
                        * Math.sin(i * 2 * Math.PI / 6D));
                polygon.addPoint(xval, yval);
            }

            return polygon;
        }

        public Polygon getHexagon() {
            return hexagon;
        }

    }
}
