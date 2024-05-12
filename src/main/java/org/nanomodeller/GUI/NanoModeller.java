package org.nanomodeller.GUI;

import org.nanomodeller.*;
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
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import static org.nanomodeller.Tools.DataAccessTools.FileOperationHelper.runFile;
import static org.nanomodeller.Tools.DataAccessTools.OverwriteGnuplotFile.*;
import static org.nanomodeller.Globals.*;
import static org.nanomodeller.Tools.StringUtils.nvl;
import static org.nanomodeller.XMLMappingFiles.XMLHelper.*;

public class NanoModeller extends JFrame {

    private static NanoModeller instance;
    public static NanoModeller getInstance(){
        if (instance == null){
            instance = new NanoModeller();
        }
        return instance;
    }

    public String getCurrentPath(){
        return leftMenuPanel.fileBrowser.getNodes().get(leftMenuPanel.fileBrowser.getSelectedFiles()[0]).getPath()[0].toString();
    }

    @Serial
    private static final long serialVersionUID = 1L;
    private int x;
    private int y;
    private double selectionXMax = -1;
    private double selectionXMin = -1;
    private double selectionYMax = -1;
    private double selectionYMin = -1;

    private Thread dynamicCalculationsThread;
//    private String surfaceCoupling;
//    private String kFa;
//    private String dE;
//    private String bColor;
//    private String dt;
//    private String energyRange;
    private double screenWidth;
    private double screenHeight;
    private PaintSurface paintSurface = new PaintSurface(this);
    private JScrollPane scrollPane = new JScrollPane(getPaintSurface());
    private ArrayList<AtomShape> shapes = new ArrayList<>();

    public ArrayList<AtomBound> getAtomBounds() {
        return bounds;
    }

    public RightMenuPanel getRightMenuPanel() {
        return rightMenuPanel;
    }

    public LeftMenuPanel getLeftMenuPanel() {
        return leftMenuPanel;
    }

    @Override
    public boolean isActive() {
        return isActive;
    }

    private ArrayList<AtomBound> bounds = new ArrayList<AtomBound>();
    private ArrayList<ElectrodeShape> electrodes = new ArrayList<ElectrodeShape>();
    private ArrayList<AtomShape>  selectedAtoms = new ArrayList<AtomShape>();
    private ArrayList<ElectrodeShape> selectedElectrodes = new ArrayList<ElectrodeShape>();
    private ArrayList<ElectrodeShape> copiedElectrodes = new ArrayList<ElectrodeShape>();
    private ArrayList<AtomBound> selectedBounds = new ArrayList<AtomBound>();
    private ArrayList<AtomShape> copiedAtoms = new ArrayList<AtomShape>();
    private ArrayList<AtomBound> copiedBounds = new ArrayList<AtomBound>();
    //private int gridSize;
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
    private RightMenuPanel rightMenuPanel;
    private LeftMenuPanel leftMenuPanel;
    private int maxFileNum = 0;
    private boolean ctrlPressed = false;
    private String currentDataPath;
    private String time = "0.0";
    private boolean isActive = true;

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
    public  void initNanoModeller() {

        NanoModeller inst = getInstance();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        inst.setScreenWidth(screenSize.getWidth()/4);
        inst.setScreenHeight(screenSize.getHeight()/4);
        ImageIcon icon = new ImageIcon(ICON_IMAGE_PATH);
        Image img = icon.getImage() ;
        inst.setIconImage(img);
        inst.setMenu(new RightMenuPanel(this));
        inst.setStepRecorder(new LeftMenuPanel());
        inst.readDataFromObject(true, null);
        inst.getStepRecorder().setPreferredSize(new Dimension((int)inst.screenWidth/2, (int)inst.screenHeight/2));
        inst.setTitle(APP_NAME);
        inst.getPaintSurface().setPreferredSize(new Dimension((int)(inst.getGridSize() * inst.getScreenWidth()),(int)(inst.getGridSize() * inst.getScreenHeight())));
        inst.setSize((int)inst.screenWidth * 3, (int)inst.screenHeight * 3);
        inst.getScrollPane().setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        inst.getScrollPane().setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        inst.scrollPane.setBackground(Color.BLACK);
        inst.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        inst.setIsInterupted(new Flag(false));
        inst.setIsCanceled(new Flag(false));
        inst.add(inst.getScrollPane(), BorderLayout.CENTER);
        inst.add(inst.getMenu(), BorderLayout.EAST);
        inst.add(inst.getStepRecorder(), BorderLayout.WEST);
        inst.getScrollPane().getVerticalScrollBar().setValue(inst.getScrollPane().getVerticalScrollBar().getValue()+ 1);
        MovingAdapter ma = new MovingAdapter();
       inst.getPaintSurface().addMouseListener(ma);
       inst.getPaintSurface().addMouseMotionListener(ma);
       inst.getPaintSurface().addMouseWheelListener(ma);
       inst.getPaintSurface().addKeyListener(new MyKeyAdapter());
       inst.getPaintSurface().requestFocus();
       inst.setJMenuBar(new org.nanomodeller.GUI.Menu(inst));
       inst.setVisible(true);
    }

//    public void readData(String path, boolean refreshData){
//        readXMLData(path, refreshData, null);
//    }
//    public void readXMLData(String path, boolean refreshData, MyTextField time) {
//        GlobalProperties gp;
//        String XMLPath =  XML_FILE_PATH;
//        gp = readGlobalPropertiesFromXMLFile(XMLPath);
//        readDataFromObject(gp, path, refreshData, time);
//    }

