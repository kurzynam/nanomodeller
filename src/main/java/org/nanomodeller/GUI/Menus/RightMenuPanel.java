package org.nanomodeller.GUI.Menus;

import org.nanomodeller.Calculation.TimeEvolutionHelper;
import org.nanomodeller.GUI.NanoModeler;
import org.nanomodeller.GUI.ViewComponents.*;
import org.nanomodeller.Globals;

import javax.swing.*;
import java.awt.*;
import java.util.Vector;

import static org.nanomodeller.Globals.*;

public class RightMenuPanel extends MyPanel {

    private static final long serialVersionUID = 1L;
    MyButton countStaticProperties = new MyButton("Count static properties", new ImageIcon(COUNT_LDOS_BUTTON_IMAGE_PATH));
    MyButton countNormalisation = new MyButton("Static normalisation(i)", new ImageIcon(NORMALISATION_BUTTON_IMAGE_PATH));
    MyButton showNormalisation = new MyButton("Charge(i)", new ImageIcon(NORMALISATION_BUTTON_IMAGE_PATH));
    MyButton showLDOS = new MyButton("Static LDOS(i)", new ImageIcon(LDOS_BUTTON_IMAGE_PATH));
    MyButton timeEvolutionButton = new MyButton("Count time evolution", new ImageIcon(TIME_EVOLUTION_BUTTON_IMAGE_PATH));
    MyButton showLDOSTimeEvolutionButton = new MyButton("LDOS(t)", new ImageIcon(SHOW_LDOS_TIME_EVOLUTION_BUTTON_IMAGE_PATH));
    MyButton showCurrentTimeEvolutionButton = new MyButton("I(t)", new ImageIcon(SHOW_CURRENT_TIME_EVOLUTION_BUTTON_IMAGE_PATH));
    MyButton showTDOSTimeEvolutionButton = new MyButton("TDOS(t)", new ImageIcon(SHOW_LDOS_TIME_EVOLUTION_BUTTON_IMAGE_PATH));
    MyButton showAVGDOSTimeEvolutionButton = new MyButton("AVG(LDOS(t))", new ImageIcon(SHOW_LDOS_TIME_EVOLUTION_BUTTON_IMAGE_PATH));
    MyButton showChargeTimeEvolutionButton = new MyButton("N(t)", new ImageIcon(SHOW_NORMALISATION_TIME_EVOLUTION_BUTTON_IMAGE_PATH));
    MyButton showFermiLDOSTimeEvolutionButton = new MyButton("<html>LDOS<sub>E</sub>(t)</html>", new ImageIcon(SHOW_NORMALISATION_TIME_EVOLUTION_BUTTON_IMAGE_PATH));
    MyButton showLDOSLastTButton = new MyButton("LDOS(T_max)", new ImageIcon(LDOS_LAST_T_IMAGE_PATH));
    MyButton showNormalisationLastTButton = new MyButton("N(T_max)", new ImageIcon(NORMALISATION_LAST_T_IMAGE_PATH));



    public RightMenuPanel(NanoModeler nanoModeler) {
        super("");
        GridBagLayout layout = new GridBagLayout();
        setLayout(layout);
        showLDOS.setToolTipText("L");
        countStaticProperties.setToolTipText("Ctrl + L");
        GridBagConstraints pointer = new GridBagConstraints();
        pointer.fill = GridBagConstraints.VERTICAL;
        pointer.weightx = 1;
        pointer.weighty = 1;
        pointer.gridy++;
        pointer.gridy++;
        add(countStaticProperties, pointer);
        pointer.gridy++;
        add(showLDOS, pointer);
        pointer.gridy++;
        add(countNormalisation, pointer);
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
        add(showNormalisation, pointer);
        pointer.gridy++;
        add(showLDOSLastTButton, pointer);
        pointer.gridy++;
        add(showNormalisationLastTButton, pointer);

        showLDOSLastTButton.addActionListener(e -> nanoModeler.showLastT(LDOS_FILE_NAME_PATTERN));
        showNormalisationLastTButton.addActionListener(e -> nanoModeler.showLastT(NORMALISATION_FILE_NAME_PATTERN));
        showLDOS.addActionListener(e -> nanoModeler.showLDOS());
        countStaticProperties.addActionListener(e -> nanoModeler.countStaticProperties());

        timeEvolutionButton.addActionListener(evt -> {
            if (nanoModeler.getDynamicCalculationsThread() != null) {
                nanoModeler.getDynamicCalculationsThread().stop();
                nanoModeler.setDynamicCalculationsThread(null);
                timeEvolutionButton.setImageIcon(new ImageIcon(TIME_EVOLUTION_BUTTON_IMAGE_PATH));
                timeEvolutionButton.setText("Count time evolution");
            } else {
                nanoModeler.getIsCanceled().setValue(false);
                timeEvolutionButton.setImageIcon(new ImageIcon(DELETE_BUTTON_IMAGE_PATH));
                timeEvolutionButton.setText("Cancel");
                Runnable myRunnable = () -> new TimeEvolutionHelper(nanoModeler, nanoModeler.getIsInterupted());
                nanoModeler.setDynamicCalculationsThread(new Thread(myRunnable));
                nanoModeler.repaint();
                nanoModeler.getDynamicCalculationsThread().start();
            }

        });
        showLDOSTimeEvolutionButton.addActionListener(evt -> nanoModeler.showLDOSTimeEvolution());
        showCurrentTimeEvolutionButton.addActionListener(evt -> nanoModeler.showCurrentTimeEvolution());
        showTDOSTimeEvolutionButton.addActionListener(evt -> nanoModeler.showTDOSTimeEvolution());
        showAVGDOSTimeEvolutionButton.addActionListener(evt -> nanoModeler.showAVGDOSTimeEvolution());
        showChargeTimeEvolutionButton.addActionListener(evt -> nanoModeler.showNormalisationTimeEvolution());
        showFermiLDOSTimeEvolutionButton.addActionListener(evt -> nanoModeler.showFermiLDOSTimeEvolution());
        showNormalisation.addActionListener(evt -> nanoModeler.showChargeTimeEvolution());
        countNormalisation.addActionListener(evt -> nanoModeler.showNormalisation());
    }

    public MyButton getCountStaticProperties() {
        return countStaticProperties;
    }

    public MyButton getCountNormalisation() {
        return countNormalisation;
    }

    public MyButton getShowNormalisation() {
        return showNormalisation;
    }

    public MyButton getShowLDOS() {
        return showLDOS;
    }

    public MyButton getTimeEvolutionButton() {
        return timeEvolutionButton;
    }

    public MyButton getShowLDOSTimeEvolutionButton() {
        return showLDOSTimeEvolutionButton;
    }

    public MyButton getShowCurrentTimeEvolutionButton() {
        return showCurrentTimeEvolutionButton;
    }

    public MyButton getShowTDOSTimeEvolutionButton() {
        return showTDOSTimeEvolutionButton;
    }

    public MyButton getShowAVGDOSTimeEvolutionButton() {
        return showAVGDOSTimeEvolutionButton;
    }

    public MyButton getShowChargeTimeEvolutionButton() {
        return showChargeTimeEvolutionButton;
    }

    public MyButton getShowFermiLDOSTimeEvolutionButton() {
        return showFermiLDOSTimeEvolutionButton;
    }

    public MyButton getShowLDOSLastTButton() {
        return showLDOSLastTButton;
    }

    public MyButton getShowNormalisationLastTButton() {
        return showNormalisationLastTButton;
    }
}
