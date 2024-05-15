package org.nanomodeller.GUI.ViewComponents;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;

public class FuturisticScrollBar extends JScrollBar {

    public FuturisticScrollBar() {
        setUI(new FuturisticScrollBarUI());
    }

    class FuturisticScrollBarUI extends BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            this.thumbColor = new Color(30, 30, 30);
            this.thumbHighlightColor = new Color(60, 60, 60);
            this.thumbDarkShadowColor = new Color(0, 0, 0);
            this.thumbLightShadowColor = new Color(90, 90, 90);
            this.trackColor = new Color(15, 15, 15);
            this.trackHighlightColor = new Color(45, 45, 45);
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createZeroButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createZeroButton();
        }

        private JButton createZeroButton() {
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(0, 0));
            button.setMinimumSize(new Dimension(0, 0));
            button.setMaximumSize(new Dimension(0, 0));
            return button;
        }
    }
}
