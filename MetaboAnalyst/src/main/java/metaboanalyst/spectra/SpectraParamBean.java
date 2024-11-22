/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package metaboanalyst.spectra;

import java.io.Serializable;
import java.util.Arrays;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import metaboanalyst.controllers.SessionBean1;
import metaboanalyst.rwrappers.RDataUtils;
import metaboanalyst.rwrappers.RSpectraUtils;
import metaboanalyst.utils.DataUtils;
import org.primefaces.event.TransferEvent;
import org.primefaces.model.DualListModel;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.Rserve.RConnection;

/**
 *
 * @author qiang
 */

@SessionScoped
@Named("spectraParamer")
public class SpectraParamBean implements Serializable{
    
    private final SessionBean1 sb = (SessionBean1) DataUtils.findBean("sessionBean1");        
        
    private boolean buttonEnable = false;
    private boolean buttonEnableAJ = true;
    private boolean buttonEnableBS = true; //BS: BLANK Subtraction

    public SpectraParamBean() {
        
    }

    public boolean isButtonEnable() {
        return buttonEnable;
    }

    public void setButtonEnable(boolean buttonEnable) {
        this.buttonEnable = buttonEnable;
    }

    public boolean isButtonEnableAJ() {
        return buttonEnableAJ;
    }

    public void setButtonEnableAJ(String meth) {
        if("customized".equals(meth)){            
            this.buttonEnableAJ = true;
        } else {
            this.buttonEnableAJ = false;
        }        
    }

    public boolean isButtonEnableBS() {
        return buttonEnableBS;
    }

    public void setButtonEnableBS(boolean buttonEnableBS) {
        this.buttonEnableBS = buttonEnableBS;
    }
        
    // define the 'method' of the whole pipeline
    private String meth = "customized";

    public String getMeth() {        
        if ("auto".equals(meth)) {
            setButtonEnable(true);
        } else {
            setButtonEnable(false);
        }
        return meth;
    }

    public void setMeth(String meth) {
        
        if ("auto".equals(meth)) {
            setButtonEnable(true);
        } else {
            setButtonEnable(false);
        }
        this.meth = meth;
    }

    private String platformOpt = "general";

    public String getPlatformOpt() {
        return platformOpt;
    }

    public void setPlatformOpt(String platformOpt) {
        this.platformOpt = platformOpt;
    }

    // update the initial parameters according to the platform information
    public void updatePlatformParam() throws REXPMismatchException {
        
        RConnection RC = sb.getRConnection();
        String[][] param_array = RDataUtils.initialPlatformParams(RC, platformOpt);

        if (param_array[0][0].equals("Peak_method")) {
            setPeakmeth(param_array[1][0]);
        }

        if (param_array[0][1].equals("RT_method")) {
            setRtmeth(param_array[1][1]);
        }

        if (param_array[0][2].equals("ppm")) {
            setPpm(Double.parseDouble(param_array[1][2]));
        }

        if (param_array[0][3].equals("min_peakwidth")) {
            setMin_peakwidth(Double.parseDouble(param_array[1][3]));
        }

        if (param_array[0][4].equals("max_peakwidth")) {
            setMax_peakwidth(Double.parseDouble(param_array[1][4]));
        }

        if (param_array[0][5].equals("mzdiff")) {
            setMzdiff(Double.parseDouble(param_array[1][5]));
        }

        if (param_array[0][6].equals("snthresh")) {
            setSnthresh(Double.parseDouble(param_array[1][6]));
        }

        if (param_array[0][7].equals("noise")) {
            setNoise(Double.parseDouble(param_array[1][7]));
        }

        if (param_array[0][8].equals("prefilter")) {
            setPrefilter(Double.parseDouble(param_array[1][8]));
        }

        if (param_array[0][9].equals("value_of_prefilter")) {
            setValue_of_prefilter(Double.parseDouble(param_array[1][9]));
        }

        if (param_array[0][10].equals("bw")) {
            setBw(Double.parseDouble(param_array[1][10]));
        }

        System.out.println("== Parameters of" + platformOpt + " has been set !==");

    }
    
    // Peak Optimization Parameter are defined below
    // update annotation parameters - rt range
    private double rttrimup = 200.0;

    public double getRttrimup() {
        return rttrimup;
    }

    public void setRttrimup(double rttrimup) {
        this.rttrimup = rttrimup;
    }

    private double rttrimdown = 0.0;

    public double getRttrimdown() {
        return rttrimdown;
    }

    public void setRttrimdown(double rttrimdown) {
        this.rttrimdown = rttrimdown;
    }

