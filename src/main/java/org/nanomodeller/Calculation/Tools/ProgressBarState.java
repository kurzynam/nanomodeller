package org.nanomodeller.Calculation.Tools;

import javax.swing.*;

public class ProgressBarState {

    public static void updateProgressBar(float n, String name, float width, JProgressBar bar) {
        int percentageSec = (int) (100 * n / width);
        if (percentageSec % 2 == 0){
            bar.setValue(percentageSec);
            bar.setString(name + ": " + percentageSec + "%");
        }
    }

}
