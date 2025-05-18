package org.nanomodeller.Calculation;

import org.nanomodeller.Calculation.CalculationEntities.CalculationAtom;
import org.nanomodeller.Calculation.CalculationEntities.CalculationBond;
import org.nanomodeller.Calculation.CalculationEntities.CalculationElectrode;
import org.nanomodeller.GUI.NanoModeler;
import org.nanomodeller.Tools.Flag;
import org.nanomodeller.Tools.DataAccessTools.MyFileWriter;
import org.nanomodeller.XMLMappingFiles.*;
import org.ejml.data.Complex_F64;
import org.nfunk.jep.JEP;
import java.util.*;

import static org.nanomodeller.Calculation.CalculationEntities.CalculationItem.applyTimeForItemsCalculation;
import static org.nanomodeller.Calculation.Tools.ComplexOperations.*;
import static org.nanomodeller.Calculation.Tools.JEPHelper.createJEP;
import static org.nanomodeller.Calculation.Tools.ProgressBarState.updateProgressBar;
import static org.nanomodeller.Constants.TRPI;
import static org.nanomodeller.Globals.*;

import static java.lang.Math.sqrt;
import static org.nanomodeller.XMLMappingFiles.Atom.initializeCalculationAtoms;
import static org.nanomodeller.XMLMappingFiles.Bond.initializeCalculationBonds;
import static org.nanomodeller.XMLMappingFiles.Electrode.initializeCalculationElectrodes;

public class DynamicCalculations {

    public boolean areCorrelations = false;
    //region public members
    public CommonProperties gp;

    public String format;
    public Parameters par = Parameters.getInstance();
    public NanoModeler modeler = NanoModeler.getInstance();
    public Complex_F64[][][] surfaceUt_k;
    private Complex_F64 temp = new Complex_F64(0,0);
    private Complex_F64 result = new Complex_F64(0,0);
    private Complex_F64 spinFlipPart = new Complex_F64(0,0);
    public double[] sumOfCharges;
    private CalculationBond[][] calculationBonds;
    private CalculationAtom[] calculationAtoms;
    private Complex_F64[] U;
    public Complex_F64 [/*i*/][/*t*/][/*k*/][/*k_sigma*/][/*n*/][/*sigma_n*/] Ut_ik;
    public Flag isInterupted;
    public double dE;
    public double dt;
    public double Emin;
    public double Emax;
    public int numberOfEnergySteps;

    public boolean shouldSaveT = true;
    public boolean shouldSaveE = true;
    public double reciprocalNumOfESteps;

    public double reciprocalEWidth;

    public double eWidth;
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

    public Complex_F64[][][] Ut_ij;
    public CalculationElectrode[] calculationElectrodes;
    boolean isSpinOrbit = false;
    JEP parser;
    private int timeRatio;

    private int energyRatio;
    private Complex_F64[][] integralEnergy;

    private Complex_F64[][][] kVec;
    private Complex_F64[][] reciprocalIntegralEnergy;
    private boolean isChainSymmetric;
    //endregion

    //region initialization
    public void initialize() {

        parser = createJEP();
        int numOfAtoms = par.getAtoms().size();
        calculationBonds = new CalculationBond[numOfAtoms][numOfAtoms];
        calculationAtoms = new CalculationAtom[numOfAtoms];
        calculationElectrodes = new CalculationElectrode[par.getElectrodes().size()];
        U = new Complex_F64[numOfAtoms];
        kVec = new Complex_F64[4][numOfAtoms][2];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < numOfAtoms; j++) {
                for (int k = 0; k < 2; k++) {
                    kVec[i][j][k] = new Complex_F64(0.0, 0.0);
                }
            }
        }
        initializeCalculationBonds(parser, par.getBonds(), calculationBonds);
        initializeCalculationAtoms(parser, par.getAtoms(), calculationAtoms);
        initializeCalculationElectrodes(parser, par.getElectrodes(), calculationElectrodes, calculationAtoms);
        areCorrelations = par.getBonds().stream().anyMatch(bond -> bond.getProperties().contains(CORRELATION_COUPLING));

        gp = CommonProperties.getInstance();

        double electrodesWidth = gp.getWidth("E");
        MyFileWriter ldosList;

        MyFileWriter chargeList;
