package org.nanomodeller.GUI;

import org.nanomodeller.GUI.ViewComponents.MyMenuItem;
import org.nanomodeller.Globals;
import org.nanomodeller.XMLMappingFiles.GlobalChainProperties;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import static org.nanomodeller.Tools.DataAccessTools.FileOperationHelper.runFile;
import static org.nanomodeller.Tools.DataAccessTools.MyFileWriter.saveBlockGivenT;
import static org.nanomodeller.XMLMappingFiles.XMLHelper.convertObjectToXML;
import static org.nanomodeller.XMLMappingFiles.XMLHelper.readParametersFromXMLFile;

public class Menu extends JMenuBar {
    NanoModeller sm;

    public Menu(NanoModeller sm) {
        this.sm = sm;
        JMenu menu = new JMenu("Menu");
        JMenu about= new JMenu("About");
        JMenu help= new JMenu("Help");
        menu.setFont(new Font("Consolas", Font.PLAIN, 22));
        about.setFont(new Font("Consolas", Font.PLAIN, 22));
        help.setFont(new Font("Consolas", Font.PLAIN, 22));
        about.setMnemonic(KeyEvent.VK_A);
        menu.setMnemonic(KeyEvent.VK_M);
        help.setMnemonic(KeyEvent.VK_H);
        MyMenuItem matrixItemOptions = new MyMenuItem("Open matrix file");
        MyMenuItem plotOptions = new MyMenuItem("Plot options");
        MyMenuItem dynamicDataDir= new MyMenuItem("Select dynamic data directory");
        MyMenuItem staticDataDir= new MyMenuItem("Select static data directory");
        MyMenuItem showGrid = new MyMenuItem("Show/hide grid");
        MyMenuItem calculateT = new MyMenuItem("Create crossection at given t");
        MyMenuItem menuItemExit = new MyMenuItem("Exit");
        menuItemExit.setMnemonic(KeyEvent.VK_E);
        menuItemExit.setToolTipText("Exit application");
        showGrid.addActionListener((ActionEvent event) -> {
            sm.setShowGrid(!sm.isShowGrid());
            sm.repaint();
        });
        calculateT.addActionListener((ActionEvent event) -> {
            String newValue = (String) JOptionPane.showInputDialog(
                    this.sm,
                    "Insert t:",
                    "Crossection",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    null,
                    "");
            saveBlockGivenT(newValue, sm.getCurrentDataPath());

        });
        menuItemExit.addActionListener((ActionEvent event) -> {
            System.exit(0);
        });
        plotOptions.addActionListener((ActionEvent event) -> {
            new TestForm();
        });
        dynamicDataDir.addActionListener((ActionEvent event) -> {

            GlobalChainProperties gp = readParametersFromXMLFile(Globals.XML_FILE_PATH);
            LookAndFeel previousLF = UIManager.getLookAndFeel();
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                JFileChooser chooser = new JFileChooser();
                chooser.setCurrentDirectory(new File(gp.getDynamicPATH()));
                chooser.setDialogTitle("Select dynamic data directory");
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                chooser.setAcceptAllFileFilterUsed(false);
                if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                    String dynamicPATH = chooser.getSelectedFile().getPath();
                    gp.setDynamicPATH(dynamicPATH);
                    convertObjectToXML(gp);
                    this.sm.getStepRecorder().fileBrowser.changeRootNode(dynamicPATH);
                }
                UIManager.setLookAndFeel(previousLF);
            }
            catch (Exception e) {

            }
        });
        staticDataDir.addActionListener((ActionEvent event) -> {
            LookAndFeel previousLF = UIManager.getLookAndFeel();
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                GlobalChainProperties gp = readParametersFromXMLFile(Globals.XML_FILE_PATH);
                JFileChooser chooser = new JFileChooser();
                chooser.setCurrentDirectory(new File(gp.getStaticPATH()));
                chooser.setDialogTitle("Select static data directory");
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                chooser.setAcceptAllFileFilterUsed(false);
                if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                    String staticPATH = chooser.getSelectedFile().getPath();
                    gp.setStaticPATH(staticPATH);
                    convertObjectToXML(gp);
                }
                UIManager.setLookAndFeel(previousLF);
            }
            catch (Exception e) {

            }
        });
        matrixItemOptions.addActionListener((ActionEvent event) -> {
            FileDialog fd = new FileDialog(new JFrame());
            fd.setVisible(true);
            File[] f = fd.getFiles();
            if(f.length > 0){
                System.out.println(fd.getFiles()[0].getAbsolutePath());
            }
        });
        help.addMenuListener(new MenuListener() {

            public void menuSelected(MenuEvent e) {
                runFile("help/help.html");
            }

            public void menuDeselected(MenuEvent e) {
                System.out.println("menuDeselected");

            }

            public void menuCanceled(MenuEvent e) {
                System.out.println("menuCanceled");

            }
        });
        menu.add(matrixItemOptions);
        menu.add(plotOptions);
        menu.add(staticDataDir);
        menu.add(dynamicDataDir);
        menu.add(showGrid);
        menu.add(calculateT);
        menu.add(menuItemExit);
        add(menu);
        add(about);
        add(help);
    }
}