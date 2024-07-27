package org.nanomodeller.Calculation;

import org.nanomodeller.GUI.NanoModeler;
import org.nanomodeller.Tools.Flag;
import org.nanomodeller.Tools.DataAccessTools.MyFileWriter;
import org.nanomodeller.Tools.StringUtils;
import org.nanomodeller.XMLMappingFiles.*;
import org.jscience.mathematics.number.Complex;
import org.nfunk.jep.JEP;
import java.util.*;

import static org.nanomodeller.Calculation.CalculationItem.applyTimeForItemsCalculation;
import static org.nanomodeller.Calculation.JEPHelper.createJEP;
import static org.nanomodeller.Calculation.ProgressBarState.updateProgressBar;
import static org.nanomodeller.CommonPhysics.toEnergy;
import static org.nanomodeller.Globals.*;

import static java.lang.Math.PI;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import static org.jscience.mathematics.number.Complex.ZERO;
import static org.jscience.mathematics.number.Complex.valueOf;
import static org.nanomodeller.XMLMappingFiles.Atom.initializeCalculationAtoms;
import static org.nanomodeller.XMLMappingFiles.Bond.initializeCalculationBonds;
import static org.nanomodeller.XMLMappingFiles.Electrode.initializeCalculationElectrodes;

public class TimeEvolutionHelper {

    public static final String CORRELATION_COUPLING = "CorrelationCoupling";
    public static final String SPIN_FLIP = "SpinFlip";
    public static final String DE = "dE";
    public static final String COUPLING = "Coupling";
    public static final String PERTURBATION_COUPLING = "PerturbationCoupling";
    //region public members
    public CommonProperties gp;

    public String format;
    public Parameters par = Parameters.getInstance();
    public NanoModeler modeler = NanoModeler.getInstance();
    public Complex[][][] surfaceUt_k;
    public double[] sumOfCharges;
    private int tmax = 1;

    private Hashtable<Integer, Hashtable<Integer, CalculationBond>> calculationBonds = new Hashtable<>();

    private Hashtable<Integer, CalculationAtom> calculationAtoms = new Hashtable<>();


    public double[] Ei;
//    public Hashtable<Integer,Double> n;
//    public Hashtable<Integer,Double> n0;
    public ArrayList<Complex [/*t*/][/*k*/][/*k_sigma*/][/*n*/][/*sigma_n*/]> Ut_ik;
    public Flag isInterupted;

    public double dE;
    public double Emin;
    public double Emax;
    public int numberOfEnergySteps;
    public int timeDigits;
    public int energyDigits;
    public int everyE;
    public int everyT;
    public double starTimeFrom;
    public int numOfAtoms;
    public int numOfElectrodes;
    public int numOfTimeSteps;
    public int electrode_id;
    public double ldosE;

    public double[] D;

    public Hashtable<Integer, Double> n = new Hashtable<>();
    public Complex[][][] Ut_ij;

    public Hashtable<Integer, CalculationElectrode> calculationElectrodes = new Hashtable<>();
    //public Electrode[] electrodes;
    //protected Atom[] atoms;
    boolean isSpinOrbit = false;
    JEP parser;
    //endregion

