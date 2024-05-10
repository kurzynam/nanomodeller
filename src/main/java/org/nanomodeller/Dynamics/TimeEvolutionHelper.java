package org.nanomodeller.Dynamics;

import org.nanomodeller.GUI.NanoModeller;
import org.nanomodeller.Tools.Flag;
import org.nanomodeller.Tools.JEP_functions.*;
import org.nanomodeller.Tools.DataAccessTools.MyFileWriter;
import org.nanomodeller.Globals;
import org.nanomodeller.Tools.StringUtils;
import org.nanomodeller.XMLMappingFiles.*;
import org.jscience.mathematics.number.Complex;
import org.nfunk.jep.JEP;
import org.nfunk.jep.function.*;

import javax.swing.*;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

import static org.nanomodeller.CommonPhysics.toEnergy;
import static org.nanomodeller.Globals.*;
import static org.nanomodeller.XMLMappingFiles.XMLHelper.readParametersFromXMLFile;
import static java.lang.Math.PI;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import static org.jscience.mathematics.number.Complex.ZERO;
import static org.jscience.mathematics.number.Complex.valueOf;

public class TimeEvolutionHelper {

    //region public members
    public GlobalChainProperties gp;
    public String format;
    public Parameters par;
    public NanoModeller modeller;
    public Complex[][][] surfaceUt_k;
    public double[] sumOfCharges;
    private int tmax = 1;
    public double[] Ei;
    public Hashtable<Integer,Double> n;
    public Hashtable<Integer,Double> n0;
    public Hashtable<Integer,String> energies;
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
    public Electrode[] electrodes;
    protected Atom[] atoms;
    boolean isSpinOrbit = false;
    JEP parser;
    //endregion

