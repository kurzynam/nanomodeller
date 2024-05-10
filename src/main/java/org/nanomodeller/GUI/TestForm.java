package org.nanomodeller.GUI;


import org.nanomodeller.Globals;
import org.nanomodeller.XMLMappingFiles.GlobalChainProperties;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import static org.nanomodeller.Globals.XML_FILE_PATH;
import static org.nanomodeller.XMLMappingFiles.XMLHelper.convertObjectToXML;
import static org.nanomodeller.XMLMappingFiles.XMLHelper.readParametersFromXMLFile;
import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;

public class TestForm {
    private JPanel MainPanel;
    private JLabel everyTLabel;
    private JTextField plotEveryNthTimeTextField;
    private JTextField plotEveryNthEnergyTextField;
    private JLabel everyELabel;
    private JLabel xRangeLabel;
    private JLabel yRangeLabel;
    private JLabel zRangeLabel;
    private JTextField xRangeTextField;
    private JTextField yRangeTextField;
    private JTextField zRangeTextField;
    private JLabel verticalOrientationLabel;
    private JTextField verticalOrientationTextField;
    private JTextField horizontalOrientationTextField;
    private JLabel horizontalOrientationLabel;
    private JPanel plotOptionsPanel;
    private JLabel gridOptionsLabel;
    private JLabel dataSaveEveryTLabel;
    private JTextField dataSaveEveryTTextField;
    private JLabel dataSaveEveryELabel;
    private JTextField dataSaveEveryETextField;
    private JPanel dataSavingPanel;
    private JComboBox multiplotTypeComboBox;
    private JComboBox plotOrderComboBox;
    private JTextField numberOfColumnsTextField;
    private JLabel multiplotStyleLabel;
    private JLabel numberOfColumnsLabel;
    private JLabel orderOfPlotsLabel;
    private JButton saveButton;
    private JLabel offsetLabel;
    private JTextField offsetTextField;
    private JButton cancelButton;
    private JLabel fontLabel;
    private JComboBox fontComboBox;
    private JLabel fontSizeLabel;
    private JTextField fontSizeTextField;
    private JLabel marginLabel;
    private JTextField marginTextField;
    private JTextField xTicsOffsetTextField;
    private JTextField yTicsOffsetTextField;
    private JLabel yTicsOffsetLabel;
    private JLabel xTicsOffsetLabel;
    private JTextField numberOfRowsTextField;
    private JLabel numberOfRowsLabel;
    private JCheckBox showGridCheckBox;
    private JButton colorBoxButton;
    private JLabel plotColorsLabel;
    private JPanel gridOptionsPanel;
    private JLabel textLabel;
    private JPanel textPanel;
    private JPanel axisPanel;
    private JLabel axisLabel;
    private JPanel threeDPanel;
    private JLabel threeDLabel;
    private JLabel multiplotLabel;
    private JPanel multiplotPanel;
    private JPanel plotOptionsPane;
    private JLabel plotOptionsLabell;
    private JLabel dataSavingLabel;
    private JPanel dataSavingJPanel;
    private JLabel customGPLabel;
    private JButton customGPButton;
    private JLabel crossectionELabel;
    private JTextField crossectionETF;
    private JLabel udvLabel;
    private JButton udvButton;
    private JLabel xticsStepLabel;
    private JLabel yticsStepLabel;
    private JLabel zticsStepLabel;
    private JTextField zticsStepTextField;
    private JTextField yticsStepTextField;
    private JTextField xticsStepTextField;
    private JLabel timeSavingStartLabel;
    private JTextField timeSavingStartTextField;
    private GlobalChainProperties gp;

    public JPanel getMainPanel() {
        return MainPanel;
    }

