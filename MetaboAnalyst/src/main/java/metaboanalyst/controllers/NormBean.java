/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package metaboanalyst.controllers;

import metaboanalyst.utils.DataUtils;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.inject.Named;
import javax.faces.view.ViewScoped;
import javax.faces.model.SelectItem;
import metaboanalyst.controllers.stats.RocAnalBean;
import metaboanalyst.models.SampleBean;
import metaboanalyst.rwrappers.RDataUtils;
import org.rosuda.REngine.Rserve.RConnection;

@ViewScoped
@Named("normBean")
public class NormBean implements Serializable {

    private final SessionBean1 sb = (SessionBean1) DataUtils.findBean("sessionBean1");

    private String rowNormOpt = "NULL";
    private String transNormOpt = "NULL";
    private String scaleNormOpt = "NULL";
    private String refSmpl = null;
    private String refGrp = null;
    private String refVar = null;

    @PostConstruct
    public void preparePrenormData() {
        RDataUtils.initPrenormData(sb.getRConnection());
    }

    public String getRefVar() {
        return refVar;
    }

    public void setRefVar(String refVar) {
        if (sb.isBigFeature()) {//in case user entered value, make sure the feature names is valid
            int res = RDataUtils.validateFeatureName(sb.getRConnection(), refVar);
            if (res == 0) {
                DataUtils.updateMsg("Error", "The feature name cannot be found in your data!");
                return;
            }
        }
        this.refVar = refVar;
        rowNormOpt = "CompNorm";
    }

    public String getRefSmpl() {
        return refSmpl;
    }

    public void setRefSmpl(String refSmpl) {
        this.refSmpl = refSmpl;
        rowNormOpt = "SamplePQN";
    }

    public String getRefGrp() {
        return refGrp;
    }

    public void setRefGrp(String refGrp) {
        this.refGrp = refGrp;
        rowNormOpt = "GroupPQN";
    }

    public String getRowNormOpt() {
        return rowNormOpt;
    }

    public void setRowNormOpt(String rowNormOpt) {
        this.rowNormOpt = rowNormOpt;
    }

    public String getTransNormOpt() {
        return transNormOpt;
    }

    public void setTransNormOpt(String transNormOpt) {
        this.transNormOpt = transNormOpt;
    }

    public String getScaleNormOpt() {
        return scaleNormOpt;
    }

    public void setScaleNormOpt(String scaleNormOpt) {
        this.scaleNormOpt = scaleNormOpt;
    }

    private boolean specNormSpecifed = false;

    public void setSmplSpecNorm() {
        rowNormOpt = "SpecNorm";
        RDataUtils.setSampleNormFactor(sb.getRConnection(), sampleBeans);
        specNormSpecifed = true;
    }

    private List<SampleBean> sampleBeans = null;

    public List<SampleBean> getSampleBeans() {
        if (sampleBeans == null) {
            sampleBeans = createPrenormSampleBeans(sb.getRConnection(), false);
        }
        return sampleBeans;
    }

    private List<SampleBean> createPrenormSampleBeans(RConnection RC, boolean withNA) {
        String[] allSamples = RDataUtils.getPrenormSampleNames(RC);
        String[] allGrps = RDataUtils.getPrenormSmplGroupNames(RC);
        int samSize = allSamples.length;
        List<SampleBean> samNABeans = new ArrayList();
        if (withNA) {
            samNABeans.add(new SampleBean("<Not set>", "NA")); // the first one is NA
        }
        for (int i = 0; i < samSize; i++) {
            samNABeans.add(new SampleBean(allSamples[i], allGrps[i]));
        }
        return samNABeans;
    }
        
    private boolean normPerformed = false;

    public boolean isNormPerformed() {
        return normPerformed;
    }