    //region initialization
    public void initialize() {

        boolean arraysInitialized = false;
        parser = createJEP();
        initializeCalculationBonds(parser, par.getBonds(), calculationBonds);
        initializeCalculationAtoms(parser, par.getAtoms(), calculationAtoms);
        initializeCalculationElectrodes(parser, par.getElectrodes(), calculationElectrodes, calculationAtoms);

        gp = CommonProperties.getInstance();

        double electrodesWidth = gp.getWidth("E");
        double dt = gp.getInc("t");
        Complex Ek = Complex.ONE;
        double constant = dE / electrodesWidth;
        Complex[][] integralEnergy = null;
        MyFileWriter ldosList;
        MyFileWriter chargeList;
        MyFileWriter currentList;
        MyFileWriter ldosEList;
        Parameters par = Parameters.getInstance();

        if (arraysInitialized && StringUtils.isEmpty(par.getPath())){
            readData();
        }
        else {
            readData(true);
            integralEnergy = new Complex[par.getAtoms().size()][2];
            for (int i = 0; i < 2; i++) {
                for (int comp = 0; comp < integralEnergy.length; comp++) {
                    integralEnergy[comp][i] = Complex.ONE;
                }
            }
        }
        String dynamicPATH = par.getPath();
        chargeList = new MyFileWriter(dynamicPATH + "/" + CHARGE_FILE_NAME_PATTERN + ".csv");
        currentList  = new MyFileWriter(dynamicPATH + "/" + CURRENT_FILE_NAME_PATTERN + ".csv");
        ldosList = new MyFileWriter(dynamicPATH + "/" + LDOS_FILE_NAME_PATTERN + ".csv");
        ldosEList = new MyFileWriter(dynamicPATH + "/" + LDOS_E_FILE_NAME_PATTERN + ".csv");
        ldosList.printf("Time, Energy");
        chargeList.printf("Time");
        currentList.printf("Time");
        for(int p = 0; p < numOfAtoms; p++){
            ldosList.printf(", LDOS%d", p);
            chargeList.printf(", Charge %d", p);
            currentList.printf(", Current %d", p);
        }
        ldosList.println();
        chargeList.println();
        currentList.println();
        ldosEList.println();


        MyFileWriter sumldosF = new MyFileWriter(dynamicPATH + "/sumLDOSF.txt");
        MyFileWriter TDOSWriter = new MyFileWriter(dynamicPATH + "/TDOS.txt");
        for (Integer i : calculationAtoms.keySet()){
            Double n0 = calculationAtoms.get(i).get(INITIAL_OCCUPATION);
            this.n.put(i, n0);
            Double sf = calculationAtoms.get(i).get(SPIN_FLIP);
            isSpinOrbit |= sf > 0 ;
        }
        double[] charges = new double[calculationAtoms.size()];
        for (int i = 0; i < calculationAtoms.size(); i++) {
            charges[i] = 0;
        }
        int time = 0;
        for (double t : gp.getVar("t")) {
            updateProgressBar(t - gp.getVar("t").getMin(), "n", gp.getVar("t").getWidth(), NanoModeler.getInstance().getMenu().getSecondPB());
            int T = time % 2;

            double[] TDOStemp = new double[numberOfEnergySteps/everyE];
            parser.addVariable("t", t);

            applyTimeForItemsCalculation(parser, par.getAtoms(), calculationAtoms);
            applyTimeForItemsCalculation(parser, par.getElectrodes(), calculationElectrodes);
            applyTimeForItemsCalculation(parser, par.getBonds(), calculationBonds);

            countUt_ij(time);

            for (CalculationElectrode electrode : calculationElectrodes.values()) {
                electrode_id = electrode.getID();
                countUt_ik(electrode, Ut_ik.get(electrode_id), time, T, dt, t-dt,
                        electrodesWidth, integralEnergy);

            }

            if (par.isSurfacePresent()) {
                //Electrode surfaceElectrode = new Electrode(-1, null, null, par.getSurfaceCoupling(), gp.getInc("E"), Globals.SURFACE_ELECTRODE_ID, null);
                //countUt_ik(surfaceElectrode, surfaceUt_k, t);
            }
            double charge;
            String chargesList = "";
            String currentsList = "";
            String ldosesEList = "";
            for (CalculationAtom atom : calculationAtoms.values()) {
                charge = countCharge(atom.getID(), time, dt);
                double current = (charge-charges[atom.getID()])/dt;
                currentsList += current;
                chargesList += charge;
                ldosesEList += ldosE;
                if (atom.getID() != calculationAtoms.size() - 1){
                    chargesList += ",";
                    currentsList += ",";
                    ldosesEList += ",";
                }
            }
            time++;
            chargeList.printf("%3f,%s\n",  t, chargesList);
            currentList.printf("%3f,%s\n", t, currentsList);
            ldosEList.printf("%3f,%s\n", t, ldosEList);
            int sigmaDim = 1;
            if (isSpinOrbit){
                sigmaDim = 2;
            }
            ArrayList<String> ldosArray = new ArrayList<>();
            for (int e = 0; e < numberOfEnergySteps; e++) {
                if (e % everyE != 0) {
                    continue;
                }
                ldosArray.add(String.format("%.3f,%.3f", t, toEnergy(e, gp)));
            }
            for (CalculationAtom a : calculationAtoms.values()) {
                int i = a.getID();
                for (int sigma = 0; sigma < sigmaDim; sigma++) {
                    countDynamicParameters(i, sigma, time, dt, constant,
                            ldosArray,
                            TDOStemp, integralEnergy);
                }
            }
            //if (time >= starTimeFrom && t % everyT == 0){
                for (int e = 0; e < ldosArray.size(); e++) {
                    ldosList.println(ldosArray.get(e));
                }
                ldosList.println();
            }
        //}
        TDOSWriter.close();
        sumldosF.close();
        if (isInterupted.getValue()) {
            isInterupted.neg();
        } else {
            tmax += numOfTimeSteps;
        }
        chargeList.println();
        currentList.println();
        ldosEList.println();
        ldosList.println();

        ldosList.close();
        chargeList.close();
        currentList.close();
        ldosEList.close();
        NanoModeler.getInstance().getMenu().clearBars();
    }