    //region initialization
    public void initialize() {

        Instant starts = Instant.now();
        boolean arraysInitialized = false;
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

        gp = readParametersFromXMLFile(Globals.XML_FILE_PATH);
        Iterator it = gp.getUserDefinedVariables().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Double> pair = (Map.Entry)it.next();
            parser.addVariable(pair.getKey(),pair.getValue());
        }
        double electrodesWidth = gp.getElectrodesWidth();
        double dt = gp.getDt();
        Complex Ek = Complex.ONE;
        double constant = dE / electrodesWidth;
        int stepNo = -1;
        Complex[][] integralEnergy = null;
        MyFileWriter ldosList = null;
        MyFileWriter chargeList = null;
        MyFileWriter currentList = null;
        MyFileWriter ldosEList = null;
        for (Parameters par : gp.getParameters()) {

            Collections.sort(par.getAtoms(), Atom.Comparators.ID);
            if(!par.getActive()){
                continue;
            }
            ArrayList<Bound>[] atomsBindings = new ArrayList[par.getAtoms().size()];
            double[] atomsElectrodes = new double[par.getAtoms().size()];
            this.atoms = par.getAtoms().toArray(new Atom[par.getAtoms().size()]);
            double[] Vsf = new double[par.getAtoms().size()];
            double[] perturbations = new double[par.getAtoms().size()];
            for (Atom atom : atoms){
                ArrayList<Bound> ints = new ArrayList<>();
                int atomID = atom.getID();
                for (Atom boundAtom : atoms){

                    if (par.isSurfacePresent()){
                        //ints.add(boundAtom);
//                        ints.add(par.getBound(atomID,boundAtom.getID()));
                    }else {
                        Bound bound = par.getBound(atomID,boundAtom.getID());
                        if (bound != null) {
                            ints.add(bound);
                        }
                    }
                }
                atomsBindings[atomID]= ints;
            }
            electrodes = new Electrode[par.getElectrodes().size()];
            for (Electrode electrode : par.getElectrodes()) {
                electrodes[electrode.getId()] = electrode;
            }
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
                arraysInitialized = true;
            }
            stepNo++;
            String dynamicPATH = par.getPath();
            if (StringUtils.isEmpty(dynamicPATH)) {
                dynamicPATH = gp.getDynamicPATH();
            }
            chargeList = new MyFileWriter(dynamicPATH + "/" + CHARGE_FILE_NAME_PATTERN + ".csv", true);
            currentList  = new MyFileWriter(dynamicPATH + "/" + CURRENT_FILE_NAME_PATTERN + ".csv", true);
            ldosList = new MyFileWriter(dynamicPATH + "/" + LDOS_FILE_NAME_PATTERN + ".csv", true);
            ldosEList = new MyFileWriter(dynamicPATH + "/" + LDOS_E_FILE_NAME_PATTERN + ".csv", true);
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
            for (int n = 0; n < par.getNumOfSubSteps(); n++) {

                if (par.getNumOfSubSteps() > 1) {
                    initializeMatrices();
                    modeller.getApplyToAllButton().setText("Next step ("+ stepNo + "." + n +")");
                }
                parser.addVariable("n", n);
                for (Atom a : par.getAtoms()) {
//                    if (ldosList.size() - 1 < par.getAtoms().indexOf(a)) {
//                        ldosList.add(new MyFileWriter(dynamicPATH + "/" + LDOS_FILE_NAME_PATTERN + par.getAtoms().indexOf(a) + TXT));
//                        normalisationList.add(new MyFileWriter(dynamicPATH + "/" + NORMALISATION_FILE_NAME_PATTERN + par.getAtoms().indexOf(a) + TXT));
//                        fermiLDOSList.add(new MyFileWriter(dynamicPATH + "/" + FERMI_LDOS_FILE_NAME_PATTERN + par.getAtoms().indexOf(a) + TXT, true));
//                    }
                    int i = a.getID();
                    parser.parseExpression(a.getN0());
                    double value = parser.getValue();
                    this.n.put(i, value);

//                    atomsElectrodes[i] = par.getElectrodeCouplingsByAtomID(i)/2;
                    n0.put(i, value);
                }
                MyFileWriter sumldosF = new MyFileWriter(dynamicPATH + "/sumLDOSF.txt");
                MyFileWriter TDOSWriter = new MyFileWriter(dynamicPATH + "/TDOS.txt");

                ArrayList<Bound> bounds = par.getBounds();
                double[] charges = new double[atoms.length];
                for (int i = 0; i < atoms.length; i++) {
                    charges[i] = 0;
                }
                for (int t = tmax; t < tmax + numOfTimeSteps; t++) {
                    for (Electrode electrode : par.getElectrodes()) {
                        double perturbation = 1;
                        parser.parseExpression(electrode.getPerturbation());
                        if (StringUtils.isNotEmpty(electrode.getPerturbation())) {
                            perturbation = parser.getValue();
                        }
                        electrode.setParsedPerturbation(perturbation);
                        double coupling = 0;
                        parser.parseExpression(electrode.getCoupling());
                        if (StringUtils.isNotEmpty(electrode.getCoupling())) {
                            coupling = parser.getValue();
                        }
                        electrode.setParsedCoupling(coupling);
//                        double dE = 0;
//                        parser.parseExpression(electrode.getdE());
//                        if (StringUtils.isNotEmpty(electrode.getdE())) {
//                            dE = parser.getValue();
//                        }
//                        electrode.setCoupling(dE+"");
                    }
                    int T = t % 2 == 0 ? 1 : 0;;
                    double time = time(t - 1, dt);
                    double[] TDOStemp = new double[numberOfEnergySteps/everyE];
                    if (isInterupted.getValue()) {
                        tmax = t;
                        break;
                    }
                    parser.addVariable("t", time);
                    for (Atom a : par.getAtoms()) {
                        int i = a.getID();
                        parser.addVariable("j", i);
                        parser.parseExpression(energies.get(i));
                        Ei[i] = parser.getValue();
                        Vsf[i] = Double.parseDouble(a.getSpinFlip());
                        parser.parseExpression(a.getPerturbation());
                        if (par.getElectrodesByAtomID(i).size() > 0) {
                            perturbations[i] = par.getElectrodesByAtomID(i).get(0).getParsedPerturbation();
                        }
                        else{
                            perturbations[i] = 1;
                        }
                        parser.parseExpression(energies.get(i));
                        a.setParsedPerturbation(parser.getValue());
                        atomsElectrodes[i] = par.getElectrodeCouplingsByAtomID(i);
                        //a.getID();
//                        parser.parseExpression(a.getN0());
//
//                        double value = parser.getValue();
//                        this.n0.put(a.getID(), value);
//                        this.n.put(a.getID(),value);
                    }
                    for (Bound bound : bounds){
                        if (bound != null) {
                            double Vij = 0;
                            double U = 0;
                            double perturbation = 1;
                            parser.parseExpression(bound.getCoupling());
                            if (StringUtils.isNotEmpty(bound.getCoupling())) {
                                Vij = parser.getValue();
                            }
                            bound.setParsedCoupling(Vij);
                            parser.parseExpression(bound.getCorrelationCoupling());
                            if (StringUtils.isNotEmpty(bound.getCorrelationCoupling())) {
                                U = parser.getValue();
                            }
                            bound.setParsedCorrelationCoupling(U);
                            parser.parseExpression(bound.getPerturbation());
                            if (StringUtils.isNotEmpty(bound.getPerturbation())) {
                                perturbation = parser.getValue();
                            }
                            bound.setParsedPerturbation(perturbation);
                        }
                    }

                        countUt_ij(t);

                    for (Electrode electrode : par.getElectrodes()) {
                        electrode_id = electrode.getId();
                        countUt_ik(electrode, Ut_ik.get(electrode_id), t, T, dt, Ek, time,
                                electrodesWidth, atomsBindings, integralEnergy, Vsf, perturbations,  atomsElectrodes);

                    }
                    if (par.isSurfacePresent()) {
                        Electrode surfaceElectrode = new Electrode(-1, null, null, par.getSurfaceCoupling(), gp.getdE(), Globals.SURFACE_ELECTRODE_ID, null);
                        //countUt_ik(surfaceElectrode, surfaceUt_k, t);
                    }
                    double charge;
                    String chargesList = "";
                    String currentsList = "";
                    String ldosesEList = "";
                    for (int i = 0; i < atoms.length; i++) {
                        charge = countCharge(i, t, dt);
                        double current = (charge-charges[i])/dt;
                        currentsList += current;
                        charges[i] = charge;
                        chargesList += charge;
                        ldosesEList += ldosE;
                        if (i != atoms.length - 1){
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
                    for (Atom a : atoms) {
                        int i = a.getID();
                        for (int sigma = 0; sigma < sigmaDim; sigma++) {
                            countDynamicParameters(i, sigma, t, dt, constant,
                                    ldosArray, normalisationArray,
                                    TDOStemp, integralEnergy, atomsBindings[i]);
                        }
                    }
                    if (time >= starTimeFrom && t % everyT == 0){
                        for (int e = 0; e < ldosArray.size(); e++) {
                            ldosList.println(ldosArray.get(e));
                        }
                        ldosList.println();
                    }
                    String timeToDisplay = String.format("%.2f\n", time);
                    modeller.getTimeEvolutionButton().setText("Cancel (" + timeToDisplay + ")");
                }
                TDOSWriter.close();
                sumldosF.close();
                if (isInterupted.getValue()) {
                    isInterupted.neg();
                } else {
                    tmax += numOfTimeSteps;
                }
                if (par.getNumOfSubSteps() > 1){
                    tmax = 0;
                }
                chargeList.println();
                currentList.println();
                ldosEList.println();
                ldosList.println();
            }
        }
        modeller.getTimeEvolutionButton().setImageIcon(new ImageIcon(TIME_EVOLUTION_BUTTON_IMAGE_PATH));
        modeller.getTimeEvolutionButton().setText("Count time evolution");
        modeller.getApplyToAllButton().setIcon(null);
        modeller.getApplyToAllButton().setEnabled(false);
        modeller.getApplyToAllButton().setText("");
        Instant ends = Instant.now();
        System.out.println("########");
        System.out.println("Execution time: " + Duration.between(starts, ends));
        ldosList.close();
        chargeList.close();
        currentList.close();
        ldosEList.close();
    }
    //endregion

