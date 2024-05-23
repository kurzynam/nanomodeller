package org.nanomodeller.GUI.Adapters;

import org.nanomodeller.GUI.NanoModeler;
import org.nanomodeller.GUI.PopUpMenu;
import org.nanomodeller.XMLMappingFiles.Atom;
import org.nanomodeller.XMLMappingFiles.Bond;
import org.nanomodeller.XMLMappingFiles.Electrode;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.Optional;

public class MovingAdapter extends MouseAdapter {
    private final NanoModeler nanoModeler;

    public MovingAdapter(NanoModeler nanoModeler) {
        this.nanoModeler = nanoModeler;
    }

    @Override
    public void mouseClicked(MouseEvent e) {

        boolean isRightMouseButton = SwingUtilities.isRightMouseButton(e);

        Optional<Atom> clickedAtom = nanoModeler.getAtoms().values().stream().
                filter(atom -> atom.contains(e.getX(), e.getY())).findFirst();
        Optional<Electrode> clickedElectrode = nanoModeler.getElectrodes().values().stream().
                filter(electrode -> electrode.
                        contains(e.getX(), e.getY(), 50)).findFirst();
        Optional<Bond> clickedBond = nanoModeler.getBonds()
                .stream().filter(bond -> {
                    int d = NanoModeler.getInstance().getGridSize() * 2;
                    int first = bond.getFirst();
                    int second = bond.getSecond();
                    Atom firstAtom = nanoModeler.getAtomByID(first);
                    Atom secondAtom = nanoModeler.getAtomByID(second);
                    Point center = new Point((firstAtom.getX() + secondAtom.getX())/2,
                            (firstAtom.getY() + secondAtom.getY())/2);
                    return center.distance(e.getPoint()) < d/2;
                }
        ).findFirst();
        if(!clickedAtom.isPresent() && !clickedElectrode.isPresent()
                && clickedBond.isPresent()) {
            nanoModeler.getSelectedBonds().clear();
            nanoModeler.getSelectedBonds().add(clickedBond.get());
        }

        if(!clickedAtom.isPresent() && !clickedElectrode.isPresent()
                && !clickedBond.isPresent()){
            if (e.getClickCount() == 2){
                Atom atom = new Atom(e.getX(), e.getY(), NanoModeler.getInstance().atomIDSeq());
                NanoModeler.getInstance().getAtoms().put(atom.getID(), atom);
            } else if (isRightMouseButton && e.getClickCount() == 1) {
                doPop(e);
            }else{
                nanoModeler.getSelectedAtoms().clear();
                nanoModeler.getSelectedBonds().clear();
                nanoModeler.getSelectedElectrodes().clear();
                return;
            }
        }

        if (nanoModeler.isCtrlPressed()) {
            int size = nanoModeler.getSelectedAtoms().size();
            if (size == 1) {
                if (clickedAtom.isPresent()) {
                    Atom first = (Atom) nanoModeler.getSelectedAtoms().values().toArray()[0];
                    int centerX = (first.getX() + clickedAtom.get().getX())/2;
                    int centerY = (first.getY() + clickedAtom.get().getY())/2;
                    nanoModeler.getBonds().add(new Bond(first.getID(), clickedAtom.get().getID()/*, centerX, centerY*/));
                }
            }
            nanoModeler.getSelectedAtoms().clear();
            nanoModeler.getSelectedElectrodes().clear();
            return;
        }

        if (clickedAtom.isPresent()) {
            if (isRightMouseButton && !nanoModeler.isCtrlPressed()) {
                nanoModeler.getSelectedAtoms().put(clickedAtom.get().getID(), clickedAtom.get());
                doPop(e);
            }else{
                if (!isRightMouseButton)
                    nanoModeler.getSelectedAtoms().clear();
                nanoModeler.getSelectedAtoms().put(clickedAtom.get().getID(), clickedAtom.get());
            }

        } else if (clickedElectrode.isPresent()) {
            nanoModeler.getSelectedElectrodes().put(clickedElectrode.get().getID(), clickedElectrode.get());
            if (isRightMouseButton && !nanoModeler.isCtrlPressed()) {
                doPop(e);
            }
        } else {
            if (isRightMouseButton && !nanoModeler.isCtrlPressed()) {
                doPop(e);
            }
        }
        nanoModeler.setSelection(null);
        ((Component)nanoModeler.getPaintSurface()).repaint();
        ((Component)nanoModeler.getPaintSurface()).requestFocus();
    }

    public void mousePressed(MouseEvent e) {
        nanoModeler.setX(e.getX());
        nanoModeler.setY(e.getY());
        nanoModeler.setAnchor(e.getPoint());
        nanoModeler.setSelection(new Rectangle(nanoModeler.getAnchor()));
        ((Component)nanoModeler.getPaintSurface()).requestFocus();
    }

    private void doPop(MouseEvent e) {
        PopUpMenu menu = new PopUpMenu();
        menu.show(e.getComponent(), e.getX(), e.getY());
    }

    public void mouseReleased(MouseEvent e) {
        int width = NanoModeler.getInstance().getGridSize()/4;
        nanoModeler.getAtoms().values().stream().
                filter(atom -> nanoModeler.getSelection().intersects(atom.getX(), atom.getY(), width, width)).
                forEach(atom -> nanoModeler.getSelectedAtoms().put(atom.getID(), atom));
        nanoModeler.getElectrodes().values().stream().
                filter(electrode -> nanoModeler.getSelection().intersects(electrode.getX(), electrode.getY(), width, width)).
                forEach(electrode -> nanoModeler.getSelectedElectrodes().put(electrode.getID(), electrode));
        nanoModeler.setSelection(null);
        nanoModeler.setAnchor(null);
        nanoModeler.repaint();
        ((Component)nanoModeler.getPaintSurface()).requestFocus();
    }

    public void mouseWheelMoved(MouseWheelEvent e) {
        int notches = e.getWheelRotation();
        nanoModeler.zoom(notches);
    }

    public void mouseDragged(MouseEvent e) {

        boolean isRightMouseButton = SwingUtilities.isRightMouseButton(e);
        int dx = e.getX() - nanoModeler.getX();
        int dy = e.getY() - nanoModeler.getY();
        nanoModeler.setX(e.getX());
        nanoModeler.setY(e.getY());
        if (!nanoModeler.getSelectedElectrodes().isEmpty() || !nanoModeler.getSelectedAtoms().isEmpty()) {
            nanoModeler.getSelectedElectrodes().values().stream().forEach(el -> el.move(dx, dy));
            nanoModeler.getSelectedAtoms().values().stream().forEach(el -> el.move(dx, dy));
        } else if (isRightMouseButton) {
            nanoModeler.getSelection().width += dx;
            nanoModeler.getSelection().height += dy;
        } else {
            nanoModeler.getScrollPane().getVerticalScrollBar().setValue(nanoModeler.getScrollPane().getVerticalScrollBar().getValue() - dy / 2);
            nanoModeler.getScrollPane().getHorizontalScrollBar().setValue(nanoModeler.getScrollPane().getHorizontalScrollBar().getValue() - dx / 2);
        }
        ((Component)nanoModeler.getPaintSurface()).repaint();
    }
}
