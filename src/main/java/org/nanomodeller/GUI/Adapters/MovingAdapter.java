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
                    return center.distance(e.getPoint()) < d / 2.0;
                }
        ).findFirst();
        if(clickedAtom.isEmpty() && clickedElectrode.isEmpty()
                && clickedBond.isPresent()) {
            clearAllSelectedElements();
            nanoModeler.getSelectedBonds().add(clickedBond.get());
        }

        if(!clickedAtom.isPresent() && !clickedElectrode.isPresent()
                && !clickedBond.isPresent()){
            if (e.getClickCount() == 2){
                if (nanoModeler.isCtrlPressed()) {
                    Electrode electrode = new Electrode(-1, e.getX(), e.getY(), NanoModeler.getInstance().electrodeIDSeq());
                    NanoModeler.getInstance().getElectrodes().put(electrode.getID(), electrode);
                }else{
                    Atom atom = new Atom(e.getX(), e.getY(), NanoModeler.getInstance().atomIDSeq());
                    NanoModeler.getInstance().getAtoms().put(atom.getID(), atom);
                }
            } else if (isRightMouseButton && e.getClickCount() == 1) {
                doPop(e);
            }else{
                clearAllSelectedElements();
                return;
            }
        }

        if (nanoModeler.isCtrlPressed()) {
            int size = nanoModeler.getSelectedAtoms().size();
            if (size == 1) {
                Atom first = (Atom) nanoModeler.getSelectedAtoms().values().toArray()[0];
                if (clickedAtom.isPresent()) {
                    nanoModeler.getBonds().add(new Bond(first.getID(), clickedAtom.get().getID()));
                } else clickedElectrode.ifPresent(electrode -> electrode.setAtomIndex(first.getID()));
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
                if (!isRightMouseButton) {
                    clearAllSelectedElements();
                }
                nanoModeler.getSelectedAtoms().put(clickedAtom.get().getID(), clickedAtom.get());
            }

        } else if (clickedElectrode.isPresent()) {
            if (!isRightMouseButton) {
                clearAllSelectedElements();
            }
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

    private void clearAllSelectedElements() {
        nanoModeler.getSelectedAtoms().clear();
        nanoModeler.getSelectedBonds().clear();
        nanoModeler.getSelectedElectrodes().clear();
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
        if (nanoModeler.getSelection() != null){
            nanoModeler.getAtoms().values().stream().
                    filter(atom -> nanoModeler.getSelection().intersects(atom.getX(), atom.getY(), width, width)).
                    forEach(atom -> nanoModeler.getSelectedAtoms().put(atom.getID(), atom));
            nanoModeler.getElectrodes().values().stream().
                    filter(electrode -> nanoModeler.getSelection().intersects(electrode.getX(), electrode.getY(), width, width)).
                    forEach(electrode -> nanoModeler.getSelectedElectrodes().put(electrode.getID(), electrode));
        }
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
