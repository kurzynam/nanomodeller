package org.nanomodeller.Calculation;

import org.nanomodeller.Calculation.CalculationEntities.CalculationAtom;
import org.nanomodeller.Calculation.CalculationEntities.CalculationBond;
import org.nanomodeller.Calculation.CalculationEntities.CalculationElectrode;
import org.nanomodeller.GUI.NanoModeler;
import org.nanomodeller.Tools.Flag;
import org.nanomodeller.Tools.DataAccessTools.MyFileWriter;
import org.nanomodeller.XMLMappingFiles.*;
import org.jscience.mathematics.number.Complex;
import org.nfunk.jep.JEP;
import java.util.*;

import static org.jscience.mathematics.number.Complex.*;
import static org.nanomodeller.Calculation.CalculationEntities.CalculationItem.applyTimeForItemsCalculation;
import static org.nanomodeller.Calculation.Tools.JEPHelper.createJEP;
import static org.nanomodeller.Calculation.Tools.ProgressBarState.updateProgressBar;
import static org.nanomodeller.CommonPhysics.toEnergy;
import static org.nanomodeller.Globals.*;

import static java.lang.Math.PI;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import static org.nanomodeller.XMLMappingFiles.Atom.initializeCalculationAtoms;
import static org.nanomodeller.XMLMappingFiles.Bond.initializeCalculationBonds;
import static org.nanomodeller.XMLMappingFiles.Electrode.initializeCalculationElectrodes;

public class DynamicCalculations {

    public static final String CORRELATION_COUPLING = "CorrelationCoupling";
    public static final String SPIN_FLIP = "SpinFlip";
    public static final String COUPLING = "Coupling";
    public static final String PERTURBATION_COUPLING = "PerturbationCoupling";
    public boolean areCorrelations = false;
    //region public members
    public CommonProperties gp;

    public String format;
    public Parameters par = Parameters.getInstance();
    public NanoModeler modeler = NanoModeler.getInstance();
    public Complex[][][] surfaceUt_k;
    public double[] sumOfCharges;
    private Hashtable<Integer, Hashtable<Integer, CalculationBond>> calculationBonds = new Hashtable<>();
    private Hashtable<Integer, CalculationAtom> calculationAtoms = new Hashtable<>();
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

    public Complex[][][] Ut_ij;
    public Hashtable<Integer, CalculationElectrode> calculationElectrodes = new Hashtable<>();
    boolean isSpinOrbit = false;
    JEP parser;
    //endregion