    //endregion

    //region data reading
    public void readData(){
        readData(false);
    }
    public void readData(boolean initializeMatrices){
        this.par = Parameters.getInstance();
        this.numOfElectrodes = par.getElectrodes().size();
        this.numOfAtoms = par.getAtoms().size();
        this.timeDigits = (gp.getInc("t") + "").length() - ((gp.getInc("t") + "").indexOf('.') - 1);
        this.numOfTimeSteps = gp.getStepsNum("t");
        this.Emin = gp.getMin("E");
        this.Emax = gp.getMax("E");
        this.dE = gp.getInc("E");
        this.energyDigits = (gp.getInc("E") + "").length() - (gp.getInc("E") + "").indexOf('.') - 1;
        this.numberOfEnergySteps = (int)((gp.getMax("E") - gp.getMin("E"))/gp.getInc("E"));
        this.everyE = 1;
        this.everyT = 1;
        this.starTimeFrom = 0;
        this.Ei = new double[par.getAtoms().size()];
        this.D = new double[numberOfEnergySteps];
        this.n = new Hashtable<>();
        this.Ut_ij = new Complex[2][numOfAtoms][numOfAtoms];
        for (Atom a : par.getAtoms()) {
            int i = a.getID();
            for (int j = 0; j < numOfAtoms; j++) {
                if (i == j) {
                    Ut_ij[0][i][j] = Complex.ONE;
                    Ut_ij[1][i][j] = Complex.ONE;
                } else {
                    Ut_ij[0][i][j] = Complex.ZERO;
                    Ut_ij[1][i][j] = Complex.ZERO;
                }
            }
        }
        int resultDigits = 2 * timeDigits + 2 * energyDigits;
        this.format = "%." + timeDigits + "f %." + energyDigits + "f %."+resultDigits+"f %d\n";
        if(initializeMatrices){
            initializeMatrices();
        }
    }

