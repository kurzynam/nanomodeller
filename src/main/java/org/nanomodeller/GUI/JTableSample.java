package org.nanomodeller.GUI;

import org.nanomodeller.GUI.ViewComponents.MyButton;
import org.nanomodeller.Tools.StringUtils;
import org.nanomodeller.XMLMappingFiles.GlobalProperties;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

public class JTableSample
{
    private JFrame mainFrame ;
    private JTable table;
    private TableColumnModel tableColumnModel;
    private JPanel panel;
    private String[] columnNamesArr;
    private ArrayList<String> columnNamesList;
    private JScrollPane scrollPane;
    private String[][] data;
    private DefaultTableModel defaultTableModel;
    private JButton addButton;
    private JButton deleteButton;
    private JButton saveButton;
    private JButton cancelButton;
    private JPanel panelButton;
    private GlobalProperties gp;

    private void addRow(Vector rowData) {
        defaultTableModel.addRow(rowData);
        table.validate();
    }

    public JTableSample(GlobalProperties gp)
    {
        mainFrame = new JFrame("JTableSample");

        mainFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.gp = gp;

        columnNamesList = new ArrayList<String>();
        columnNamesList.add("Variable");
        columnNamesList.add("Value");

        data = null;

        columnNamesArr = new String[columnNamesList.size()];
        for(int i=0;i<columnNamesList.size();i++)
        {
            columnNamesArr[i] = columnNamesList.get(i);
        }

        defaultTableModel = new DefaultTableModel(data, columnNamesArr);

        table = new JTable(defaultTableModel);
        table.setFont(new Font("Consolas", Font.BOLD, 22));
        table.getTableHeader().setFont(new Font("Consolas", Font.BOLD, 22));

        table.setRowHeight(30);
        tableColumnModel = table.getColumnModel();

        for(int i=0;i<columnNamesList.size();i++)
        {
            tableColumnModel.getColumn(i).setPreferredWidth(columnNamesList.get(i).length());
        }
        table.setPreferredScrollableViewportSize(table.getPreferredSize());
        scrollPane = new JScrollPane(table);



        addButton = new MyButton("Add");
        deleteButton = new MyButton("Delete");
        cancelButton = new MyButton("Cancel");
        saveButton = new MyButton("Save");

        addButton.addActionListener(e -> addRow(null));

        deleteButton.addActionListener(e -> {
            defaultTableModel.removeRow(table.getSelectedRow());
            table.validate();
        });

        saveButton.addActionListener(e -> {
            gp.getUserDefinedVariables().clear();

            for (int i = 0; i < table.getRowCount(); i++){
                String key = table.getValueAt(i,0) + "";
                Double value = Double.parseDouble(table.getValueAt(i,1) + "");
                if (StringUtils.isNotEmpty(key) && StringUtils.isNotEmpty(table.getValueAt(i,1) + "")){
                    gp.getUserDefinedVariables().put(key, value);
                }
            }
            this.mainFrame.dispose();
        });
        panel = new JPanel();

        panelButton = new JPanel();
        panelButton.add(addButton);
        panelButton.add(deleteButton);
        panelButton.add(saveButton);
        panelButton.add(cancelButton);

        mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        mainFrame.add(scrollPane, BorderLayout.CENTER);
        mainFrame.add(panel, BorderLayout.NORTH);
        mainFrame.add(panelButton,BorderLayout.SOUTH);
        mainFrame.pack();
        mainFrame.setVisible(true);
        mainFrame.setResizable(false);
        mainFrame.setSize(750,400);
        readData();
    }

    private void readData() {
        Iterator it = gp.getUserDefinedVariables().entrySet().iterator();
        while (it.hasNext()) {
            Vector v = new Vector();
            Map.Entry pair = (Map.Entry)it.next();
            v.add(pair.getKey());
            v.add(pair.getValue());
            addRow(v);
        }
    }
}