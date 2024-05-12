package org.nanomodeller.GUI.ViewComponents;

import org.nanomodeller.GUI.NanoModeller;
import org.nanomodeller.GUI.LeftMenuPanel;
import org.nanomodeller.Tools.DataAccessTools.FileOperationHelper;
import org.nanomodeller.Tools.StringUtils;
import org.nanomodeller.XMLMappingFiles.GlobalProperties;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import javax.swing.*;
import javax.swing.tree.*;

import static org.nanomodeller.XMLMappingFiles.XMLHelper.convertObjectToXML;
import static org.nanomodeller.XMLMappingFiles.XMLHelper.readPropertiesFromXMLFile;


public class FileBrowser extends JPanel{

    private String currentNodeName;

    public boolean isNodeChanged(String name){
        return StringUtils.isNotEmpty(currentNodeName) && !currentNodeName.equals(name);
    }
    private LeftMenuPanel recorder;
    private DefaultMutableTreeNode root;

    private DefaultMutableTreeNode assignedNode;
    private DefaultTreeModel treeModel;
    private JTree tree;

    public ArrayList<DefaultMutableTreeNode> getNodes() {
        return nodes;
    }

    private ArrayList<DefaultMutableTreeNode> nodes;

    public DefaultMutableTreeNode getAssignedNode() {
        return assignedNode;
    }

    public JTree getTree() {
        return tree;
    }