    //region initialization
    public void initialize() {

        parser = createJEP();
        initializeCalculationBonds(parser, par.getBonds(), calculationBonds);
        initializeCalculationAtoms(parser, par.getAtoms(), calculationAtoms);
        initializeCalculationElectrodes(parser, par.getElectrodes(), calculationElectrodes, calculationAtoms);
        areCorrelations = par.getBonds().stream().anyMatch(bond -> bond.getProperties().contains(CORRELATION_COUPLING));

        gp = CommonProperties.getInstance();

        double electrodesWidth = gp.getWidth("E");
        double dt = gp.getInc("t");
        Complex[][] integralEnergy = null;
        MyFileWriter ldosList;

        MyFileWriter chargeList;
        MyFileWriter currentList;
        MyFileWriter ldosEList;
        Parameters par = Parameters.getInstance();

        readData(true);
        integralEnergy = new Complex[par.getAtoms().size()][2];
        for (int i = 0; i < 2; i++) {
            for (int comp = 0; comp < integralEnergy.length; comp++) {
                integralEnergy[comp][i] = ONE;
            }
        }
        String dynamicPATH = par.getPath();
        StringBuilder ldosBuilder = new StringBuilder();
        chargeList = new MyFileWriter(dynamicPATH + "/" + CHARGE_FILE_NAME_PATTERN + ".csv");
        currentList  = new MyFileWriter(dynamicPATH + "/" + CURRENT_FILE_NAME_PATTERN + ".csv");
        ldosList = new MyFileWriter(dynamicPATH + "/" + LDOS_FILE_NAME_PATTERN + ".csv");
        ldosEList = new MyFileWriter(dynamicPATH + "/" + LDOS_E_FILE_NAME_PATTERN + ".csv");
        ldosList.printf("Time, Energy");
        ldosBuilder.append("Time, Energy");
        chargeList.printf("Time,i,q");
        currentList.printf("Time");
        for(int p = 0; p < numOfAtoms; p++){
            ldosList.printf(", LDOS%d", p);
            currentList.printf(", Current %d", p);
        }
        ldosList.println();
        chargeList.println();
        currentList.println();
        ldosEList.println();


        MyFileWriter sumldosF = new MyFileWriter(dynamicPATH + "/sumLDOSF.txt");
        MyFileWriter TDOSWriter = new MyFileWriter(dynamicPATH + "/TDOS.txt");
        for (Integer i : calculationAtoms.keySet()){
            Double sf = calculationAtoms.get(i).get(SPIN_FLIP);
            isSpinOrbit |= sf > 0 ;
        }
        double[] charges = new double[calculationAtoms.size()];
        for (int i = 0; i < calculationAtoms.size(); i++) {
            charges[i] = 0;
        }
        int time = 0;
        float t = gp.getMin("t");
        do {
            updateProgressBar(t - gp.getVar("t").getMin(), "t", gp.getVar("t").getWidth(), NanoModeler.getInstance().getMenu().getSecondPB());
            double[] TDOStemp = new double[numberOfEnergySteps/everyE];
            parser.addVariable("t", t);
            applyTimeForItemsCalculation(parser, calculationAtoms);
            applyTimeForItemsCalculation(parser, calculationElectrodes);
            applyTimeForItemsCalculation(parser, calculationBonds, true);
            countUt_ij(time);
            for (CalculationElectrode electrode : calculationElectrodes.values()) {
                electrode_id = electrode.getID();
                countUt_ik(electrode, Ut_ik.get(electrode_id), time, dt,
                        integralEnergy);
            }
            if (par.isSurfacePresent()) {
                //Electrode surfaceElectrode = new Electrode(-1, null, null, par.getSurfaceCoupling(), gp.getInc("E"), Globals.SURFACE_ELECTRODE_ID, null);
                //countUt_ik(surfaceElectrode, surfaceUt_k, t);
            }
            double charge;
            String currentsList = "";
            for (CalculationAtom atom : calculationAtoms.values()) {
                charge = countCharge(atom.getID(), time);
                double current = (charge-charges[atom.getID()])/dt;
                currentsList += current;
                chargeList.printf("%3f,%d,%s\n", t, atom.getID(), charge);
                if (atom.getID() != calculationAtoms.size() - 1){
                    currentsList += ",";
                }
            }
            time++;
            currentList.printf("%3f%s\n", t, currentsList);
            ldosEList.printf("%3f%s\n", t, ldosEList);
            int sigmaDim = 1;
            if (isSpinOrbit){
                sigmaDim = 2;
            }
            ArrayList<String> ldosArray = new ArrayList<>();
            if (gp.shouldCompute("E")){
                for (int e = 0; e < numberOfEnergySteps; e++) {
                    if (e % everyE != 0) {
                        continue;
                    }
                    ldosArray.add(String.format("%.3f,%.3f", t, toEnergy(e, gp)));
                }
            } else {
                ldosArray.add(String.format("%.3f", t));
                numberOfEnergySteps = 1;
            }
            for (CalculationAtom a : calculationAtoms.values()) {
                int i = a.getID();
                for (int sigma = 0; sigma < sigmaDim; sigma++) {
                    countDynamicParameters(i, sigma, time, dt,
                            ldosArray,
                            integralEnergy);
                }
            }
            for (int e = 0; e < ldosArray.size(); e++) {
                ldosList.println(ldosArray.get(e));
            }
            ldosList.println();
            chargeList.println();
            t += gp.getInc("t");
        } while (t <= gp.getMax("t"));
        TDOSWriter.close();
        sumldosF.close();
        if (isInterupted.getValue()) {
            isInterupted.neg();
        } else {
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
    public void readData(boolean initializeMatrices){
        this.par = Parameters.getInstance();
        this.numOfElectrodes = par.getElectrodes().size();
        this.numOfAtoms = par.getAtoms().size();
        this.timeDigits = (gp.getInc("t") + "").length() - ((gp.getInc("t") + "").indexOf('.') - 1) - 1;
        this.numOfTimeSteps = gp.getStepsNum("t");
        this.Emin = gp.getMin("E");
        this.Emax = gp.getMax("E");
        this.dE = gp.getInc("E");
        this.energyDigits = (gp.getInc("E") + "").length() - (gp.getInc("E") + "").indexOf('.') - 2;
        this.numberOfEnergySteps = (int)((gp.getMax("E") - gp.getMin("E"))/gp.getInc("E"));
        this.everyE = 1;
        this.everyT = 1;
        this.starTimeFrom = 0;
        this.D = new double[numberOfEnergySteps];
        this.Ut_ij = new Complex[2][numOfAtoms][numOfAtoms];
        for (Atom a : par.getAtoms()) {
            int i = a.getID();
            for (int j = 0; j < numOfAtoms; j++) {
                if (i == j) {
                    Ut_ij[0][i][j] = ZERO;
                    Ut_ij[1][i][j] = ZERO;
                } else {
                    Ut_ij[0][i][j] = Complex.ZERO;
                    Ut_ij[1][i][j] = Complex.ZERO;
                }
            }
        }
        int resultDigits = 3;//2 * timeDigits + 2 * energyDigits;
        this.format = "%." + 3 + "f %." + 3 + "f %."+resultDigits+"f %d\n";
        if(initializeMatrices){
            initializeMatrices();
        }
    }
    public void initializeMatrices() {
        Ut_ik = new ArrayList<>();
        int sigmaDim = 1;
        if (isSpinOrbit){
            sigmaDim = 2;
        }
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
                    Ut_ij[0][i][j] = ONE;
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
        Complex[][][] arrays = new Complex[4][numOfAtoms][numOfAtoms];
        for (int k = 0; k <4; k++){
            for (int i = 0; i < numOfAtoms; i++){
                for (int j = 0; j < numOfAtoms; j++){
                    CalculateU(t, arrays, k, i, j);
                }
            }
        }
    }

    private void updateU(Hashtable<Integer, Complex> U, int T, int i, int j,
                         Complex[][][] array, int rkn,
                         double dt, double prevTime,
                         int factor) {
        for (CalculationBond b : calculationBonds.get(i).values()) {
            int second = b.getOtherAtomID(i);
            U.put(second, Ut_ij[T][second][j].plus(array[rkn][second][j].times(dt / factor)));
        }
        if (!par.getElectrodesByAtomID(i).isEmpty()) {
            U.put(i, Ut_ij[T][i][j].plus(array[rkn][i][j].times(dt / factor)));
        }
        array[rkn+1][i][j] = hammiltonian(i, prevTime + dt / 2,U);
    }

    private void CalculateU(int t, Complex[][][] arrays, int k, int i, int j) {
        int T = t % 2;
        Hashtable<Integer, Complex> U = new Hashtable<>();
        Integer id = i;
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
                    U.put(second, Ut_ij[T][second][j]);
                }
                if (!par.getElectrodesByAtomID(i).isEmpty()){
                    U.put(id, Ut_ij[T][i][j]);
                }
                arrays[0][i][j] = hammiltonian(i, prevTime, U);
                break;
            case 1:
                updateU(U, T, i, j, arrays, 0, dt, prevTime, 2);
                break;
            case 2:
                updateU(U, T, i, j, arrays, 1, dt, prevTime, 2);
                break;
            default:
                updateU(U, T, i, j, arrays, 2, dt, prevTime, 2);
                Ut_ij[t % 2][i][j] =
                        Ut_ij[T][i][j].plus(
                                (arrays[0][i][j].plus(
                                        arrays[1][i][j].times(2)).plus(
                                        arrays[2][i][j].times(2)).plus(
                                        arrays[3][i][j]))
                                        .times(dt/6));
                break;
        }
    }
    private Complex hammiltonian(int i, double time, Hashtable<Integer, Complex> U) {
        Complex result = Complex.ZERO;
        Hashtable<Integer, CalculationBond> bonds = calculationBonds.get(i);
        int anotherAtom;
        for (CalculationBond b : bonds.values()){
            anotherAtom = b.getOtherAtomID(i);
            result = result.plus(Complex.I.times(exp_i((getEnergy(i) - getEnergy(anotherAtom)) * time)).times(U.get(anotherAtom)).times(b.get(COUPLING)));
        }
        CalculationAtom at = calculationAtoms.get(i);
        if (at.getElID() != null) {
            CalculationElectrode cE = calculationElectrodes.get(at.getElID());
            result = result.plus(U.get(i).times(-cE.get(COUPLING)/ 2.0));
        }
        return result;
    }
    //region Rk4
    public void countUt_ik(CalculationElectrode electrode, Complex[][][][][] Ut_ik, int t, double dt,
                           Complex[][] integralEnergy){
        int T = t % 2;
        double prevTime = (t - 1) * dt;
        int numberOfEnergySteps = gp.getVariables().get("E").getStepsNum();

        Complex[][][] kVec = new Complex[4][numOfAtoms][2];
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
                            double per = (cElectrode != null) && cElectrode.get(PERTURBATION_COUPLING) != null ?
                                    cElectrode.get(PERTURBATION_COUPLING) : 1;
                            double Vk = (i == elAtID) ?
                                    calculatedVk : 0;
                            Complex[][] kVecTemp = (k > 0) ? kVec[k - 1] : null;
                            kVec[k][i][nSigma] = functionUik(i, nSigma, sigma_k, k, dt, Ek, array, kVecTemp, Vk, per,  Vsf[i],
                                    integralEnergy, sigmaDim);
                            if (k == 3){
                                Ut_ik[t % 2][e][sigma_k][i][nSigma] =
                                        Ut_ik[T][e][sigma_k][i][nSigma].plus(
                                                (kVec[0][i][nSigma].plus(
                                                        kVec[1][i][nSigma].times(2)).plus(
                                                        kVec[2][i][nSigma].times(2)).plus(kVec[k][i][nSigma]))
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
    public DynamicCalculations(NanoModeler modeller, Flag isInterupted){
        long startTime = System.currentTimeMillis();
        this.modeler = modeller;
        this.isInterupted = isInterupted;
        initialize();
        long endTime = System.currentTimeMillis();

        System.out.println(endTime-startTime);
    }
    //endregion

    //region maths
    public Complex exp_i(double argument){
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
                gammaHalf = ce.get(COUPLING)/2.0;
        }

        int minusSigma = (sigma + 1) % sigmaDim;
        Complex spinFlipPart =
                Complex.valueOf(0, -Vsf).times(integralEnergy[i][sigma].divide(integralEnergy[i][minusSigma]));

        if (sigmaK == sigma){
            if (k == 0) {
                result = Ut_ik[i][sigma].times(-gammaHalf*f*f);
            }
            else{
                double tStep = (k < 3) ? dt : dt/2;
                result = Ut_ik[i][sigma].plus(kVec[i][sigma].times(tStep)).times(-gammaHalf*f*f);
            }
        }
        if (k == 0) {
            result = result.plus(Ut_ik[i][minusSigma].times(spinFlipPart));
        }
        else{
            result = result.
                    plus(spinFlipPart.times(Ut_ik[i][minusSigma].plus(kVec[i][minusSigma].times(k == 3 ? dt : dt/2))));
        }
        Hashtable<Integer, CalculationBond> table = calculationBonds.get(i);
        for (CalculationBond b : table.values()){
            int j = b.getSecond();
            // * b.get(PERTURBATION_COUPLING);
            result = result.minus(



                            Complex.valueOf(0, b.get(COUPLING)).
                            times(integralEnergy[j][sigma].
                            divide(integralEnergy[i][sigma])).
                            times(k == 0 ? Ut_ik[j][sigma] : Ut_ik[j][sigma].plus(kVec[j][sigma].times(k == 3 ? dt : dt/2)))



                            );
//
//            cmplx temp = new cmplx(Ut_ik[j][sigma]);
//            if (k != 0){
//                cmplx temp2 = new cmplx(kVec[j][sigma]);
//                temp2.timesRe(k == 3 ? dt : dt/2);
//
//                temp.plus(temp2);
//            }
//            temp.timesIm(b.get(COUPLING));
//            temp.timesRe(integralEnergy[j][sigma]);
//            temp.divide(integralEnergy[i][sigma]);
//            temp.subtractFrom(result);
//            result = temp.toComplex();

        }
        if (sigmaK == sigma) {
            Complex decrement = Ek.divide((integralEnergy[i][sigma])).times(Vk*f);
            result = result.minus(decrement);
        }
        return result;
    }
    public void countDynamicParameters(int i, int sigmaN, int t, double dt,
                                       ArrayList<String> ldosArray,
                                       Complex[][] integralEnergy){

        for (int e = 0; e < numberOfEnergySteps; e++) {
            for (int n_sigma = 0; n_sigma < 1; n_sigma++) {
                double ldos = 0;
                for (int n = 0; n < numOfElectrodes; n++) {
                    int step = (int) (1.0 * e * Ut_ik.get(n)[t % 2].length / numberOfEnergySteps);
                    ldos += pow(Ut_ik.get(n)[t % 2][step][n_sigma][i][n_sigma].magnitude(), 2);
                }
                ldos = ldos / gp.getWidth("E");
                ldosArray.set(e, ldosArray.get(e) + "," + String.format("%.4f",ldos));
            }
        }
        integralEnergy[i][sigmaN] = integralEnergy[i][sigmaN].times(exp_i(getEnergy(i) * dt));
        if (areCorrelations){
            Hashtable<Integer, CalculationBond> bonds = calculationBonds.get(i);
            for (CalculationBond b : bonds.values()){
                int j = b.getOtherAtomID(i);
                double u = getCorrelationCoupling(b) != null ? getCorrelationCoupling(b) : 1;
                integralEnergy[i][sigmaN] = integralEnergy[i][sigmaN].times(exp_i(u* getEnergy(j) * dt));
            }
        }
    }

    private static Double getCorrelationCoupling(CalculationBond b) {
        return b.get(CORRELATION_COUPLING);
    }

    private Double getEnergy(int j) {
        return calculationAtoms.get(j).get("OnSiteEnergy");
    }

    public double countCharge(int i, int t) {
        double charge = 0;
        double resultN = 0;
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
        return charge;
            //return fermiLDOS;

    }
    //endregion
}
