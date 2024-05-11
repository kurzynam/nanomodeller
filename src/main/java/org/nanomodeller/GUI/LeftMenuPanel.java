package org.nanomodeller.GUI;

import org.nanomodeller.GUI.Shapes.ElectrodeShape;
import org.nanomodeller.GUI.ViewComponents.*;
import org.nanomodeller.Tools.StringUtils;
import org.nanomodeller.XMLMappingFiles.Parameters;
import org.nanomodeller.XMLMappingFiles.GlobalProperties;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.io.File;

import static org.nanomodeller.Globals.*;
import static org.nanomodeller.XMLMappingFiles.XMLHelper.convertObjectToXML;


public class LeftMenuPanel extends MyPanel {

    MyButton nextButton;
    MyButton alignButton = new MyButton("Align", new ImageIcon(ALIGN_BUTTON_IMAGE_PATH));
    MyButton zoomInButton = new MyButton("Zoom+", new ImageIcon(ZOOM_IN_BUTTON_IMAGE_PATH));
    MyButton zoomOutButton = new MyButton("Zoom-", new ImageIcon(ZOOM_OUT_BUTTON_IMAGE_PATH));
    MyButton addButton = new MyButton("Add Electrode", new ImageIcon(ADD_BUTTON_IMAGE_PATH));
    MyButton delButton = new MyButton("Delete Item", new ImageIcon(DELETE_BUTTON_IMAGE_PATH));
    MyButton saveButton = new MyButton("Save", new ImageIcon(SAVE_BUTTON_IMAGE_PATH));
    MyButton clearButton = new MyButton("Clear", new ImageIcon(CLEAR_BUTTON_IMAGE_PATH));
    MyButton refreshButton = new MyButton("Refresh", new ImageIcon(REFRESH_BUTTON_IMAGE_PATH));


    ConsolasFontLabel timeLabel;
    public MyTextField timeTextField;
    FileBrowser fileBrowser;
    boolean enableScrollListener = true;
    NanoModeller modeller;


    public LeftMenuPanel(){
        super("");
        this.modeller = NanoModeller.getInstance();
        initializeComponents();
        initializeLayout();
        initializeEvents();
    }
    private void initializeComponents(){
        nextButton = new MyButton("NEXT");
        timeLabel = new ConsolasFontLabel(Color.WHITE,"Duration", 22);
        timeTextField = new MyTextField();
        fileBrowser = new FileBrowser(this);
        zoomInButton.setToolTipText("Ctrl+");
        zoomOutButton.setToolTipText("Ctrl-");
        addButton.setToolTipText("Ctrl + double mouse click");
        saveButton.setToolTipText("Ctrl + S");
        clearButton.setToolTipText("Ctrl + Del");
        delButton.setToolTipText("Delete");
        refreshButton.setToolTipText("Ctrl + R");

    }


    public class ActiveString {
        public String text;
        public boolean isActive;

        public void swapActive(){
            this.isActive = !this.isActive;
        }
        public  ActiveString(String text, boolean isActive){
            this.text = text;
            this.isActive = isActive;
        }
        public  ActiveString(ActiveString aString){
            this.text = aString.text;
            this.isActive = aString.isActive;
        }
        public String toString(){
            return text;
        }
    }
    private void initializeLayout(){
        GridBagLayout layout = new GridBagLayout();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = (int)screenSize.getWidth();
        int height = (int)screenSize.getHeight();
        setLayout(layout);
        GridBagConstraints pointer = new GridBagConstraints();
        pointer.fill = GridBagConstraints.HORIZONTAL;
        pointer.weightx = 0.5;
        pointer.weighty = 0.5;
        pointer.gridx = 0;
        pointer.gridy = 0;
        pointer.gridwidth = 2;
        pointer.gridy++;


        pointer.gridwidth = 1;
        add(timeLabel,pointer);
        pointer.gridx++;
        add(timeTextField, pointer);
        pointer.gridx--;
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
            int diameter = 2 * this.modeller.getGridSize();
            Rectangle2D newElectrode = new Rectangle2D.Float(10, 10, diameter, diameter);
            this.modeller.getElectrodes().add(new ElectrodeShape(newElectrode, this.modeller.electrodeIDSeq()));
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
        //ActiveString element = (ActiveString)(modeller.getList().getSelectedValue());
        String time = timeTextField.getText();
        String name = fileBrowser.getNodes().get(fileBrowser.getSelectedFiles()[0]).getPath()[0].toString();//element.text;
        if (!name.isEmpty() && !time.isEmpty()){
//            if (!modeller.getListModel().contains(element)) {
//                modeller.getListModel().addElement(element);
//            }
            //GlobalProperties gp = modeller.mapGlobalPropertiesObject( time, name, true);;
            modeller.saveData();
           // modeller.getList().setSelectedIndex(modeller.getListModel().indexOf(name));
        }
    }

}