    public void initializeMatrices() {
        tmax = 1;
        Ut_ik = new ArrayList<>();
        int sigmaDim = 1;
        if (isSpinOrbit){
            sigmaDim = 2;
        }
        this.n = new Hashtable<>();
        this.sumOfCharges = new double[numOfAtoms];
        for (CalculationElectrode e : calculationElectrodes.values())
        {
            int dim = (int)((gp.getMax("E") - gp.getMin("E"))/gp.getInc("E"));
            Ut_ik.add(new Complex[2][dim][sigmaDim][numOfAtoms][sigmaDim]);
        }
        surfaceUt_k = new Complex[2][numberOfEnergySteps][numOfAtoms];
        for (int i = 0; i < numOfAtoms; i++){
            //sumOfCharges[i] = 0.0 + n.get(Integer.valueOf(i));
            for (int j = 0; j < numOfAtoms; j++) {
                if (i == j) {
                    Ut_ij[0][i][j] = Complex.ONE;
                } else {
                    Ut_ij[0][i][j] = Complex.ZERO;
                }
            }
            for (int n_sigma = 0 ; n_sigma < sigmaDim; n_sigma++) {
                for (int k_sigma = 0 ; k_sigma < sigmaDim; k_sigma++) {
                    for (int t = 0; t < 2; t++) {
                        for (int n = 0; n < numOfElectrodes; n++) {
                            int dim = gp.getStepsNum("E");
                            for (int e = 0; e < dim; e++) {
                                Ut_ik.get(n)[t][e][k_sigma][i][n_sigma] = ZERO;
                            }
                        }
                    }
                }
            }

            for (int e = 0; e < numberOfEnergySteps ; e ++) {
                for (int t = 0; t < 2; t++){
                    surfaceUt_k[t][e][i] = ZERO;
                }
            }
        }
    }
    //endregion

    //region Uij methods
    public void countUt_ij(int t){
        t = t % 2;
        Complex[][] k1, k2, k3, k4;
        k1 = new Complex[numOfAtoms][numOfAtoms];
        k2 = new Complex[numOfAtoms][numOfAtoms];
        k3 = new Complex[numOfAtoms][numOfAtoms];
        k4 = new Complex[numOfAtoms][numOfAtoms];
        for (int k = 0; k <4; k++){
            for (int i = 0; i < numOfAtoms; i++){
                for (int j = 0; j < numOfAtoms; j++){
                    CalculateU(t, k1, k2, k3, k4, k, i, j);
                }
            }
        }
    }

    private void CalculateU(int t, Complex[][] k1, Complex[][] k2, Complex[][] k3, Complex[][] k4, int k, int i, int j) {
        int T = t % 2;
        Hashtable<String,Complex> U = new Hashtable<String,Complex>();
        String id = i + "";
        int second;
        double dt = (gp.getInc("t"));
        Hashtable<Integer, CalculationBond> bonds = calculationBonds.get(i);
        if (bonds == null)
            return;
        double prevTime = time(t - 1, dt);
        switch (k){
            case 0:
                for (CalculationBond b : bonds.values()){
                    second = b.getOtherAtomID(i);
                    U.put(second + "",Ut_ij[T][second][j]);
                }
                if (!par.getElectrodesByAtomID(i).isEmpty()){
                    U.put(id, Ut_ij[T][i][j]);
                }
                k1[i][j] = function(i, prevTime, U);
                break;
            case 1:
                for (CalculationBond b : bonds.values()){
                    second = b.getOtherAtomID(i);
                    U.put(second + "",Ut_ij[T][second][j].plus(k1[second][j].times(dt / 2)));
                }
                if (!par.getElectrodesByAtomID(i).isEmpty()){
                    U.put(id, Ut_ij[T][i][j].plus(k1[i][j].times(dt/2)));
                }
                k2[i][j] = function(i, prevTime + dt / 2,U);
                break;
            case 2:
                for (CalculationBond b : bonds.values()){
                    second = b.getOtherAtomID(i);
                    U.put(second + "",Ut_ij[T][second][j].plus(k2[second][j].times(dt / 2)));
                }
                if (!par.getElectrodesByAtomID(i).isEmpty()){
                    U.put(id, Ut_ij[T][i][j].plus(k2[i][j].times(dt/2)));
                }
                k3[i][j] = function(i, prevTime + dt / 2, U);
                break;
            default:
                for (CalculationBond b : bonds.values()){
                    second = b.getOtherAtomID(i);
                    U.put(second + "", Ut_ij[T][second][j].plus(k3[second][j].times(dt)));
                }
                if (par.getElectrodesByAtomID(i).size() > 0){
                    U.put(id, Ut_ij[T][i][j].plus(k3[i][j].times(dt)));
                }
                k4[i][j] = function(i, prevTime + dt, U);
                Ut_ij[t % 2][i][j] =
                        Ut_ij[T][i][j].plus(
                                (k1[i][j].plus(
                                        k2[i][j].times(2)).plus(
                                        k3[i][j].times(2)).plus(
                                        k4[i][j]))
                                        .times(dt/6));
                break;
        }
    }


