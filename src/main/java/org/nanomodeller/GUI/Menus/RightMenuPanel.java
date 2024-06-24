package org.nanomodeller.GUI.Menus;

import org.nanomodeller.Calculation.TimeEvolutionHelper;
import org.nanomodeller.GUI.NanoModeler;
import org.nanomodeller.GUI.ViewComponents.*;

import javax.swing.*;
import java.awt.*;

import static org.nanomodeller.Globals.*;

public class RightMenuPanel extends MyPanel {
    JProgressBar firstPB = new JProgressBar();
    JProgressBar secondPB = new JProgressBar();
    JProgressBar thirdPB = new JProgressBar();
    MyButton countStaticProperties = new MyButton("Count static properties", new ImageIcon(COUNT_LDOS_BUTTON_IMAGE_PATH));
    MyButton countCharge = new MyButton("Static charge(i)", new ImageIcon(NORMALISATION_BUTTON_IMAGE_PATH));
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

    public void clearBars(){
        getFirstPB().setString("0%");
        getFirstPB().setValue(0);
        getSecondPB().setString("0%");
        getSecondPB().setValue(0);
        getThirdPB().setString("0%");
        getThirdPB().setValue(0);
    }
    public RightMenuPanel(NanoModeler nanoModeler) {
        super("");
        GridBagLayout layout = new GridBagLayout();
        setLayout(layout);
        showLDOS.setToolTipText("L");
        countStaticProperties.setToolTipText("Ctrl + L");
        GridBagConstraints pointer = new GridBagConstraints();
        pointer.fill = GridBagConstraints.BOTH;
        pointer.weightx = 1;
        pointer.weighty = 1;
        pointer.gridy++;
        pointer.gridy++;
        put(firstPB, pointer);
        firstPB.setStringPainted(true);
        secondPB.setStringPainted(true);
        firstPB.setForeground(new Color(0x109613));
        secondPB.setForeground(Color.BLUE);
        thirdPB.setForeground(new Color(0x0000A4));
        firstPB.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        secondPB.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        thirdPB.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        thirdPB.setStringPainted(true);
        put(secondPB, pointer);
        put(thirdPB, pointer);
        put(countStaticProperties, pointer);
        put(showLDOS, pointer);
        put(countCharge, pointer);
        put(timeEvolutionButton, pointer);
        put(showLDOSTimeEvolutionButton, pointer);
        put(showCurrentTimeEvolutionButton, pointer);
        put(showAVGDOSTimeEvolutionButton, pointer);
        put(showTDOSTimeEvolutionButton, pointer);
        put(showChargeTimeEvolutionButton, pointer);
        put(showFermiLDOSTimeEvolutionButton, pointer);
        put(showNormalisation, pointer);
        put(showLDOSLastTButton, pointer);
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
        countCharge.addActionListener(evt -> nanoModeler.showCharge());
    }
    public void put(JComponent c, GridBagConstraints pointer){
        add(c, pointer);
        pointer.gridy++;
    }
    public MyButton getTimeEvolutionButton() {
        return timeEvolutionButton;
    }

    public JProgressBar getFirstPB() {
        return firstPB;
    }

    public JProgressBar getSecondPB() {
        return secondPB;
    }

    public JProgressBar getThirdPB() {
        return thirdPB;
    }
}
