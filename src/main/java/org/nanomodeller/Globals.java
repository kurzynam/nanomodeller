package org.nanomodeller;

import org.nanomodeller.Tools.StringUtils;

import java.util.Arrays;

public class Globals {

    //region PATHS

    //region file PATHS
    public static final String XML_FILE_PATH = "xml/data.xml";

    public static final String GNUPLOT_PATH = "gnuplot\\bin\\wgnuplot.exe";
    public static final String LDOS_FILE_NAME_PATTERN = "LDOS";
    public static final String LDOS_E_FILE_NAME_PATTERN = "LDOS_E";
    public static final String NORMALISATION_FILE_NAME_PATTERN = "normalisation";
    public static final String STATIC_LDOS_FILE_NAME_PATTERN = "sLDOS.csv";
    public static final String STATIC_NORMALISATION_FILE_NAME_PATTERN = "sCharge3.csv";
    public static final String CHARGE_FILE_NAME_PATTERN = "charge";
    public static final String CURRENT_FILE_NAME_PATTERN = "current";
    public static final String SLDOS_FILE_NAME_PATTERN = "sLDOS";
    public static final String SNORM_FILE_NAME_PATTERN = "sCharge";
    public static final String FERMI_LDOS_FILE_NAME_PATTERN = "fermiLDOS";
    public static final String STATIC_LDOS_GNUPLOT_FILE_PATH = "plots/staticLDOS.plt";
    public static final String STATIC_NORMALISATION_GNUPLOT_FILE_PATH = "plots/normalisation.plt";
    public static final String DYNAMIC_TDOS_GNUPLOT_FILE_PATH = "plots/dynamicTDOS.plt";
    public static final String DYNAMIC_AVGDOS_GNUPLOT_FILE_PATH = "plots/dynamicAVGDOS.plt";
    public static final String DYNAMIC_CHARGE_GNUPLOT_FILE_PATH = "plots/dynamicCharge.plt";
    public static final String DYNAMIC_FERMI_LDOS_GNUPLOT_FILE_PATH = "plots/fermiLDOS.plt";
    public static final String LOG_PATH = "log/log.txt";
    public static final String LAST_T_GNUPLOT_FILE_PATH = "plots/last_T.plt";
    public static final String LAST_T_LDOS_PATTERN = "/lastLDOSTime";
    public static final String LAST_T_NORMALISATION_PATTERN = "/lastNormalisationTime";
    public static final String TXT = ".txt";
    //endregion


    //region dialog texts
    public static final String WARNING_CALCULATE_DYNAMICS = "Calculate LDOS first.";
    public static final String APP_NAME = "NM";
    //endregion


    //region image PATHS
	public static final String ICON_IMAGE_PATH = "img/icon.png";

    public static final String ICON_ATOM_PATH = "img/node.svg";

    public static final String USER_ICON_ATOM_PATH = "img/userNode.svg";

    public static final String ICON_HATOM_PATH = "img/hNode.svg";