    private Complex function(int i, double time, Hashtable<String,Complex> U ) {
        return function(i, time, U,null);
    }



    private Complex function(int i, double time, Hashtable<String,Complex> U , Electrode electrode) {

        Complex result = Complex.ZERO;
        String id = i + "";
        Hashtable<Integer, CalculationBond> bonds = calculationBonds.get(i);
        int anotherAtom;
        for (CalculationBond b : bonds.values()){
            anotherAtom = b.getOtherAtomID(i);
            result = result.plus(Complex.I.times(exp_i((Ei[i] - Ei[anotherAtom]) * time)).times(U.get(anotherAtom + "")).times(b.get(COUPLING)));
        }
        CalculationAtom at = calculationAtoms.get(i);
        if (at.getElID() != null) {
            CalculationElectrode cE = calculationElectrodes.get(at.getElID());
            result = result.plus(U.get(i + "").times(-cE.get(COUPLING) / 2.0));
        }
        return result;
    }

    //region Rk4

    public void countUt_ik(CalculationElectrode electrode, Complex[][][][][] Ut_ik, int t, int T, double dt,
                           double prevTime, double electrodesWidth,
                           Complex[][] integralEnergy){

//        if (Globals.SURFACE_ELECTRODE_ID == (electrode)){
//            electrodeID = new Electrode(-1,null,null,par.getSurfaceCoupling(),gp.getInc("E"),Globals.SURFACE_ELECTRODE_ID,null);
//        }
        double energyStep = dE;
//        if (electrode != null){
//            energyStep = electrode.get(DE);
//        }
        int numberOfEnergySteps = (int)(electrodesWidth/energyStep);

        Complex[][][] kVec = new Complex[3][numOfAtoms][2];
        double calculatedVk = sqrt(gp.getWidth("E") * electrode.get(COUPLING) / ( 2 * PI));
        int elAtID = electrode.getID();
        double[] Vsf = new double[numOfAtoms];
        for (int i = 0; i < numOfAtoms; i++) {
            Vsf[i] = calculationAtoms.get(i).get(SPIN_FLIP);
        }
        int sigmaDim = 1;
        if (isSpinOrbit){
            sigmaDim = 2;
        }
        for (int e = 0; e < numberOfEnergySteps; e++) {

            for (int sigma_k = 0; sigma_k < sigmaDim; sigma_k++) {
                Complex[][] array = Ut_ik[T][e][sigma_k];
                Complex Ek = exp_i((toEnergy(e, gp) * (prevTime)));
                for (int k = 0; k < 4; k++) {
                    for (int nSigma = 0; nSigma < sigmaDim; nSigma++) {
                        for (int i = 0; i < numOfAtoms; i++) {
                            Integer elID = calculationAtoms.get(i).getElID();
                            CalculationElectrode cElectrode = elID != null ? calculationElectrodes.get(elID) : null;
                            double per = 1;
                            if (cElectrode != null) {
                                per = cElectrode.get(PERTURBATION_COUPLING) != null ? cElectrode.get(PERTURBATION_COUPLING) : 1;
                            }
                            double Vk;
                            if (i == elAtID) {
                                Vk = calculatedVk;
                            } else {
                                Vk = 0;
                            }
                            if (k == 0) {
                                kVec[k][i][nSigma] = functionUik(i, nSigma, sigma_k, k, dt, Ek, array, null, Vk, per, Vsf[i],
                                        integralEnergy, sigmaDim);
                            } else if (k < 3) {
                                kVec[k][i][nSigma] = functionUik(i, nSigma, sigma_k, k, dt, Ek, array, kVec[k - 1], Vk, per,  Vsf[i],
                                        integralEnergy, sigmaDim);
                            } else {
                                Complex kVec3 = functionUik(i, nSigma, sigma_k, k, dt, Ek, array, kVec[k - 1], Vk, per,  Vsf[i],
                                        integralEnergy, sigmaDim);
                                Ut_ik[t % 2][e][sigma_k][i][nSigma] =
                                        Ut_ik[T][e][sigma_k][i][nSigma].plus(
                                                (kVec[0][i][nSigma].plus(
                                                        kVec[1][i][nSigma].times(2)).plus(
                                                        kVec[2][i][nSigma].times(2)).plus(kVec3))
                                                        .times(dt / 6));

                            }
                        }
                    }
                }
            }
        }
    }
    //endregion

