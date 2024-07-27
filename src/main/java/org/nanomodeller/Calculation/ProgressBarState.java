package org.nanomodeller.Calculation;

import javax.swing.*;

public class ProgressBarState {

    public static void updateProgressBar(Double n, String name, double width, JProgressBar bar) {
        int percentageSec = (int) (100 * n / width);
        if (percentageSec % 2 == 0){
            bar.setValue(percentageSec);
            bar.setString(name + ": " + percentageSec + "%");
        }
    }

}