    //region data reading
    public void readData(String paramID){
        readData(paramID, false);
    }
    public void readData(String paramID, boolean initializeMatrices){
        this.par = gp.getParamByID(paramID);
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
        this.n0 = new Hashtable<>();
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
        energies = new Hashtable<>();
        for (Atom a : par.getAtoms()){
            int i = a.getID();
            energies.put(i, a.getE());
            parser.parseExpression(a.getN0());
            double value = parser.getValue();
            n.put(i, value);
            n0.put(i, value);
            isSpinOrbit &= a.getParsedCorrelation() > 0;
        }
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
        this.n0 = new Hashtable<>();

        this.sumOfCharges = new double[numOfAtoms];
        for (Electrode e :par.getElectrodes())
        {
            int dim = (int)(gp.getElectrodesWidth()/e.getDoubleDE(dE));
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
                            int dim = (int) (gp.getElectrodesWidth() / par.getElectrodes().get(n).getDoubleDE(dE));
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
        double dt =gp.getDt();
        ArrayList<Bound> bounds = par.getBoundsByAtomID(i);
        double prevTime = time(t - 1, dt);
        switch (k){
            case 0:
                for (Bound b : bounds){
                    second = b.getAnotherAtomID(i);
                    U.put(second + "",Ut_ij[T][second][j]);
                }
                if (par.getElectrodesByAtomID(i).size() > 0){
                    U.put(id, Ut_ij[T][i][j]);
                }
                k1[i][j] = function(i, prevTime, U);
                break;
            case 1:
                for (Bound b : bounds){
                    second = b.getAnotherAtomID(i);
                    U.put(second + "",Ut_ij[T][second][j].plus(k1[second][j].times(dt / 2)));
                }
                if (par.getElectrodesByAtomID(i).size() > 0){
                    U.put(id, Ut_ij[T][i][j].plus(k1[i][j].times(dt/2)));
                }
                k2[i][j] = function(i, prevTime + dt / 2,U);
                break;
            case 2:
                for (Bound b : bounds){
                    second = b.getAnotherAtomID(i);
                    U.put(second + "",Ut_ij[T][second][j].plus(k2[second][j].times(dt / 2)));
                }
                if (par.getElectrodesByAtomID(i).size() > 0){
                    U.put(id, Ut_ij[T][i][j].plus(k2[i][j].times(dt/2)));
                }
                k3[i][j] = function(i, prevTime + dt / 2, U);
                break;
            default:
                for (Bound b : bounds){
                    second = b.getAnotherAtomID(i);
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
        ArrayList<Bound> bounds = par.getBoundsByAtomID(i);
        int anotherAtom;
        for (Bound b : bounds){
            anotherAtom = b.getAnotherAtomID(i);
            result = result.plus(Complex.I.times(exp_i((Ei[i] - Ei[anotherAtom]) * time)).times(U.get(anotherAtom + "")).times(Double.parseDouble(b.getCoupling())));
        }
        for (Electrode el : par.getElectrodesByAtomID(i)){
            result = result.plus(U.get(id).times(-el.getParsedCoupling()/ 2.0));
        }
        return result;
    }

    //region Rk4

    public void countUt_ik(Electrode electrode, Complex[][][][][] Ut_ik, int t, int T, double dt, Complex Ek,
                           double prevTime, double electrodesWidth, ArrayList<Bound>[] atomsBindings,
                           Complex[][] integralEnergy,
                           double[] Vsf, double f[],
                           double[] atomsElectrodes){

//        if (Globals.SURFACE_ELECTRODE_ID == (electrode)){
//            electrodeID = new Electrode(-1,null,null,par.getSurfaceCoupling(),gp.getdE(),Globals.SURFACE_ELECTRODE_ID,null);
//        }
        double energyStep = dE;
        if (electrode != null){
            energyStep = electrode.getDoubleDE(dE);
        }
        int numberOfEnergySteps = (int)(electrodesWidth/energyStep);

        Complex[][][] kVec = new Complex[3][numOfAtoms][2];
        double calculatedVk = sqrt(gp.getElectrodesWidth() * electrode.getParsedCoupling() / ( 2 * PI));
        int elAtID = electrode.getIntAtomIndex();
        int sigmaDim = 1;
        if (isSpinOrbit){
            sigmaDim = 2;
        }
        for (int e = 0; e < numberOfEnergySteps; e++) {
            for (int sigma_k = 0; sigma_k < sigmaDim; sigma_k++) {
                Complex[][] array = Ut_ik[T][e][sigma_k];
                Ek = exp_i((toEnergy(e, energyStep, gp) * prevTime));
                for (int k = 0; k < 4; k++) {
                    for (int nSigma = 0; nSigma < sigmaDim; nSigma++) {
                        for (int i = 0; i < numOfAtoms; i++) {
                            double Vk;
                            if (i == elAtID) {
                                Vk = calculatedVk;
                            } else {
                                Vk = 0;
                            }
                            if (k == 0) {
                                kVec[k][i][nSigma] = functionUik(i, nSigma, sigma_k, k, dt, Ek, array, null, Vk, f[i],  Vsf[i],
                                        integralEnergy, atomsElectrodes, atomsBindings[i], sigmaDim);
                            } else if (k < 3) {
                                kVec[k][i][nSigma] = functionUik(i, nSigma, sigma_k, k, dt, Ek, array, kVec[k - 1], Vk, f[i],  Vsf[i],
                                        integralEnergy, atomsElectrodes, atomsBindings[i], sigmaDim);
                            } else {
                                Complex kVec3 = functionUik(i, nSigma, sigma_k, k, dt, Ek, array, kVec[k - 1], Vk, f[i],  Vsf[i],
                                        integralEnergy, atomsElectrodes, atomsBindings[i], sigmaDim);
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
    public TimeEvolutionHelper(NanoModeller modeller, Flag isInterupted){
        this.modeller = modeller;
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
                                  Complex[][] kVec, double Vk,  double f, double Vsf,
                                  Complex[][] integralEnergy,
                                  double[] atomsElectrodes, ArrayList<Bound> atomBindings, int sigmaDim) {

        Complex result = Complex.ZERO;
        double gammaIJ = 0;
        double gammaHalf = atomsElectrodes[i];//0.5;//el.getDoubleCoupling()/ 2.0;
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
        for (Bound b : atomBindings){
            int j = b.getAnotherAtomID(i);
            double Vij = b.getParsedCoupling()*b.getParsedPerturbation();
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
                                       ArrayList<String> normalisationArray,
                                       double[] TDOStemp, Complex[][] integralEnergy,
                                       ArrayList<Bound> atomBindings){
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
                if (atoms[i].isSaveNormalisation()) {
                    String val = normalisationArray.get(e);
                }
                if (e % everyE == 0 && t % everyT == 0  && time >= starTimeFrom) {
                    String val = ldosArray.get(e / everyE);
                    ldosArray.set(e / everyE, val + "," + ldos);
                }

            }
        }
        integralEnergy[i][sigmaN] = integralEnergy[i][sigmaN].times(exp_i(Ei[i] * dt));
        for (Bound b : atomBindings){
            int j = b.getAnotherAtomID(i);
            double u = b.getParsedCorrelationCoupling();
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
            resultN +=  n0.get(j)*Math.pow(Ut_ij[t % 2][i][j].magnitude(), 2);
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