    // update annotation parameters - mz range
    private double mztrimup = 250.0;

    public double getMztrimup() {
        return mztrimup;
    }

    public void setMztrimup(double mztrimup) {
        this.mztrimup = mztrimup;
    }

    private double mztrimdown = 150.0;

    public double getMztrimdown() {
        return mztrimdown;
    }

    public void setMztrimdown(double mztrimdown) {
        this.mztrimdown = mztrimdown;
    }

    // Peak Processing Parameters are defined below
    // update method parameters
    private String peakmeth = "centWave";

    public String getPeakmeth() {
        return peakmeth;
    }

    public void setPeakmeth(String peakmeth) {
        this.peakmeth = peakmeth;
    }

    private String rtmeth = "loess";

    public String getRtmeth() {
        return rtmeth;
    }

    public void setRtmeth(String rtmeth) {
        this.rtmeth = rtmeth;
    }

    // update annotation parameters - polarity
    private String polarity = "negative";

    public String getPolarity() {
        return polarity;
    }

    public void setPolarity(String polarity) {
        this.polarity = polarity;
    }

    // update annotation parameters - fwhmThresh
    private double fwhmThresh = 0.6;

    public double getFwhmThresh() {
        return fwhmThresh;
    }

    public void setFwhmThresh(double fwhmThresh) {
        this.fwhmThresh = fwhmThresh;
    }

    // update annotation parameters - Mzmabsmiso
    private double mzmabsmiso = 0.005;

    public double getMzmabsmiso() {
        return mzmabsmiso;
    }

    public void setMzmabsmiso(double mzmabsmiso) {
        this.mzmabsmiso = mzmabsmiso;
    }

    // update annotation parameters - Max_charge
    private int max_charge = 2;

    public int getMax_charge() {
        return max_charge;
    }

    public void setMax_charge(int max_charge) {
        this.max_charge = max_charge;
    }

    // update annotation parameters - Max_charge
    private int max_iso = 2;

    public int getMax_iso() {
        return max_iso;
    }

    public void setMax_iso(int max_iso) {
        this.max_iso = max_iso;
    }

    // update annotation parameters - corr_eic_th
    private double corr_eic_th = 0.85;

    public double getCorr_eic_th() {
        return corr_eic_th;
    }

    public void setCorr_eic_th(double corr_eic_th) {
        this.corr_eic_th = corr_eic_th;
    }

    // update annotation parameters - mz_abs_add
    private double mz_abs_add = 0.001;

    public double getMz_abs_add() {
        return mz_abs_add;
    }

    public void setMz_abs_add(double mz_abs_add) {
        this.mz_abs_add = mz_abs_add;
    }

    // update annotation parameters - mz_abs_add
    private double bw = 10;

    public double getBw() {
        return bw;
    }

    public void setBw(double bw) {
        this.bw = bw;

    }

    // update annotation parameters - minFraction
    private double minFraction = 0.8;

    public double getMinFraction() {
        return minFraction;
    }

    public void setMinFraction(double minFraction) {
        this.minFraction = minFraction;
    }

    // update annotation parameters - mz_abs_add
    private int minSamples = 1;

    public int getMinSamples() {
        return minSamples;
    }

    public void setMinSamples(int minSamples) {
        this.minSamples = minSamples;
    }

    // update annotation parameters - mz_abs_add
    private int maxFeatures = 100;

    public int getMaxFeatures() {
        return maxFeatures;
    }

    public void setMaxFeatures(int maxFeatures) {
        this.maxFeatures = maxFeatures;
    }

    // update annotation parameters - mz_abs_add
    private int integrate = 1;

    public int getIntegrate() {
        return integrate;
    }

    public void setIntegrate(int integrate) {
        this.integrate = integrate;
    }

    // update annotation parameters - mz_abs_add
    private int extra = 1;

    public int getExtra() {
        return extra;
    }

    public void setExtra(int extra) {
        this.extra = extra;
    }

    // update annotation parameters - mz_abs_add
    private double span = 0.25;

    public double getSpan() {
        return span;
    }

    public void setSpan(double span) {
        this.span = span;
    }

    // update annotation parameters - mz_abs_add
    private int profStep = 1;

    public int getProfStep() {
        return profStep;
    }

    public void setProfStep(int profStep) {
        this.profStep = profStep;
    }

    // update annotation parameters - ppm
    private double ppm = 5;

    public double getPpm() {
        return ppm;
    }

    public void setPpm(double ppm) {
        this.ppm = ppm;
    }

    // update annotation parameters - min_peakwidth
    private double min_peakwidth = 5;