//        MyFileWriter currentList;
//        MyFileWriter ldosEList;
        Parameters par = Parameters.getInstance();

        readData(true);
        integralEnergy = new Complex_F64[par.getAtoms().size()][2];
        reciprocalIntegralEnergy = new Complex_F64[par.getAtoms().size()][2];
        for (int i = 0; i < 2; i++) {
            for (int comp = 0; comp < integralEnergy.length; comp++) {
                integralEnergy[comp][i] = new Complex_F64(1,0);
                reciprocalIntegralEnergy[comp][i] = new Complex_F64(1,0);
            }
        }
        String dynamicPATH = par.getPath();
//        StringBuilder ldosBuilder = new StringBuilder();
        chargeList = new MyFileWriter(dynamicPATH + "/" + CHARGE_FILE_NAME_PATTERN + ".csv");
//        currentList  = new MyFileWriter(dynamicPATH + "/" + CURRENT_FILE_NAME_PATTERN + ".csv");
        ldosList = new MyFileWriter(dynamicPATH + "/" + LDOS_FILE_NAME_PATTERN + ".csv");
//        ldosEList = new MyFileWriter(dynamicPATH + "/" + LDOS_E_FILE_NAME_PATTERN + ".csv");
        ldosList.printf("Time\t\t\t Energy");
//        ldosBuilder.append("Time, Energy")
        chargeList.printf("time\t\t\t\tatom\t\tcharge\n");
//        currentList.printf("Time");
        for(CalculationAtom atom : calculationAtoms){
            if(atom.getProperties().get("Save") > 0){

                ldosList.printf("\t\t\t\tLDOS%d", atom.getID());
//            currentList.printf(", Current %d", p);
            }
        }
        ldosList.println();
//        currentList.println();
//        ldosEList.println();


        MyFileWriter sumldosF = new MyFileWriter(dynamicPATH + "/sumLDOSF.txt");
        MyFileWriter TDOSWriter = new MyFileWriter(dynamicPATH + "/TDOS.txt");
        for (int i = 0; i < calculationAtoms.length; i++){
            Double sf = calculationAtoms[i].getSpinFlip();
            isSpinOrbit |= sf > 0 ;
        }
        double[] charges = new double[calculationAtoms.length];
        for (int i = 0; i < calculationAtoms.length; i++) {
            charges[i] = 0;
        }
        int time = 0;

        Range tRange = gp.getVar("t");
        double tMin = tRange.getMin();
        double tWidth = tRange.getWidth();
        timeRatio = numOfTimeSteps/gp.getInt("timeStepsInFile");
        if (timeRatio == 0)
            timeRatio = 1;
        energyRatio = numberOfEnergySteps/gp.getInt("energyStepsInFile");
        if (energyRatio == 0)
            energyRatio = 1;
        for (double t : tRange) {
            updateProgressBar(t - tMin, "t", tWidth, NanoModeler.getInstance().getMenu().getSecondPB());
            shouldSaveT = time % timeRatio == 0;

//            double[] TDOStemp = new double[numberOfEnergySteps/everyE];
            parser.addVariable("t", t);

            applyTimeForItemsCalculation(parser, calculationAtoms);
            applyTimeForItemsCalculation(parser, calculationElectrodes);
            applyTimeForItemsCalculation(parser, calculationBonds);

            countUt_ij(time);
            int T = time % 2;
            int ecount = 0;




            isChainSymmetric = par.isChainSymmetric();

            int length = calculationElectrodes.length;
            if (isChainSymmetric) {
                length /= 2;
                if (calculationElectrodes.length % 2 != 0) {
                    length++;
                }
            }
            for (int i = 0; i < length; i++) {
                countUt_ik(calculationElectrodes[i], time, T);
                updateProgressBar( ecount++, "E", length, NanoModeler.getInstance().getMenu().getFirstPB());
            }




            if (par.isSurfacePresent()) {
                //Electrode surfaceElectrode = new Electrode(-1, null, null, par.getSurfaceCoupling(), gp.getInc("E"), Globals.SURFACE_ELECTRODE_ID, null);
                //countUt_ik(surfaceElectrode, surfaceUt_k, t);
            }
            double charge;

            String currentsList = "";
            if(shouldSaveT) {
                for (CalculationAtom atom : calculationAtoms) {
                    charge = countCharge(atom.getID(), time);
                    double current = (charge - charges[atom.getID()]) / dt;
                    currentsList += current;
                    chargeList.printf("%4f\t\t\t%d\t\t\t%4f\n", t, atom.getID(), charge);
                    if (atom.getID() != calculationAtoms.length - 1) {
                        currentsList += ",";
                    }
                }
                chargeList.println();
            }
            time++;

//            currentList.printf("%3f%s\n", t, currentsList);
//            ldosEList.printf("%3f%s\n", t, ldosEList);
            int sigmaDim = 1;
            if (isSpinOrbit){
                sigmaDim = 2;
            }
            ArrayList<String> ldosArray = new ArrayList<>();
            for (int e = 0; shouldSaveT && e < numberOfEnergySteps; e++) {
                shouldSaveE = e % energyRatio == 0;
                if (shouldSaveE) {
                    ldosArray.add(String.format("%.4f\t\t\t%.4f", t, (Emin + e * dE)));
                }
            }
            for (CalculationAtom a : calculationAtoms) {

                for (int sigma = 0; sigma < sigmaDim; sigma++) {
                    countDynamicParameters(a, sigma, time, dt,
                            ldosArray);
                }

            }
            if(shouldSaveT){
                for (int e = 0; e < ldosArray.size(); e++) {
                    ldosList.println(ldosArray.get(e));
                }
                ldosList.println();
            }
        }
        TDOSWriter.close();
        sumldosF.close();
        if (isInterupted.getValue()) {
            isInterupted.neg();
        }
