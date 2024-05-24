package org.nanomodeller.GUI;

import org.nanomodeller.*;
import org.nanomodeller.Calculation.StaticProperties;
import org.nanomodeller.GUI.Adapters.MovingAdapter;
import org.nanomodeller.GUI.Adapters.KeyAdapter;
import org.nanomodeller.GUI.Dialogs.ElementPropertiesDialog;
import org.nanomodeller.GUI.Dialogs.ToastMessage;
import org.nanomodeller.GUI.Menus.LeftMenuPanel;
import org.nanomodeller.GUI.Menus.Menu;
import org.nanomodeller.GUI.Menus.RightMenuPanel;
import org.nanomodeller.GUI.ViewComponents.*;
import org.nanomodeller.Tools.DataAccessTools.MyFileWriter;
import org.nanomodeller.Tools.Flag;
import org.nanomodeller.Tools.JGnuPlot;
import org.nanomodeller.Tools.StringUtils;
import org.nanomodeller.XMLMappingFiles.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import static org.nanomodeller.GUI.ImageTools.SvgImageHelper.imageFromSVGFile;
import static org.nanomodeller.Tools.DataAccessTools.FileOperationHelper.runFile;
import static org.nanomodeller.Tools.DataAccessTools.OverwriteGnuplotFile.*;
import static org.nanomodeller.Globals.*;
import static org.nanomodeller.Tools.StringUtils.nvl;
import static org.nanomodeller.XMLMappingFiles.XMLHelper.*;

public class NanoModeler extends JFrame {
    private static NanoModeler instance;
    public static NanoModeler getInstance(){
        if (instance == null){
            instance = new NanoModeler();
        }
        return instance;
    }
    @Serial
    private static final long serialVersionUID = 1L;
    private int x;
    private int y;
    private BufferedImage hAtomImage;
    private BufferedImage hElectrodeImage;
    private BufferedImage atomImage;
    private Image scalledAtomImage;
    private Image scalledHAtomImage;
    private Image scalledHElectrodeImage;
    private Image electrodeImage;
    private Image scalledElectrodeImage;
    private Thread dynamicCalculationsThread;
    private double screenWidth;
    private double screenHeight;
    private PaintSurface paintSurface = new PaintSurface(this);
    private JScrollPane scrollPane = new FuturisticScrollPane(getPaintSurface());
    private Hashtable<Integer,Atom> atoms = new Hashtable<>();
    private Hashtable<Integer,Electrode> electrodes = new Hashtable<>();
    public ArrayList<Bond> getBonds() {
        return Parameters.getInstance().getBonds();
    }
    private ArrayList<Bond> selectedBonds = new ArrayList<>();
    private Hashtable<Integer, Atom>  selectedAtoms = new Hashtable<Integer, Atom>();
    private Hashtable<Integer, Electrode> selectedElectrodes = new Hashtable<Integer, Electrode>();
    private Hashtable<Integer, Electrode> copiedElectrodes = new Hashtable<Integer, Electrode>();
    private Hashtable<Integer, Atom> copiedAtoms = new Hashtable<Integer, Atom>();
    private ArrayList<Bond> copiedBonds = new ArrayList<>();
    private boolean selectionFlag = false;
    private boolean showGrid = true;
    private Flag isInterupted;
    private Flag isCanceled;
    private Rectangle selection = null;
    private Point anchor;
    private Atom currentAtom = null;
    private RightMenuPanel rightMenuPanel;
    private LeftMenuPanel leftMenuPanel;
    private int maxFileNum = 0;
    private boolean ctrlPressed = false;
    private String currentDataPath;
    private String time = "0.0";
    public Atom getAtomByID(int id){
        return atoms.get(id);
    }
    public BufferedImage getAtomImage() {
        return atomImage;
    }
    public Image getScalledAtomImage() {
        return scalledAtomImage;
    }
    public BufferedImage gethAtomImage() {
        return hAtomImage;
    }

