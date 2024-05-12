package org.nanomodeller.GUI;

import org.nanomodeller.Dynamics.TimeEvolutionHelper;
import org.nanomodeller.GUI.Shapes.AtomBound;
import org.nanomodeller.GUI.Shapes.AtomShape;
import org.nanomodeller.GUI.Shapes.ElectrodeShape;
import org.nanomodeller.GUI.ViewComponents.*;
import org.nanomodeller.Globals;
import org.nanomodeller.Tools.StringUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Vector;

import static org.nanomodeller.Globals.*;

class RightMenuPanel extends MyPanel {

    private static final long serialVersionUID = 1L;
    private final NanoModeller nanoModeller;
    MyButton applyToAllButton = new MyButton("", null);
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
    JLabel additionalParamsLabel = new ConsolasFontLabel(Color.WHITE, "Surface Coupling", 16);
    JLabel parameterLabel = new ConsolasFontLabel(Color.WHITE, "Time Step", 16);
    JLabel secondAdditionalParamsLabel = new ConsolasFontLabel(Color.WHITE, "<html>k<sub>F</sub>a</html>", 16);
    JLabel thirdAdditionalParamsLabel = new ConsolasFontLabel(Color.WHITE, "Energy range", 16);
    JLabel atomIDLabel = new ConsolasFontLabel(Color.WHITE, "ID", 16);
    JLabel atomDataSavingLabel = new ConsolasFontLabel(Color.WHITE, "Data saving", 16);
    JLabel perturbationLabel = new ConsolasFontLabel(Color.WHITE, "Perturbation", 16);
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