    //region constructor
    public TimeEvolutionHelper(NanoModeler modeller, Flag isInterupted){
        this.modeler = modeller;
        this.isInterupted = isInterupted;
        initialize();
    }
    //endregion

    //region maths
    public Complex  exp_i(double argument){
        return valueOf(Math.cos(argument), Math.sin(argument));
    }
    public double time(int timeStep, double dt){
        return timeStep * dt;
    }
    //endregion

    protected Complex functionUik(int i, int sigma,
                                  int sigmaK, int k,
                                  double dt, Complex Ek,
                                  Complex[][] Ut_ik,
                                  Complex[][] kVec, double Vk,
                                  double f, double Vsf,
                                  Complex[][] integralEnergy, int sigmaDim) {

        Complex result = Complex.ZERO;
        double gammaIJ = 0;
        double gammaHalf = 0;
        Integer index = calculationAtoms.get(i).getElID();
        if (index != null){
            CalculationElectrode ce = calculationElectrodes.get(index);
            if (ce != null)
                gammaHalf = ce.get(COUPLING)/2.0;//0.5;//el.getDoubleCoupling()/ 2.0;
        }

        int minusSigma = (sigma + 1) % sigmaDim;
        Complex spinFlipPart =
                Complex.valueOf(0, -Vsf).times(integralEnergy[i][sigma].divide(integralEnergy[i][minusSigma]));

        if (sigmaK == sigma){
            if (k == 0) {
                result = Ut_ik[i][sigma].times(-gammaHalf*f*f);
            }
            else if (k == 3){
                result = Ut_ik[i][sigma].plus(kVec[i][sigma].times(dt)).times(-gammaHalf*f*f);
            }
            else{
                result = Ut_ik[i][sigma].plus(kVec[i][sigma].times(dt/2)).times(-gammaHalf*f*f);
            }
        }
        if (k == 0) {
            result = result.plus(Ut_ik[i][minusSigma].times(spinFlipPart));
        }
        else if (k == 3){
            result = result.
                    plus(spinFlipPart.times(Ut_ik[i][minusSigma].plus(kVec[i][minusSigma].times(dt))));
        }
        else{
            result = result.
                    plus(spinFlipPart.times(Ut_ik[i][minusSigma].plus(kVec[i][minusSigma].times(dt/2))));
        }
        Hashtable<Integer, CalculationBond> table = calculationBonds.get(i);
        for (CalculationBond b : table.values()){
            int j = b.getSecond();
            double Vij = b.get(COUPLING);// * b.get(PERTURBATION_COUPLING);
            Complex correlationPart =
                    Complex.valueOf(0, Vij).times(integralEnergy[j][sigma].divide(integralEnergy[i][sigma]));
            if (k == 0){
                correlationPart = correlationPart.times(Ut_ik[j][sigma]);
            }
            else if (k == 3){
                correlationPart = correlationPart.times(Ut_ik[j][sigma].plus(kVec[j][sigma].times(dt)));
            }
            else{
                correlationPart = correlationPart.times(Ut_ik[j][sigma].plus(kVec[j][sigma].times(dt/2)));
            }
            result = result.minus(correlationPart);
        }

        if (sigmaK == sigma) {
            Complex decrement = Ek.divide((integralEnergy[i][sigma])).times(Vk*f);
            result = result.minus(decrement);
        }
        return result;
    }