    public String getCurrentPath(){
        return getNodes().get(getSelectedFiles()[0]).getPath()[0].toString();
    }
    public void changeRootNode(String path){
        FileNode fileRoot = new FileNode(path);
        DefaultMutableTreeNode newRoot = new DefaultMutableTreeNode(fileRoot);
        treeModel.setRoot(newRoot);
        CreateChildNodes ccn =
                new CreateChildNodes(fileRoot, newRoot);
        treeModel.reload();
        root = newRoot;
        TreePath treePath = new TreePath(root.getPath());
        tree.setSelectionPath(treePath);
    }
    public int[] getSelectedFiles(){
       return tree.getSelectionRows();
    }
    public FileBrowser(LeftMenuPanel recorder){

        this.recorder = recorder;
        MouseAdapter ma = new MouseAdapter() {
            private void myPopupEvent(MouseEvent e) {
                if(!getSelectedFileNode().isHidden()) {
                    PopUpMenu menu = new PopUpMenu();
                    menu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
            public void mousePressed(MouseEvent e) {
                String path = ((JTree) e.getSource()).getAnchorSelectionPath().getLastPathComponent().toString();
                if (isNodeChanged(path)){
                    readPropertiesFromXMLFile(getAbsolutePath() + "/parameters.xml");
                    NanoModeller.getInstance().readDataFromObject( true, NanoModeller.getInstance().getStepRecorder().timeTextField);;
                    NanoModeller.getInstance().refresh();
                }
                currentNodeName = path;
            }
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) myPopupEvent(e);
            }
        };
        GlobalProperties gp = GlobalProperties.getInstance();
        FileNode fileRoot = new FileNode(gp.getDynamicPATH());
        root = new DefaultMutableTreeNode(fileRoot);
        nodes = new ArrayList<>();
        nodes.add(root);
        treeModel = new DefaultTreeModel(root);
        tree = new JTree(treeModel);
        tree.addMouseListener(ma);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.
                DISCONTIGUOUS_TREE_SELECTION);
        tree.setShowsRootHandles(true);
        tree.setFont(new Font("Consolas", Font.PLAIN, 22));
        tree.setCellRenderer(new MyTreeCellRenderer());
        JScrollPane scrollPane = new JScrollPane(tree);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = (int)screenSize.getWidth();
        int height = (int)screenSize.getHeight();
        scrollPane.setPreferredSize(new Dimension(width/8, height/5));
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        add(scrollPane);
        setMinimumSize(new Dimension(width/11, height/5));
        setVisible(true);
        CreateChildNodes ccn =
                new CreateChildNodes(fileRoot, root);
        TreePath path = new TreePath(root.getPath());
        tree.setSelectionPath(path);
    }

    class MyTreeCellRenderer implements TreeCellRenderer {

        private JLabel label;

        MyTreeCellRenderer() {
            label = new JLabel();
        }

        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
                                                      boolean leaf, int row, boolean hasFocus) {

            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;

            Object o = node.getUserObject();
            if (o instanceof FileNode) {
                FileNode fnode = (FileNode) o;
                String imgPATH = "img/dirIcon.png";
                if (fnode.isHidden()){
                    if (tree.isPathSelected(new TreePath(node.getPath()))) {
                        imgPATH = "img/nextIconSelected.png";
                    }else {
                        imgPATH = "img/nextIcon.png" ;
                    }
                }
                else{
                    if (node.equals(assignedNode) && tree.isPathSelected(new TreePath(node.getPath()))) {
                        imgPATH = "img/assignedSelectedDirIcon.png";
                    } else if (node.equals(assignedNode)) {
                        imgPATH = "img/assignedDirIcon.png";
                    } else if (tree.isPathSelected(new TreePath(node.getPath()))) {
                        imgPATH = "img/selDirIcon.png";
                    }
                }
                Image img = new ImageIcon(imgPATH).getImage() ;
                Image newimg = img.getScaledInstance( 30, 25,  java.awt.Image.SCALE_SMOOTH );
                ImageIcon imIc = new ImageIcon(newimg);

                label.setIcon(imIc);
                label.setText(fnode.toString());
            } else {
                label.setIcon(null);
            }
            return label;
        }
    }
    private class PopUpMenu extends JPopupMenu {

        private static final long serialVersionUID = 1L;
        JMenuItem newDirItem;
        JMenuItem renameDirItem;
        JMenuItem removeDirItem;
        public PopUpMenu(){
            newDirItem = new MyMenuItem("New Directory","img/addDirIcon.png", 30, 25);
            renameDirItem = new MyMenuItem("Rename directory","img/renameDirIcon.png", 30, 25);
            removeDirItem = new MyMenuItem("Remove directory", "img/removeDirIcon.png", 30, 25);

            add(newDirItem);
            add(renameDirItem);
            add(removeDirItem);
            newDirItem.addActionListener(evt -> addNewDir());
            renameDirItem.addActionListener(evt -> renameDirectory());
            removeDirItem.addActionListener(evt -> removeDirectory());
        }
    }

    private void removeDirectory() {

        int n = JOptionPane.showConfirmDialog(
                NanoModeller.getInstance(),
                "Are you sure you want to delete this directory?",
                "Removal Confirmation",
                JOptionPane.YES_NO_OPTION);
        if (n == JOptionPane.YES_OPTION) {
            DefaultMutableTreeNode selectedNode =
                    (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
            if (selectedNode != root) {
                treeModel.removeNodeFromParent(selectedNode);
                File file = null;
                try {
                    file = ((FileNode) selectedNode.getUserObject()).getCanonicalFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                deleteDirectory(file);
                ((FileNode) selectedNode.getUserObject()).delete();
            }
        }
    }
    boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }
    private void renameDirectory() {
        DefaultMutableTreeNode selectedNode =
                (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
        String newDirName = (String)JOptionPane.showInputDialog(
                NanoModeller.getInstance(),
                "New name:",
                "Rename Directory",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                selectedNode.getUserObject().toString());
        if(StringUtils.isEmpty(newDirName)){
            return;
        }
        FileNode newDir = null;
        if(selectedNode.getParent() != null){
            newDir = new FileNode((((FileNode)((DefaultMutableTreeNode)selectedNode.getParent()).getUserObject()).getPath() + "/" + newDirName));
        }
        else{
            File parent = ((FileNode)root.getUserObject()).getParentFile();
            newDir = new FileNode(parent.getPath() + "/" + newDirName);
            GlobalProperties gp = GlobalProperties.getInstance();
            gp.setDynamicPATH(newDir.getAbsolutePath());
            convertObjectToXML(gp);
        }
        DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(newDir);
        FileNode selectedDir = ((FileNode)selectedNode.getUserObject());
        selectedDir.renameTo(newDir);
        selectedNode.setUserObject(newDir);
        tree.repaint();
        treeModel.reload();
        TreePath path = new TreePath(newNode.getPath());
        tree.setSelectionPath(path);
        tree.scrollPathToVisible(path);
    }

    public FileNode getSelectedFileNode(){
        DefaultMutableTreeNode selectedNode =
                (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
        return (FileNode)selectedNode.getUserObject();

    }

    public FileNode getAssignedFileNode(){
        return (FileNode)assignedNode.getUserObject();

    }

    private void addNewDir() {
        String newDirName = (String)JOptionPane.showInputDialog(
                NanoModeller.getInstance(),
                "Directory name:",
                "New Directory",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                getCurrentPath());
        if(StringUtils.isEmpty(newDirName)){
            return;
        }
        TreePath path = createDir(newDirName, false);
        tree.scrollPathToVisible(path);
        treeModel.reload();
    }

    public String getAbsolutePath() {
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        FileNode fileRoot = ((FileNode) selectedNode.getUserObject());
        return fileRoot.getAbsolutePath();
    }

    public String createFile(String path){
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        FileNode fileRoot = ((FileNode)selectedNode.getUserObject());
        String fullPath = fileRoot.getAbsolutePath()+"/"+path;
        if(FileOperationHelper.fileExists(fullPath)){
            return fullPath;
        }
        FileNode newDir = new FileNode(fullPath);
        try {
            newDir.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return fullPath;
    }
    public TreePath createDir(String newDirName, boolean fromAssignedNode){

        DefaultMutableTreeNode selectedNode = null;
        if(fromAssignedNode){
            selectedNode = assignedNode;
        }else {
            selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        }
        if (selectedNode == null){
            selectedNode = root;
        }
        FileNode fileRoot = ((FileNode)selectedNode.getUserObject());
        FileNode newDir = new FileNode((fileRoot.getAbsolutePath()+"/"+newDirName));
        DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(newDir);
        newDir.mkdir();
        selectedNode.add(newNode);
        TreePath path = new TreePath(newNode.getPath());
        tree.setSelectionPath(path);
        if(fromAssignedNode){
            setHiddenAttrib(newDir);
        }
        return path;
    }
    private static void setHiddenAttrib(File file) {
        try {
            Process p = Runtime.getRuntime().exec("attrib +H " + file.getPath());
            p.waitFor();
        } catch (IOException | InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void reload(){
        treeModel.reload();
    }

    public void navigateToPath(String filePATH){
        if (StringUtils.isNotEmpty(filePATH)) {
            DefaultMutableTreeNode node = getNodeByPATH(filePATH);
            if (node != null) {
                TreePath path = new TreePath(node.getPath());
                treeModel.reload();
                tree.setSelectionPath(path);
                tree.scrollPathToVisible(path);
                assignedNode = node;
                return;
            }
        }
        treeModel.reload();
        assignedNode = null;
    }

    public class CreateChildNodes{

        private DefaultMutableTreeNode root;

        private FileNode fileRoot;

        public CreateChildNodes(FileNode fileRoot,
                                DefaultMutableTreeNode root) {
            this.fileRoot = fileRoot;
            this.root = root;
            createChildren(fileRoot, root);
        }

        private void createChildren(FileNode fileRoot,
                                    DefaultMutableTreeNode node) {
            File[] files = fileRoot.listFiles();
            sortByNumber(files);
            if (files == null) return;

            for (File file : files) {
                FileNode fileNode = new FileNode(file);
                if (file.isDirectory()) {
                    DefaultMutableTreeNode childNode =
                            new DefaultMutableTreeNode(fileNode);
                    node.add(childNode);
                    if (file.isDirectory()) {
                        createChildren(fileNode, childNode);
                        nodes.add(childNode);
                    }
                }
            }
        }
        public void sortByNumber(File[] files) {
            if(files.length == 0)
                return;
            Arrays.sort(files, new Comparator<File>() {
                @Override
                public int compare(File o1, File o2) {
                    try {
                        if(o1.isHidden() && o2.isHidden()) {
                            int n1 = Integer.parseInt(o1.getName());
                            int n2 = Integer.parseInt(o2.getName());
                            return n1 - n2;
                        }
                    }catch (java.lang.NumberFormatException e){

                    }
                    return 0;
                }

            });
        }

    }

    public DefaultMutableTreeNode getNodeByPATH(String path){
        for (DefaultMutableTreeNode node : nodes){
            FileNode fn = (FileNode)node.getUserObject();
            String fileNodePATH = fn.getAbsolutePath();
            if (StringUtils.equals(path, fileNodePATH)) {
                return node;
            }
        }
        return null;
    }

    public class FileNode extends File{

        public FileNode(String path){
            super(path);
        }
        public FileNode(File file){
            super(file.getAbsolutePath());
        }
        @Override
        public String toString() {
            String name = this.getName();
            if (name.equals("")) {
                return this.getAbsolutePath();
            } else {
                return name;
            }
        }
    }

}