    public double getMin_peakwidth() {

        return min_peakwidth;
    }

    public void setMin_peakwidth(double min_peakwidth) {
        this.min_peakwidth = min_peakwidth;

    }

    // update annotation parameters - max_peakwidth
    private double max_peakwidth = 30;

    public double getMax_peakwidth() {
        return max_peakwidth;
    }

    public void setMax_peakwidth(double max_peakwidth) {
        this.max_peakwidth = max_peakwidth;
    }

    // update annotation parameters - mzdiff
    private double mzdiff = 0.01;

    public double getMzdiff() {
        return mzdiff;
    }

    public void setMzdiff(double mzdiff) {
        this.mzdiff = mzdiff;
    }

    // update annotation parameters - snthresh
    private double snthresh = 10;

    public double getSnthresh() {
        return snthresh;
    }

    public void setSnthresh(double snthresh) {
        this.snthresh = snthresh;
    }

    // update annotation parameters - noise
    private double noise = 1000;

    public double getNoise() {
        return noise;
    }

    public void setNoise(double noise) {
        this.noise = noise;
    }

    // update annotation parameters - prefilter
    private double prefilter = 3;

    public double getPrefilter() {
        return prefilter;
    }

    public void setPrefilter(double prefilter) {
        this.prefilter = prefilter;
    }

    // update annotation parameters - value_of_prefilter
    private double value_of_prefilter = 100;

    public double getValue_of_prefilter() {
        return value_of_prefilter;
    }

    public void setValue_of_prefilter(double value_of_prefilter) {
        this.value_of_prefilter = value_of_prefilter;
    }

    //params for mathedFilter
    private double fwhm = 30;

    public double getFwhm() {
        return fwhm;
    }

    public void setFwhm(double fwhm) {
        this.fwhm = fwhm;
    }

    private double sigma = 12.74;

    public double getSigma() {
        return sigma;
    }

    public void setSigma(double sigma) {
        this.sigma = sigma;
    }

    private double steps = 2;

    public double getSteps() {
        return steps;
    }

    public void setSteps(double steps) {
        this.steps = steps;
    }

    private double max = 10;

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
    }
    
    private boolean rmConts = true;

    public boolean isRmConts() {
        return rmConts;
    }

    public void setRmConts(boolean rmConts) {
        this.rmConts = rmConts;
    }
    
    private boolean blksub = false;

    public boolean isBlksub() {
        return blksub;
    }

    public void setBlksub(boolean blksub) {
        this.blksub = blksub;
    }
        
    //params for adducts customization
    private String adducts = "NULL";

    public String getAdducts() {
        return adducts;
    }

    public void setAdducts(String adducts) {
        this.adducts = adducts;
    }
    private String[] newsadducts;
    private String[] newtadducts;
    private DualListModel<String> adductItems;
    
    public void initializeAdductList() {
        String[] adds = RSpectraUtils.readAdductList(sb.getRConnection(), polarity);
        this.newtadducts = Arrays.copyOfRange(adds, 0, 7);
        this.newsadducts = Arrays.copyOfRange(adds, 8, adds.length - 1);
    }
       
    public DualListModel<String> getAdductItems() {
        if(newtadducts == null || newsadducts == null){
            initializeAdductList();
        }        
        adductItems = new DualListModel(Arrays.asList(newsadducts), Arrays.asList(newtadducts));
        return adductItems;
    }
    
    public void setAdductItems(DualListModel<String> adductItems) {
        this.adductItems = adductItems;
    }
    
    public void updateAdductParam(){
        for(String aditem : newtadducts){
            if(adducts.equals("NULL")) {
                adducts = aditem;
            } else {
                adducts = adducts + "|" + aditem;
            }
        }
    }
    
    public void doTransfer(TransferEvent event) {
        
        event.getItems().forEach((item) -> {
            if(event.isAdd()){
                newtadducts = addX(newtadducts, item.toString());
            } else if(event.isRemove()) {
                newtadducts = deleteX(newtadducts, item.toString());
            }
        });
    }
    
    public static String[] addX(String[] arr, String x) {
        int n = arr.length;
        String newarr[] = new String[n + 1];
        System.arraycopy(arr, 0, newarr, 0, n);
        newarr[n] = x;
        return newarr;
    }
    
    public static String[] deleteX(String[] arr, String x) {
        int n = arr.length;
        String newarr[] = new String[n - 1];
        int num2 =0;
        for (int num = 0; num < n; num ++) {
            if(!arr[num].equals(x)) {
                newarr[num2] = arr[num];
                num2++;
            }
        }        
        return newarr;
    }

}
