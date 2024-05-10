package org.nanomodeller.GUI;

import org.nanomodeller.GUI.Shapes.ElectrodeShape;
import org.nanomodeller.GUI.ViewComponents.*;
import org.nanomodeller.Globals;
import org.nanomodeller.Tools.StringUtils;
import org.nanomodeller.XMLMappingFiles.Parameters;
import org.nanomodeller.XMLMappingFiles.GlobalChainProperties;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.io.File;

import static org.nanomodeller.Globals.*;
import static org.nanomodeller.XMLMappingFiles.XMLHelper.convertObjectToXML;
import static org.nanomodeller.XMLMappingFiles.XMLHelper.readParametersFromXMLFile;


public class StepRecorder extends MyPanel {

    MyButton nextButton;
    MyButton alignButton = new MyButton("Align", new ImageIcon(ALIGN_BUTTON_IMAGE_PATH));
    MyButton zoomInButton = new MyButton("Zoom+", new ImageIcon(ZOOM_IN_BUTTON_IMAGE_PATH));
    MyButton zoomOutButton = new MyButton("Zoom-", new ImageIcon(ZOOM_OUT_BUTTON_IMAGE_PATH));
    MyButton addButton = new MyButton("Add Electrode", new ImageIcon(ADD_BUTTON_IMAGE_PATH));
    MyButton delButton = new MyButton("Delete Item", new ImageIcon(DELETE_BUTTON_IMAGE_PATH));
    MyButton saveButton = new MyButton("Save", new ImageIcon(SAVE_BUTTON_IMAGE_PATH));
    MyButton clearButton = new MyButton("Clear", new ImageIcon(CLEAR_BUTTON_IMAGE_PATH));
    MyButton refreshButton = new MyButton("Refresh", new ImageIcon(REFRESH_BUTTON_IMAGE_PATH));

    MyButton upButton;
    MyButton downButton;
    ConsolasFontLabel timeLabel;
    ConsolasFontLabel stepsLabel;
    MyTextField timeTextField;
    FileBrowser fileBrowser;
    boolean enableScrollListener = true;
    int previousIndex;
    NanoModeller modeller;

    public NanoModeller getModeller() {
        return modeller;
    }

    public void setModeller(NanoModeller modeller) {
        this.modeller = modeller;
    }