    public static final String ICON_ELECTRODE_PATH = "img/el.svg";
    public static final String ICON_HELECTRODE_PATH = "img/hel.svg";
    public static final String ADD_BUTTON_IMAGE_PATH = "img/addIcon.png";
    public static final String ALIGN_BUTTON_IMAGE_PATH = "img/alignIcon.png";
    public static final String DELETE_BUTTON_IMAGE_PATH = "img/deleteIcon.png";
    public static final String ZOOM_IN_BUTTON_IMAGE_PATH = "img/zoomIn.png";
    public static final String ZOOM_OUT_BUTTON_IMAGE_PATH = "img/zoomOut.png";
    public static final String SAVE_BUTTON_IMAGE_PATH = "img/saveImage.png";
    public static final String CLEAR_BUTTON_IMAGE_PATH = "img/clear.png";
    public static final String REFRESH_BUTTON_IMAGE_PATH = "img/refresh.png";
    public static final String APPLY_E_ALL_BUTTON_IMAGE_PATH = "img/applyEnergyToAllButton.png";
    public static final String APPLY_E_ALL_ELECTRODES_BUTTON_IMAGE_PATH = "img/applyEnergyToAllElectrodesButton.png";
    public static final String APPLY_V_ALL_BUTTON_IMAGE_PATH = "img/applyPotentialToAll.png";
    public static final String COUNT_LDOS_BUTTON_IMAGE_PATH = "img/countLDOSImage.png";
    public static final String NORMALISATION_BUTTON_IMAGE_PATH = "img/normalisation.png";
    public static final String LOGO_IMAGE_PATH = "img/logo.png";
    public static final String LDOS_BUTTON_IMAGE_PATH = "img/LDOSIImage.png";
    public static final String TIME_EVOLUTION_BUTTON_IMAGE_PATH = "img/calculateTime.png";
    public static final String SHOW_LDOS_TIME_EVOLUTION_BUTTON_IMAGE_PATH = "img/timeIcon.png";
    public static final String SHOW_CURRENT_TIME_EVOLUTION_BUTTON_IMAGE_PATH = "img/timeIcon.png";
    public static final String SHOW_NORMALISATION_TIME_EVOLUTION_BUTTON_IMAGE_PATH = "img/timeIcon.png";
    public static final String BACKGROUND_IMAGE_PATH = "img/pattern.jpg";
    public static final String LDOS_LAST_T_IMAGE_PATH = "img/lastTimeLDOS.jpg";
    public static final String NORMALISATION_LAST_T_IMAGE_PATH = "img/lastTimeNormalisation.jpg";
    public static final String NEXT_STEP_IMAGE_PATH = "img/nextStep.png";
    //endregion
    public static final String MULTIPLOT = "Each graph on separate plot";
    //endregion


    //region colors
    public static final String WHITE = "WHITE";
    public static final String BLUE = "BLUE";
    public static final String GREEN = "GREEN";
    public static final String YELLOW = "YELLOW";
    public static final String DARK_YELLOW = "DARK YELLOW";
    public static final String ORANGE = "ORANGE";
    public static final String CYAN = "CYAN";
    public static final String DARK_GRAY = "DARK GRAY";
    public static final String GRAY = "GRAY";
    public static final String RED = "RED";
    public static final String PINK = "PINK";
    public static final String MAGENTA = "MAGENTA";
    public static final String BLACK = "BLACK";
    //endregion
    //region constants
    public static final double ETA = 0.01;
    public static final int TOAST_MESSAGE_DURATION = 1500;
    public static final int SURFACE_ELECTRODE_ID = -1;
    //endregion


    //region operators
    public static final String AiS = "a";//  a†iσ
    public static final String aiS = "A";//  aiσ
    public static final String AjS = "b";//  a†jσ
    public static final String ajS = "B";//  ajσ
    public static final String AlS = "c";//  a†lσ
    public static final String alS = "C";//  alσ
    public static final String AmS = "d";//  a†mσ
    public static final String amS = "D";//  amσ


    public static final String Ais = "e";//  a†i-σ
    public static final String ais = "E";//  ai-σ
    public static final String Ajs = "f";//  a†j-σ
    public static final String ajs = "F";//  aj-σ
    public static final String Als = "g";//  a†l-σ
    public static final String als = "G";//  al-σ
    public static final String Ams = "h";//  a†m-σ
    public static final String ams = "H";//  am-σ


    public static final String AkS = "k";//  a†kσ
    public static final String akS = "elypticFunction";//  akσ
    public static final String AqS = "l";//  a†k'σ
    public static final String aqS = "L";//  ak'σ
    public static final String ArS = "m";//  a†k''σ
    public static final String arS = "M";//  ak''σ
    public static final String AwS = "n";//  a†k'''σ
    public static final String awS = "N";//  ak'''σ


    public static final String Aks = "o";//  a†k-σ
    public static final String aks = "O";//  ak-σ
    public static final String Aqs = "p";//  a†k'-σ
    public static final String aqs = "P";//  ak'-σ
    public static final String Ars = "r";//  a†k''-σ
    public static final String ars = "R";//  ak''-σ
    public static final String Aws = "s";//  a†k'''-σ
    public static final String aws = "S";//  ak'''-σ
    public static final String INITIAL_OCCUPATION = "InitialOccupation";

    public static boolean isTrue(String value) {
        String[] vals = {"yes", "true", "1", "tak", "YES", "TRUE", "y", "t", "Y", "T"};
        return Arrays.stream(vals).anyMatch(val -> StringUtils.equals(val, value));
    }
    //endregion
}