    public TestForm(){
        initializeComponents();
        JFrame frame = new JFrame();
        frame.setContentPane(getMainPanel());
        frame.setVisible(true);
        double height = getMainPanel().getPreferredSize().getHeight() * 1.05;
        double width = getMainPanel().getPreferredSize().getWidth() * 1.05;
        frame.setSize((int)width,(int)height);
        frame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        frame.setResizable(false);
        saveButton.addActionListener((ActionEvent event) -> {
            gp.setEveryE(Integer.parseInt(plotEveryNthEnergyTextField.getText()));
            gp.setEveryT(Integer.parseInt(plotEveryNthTimeTextField.getText()));
            gp.setVerticalView(Integer.parseInt(verticalOrientationTextField.getText()));
            gp.setHorizontalView(Integer.parseInt(horizontalOrientationTextField.getText()));
            gp.setTimeDependentWriteEveryE(Integer.parseInt(dataSaveEveryETextField.getText()));
            gp.setTimeDependentWriteEveryT(Integer.parseInt(dataSaveEveryTTextField.getText()));
            gp.setxRange(xRangeTextField.getText());
            gp.setyRange(yRangeTextField.getText());
            gp.setzRange(zRangeTextField.getText());
            gp.setMultiplotRows(Integer.parseInt(numberOfRowsTextField.getText()));
            gp.setMultiplotCols(Integer.parseInt(numberOfColumnsTextField.getText()));
            gp.setRowsFirst(plotOrderComboBox.getSelectedItem().toString());
            gp.setOffsetStep(Double.parseDouble(offsetTextField.getText()));
            gp.setTextSize(Double.parseDouble(fontSizeTextField.getText()));
            gp.setFont(fontComboBox.getSelectedItem().toString());
            gp.setMultiplotStyle(multiplotTypeComboBox.getSelectedItem().toString());
            gp.setMargin(Double.parseDouble(marginTextField.getText()));
            gp.setxTicsOffset(Double.parseDouble(xTicsOffsetTextField.getText()));
            gp.setyTicsOffset(Double.parseDouble(yTicsOffsetTextField.getText()));
            gp.setShowGrid(showGridCheckBox.isSelected());
            gp.setCrossSectionEnergy(crossectionETF.getText());
            gp.setXticsStep(xticsStepTextField.getText());
            gp.setYticsStep(yticsStepTextField.getText());
            gp.setZticsStep(zticsStepTextField.getText());
            gp.setSaveTimeFrom(timeSavingStartTextField.getText());
            convertObjectToXML(gp);
            frame.dispose();
        });
        cancelButton.addActionListener((ActionEvent event) -> {
            frame.dispose();
        });
        multiplotTypeComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                setMultiplotFieldsVisibility();
            }
        });
        setMultiplotFieldsVisibility();
        numberOfRowsTextField.setDisabledTextColor(new Color(0xFFD53A));
        numberOfColumnsTextField.setDisabledTextColor(Color.DARK_GRAY);
        colorBoxButton.addActionListener(e -> new PaletteChooserFrame(gp));
        udvButton.addActionListener(e -> new JTableSample(gp));
        customGPButton.addActionListener(e -> showTextArea());
    }
    private void showTextArea(){
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = (int)screenSize.getWidth();
        int height = (int)screenSize.getHeight();
        JTextArea tf = new JTextArea(gp.getCustomGnuplotCommands());
        tf.setAutoscrolls(true);
        tf.setFont(new Font("Consolas", Font.PLAIN, 20));
        //tf.setPreferredSize(new Dimension(width/3,height/4));
        tf.setLineWrap(true);

        JScrollPane sp = new JScrollPane(tf);
        sp.setPreferredSize(new Dimension(width/3,height/4));
        sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        int result = JOptionPane.showConfirmDialog(
                null, sp, "Input",
                JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            gp.setCustomGnuplotCommands(tf.getText());
        }
    }

    private void setMultiplotFieldsVisibility() {
        Object item = multiplotTypeComboBox.getSelectedItem();
        boolean editable = Globals.MULTIPLOT.equals(item);
        numberOfColumnsTextField.setEditable(editable);
        numberOfRowsTextField.setEditable(editable);
    }

    private void initializeComponents(){

        gp = readParametersFromXMLFile(XML_FILE_PATH);
        plotOrderComboBox.setSelectedItem("" + gp.getRowsFirst());
        fontComboBox.setSelectedItem("" + gp.getFont());
        multiplotTypeComboBox.setSelectedItem("" + gp.getMultiplotStyle());
        fontSizeTextField.setText(gp.getTextSize() + "");
        marginTextField.setText(gp.getMargin() + "");
        xTicsOffsetTextField.setText(gp.getxTicsOffset() + "");
        yTicsOffsetTextField.setText(gp.getyTicsOffset() + "");
        numberOfColumnsTextField.setText("" + gp.getMultiplotCols());
        numberOfRowsTextField.setText("" + gp.getMultiplotRows());
        verticalOrientationTextField.setText("" + gp.getVerticalView());
        horizontalOrientationTextField.setText("" + gp.getHorizontalView());
        plotEveryNthTimeTextField.setText("" + gp.getEveryT());
        plotEveryNthEnergyTextField.setText("" + gp.getEveryE());
        xRangeTextField.setText(gp.getxRange());
        yRangeTextField.setText(gp.getyRange());
        zRangeTextField.setText(gp.getzRange());
        dataSaveEveryTTextField.setText("" + gp.getTimeDependentWriteEveryT());
        dataSaveEveryETextField.setText("" + gp.getTimeDependentWriteEveryE());
        offsetTextField.setText(gp.getOffsetStep()+"");
        showGridCheckBox.setSelected(gp.isShowGrid());
        crossectionETF.setText(gp.getCrossSectionEnergy());
        xticsStepTextField.setText(gp.getXticsStep());
        yticsStepTextField.setText(gp.getYticsStep());
        zticsStepTextField.setText(gp.getZticsStep());
        timeSavingStartTextField.setText(gp.getSaveTimeFrom());
    }
}
