package org.nanomodeller.GUI;

import org.nanomodeller.Globals;
import org.nanomodeller.Tools.StringUtils;
import org.nanomodeller.XMLMappingFiles.Atom;
import org.nanomodeller.XMLMappingFiles.Bond;
import org.nanomodeller.XMLMappingFiles.Electrode;
import org.nanomodeller.XMLMappingFiles.Parameters;

import java.awt.*;
import java.awt.geom.Line2D;
import static org.nanomodeller.GUI.Dialogs.ColorDialog.convertStringToColor;
class PaintSurface extends Component {

    private static final long serialVersionUID = 1L;
    private final NanoModeler nanoModeler;
    private Stroke thindashed = new BasicStroke(4.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
            1.0f, new float[]{8.0f, 3.0f, 2.0f, 3.0f}, 0.0f);
    private Stroke basicStroke = new BasicStroke();
    public PaintSurface(NanoModeler nanoModeler) {
        this.nanoModeler = nanoModeler;
    }
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        int screenHeight = getSize().height;
        int screenWidth = getSize().width;
        Color surfaceColor;
        String surfColor = Parameters.getInstance().getSurface().getColor();
        if (StringUtils.isNotEmpty(surfColor)){
            surfaceColor = convertStringToColor(surfColor);
        }else {
            surfaceColor = Color.WHITE;
        }
        Color basicElementColor = Color.BLACK;
        Color basicHiglightedElementColor = new Color(32, 255, 32);
        if (surfaceColor.getGreen() < 128 && surfaceColor.getBlue() < 128 && surfaceColor.getRed() < 128){
            basicElementColor = Color.WHITE;
            basicHiglightedElementColor = Color.red;
        }
        g2.setColor(surfaceColor);
        g2.fillRect(0, 0, screenWidth, screenHeight);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (Globals.DARK_GRAY.equals(nanoModeler.getbColor())) {
            g2.setPaint(Color.LIGHT_GRAY);
        } else {
            g2.setPaint(Color.DARK_GRAY);
        }
        if (nanoModeler.isShowGrid()) {
            int d = nanoModeler.getGridSize();
            for (int i = 0; i < screenWidth; i += d)
                g2.draw(new Line2D.Float(i, 0, i, screenHeight));
            for (int i = 0; i < screenHeight; i += d)
                g2.draw(new Line2D.Float(0, i, screenWidth, i));
        }
        g2.setStroke(new BasicStroke(4));
        g2.setColor(basicElementColor);
        for (Bond bond : nanoModeler.getBonds()) {
            Color highlightedColor;
            if(StringUtils.isNotEmpty(bond.getColor())) {
                highlightedColor = convertStringToColor(bond.getColor());
            }else{
                highlightedColor = basicHiglightedElementColor;
            }
            boolean isSelected = NanoModeler.getInstance().getSelectedBonds().contains(bond);
            Atom first = NanoModeler.getInstance().getAtoms().get(bond.getFirst());
            Atom second = NanoModeler.getInstance().getAtoms().get(bond.getSecond());
            int d = NanoModeler.getInstance().getGridSize()*2;
            thindashed = new BasicStroke(d/11, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
                    1.0f, new float[]{8.0f, 3.0f, 2.0f, 3.0f}, 0.0f);
            g2.setStroke(this.thindashed);
            int x1 = first.getX();
            int y1 = first.getY();
            int x2 = second.getX();
            int y2 = second.getY();
            if (isSelected){
                g2.setColor(highlightedColor);
            }else {
                g2.setColor(basicElementColor);
            }
            g2.drawLine(x1, y1, x2, y2);
            g2.setStroke(basicStroke);
            g2.setColor(highlightedColor);
            g2.fillOval((x1 + x2 - d/3)/2, (y1 + y2 - d/3)/2 , d/3, d/3);
        }
        for (Electrode electrode : nanoModeler.getElectrodes().values()) {
            if (electrode.getAtomIndex() >= 0){
                g2.setStroke(thindashed);
                Atom connectedAtom = nanoModeler.getAtoms().get(electrode.getAtomIndex());
                g2.drawLine(electrode.getX(), electrode.getY(), connectedAtom.getX(), connectedAtom.getY());
            }
            if (!nanoModeler.getSelectedElectrodes().contains(electrode)) {
                drawElectrode(g2, electrode.getX(), electrode.getY());
            }else{
                drawHElectrode(g2, electrode.getX(), electrode.getY());
            }
        }
        for (Atom s : nanoModeler.getAtoms().values()) {
            if (!nanoModeler.getSelectedAtoms().contains(s)) {
                drawAtom(g2, s.getX(), s.getY());
            }else{
                drawHighlightedAtom(g2, s.getX(), s.getY());
            }
        }
        g2.setColor(new Color(125,125, 125, 125));
        if (nanoModeler.getSelection() != null)
            g2.fill(nanoModeler.getSelection());
    }

    private void drawElectrode(Graphics2D g2, int x, int y) {
        g2.drawImage(NanoModeler.getInstance().getScalledElectrodeImage(), x - nanoModeler.getGridSize(), y - nanoModeler.getGridSize(),null);
    }

    private void drawHElectrode(Graphics2D g2, int x, int y) {
        g2.drawImage(NanoModeler.getInstance().getScalledHElectrodeImage(), x - nanoModeler.getGridSize(), y -  nanoModeler.getGridSize(),null);
    }
    private void drawAtom(Graphics2D g2, int x, int y) {
        g2.drawImage(NanoModeler.getInstance().getScalledAtomImage(), x - nanoModeler.getGridSize(), y - nanoModeler.getGridSize(),null);
    }
    private void drawHighlightedAtom(Graphics2D g2, int x, int y) {
        g2.drawImage(NanoModeler.getInstance().getScalledHAtomImage(), x - nanoModeler.getGridSize(), y -  nanoModeler.getGridSize(),null);
    }
}