    public BufferedImage gethElectrodeImage() {
        return hElectrodeImage;
    }

    public Image getScalledHAtomImage() {
        return scalledHAtomImage;
    }

    public Image getScalledHElectrodeImage() {
        return scalledHElectrodeImage;
    }

    public Image getElectrodeImage() {
        return electrodeImage;
    }
    public Image getScalledElectrodeImage() {
        return scalledElectrodeImage;
    }
    public  void initNanoModeller() {
        try {
            UIManager.setLookAndFeel("com.formdev.flatlaf.FlatDarkLaf");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        NanoModeler inst = getInstance();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        inst.setScreenWidth(screenSize.getWidth()/4);
        inst.setScreenHeight(screenSize.getHeight()/4);
        ImageIcon icon = new ImageIcon(ICON_IMAGE_PATH);
        atomImage = imageFromSVGFile(ICON_ATOM_PATH);
        hAtomImage = imageFromSVGFile(ICON_HATOM_PATH);
        hElectrodeImage = imageFromSVGFile(ICON_HELECTRODE_PATH);
        electrodeImage = imageFromSVGFile(ICON_ELECTRODE_PATH);
        Image img = icon.getImage() ;
        inst.setIconImage(img);
        inst.setMenu(new RightMenuPanel(this));
        inst.setStepRecorder(new LeftMenuPanel());
        inst.readDataFromObject(null);
        inst.getStepRecorder().setPreferredSize(new Dimension((int)inst.screenWidth/2, (int)inst.screenHeight/2));
        inst.setTitle(APP_NAME);
        resizeImages(Parameters.getInstance().getGridSize());
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
        MovingAdapter ma = new MovingAdapter(this);
        inst.getPaintSurface().addMouseListener(ma);
        inst.getPaintSurface().addMouseMotionListener(ma);
        inst.getPaintSurface().addMouseWheelListener(ma);
        inst.getPaintSurface().addKeyListener(new KeyAdapter(this));
        inst.getPaintSurface().requestFocus();
        inst.setJMenuBar(new Menu(inst));
        inst.setVisible(true);
    }
    public void readDataFromObject(MyTextField time){
        Parameters p = Parameters.getInstance();
        GlobalProperties gp = GlobalProperties.getInstance();
        setbColor(gp.getColor());
        this.setDt(gp.getDt() + "");
        this.setEnergyRange(gp.getEnergyRange());
        if (StringUtils.isNotEmpty(p.getPath())) {
            setGridSize(p.getGridSize());
            if (time != null){
                time.setText(p.getTime());
            }
            this.setSurfaceCoupling(p.getSurfaceCoupling());
            this.setkFa(p.getkFa());
            for (Atom atom : p.getAtoms()){
                atoms.put(atom.getID(),atom);
            }
            for (Electrode el : p.getElectrodes()){
                electrodes.put(el.getID(),el);
            }
        }else{
            clearAll();
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
        if (getSelectedAtoms() != null && !getSelectedAtoms().isEmpty()) {
            TreePath[] selectedFilesPATHS = leftMenuPanel.fileBrowser.getTree().getSelectionPaths();
            ArrayList<String> selectedSteps = new ArrayList<>();
            assert selectedFilesPATHS != null;
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
        int id = atoms.get(i).getID() + 4;
        String ids = "2:3:" + id;
        command += StringUtils.toSingleQuotes(paths) + String.format(" i %s ", step) + " u " + ids + " title " + StringUtils.toSingleQuotes("");
        command += " with pm3d";
        plot.appendCommand(command);
        plot.pause(1000);
        plot.plot();
    }

    private void showPlot(String filePattern, boolean is3D, boolean isMultiplot){

        if (getSelectedAtoms() != null && !getSelectedAtoms().isEmpty()) {
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
//                    if (isHidden || !hasSteps){
//                        if (isMultiplot)
//                            jgp.addPlotCommandMultiplot(paths, getSelectedAtoms(), selectedSteps, "");
//                        else
                            jgp.addPlotCommandForSelectedAtoms(paths, getSelectedAtoms(), "E", "LDOS");
//                    }
//                    else{
//                        if (isMultiplot)
//                            jgp.add2DSplotCommandMultiplot(paths, getSelectedAtoms(), "");
//                        else
//                            jgp.add2DSplotCommand(paths, getSelectedAtoms(), "");
//
//                    }
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
    public void showCurrentTimeEvolution() {
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
        getAtoms().clear();
        getBonds().clear();
        getElectrodes().clear();
        getPaintSurface().repaint();
    }

    public void recalculateIDS(){
        int count = 0;
        for (Integer i: getSortedKeys(atoms)){
            Atom atom = atoms.get(i);
            int oldID = atom.getID();
            atom.setID(count);
            int newID = count;
            getBonds().stream().filter(bond -> bond.getFirst() == oldID).forEach(bond -> bond.setFirst(-newID - 1));
            getBonds().stream().filter(bond -> bond.getSecond() == oldID).forEach(bond -> bond.setSecond(-newID - 1));
            getElectrodes().values().stream().filter(electrode -> electrode.getAtomIndex() == oldID).
                    forEach(electrode -> electrode.setAtomIndex(newID));
            atoms.remove(oldID);
            atoms.put(newID, atom);
            count++;
        }
        count = 0;
        for (Integer i: getSortedKeys(electrodes)){
            electrodes.get(i).setID(count);
        }
        getBonds().stream().filter(bond -> bond.getFirst() < 0).forEach(bond -> bond.setFirst(-bond.getFirst() - 1));
        getBonds().stream().filter(bond -> bond.getSecond() < 0).forEach(bond -> bond.setSecond(-bond.getSecond() - 1));

    }
    public void delete() {
        for (Atom s : getSelectedAtoms().values()) {
            removeAtom(s);
        }
        ArrayList<Bond> clone = (ArrayList<Bond>) getBonds().clone();
        for (Bond s : clone) {
            if (getSelectedBonds().contains(s) ||
                    (atoms.get(s.getFirst()) == null || atoms.get(s.getSecond()) == null)){
                getBonds().remove(s);
            }
        }
        for (Electrode electrode : getSelectedElectrodes().values()) {
            getElectrodes().remove(electrode);
        }
        recalculateIDS();
        getPaintSurface().repaint();
    }

    private void removeAtom(Atom s) {
        if (s != null) {
            int id = s.getID();
            atoms.remove(id);
        }
    }


    public void refresh() {
        clearAll();
        readPropertiesFromXMLFile(Parameters.getInstance().getPath() + "/parameters.xml");
        readDataFromObject(NanoModeler.getInstance().getStepRecorder().timeTextField);
        getPaintSurface().repaint();
    }

    public void zoom(int zoomMagnitude) {

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
        p.getAtoms().clear();
        p.getElectrodes().clear();
        getAtoms().values().stream().forEach(atom -> p.addAtom(atom));
        getElectrodes().values().stream().forEach(electrode -> p.addElectrode(electrode));
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
//        for (Atom s : getShapes()) {
//            s.getAtom().setX(s.getBounds().x/(1.0 * getGridSize())+"");
//            s.getAtom().setY(s.getBounds().y/(1.0 * getGridSize())+ "");
//            p.addAtom(s.getAtom());
//            s.setID(ii++);
//        }
//        p.getElectrodes().clear();
//        ListIterator iter = getElectrodes().listIterator();
//        while (iter.hasNext()){
//            Electrode electrode = (Electrode)iter.next();
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
//        for (Bond bound : bounds) {
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
            recalculateIDS();
            convertObjectToXMLFile(GlobalProperties.getInstance());
            Parameters.getInstance().setPath(leftMenuPanel.fileBrowser.getAbsolutePath());
            String path = "parameters.xml";
            String fullPath = leftMenuPanel.fileBrowser.createFile(path);
            mapParameters();
            convertObjectToXMLFile(Parameters.getInstance(),fullPath);
            ToastMessage toastMessage = new ToastMessage("Data saved", TOAST_MESSAGE_DURATION, this);
            toastMessage.setVisible(true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void rescaleShapes(double mlply){
        for (Atom atom : getAtoms().values()){
            atom.setCoordinates(atom.getX() * mlply, atom.getY() * mlply);
        }
        for (Electrode electrode : getElectrodes().values()){
            electrode.setCoordinates(electrode.getX() * mlply  , electrode.getY() * mlply);
        }
    }
    public void align(){
        double gridSize = getGridSize() + 0.0;
        for (Atom atom : getAtoms().values()){
            int xMultiplier = (int) Math.round(atom.getX()/ gridSize);
            int yMultiplier = (int) Math.round(atom.getY()/ gridSize);
            double newX = xMultiplier * gridSize;
            double newY = yMultiplier * gridSize;
            atom.setCoordinates(newX, newY);
        }
        for (Electrode electrode : getElectrodes().values()) {
            int xMultiplier = (int) Math.round(electrode.getX() / gridSize);
            int yMultiplier = (int) Math.round(electrode.getY() / gridSize);
            int newX = (int) (xMultiplier * gridSize);
            int newY = (int) (yMultiplier * gridSize);
            electrode.setCoordinates(newX, newY);
        }
        getPaintSurface().repaint();
    }
    public void flipVertically() {
        int maxX = Integer.MIN_VALUE;
        int minX = Integer.MAX_VALUE;
        if (getSelectedAtoms().size() > 0) {
            minX = getSelectedAtoms().values().stream().map(atom -> atom.getX()).min(Integer::compareTo).get();
            maxX = getSelectedAtoms().values().stream().map(atom -> atom.getX()).max(Integer::compareTo).get();
        }
        if (getSelectedElectrodes().size() > 0) {
            int eminX = getSelectedElectrodes().values().stream().map(electrode -> electrode.getX()).min(Integer::compareTo).get();
            int emaxX = getSelectedElectrodes().values().stream().map(electrode -> electrode.getX()).max(Integer::compareTo).get();
            if(emaxX > maxX)
                maxX = emaxX;
            if(eminX < minX)
                minX = eminX;
        }
        int middleX = (minX + maxX)/2;
        selectedAtoms.values().stream().forEach(a -> a.setX(2 * middleX - a.getX()));
        selectedElectrodes.values().stream().forEach(e -> e.setX(2 * middleX - e.getX()));
        paintSurface.repaint();
    }
    public void flipHorizontally() {
        int maxY = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE;
        if (getSelectedAtoms().size() > 0) {
            minY = getSelectedAtoms().values().stream().map(atom -> atom.getY()).min(Integer::compareTo).get();
            maxY = getSelectedAtoms().values().stream().map(atom -> atom.getY()).max(Integer::compareTo).get();
        }
        if (getSelectedElectrodes().size() > 0) {
            int eminY = getSelectedElectrodes().values().stream().map(electrode -> electrode.getY()).min(Integer::compareTo).get();
            int emaxY = getSelectedElectrodes().values().stream().map(electrode -> electrode.getY()).max(Integer::compareTo).get();
            if(emaxY > maxY)
                maxY = emaxY;
            if(eminY < minY)
                minY = eminY;
        }
        int middleY = (minY + maxY)/2;
        selectedAtoms.values().stream().forEach(a -> a.setY(2 * middleY - a.getY()));
        selectedElectrodes.values().stream().forEach(e -> e.setY(2 * middleY - e.getY()));
        paintSurface.repaint();
    }
    public List<Integer> getSortedKeys(Hashtable<Integer, ?> table){
       return table.keySet().stream().sorted().collect(Collectors.toList());
    }
    public void paste() {

        ArrayList<Electrode> newElectrodeSelection = new ArrayList<Electrode>();
        Hashtable<Atom, Atom> copy = new Hashtable<>();
        int leftMostX = copiedAtoms.values().stream().map(atom -> atom.getX()).min(Integer::compareTo).get();
        int upperMostY = copiedAtoms.values().stream().map(atom -> atom.getY()).min(Integer::compareTo).get();
        selectedAtoms.clear();
        for (Integer i: getSortedKeys(copiedAtoms)){
            Atom s = copiedAtoms.get(i);
            Atom as = new Atom(s, getX() + (s.getX() - leftMostX), getY() + (s.getY() - upperMostY), atomIDSeq());
            atoms.put(as.getID(), as);
            selectedAtoms.put(as.getID(), as);
            copy.put(s, as);
        }
        for (Atom first : copy.keySet()){
            for (Atom second : copy.keySet()){
                Bond bond = Parameters.getInstance().getBond(first.getID(), second.getID());
                if(bond != null){
                    Atom firstAtom = copy.get(first);
                    Atom secondAtom = copy.get(second);
                    if (Parameters.getInstance().areBond(firstAtom, secondAtom)){
                        continue;
                    }
                    Bond newBond = new Bond(firstAtom.getID(), secondAtom.getID(), bond);
                    getBonds().add(newBond);
                }
            }
        }
        for (Integer e : getSortedKeys(copiedElectrodes)){
            Electrode electrode = copiedElectrodes.get(e);
            Atom as = null;
            if (electrode.getAtomIndex() >= 0){
                as = copy.get(electrode.getAtomIndex());
            }
            Electrode el = new Electrode(as.getID(), electrodeIDSeq(), electrode.getX(), electrode.getY() );
            getElectrodes().put(el.getID(), el);
            newElectrodeSelection.add(el);
        }
        getSelectedElectrodes().clear();
        getPaintSurface().repaint();
    }
    public void copy() {
        getCopiedAtoms().clear();
        setCopiedAtoms((Hashtable<Integer, Atom>) getSelectedAtoms().clone());
        setCopiedElectrodes((Hashtable<Integer, Electrode>) getSelectedElectrodes().clone());
        getCopiedBonds().clear();
        ArrayList <Bond> resultBounds = new ArrayList();
        ArrayList <Bond> boundsToCopy = (ArrayList<Bond>) getSelectedBonds().clone();
        for (Bond bound : boundsToCopy){
            if (getCopiedAtoms().contains(getAtomByID(bound.getFirst())) && getCopiedAtoms().contains(getAtomByID(bound.getSecond()))){
                resultBounds.add(bound);
            }
        }
        setCopiedBonds(resultBounds);
    }
    private void setCopiedAtoms(Hashtable<Integer, Atom> toCopy) {
        copiedAtoms = toCopy;
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
    public Hashtable<Integer, Atom> getAtoms() {
        return atoms;
    }
    public Hashtable<Integer, Electrode> getElectrodes() {
        return electrodes;
    }
    public void setElectrodes(Hashtable<Integer,Electrode> electrodes) {
        this.electrodes = electrodes;
    }
    public Hashtable<Integer,Atom> getSelectedAtoms() {
        return selectedAtoms;
    }
    public Hashtable<Integer,Electrode> getSelectedElectrodes() {
        return selectedElectrodes;
    }
    public Hashtable<Integer,Electrode> getCopiedElectrodes() {
        return copiedElectrodes;
    }
    public void setCopiedElectrodes(Hashtable<Integer,Electrode> copiedElectrodes) {
        this.copiedElectrodes = copiedElectrodes;
    }
    public ArrayList<Bond> getSelectedBonds() {
        return selectedBonds;
    }
    public Hashtable<Integer,Atom> getCopiedAtoms() {
        return copiedAtoms;
    }
    public ArrayList<Bond> getCopiedBonds() {
        return copiedBonds;
    }
    public void setCopiedBonds(ArrayList<Bond> copiedBonds) {
        this.copiedBonds = copiedBonds;
    }
    public int getGridSize() {
        return Parameters.getInstance().getGridSize() > 0 ? Parameters.getInstance().getGridSize() : 20;
    }
    public void setGridSize(int gridSize) {
        Parameters.getInstance().setGridSize(gridSize);
        resizeImages(gridSize);
    }
    private void resizeImages(int gridSize) {
        scalledAtomImage = atomImage.getScaledInstance(gridSize * 4, gridSize * 4, 1);
        scalledHAtomImage = hAtomImage.getScaledInstance(gridSize * 4, gridSize * 4, 1);
        scalledElectrodeImage = electrodeImage.getScaledInstance(gridSize * 4, gridSize * 4, 1);
        scalledHElectrodeImage = hElectrodeImage.getScaledInstance(gridSize * 4, gridSize * 4, 1);
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
    public Atom getCurrentAtom() {
        return currentAtom;
    }
    public void setCurrentAtom(Atom currentAtom) {
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
    public MyButton getTimeEvolutionButton() {
        return rightMenuPanel.getTimeEvolutionButton();
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
            for (Atom atom : getSelectedAtoms().values()) {
                if (!Globals.isTrue(atom.getString("save_ldos").toString())) {
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
    public int atomIDSeq() {
        int max = -1;
        for (Atom shape : getAtoms().values()){
            max = shape.getID() > max ? shape.getID() : max;
        }
        max++;
        return max;
    }
    public int electrodeIDSeq() {
        int max = -1;
        for (Electrode e : getElectrodes().values()){
            max = e.getID() > max ? e.getID() : max;
        }
        max++;
        return max;
    }
    public void repaint(){
        getPaintSurface().repaint();
    }
    public void showPropertiesTextArea(){
        Element element = null;
        Optional<Atom> selectedAtom = getSelectedAtoms().values().stream().findFirst();
        if (selectedAtom.isPresent()){
            element = selectedAtom.get();
        }
        if (element == null){
            Optional<Electrode> electrode = getSelectedElectrodes().values().stream().findFirst();
            if (electrode.isPresent()){
                element = electrode.get();
            }
        }
        if (element == null){
            Optional<Bond> bond = getSelectedBonds().stream().findFirst();
            if (bond.isPresent()){
                element = bond.get();
            }
        }
        if (element == null){

        }else {
            String data = XMLHelper.convertObjectToXMLString(element);
            showElementPropertiesTextArea(element, data);
        }

    }
    public static String showTextArea(String text, String title){
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = (int)screenSize.getWidth();
        int height = (int)screenSize.getHeight();
        JTextArea tf = new JTextArea(text);
        tf.setAutoscrolls(true);
        tf.setFont(new Font("Consolas", Font.PLAIN, 20));
        tf.setLineWrap(true);
        JScrollPane sp = new JScrollPane(tf);
        sp.setPreferredSize(new Dimension(width/3,height/4));
        sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        int result = JOptionPane.showConfirmDialog(
                null, sp, title,
                JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            return tf.getText();
        }
        return null;
    }
    public static void showElementPropertiesTextArea(Element element, String text){
        String formattedText = text.substring(text.indexOf("\n") + 1);
        formattedText = formattedText.replaceAll("<x>.*?</x>\n", "").replaceAll("<y>.*?</y>\n", "");
        formattedText = formattedText.replaceAll("        </Atom>", "</Atom>");
        formattedText = formattedText.replaceAll("        </Electrode>", "</Electrode>");
        ElementPropertiesDialog elementPropertiesDialog = new ElementPropertiesDialog(element, formattedText);
        elementPropertiesDialog.setVisible(true);
    }
}