    public RightMenuPanel(NanoModeller nanoModeller) {
        super("");
        this.nanoModeller = nanoModeller;
        GridBagLayout layout = new GridBagLayout();
        setLayout(layout);
        applyToAllButton.setEnabled(false);
        ImageIcon icon = new ImageIcon(LOGO_IMAGE_PATH);
        Image img = icon.getImage();
        Image newimg = img.getScaledInstance(250, 110, Image.SCALE_SMOOTH);
        ImageIcon imIc = new ImageIcon(newimg);
        logo.setIcon(imIc);
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
        add(parameterLabel, pointer);
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
        add(secondAdditionalParamsLabel, pointer);
        pointer.gridy++;


        pointer.gridwidth = 2;

        //Third parameters
        add(nZeroTextField, pointer);
        add(correlationTextField, pointer);
        add(kfaTextField, pointer);
        add(electrodeTextField, pointer);

        pointer.gridy++;


        add(thirdAdditionalParamsLabel, pointer);
        pointer.gridwidth = 1;
        pointer.gridx++;

        add(atomIDLabel, pointer);
        add(perturbationLabel, pointer);
        pointer.gridx--;
        pointer.gridy++;

        //Fourth parameters

        add(electrodeTypeTextField, pointer);
        add(energyRangeTextField, pointer);
        add(atomTypeTextField, pointer);
        add(boundTypeTextField, pointer);
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

        showLDOSLastTButton.addActionListener(e -> nanoModeller.showLastT(LDOS_FILE_NAME_PATTERN));
        showNormalisationLastTButton.addActionListener(e -> nanoModeller.showLastT(NORMALISATION_FILE_NAME_PATTERN));
        showLDOS.addActionListener(e -> nanoModeller.showLDOS());
        countStaticProperties.addActionListener(e -> nanoModeller.countStaticProperties());

        timeEvolutionButton.addActionListener(evt -> {
            if (nanoModeller.getDynamicCalculationsThread() != null) {
                nanoModeller.getDynamicCalculationsThread().stop();
                nanoModeller.setDynamicCalculationsThread(null);
                timeEvolutionButton.setImageIcon(new ImageIcon(TIME_EVOLUTION_BUTTON_IMAGE_PATH));
                timeEvolutionButton.setText("Count time evolution");
                nanoModeller.getMenu().applyToAllButton.setIcon(null);
                nanoModeller.getMenu().applyToAllButton.setEnabled(false);
                nanoModeller.getMenu().applyToAllButton.setText("");
                nanoModeller.setStepCount("0");
            } else {
                nanoModeller.getIsCanceled().setValue(false);
                timeEvolutionButton.setImageIcon(new ImageIcon(DELETE_BUTTON_IMAGE_PATH));
                timeEvolutionButton.setText("Cancel");
                Runnable myRunnable = () -> new TimeEvolutionHelper(nanoModeller, nanoModeller.getIsInterupted());
                nanoModeller.setDynamicCalculationsThread(new Thread(myRunnable));
                nanoModeller.getMenu().applyToAllButton.setEnabled(true);
                nanoModeller.getMenu().applyToAllButton.setImageIcon(new ImageIcon(NEXT_STEP_IMAGE_PATH));
                nanoModeller.getMenu().applyToAllButton.setEnabled(true);
                nanoModeller.repaint();
                nanoModeller.getDynamicCalculationsThread().start();
            }

        });
        showLDOSTimeEvolutionButton.addActionListener(evt -> nanoModeller.showLDOSTimeEvolution());
        showCurrentTimeEvolutionButton.addActionListener(evt -> nanoModeller.showCurrentTimeEvolution());
        showTDOSTimeEvolutionButton.addActionListener(evt -> nanoModeller.showTDOSTimeEvolution());
        showAVGDOSTimeEvolutionButton.addActionListener(evt -> nanoModeller.showAVGDOSTimeEvolution());
        showChargeTimeEvolutionButton.addActionListener(evt -> nanoModeller.showNormalisationTimeEvolution());
        showFermiLDOSTimeEvolutionButton.addActionListener(evt -> nanoModeller.showFermiLDOSTimeEvolution());
        showNormalisation.addActionListener(evt -> nanoModeller.showChargeTimeEvolution());
        colorBox.setSelectedItem(Globals.BLACK);
        colorBox.addItemListener(itemEvent -> {
            if (nanoModeller.getHighlightedShape() != null) {
                nanoModeller.getHighlightedShape().setColor(colorBox.getSelectedItem() + "");
            } else if (nanoModeller.getHighlightedElectrode() != null) {
                nanoModeller.getHighlightedElectrode().setColor(colorBox.getSelectedItem() + "");
            } else if (nanoModeller.getHighlightedBound() != null) {
                nanoModeller.getHighlightedBound().setColor(colorBox.getSelectedItem() + "");
            } else {
                nanoModeller.setbColor(colorBox.getSelectedItem() + "");
            }
            nanoModeller.getPaintSurface().repaint();
        });
        countNormalisation.addActionListener(evt -> nanoModeller.showNormalisation());

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

            public void performAction() {
                if (nanoModeller.getHighlightedElectrode() != null) {
                    nanoModeller.getHighlightedElectrode().setdE(dETextField.getText());
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

            public void performAction() {
                if (nanoModeller.getHighlightedShape() != null) {
                    nanoModeller.getHighlightedShape().setEnergy(atomEnergyTextField.getText());
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

            public void performAction() {
                if (nanoModeller.getHighlightedBound() != null) {
                    nanoModeller.getHighlightedBound().setValue(boundVTextField.getText());
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

            public void performAction() {
                nanoModeller.setDt(dTTextField.getText());
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

            public void performAction() {
                String text = electrodeCouplingTextField.getText();
                if (nanoModeller.getHighlightedElectrode() != null) {
                    nanoModeller.getHighlightedElectrode().setCoupling(text);
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

            public void performAction() {
                String text = surfaceCouplingTextField.getText();
                nanoModeller.setSurfaceCoupling(text);
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

            public void performAction() {
                String text = spinOrbitTextField.getText();
                if (nanoModeller.getHighlightedBound() != null) {
                    nanoModeller.getHighlightedBound().setSpinOrbit(text);
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

            public void performAction() {
                String text = spinFlipTextField.getText();
                if (nanoModeller.getHighlightedShape() != null) {
                    nanoModeller.getHighlightedShape().setSpinFlip(text);
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

            public void performAction() {
                String text = energyRangeTextField.getText();
                nanoModeller.setEnergyRange(text);
                nanoModeller.getPaintSurface().repaint();
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

            public void performAction() {
                String text = boundTypeTextField.getText();
                if (nanoModeller.getHighlightedBound() != null) {
                    nanoModeller.getHighlightedBound().setType(text);
                }
                nanoModeller.getPaintSurface().repaint();
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

            public void performAction() {
                String text = electrodeTypeTextField.getText();
                if (nanoModeller.getHighlightedElectrode() != null) {
                    nanoModeller.getHighlightedElectrode().setType(text);
                }
                nanoModeller.getPaintSurface().repaint();
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

            public void performAction() {
                String text = atomTypeTextField.getText();
                if (nanoModeller.getHighlightedShape() != null) {
                    nanoModeller.getHighlightedShape().setType(text);
                }
                nanoModeller.getPaintSurface().repaint();
            }
        });

        atomIDComboBox.addItemListener(itemEvent -> {
            if (nanoModeller.getHighlightedShape() != null) {
                int previousID = nanoModeller.getHighlightedShape().getID();
                int newID = (Integer) atomIDComboBox.getSelectedItem();
                for (AtomShape shape : nanoModeller.getShapes()) {
                    if (shape.getID() == newID) {
                        shape.setID(previousID);
                    }
                }
                nanoModeller.getHighlightedShape().setID(newID);

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

            public void performAction() {
                String text = perturbationTextField.getText();
                if (nanoModeller.getHighlightedBound() != null) {
                    nanoModeller.getHighlightedBound().setPerturbation(text);

                } else if (nanoModeller.getHighlightedElectrode() != null) {
                    nanoModeller.getHighlightedElectrode().setPerturbation(text);
                }
            }
        });
        atomSavingComboBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent itemEvent) {
                if (nanoModeller.getHighlightedShape() != null) {
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
                if (nanoModeller.getHighlightedShape() != null) {
                    nanoModeller.getHighlightedShape().setSaveLDOS(atomSavingComboBox.getLDOS());
                    nanoModeller.getHighlightedShape().setSaveNormalisation(atomSavingComboBox.getNormalisation());
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

            public void performAction() {
                String text = kfaTextField.getText();
                nanoModeller.setkFa(text);
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

            public void performAction() {
                String text = correlationTextField.getText();
                if (nanoModeller.getHighlightedBound() != null) {
                    nanoModeller.getHighlightedBound().setCorrelationCoupling(text);
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

            public void performAction() {
                String text = nZeroTextField.getText();
                if (nanoModeller.getHighlightedShape() != null) {
                    nanoModeller.getHighlightedShape().setnZero(text);
                }
            }
        });
        applyToAllButton.addActionListener(evt -> {
            if (nanoModeller.getHighlightedElectrode() != null) {
                for (ElectrodeShape e : nanoModeller.getElectrodes()) {
                    if (StringUtils.equalsIgnoreNullWhitespace(nanoModeller.getHighlightedElectrode().getType(), e.getType())) {
                        e.setdE(dETextField.getText());
                        e.setCoupling(electrodeCouplingTextField.getText());
                        e.setColor(nanoModeller.getHighlightedElectrode().getColor());
                        e.setPerturbation(nanoModeller.getHighlightedElectrode().getPerturbation());
                    }
                }
                ToastMessage toastMessage = new ToastMessage("Applied to all electrodes", TOAST_MESSAGE_DURATION, nanoModeller);
                toastMessage.setVisible(true);
            } else if (nanoModeller.getHighlightedBound() != null) {
                for (AtomBound b : nanoModeller.getAtomBounds()) {
                    if (StringUtils.equalsIgnoreNullWhitespace(nanoModeller.getHighlightedBound().getType(), b.getType())) {
                        b.setValue(boundVTextField.getText());
                        b.setCorrelationCoupling(correlationTextField.getText());
                        b.setSpinOrbit(spinOrbitTextField.getText());
                        b.setColor(nanoModeller.getHighlightedBound().getColor());
                        b.setPerturbation(nanoModeller.getHighlightedBound().getPerturbation());
                    }
                }
                ToastMessage toastMessage = new ToastMessage("Applied to all bounds", TOAST_MESSAGE_DURATION, nanoModeller);
                toastMessage.setVisible(true);
            } else if (nanoModeller.getHighlightedShape() != null) {
                for (AtomShape shape : nanoModeller.getShapes()) {
                    if (StringUtils.equalsIgnoreNullWhitespace(nanoModeller.getHighlightedShape().getType(), shape.getType())) {
                        shape.setEnergy(atomEnergyTextField.getText());
                        shape.setSpinFlip(spinFlipTextField.getText());
                        shape.setnZero(nZeroTextField.getText());
                        shape.setColor(nanoModeller.getHighlightedShape().getColor());
                        shape.setCorrelation(nanoModeller.getHighlightedShape().getCorrelation());
                        shape.setSaveNormalisation(nanoModeller.getHighlightedShape().isSaveNormalisation());
                        shape.setSaveLDOS(nanoModeller.getHighlightedShape().isSaveLDOS());
                    }
                }
                ToastMessage toastMessage = new ToastMessage("Applied to all atoms", TOAST_MESSAGE_DURATION, nanoModeller);
                toastMessage.setVisible(true);
            } else if (nanoModeller.getDynamicCalculationsThread() != null) {
                nanoModeller.getIsInterupted().neg();
                nanoModeller.repaint();
            }
            nanoModeller.getPaintSurface().repaint();
        });
        setTextFieldsVisibility(NanoModeller.TextFieldType.SURFACE);
    }


    public void setTextFieldsVisibility(NanoModeller.TextFieldType type) {

        spinOrbitTextField.setVisible(NanoModeller.TextFieldType.BOUND.equals(type));
        boundVTextField.setVisible(NanoModeller.TextFieldType.BOUND.equals(type));
        boundTypeTextField.setVisible(NanoModeller.TextFieldType.BOUND.equals(type));
        correlationTextField.setVisible(NanoModeller.TextFieldType.BOUND.equals(type));


        spinFlipTextField.setVisible(NanoModeller.TextFieldType.ATOM.equals(type));
        atomEnergyTextField.setVisible(NanoModeller.TextFieldType.ATOM.equals(type));
        atomTypeTextField.setVisible(NanoModeller.TextFieldType.ATOM.equals(type));
        nZeroTextField.setVisible(NanoModeller.TextFieldType.ATOM.equals(type));
        atomSavingComboBox.setVisible(NanoModeller.TextFieldType.ATOM.equals(type));
        atomDataSavingLabel.setVisible(NanoModeller.TextFieldType.ATOM.equals(type));

        dETextField.setVisible(NanoModeller.TextFieldType.ELECTRODE.equals(type));
        electrodeCouplingTextField.setVisible(NanoModeller.TextFieldType.ELECTRODE.equals(type));
        electrodeTypeTextField.setVisible(NanoModeller.TextFieldType.ELECTRODE.equals(type));
        electrodeTextField.setVisible(NanoModeller.TextFieldType.ELECTRODE.equals(type));

        surfaceCouplingTextField.setVisible(NanoModeller.TextFieldType.SURFACE.equals(type));
        dTTextField.setVisible(NanoModeller.TextFieldType.SURFACE.equals(type));
        energyRangeTextField.setVisible(NanoModeller.TextFieldType.SURFACE.equals(type));
        kfaTextField.setVisible(NanoModeller.TextFieldType.SURFACE.equals(type));


    }
}