//        currentList.println();
//        ldosEList.println();
        ldosList.println();
//
        ldosList.close();
        chargeList.close();
//        currentList.close();
//        ldosEList.close();
        NanoModeler.getInstance().getMenu().clearBars();
    }











    //endregion

    //region data reading
    public void readData(boolean initializeMatrices){
        this.par = Parameters.getInstance();
        this.numOfElectrodes = par.getElectrodes().size();
        this.numOfAtoms = par.getAtoms().size();
        this.timeDigits = (gp.getInc("t") + "").length() - ((gp.getInc("t") + "").indexOf('.') - 1);
        this.numOfTimeSteps = gp.getStepsNum("t");
        this.Emin = gp.getMin("E");
        this.Emax = gp.getMax("E");
        this.dE = gp.getInc("E");
        this.dt = gp.getInc("t");
        this.eWidth = Emax - Emin;
        this.reciprocalEWidth = 1.0 / eWidth;
        this.energyDigits = (dE + "").length() - (dE + "").indexOf('.') - 1;
        this.numberOfEnergySteps = (int)(eWidth / dE);
        this.reciprocalNumOfESteps = 1.0 / numberOfEnergySteps;
        this.everyE = 1;
        this.everyT = 1;
        this.starTimeFrom = 0;
        this.D = new double[numberOfEnergySteps];
        this.Ut_ij = new Complex_F64[2][numOfAtoms][numOfAtoms];
        for (Atom a : par.getAtoms()) {
            int i = a.getID();
            for (int j = 0; j < numOfAtoms; j++) {
                if (i == j) {
                    Ut_ij[0][i][j] = new Complex_F64(0,0);
                    Ut_ij[1][i][j] = new Complex_F64(0,0);
                } else {
                    Ut_ij[0][i][j] = new Complex_F64(0,0);
                    Ut_ij[1][i][j] = new Complex_F64(0,0);
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

        int sigmaDim = 1;
        if (isSpinOrbit){
            sigmaDim = 2;
        }
        this.sumOfCharges = new double[numOfAtoms];
        Ut_ik = new Complex_F64[numOfElectrodes][2][numberOfEnergySteps][sigmaDim][numOfAtoms][sigmaDim];
        surfaceUt_k = new Complex_F64[2][numberOfEnergySteps][numOfAtoms];
        for (int i = 0; i < numOfAtoms; i++){
            for (int j = 0; j < numOfAtoms; j++) {
                if (i == j) {
                    Ut_ij[0][i][j] = new Complex_F64(1,0);
                } else {
                    Ut_ij[0][i][j] = new Complex_F64(0,0);
                }
            }
            for (int n_sigma = 0 ; n_sigma < sigmaDim; n_sigma++) {
                for (int k_sigma = 0 ; k_sigma < sigmaDim; k_sigma++) {
                    for (int t = 0; t < 2; t++) {
                        for (int n = 0; n < numOfElectrodes; n++) {
                            for (int e = 0; e < numberOfEnergySteps; e++) {
                                Ut_ik[n][t][e][k_sigma][i][n_sigma] = new Complex_F64(0,0);
                            }
                        }
                    }
                }
            }

            for (int e = 0; e < numberOfEnergySteps ; e ++) {
                for (int t = 0; t < 2; t++){
                    surfaceUt_k[t][e][i] = new Complex_F64(0,0);
                }
            }
        }
    }
    //endregion

    //region Uij methods
    public void countUt_ij(int t){
        t = t % 2;
        Complex_F64[][][] arrays = new Complex_F64[4][numOfAtoms][numOfAtoms];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < numOfAtoms; j++) {
                for (int k = 0; k < numOfAtoms; k++) {
                    arrays[i][j][k] = new Complex_F64(0.0, 0.0);
                }
            }
        }
        for (int k = 0; k <4; k++){
            for (int i = 0; i < numOfAtoms; i++){
                for (int j = 0; j < numOfAtoms; j++){
                    CalculateU(t, arrays, k, i, j);
                }
            }
        }
    }

    private void updateU(int T, int i, int j,
                         Complex_F64[][][] array, int rkn,
                         double dt, double prevTime,
                         int factor) {
        for (CalculationBond b : calculationBonds[i]) {
            if (b != null) {
                int second = b.getOtherAtomID(i);
                set(temp, array[rkn][second][j]);
                timesR(temp, dt/ factor);
                plusC(temp, Ut_ij[T][second][j]);
                if (U[second] != null) {
                    set(U[second], temp);
                }
                else {
                    U[second] = new Complex_F64(temp.real,temp.imaginary);
                }
            }
        }
        if (par.getElectrodeByAtomID(i) != null) {
            set(temp, array[rkn][i][j]);
            timesR(temp, dt/ factor);
            plusC(temp, Ut_ij[T][i][j]);
            if (U[i] != null) {
                set(U[i], temp);
            }
            else {
                U[i] = new Complex_F64(temp.real,temp.imaginary);
            }
        }
        hammiltonian(i, prevTime + dt  * 0.5);
        set(array[rkn+1][i][j], result) ;
    }

    private void CalculateU(int t, Complex_F64[][][] arrays, int k, int i, int j) {
        int T = t % 2;
        Integer id = i;
        int second;
        CalculationBond[] bonds = calculationBonds[i];
        if (bonds == null)
            return;
        switch (k){
            case 0:
                for (CalculationBond b : bonds){
                    if (b != null){
                        second = b.getOtherAtomID(i);
                        U[second] = Ut_ij[T][second][j];
                    }
                }
                if (par.getElectrodeByAtomID(i) != null){
                    U[id] = Ut_ij[T][i][j];
                }
                hammiltonian(i, time(t - 1, dt));
                set(arrays[0][i][j], result);
                break;
            case 1:
                updateU(T, i, j, arrays, 0, dt, time(t - 1, dt), 2);
                break;
            case 2:
                updateU(T, i, j, arrays, 1, dt, time(t - 1, dt), 2);
                break;
            default:
                updateU(T, i, j, arrays, 2, dt, time(t - 1, dt), 2);
                set(temp, arrays[1][i][j]);
                plusC(temp, arrays[2][i][j]);
                timesR(temp, 2);
                plusC(temp, arrays[3][i][j]);
                plusC(temp, arrays[0][i][j]);
                timesR(temp, dt*0.16666);
                plusC(temp, Ut_ij[T][i][j]);
                set(Ut_ij[t % 2][i][j], temp);
                break;
        }
    }


    private void hammiltonian(int i, double time) {
        CalculationBond[] bonds = calculationBonds[i];
        int anotherAtom;
        for (CalculationBond b : bonds){
            if (b != null){
                anotherAtom = b.getOtherAtomID(i);
                setExp(temp, (getEnergy(i) - getEnergy(anotherAtom)) * time);
                timesC(temp, U[anotherAtom]);
                timesI(temp, b.getCoupling());
                plusC(result, temp);
            }

        }
        CalculationAtom at = calculationAtoms[i];
        if (at.getElID() > -1) {
            CalculationElectrode cE = calculationElectrodes[at.getElID()];
            set(temp,U[i]);
            timesI(temp,-cE.getCoupling()*0.5);
            plusC(result, temp);
        }
    }

    //region Rk4

    public void countUt_ik(CalculationElectrode electrode, int t, int T){
        double prevTime = (t - 1) * dt;
        electrode_id = electrode.getID();
        Complex_F64[][][][][] Ut_ik = this.Ut_ik[electrode_id];
        double calculatedVk = sqrt(eWidth * electrode.getCoupling() * TRPI);
        int elAtID = electrode.getID();
        int sigmaDim = 1;
        if (isSpinOrbit){
            sigmaDim = 2;
        }
        int elIncr = electrode.getStepSkip();
        for (int e = 0; e < numberOfEnergySteps; e += elIncr) {
            double phase = (Emin + e * dE) * prevTime;
            for (int sigma_k = 0; sigma_k < sigmaDim; sigma_k++) {
                Complex_F64[][] array = Ut_ik[T][e][sigma_k];
                for (int k = 0; k < 4; k++) {
                    for (int nSigma = 0; nSigma < sigmaDim; nSigma++) {
                        for (int i = 0; i < numOfAtoms; i++) {
                            int elID = calculationAtoms[i].getElID();
                            CalculationElectrode cElectrode = elID > -1 ? calculationElectrodes[elID] : null;
                            double per = (cElectrode != null) && cElectrode.getPerturbationCoupling() != null ?
                                    cElectrode.getPerturbationCoupling() : 1;
                            Complex_F64[][] kVecTemp = (k > 0) ? kVec[k - 1] : null;
                            functionUik(i, nSigma, sigma_k, k, phase, array, kVecTemp, (i == elAtID) ?
                                    calculatedVk : 0, per, sigmaDim);
                            set(kVec[k][i][nSigma], result);
                            if (k == 3){
                                set(temp, kVec[1][i][nSigma]);
                                plusC(temp, kVec[2][i][nSigma]);
                                timesR(temp, 2);
                                plusC(temp, kVec[0][i][nSigma]);
                                plusC(temp, kVec[3][i][nSigma]);
                                timesR(temp, dt*0.16666);
                                plusC(temp, Ut_ik[T][e][sigma_k][i][nSigma]);
                                for (int etmp = 0; etmp < elIncr; etmp++) {
                                    set(this.Ut_ik[electrode_id][t % 2][e + etmp][sigma_k][i][nSigma], temp);
                                    if(isChainSymmetric){
                                        int lastElID = numOfElectrodes - electrode_id - 1;
                                        int lastAtomID = numOfAtoms - i - 1;
                                        set(this.Ut_ik[lastElID]
                                                [t % 2][e + etmp][sigma_k][lastAtomID][nSigma], temp);
                                    }
                                }
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

    public double time(int timeStep, double dt){
        return timeStep * dt;
    }
    //endregion

    protected void functionUik(int i, int sigma,
                               int sigmaK, int k, double phase,
                               Complex_F64[][] Ut_ik,
                               Complex_F64[][] kVec, double Vk,
                               double f,
                               int sigmaDim) {

        double gammaIJ = 0;
        double gammaHalf = 0;
        int index = calculationAtoms[i].getElID();
        if (index > -1){
            CalculationElectrode ce = calculationElectrodes[index];
            if (ce != null)
                gammaHalf = ce.getCoupling()*2;
        }
        if (sigmaK == sigma){
            if (k == 0) {
                set(result,Ut_ik[i][sigma]);//.times(-gammaHalf*f*f);
            }
            else{
                double tStep = (k == 3) ? dt : dt * 0.5;
                set(temp, kVec[i][sigma]);
                timesR(temp, tStep);
                plusC(temp, Ut_ik[i][sigma]);
                set(result, temp);
            }
        }
        timesR(result, -gammaHalf*f*f);
        if (isSpinOrbit){

            int minusSigma = (sigma + 1) % sigmaDim;
            set(spinFlipPart, integralEnergy[i][sigma]);
            timesC(spinFlipPart, reciprocalIntegralEnergy[i][minusSigma]);
            timesI(spinFlipPart, -calculationAtoms[i].getSpinFlip());
            if (k == 0) {
                set(temp, Ut_ik[i][minusSigma]);
                timesC(temp, spinFlipPart);
            }
            else{
                set(temp, kVec[i][minusSigma]);
                timesR(temp, k == 3 ? dt : dt*0.5);
                plusC(temp, Ut_ik[i][minusSigma]);
                timesC(temp,spinFlipPart);
            }
            plusC(result,temp);
        }else{
            if (k == 0) {
                plusC(result,Ut_ik[i][0]);
            }
        }


        for (CalculationBond b : calculationBonds[i]){
            if (b != null){
                int j = b.getSecond(i);
                // * b.get(PERTURBATION_COUPLING);
                if (k != 0){
                    set(temp, kVec[j][sigma]);
                    timesR(temp, k == 3 ? dt : dt*0.5);
                    plusC(temp, Ut_ik[j][sigma]);
                }
                else {
                    set(temp, Ut_ik[j][sigma]);
                }
                timesC(temp, reciprocalIntegralEnergy[j][sigma]);
                timesC(temp, integralEnergy[i][sigma]);
                timesI(temp, 2*b.getCoupling());
                minusC(result, temp);
            }

        }
        if (sigmaK == sigma) {
            setExp(temp, -phase);
            timesC(temp, integralEnergy[i][sigma]);
            timesI(temp, -Vk*f);
            minusC(result, temp);
        }
    }



    public void countDynamicParameters(CalculationAtom atom, int sigmaN, int t, double dt,
                                       ArrayList<String> ldosArray){
        int atomID = atom.getID();
        if (shouldSaveT && atom.getProperties().get("Save") > 0) {
            for (int e = 0; e < numberOfEnergySteps; e++) {
                shouldSaveE = e % energyRatio == 0;
                for (int n_sigma = 0; shouldSaveE && n_sigma < 1; n_sigma++) {
                    double ldos = 0;
                    for (int n = 0; n < numOfElectrodes; n++) {
                        int step = (int) (e * Ut_ik[n][t % 2].length * reciprocalNumOfESteps);
                        ldos += Ut_ik[n][t % 2][step][n_sigma][atomID][n_sigma].getMagnitude2();
                    }
                    ldos = ldos * reciprocalEWidth;
                    ldosArray.set(e/energyRatio, ldosArray.get(e/energyRatio) + String.format("\t\t\t%.4f", ldos));
                }
            }
        }
        setExp(temp, getEnergy(atomID) * dt);
        timesC(integralEnergy[atomID][sigmaN], temp);
        if (areCorrelations){
            CalculationBond[] bonds = calculationBonds[atomID];
            for (CalculationBond b : bonds){
                int j = b.getOtherAtomID(atomID);
                double u = b.getCorrelationCoupling() != null ? b.getCorrelationCoupling() : 1;
                setExp(temp, u * getEnergy(j) * dt);
                timesC(integralEnergy[atomID][sigmaN], temp);
            }
        }
        set(reciprocalIntegralEnergy[atomID][sigmaN],integralEnergy[atomID][sigmaN]);
        invC(reciprocalIntegralEnergy[atomID][sigmaN]);
    }


    private Double getEnergy(int j) {
        return calculationAtoms[j].getOnSiteEnergy();
    }

    public double countCharge(int i, int t) {
        double resultN = 0;
        double normalisation = 0;
        double constant = reciprocalNumOfESteps;
        int sigmaDim = 1;//Ut_ik.get(0).length;
        boolean breakConditionCharge = false;
        boolean breakConditionLDOS = false;
        ldosE = 0;
        double csEnergy = 0;//Double.parseDouble(gp.getCrossSectionEnergy());
//        for (int j = 0; j < numOfAtoms; j++) {
//            resultN +=  calculationAtoms[j].getInitialOccupation() * Ut_ij[t % 2][i][j].getMagnitude2();
//        }
        for (int n_sigma = 0; n_sigma < sigmaDim; n_sigma++){
            for (int e = 0; e < numberOfEnergySteps - 2; e++) {
                double ldos = 0;
                for (int n = 0; n < numOfElectrodes; n++) {
                    int step = (int) (1.0 * e * Ut_ik[n][t % 2].length * reciprocalNumOfESteps);
                    ldos += Ut_ik[n][t % 2][step][n_sigma][i][n_sigma].getMagnitude2();
                }
                if (par.isSurfacePresent()) {
                    ldos += surfaceUt_k[t % 2][e][i].getMagnitude2();
                }
                normalisation += ldos * constant;
                double energy = Emin + e * dE;
                if (energy >= 0 && !breakConditionCharge) {
                    breakConditionCharge = true;
                }
                if (energy >= csEnergy && !breakConditionLDOS){
                    ldosE = ldos * reciprocalEWidth;
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
        return normalisation + resultN;
        //return fermiLDOS;

    }
    //endregion
}