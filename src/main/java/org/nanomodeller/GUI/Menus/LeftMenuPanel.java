package org.nanomodeller.GUI.Menus;

import org.nanomodeller.GUI.NanoModeler;
import org.nanomodeller.GUI.ViewComponents.*;
import org.nanomodeller.XMLMappingFiles.Electrode;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;

import static org.nanomodeller.Globals.*;

public class LeftMenuPanel extends MyPanel {

    JLabel logo = new JLabel();
    MyButton alignButton = new MyButton("Align", new ImageIcon(ALIGN_BUTTON_IMAGE_PATH));
    MyButton zoomInButton = new MyButton("Zoom+", new ImageIcon(ZOOM_IN_BUTTON_IMAGE_PATH));
    MyButton zoomOutButton = new MyButton("Zoom-", new ImageIcon(ZOOM_OUT_BUTTON_IMAGE_PATH));
    MyButton addButton = new MyButton("Add Electrode", new ImageIcon(ADD_BUTTON_IMAGE_PATH));
    MyButton delButton = new MyButton("Delete Item", new ImageIcon(DELETE_BUTTON_IMAGE_PATH));
    MyButton saveButton = new MyButton("Save", new ImageIcon(SAVE_BUTTON_IMAGE_PATH));
    MyButton clearButton = new MyButton("Clear", new ImageIcon(CLEAR_BUTTON_IMAGE_PATH));
    MyButton refreshButton = new MyButton("Refresh", new ImageIcon(REFRESH_BUTTON_IMAGE_PATH));

    public FileBrowser fileBrowser;
    boolean enableScrollListener = true;
    NanoModeler modeller;


    public LeftMenuPanel(){
        super("");
        this.modeller = NanoModeler.getInstance();
        initializeComponents();
        initializeLayout();
        initializeEvents();
    }
    private void initializeComponents(){

        ImageIcon icon = new ImageIcon(LOGO_IMAGE_PATH);
        Image img = icon.getImage();
        Image newimg = img.getScaledInstance(170, 180, Image.SCALE_SMOOTH);
        ImageIcon imIc = new ImageIcon(newimg);
        logo.setIcon(imIc);
        fileBrowser = new FileBrowser(this);
        zoomInButton.setToolTipText("Ctrl+");
        zoomOutButton.setToolTipText("Ctrl-");
        addButton.setToolTipText("Ctrl + double mouse click");
        saveButton.setToolTipText("Ctrl + S");
        clearButton.setToolTipText("Ctrl + Del");
        delButton.setToolTipText("Delete");
        refreshButton.setToolTipText("Ctrl + R");

    }

    private void initializeLayout(){
        GridBagLayout layout = new GridBagLayout();

        setLayout(layout);
        add(logo);
        GridBagConstraints pointer = new GridBagConstraints();
        pointer.fill = GridBagConstraints.BOTH;

        pointer.weightx = 1;
        pointer.weighty = 1;
        pointer.gridx = 0;
        pointer.gridy = 0;
        pointer.gridy++;


        pointer.gridwidth = 2;
        pointer.gridy++;
        add(fileBrowser,pointer);
        pointer.gridy++;
        add(addButton, pointer);
        pointer.gridy++;
        add(delButton, pointer);
        pointer.gridy++;
        add(saveButton, pointer);
        pointer.gridy++;
        add(clearButton, pointer);
        pointer.gridy++;
        add(refreshButton, pointer);
        pointer.gridy++;
        add(alignButton, pointer);
        pointer.gridy++;
        add(zoomInButton, pointer);
        pointer.gridy++;
        add(zoomOutButton, pointer);
    }
    private void initializeEvents(){

        delButton.addActionListener(evt -> modeller.delete());
        clearButton.addActionListener(evt -> modeller.clearAll());
        refreshButton.addActionListener(evt -> modeller.refresh());
        zoomInButton.addActionListener(evt -> this.modeller.zoom(2));
        zoomOutButton.addActionListener(evt -> this.modeller.zoom(-2));
        addButton.addActionListener(evt -> {
            Electrode el = new Electrode(-1 , this.modeller.electrodeIDSeq(), 30, 30);
            this.modeller.getElectrodes().put(el.getID(), el);
            this.modeller.repaint();
        });
        saveButton.addActionListener(evt -> {
            this.modeller.getStepRecorder().saveStep();

        });
        alignButton.addActionListener(evt -> {
            this.modeller.align();
        });
    }
    public void saveStep() {
        modeller.saveData();
    }

}
