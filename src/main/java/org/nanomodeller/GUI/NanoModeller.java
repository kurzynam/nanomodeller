package org.nanomodeller.GUI;

import org.nanomodeller.*;
import org.nanomodeller.Dynamics.TimeEvolutionHelper;
import org.nanomodeller.GUI.Shapes.AtomBound;
import org.nanomodeller.GUI.Shapes.AtomShape;
import org.nanomodeller.GUI.Shapes.ElectrodeShape;
import org.nanomodeller.GUI.ViewComponents.*;
import org.nanomodeller.Tools.DataAccessTools.MyFileWriter;
import org.nanomodeller.Tools.Flag;
import org.nanomodeller.Tools.JGnuPlot;
import org.nanomodeller.Tools.StringUtils;
import org.nanomodeller.XMLMappingFiles.*;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.*;
import java.io.*;
import java.util.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import static org.nanomodeller.Tools.DataAccessTools.FileOperationHelper.runFile;
import static org.nanomodeller.Tools.DataAccessTools.MyFileWriter.saveBlockGivenT;
import static org.nanomodeller.Tools.DataAccessTools.OverwriteGnuplotFile.*;
import static org.nanomodeller.Globals.*;
import static org.nanomodeller.Tools.StringUtils.nvl;
import static org.nanomodeller.XMLMappingFiles.XMLHelper.convertObjectToXML;
import static org.nanomodeller.XMLMappingFiles.XMLHelper.readParametersFromXMLFile;

public class NanoModeller extends JFrame {

    private static final long serialVersionUID = 1L;
    private int x;
    private int y;
    private double selectionXMax = -1;
    private double selectionXMin = -1;
    private double selectionYMax = -1;
    private double selectionYMin = -1;
    private Thread dynamicCalculationsThread;
    private String surfaceCoupling;
    private String kFa;
    private String dE;
    private String bColor;
    private String dt;
    private String energyRange;
    private double screenWidth;
    private double screenHeight;
    private PaintSurface paintSurface = new PaintSurface();
    private JScrollPane scrollPane = new JScrollPane(getPaintSurface());
    private ArrayList<AtomShape> shapes = new ArrayList<AtomShape>();
    private ArrayList<AtomBound> bounds = new ArrayList<AtomBound>();
    private ArrayList<ElectrodeShape> electrodes = new ArrayList<ElectrodeShape>();
    private ArrayList<AtomShape>  selectedAtoms = new ArrayList<AtomShape>();
    private ArrayList<ElectrodeShape> selectedElectrodes = new ArrayList<ElectrodeShape>();
    private ArrayList<ElectrodeShape> copiedElectrodes = new ArrayList<ElectrodeShape>();
    private ArrayList<AtomBound> selectedBounds = new ArrayList<AtomBound>();
    private ArrayList<AtomShape> copiedAtoms = new ArrayList<AtomShape>();
    private ArrayList<AtomBound> copiedBounds = new ArrayList<AtomBound>();
    private int gridSize;
    private boolean selectionFlag = false;
    private boolean showGrid = true;
    private Flag isInterupted;
    private Flag isCanceled;
    private AtomShape highlightedShape = null;
    private AtomBound highlightedBound = null;
    private ElectrodeShape highlightedElectrode = null;
    private Rectangle selection = null;
    private Point anchor;
    private AtomShape currentAtom = null;
    private Menu menu;
    private StepRecorder stepRecorder;
    private int maxFileNum = 0;
    private boolean ctrlPressed = false;
    private String currentDataPath;
    private String time = "0.0";
    private boolean isActive = true;
    private DefaultListModel listModel;
    private JList list;
    private JList stepsList;
    private String stepCount = "0";

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public AtomShape getAtomByID(int id){
        for (AtomShape shape : getShapes()){
            if (shape.getID() == id){
                return shape;
            }
        }
        return null;
    }
    public NanoModeller() {

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setScreenWidth(screenSize.getWidth()/4);
        setScreenHeight(screenSize.getHeight()/4);
        ImageIcon icon = new ImageIcon(ICON_IMAGE_PATH);
        Image img = icon.getImage() ;
        setIconImage(img);
        setMenu(new Menu());
        setStepRecorder(new StepRecorder(this));
        getStepRecorder().setPreferredSize(new Dimension((int)screenWidth/2, (int)screenHeight/2));
        new File("undo").mkdir();
        readData(null, false);
        setTitle(APP_NAME);
        this.getList().setSelectedIndex(0);
        getPaintSurface().setPreferredSize(new Dimension((int)(getGridSize() * getScreenWidth()),(int)(getGridSize() * getScreenHeight())));
        this.setSize((int)screenWidth * 3, (int)screenHeight * 3);
        getScrollPane().setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        getScrollPane().setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setBackground(Color.BLACK);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setIsInterupted(new Flag(false));
        setIsCanceled(new Flag(false));
        this.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                File directory = new File("undo");
                if (directory.exists()){
                    for (File f : directory.listFiles()){
                        f.delete();
                    }
                }
                directory.delete();
            }
        });
        this.add(getScrollPane(), BorderLayout.CENTER);
        this.add(getMenu(), BorderLayout.EAST);
        this.add(getStepRecorder(), BorderLayout.WEST);
        this.setVisible(true);
        getScrollPane().getVerticalScrollBar().setValue(getScrollPane().getVerticalScrollBar().getValue()+ 1);
        MovingAdapter ma = new MovingAdapter();
        getPaintSurface().addMouseListener(ma);
        getPaintSurface().addMouseMotionListener(ma);
        getPaintSurface().addMouseWheelListener(ma);
        getPaintSurface().addKeyListener(new MyKeyAdapter());
        getPaintSurface().requestFocus();
        setJMenuBar(new org.nanomodeller.GUI.Menu(this));
        setVisible(true);
    }

    public void readData(String path, boolean refreshData){
        readXMLData(path, refreshData, null);
    }
    public void readXMLData(String path, boolean refreshData, MyTextField time) {
        GlobalChainProperties gp;
        String XMLPath =  XML_FILE_PATH;
        gp = readParametersFromXMLFile(XMLPath);
        readDataFromObject(gp, path, refreshData, time);
    }

    public void readDataFromObject(GlobalChainProperties gp, String stepName, boolean refreshData, MyTextField time){
        Parameters p = null;
        getShapes().clear();
        bounds.clear();
        getElectrodes().clear();
        int diameter = 2 * getGridSize();
        if (StringUtils.isEmpty(stepName)){
            p = gp.getParameters().get(0);
        }
        else {
            p = gp.getParamByName(stepName);
        }
        getMenu().dTTextField.setText(getDt());
        setbColor(gp.getColor());
        getMenu().colorBox.setSelectedItem(nvl(getbColor(), Globals.WHITE));
        this.setdE(gp.getdE() + "");
        this.setDt(gp.getDt() + "");
        this.setEnergyRange(gp.getEnergyRange());
        getMenu().energyRangeTextField.setText(getEnergyRange());
        if (p != null) {
            if (!refreshData) {
                setGridSize(Integer.parseInt(p.getGridSize()));
                diameter = 2 * getGridSize();
            }
            if (time != null){
                time.setText(p.getTime());
            }
            this.setSurfaceCoupling(p.getSurfaceCoupling());
            getMenu().surfaceCouplingTextField.setText(getSurfaceCoupling());
            this.setkFa(p.getkFa());
            getMenu().kfaTextField.setText(getkFa());

            for (Atom atom : p.getAtoms()){
                AtomShape newAtom = new AtomShape(new Ellipse2D.Float(Float.parseFloat(atom.getX()) * getGridSize(),
                        Float.parseFloat(atom.getY()) * getGridSize(), diameter, diameter),
                        atom);
                if (!getMenu().model.contains(atom.getID()))
                    getMenu().model.addElement(atom.getID());
                getShapes().add(newAtom);

            }
            for(Electrode e : p.getElectrodes()){
                Line2D line2d = null;
                int index = e.getIntAtomIndex();
                AtomShape atom = index > -1 ? getAtomByID(index) : null;
                if (atom != null){
                    line2d = new Line2D.Float(Float.parseFloat(e.getX()) * getGridSize() + (float) atom.getShape().getWidth() / 2,
                            Float.parseFloat(e.getY()) * getGridSize() + (float) atom.getShape().getHeight() / 2,
                            (float) atom.getShape().getX() + (float) atom.getShape().getWidth() / 2,
                            (float) atom.getShape().getY() + (float) atom.getShape().getHeight() / 2);

                }
                ElectrodeShape newElectrode = new ElectrodeShape(atom, new Rectangle2D.Float(Float.parseFloat(e.getX()) * getGridSize(),
                        Float.parseFloat(e.getY()) * getGridSize(), diameter, diameter), line2d, e);
                getElectrodes().add(newElectrode);
            }
            for(Bound b : p.getBounds()){
                AtomShape shape = getAtomByID(b.getFirst());
                AtomShape prevShape = getAtomByID(b.getSecond());
                AtomBound loadedBound = new AtomBound(shape.getAtom(), prevShape.getAtom(), null, b);
                loadedBound.updateLine(shape, prevShape);
                bounds.add(loadedBound);
            }
        }
    }

    private void countStaticProperties() {
        GlobalChainProperties gp = mapGlobalPropertiesObject(getTime(), getCurrentDataPath(), getIsActive());
        saveData(gp);
        StaticProperties.countStaticProperties(getCurrentDataPath());
        ToastMessage toastMessage = new ToastMessage("LDOS counting finished ", TOAST_MESSAGE_DURATION, this);
        toastMessage.setVisible(true);
    }
    private void showNormalisation() {
        runGnuplotThread(SNORM_FILE_NAME_PATTERN, false, true);
    }