    public void readDataFromObject(boolean refreshData, MyTextField time){
        Parameters p = Parameters.getInstance();
        GlobalProperties gp = GlobalProperties.getInstance();
        getShapes().clear();
        bounds.clear();
        getElectrodes().clear();
        int diameter = 2 * getGridSize();
        getMenu().dTTextField.setText(getDt());
        setbColor(gp.getColor());
        getMenu().colorBox.setSelectedItem(nvl(getbColor(), Globals.WHITE));
        double dE = gp.getdE();
        this.setDt(gp.getDt() + "");
        this.setEnergyRange(gp.getEnergyRange());
        getMenu().energyRangeTextField.setText(getEnergyRange());
        if (p != null) {
            if (!refreshData) {
                setGridSize(p.getGridSize());
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

    public void countStaticProperties() {
        saveData();
        StaticProperties.countStaticProperties(getCurrentDataPath());
        ToastMessage toastMessage = new ToastMessage("LDOS counting finished ", TOAST_MESSAGE_DURATION, this);
        toastMessage.setVisible(true);
    }
    public void showNormalisation() {
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
        GlobalProperties gp;
        gp = GlobalProperties.getInstance();
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
            TreePath[] selectedFilesPATHS = leftMenuPanel.fileBrowser.getTree().getSelectionPaths();
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
            TreePath[] selectedFilesPATHS = leftMenuPanel.fileBrowser.getTree().getSelectionPaths();
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

    public void showLDOSTimeEvolution() {
        runGnuplotThread(LDOS_FILE_NAME_PATTERN, true);
    }
    public void showNormalisationTimeEvolution() {
        runGnuplotThread(NORMALISATION_FILE_NAME_PATTERN, true);
    }
    public void showChargeTimeEvolution() {
        runGnuplotThread(CHARGE_FILE_NAME_PATTERN, false);
    }
    void showCurrentTimeEvolution() {
        runGnuplotThread(CURRENT_FILE_NAME_PATTERN, false);
    }
    public void showFermiLDOSTimeEvolution() {
        runGnuplotThread(LDOS_E_FILE_NAME_PATTERN, false);
    }

    public void showLastT(String filePattern) {

//            saveBlockGivenT("/" + filePattern   ".csv");
//            overwriteLastTFile(filePattern, index);
//            String path = Globals.LAST_T_GNUPLOT_FILE_PATH;
//            runFile(path);
    }

    public void showLDOS() {
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


    public void refresh() {
        setHighlightedShape(null);
        setHighlightedBound(null);
        setHighlightedElectrode(null);
        getPaintSurface().repaint();
    }

    public void reload(){

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
    public Parameters mapParameters(){
        Parameters p = Parameters.getInstance();
        p.setTime(time);
        p.setSurfaceCoupling(getSurfaceCoupling());
        p.setkFa(getkFa());
        p.setNumber("" + getShapes().size());
        //p.setGridSize(getGridSize() + "");
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
        return p;
    }
    public GlobalProperties mapGlobalPropertiesObject(String time, String path, boolean active){
        GlobalProperties gp;
        gp = GlobalProperties.getInstance();
      //  readPropertiesFromXMLFile(getCurrentPath());


//        Parameters p = gp.getParamByName(path);
//        if (p == null){
//            p = new Parameters();
//            p.setName(path);
//            gp.addParameters(p);
//        }
//        p.setId("0");
//        p.setTime(time);
//        p.setActive(active);
        gp.setEnergyRange(getEnergyRange());
        gp.setDt(Double.parseDouble(getDt()));
        gp.setColor(getbColor());
//        p.setSurfaceCoupling(getSurfaceCoupling());
//        p.setkFa(getkFa());
//        p.setNumber("" + getShapes().size());
//        p.setGridSize(getGridSize() + "");
//        p.getAtoms().clear();
//        int ii = 0;
//        Collections.sort(getShapes());
//        for (AtomShape s : getShapes()) {
//            s.getAtom().setX(s.getShape().getBounds().x/(1.0 * getGridSize())+"");
//            s.getAtom().setY(s.getShape().getBounds().y/(1.0 * getGridSize())+ "");
//            p.addAtom(s.getAtom());
//            s.setID(ii++);
//        }
//        p.getElectrodes().clear();
//        ListIterator iter = getElectrodes().listIterator();
//        while (iter.hasNext()){
//            ElectrodeShape electrode = (ElectrodeShape)iter.next();
//            Electrode electrodeToSave = electrode.getElectrode();
//            electrodeToSave.setX("" + electrode.getRectangle().getBounds().x/(getGridSize() * 1.0));
//            electrodeToSave.setY("" + electrode.getRectangle().getBounds().y/(getGridSize() * 1.0));
//            if (electrode.getLine() != null){
//                electrodeToSave.setAtomIndex(electrode.getAtom().getID());
//            }
//            else{
//                electrodeToSave.setAtomIndex(-1);
//            }
//            electrodeToSave.setId(iter.nextIndex() - 1);
//            p.addElectode(electrodeToSave);
//        }
//        p.getBounds().clear();
//        for (AtomBound bound : bounds) {
//            bound.updateAtoms();
//            p.addBound(bound.getBound());
//        }
//        if (getList() != null){
//           // ArrayList<Parameters> paramsCopy = new ArrayList<Parameters>();
////            for(int i = 0; i< getListModel().getSize(); i++){
////                Parameters param = gp.getParamByName(getList().getModel().getElementAt(i).toString());
////                param.setId((i+1) + "");
////                paramsCopy.add(param);
////            }
//           // gp.setParameters();
//        }
        return gp;
    }

    public void saveData() {
        try {
            convertObjectToXML(GlobalProperties.getInstance());
            Parameters.getInstance().setPath(leftMenuPanel.fileBrowser.getAbsolutePath());
            String path = "parameters.xml";
            String fullPath = leftMenuPanel.fileBrowser.createFile(path);
            mapParameters();
            convertObjectToXML(Parameters.getInstance(),fullPath);
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
        return Parameters.getInstance().getSurfaceCoupling();
    }

    public void setSurfaceCoupling(String surfaceCoupling) {
        Parameters.getInstance().setSurfaceCoupling(surfaceCoupling);
    }

    public String getkFa() {
        return Parameters.getInstance().getkFa();
    }

    public void setkFa(String kFa) {
        Parameters.getInstance().setkFa(kFa);
    }



    public String getbColor() {
        return  GlobalProperties.getInstance().getColor();
    }

    public void setbColor(String bColor) {
        GlobalProperties.getInstance().setColor(bColor);
    }

    public String getDt() {
        return GlobalProperties.getInstance().getDt() + "";
    }

    public void setDt(String dt) {
        if (StringUtils.isNotEmpty(dt))
            GlobalProperties.getInstance().setDt(Double.parseDouble(dt));
    }

    public String getEnergyRange() {
        return GlobalProperties.getInstance().getEnergyRange();
    }

    public void setEnergyRange(String energyRange) {
        if (StringUtils.isNotEmpty(energyRange))
            GlobalProperties.getInstance().setEnergyRange(energyRange);
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
    public JScrollPane getScrollPane() {
        return scrollPane;
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
        return Parameters.getInstance().getGridSize() > 0 ? Parameters.getInstance().getGridSize() : 20;
    }

    public void setGridSize(int gridSize) {
        Parameters.getInstance().setGridSize(gridSize);
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

    public RightMenuPanel getMenu() {
        return rightMenuPanel;
    }

    public void setMenu(RightMenuPanel rightMenuPanel) {
        this.rightMenuPanel = rightMenuPanel;
    }

    public LeftMenuPanel getStepRecorder() {
        return leftMenuPanel;
    }

    public void setStepRecorder(LeftMenuPanel leftMenuPanel) {
        this.leftMenuPanel = leftMenuPanel;
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
                    refresh();
                    setCtrlPressed(false);
                }
                if (e.getKeyCode() == KeyEvent.VK_S){
                    saveData();
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

    public MyButton getApplyToAllButton() {
        return rightMenuPanel.applyToAllButton;
    }

    public MyButton getTimeEvolutionButton() {
        return rightMenuPanel.timeEvolutionButton;
    }


    enum TextFieldType {
        ATOM,
        BOUND,
        ELECTRODE,
        SURFACE
    }

    public void showAVGDOSTimeEvolution() {

        int n = JOptionPane.showConfirmDialog(
                this,
                "Do you want to recalculate?",
                "Recalculation",
                JOptionPane.YES_NO_OPTION);
        if (n == JOptionPane.YES_OPTION) {

            TreePath[] selectedFilesPATHS = leftMenuPanel.fileBrowser.getTree().getSelectionPaths();
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

    public void showTDOSTimeEvolution() {
        TreePath[] selectedFilesPATHS = leftMenuPanel.fileBrowser.getTree().getSelectionPaths();
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