    public void countDynamicParameters(int i, int sigmaN, int t, double dt,
                                       double constant, ArrayList<String> ldosArray,
                                       double[] TDOStemp, Complex[][] integralEnergy){

        for (int e = 0; e < numberOfEnergySteps; e++) {
            for (int n_sigma = 0; n_sigma < 1; n_sigma++) {
                double ldos = 0;
                for (int n = 0; n < numOfElectrodes; n++) {
                    int step = (int) (1.0 * e * Ut_ik.get(n)[t % 2].length / numberOfEnergySteps);
                    ldos += pow(Ut_ik.get(n)[t % 2][step][n_sigma][i][n_sigma].magnitude(), 2);
                }
                ldos = ldos / gp.getWidth("E");
                ldosArray.set(e, ldosArray.get(e) + "," + String.format("%.6f",ldos));
            }
        }
        integralEnergy[i][sigmaN] = integralEnergy[i][sigmaN].times(exp_i(Ei[i] * dt));
        Hashtable<Integer, CalculationBond> bonds = calculationBonds.get(i);
        for (CalculationBond b : bonds.values()){
            int j = b.getOtherAtomID(i);
            double u = b.get(CORRELATION_COUPLING) != null ? b.get(CORRELATION_COUPLING) : 1;
            integralEnergy[i][sigmaN] = integralEnergy[i][sigmaN].times(exp_i(u* n.get(j)* dt));
        }
    }

    public double countCharge(int i, int t, double dt) {
        double charge = 0;
        double resultN = 0;
        double time = time(t, dt);
        double normalisation = 0;
        double constant = dE / (gp.getWidth("E"));
        int sigmaDim = 1;//Ut_ik.get(0).length;
        boolean breakConditionCharge = false;
        boolean breakConditionLDOS = false;
        ldosE = 0;
        double csEnergy = 0;//Double.parseDouble(gp.getCrossSectionEnergy());
        for (int j = 0; j < numOfAtoms; j++) {
            resultN +=  calculationAtoms.get(j).get(INITIAL_OCCUPATION) * Math.pow(Ut_ij[t % 2][i][j].magnitude(), 2);
        }
        for (int n_sigma = 0; n_sigma < sigmaDim; n_sigma++){
            for (int e = 0; e < numberOfEnergySteps - 2; e++) {
                double ldos = 0;
                for (int n = 0; n < numOfElectrodes; n++) {
                    int step = (int) (1.0 * e * Ut_ik.get(n)[t % 2].length / numberOfEnergySteps);
                    ldos += pow(Ut_ik.get(n)[t % 2][step][n_sigma][i][n_sigma].magnitude(), 2);
                }
                if (par.isSurfacePresent()) {
                    ldos += pow(surfaceUt_k[t % 2][e][i].magnitude(), 2);
                }
                normalisation += ldos * constant;
                double energy = toEnergy(e, gp);
                if (energy >= 0 && !breakConditionCharge) {
                    charge = normalisation + resultN;
                    breakConditionCharge = true;
                }
                if (energy >= csEnergy && !breakConditionLDOS){
                    ldosE = ldos / gp.getWidth("E");
                    breakConditionLDOS = true;
                }
                if(breakConditionCharge && breakConditionLDOS){
                    break;
                }
            }
            if (breakConditionCharge && breakConditionLDOS){
                break;
            }
        }
        n.put(i, charge);
        return charge;
            //return fermiLDOS;

    }
    //endregion
}