//    private void showCharge() {
//        if (getHighlightedShape() != null && (getSelectedAtoms() == null || getSelectedAtoms().size() == 0)) {
//            String path = stepRecorder.fileBrowser.getSelectedFileNode().getPath();
//            int index = getHighlightedShape().getID();
//            overwriteDynamicChargeFile(index, path);
//            runFile(Globals.DYNAMIC_CHARGE_GNUPLOT_FILE_PATH);
//
//        }
//    }
//
//    private void showFermiLDOS() {
//        if (getHighlightedShape() != null && (getSelectedAtoms() == null || getSelectedAtoms().size() == 0)) {
//            String path = stepRecorder.fileBrowser.getSelectedFileNode().getPath();
//            int index = getHighlightedShape().getID();
//            overwriteDynamicFermiLDOSFile(index, path);
//            runFile(Globals.DYNAMIC_FERMI_LDOS_GNUPLOT_FILE_PATH);
//
//        }
//    }

    private void runGnuplotThread(String filePattern, boolean is3D){
        runGnuplotThread(filePattern, is3D, false);
    }
    private void runGnuplotThread(String filePattern, boolean is3D, boolean isStatic){
        GlobalChainProperties gp;
        gp = readParametersFromXMLFile(Globals.XML_FILE_PATH);
        boolean isMultiplot = Globals.MULTIPLOT.equals(gp.getMultiplotStyle());
        if (false/*is3D && !isMultiplot*/) {
            runGnuplotThreads(filePattern,is3D, isStatic);
        }
        else{
            Runnable myRunnable = () -> showPlot(filePattern, is3D, isMultiplot);
            Thread t = new Thread(myRunnable);
            t.start();
        }
    }

    private void runGnuplotThreads(String filePattern, boolean is3D, boolean isStatic){
        if (getSelectedAtoms() != null && getSelectedAtoms().size() > 0) {
            TreePath[] selectedFilesPATHS = stepRecorder.fileBrowser.getTree().getSelectionPaths();
            ArrayList<String> selectedSteps = new ArrayList<>();
            DefaultMutableTreeNode lastPathComponent = (DefaultMutableTreeNode) selectedFilesPATHS[0].getLastPathComponent();
            FileBrowser.FileNode fn = (FileBrowser.FileNode) (lastPathComponent.getUserObject());
            String paths;
            if (fn.isHidden()) {
                for (TreePath path : selectedFilesPATHS) {
                    selectedSteps.add(path.getLastPathComponent().toString());
                }
                paths = fn.getParent();
            } else {
                paths = fn.getPath();
            }
            paths += String.format("%s.csv", "/" + filePattern);
                Collections.sort(selectedSteps);
                if (!is3D) {
                } else {
                    for (int j = 0; j < selectedSteps.size(); j++) {
                        String step = selectedSteps.get(j);
                        for (int i = 0; i < selectedAtoms.size(); i++) {
                            String finalPaths = paths;
                            int finalI = selectedAtoms.get(i).getID();
                            Runnable myRunnable = () -> runPlot(is3D, finalPaths, step, finalI);
                            Thread t = new Thread(myRunnable);
                            t.start();

                        }
                    }
                }
            }

    }

    private void runPlot(boolean is3D, String paths, String step, int i) {
        JGnuPlot plot = new JGnuPlot(is3D);
        plot.setSamples(1000);
        plot.readXMLGraphProperties();
        String command = "splot ";
        int id = shapes.get(i).getID() + 4;
        String ids = "2:3:" + id;
        command += StringUtils.toSingleQuotes(paths) + String.format(" i %s ", step) + " u " + ids + " title " + StringUtils.toSingleQuotes("");
        command += " with pm3d";
        plot.appendCommand(command);
        plot.pause(1000);
        plot.plot();
    }

    private void showPlot(String filePattern, boolean is3D, boolean isMultiplot){

        if (getSelectedAtoms() != null && getSelectedAtoms().size() > 0) {
            TreePath[] selectedFilesPATHS = stepRecorder.fileBrowser.getTree().getSelectionPaths();
            ArrayList<String> selectedSteps = new ArrayList<>();
            DefaultMutableTreeNode lastPathComponent = (DefaultMutableTreeNode) selectedFilesPATHS[0].getLastPathComponent();
            FileBrowser.FileNode fn = (FileBrowser.FileNode)(lastPathComponent.getUserObject());
            String paths;
            boolean isHidden = fn.isHidden();
            boolean hasSteps = (new File(fn.getAbsolutePath()).listFiles(File::isDirectory)).length > 0;

            if (isHidden) {
                for (TreePath path : selectedFilesPATHS) {
                    selectedSteps.add(path.getLastPathComponent().toString());
                }
                paths = fn.getParent();
            }
            else {
                paths = fn.getPath();
            }
            paths += String.format("%s.csv", "/" + filePattern);
            JGnuPlot jgp = new JGnuPlot(is3D);
            jgp.setSamples(1000);
            jgp.readXMLGraphProperties();
//            if(isCSLDOS){
//                createCrossSectionPlot(selectedSteps, paths, jgp, isMultiplot, isHidden);
//            }else {
                if (is3D) {
                    jgp.setMultiplotStyle();
                    jgp.addSplotCommand(paths, getSelectedAtoms(), selectedSteps);
                }
                else {
                    if (isHidden || !hasSteps){
                        if (isMultiplot)
                            jgp.addPlotCommandMultiplot(paths, getSelectedAtoms(), selectedSteps, "");
                        else
                            jgp.addPlotCommand(paths, getSelectedAtoms(), selectedSteps);
                    }
                    else{
                        if (isMultiplot)
                            jgp.add2DSplotCommandMultiplot(paths, getSelectedAtoms(), "");
                        else
                            jgp.add2DSplotCommand(paths, getSelectedAtoms(), "");

                    }
                }
//            }
            jgp.pause(1000);
            jgp.plot();
        }
    }

    private void createCrossSectionPlot(ArrayList<String> selectedSteps, String paths, JGnuPlot jgp, boolean isMultiplot, boolean isHidden) {
        File myObj = new File(paths);
        Scanner myReader = null;
        try {
            myReader = new Scanner(myObj);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        int counter = 0;
        double limit = Double.parseDouble(jgp.getProperties().getCrossSectionEnergy());
        double prevDiff = 9999;
        myReader.nextLine();
//        int linesToSkip = 0;
//        MyFileWriter ldosE = new MyFileWriter(jgp.getProperties().getDynamicPATH() + "/" + LDOS_E_FILE_NAME_PATTERN + ".csv", true);
        while (myReader.hasNextLine()) {

            String data = myReader.nextLine();
            double newVal = Double.parseDouble(data.split(",")[2]);
            double newDiff = Math.abs(newVal - limit);
            if (newDiff > prevDiff){
//                linesToSkip = counter + 1;
                break;
            }
            else {
                prevDiff = newDiff;
            }
            counter++;
        }
//        while (myReader.hasNextLine()) {
//            String line = myReader.nextLine();
//            if (counter % linesToSkip == 0){
//                ldosE.println(line);
//            }
//            counter++;
//        }
        myReader.close();
        if (isHidden){
            if(isMultiplot)
                jgp.crossSectionMultiplot(paths, getSelectedAtoms(), selectedSteps, counter + "", "");
            else
                jgp.crossSection(paths, getSelectedAtoms(), selectedSteps, counter + "", "");
        }else{
            if(isMultiplot)
                jgp.crossSection3DMultiplot(paths, getSelectedAtoms(), selectedSteps, counter + "", "");
            else
                jgp.crossSection3D(paths, getSelectedAtoms(), selectedSteps, counter + "", "");
        }
    }

    private void showLDOSTimeEvolution() {
        runGnuplotThread(LDOS_FILE_NAME_PATTERN, true);
    }
    private void showNormalisationTimeEvolution() {
        runGnuplotThread(NORMALISATION_FILE_NAME_PATTERN, true);
    }
    private void showChargeTimeEvolution() {
        runGnuplotThread(CHARGE_FILE_NAME_PATTERN, false);
    }
    private void showCurrentTimeEvolution() {
        runGnuplotThread(CURRENT_FILE_NAME_PATTERN, false);
    }
    private void showFermiLDOSTimeEvolution() {
        runGnuplotThread(LDOS_E_FILE_NAME_PATTERN, false);
    }

    private void showLastT(String filePattern) {

//            saveBlockGivenT("/" + filePattern   ".csv");
//            overwriteLastTFile(filePattern, index);
//            String path = Globals.LAST_T_GNUPLOT_FILE_PATH;
//            runFile(path);
    }

    private void showLDOS() {
        runGnuplotThread(SLDOS_FILE_NAME_PATTERN, false, true);
    }
    public void clearAll() {
        setHighlightedShape(null);
        setHighlightedBound(null);
        setHighlightedElectrode(null);
        getShapes().clear();
        bounds.clear();
        getElectrodes().clear();
        getPaintSurface().repaint();
    }
    protected void delete() {
        ArrayList<AtomBound> boundsToDelete = new ArrayList<AtomBound>();

        for (AtomBound bound : bounds) {
            if (getHighlightedShape() != null) {
                if (getHighlightedShape().getShape().contains(bound.getLine().getX1(), bound.getLine().getY1())
                        || getHighlightedShape().getShape().contains(bound.getLine().getX2(), bound.getLine().getY2())) {
                    boundsToDelete.add(bound);
                }
            }
        }
        for (AtomBound bound : bounds) {
            if (getSelectedBounds().contains(bound)) {
                boundsToDelete.add(bound);
            }
        }
        for (AtomBound s : boundsToDelete) {
            bounds.remove(s);
        }
        bounds.remove(getHighlightedBound());
        setHighlightedBound(null);
        removeAtom(getHighlightedShape());
        for(ElectrodeShape e : getElectrodes()){
            if (e.getAtom() == getHighlightedShape()){
                e.setLine(null);
            }
        }
        getElectrodes().remove(getHighlightedElectrode());
        setHighlightedElectrode(null);
        //menu.model.removeElement(highlightedShape.ID);
        setHighlightedShape(null);

        for (AtomShape s : getSelectedAtoms()) {
            removeAtom(s);
            for(ElectrodeShape e : getElectrodes()){
                if (e.getAtom() == s){
                    e.setLine(null);
                }
            }
        }
        for (ElectrodeShape electrode : getSelectedElectrodes()) {
            getElectrodes().remove(electrode);
        }
        getPaintSurface().repaint();
    }

    private void removeAtom(AtomShape s) {
        if (s != null) {
            int id = s.getID();
            for (AtomShape shape : shapes) {
                int newID = shape.getID();
                if (newID > id) {
                    shape.setID(newID - 1);
                }
            }
            getShapes().remove(s);
            getMenu().model.removeLastElement();
        }

    }

    private void redo() {
        if(UndoRedoQueue.getInstance().next() != null) {
            UndoRedoQueue.getInstance().currentUp();
            readDataFromObject(UndoRedoQueue.getInstance().currentElement.value, getCurrentDataPath(), true, getStepRecorder().timeTextField);
            getPaintSurface().repaint();
        }
    }
    private void undo() {
        if(UndoRedoQueue.getInstance().prev() != null) {
            UndoRedoQueue.getInstance().currentDown();
            readDataFromObject(UndoRedoQueue.getInstance().currentElement.value, getCurrentDataPath(), true, getStepRecorder().timeTextField);
            getPaintSurface().repaint();
        }

    }
    public void refresh(String path){
        refresh(path, null);
    }
    public void refresh(String path, MyTextField time) {
        setHighlightedShape(null);
        setHighlightedBound(null);
        setHighlightedElectrode(null);
        readXMLData(path,true, time);
        getPaintSurface().repaint();
    }
    protected void zoom(int zoomMagnitude) {

        if (getGridSize() + zoomMagnitude > 2){
            double mlply = (getGridSize() + zoomMagnitude + 0.0)/(getGridSize());
            Dimension newPrefSize = new Dimension((int)(getGridSize() * getScreenWidth() * mlply), (int)(getGridSize() * getScreenHeight() * mlply));
            getPaintSurface().setPreferredSize(newPrefSize);
            rescaleShapes(mlply);
            setGridSize(getGridSize() + zoomMagnitude);
            getScrollPane().getVerticalScrollBar().setValue(getScrollPane().getVerticalScrollBar().getValue()+ 1);
            getPaintSurface().repaint();
        }
    }

    public GlobalChainProperties mapGlobalPropertiesObject(String time, String path, boolean active){
        GlobalChainProperties gp;
        gp = readParametersFromXMLFile(Globals.XML_FILE_PATH);

        Parameters p = gp.getParamByName(path);
        if (p == null){
            p = new Parameters();
            p.setName(path);
            gp.addParameters(p);
        }
        p.setId("0");
        p.setTime(time);
        p.setActive(active);
        gp.setEnergyRange(getEnergyRange());
        gp.setDt(Double.parseDouble(getDt()));
        gp.setColor(getbColor());
        p.setSurfaceCoupling(getSurfaceCoupling());
        p.setkFa(getkFa());
        p.setNumber("" + getShapes().size());
        p.setGridSize(getGridSize() + "");
        p.getAtoms().clear();
        int ii = 0;
        Collections.sort(getShapes());
        for (AtomShape s : getShapes()) {
            s.getAtom().setX(s.getShape().getBounds().x/(1.0 * getGridSize())+"");
            s.getAtom().setY(s.getShape().getBounds().y/(1.0 * getGridSize())+ "");
            p.addAtom(s.getAtom());
            s.setID(ii++);
        }
        p.getElectrodes().clear();
        ListIterator iter = getElectrodes().listIterator();
        while (iter.hasNext()){
            ElectrodeShape electrode = (ElectrodeShape)iter.next();
            Electrode electrodeToSave = electrode.getElectrode();
            electrodeToSave.setX("" + electrode.getRectangle().getBounds().x/(getGridSize() * 1.0));
            electrodeToSave.setY("" + electrode.getRectangle().getBounds().y/(getGridSize() * 1.0));
            if (electrode.getLine() != null){
                electrodeToSave.setAtomIndex(electrode.getAtom().getID());
            }
            else{
                electrodeToSave.setAtomIndex(-1);
            }
            electrodeToSave.setId(iter.nextIndex() - 1);
            p.addElectode(electrodeToSave);
        }
        p.getBounds().clear();
        for (AtomBound bound : bounds) {
            bound.updateAtoms();
            p.addBound(bound.getBound());
        }
        if (getList() != null){
            ArrayList<Parameters> paramsCopy = new ArrayList<Parameters>();
            for(int i = 0; i< getListModel().getSize(); i++){
                Parameters param = gp.getParamByName(getList().getModel().getElementAt(i).toString());
                param.setId((i+1) + "");
                paramsCopy.add(param);
            }
            gp.setParameters(paramsCopy);
        }
        return gp;
    }

    public void saveData(GlobalChainProperties gp) {
        try {
            convertObjectToXML(gp);
            ToastMessage toastMessage = new ToastMessage("Data saved", TOAST_MESSAGE_DURATION, this);
            toastMessage.setVisible(true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void rescaleShapes(double mlply){
        for (AtomShape atom : getShapes()){
            atom.getShape().setFrame(atom.getShape().getX() * mlply  , atom.getShape().getY() * mlply, atom.getShape().getWidth() * mlply, atom.getShape().getHeight() * mlply);
        }
        for (AtomBound bound : bounds){
            bound.updateLine(getShapeByID(bound.getFirst()),getShapeByID(bound.getSecond()));
        }
        for (ElectrodeShape electrode : getElectrodes()){
            electrode.getRectangle().setFrame(electrode.getRectangle().getX() * mlply  , electrode.getRectangle().getY() * mlply, electrode.getRectangle().getWidth() * mlply, electrode.getRectangle().getHeight() * mlply);
            electrode.updateLine();
        }
    }
    public void align(){
        for (AtomShape atom : getShapes()){
            int xMultiplier = (int)Math.round(atom.getShape().getX()/ getGridSize());
            int yMultiplier = (int)Math.round(atom.getShape().getY()/ getGridSize());
            double newX = xMultiplier * getGridSize();
            double newY = yMultiplier * getGridSize();
            atom.getShape().setFrame(newX, newY, atom.getShape().getWidth(), atom.getShape().getHeight());
        }
        for (AtomBound bound : bounds){
            bound.updateLine(getShapeByID(bound.getFirst()),getShapeByID(bound.getSecond()));
        }
        for (ElectrodeShape electrode : getElectrodes()){
            int xMultiplier = (int)Math.round(electrode.getRectangle().getX()/ getGridSize());
            int yMultiplier = (int)Math.round(electrode.getRectangle().getY()/ getGridSize());
            double newX = xMultiplier * getGridSize();
            double newY = yMultiplier * getGridSize();
            electrode.getRectangle().setFrame(newX, newY,
                    electrode.getRectangle().getWidth(), electrode.getRectangle().getHeight());
            electrode.updateLine();
        }
        getPaintSurface().repaint();
    }
    private void flipVertically() {
        double newSelectionXMax = getSelectionXMax();
        double selectionWidth = (getSelectionXMax() + getSelectionXMin())/2;
        for (AtomShape atom : getSelectedAtoms()){
            double newX = 2 * selectionWidth - atom.getShape().getX();
            newSelectionXMax = (newX > newSelectionXMax) ? newX : newSelectionXMax ;
            atom.getShape().setFrame(newX , atom.getShape().getY(), atom.getShape().getWidth(), atom.getShape().getHeight());
        }
        for (AtomBound bound : getSelectedBounds()){
            bound.updateLine(getShapeByID(bound.getFirst()),getShapeByID(bound.getSecond()));
        }
        for (ElectrodeShape electrode : getSelectedElectrodes()){
            double newX = 2 * selectionWidth - electrode.getRectangle().getX();
            newSelectionXMax = (newX > newSelectionXMax) ? newX : newSelectionXMax ;
            electrode.getRectangle().setFrame(newX , electrode.getRectangle().getY(), electrode.getRectangle().getWidth(), electrode.getRectangle().getHeight());
            electrode.updateLine();

        }
        setSelectionXMax(newSelectionXMax);
        getPaintSurface().repaint();
    }
    private void flipHorizontally() {
        double newSelectionYMax = getSelectionYMax();
        double selecionYcenter = (getSelectionYMax() + getSelectionYMin())/2;
        for (AtomShape atom : getSelectedAtoms()){
            double newY = 2 *selecionYcenter - atom.getShape().getY() ;
            newSelectionYMax = (newY > newSelectionYMax) ? newY : newSelectionYMax ;
            atom.getShape().setFrame(atom.getShape().getX(), newY, atom.getShape().getWidth(), atom.getShape().getHeight());
        }
        for (AtomBound bound : getSelectedBounds()){
            bound.updateLine(getShapeByID(bound.getFirst()),getShapeByID(bound.getSecond()));
        }
        for (ElectrodeShape electrode : getSelectedElectrodes()){
            double newY = 2 * selecionYcenter - electrode.getRectangle().getY();
            newSelectionYMax = (newY > newSelectionYMax) ? newY : newSelectionYMax ;
            electrode.getRectangle().setFrame(electrode.getRectangle().getX(), newY, electrode.getRectangle().getWidth(), electrode.getRectangle().getHeight());
            electrode.updateLine();
        }
        setSelectionYMax(newSelectionYMax);
        getPaintSurface().repaint();
    }
    private void paste() {
        int x0 = getX() -(int) getCopiedAtoms().get(0).getShape().getX();
        int y0 = getY() -(int) getCopiedAtoms().get(0).getShape().getY();
        ArrayList<AtomShape> newSelection = new ArrayList<AtomShape>();
        ArrayList<AtomBound> newBoundSelection = new ArrayList<AtomBound>();
        ArrayList<ElectrodeShape> newElectrodeSelection = new ArrayList<ElectrodeShape>();
        int translation = getShapes().size();
        int diameter = 2 * getGridSize();
        Hashtable<AtomShape, AtomShape> copy = new Hashtable<>();
        for (AtomShape s: getCopiedAtoms()){
            AtomShape as = new AtomShape(new Ellipse2D.Float((int)(s.getShape().getX() + x0),
                    (int)(s.getShape().getY() + y0), diameter, diameter), new Atom(s.getAtom(),atomIDSeq()));
            getShapes().add(as);
            copy.put(s, as);
            setSelectionXMax((as.getShape().getX() > getSelectionXMax()) ? as.getShape().getX() : getSelectionXMax());
            setSelectionXMin((as.getShape().getX() < getSelectionXMin() ||  getSelectionXMin() < 0) ? as.getShape().getX() : getSelectionXMin());
            setSelectionYMax((as.getShape().getY() > getSelectionYMax()) ? as.getShape().getY() : getSelectionYMax());
            setSelectionYMin((as.getShape().getY() < getSelectionYMin() || getSelectionYMin() < 0) ? as.getShape().getY() : getSelectionYMin());
            newSelection.add(as);
            if (!getMenu().model.contains(as.getID()))
                getMenu().model.addElement(as.getID());
        }
        for (AtomBound bound: getCopiedBounds()){
            Line2D newline = new Line2D.Float((int)(bound.getLine().getX1() + x0), (int)(bound.getLine().getY1() + y0), (int)(bound.getLine().getX2() + x0), (int)(bound.getLine().getY2() + y0));
            AtomBound newBound = new AtomBound( newline, new Bound(copy.get(getAtomByID(bound.getFirst())).getID(), copy.get(getAtomByID(bound.getSecond())).getID(), bound.getBound()));
            bounds.add(newBound);
            newBoundSelection.add(newBound);
        }
        for (ElectrodeShape electrode: getCopiedElectrodes()){
            Line2D newline = null;
            AtomShape as = null;
            if (electrode.getLine() != null) {
                newline = new Line2D.Float((int) (electrode.getLine().getX1() + x0), (int) (electrode.getLine().getY1() + y0), (int) (electrode.getLine().getX2() + x0), (int) (electrode.getLine().getY2() + y0));
            }
            if(electrode.getAtom() != null){
                as = copy.get(electrode.getAtom());
            }
            ElectrodeShape el = new ElectrodeShape(as, new Rectangle2D.Float((int)(electrode.getRectangle().getX() + x0), (int)(electrode.getRectangle().getY() + y0), diameter, diameter), newline, new Electrode(as.getID(), electrodeIDSeq(),electrode.getElectrode()));
            getElectrodes().add(el);
            setSelectionXMax((el.getRectangle().getX() > getSelectionXMax()) ? el.getRectangle().getX() : getSelectionXMax());
            setSelectionXMin((el.getRectangle().getX() < getSelectionXMin() ||  getSelectionXMin() < 0) ? el.getRectangle().getX() : getSelectionXMin());
            setSelectionYMax((el.getRectangle().getY() > getSelectionYMax()) ? el.getRectangle().getY() : getSelectionYMax());
            setSelectionYMin((el.getRectangle().getY() < getSelectionYMin() || getSelectionYMin() < 0) ? el.getRectangle().getY() : getSelectionYMin());
            newElectrodeSelection.add(el);
        }
        getSelectedAtoms().clear();
        getSelectedBounds().clear();
        getSelectedElectrodes().clear();
        setSelectedAtoms((ArrayList<AtomShape>)newSelection.clone());
        setSelectedBounds((ArrayList<AtomBound>)newBoundSelection.clone());
        setSelectedElectrodes((ArrayList<ElectrodeShape>)newElectrodeSelection.clone());
        getPaintSurface().repaint();
    }
    private void copy() {
        getCopiedAtoms().clear();
        setCopiedAtoms((ArrayList<AtomShape>) getSelectedAtoms().clone());
        setCopiedElectrodes((ArrayList<ElectrodeShape>) getSelectedElectrodes().clone());
        getCopiedBounds().clear();
        ArrayList <AtomBound> resultBounds = new ArrayList();
        ArrayList <AtomBound> boundsToCopy = (ArrayList<AtomBound>) getSelectedBounds().clone();
        for (AtomBound bound : boundsToCopy){
            if (getCopiedAtoms().contains(getAtomByID(bound.getFirst())) && getCopiedAtoms().contains(getAtomByID(bound.getSecond()))){
                resultBounds.add(bound);
            }
        }
        setCopiedBounds(resultBounds);
    }

    @Override
    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    @Override
    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public double getSelectionXMax() {
        return selectionXMax;
    }

    public void setSelectionXMax(double selectionXMax) {
        this.selectionXMax = selectionXMax;
    }

    public double getSelectionXMin() {
        return selectionXMin;
    }

    public void setSelectionXMin(double selectionXMin) {
        this.selectionXMin = selectionXMin;
    }

    public double getSelectionYMax() {
        return selectionYMax;
    }

    public void setSelectionYMax(double selectionYMax) {
        this.selectionYMax = selectionYMax;
    }

    public double getSelectionYMin() {
        return selectionYMin;
    }

    public void setSelectionYMin(double selectionYMin) {
        this.selectionYMin = selectionYMin;
    }

    public Thread getDynamicCalculationsThread() {
        return dynamicCalculationsThread;
    }

    public void setDynamicCalculationsThread(Thread dynamicCalculationsThread) {
        this.dynamicCalculationsThread = dynamicCalculationsThread;
    }

    public String getSurfaceCoupling() {
        return surfaceCoupling;
    }

    public void setSurfaceCoupling(String surfaceCoupling) {
        this.surfaceCoupling = surfaceCoupling;
    }

    public String getkFa() {
        return kFa;
    }

    public void setkFa(String kFa) {
        this.kFa = kFa;
    }

    public String getdE() {
        return dE;
    }

    public void setdE(String dE) {
        this.dE = dE;
    }

    public String getbColor() {
        return bColor;
    }

    public void setbColor(String bColor) {
        this.bColor = bColor;
    }

    public String getDt() {
        return dt;
    }

    public void setDt(String dt) {
        this.dt = dt;
    }

    public String getEnergyRange() {
        return energyRange;
    }

    public void setEnergyRange(String energyRange) {
        this.energyRange = energyRange;
    }

    public double getScreenWidth() {
        return screenWidth;
    }

    public void setScreenWidth(double screenWidth) {
        this.screenWidth = screenWidth;
    }

    public double getScreenHeight() {
        return screenHeight;
    }

    public void setScreenHeight(double screenHeight) {
        this.screenHeight = screenHeight;
    }

    public PaintSurface getPaintSurface() {
        return paintSurface;
    }

    public void setPaintSurface(PaintSurface paintSurface) {
        this.paintSurface = paintSurface;
    }

    public JScrollPane getScrollPane() {
        return scrollPane;
    }

    public void setScrollPane(JScrollPane scrollPane) {
        this.scrollPane = scrollPane;
    }

    private AtomShape getShapeByID(int id){
        for(AtomShape shape :shapes){
            if (shape.getID() == id){
                return  shape;
            }
        }
        return null;
    }
    public ArrayList<AtomShape> getShapes() {
        return shapes;
    }

    public void setShapes(ArrayList<AtomShape> shapes) {
        this.shapes = shapes;
    }

    public ArrayList<ElectrodeShape> getElectrodes() {
        return electrodes;
    }

    public void setElectrodes(ArrayList<ElectrodeShape> electrodes) {
        this.electrodes = electrodes;
    }

    public ArrayList<AtomShape> getSelectedAtoms() {
        return selectedAtoms;
    }

    public void setSelectedAtoms(ArrayList<AtomShape> selectedAtoms) {
        this.selectedAtoms = selectedAtoms;
    }

    public ArrayList<ElectrodeShape> getSelectedElectrodes() {
        return selectedElectrodes;
    }

    public void setSelectedElectrodes(ArrayList<ElectrodeShape> selectedElectrodes) {
        this.selectedElectrodes = selectedElectrodes;
    }

    public ArrayList<ElectrodeShape> getCopiedElectrodes() {
        return copiedElectrodes;
    }

    public void setCopiedElectrodes(ArrayList<ElectrodeShape> copiedElectrodes) {
        this.copiedElectrodes = copiedElectrodes;
    }

    public ArrayList<AtomBound> getSelectedBounds() {
        return selectedBounds;
    }

    public void setSelectedBounds(ArrayList<AtomBound> selectedBounds) {
        this.selectedBounds = selectedBounds;
    }

    public ArrayList<AtomShape> getCopiedAtoms() {
        return copiedAtoms;
    }

    public void setCopiedAtoms(ArrayList<AtomShape> copiedAtoms) {
        this.copiedAtoms = copiedAtoms;
    }

    public ArrayList<AtomBound> getCopiedBounds() {
        return copiedBounds;
    }

    public void setCopiedBounds(ArrayList<AtomBound> copiedBounds) {
        this.copiedBounds = copiedBounds;
    }

    public int getGridSize() {
        return gridSize;
    }

    public void setGridSize(int gridSize) {
        this.gridSize = gridSize;
    }

    public boolean isSelectionFlag() {
        return selectionFlag;
    }

    public void setSelectionFlag(boolean selectionFlag) {
        this.selectionFlag = selectionFlag;
    }

    public boolean isShowGrid() {
        return showGrid;
    }

    public void setShowGrid(boolean showGrid) {
        this.showGrid = showGrid;
    }

    public Flag getIsInterupted() {
        return isInterupted;
    }

    public void setIsInterupted(Flag isInterupted) {
        this.isInterupted = isInterupted;
    }

    public Flag getIsCanceled() {
        return isCanceled;
    }

    public void setIsCanceled(Flag isCanceled) {
        this.isCanceled = isCanceled;
    }

    public AtomShape getHighlightedShape() {
        return highlightedShape;
    }

    public void setHighlightedShape(AtomShape highlightedShape) {
        this.highlightedShape = highlightedShape;
//        if (highlightedShape != null){
//            getSelectedAtoms().clear();
//            getSelectedAtoms().add(highlightedShape);
//        }
    }

    public AtomBound getHighlightedBound() {
        return highlightedBound;
    }

    public void setHighlightedBound(AtomBound highlightedBound) {
        this.highlightedBound = highlightedBound;
    }

    public ElectrodeShape getHighlightedElectrode() {
        return highlightedElectrode;
    }

    public void setHighlightedElectrode(ElectrodeShape highlightedElectrode) {
        this.highlightedElectrode = highlightedElectrode;
    }

    public Rectangle getSelection() {
        return selection;
    }

    public void setSelection(Rectangle selection) {
        this.selection = selection;
    }

    public Point getAnchor() {
        return anchor;
    }

    public void setAnchor(Point anchor) {
        this.anchor = anchor;
    }

    public AtomShape getCurrentAtom() {
        return currentAtom;
    }

    public void setCurrentAtom(AtomShape currentAtom) {
        this.currentAtom = currentAtom;
    }

    public Menu getMenu() {
        return menu;
    }

    public void setMenu(Menu menu) {
        this.menu = menu;
    }

    public StepRecorder getStepRecorder() {
        return stepRecorder;
    }

    public void setStepRecorder(StepRecorder stepRecorder) {
        this.stepRecorder = stepRecorder;
    }

    public int getMaxFileNum() {
        return maxFileNum;
    }

    public void setMaxFileNum(int maxFileNum) {
        this.maxFileNum = maxFileNum;
    }

    public boolean isCtrlPressed() {
        return ctrlPressed;
    }

    public void setCtrlPressed(boolean ctrlPressed) {
        this.ctrlPressed = ctrlPressed;
    }

    public String getCurrentDataPath() {
        return currentDataPath;
    }

    public void setCurrentDataPath(String currentDataPath) {
        this.currentDataPath = currentDataPath;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    public DefaultListModel getListModel() {
        return listModel;
    }

    public void setListModel(DefaultListModel listModel) {
        this.listModel = listModel;
    }

    public JList getList() {
        return list;
    }

    public void setList(JList list) {
        this.list = list;
        list.setCellRenderer(new DefaultListCellRenderer() {

            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof StepRecorder.ActiveString) {
                    StepRecorder.ActiveString element = (StepRecorder.ActiveString)value;
                    if (element.isActive) {
                        setForeground(Color.BLACK);
                    }else {
                        setForeground(Color.GRAY);
                    }
                    if (isSelected) {
                        setBackground(Color.GREEN);
                    }
                }
                setBorder(BorderFactory.createEtchedBorder());
                return component;
            }

        });
    }

    public String getStepCount() {
        return stepCount;
    }

    public void setStepCount(String stepCount) {
        this.stepCount = stepCount;
    }

    private class PopUpMenu extends JPopupMenu {

        private static final long serialVersionUID = 1L;
        JMenuItem copyItem;
        JMenuItem pasteItem;
        JMenuItem flipVerticalItem;
        JMenuItem flipHorizontalItem;
        public PopUpMenu(){
            copyItem = new MyMenuItem("Copy", new ImageIcon("img/copyIcon.png"));
            pasteItem = new MyMenuItem("Paste", new ImageIcon("img/pasteIcon.png"));
            flipVerticalItem = new MyMenuItem("Flip vertically", new ImageIcon("img/flipRight.png"));
            flipHorizontalItem = new MyMenuItem("Flip horizontally", new ImageIcon("img/flipUp.png"));
            add(copyItem);
            add(pasteItem);
            add(flipVerticalItem);
            add(flipHorizontalItem);
            copyItem.addActionListener(evt -> copy());
            flipHorizontalItem.addActionListener(evt -> flipHorizontally());
            flipVerticalItem.addActionListener(evt -> flipVertically());
            pasteItem.addActionListener(evt -> paste());
        }

    }
    class MyKeyAdapter implements KeyListener{

        @Override
        public void keyTyped(KeyEvent e) {}

        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_CONTROL){
                setCtrlPressed(true);
            }

        }

        @Override
        public void keyReleased(KeyEvent e) {
            if (isCtrlPressed()){
                if (e.getKeyCode() == KeyEvent.VK_DELETE){
                    clearAll();
                    setCtrlPressed(false);
                }
                if (e.getKeyCode() == KeyEvent.VK_Z){
                    undo();
                }
                else if (e.getKeyCode() == KeyEvent.VK_Y){
                    redo();
                }
                else if (e.getKeyCode() == KeyEvent.VK_C){
                    copy();
                }
                else if (e.getKeyCode() == KeyEvent.VK_V){
                    paste();
                }
                else if (e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_LEFT){
                    flipVertically();
                }
                else if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN){
                    flipHorizontally();
                }
                else if (e.getKeyCode() == KeyEvent.VK_MINUS){
                    zoom(-2);
                }
                else if (e.getKeyCode() == KeyEvent.VK_EQUALS){
                    zoom(2);
                }
                if (e.getKeyCode() == KeyEvent.VK_L){
                    countStaticProperties();
                    setCtrlPressed(false);
                }
                if (e.getKeyCode() == KeyEvent.VK_R){
                    refresh(null);
                    setCtrlPressed(false);
                }
                if (e.getKeyCode() == KeyEvent.VK_S){
                    GlobalChainProperties gp = mapGlobalPropertiesObject(getTime(), getCurrentDataPath(), getIsActive());
                    saveData(gp);
                    setCtrlPressed(false);
                }
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    if(highlightedShape != null){
                        AtomShape nextShape = getAtomByID(highlightedShape.getID()-1);
                        if(nextShape == null){
                            nextShape = getAtomByID(getShapes().size()-1);
                        }
                        if (nextShape != null){
                            highlightedShape = nextShape;
                            currentAtom = highlightedShape;
                            reloadAtomFields();
                            repaint();
                        }
                    }
                }
            }
            else {
                if (e.getKeyCode() == KeyEvent.VK_DELETE){
                    delete();
                }
                if (e.getKeyCode() == KeyEvent.VK_L){
                    showLDOS();
                }

                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    if(highlightedShape != null){
                        AtomShape nextShape = getAtomByID(highlightedShape.getID()+1);
                        if(nextShape == null){
                            nextShape = getAtomByID(0);
                        }
                        if (nextShape != null){
                            highlightedShape = nextShape;
                            currentAtom = highlightedShape;
                            reloadAtomFields();
                            repaint();
                        }
                    }
                }
            }
            if(e.getKeyCode() == KeyEvent.VK_CONTROL){
                setCtrlPressed(false);
            }
        }
    }
    class MovingAdapter extends MouseAdapter {

        @Override
        public void mouseClicked(MouseEvent e) {

            getMenu().colorBox.setEnabled(true);
            getMenu().atomIDComboBox.setVisible(false);
            getMenu().perturbationLabel.setVisible(false);
            getMenu().perturbationTextField.setVisible(false);
            getMenu().atomSavingComboBox.setVisible(false);
            getMenu().atomIDLabel.setVisible(false);
            getMenu().atomDataSavingLabel.setVisible(false);
            getPaintSurface().requestFocus();
            getSelectedBounds().clear();
            getSelectedAtoms().clear();
            getSelectedElectrodes().clear();
            setSelectionFlag(false);
            getPaintSurface().repaint();
            boolean checkBounds = true;
            boolean isRightMouseButton = SwingUtilities.isRightMouseButton(e);
            if (!isRightMouseButton && e.getClickCount() == 2 && !e.isConsumed()) {
                e.consume();
                boolean isPlaceOcupied = false;
                for (AtomShape s : getShapes()){
                    if (s.getShape().getBounds2D().contains(e.getX(),e.getY())){
                        isPlaceOcupied = true;
                        break;
                    }
                }
                int diameter = 2* getGridSize();
                if (!isPlaceOcupied && isCtrlPressed()){
                    Rectangle2D newElectrode = new Rectangle2D.Float(e.getX() - getGridSize(), e.getY() - getGridSize(), diameter, diameter);
                    getElectrodes().add(new ElectrodeShape(newElectrode, electrodeIDSeq()));
                }
                else if(!isPlaceOcupied){
                    Ellipse2D s = new Ellipse2D.Float(e.getX() - getGridSize(), e.getY() - getGridSize(), diameter, diameter);
                    AtomShape shap = new AtomShape(s, atomIDSeq());
                    getShapes().add(shap);
                    if (!getMenu().model.contains(shap.getID()))
                        getMenu().model.addElement(shap.getID());
                }
                getPaintSurface().repaint();
                return;
            }
            setHighlightedBound(null);
            setSelectionFlag(false);
            setX(e.getX());
            setY(e.getY());

            boolean isOutside = true;

            for (ElectrodeShape electrode : getElectrodes()) {
                if (electrode.getRectangle().intersects(getX(), getY(), 20, 20)) {
                    setHighlightedElectrode(electrode);
                    setHighlightedShape(null);
                    setHighlightedBound(null);
                    getMenu().electrodeCouplingTextField.setText(electrode.getCoupling());
                    getMenu().additionalParamsLabel.setText("Electrode Coupling");
                    getMenu().secondAdditionalParamsLabel.setText("Not implemented");
                    getMenu().setTextFieldsVisibility(TextFieldType.ELECTRODE);
                    getMenu().parameterLabel.setText("Energy Step");
                    getMenu().perturbationTextField.setVisible(true);
                    getMenu().perturbationTextField.setText(electrode.getPerturbation());
                    getMenu().perturbationLabel.setVisible(true);
                    getMenu().applyToAllButton.setText("Apply to all electrodes");
                    getMenu().applyToAllButton.setEnabled(true);
                    getMenu().colorBox.setSelectedItem(getHighlightedElectrode().getColor());
                    getMenu().electrodeTypeTextField.setText(getHighlightedElectrode().getType());
                    getMenu().thirdAdditionalParamsLabel.setText("Elec. Type");
                    getMenu().applyToAllButton.setImageIcon(new ImageIcon(APPLY_E_ALL_ELECTRODES_BUTTON_IMAGE_PATH));
                    getMenu().dETextField.setText(electrode.getdE());
                    checkBounds = false;
                    isOutside = false;
                }
            }
            if (isOutside){
                //UndoRedoQueue.getInstance().push(mapGlobalPropertiesObject(getTime(), getCurrentDataPath(), getIsActive()));
                setHighlightedBound(null);
                setHighlightedElectrode(null);
                getMenu().surfaceCouplingTextField.setText(getSurfaceCoupling());
                getMenu().additionalParamsLabel.setText("Surface Coupling");
                getMenu().setTextFieldsVisibility(TextFieldType.SURFACE);
                getMenu().secondAdditionalParamsLabel.setText("kFa");
                getMenu().parameterLabel.setText("Time Step");
                if (getDynamicCalculationsThread() != null){
                    getMenu().applyToAllButton.setText("Next step (" + getStepCount() +")");
                    NanoModeller.this.repaint();
                    getMenu().applyToAllButton.setEnabled(true);
                    getMenu().applyToAllButton.setImageIcon(new ImageIcon(NEXT_STEP_IMAGE_PATH));
                }else{
                    getMenu().applyToAllButton.setText("");
                    getMenu().applyToAllButton.setIcon(null);
                    getMenu().applyToAllButton.setEnabled(false);
                }
                getMenu().energyRangeTextField.setText(getEnergyRange());
                getMenu().thirdAdditionalParamsLabel.setText("Energy range");
                getMenu().kfaTextField.setText(getkFa());
                getMenu().dTTextField.setText(getDt());
            }

            ElectrodeShape currentElectrode = null;
            if (!isRightMouseButton) {
                for (AtomShape s : getShapes()) {
                    if (s.getShape().getBounds2D().intersects(getX(), getY(), 20, 20)) {
                        setCurrentAtom(s);
                        checkBounds = false;
                        isOutside = false;
                    }
                }
                if(getHighlightedShape() != null){
                    for (ElectrodeShape electrode : getElectrodes()) {
                        if (electrode.getRectangle().getBounds2D().intersects(getX(), getY(), 20, 20)) {
                            currentElectrode = electrode;
                            checkBounds = false;
                            isOutside = false;
                        }
                    }
                }
            }
            if (checkBounds){
                for (AtomBound bound : bounds) {
                    if (bound.getLine().intersects(getX(), getY(), 50, 50)) {
                        setHighlightedShape(null);
                        setHighlightedElectrode(null);
                        setHighlightedBound(bound);
                        AtomBound highlightedBound = getHighlightedBound();
                        getMenu().colorBox.setSelectedItem(highlightedBound.getColor());
                        getMenu().boundVTextField.setText(highlightedBound.getValue());
                        getMenu().correlationTextField.setText(highlightedBound.getCorrelationCoupling());
                        getMenu().parameterLabel.setText("Coupling");
                        getMenu().perturbationTextField.setVisible(true);
                        getMenu().perturbationTextField.setText(highlightedBound.getPerturbation());
                        getMenu().perturbationLabel.setVisible(true);
                        getMenu().applyToAllButton.setText("Apply to all bounds");
                        getMenu().secondAdditionalParamsLabel.setText("Correlation U");
                        getMenu().applyToAllButton.setEnabled(true);
                        getMenu().boundTypeTextField.setText(highlightedBound.getType());
                        getMenu().thirdAdditionalParamsLabel.setText("Coupling Type");
                        getMenu().applyToAllButton.setImageIcon(new ImageIcon(APPLY_V_ALL_BUTTON_IMAGE_PATH));
                        getMenu().additionalParamsLabel.setText("Spin-orbit coupling");
                        getMenu().setTextFieldsVisibility(TextFieldType.BOUND);
                        getMenu().spinOrbitTextField.setText(bound.getSpinOrbit());

                        getPaintSurface().repaint();
                        return;
                    }
                }
            }

            if (isOutside){
                setHighlightedShape(null);
                setHighlightedBound(null);
                setHighlightedElectrode(null);
                getPaintSurface().repaint();
                getMenu().setTextFieldsVisibility(TextFieldType.SURFACE);
                getMenu().colorBox.setSelectedItem(getbColor());
                getMenu().secondAdditionalParamsLabel.setText("kFa");
                getMenu().atomIDComboBox.setVisible(false);
                getMenu().atomIDLabel.setVisible(false);
                getMenu().perturbationTextField.setVisible(false);
                getMenu().perturbationLabel.setVisible(false);
                getMenu().parameterLabel.setText("Time Step");
                if (getDynamicCalculationsThread() != null){
                    getMenu().applyToAllButton.setText("Next step (" + getStepCount() +")");
                    NanoModeller.this.repaint();
                    getMenu().applyToAllButton.setEnabled(true);
                }else{
                    getMenu().applyToAllButton.setText("");
                    getMenu().applyToAllButton.setIcon(null);
                    getMenu().applyToAllButton.setEnabled(false);
                }
                getMenu().energyRangeTextField.setText(getEnergyRange());
                getMenu().thirdAdditionalParamsLabel.setText("Energy range");
                getMenu().dTTextField.setText(getDt());
                getMenu().surfaceCouplingTextField.setText(getSurfaceCoupling());
                getMenu().kfaTextField.setText(getkFa());
                return;
            }
            if (isCtrlPressed() && getCurrentAtom() != null && getHighlightedShape() != null && getCurrentAtom() != getHighlightedShape()) {

                Line2D line = new Line2D.Float((int) getCurrentAtom().getShape().getX() + (int) getCurrentAtom().getShape().getWidth() / 2, (int) getCurrentAtom().getShape().getY() + (int) getCurrentAtom().getShape().getHeight() / 2,
                        (int) getHighlightedShape().getShape().getX() + (int) getHighlightedShape().getShape().getWidth() / 2, (int) getHighlightedShape().getShape().getY() + (int) getHighlightedShape().getShape().getHeight() / 2);
                bounds.add(new AtomBound(getCurrentAtom().getID(), getHighlightedShape().getID(), line));
                setHighlightedShape(null);
                setCurrentAtom(null);
                getMenu().boundVTextField.setText("");
                getMenu().spinOrbitTextField.setText("");

            }
            else if (isCtrlPressed() && (currentElectrode != null && getHighlightedShape() != null )){

                Line2D line = new Line2D.Float((int) currentElectrode.getRectangle().getX() + (int) currentElectrode.getRectangle().getWidth() / 2, (int) currentElectrode.getRectangle().getY() + (int) currentElectrode.getRectangle().getHeight() / 2,
                        (int) getHighlightedShape().getShape().getX() + (int) getHighlightedShape().getShape().getWidth() / 2, (int) getHighlightedShape().getShape().getY() + (int) getHighlightedShape().getShape().getHeight() / 2);
                currentElectrode.setLine(line);
                currentElectrode.setAtom(getHighlightedShape());

                setHighlightedShape(null);
                setHighlightedElectrode(null);
            }
            else if (isCtrlPressed() && (getHighlightedElectrode() != null && getCurrentAtom() != null )){

                Line2D line = new Line2D.Float((int) getHighlightedElectrode().getRectangle().getX() + (int) getHighlightedElectrode().getRectangle().getWidth() / 2, (int) getHighlightedElectrode().getRectangle().getY() + (int) getHighlightedElectrode().getRectangle().getHeight() / 2,
                        (int) getCurrentAtom().getShape().getX() + (int) getCurrentAtom().getShape().getWidth() / 2, (int) getCurrentAtom().getShape().getY() + (int) getCurrentAtom().getShape().getHeight() / 2);
                getHighlightedElectrode().setLine(line);
                getHighlightedElectrode().setAtom(getCurrentAtom());
                setHighlightedElectrode(null);
            }
            else if (getCurrentAtom() != null && getHighlightedElectrode() == null) {
                reloadAtomFields();
            }
            getPaintSurface().repaint();
        }

        public void mousePressed(MouseEvent e) {
            if (e.isPopupTrigger())
                doPop(e);
            setX(e.getX());
            setY(e.getY());
            setAnchor(e.getPoint());
            setSelection(new Rectangle(getAnchor()));
        }

        private void doPop(MouseEvent e){
            PopUpMenu menu = new PopUpMenu();
            menu.show(e.getComponent(), e.getX(), e.getY());
        }

        public void mouseReleased(MouseEvent e) {
            if (e.isPopupTrigger() && getAnchor() != null && getAnchor().distance(e.getPoint()) == 0) {
                for (AtomShape s : getSelectedAtoms()) {
                    if (s.getShape().contains(getAnchor())) {
                        doPop(e);
                        return;
                    }
                }
                for (ElectrodeShape s : getSelectedElectrodes()) {
                    if (s.getRectangle().contains(getAnchor())) {
                        doPop(e);
                        return;
                    }
                }
                if (getCopiedAtoms().size() > 0) {
                    doPop(e);
                    return;
                }
            }
            getSelectedAtoms().clear();
            getSelectedBounds().clear();
            getSelectedElectrodes().clear();
            if(getSelection() != null){
                for (AtomShape ashape : getShapes()) {
                    if (getSelection().intersects(ashape.getShape().getBounds2D())) {
                        getSelectedAtoms().add(ashape);
                        double x = ashape.getShape().getX();
                        double y = ashape.getShape().getY();
                        setSelectionXMax((x > getSelectionXMax()) ? x : getSelectionXMax());
                        setSelectionXMin((x < getSelectionXMin() || getSelectionXMin() < 0) ? x : getSelectionXMin());
                        setSelectionYMax((y > getSelectionYMax()) ? y : getSelectionYMax());
                        setSelectionYMin((y < getSelectionYMin() || getSelectionYMin() < 0) ? y : getSelectionYMin());
                    }
                }
                for (ElectrodeShape electrode : getElectrodes()) {
                    if (getSelection().intersects(electrode.getRectangle())) {
                        getSelectedElectrodes().add(electrode);
                        double x = electrode.getRectangle().getX();
                        double y = electrode.getRectangle().getY();
                        setSelectionXMax((x > getSelectionXMax()) ? x : getSelectionXMax());
                        setSelectionXMin((x < getSelectionXMin() || getSelectionXMin() < 0) ? x : getSelectionXMin());
                        setSelectionYMax((y > getSelectionYMax()) ? y : getSelectionYMax());
                        setSelectionYMin((y < getSelectionYMin() || getSelectionYMin() < 0) ? y : getSelectionYMin());
                    }
                }
                for (AtomBound bound : bounds) {
                    if (bound.getLine().intersects(getSelection())) {
//                    if (getSelectedAtoms().contains(bound.getFirst()) || getSelectedAtoms().contains(bound.getSecond())){
                        getSelectedBounds().add(bound);
                    }
                }
            }
            setSelection(null);
            if (isSelectionFlag()){
                getSelectedAtoms().clear();
                getSelectedBounds().clear();
                getSelectedElectrodes().clear();
                setSelectionXMax(-1);
                setSelectionXMin(-1);
                setSelectionYMax(-1);
                setSelectionYMin(-1);
            }
            setAnchor(null);
            repaint();
        }

        public void mouseWheelMoved(MouseWheelEvent e) {
            int notches = e.getWheelRotation();
            zoom(notches);
        }

        public void mouseDragged(MouseEvent e) {

            boolean isRightMouseButton = SwingUtilities.isRightMouseButton(e);
            int dx = e.getX() - getX();
            int dy = e.getY() - getY();
            if (isRightMouseButton){
                getSelection().setBounds( (int)Math.min(getAnchor().x,e.getX()), (int)Math.min(getAnchor().y,e.getY()),
                        (int)Math.abs(e.getX()- getAnchor().x), (int)Math.abs(e.getY()- getAnchor().y));
                getPaintSurface().repaint();
                return;
            }
            else if (getHighlightedShape() == null && getHighlightedBound() == null
                    && getSelectedAtoms().size() == 0 && getSelectedBounds().size() == 0
                    && getHighlightedElectrode() == null && getSelectedElectrodes().size() == 0){
                getScrollPane().getVerticalScrollBar().setValue(getScrollPane().getVerticalScrollBar().getValue() - dy/7);
                getScrollPane().getHorizontalScrollBar().setValue(getScrollPane().getHorizontalScrollBar().getValue() - dx/7);
                return;
            }
            if (getSelectedElectrodes().size() > 0 || getSelectedAtoms().size() > 0){
                setSelectionXMax(getSelectionXMax() + dx);
                setSelectionXMin(getSelectionXMin() + dx);
                setSelectionYMax(getSelectionYMax() + dy);
                setSelectionYMin(getSelectionYMin() + dy);
            }
            if (getSelectedAtoms().size() > 0){
                for (AtomShape atom : getSelectedAtoms()){
                    atom.getShape().setFrame(atom.getShape().getX() + dx, atom.getShape().getY() + dy, atom.getShape().getWidth(), atom.getShape().getHeight());
                }
                for (AtomBound bound : getSelectedBounds()){
                    bound.updateLine(getShapeByID(bound.getFirst()),getShapeByID(bound.getSecond()));
                }
                for (ElectrodeShape electrode : getElectrodes()){
                    if (getSelectedElectrodes().contains(electrode)) {
                        electrode.getRectangle().setFrame(electrode.getRectangle().getX() + dx, electrode.getRectangle().getY() + dy, electrode.getRectangle().getWidth(), electrode.getRectangle().getHeight());
                    }
                    electrode.updateLine();
                }
                getPaintSurface().repaint();
                setX(getX() + dx);
                setY(getY() + dy);
                return;
            }

            AtomShape shape = getHighlightedShape();
            AtomBound line = null;
            for (AtomBound s : bounds) {
                if (shape != null && shape.getShape().contains(e.getPoint())) {
                    if (s.getFirst() == shape.getID()) {
                        line = s;
                        line.getLine().setLine(line.getLine().getX1() + dx, line.getLine().getY1() + dy, line.getLine().getX2(), line.getLine().getY2());
                    }
                    if (s.getSecond() == shape.getID()) {
                        line = s;
                        line.getLine().setLine(line.getLine().getX1(), line.getLine().getY1(), line.getLine().getX2() + dx, line.getLine().getY2() + dy);
                    }
                }
            }
            for (ElectrodeShape electrode : getElectrodes()){
                if (electrode.getAtom() == shape && electrode.getLine() != null){
                    electrode.updateLine();
                }
            }
            if (shape != null && shape.getShape().contains(e.getPoint())) {
                shape.getShape().setFrame(shape.getShape().getX() + dx, shape.getShape().getY() + dy, shape.getShape().getWidth(), shape.getShape().getHeight());
            }
            if (getHighlightedElectrode() != null && getHighlightedElectrode().getRectangle().contains(e.getPoint())){
                getHighlightedElectrode().getRectangle().setFrame(getHighlightedElectrode().getRectangle().getX() + dx, getHighlightedElectrode().getRectangle().getY() + dy, getHighlightedElectrode().getRectangle().getWidth(), getHighlightedElectrode().getRectangle().getHeight());
                getHighlightedElectrode().updateLine();
            }
            getPaintSurface().repaint();
            setX(getX() + dx);
            setY(getY() + dy);
        }
    }

    public void reloadAtomFields() {
        setHighlightedShape(getCurrentAtom());
        getMenu().atomEnergyTextField.setText(getHighlightedShape().getEnergy());
        getMenu().spinFlipTextField.setText(getHighlightedShape().getSpinFlip());
        getMenu().nZeroTextField.setText(getHighlightedShape().getnZero());
        getMenu().atomSavingComboBox.setLDOS(getHighlightedShape().isSaveLDOS());
        getMenu().atomSavingComboBox.setNormalisation(getHighlightedShape().isSaveNormalisation());
        getMenu().atomTypeTextField.setText(getHighlightedShape().getType());
        getMenu().thirdAdditionalParamsLabel.setText("Atom Type");
        getMenu().atomIDComboBox.setSelectedItem(getHighlightedShape().getID());
//        getMenu().perturbationLabel.setText("Perturbation");
//        getMenu().perturbationTextField.setText(getHighlightedShape().getPerturbation());
        getMenu().atomIDLabel.setText("Atom ID");
        getMenu().additionalParamsLabel.setText("Spin-flip");
        getMenu().setTextFieldsVisibility(TextFieldType.ATOM);
        getMenu().secondAdditionalParamsLabel.setText("Init charge");
        getMenu().atomIDComboBox.setVisible(true);
        getMenu().perturbationLabel.setText("Perturbation");
        getMenu().perturbationTextField.setVisible(false);
        getMenu().perturbationLabel.setVisible(false);
        getMenu().atomSavingComboBox.setVisible(true);
        getMenu().atomIDLabel.setVisible(true);
        getMenu().atomDataSavingLabel.setVisible(true);

        getMenu().colorBox.setSelectedItem(getHighlightedShape().getColor());
        getMenu().parameterLabel.setText("Energy");
        getMenu().applyToAllButton.setText("Apply to all atoms");
        getMenu().applyToAllButton.setEnabled(true);
        getMenu().applyToAllButton.setImageIcon(new ImageIcon(APPLY_E_ALL_BUTTON_IMAGE_PATH));
    }

    private class PaintSurface extends JComponent {

        private static final long serialVersionUID = 1L;

        public void paint(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            int screenHeight = PaintSurface.this.getSize().height;
            int screenWidth = PaintSurface.this.getSize().width;
            g2.setColor(getMenu().colorBox.colors.get(getbColor()));
            g2.fillRect(0, 0, screenWidth, screenHeight);
            Stroke thindashed = new BasicStroke(4.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
                    1.0f,new float[] { 8.0f, 3.0f, 2.0f, 3.0f },0.0f);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (Globals.DARK_GRAY.equals(getbColor())){
                g2.setPaint(Color.LIGHT_GRAY);
            }
            else {
                g2.setPaint(Color.DARK_GRAY);
            }

            if (isShowGrid()) {
                for (int i = 0; i < screenWidth; i += getGridSize())
                    g2.draw(new Line2D.Float(i, 0, i, screenHeight));
                for (int i = 0; i < screenHeight; i += getGridSize())
                    g2.draw(new Line2D.Float(0, i, screenWidth, i));
            }
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(4));
            for (AtomShape s : getShapes()){
                String color = s.getColor();
                Color innerColor = Color.BLACK;
                Color selectedColor = Color.orange;
                if (Globals.BLACK.equals(color) || Globals.DARK_GRAY.equals(color)){
                    innerColor = Color.WHITE;
                }
                else if (Globals.YELLOW.equals(color) || Globals.DARK_YELLOW.equals(color) || Globals.ORANGE.equals(color)){
                    selectedColor = Color.RED;
                }
                double centerX = s.getShape().getCenterX();
                double centerY = s.getShape().getCenterY();
                double outerRadius = s.getShape().getWidth() / 2.5;
                double innerRadius = 0.55 * s.getShape().getWidth();
                g2.setPaint(new RadialGradientPaint(
                        (float)centerX, (float)centerY, (float)outerRadius, new float[] { 0, 1 },
                        new Color[] { Color.darkGray, getMenu().colorBox.colors.get(s.getColor()) }));

                g2.fill(createStar(centerX, centerY,
                        innerRadius,
                        outerRadius, 13, 2));
                if (s != getHighlightedShape() && !getSelectedAtoms().contains(s)) {
                    g2.setPaint(new RadialGradientPaint(
                            (float) centerX, (float) centerY, (float) outerRadius, new float[]{0, 1},
                            new Color[]{innerColor, getMenu().colorBox.colors.get(s.getColor())}));

                }
                else{
                    g2.setPaint(new RadialGradientPaint(
                            (float)centerX, (float)centerY, (float)outerRadius, new float[] { 0, 1 },
                            new Color[] { selectedColor, getMenu().colorBox.colors.get(s.getColor()) }));
                }

                g2.fill(createStar(centerX, centerY,
                        innerRadius/2,
                        outerRadius/2, 13, 2));
            }
            for (AtomBound bound : bounds) {
                g2.setColor(Color.BLACK);
                if (bound != getHighlightedBound()){
                    g2.setColor(getMenu().colorBox.colors.get(bound.getColor()));
                    g2.draw(bound.getLine());
                }
            }
            g2.setColor(Color.BLACK);
            for (ElectrodeShape e : getElectrodes()) {

                String color = e.getColor();
                Color innerColor = Color.BLACK;
                Color selectedColor = Color.GREEN;
                if ("BLACK".equals(color) || "DARK GRAY".equals(color)){
                    innerColor = Color.WHITE;
                }
                else if ("GREEN".equals(color) || "DARK GREEN".equals(color)){
                    selectedColor = Color.RED;
                }
                int outerRadius = (int)(e.getRectangle().getWidth() / 1.76);
                Rectangle2D rect = e.getRectangle();
                g2.setColor(getMenu().colorBox.colors.get(e.getColor()));
                Hexagon hexagon = new Hexagon(new Point((int)rect.getX() + (int)rect.getWidth()/2, (int)rect.getY() + (int)rect.getWidth()/2), outerRadius);
                Hexagon innerHexagon = new Hexagon(new Point((int)rect.getX() + (int)rect.getWidth()/2, (int)rect.getY() + (int)rect.getWidth()/2), outerRadius/2);
                g2.setPaint(new RadialGradientPaint(
                        new Point((int)rect.getX() + (int)rect.getWidth()/2, (int)rect.getY() + (int)rect.getWidth()/2), (float)outerRadius, new float[] { 0, 1 },
                        new Color[] { Color.darkGray, getMenu().colorBox.colors.get(e.getColor()) }));
                g2.drawPolygon(hexagon.getHexagon());
                g2.fillPolygon(hexagon.getHexagon());
                if (e != getHighlightedElectrode() && !getSelectedElectrodes().contains(e)) {
                    g2.setPaint(new RadialGradientPaint(
                            new Point((int)rect.getX() + (int)rect.getWidth()/2, (int)rect.getY() + (int)rect.getWidth()/2), (float)outerRadius, new float[] { 0, 1 },
                            new Color[] { innerColor, getMenu().colorBox.colors.get(e.getColor()) }));
                }else{
                    g2.setPaint(new RadialGradientPaint(
                            new Point((int)rect.getX() + (int)rect.getWidth()/2, (int)rect.getY() + (int)rect.getWidth()/2), (float)outerRadius, new float[] { 0, 1 },
                            new Color[] { selectedColor, getMenu().colorBox.colors.get(e.getColor()) }));
                }
                g2.drawPolygon(innerHexagon.getHexagon());
                g2.fillPolygon(innerHexagon.getHexagon());

                if(e.getLine() != null){
                    if (Globals.BLACK.equals(getbColor())) {
                        g2.setColor(Color.WHITE);
                    }
                    else {
                        g2.setColor(Color.BLACK);
                    }
                    g2.setStroke(thindashed);
                    g2.draw(e.getLine());
                    g2.setStroke(new BasicStroke(4));
                }
            }
            g2.setColor(Color.ORANGE);
            for (AtomBound s : getSelectedBounds())
                if (bounds.contains(s))
                    g2.draw(s.getLine());
            if (getHighlightedBound() != null){
                g2.setColor(Color.GREEN);
                g2.draw(getHighlightedBound().getLine());
            }
            g2.setColor(Color.GRAY);
            if (getSelection() != null)
                g2.draw(getSelection());
        }
        private Shape createDefaultStar(double radius, double centerX,
                                        double centerY)
        {
            return createStar(centerX, centerY, radius, radius * 2.63, 5,
                    Math.toRadians(-18));
        }

        private Shape createStar(double centerX, double centerY,
                                 double innerRadius, double outerRadius, int numRays,
                                 double startAngleRad)
        {
            Path2D path = new Path2D.Double();
            double deltaAngleRad = Math.PI / numRays;
            for (int i = 0; i < numRays * 2; i++)
            {
                double angleRad = startAngleRad + i * deltaAngleRad;
                double ca = Math.cos(angleRad);
                double sa = Math.sin(angleRad);
                double relX = ca;
                double relY = sa;
                if ((i & 1) == 0)
                {
                    relX *= outerRadius;
                    relY *= outerRadius;
                }
                else
                {
                    relX *= innerRadius;
                    relY *= innerRadius;
                }
                if (i == 0)
                {
                    path.moveTo(centerX + relX, centerY + relY);
                }
                else
                {
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
    public MyButton getApplyToAllButton() {
        return menu.applyToAllButton;
    }

    public MyButton getTimeEvolutionButton() {
        return menu.timeEvolutionButton;
    }


    private class Menu extends MyPanel {

        private static final long serialVersionUID = 1L;
        MyButton applyToAllButton = new MyButton("", null);
        MyButton countStaticProperties = new MyButton("Count static properties", new ImageIcon(COUNT_LDOS_BUTTON_IMAGE_PATH));
        MyButton countNormalisation = new MyButton("Static normalisation(i)", new ImageIcon(NORMALISATION_BUTTON_IMAGE_PATH));
        MyButton showNormalisation = new MyButton("Charge(i)", new ImageIcon(NORMALISATION_BUTTON_IMAGE_PATH));
        MyButton showLDOS = new MyButton("Static LDOS(i)", new ImageIcon(LDOS_BUTTON_IMAGE_PATH));
        MyButton undoButton = new MyButton("Undo", new ImageIcon(UNDO_BUTTON_IMAGE_PATH));
        MyButton redoButton = new MyButton("Redo", new ImageIcon(REDO_BUTTON_IMAGE_PATH));
        MyButton timeEvolutionButton = new MyButton("Count time evolution", new ImageIcon(TIME_EVOLUTION_BUTTON_IMAGE_PATH));
        MyButton showLDOSTimeEvolutionButton = new MyButton("LDOS(t)", new ImageIcon(SHOW_LDOS_TIME_EVOLUTION_BUTTON_IMAGE_PATH));
        MyButton showCurrentTimeEvolutionButton = new MyButton("I(t)", new ImageIcon(SHOW_CURRENT_TIME_EVOLUTION_BUTTON_IMAGE_PATH));
        MyButton showTDOSTimeEvolutionButton = new MyButton("TDOS(t)", new ImageIcon(SHOW_LDOS_TIME_EVOLUTION_BUTTON_IMAGE_PATH));
        MyButton showAVGDOSTimeEvolutionButton = new MyButton("AVG(LDOS(t))", new ImageIcon(SHOW_LDOS_TIME_EVOLUTION_BUTTON_IMAGE_PATH));
        MyButton showChargeTimeEvolutionButton = new MyButton("N(t)", new ImageIcon(SHOW_NORMALISATION_TIME_EVOLUTION_BUTTON_IMAGE_PATH));
        MyButton showFermiLDOSTimeEvolutionButton = new MyButton("<html>LDOS<sub>E</sub>(t)</html>", new ImageIcon(SHOW_NORMALISATION_TIME_EVOLUTION_BUTTON_IMAGE_PATH));
        MyButton showLDOSLastTButton = new MyButton("LDOS(T_max)", new ImageIcon(LDOS_LAST_T_IMAGE_PATH));
        MyButton showNormalisationLastTButton = new MyButton("N(T_max)", new ImageIcon(NORMALISATION_LAST_T_IMAGE_PATH));
        JLabel additionalParamsLabel = new ConsolasFontLabel(Color.WHITE,"Surface Coupling", 16);
        JLabel parameterLabel = new ConsolasFontLabel(Color.WHITE,"Time Step", 16);
        JLabel secondAdditionalParamsLabel = new ConsolasFontLabel(Color.WHITE,"<html>k<sub>F</sub>a</html>", 16);
        JLabel thirdAdditionalParamsLabel = new ConsolasFontLabel(Color.WHITE,"Energy range", 16);
        JLabel atomIDLabel = new ConsolasFontLabel(Color.WHITE,"ID", 16);
        JLabel atomDataSavingLabel = new ConsolasFontLabel(Color.WHITE,"Data saving", 16);
        JLabel perturbationLabel = new ConsolasFontLabel(Color.WHITE,"Perturbation", 16);
        JLabel logo = new JLabel();
        JComboBox atomIDComboBox;
        MyColorBox colorBox = new MyColorBox();
        JComboCheckBox atomSavingComboBox;
        MyTextField perturbationTextField = new MyTextField();

        MyTextField energyRangeTextField = new MyTextField();
        MyTextField electrodeTypeTextField = new MyTextField();
        MyTextField atomTypeTextField = new MyTextField();
        MyTextField boundTypeTextField = new MyTextField();

        MyTextField kfaTextField = new MyTextField();
        MyTextField nZeroTextField = new MyTextField();
        MyTextField correlationTextField = new MyTextField();
        MyTextField electrodeTextField = new MyTextField();

        MyTextField dTTextField = new MyTextField();
        MyTextField dETextField = new MyTextField();
        MyTextField boundVTextField = new MyTextField();
        MyTextField atomEnergyTextField = new MyTextField();

        MyTextField surfaceCouplingTextField = new MyTextField();
        MyTextField electrodeCouplingTextField = new MyTextField();
        MyTextField spinOrbitTextField = new MyTextField();
        MyTextField spinFlipTextField = new MyTextField();

        SortedComboBoxModel model;

        public Menu() {
            super("");
            GridBagLayout layout = new GridBagLayout();
            setLayout(layout);
            applyToAllButton.setEnabled(false);
            ImageIcon icon = new ImageIcon(LOGO_IMAGE_PATH);
            Image img = icon.getImage() ;
            Image newimg = img.getScaledInstance( 250, 110,  Image.SCALE_SMOOTH ) ;
            ImageIcon imIc = new ImageIcon(newimg);
            logo.setIcon(imIc);
            redoButton.setToolTipText("Ctrl + Y");
            undoButton.setToolTipText("Ctrl + Z");
            showLDOS.setToolTipText("L");
            countStaticProperties.setToolTipText("Ctrl + L");
            GridBagConstraints pointer = new GridBagConstraints();
            model = new SortedComboBoxModel(new Vector());
            perturbationLabel.setVisible(false);
            atomIDComboBox = new JComboBox(model);
            atomIDComboBox.setVisible(false);
            atomIDLabel.setVisible(false);
            perturbationTextField.setVisible(false);
            Vector v = new Vector();
            v.add("Select...");
            v.add(new JCheckBox("LDOS", true));
            v.add(new JCheckBox("Normalisation", true));
            atomSavingComboBox = new JComboCheckBox(v);
            atomSavingComboBox.setVisible(false);
            atomDataSavingLabel.setVisible(false);
            pointer.fill = GridBagConstraints.HORIZONTAL;
            pointer.weightx = 0.5;
            pointer.weighty = 0.5;
            pointer.gridx = 0;
            pointer.gridy = 0;
            pointer.gridwidth = 2;
            add(logo, pointer);
            pointer.gridy++;
            pointer.gridwidth = 1;
            add(parameterLabel,pointer);
            pointer.gridy++;
            pointer.gridwidth = 2;

            //First parameters;
            add(boundVTextField, pointer);
            add(dTTextField, pointer);
            add(dETextField, pointer);
            pointer.gridy--;
            pointer.gridwidth = 1;
            pointer.gridx++;
            add(atomDataSavingLabel, pointer);
            pointer.gridx--;
            pointer.gridy++;

            add(atomEnergyTextField, pointer);

            pointer.gridx++;
            add(atomSavingComboBox, pointer);
            pointer.gridx--;
            pointer.gridy++;
            pointer.gridwidth = 2;
            add(additionalParamsLabel, pointer);
            pointer.gridy++;

            //Second parameters

            add(spinOrbitTextField, pointer);
            add(spinFlipTextField, pointer);
            add(electrodeCouplingTextField, pointer);
            add(surfaceCouplingTextField, pointer);
            pointer.gridy++;

            pointer.gridwidth = 1;
            add(secondAdditionalParamsLabel,pointer);
            pointer.gridy++;


            pointer.gridwidth = 2;

            //Third parameters
            add(nZeroTextField,pointer);
            add(correlationTextField,pointer);
            add(kfaTextField,pointer);
            add(electrodeTextField, pointer);

            pointer.gridy++;


            add(thirdAdditionalParamsLabel,pointer);
            pointer.gridwidth = 1;
            pointer.gridx++;

            add(atomIDLabel, pointer);
            add(perturbationLabel, pointer);
            pointer.gridx--;
            pointer.gridy++;

            //Fourth parameters

            add(electrodeTypeTextField,pointer);
            add(energyRangeTextField,pointer);
            add(atomTypeTextField,pointer);
            add(boundTypeTextField,pointer);
            pointer.gridx++;

            add(atomIDComboBox, pointer);
            add(perturbationTextField, pointer);
            pointer.gridx--;
            pointer.gridy++;
            pointer.gridwidth = 2;

            add(colorBox, pointer);
            pointer.gridy++;
            add(applyToAllButton, pointer);
            pointer.gridy++;
            add(countStaticProperties, pointer);
            pointer.gridy++;
            add(showLDOS, pointer);
            pointer.gridy++;
            add(countNormalisation, pointer);
            pointer.gridy++;
            add(undoButton, pointer);
            pointer.gridy++;
            add(redoButton, pointer);
            pointer.gridy++;
            add(timeEvolutionButton, pointer);
            pointer.gridy++;
            add(showLDOSTimeEvolutionButton, pointer);
            pointer.gridy++;
            add(showCurrentTimeEvolutionButton, pointer);
            pointer.gridy++;
            add(showAVGDOSTimeEvolutionButton, pointer);
            pointer.gridy++;
            add(showTDOSTimeEvolutionButton, pointer);
            pointer.gridy++;
            add(showChargeTimeEvolutionButton, pointer);
            pointer.gridy++;
            add(showFermiLDOSTimeEvolutionButton, pointer);
            pointer.gridy++;
            add(showNormalisation,pointer);
            pointer.gridy++;
            add(showLDOSLastTButton, pointer);
            pointer.gridy++;
            add(showNormalisationLastTButton, pointer);

            showLDOSLastTButton.addActionListener(e -> showLastT(LDOS_FILE_NAME_PATTERN));
            showNormalisationLastTButton.addActionListener(e -> showLastT(NORMALISATION_FILE_NAME_PATTERN));
            showLDOS.addActionListener(e -> showLDOS());
            undoButton.addActionListener(e -> undo());
            redoButton.addActionListener(e -> redo());
            countStaticProperties.addActionListener(e -> countStaticProperties());

            timeEvolutionButton.addActionListener(evt -> {
                if (getDynamicCalculationsThread() != null){
                    getDynamicCalculationsThread().stop();
                    setDynamicCalculationsThread(null);
                    timeEvolutionButton.setImageIcon(new ImageIcon(TIME_EVOLUTION_BUTTON_IMAGE_PATH));
                    timeEvolutionButton.setText("Count time evolution");
                    getMenu().applyToAllButton.setIcon(null);
                    getMenu().applyToAllButton.setEnabled(false);
                    getMenu().applyToAllButton.setText("");
                    setStepCount("0");
                }
                else
                {
                    getIsCanceled().setValue(false);
                    timeEvolutionButton.setImageIcon(new ImageIcon(DELETE_BUTTON_IMAGE_PATH));
                    timeEvolutionButton.setText("Cancel");
                    Runnable myRunnable = () -> new TimeEvolutionHelper(NanoModeller.this, getIsInterupted());
                    setDynamicCalculationsThread(new Thread(myRunnable));
                    getMenu().applyToAllButton.setEnabled(true);
                    getMenu().applyToAllButton.setImageIcon(new ImageIcon(NEXT_STEP_IMAGE_PATH));
                    getMenu().applyToAllButton.setEnabled(true);
                    NanoModeller.this.repaint();
                    getDynamicCalculationsThread().start();
                }

            });
            showLDOSTimeEvolutionButton.addActionListener(evt -> showLDOSTimeEvolution());
            showCurrentTimeEvolutionButton.addActionListener(evt -> showCurrentTimeEvolution());
            showTDOSTimeEvolutionButton.addActionListener(evt -> showTDOSTimeEvolution());
            showAVGDOSTimeEvolutionButton.addActionListener(evt -> showAVGDOSTimeEvolution());
            showChargeTimeEvolutionButton.addActionListener(evt -> showNormalisationTimeEvolution());
            showFermiLDOSTimeEvolutionButton.addActionListener(evt -> showFermiLDOSTimeEvolution());
            showNormalisation.addActionListener(evt -> showChargeTimeEvolution());
            colorBox.setSelectedItem(Globals.BLACK);
            colorBox.addItemListener(itemEvent -> {
                if (getHighlightedShape() != null)
                {
                    getHighlightedShape().setColor(colorBox.getSelectedItem() + "");
                }
                else if (getHighlightedElectrode() != null){
                    getHighlightedElectrode().setColor(colorBox.getSelectedItem() + "");
                }
                else if (getHighlightedBound() != null){
                    getHighlightedBound().setColor(colorBox.getSelectedItem() + "");
                }
                else {
                    setbColor(colorBox.getSelectedItem() + "");
                }
                getPaintSurface().repaint();
            });
            countNormalisation.addActionListener(evt -> showNormalisation());

            dETextField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    performAction();
                }
                @Override
                public void removeUpdate(DocumentEvent e) {
                    performAction();
                }
                public void changedUpdate(DocumentEvent e) {
                    performAction();
                }
                public void performAction(){
                    if (getHighlightedElectrode() != null){
                        getHighlightedElectrode().setdE(dETextField.getText());
                    }
                }
            });
            atomEnergyTextField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    performAction();
                }
                @Override
                public void removeUpdate(DocumentEvent e) {
                    performAction();
                }
                public void changedUpdate(DocumentEvent e) {
                    performAction();
                }
                public void performAction(){
                    if (getHighlightedShape() != null)
                    {
                        getHighlightedShape().setEnergy(atomEnergyTextField.getText());
                    }
                }
            });
            boundVTextField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    performAction();
                }
                @Override
                public void removeUpdate(DocumentEvent e) {
                    performAction();
                }
                public void changedUpdate(DocumentEvent e) {
                    performAction();
                }
                public void performAction(){
                    if (getHighlightedBound() != null){
                        getHighlightedBound().setValue(boundVTextField.getText());
                    }
                }
            });
            dTTextField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    performAction();
                }
                @Override
                public void removeUpdate(DocumentEvent e) {
                    performAction();
                }
                public void changedUpdate(DocumentEvent e) {
                    performAction();
                }
                public void performAction(){
                        setDt(dTTextField.getText());
                }
            });

            electrodeCouplingTextField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    performAction();
                }
                @Override
                public void removeUpdate(DocumentEvent e) {
                    //performAction();
                }
                public void changedUpdate(DocumentEvent e) {
                    performAction();
                }
                public void performAction(){
                    String text = electrodeCouplingTextField.getText();
                    if (getHighlightedElectrode() != null){
                        getHighlightedElectrode().setCoupling(text);
                    }
                }
            });
            surfaceCouplingTextField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    performAction();
                }
                @Override
                public void removeUpdate(DocumentEvent e) {
                    //performAction();
                }
                public void changedUpdate(DocumentEvent e) {
                    performAction();
                }
                public void performAction(){
                    String text = surfaceCouplingTextField.getText();
                    setSurfaceCoupling(text);
                }
            });
            spinOrbitTextField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    performAction();
                }
                @Override
                public void removeUpdate(DocumentEvent e) {
                    //performAction();
                }
                public void changedUpdate(DocumentEvent e) {
                    performAction();
                }
                public void performAction(){
                    String text = spinOrbitTextField.getText();
                    if (getHighlightedBound() != null){
                        getHighlightedBound().setSpinOrbit(text);
                    }
                }
            });
            spinFlipTextField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    performAction();
                }
                @Override
                public void removeUpdate(DocumentEvent e) {
                    //performAction();
                }
                public void changedUpdate(DocumentEvent e) {
                    performAction();
                }
                public void performAction(){
                    String text = spinFlipTextField.getText();
                    if (getHighlightedShape() != null){
                        getHighlightedShape().setSpinFlip(text);
                    }
                }
            });
            energyRangeTextField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    performAction();
                }
                @Override
                public void removeUpdate(DocumentEvent e) {
                    performAction();
                }
                public void changedUpdate(DocumentEvent e) {
                    performAction();
                }
                public void performAction(){
                    String text = energyRangeTextField.getText();
                    setEnergyRange(text);
                    getPaintSurface().repaint();
                }
            });
            boundTypeTextField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    performAction();
                }
                @Override
                public void removeUpdate(DocumentEvent e) {
                    performAction();
                }
                public void changedUpdate(DocumentEvent e) {
                    performAction();
                }
                public void performAction(){
                    String text = boundTypeTextField.getText();
                    if (getHighlightedBound() != null){
                        getHighlightedBound().setType(text);
                    }
                    getPaintSurface().repaint();
                }
            });
            electrodeTypeTextField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    performAction();
                }
                @Override
                public void removeUpdate(DocumentEvent e) {
                    performAction();
                }
                public void changedUpdate(DocumentEvent e) {
                    performAction();
                }
                public void performAction(){
                    String text = electrodeTypeTextField.getText();
                    if (getHighlightedElectrode() != null){
                        getHighlightedElectrode().setType(text);
                    }
                    getPaintSurface().repaint();
                }
            });
            atomTypeTextField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    performAction();
                }
                @Override
                public void removeUpdate(DocumentEvent e) {
                    performAction();
                }
                public void changedUpdate(DocumentEvent e) {
                    performAction();
                }
                public void performAction(){
                    String text = atomTypeTextField.getText();
                    if (getHighlightedShape() != null){
                        getHighlightedShape().setType(text);
                    }
                    getPaintSurface().repaint();
                }
            });

            atomIDComboBox.addItemListener(itemEvent -> {
                if (getHighlightedShape() != null)
                {
                    int previousID = getHighlightedShape().getID();
                    int newID = (Integer) atomIDComboBox.getSelectedItem();
                    for (AtomShape shape : getShapes()){
                        if (shape.getID() == newID){
                            shape.setID(previousID);
                        }
                    }
                    getHighlightedShape().setID(newID);

                }
            });

            perturbationTextField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    performAction();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    performAction();
                }

                public void changedUpdate(DocumentEvent e) {
                    performAction();
                }
                public void performAction(){
                    String text = perturbationTextField.getText();
                    if (getHighlightedBound() != null) {
                        getHighlightedBound().setPerturbation(text);

                    } else if (getHighlightedElectrode() != null) {
                        getHighlightedElectrode().setPerturbation(text);
                    }
                }
            });
            atomSavingComboBox.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent itemEvent) {
                    if (getHighlightedShape() != null){
                        atomSavingComboBox.setPopupVisible(true);
                    }
                }
            });
            atomSavingComboBox.addPopupMenuListener(new PopupMenuListener() {
                @Override
                public void popupMenuWillBecomeVisible(PopupMenuEvent e) {

                }

                @Override
                public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                    if (getHighlightedShape() != null){
                        getHighlightedShape().setSaveLDOS(atomSavingComboBox.getLDOS());
                        getHighlightedShape().setSaveNormalisation(atomSavingComboBox.getNormalisation());
                        atomSavingComboBox.setSelectedIndex(0);
                    }
                }

                @Override
                public void popupMenuCanceled(PopupMenuEvent e) {

                }
            });
            kfaTextField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    performAction();
                }
                @Override
                public void removeUpdate(DocumentEvent e) {
                    performAction();
                }
                public void changedUpdate(DocumentEvent e) {
                    performAction();
                }
                public void performAction(){
                    String text = kfaTextField.getText();
                    setkFa(text);
                }
            });
            correlationTextField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    performAction();
                }
                @Override
                public void removeUpdate(DocumentEvent e) {
                    performAction();
                }
                public void changedUpdate(DocumentEvent e) {
                    performAction();
                }
                public void performAction(){
                    String text = correlationTextField.getText();
                    if (getHighlightedBound() != null){
                        getHighlightedBound().setCorrelationCoupling(text);
                    }
                }
            });
            nZeroTextField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    performAction();
                }
                @Override
                public void removeUpdate(DocumentEvent e) {
                    performAction();
                }
                public void changedUpdate(DocumentEvent e) {
                    performAction();
                }
                public void performAction(){
                    String text = nZeroTextField.getText();
                    if (getHighlightedShape() != null){
                        getHighlightedShape().setnZero(text);
                    }
                }
            });
            applyToAllButton.addActionListener(evt -> {
                if (getHighlightedElectrode() != null){
                    for (ElectrodeShape e : getElectrodes()){
                        if (StringUtils.equalsIgnoreNullWhitespace(getHighlightedElectrode().getType(), e.getType())){
                            e.setdE(dETextField.getText());
                            e.setCoupling(electrodeCouplingTextField.getText());
                            e.setColor(getHighlightedElectrode().getColor());
                            e.setPerturbation(getHighlightedElectrode().getPerturbation());
                        }
                    }
                    ToastMessage toastMessage = new ToastMessage("Applied to all electrodes", TOAST_MESSAGE_DURATION, NanoModeller.this);
                    toastMessage.setVisible(true);
                }
                else if (getHighlightedBound() != null){
                    for (AtomBound b : bounds){
                        if (StringUtils.equalsIgnoreNullWhitespace(getHighlightedBound().getType(), b.getType())) {
                            b.setValue(boundVTextField.getText());
                            b.setCorrelationCoupling(correlationTextField.getText());
                            b.setSpinOrbit(spinOrbitTextField.getText());
                            b.setColor(getHighlightedBound().getColor());
                            b.setPerturbation(getHighlightedBound().getPerturbation());
                        }
                    }
                    ToastMessage toastMessage = new ToastMessage("Applied to all bounds", TOAST_MESSAGE_DURATION, NanoModeller.this);
                    toastMessage.setVisible(true);
                }
                else if (getHighlightedShape() != null){
                    for(AtomShape shape: getShapes()){
                        if (StringUtils.equalsIgnoreNullWhitespace(getHighlightedShape().getType(), shape.getType())) {
                            shape.setEnergy(atomEnergyTextField.getText());
                            shape.setSpinFlip(spinFlipTextField.getText());
                            shape.setnZero(nZeroTextField.getText());
                            shape.setColor(getHighlightedShape().getColor());
                            shape.setCorrelation(getHighlightedShape().getCorrelation());
                            shape.setSaveNormalisation(getHighlightedShape().isSaveNormalisation());
                            shape.setSaveLDOS(getHighlightedShape().isSaveLDOS());
                        }
                    }
                    ToastMessage toastMessage = new ToastMessage("Applied to all atoms", TOAST_MESSAGE_DURATION, NanoModeller.this);
                    toastMessage.setVisible(true);
                }
                else if (getDynamicCalculationsThread() != null){
                    getIsInterupted().neg();
                    NanoModeller.this.repaint();
                }
                getPaintSurface().repaint();
            });
            setTextFieldsVisibility(TextFieldType.SURFACE);
        }



        private void setTextFieldsVisibility(TextFieldType type){

            spinOrbitTextField.setVisible(TextFieldType.BOUND.equals(type));
            boundVTextField.setVisible(TextFieldType.BOUND.equals(type));
            boundTypeTextField.setVisible(TextFieldType.BOUND.equals(type));
            correlationTextField.setVisible(TextFieldType.BOUND.equals(type));


            spinFlipTextField.setVisible(TextFieldType.ATOM.equals(type));
            atomEnergyTextField.setVisible(TextFieldType.ATOM.equals(type));
            atomTypeTextField.setVisible(TextFieldType.ATOM.equals(type));
            nZeroTextField.setVisible(TextFieldType.ATOM.equals(type));
            atomSavingComboBox.setVisible(TextFieldType.ATOM.equals(type));
            atomDataSavingLabel.setVisible(TextFieldType.ATOM.equals(type));

            dETextField.setVisible(TextFieldType.ELECTRODE.equals(type));
            electrodeCouplingTextField.setVisible(TextFieldType.ELECTRODE.equals(type));
            electrodeTypeTextField.setVisible(TextFieldType.ELECTRODE.equals(type));
            electrodeTextField.setVisible(TextFieldType.ELECTRODE.equals(type));

            surfaceCouplingTextField.setVisible(TextFieldType.SURFACE.equals(type));
            dTTextField.setVisible(TextFieldType.SURFACE.equals(type));
            energyRangeTextField.setVisible(TextFieldType.SURFACE.equals(type));
            kfaTextField.setVisible(TextFieldType.SURFACE.equals(type));


        }
    }
    enum TextFieldType {
        ATOM,
        BOUND,
        ELECTRODE,
        SURFACE
    }

    private void showAVGDOSTimeEvolution() {

        int n = JOptionPane.showConfirmDialog(
                this,
                "Do you want to recalculate?",
                "Recalculation",
                JOptionPane.YES_NO_OPTION);
        if (n == JOptionPane.YES_OPTION) {

            TreePath[] selectedFilesPATHS = stepRecorder.fileBrowser.getTree().getSelectionPaths();
            File[] files = new File[getSelectedAtoms().size()];
            int i = 0;
            if (selectedFilesPATHS.length != 1) {
                JOptionPane.showMessageDialog(this, "You cannot have multiple diectories selected!");
                return;
            }
            DefaultMutableTreeNode lastPathComponent = (DefaultMutableTreeNode) selectedFilesPATHS[0].getLastPathComponent();
            File directory = (FileBrowser.FileNode) (lastPathComponent.getUserObject());
            for (AtomShape atom : getSelectedAtoms()) {
                if (!atom.getAtom().isSaveLDOS()) {
                    JOptionPane.showMessageDialog(this, "For atom with ID " + atom.getID() +
                            " there was no LDOS calculated. You cannot proceed!");
                    return;
                }
                files[i] = new File(directory.getAbsolutePath() + "/" + LDOS_FILE_NAME_PATTERN + atom.getID() + TXT);
                i++;
            }
            MyFileWriter.sumFiles(files, directory.getAbsolutePath() + "/AVGDOS.txt");
            overwriteDynamicAVGDOSGnuplotFile(directory.getAbsolutePath());
        }
        runFile(DYNAMIC_AVGDOS_GNUPLOT_FILE_PATH);

    }

    private void showTDOSTimeEvolution() {
        TreePath[] selectedFilesPATHS = stepRecorder.fileBrowser.getTree().getSelectionPaths();
        if (selectedFilesPATHS.length != 1){
            JOptionPane.showMessageDialog(this, "You cannot have multiple diectories selected!");
            return;
        }
        DefaultMutableTreeNode lastPathComponent = (DefaultMutableTreeNode) selectedFilesPATHS[0].getLastPathComponent();
        File directory = (FileBrowser.FileNode) (lastPathComponent.getUserObject());
        overwriteDynamicTDOSGnuplotFile(directory.getAbsolutePath());
        runFile(DYNAMIC_TDOS_GNUPLOT_FILE_PATH);
    }

    private int atomIDSeq() {
        int max = -1;
        for (AtomShape shape : getShapes()){
            max = shape.getID() > max ? shape.getID() : max;
        }
        max++;
        return max;
    }
    protected int electrodeIDSeq() {
        int max = -1;
        for (ElectrodeShape e : getElectrodes()){
            max = e.getID() > max ? e.getID() : max;
        }
        max++;
        return max;
    }
    public void repaint(){
        getPaintSurface().repaint();
    }
}