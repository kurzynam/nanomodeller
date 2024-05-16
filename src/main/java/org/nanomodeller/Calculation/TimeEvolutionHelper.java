package org.nanomodeller.Calculation;

import org.nanomodeller.GUI.NanoModeler;
import org.nanomodeller.Tools.Flag;
import org.nanomodeller.Tools.JEP_functions.*;
import org.nanomodeller.Tools.DataAccessTools.MyFileWriter;
import org.nanomodeller.Tools.StringUtils;
import org.nanomodeller.XMLMappingFiles.*;
import org.jscience.mathematics.number.Complex;
import org.nfunk.jep.JEP;
import org.nfunk.jep.function.*;

import javax.swing.*;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

import static org.nanomodeller.Calculation.CalculationItem.applyTimeForItemsCalculation;
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
    public GlobalProperties gp;

    public String format;
    public Parameters par;
    public NanoModeler modeler;
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

    public Hashtable<Integer, Double> n;
    public Complex[][][] Ut_ij;

    public Hashtable<Integer, CalculationElectrode> calculationElectrodes;
    //public Electrode[] electrodes;
    //protected Atom[] atoms;
    boolean isSpinOrbit = false;
    JEP parser;
    //endregion

    //region initialization
    public void initialize() {

        Instant starts = Instant.now();
        boolean arraysInitialized = false;
        initJEPfunctions();

        gp = GlobalProperties.getInstance();
        Iterator it = gp.getUserDefinedVariables().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Double> pair = (Map.Entry)it.next();
            parser.addVariable(pair.getKey(),pair.getValue());
        }
        double electrodesWidth = gp.getElectrodesWidth();
        double dt = gp.getDt();
        Complex Ek = Complex.ONE;
        double constant = dE / electrodesWidth;
        Complex[][] integralEnergy = null;
        MyFileWriter ldosList = null;
        MyFileWriter chargeList = null;
        MyFileWriter currentList = null;
        MyFileWriter ldosEList = null;
        Parameters par = Parameters.getInstance();

        if (arraysInitialized && StringUtils.isEmpty(par.getPath())){
            readData(par.getId());
        }
        else {
            readData(par.getId(), true);
            integralEnergy = new Complex[par.getAtoms().size()][2];
            for (int i = 0; i < 2; i++) {
                for (int comp = 0; comp < integralEnergy.length; comp++) {
                    integralEnergy[comp][i] = Complex.ONE;
                }
            }
        }
        String dynamicPATH = par.getPath();
        if (StringUtils.isEmpty(dynamicPATH)) {
            dynamicPATH = gp.getDynamicPATH();
        }
        chargeList = new MyFileWriter(dynamicPATH + "/" + CHARGE_FILE_NAME_PATTERN + ".csv");
        currentList  = new MyFileWriter(dynamicPATH + "/" + CURRENT_FILE_NAME_PATTERN + ".csv");
        ldosList = new MyFileWriter(dynamicPATH + "/" + LDOS_FILE_NAME_PATTERN + ".csv");
        ldosEList = new MyFileWriter(dynamicPATH + "/" + LDOS_E_FILE_NAME_PATTERN + ".csv");
        ldosList.printf("Step, Time, Energy");
        chargeList.printf("Step, Time");
        currentList.printf("Step, Time");
        for(int p = 0; p < numOfAtoms; p++){
            ldosList.printf(", LDOS%d", p);
            chargeList.printf(", Charge %d", p);
            currentList.printf(", Current %d", p);

        }
        ldosList.println();
        chargeList.println();
        currentList.println();
        ldosEList.println();

        for (int n = 0; n < 1; n++) {
            MyFileWriter sumldosF = new MyFileWriter(dynamicPATH + "/sumLDOSF.txt");
            MyFileWriter TDOSWriter = new MyFileWriter(dynamicPATH + "/TDOS.txt");
            initializeCalculationBonds(parser, par.getBonds(), calculationBonds);
            initializeCalculationAtoms(parser, par.getAtoms(), calculationAtoms);
            initializeCalculationElectrodes(parser, par.getElectrodes(), calculationElectrodes);
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
            for (int t = tmax; t < tmax + numOfTimeSteps; t++) {

                int T = t % 2 == 0 ? 1 : 0;;
                double time = time(t - 1, dt);
                double[] TDOStemp = new double[numberOfEnergySteps/everyE];
                parser.addVariable("t", time);

                applyTimeForItemsCalculation(parser, par.getAtoms(), calculationAtoms);
                applyTimeForItemsCalculation(parser, par.getElectrodes(), calculationElectrodes);
                applyTimeForItemsCalculation(parser, par.getBonds(), calculationBonds);

                countUt_ij(t);

                for (CalculationElectrode electrode : calculationElectrodes.values()) {
                    electrode_id = electrode.getID();
                    countUt_ik(electrode, Ut_ik.get(electrode_id), t, T, dt, time,
                            electrodesWidth, integralEnergy);

                }
                if (par.isSurfacePresent()) {
                    //Electrode surfaceElectrode = new Electrode(-1, null, null, par.getSurfaceCoupling(), gp.getdE(), Globals.SURFACE_ELECTRODE_ID, null);
                    //countUt_ik(surfaceElectrode, surfaceUt_k, t);
                }
                double charge;
                String chargesList = "";
                String currentsList = "";
                String ldosesEList = "";
                for (CalculationAtom atom : calculationAtoms.values()) {
                    charge = countCharge(atom.getID(), t, dt);
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
                chargeList.println(n + "," + time + "," + chargesList);
                currentList.println(n + "," + time + "," + currentsList);
                ldosEList.println(n + "," + time + "," + ldosesEList);
                int sigmaDim = 1;
                if (isSpinOrbit){
                    sigmaDim = 2;
                }
                ArrayList<String> ldosArray = new ArrayList<>();
                ArrayList<String> normalisationArray = new ArrayList<>();
                for (int e = 0; e < numberOfEnergySteps; e++) {
                    if (e % everyE != 0) {
                        continue;
                    }
                    ldosArray.add(n +"," + time(t, dt)+","+toEnergy(e, dE, gp));
                }
                for (CalculationAtom a : calculationAtoms.values()) {
                    int i = a.getID();
                    for (int sigma = 0; sigma < sigmaDim; sigma++) {
                        countDynamicParameters(i, sigma, t, dt, constant,
                                ldosArray,
                                TDOStemp, integralEnergy);
                    }
                }
                if (time >= starTimeFrom && t % everyT == 0){
                    for (int e = 0; e < ldosArray.size(); e++) {
                        ldosList.println(ldosArray.get(e));
                    }
                    ldosList.println();
                }
                String timeToDisplay = String.format("%.2f\n", time);
                modeler.getTimeEvolutionButton().setText("Cancel (" + timeToDisplay + ")");
            }
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
        }
        modeler.getTimeEvolutionButton().setImageIcon(new ImageIcon(TIME_EVOLUTION_BUTTON_IMAGE_PATH));
        modeler.getTimeEvolutionButton().setText("Count time evolution");
        Instant ends = Instant.now();
        System.out.println("########");
        System.out.println("Execution time: " + Duration.between(starts, ends));
        ldosList.close();
        chargeList.close();
        currentList.close();
        ldosEList.close();
    }


    private void initJEPfunctions() {
        parser = new JEP();
        parser.addFunction("cos", new Cosine());
        parser.addFunction("exp", new Exponent());
        parser.addFunction("pulse", new Pulse());
        parser.addFunction("sin", new Sine());
        parser.addFunction("tan", new Tangent());
        parser.addFunction("abs", new Abs());
        parser.addFunction("ln", new Logarithm());
        parser.addFunction("arccos", new ArcCosine());
        parser.addFunction("arcsin", new ArcSine());
        parser.addFunction("sinh", new SineH());
        parser.addFunction("cosh", new CosineH());
        parser.addFunction("tanh", new TanH());
        parser.addFunction("arctan", new ArcTangent());
        parser.addFunction("arctanh", new ArcTanH());
        parser.addFunction("arcsinh",new ArcSineH());
        parser.addFunction("arccosh", new ArcCosineH());
        parser.addFunction("rect", new Rectangular());
        parser.addFunction("step", new Heaviside());
        parser.addFunction("dirac", new DiracDelta());
        parser.addFunction("delta", new DiracDelta());
        parser.addFunction("ndirac", new NDelta());
        parser.addFunction("ndelta", new NDelta());
        parser.addFunction("tri", new Triangular());
        parser.addFunction("linear", new Linear());
        parser.addFunction("tstep", new TriangularStep());
    }

    //endregion

    //region data reading
    public void readData(String paramID){
        readData(paramID, false);
    }
    public void readData(String paramID, boolean initializeMatrices){
        this.par = Parameters.getInstance();
        this.numOfElectrodes = par.getElectrodes().size();
        this.numOfAtoms = Integer.parseInt(par.getNumber());
        this.timeDigits = (gp.getDt() + "").length() - (gp.getDt() + "").indexOf('.') - 1;
        this.numOfTimeSteps = (int)(Double.parseDouble(par.getTime())/gp.getDt());
        this.Emin = gp.getDoubleEmin();
        this.Emax = gp.getDoubleEmax();
        this.dE = gp.getdE();
        this.energyDigits = (gp.getdE() + "").length() - (gp.getdE() + "").indexOf('.') - 1;
        this.numberOfEnergySteps = gp.getNumberOfEnergySteps();
        this.everyE = gp.getTimeDependentWriteEveryE();
        this.everyT = gp.getTimeDependentWriteEveryT();
        this.starTimeFrom = Double.parseDouble(gp.getSaveTimeFrom());
        this.Ei = new double[par.getAtoms().size()];
        this.D = new double[numberOfEnergySteps];
        this.n = new Hashtable<>();
        this.Ut_ij = new Complex[2][numOfAtoms][numOfAtoms];
        for (Atom a : par.getAtoms()) {
            int i = a.getID();
            for (int j = 0; j < numOfAtoms; j++) {
                if (i == j) {
                    Ut_ij[0][i][j] = Complex.ONE;
                } else {
                    Ut_ij[0][i][j] = Complex.ZERO;
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
            int dim = (int)(gp.getElectrodesWidth()/e.get(DE));
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
                            int dim = (int) (gp.getElectrodesWidth() / par.getElectrodes().get(n).getDouble(DE));
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
        int T = t % 2 == 0 ? 1 : 0;
        Hashtable<String,Complex> U = new Hashtable<String,Complex>();
        String id = i + "";
        int second;
        double dt = gp.getDt();
        Hashtable<Integer, CalculationBond> bonds = calculationBonds.get(i);
        double prevTime = time(t - 1, dt);
        switch (k){
            case 0:
                for (CalculationBond b : bonds.values()){
                    second = b.getOtherAtomID(i);
                    U.put(second + "",Ut_ij[T][second][j]);
                }
                if (par.getElectrodesByAtomID(i).size() > 0){
                    U.put(id, Ut_ij[T][i][j]);
                }
                k1[i][j] = function(i, prevTime, U);
                break;
            case 1:
                for (CalculationBond b : bonds.values()){
                    second = b.getOtherAtomID(i);
                    U.put(second + "",Ut_ij[T][second][j].plus(k1[second][j].times(dt / 2)));
                }
                if (par.getElectrodesByAtomID(i).size() > 0){
                    U.put(id, Ut_ij[T][i][j].plus(k1[i][j].times(dt/2)));
                }
                k2[i][j] = function(i, prevTime + dt / 2,U);
                break;
            case 2:
                for (CalculationBond b : bonds.values()){
                    second = b.getOtherAtomID(i);
                    U.put(second + "",Ut_ij[T][second][j].plus(k2[second][j].times(dt / 2)));
                }
                if (par.getElectrodesByAtomID(i).size() > 0){
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
        result = result.plus(U.get(id).times(-calculationElectrodes.get(i).get(COUPLING)/ 2.0));
        return result;
    }

    //region Rk4

    public void countUt_ik(CalculationElectrode electrode, Complex[][][][][] Ut_ik, int t, int T, double dt,
                           double prevTime, double electrodesWidth,
                           Complex[][] integralEnergy){

//        if (Globals.SURFACE_ELECTRODE_ID == (electrode)){
//            electrodeID = new Electrode(-1,null,null,par.getSurfaceCoupling(),gp.getdE(),Globals.SURFACE_ELECTRODE_ID,null);
//        }
        double energyStep = dE;
        if (electrode != null){
            energyStep = electrode.get(DE);
        }
        int numberOfEnergySteps = (int)(electrodesWidth/energyStep);

        Complex[][][] kVec = new Complex[3][numOfAtoms][2];
        double calculatedVk = sqrt(gp.getElectrodesWidth() * electrode.get(COUPLING) / ( 2 * PI));
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
                Complex Ek = exp_i((toEnergy(e, energyStep, gp) * prevTime));
                for (int k = 0; k < 4; k++) {
                    for (int nSigma = 0; nSigma < sigmaDim; nSigma++) {
                        for (int i = 0; i < numOfAtoms; i++) {
                            CalculationElectrode cElectrode = calculationElectrodes.get(i);
                            double per = 0;
                            if (cElectrode != null)
                                per = cElectrode.get(PERTURBATION_COUPLING);
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

    protected Complex functionUik(int i, int sigma, int sigmaK, int k, double dt, Complex Ek,
                                  Complex[][] Ut_ik,
                                  Complex[][] kVec, double Vk, double f, double Vsf,
                                  Complex[][] integralEnergy, int sigmaDim) {

        Complex result = Complex.ZERO;
        double gammaIJ = 0;
        double gammaHalf = calculationElectrodes.get(i).get(COUPLING);//0.5;//el.getDoubleCoupling()/ 2.0;
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
            double Vij = b.get(COUPLING) * b.get(PERTURBATION_COUPLING);
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
        double time = time(t, dt);
        double normalisation = 0;
        int sigmaDim = Ut_ik.get(0).length;
        for (int e = 0; e < numberOfEnergySteps; e++) {

            for (int n_sigma = 0; n_sigma < 1; n_sigma++) {
                double ldos = 0;
                for (int n = 0; n < numOfElectrodes; n++) {
                    int step = (int) (1.0 * e * Ut_ik.get(n)[t % 2].length / numberOfEnergySteps);
                    ldos += pow(Ut_ik.get(n)[t % 2][step][n_sigma][i][n_sigma].magnitude(), 2);
                }
                normalisation += ldos * constant;
                ldos = ldos / gp.getElectrodesWidth();

                double energy = toEnergy(e, dE, gp);
                TDOStemp[e / everyE] += ldos;
                if (e % everyE == 0 && t % everyT == 0  && time >= starTimeFrom) {
                    String val = ldosArray.get(e / everyE);
                    ldosArray.set(e / everyE, val + "," + ldos);
                }

            }
        }
        integralEnergy[i][sigmaN] = integralEnergy[i][sigmaN].times(exp_i(Ei[i] * dt));
        Hashtable<Integer, CalculationBond> bonds = calculationBonds.get(i);
        for (CalculationBond b : bonds.values()){
            int j = b.getOtherAtomID(i);
            double u = b.get(CORRELATION_COUPLING);
            integralEnergy[i][sigmaN] = integralEnergy[i][sigmaN].times(exp_i(u* n.get(j)* dt));
        }
    }

    public double countCharge(int i, int t, double dt) {
        double charge = 0;
        double resultN = 0;
        double time = time(t, dt);
        double normalisation = 0;
        double constant = dE / (gp.getElectrodesWidth());
        int sigmaDim = 1;//Ut_ik.get(0).length;
        boolean breakConditionCharge = false;
        boolean breakConditionLDOS = false;
        ldosE = 0;
        double csEnergy = Double.parseDouble(gp.getCrossSectionEnergy());
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
                double energy = toEnergy(e, dE, gp);
                if (energy >= 0 && !breakConditionCharge) {
                    charge = normalisation + resultN;
                    breakConditionCharge = true;
                }
                if (energy >= csEnergy && !breakConditionLDOS){
                    ldosE = ldos / gp.getElectrodesWidth();
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
