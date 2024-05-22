package org.nanomodeller.GUI;

import org.nanomodeller.GUI.ViewComponents.MyMenuItem;

import javax.swing.*;

public class PopUpMenu extends JPopupMenu {

    private static final long serialVersionUID = 1L;
    JMenuItem copyItem;
    JMenuItem pasteItem;
    JMenuItem flipVerticalItem;
    JMenuItem flipHorizontalItem;

    JMenuItem propertiesItem;

    public PopUpMenu() {
        copyItem = new MyMenuItem("Copy", new ImageIcon("img/copyIcon.png"));
        pasteItem = new MyMenuItem("Paste", new ImageIcon("img/pasteIcon.png"));
        flipVerticalItem = new MyMenuItem("Flip vertically", new ImageIcon("img/flipRight.png"));
        flipHorizontalItem = new MyMenuItem("Flip horizontally", new ImageIcon("img/flipUp.png"));
        propertiesItem = new MyMenuItem("Properties", new ImageIcon("img/flipUp.png"));
        int selElSize = NanoModeler.getInstance().getSelectedElectrodes().size();
        int selAtSize = NanoModeler.getInstance().getSelectedAtoms().size();
        int selBondSize = NanoModeler.getInstance().getSelectedBonds().size();
        int copiedAtSize = NanoModeler.getInstance().getCopiedAtoms().size();
        int copiedElSize = NanoModeler.getInstance().getCopiedElectrodes().size();
        if (copiedAtSize > 0 || copiedElSize > 0)
            add(pasteItem);
        if (selElSize > 0 ||
            selAtSize > 0){
            add(copyItem);
            add(flipVerticalItem);
            add(flipHorizontalItem);
        }
        if (selElSize + selAtSize + selBondSize <= 1) {
            add(propertiesItem);
            remove(flipVerticalItem);
            remove(flipHorizontalItem);
        }
        copyItem.addActionListener(evt -> NanoModeler.getInstance().copy());
        flipHorizontalItem.addActionListener(evt -> NanoModeler.getInstance().flipHorizontally());
        flipVerticalItem.addActionListener(evt -> NanoModeler.getInstance().flipVertically());
        pasteItem.addActionListener(evt -> NanoModeler.getInstance().paste());
        propertiesItem.addActionListener(evt -> NanoModeler.getInstance().showAtomPropertiesTextArea());
    }

}