    public void performDataNormalization() {

        //String specNorm = smplSpecNorm ? "T" : "NULL";
        if (rowNormOpt.equals("SpecNorm") && !specNormSpecifed) {
            DataUtils.updateMsg("Error", "You need to manually specify sample specific norm factors!");
            return;
        }
        if (rowNormOpt.equals("CompNorm") && refVar == null) {
            DataUtils.updateMsg("Error", "You need to manually specify a reference feature for normalization");
            return;
        }
        if (rowNormOpt.equals("GroupPQN") && refGrp == null) {
            DataUtils.updateMsg("Error", "You need to manually specify a reference group for normalization");
            return;
        }
        if (rowNormOpt.equals("SamplePQN") && refSmpl == null) {
            DataUtils.updateMsg("Error", "You need to manually specify a reference sample for normalization");
            return;
        }
        
        RConnection RC = sb.getRConnection();
        String ref;
        switch (rowNormOpt) {
            case "CompNorm":
                ref = refVar;
                break;
            case "GroupPQN":
                ref = refGrp;
                break;
            default:
                ref = refSmpl;
                break;
        }
        
        int res = RDataUtils.normalizeData(RC, rowNormOpt, transNormOpt, scaleNormOpt, ref, includeRatio, ratioNumOpt);

        if (res > 0) {
            //plot the new image
            RDataUtils.plotNormSummaryGraph(sb, sb.getNewImage("norm"), "png", 72);
            RDataUtils.plotSampleNormSummaryGraph(sb, sb.getNewImage("snorm"), "png", 72);
            //now reset all data analysis to default
            sb.setDataNormed(true);
            int featureNum = RDataUtils.getNormFeatureNumber(RC);
            sb.updateFeatureNum(featureNum);
            sb.resetAnalysis();
            if (sb.getAnalType().equals("roc")) {
                RocAnalBean rocAnalBean = (RocAnalBean) DataUtils.findBean("rocAnalBean");
                rocAnalBean.resetData();
            }
            DataUtils.updateMsg("OK", "You can click <b>View Result</b> button to view the effect, or <b>Proceed</b> button to analysis page!");
            normPerformed = true;
        } else {
            DataUtils.updateMsg("Error", "Unknown error happened during data normalization process!");
        }
    }

    private boolean includeRatio = false;

    public boolean isIncludeRatio() {
        return includeRatio;
    }

    public void setIncludeRatio(boolean includeRatio) {
        this.includeRatio = includeRatio;
    }

    private int ratioNumOpt = 20;

    public int getRatioNumOpt() {
        return ratioNumOpt;
    }

    public void setRatioNumOpt(int ratioNumOpt) {
        this.ratioNumOpt = ratioNumOpt;
    }

    public void setGrpNmOpts(SelectItem[] grpNmOpts) {
        this.grpNmOpts = grpNmOpts;
    }

    private SelectItem[] varNmOpts = null;

    public SelectItem[] getVarNmOpts() {

        if (varNmOpts == null && !sb.isBigFeature()) {
            String[] varNms = RDataUtils.getPrenormFeatureNames(sb.getRConnection());
            int colLen = varNms.length;
            varNmOpts = new SelectItem[colLen];
            for (int i = 0; i < colLen; i++) {
                varNmOpts[i] = new SelectItem(varNms[i], varNms[i]);
            }
        }
        return varNmOpts;
    }

    private SelectItem[] smplNmOpts = null;

    public SelectItem[] getSmplNmOpts() {
        if (smplNmOpts == null) {
            setupSmplNmOpts();
        }
        return smplNmOpts;
    }

    private void setupSmplNmOpts() {
        String[] smplNms = RDataUtils.getPrenormSampleNames(sb.getRConnection());
        int rowLen = smplNms.length;
        smplNmOpts = new SelectItem[rowLen];
        for (int i = 0; i < rowLen; i++) {
            smplNmOpts[i] = new SelectItem(smplNms[i], smplNms[i]);
        }
    }

    public SelectItem[] getGrpNmOpts() {
        if (grpNmOpts == null) {
            setupGrpNmOpts();
        }
        return grpNmOpts;
    }

    private SelectItem[] grpNmOpts = null;

    private void setupGrpNmOpts() {
        if (!sb.isRegresion()) {
            String[] grpNms = RDataUtils.getPrenormGroupNames(sb.getRConnection());
            int grpLen = grpNms.length;
            grpNmOpts = new SelectItem[grpLen];
            for (int i = 0; i < grpLen; i++) {
                grpNmOpts[i] = new SelectItem(grpNms[i], grpNms[i]);
            }
        } else {
            grpNmOpts = new SelectItem[]{new SelectItem("NULL", "<Not set>")};
        }
    }
}
