package org.nanomodeller.GUI.Menus;

import org.nanomodeller.GUI.NanoModeler;
import org.nanomodeller.GUI.TestForm;
import org.nanomodeller.GUI.ViewComponents.MyMenuItem;
import org.nanomodeller.XMLMappingFiles.GlobalProperties;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import static org.nanomodeller.Tools.DataAccessTools.FileOperationHelper.runFile;
import static org.nanomodeller.Tools.DataAccessTools.MyFileWriter.saveBlockGivenT;
import static org.nanomodeller.XMLMappingFiles.XMLHelper.convertObjectToXMLFile;


public class Menu extends JMenuBar {
    NanoModeler sm;

    public Menu(NanoModeler sm) {
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
        MyMenuItem plotOptions = new MyMenuItem("Plot options");
        MyMenuItem dynamicDataDir= new MyMenuItem("Select root directory");
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

            GlobalProperties gp = GlobalProperties.getInstance();
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
                    convertObjectToXMLFile(gp);
                    this.sm.getStepRecorder().fileBrowser.changeRootNode(dynamicPATH);
                }
                UIManager.setLookAndFeel(previousLF);
            }
            catch (Exception e) {

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
        menu.add(plotOptions);
        menu.add(dynamicDataDir);
        menu.add(showGrid);
        menu.add(calculateT);
        menu.add(menuItemExit);
        add(menu);
        add(about);
        add(help);
    }
}