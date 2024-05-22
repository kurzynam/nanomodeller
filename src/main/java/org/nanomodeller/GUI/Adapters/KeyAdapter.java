package org.nanomodeller.GUI.Adapters;

import org.nanomodeller.GUI.NanoModeler;
import org.nanomodeller.XMLMappingFiles.Atom;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyAdapter implements KeyListener {

    private final NanoModeler nanoModeler;

    public KeyAdapter(NanoModeler nanoModeler) {
        this.nanoModeler = nanoModeler;
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
            nanoModeler.setCtrlPressed(true);
        }

    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (nanoModeler.isCtrlPressed()) {
            if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                nanoModeler.clearAll();
                nanoModeler.setCtrlPressed(false);
            } else if (e.getKeyCode() == KeyEvent.VK_C) {
                nanoModeler.copy();
            } else if (e.getKeyCode() == KeyEvent.VK_V) {
                nanoModeler.paste();
            } else if (e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_LEFT) {
                nanoModeler.flipVertically();
            } else if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN) {
                nanoModeler.flipHorizontally();
            } else if (e.getKeyCode() == KeyEvent.VK_MINUS) {
                nanoModeler.zoom(-2);
            } else if (e.getKeyCode() == KeyEvent.VK_EQUALS) {
                nanoModeler.zoom(2);
            }
            if (e.getKeyCode() == KeyEvent.VK_L) {
                nanoModeler.countStaticProperties();
                nanoModeler.setCtrlPressed(false);
            }
            if (e.getKeyCode() == KeyEvent.VK_R) {
                nanoModeler.refresh();
                nanoModeler.setCtrlPressed(false);
            }
            if (e.getKeyCode() == KeyEvent.VK_S) {
                nanoModeler.saveData();
                nanoModeler.setCtrlPressed(false);
            }

        } else {
            if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                nanoModeler.delete();
            }
            if (e.getKeyCode() == KeyEvent.VK_L) {
                nanoModeler.showLDOS();
            }
        }
        if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
            nanoModeler.setCtrlPressed(false);
        }
    }
}