    public StepRecorder(NanoModeller modeller){
        super("");
        this.modeller = modeller;
        initializeComponents();
        initializeList();
        initializeLayout();
        initializeEvents();
    }
    private void initializeComponents(){
        nextButton = new MyButton("NEXT");
        upButton = new MyButton("Move step UP");
        downButton = new MyButton("Move step DOWN");
        timeLabel = new ConsolasFontLabel(Color.WHITE,"Duration", 22);
        stepsLabel = new ConsolasFontLabel(Color.WHITE,"Steps", 22);
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
    private void initializeList(){
        String[] paths = null;
        GlobalChainProperties gp = readParametersFromXMLFile(Globals.XML_FILE_PATH);
        modeller.setListModel(new DefaultListModel());
        for (Parameters p : gp.getParameters()) {
            modeller.getListModel().addElement(new ActiveString(p.getName(),p.getIsActive()));
        }
        modeller.setList(new JList());
        modeller.getList().setFont(new Font("Consolas", Font.PLAIN, 20));
        modeller.getList().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        modeller.getList().setModel(modeller.getListModel());
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
        JScrollPane scrollpane = new JScrollPane(modeller.getList());
        scrollpane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollpane.setMinimumSize(new Dimension(width/11, height/5));
        setLayout(layout);
        GridBagConstraints pointer = new GridBagConstraints();
        pointer.fill = GridBagConstraints.HORIZONTAL;
        pointer.weightx = 0.5;
        pointer.weighty = 0.5;
        pointer.gridx = 0;
        pointer.gridy = 0;
        pointer.gridwidth = 2;
        add(stepsLabel,pointer);
        pointer.gridy++;

        pointer.gridwidth = 1;
        add(upButton,pointer);
        pointer.gridx++;
        add(downButton, pointer);
        pointer.gridx--;
        pointer.gridy++;

        pointer.gridwidth = 2;
        add(scrollpane, pointer);
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
        refreshButton.addActionListener(evt -> modeller.refresh(modeller.getCurrentDataPath()));
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
        upButton.addActionListener((ActionEvent event) -> {
            if (modeller.getList().getSelectedValue() != null) {
                int selectedIndex = modeller.getList().getSelectedIndex();
                if (selectedIndex > 0) {
                    swapElements(selectedIndex, selectedIndex - 1);
                    modeller.getList().setSelectedIndex(selectedIndex - 1);
                }
                previousIndex = selectedIndex;
            }
        });

        downButton.addActionListener((ActionEvent event) -> {
            if (modeller.getList().getSelectedValue() != null) {
                int selectedIndex = modeller.getList().getSelectedIndex();
                if (selectedIndex < modeller.getListModel().getSize() - 1) {
                    swapElements(selectedIndex, selectedIndex + 1);
                    modeller.getList().setSelectedIndex(selectedIndex + 1);
                }
                previousIndex = selectedIndex;
            }
        });

        ListSelectionListener listSelectionListener = listSelectionEvent -> {
            if (enableScrollListener){
                GlobalChainProperties gp = readParametersFromXMLFile(Globals.XML_FILE_PATH);
                if (modeller.getList().getSelectedValue() == null)
                    return;
                String selectedVal = getCurrentStepName();
                modeller.refresh(selectedVal, timeTextField);
                modeller.setCurrentDataPath(selectedVal);
                modeller.setTime(timeTextField.getText());
                Parameters par = gp.getParamByName(selectedVal);
                if (par != null) {
                    fileBrowser.navigateToPath(par.getPath());
                }
            }
        };
        modeller.getList().addListSelectionListener(listSelectionListener);
        MouseAdapter ma = new MouseAdapter() {
            private void myPopupEvent(MouseEvent e) {
                PopUpMenu menu = new PopUpMenu();
                menu.show(e.getComponent(), e.getX(), e.getY());
            }
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) myPopupEvent(e);
            }
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) myPopupEvent(e);
            }
        };
        modeller.getList().addMouseListener(ma);
    }
    private class PopUpMenu extends JPopupMenu {

        private static final long serialVersionUID = 1L;
        JMenuItem cloneStepItem;
        JMenuItem renameStepItem;
        JMenuItem removeStepItem;
        JMenuItem setSubStepsNumItem;
        JMenuItem activateItem;
        public PopUpMenu(){
            cloneStepItem = new MyMenuItem("Clone step","img/addStepIcon.png", 50, 50);
            renameStepItem = new MyMenuItem("Rename step","img/renameStepIcon.png", 50, 50);
            removeStepItem = new MyMenuItem("Remove step", "img/deleteStepIcon.png",50, 50);
            setSubStepsNumItem = new MyMenuItem("Set number of substeps","img/numOfSubstepsIcon.png", 50, 50);
            activateItem = new MyMenuItem("Activate/Desactivate","img/numOfSubstepsIcon.png", 50, 50);
            add(cloneStepItem);
            add(renameStepItem);
            add(removeStepItem);
            add(setSubStepsNumItem);
            add(activateItem);
            cloneStepItem.addActionListener(evt -> cloneStep());
            renameStepItem.addActionListener(evt -> changeStepName());
            setSubStepsNumItem.addActionListener(evt -> setNumOfSubSteps());
            removeStepItem.addActionListener(evt -> deleteStep());
            activateItem.addActionListener(evt -> activate());
        }
    }

    private void activate() {
        GlobalChainProperties gp = readParametersFromXMLFile(Globals.XML_FILE_PATH);
        Parameters par = gp.getParamByName(getCurrentStepName());
        par.setActive(!par.getIsActive());
        ((ActiveString)modeller.getList().getSelectedValue()).swapActive();
        modeller.saveData(gp);
        modeller.getList().repaint();
    }

    private void deleteStep() {
        if (modeller.getListModel().getSize() > 1) {
            int n = JOptionPane.showConfirmDialog(
                    this.modeller,
                    "Are you sure you want to delete this step?",
                    "Removal Confirmation",
                    JOptionPane.YES_NO_OPTION);
            if (n == JOptionPane.YES_OPTION) {
                GlobalChainProperties gp = null;
                gp = readParametersFromXMLFile(Globals.XML_FILE_PATH);
                ActiveString element = ((ActiveString)modeller.getList().getSelectedValue());
                int pos = modeller.getList().getSelectedIndex();
                if (pos > 0){
                    pos--;
                }
                if (!element.text.isEmpty()) {
                    modeller.getListModel().removeElement(element);
                    Parameters p = gp.getParamByName(element.text);
                    gp.deleteParameter(p);
                    convertObjectToXML(gp);
                }
                if (!element.text.isEmpty()) {
                    modeller.getList().setSelectedIndex(pos);
                } else {
                    modeller.clearAll();
                }
            }
        }
        else {
            JOptionPane.showMessageDialog(this.modeller, "There is only one step left. You cannot delete it !");
        }
    }
    private void addStep() {
        String name = (String)JOptionPane.showInputDialog(
                this.modeller,
                "Step name:",
                "New step",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                getCurrentStepName());
        if(StringUtils.isEmpty(name)){
            return;
        }
        int selectedIndex = modeller.getList().getSelectedIndex();

        ActiveString element = ((ActiveString)modeller.getList().getSelectedValue());
        String time = timeTextField.getText();
        if (!name.isEmpty() && !time.isEmpty()){
            if (!modeller.getListModel().contains(name)) {
                modeller.getListModel().add(selectedIndex + 1, name);
            }
            GlobalChainProperties gp = readParametersFromXMLFile(Globals.XML_FILE_PATH);
            Parameters p = new Parameters();
            p.setName(element.text);
            gp.addParameters(p);
            modeller.saveData(gp);
            modeller.getList().setSelectedIndex(modeller.getListModel().indexOf(name));
        }
    }

    private void cloneStep() {
        String name = (String)JOptionPane.showInputDialog(
                this.modeller,
                "Step name:",
                "New step",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                getCurrentStepName());
        if(StringUtils.isEmpty(name)){
            return;
        }
        int selectedIndex = modeller.getList().getSelectedIndex();

        ActiveString copiedElement = ((ActiveString)modeller.getList().getSelectedValue());
        ActiveString element = new ActiveString(name, copiedElement.isActive);
        String time = timeTextField.getText();
        if (!name.isEmpty() && !time.isEmpty()){
            if (!modeller.getListModel().contains(name)) {
                modeller.getListModel().add(selectedIndex + 1, element);
            }
            GlobalChainProperties gp = modeller.mapGlobalPropertiesObject( time, name, element.isActive);;
            modeller.saveData(gp);
            modeller.getList().setSelectedIndex(selectedIndex + 1);
            modeller.getList().repaint();
        }
    }

    private void setNumOfSubSteps() {
        GlobalChainProperties gp = readParametersFromXMLFile(Globals.XML_FILE_PATH);
        Parameters par = gp.getParamByName(getCurrentStepName());
        if(StringUtils.isNotEmpty(par.getPath())) {
            int n = JOptionPane.showConfirmDialog(
                    this.modeller,
                    "Content of assigned directory will be cleared. Would you like to proceed?",
                    "Removal Confirmation",
                    JOptionPane.YES_NO_OPTION);
            if (n == JOptionPane.YES_OPTION) {

                Integer newValue = Integer.parseInt((String) JOptionPane.showInputDialog(
                        this.modeller,
                        "Insert number of substeps (min 2):",
                        "Substeps settings",
                        JOptionPane.PLAIN_MESSAGE,
                        null,
                        null,
                        ""));
                if (newValue > 0) {
                    File assignedDir = fileBrowser.getAssignedFileNode();
                    deleteFolderContent(assignedDir);
                    fileBrowser.getAssignedNode().removeAllChildren();
                    for (int i = 0; i < newValue; i++){
                        fileBrowser.createDir(i + "", true);
                    }
                    fileBrowser.reload();
                    par.setNumOfSubSteps(newValue);
                    modeller.saveData(gp);
                }
                else if (newValue > 0){
                    JOptionPane.showMessageDialog(this.modeller, "Value cannot be less that one!");
                }
            }
        } else {
            JOptionPane.showMessageDialog(this.modeller, "Please assign any directory to that step first!");
        }
    }

    public static void deleteFolderContent(File folder) {
        File[] files = folder.listFiles();
        if(files != null) {
            for(File f: files) {
                if(f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    f.delete();
                }
            }
        }
    }
    public static void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if(files!=null) {
            for(File f: files) {
                if(f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }
    private void changeStepName() {
        String newValue = (String)JOptionPane.showInputDialog(
                this.modeller,
                "New step name:",
                "Rename step",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                getCurrentStepName());
        if(StringUtils.isEmpty(newValue)){
            return;
        }
        String time = timeTextField.getText();
        int selectedIndex = modeller.getList().getSelectedIndex();
        ActiveString element = ((ActiveString)modeller.getList().getSelectedValue());
        if (!newValue.isEmpty() && !time.isEmpty()){
            modeller.getListModel().removeElement(element);
            if (!modeller.getListModel().contains(newValue)) {
                modeller.getListModel().add(selectedIndex, newValue);
            }
            GlobalChainProperties gp = modeller.mapGlobalPropertiesObject(time, newValue, element.isActive);;
            modeller.saveData(gp);
            modeller.getList().setSelectedIndex(modeller.getListModel().indexOf(newValue));
        }
    }
    public String getCurrentStepName() {
        return modeller.getList().getSelectedValue().toString();
    }
    public void saveStep() {
        ActiveString element = (ActiveString)(modeller.getList().getSelectedValue());
        String time = timeTextField.getText();
        String name = element.text;
        if (!name.isEmpty() && !time.isEmpty()){
            if (!modeller.getListModel().contains(element)) {
                modeller.getListModel().addElement(element);
            }
            GlobalChainProperties gp = modeller.mapGlobalPropertiesObject( time, name, element.isActive);;
            modeller.saveData(gp);
            modeller.getList().setSelectedIndex(modeller.getListModel().indexOf(name));
        }
    }
    public void swapElements(int pos1, int pos2) {
        ActiveString tmp = (ActiveString) modeller.getListModel().get(pos1);
        modeller.getListModel().set(pos1, modeller.getListModel().get(pos2));
        modeller.getListModel().set(pos2, tmp);
        String selectedVal = getCurrentStepName();
        modeller.refresh(selectedVal, timeTextField);
    }

}
