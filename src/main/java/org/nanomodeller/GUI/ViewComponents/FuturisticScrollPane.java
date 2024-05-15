package org.nanomodeller.GUI.ViewComponents;

import javax.swing.*;
import java.awt.*;

public class FuturisticScrollPane extends JScrollPane {

    public FuturisticScrollPane(Component view) {
        super(view);
        getVerticalScrollBar().setUI(new FuturisticScrollBar().new FuturisticScrollBarUI());
        getHorizontalScrollBar().setUI(new FuturisticScrollBar().new FuturisticScrollBarUI());
    }
}