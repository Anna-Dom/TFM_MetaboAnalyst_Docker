/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package metaboanalyst.controllers;

import metaboanalyst.utils.DataUtils;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import metaboanalyst.controllers.enrich.IntegProcessBean;
import metaboanalyst.rwrappers.RDataUtils;
import metaboanalyst.rwrappers.SearchUtils;
import org.rosuda.REngine.Rserve.RConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@ViewScoped
@Named("procBean")
public class ProcessBean implements Serializable {

    private final ApplicationBean1 ab = (ApplicationBean1) DataUtils.findBean("applicationBean1");
    private final SessionBean1 sb = (SessionBean1) DataUtils.findBean("sessionBean1");
    private static final Logger LOGGER = LogManager.getLogger(GenericControllers.class);
    private String msgText;

    private int filterCutoff = -1;

    public int getFilterCutoff() {
        if (filterCutoff == -1) {
            filterCutoff = sb.getDefaultFilterCutoff();
        }
        return filterCutoff;
    }

    public void setFilterCutoff(int filterCutoff) {
        this.filterCutoff = filterCutoff;
    }

    public String getMsgText() {
        return msgText;
    }
    private boolean missingDisabled = true;

    public boolean isMissingDisabled() {
        return missingDisabled;
    }

    public void setMissingDisabled(boolean missingDisabled) {
        this.missingDisabled = missingDisabled;
    }

    private boolean editBnDisabled = false;

    public boolean isEditBnDisabled() {
        return editBnDisabled;
    }

    public void setEditBnDisabled(boolean editBnDisable) {
        this.editBnDisabled = editBnDisable;
    }

    private String selectedMetaData = "Meta-data";

    public String getSelectedMetaData() {
        return selectedMetaData;
    }

    public void setSelectedMetaData(String selectedMetaData) {
        this.selectedMetaData = selectedMetaData;
    }

    public void updateSmplGroup() {
        RDataUtils.setSampleGroups(sb.getRConnection(), sb.getSampleBeans(), selectedMetaData);
        performSanityCheck();
    }

    private boolean proceedBnDisabled = false;

    public boolean isProceedBnDisabled() {
        return proceedBnDisabled;
    }

    public void setProceedBnDisabled(boolean bnDisabled) {
        this.proceedBnDisabled = bnDisabled;
    }

    private boolean doQCFiltering = false;

    public boolean isDoQCFiltering() {
        return doQCFiltering;
    }

    public void setDoQCFiltering(boolean doQCFiltering) {
        this.doQCFiltering = doQCFiltering;
    }

    private int qcCutoff = 25;

    public int getQcCutoff() {
        return qcCutoff;
    }

    public void setQcCutoff(int qcCutoff) {
        this.qcCutoff = qcCutoff;
    }

    @PostConstruct
    public void performSanityCheck() {

        RConnection RC = sb.getRConnection();
        ArrayList<String> msgVec = new ArrayList();
        String[] msgArray = null;

        try {
            switch (sb.getAnalType()) {
                case "mummichog":
                    if (RDataUtils.sanityCheckMummichogData(RC)) {
                        msgVec.add("Checking data content ...passed.");
                        msgArray = RDataUtils.getSanityCheckMessage(RC);
                        int featureNum = RDataUtils.getOrigFeatureNumber(RC);
                        int sampleNum = RDataUtils.getSampleSize(RC);
                        sb.setupDataSize(featureNum, sampleNum);
                        proceedBnDisabled = false;
                    } else {
                        msgVec.add("Checking data content ...failed.");
                        msgArray = RDataUtils.getErrorMsgs(RC);
                        proceedBnDisabled = true;
                    }
                    if (sb.getDataType().equals("mass_table")) {
                        missingDisabled = !RDataUtils.containMissing(RC);
                    } else {
                        editBnDisabled = true;
                    }
                    break;
                case "pathinteg":
                    editBnDisabled = true;
                    IntegProcessBean ipb = (IntegProcessBean) DataUtils.findBean("integProcesser");
                    if (!ipb.getDatatype().equals("peak")) {
                        return;
                    }
                    if (RDataUtils.sanityCheckMummichogData(RC)) {
                        msgVec.add("Checking data content ...passed.");
                        msgArray = RDataUtils.getSanityCheckMessage(RC);
                        proceedBnDisabled = false;
                    } else {
                        msgVec.add("Checking data content ...failed.");
                        msgArray = RDataUtils.getErrorMsgs(RC);
                        proceedBnDisabled = true;
                    }
                    break;
                default:
                    int res = RDataUtils.sanityCheckData(RC);
                    if (res == 1) {
                        msgVec.add("Checking data content ...passed.");
                        msgArray = RDataUtils.getSanityCheckMessage(RC);
                        int featureNum = RDataUtils.getOrigFeatureNumber(RC);
                        int sampleNum = RDataUtils.getSampleSize(RC);
                        sb.setupDataSize(featureNum, sampleNum);
                        sb.settingRoc1Col(featureNum);
                        proceedBnDisabled = false;
                        missingDisabled = !RDataUtils.containMissing(RC);
                    } else {
                        msgVec.add("Checking data content ...failed.");
                        msgArray = RDataUtils.getErrorMsgs(RC);
                        editBnDisabled = (res == -2);
                        proceedBnDisabled = true;
                    }
                    break;
            }

        } catch (Exception e) {
            msgVec.add("Checking data content ...failed.");
            //e.printStackTrace();
            LOGGER.error("performSanityCheck", e);
        }

        if (sb.getAnalType().equals("mf")) {
            editBnDisabled = true;
        }
        msgVec.addAll(Arrays.asList(msgArray));
        String msg;
        msg = "<table face=\"times\" size = \"3\">";
        if (!sb.getAnalType().equals("pathinteg")) {
            msg = msg + "<tr><th> Data processing information: " + "</th></tr>";
        }
        for (int i = 0; i < msgVec.size(); i++) {
            msg = msg + "<tr><td align=\"left\">" + msgVec.get(i) + "</td></tr>";
        }
        msg = msg + "</table>";
        msgText = msg;
    }

    public String imputeButton_action() {
        sb.setMultiGroup(RDataUtils.getGroupNumber(sb.getRConnection()) > 2);
        return "Missing value";
    }

    public String skipButton_action_default() {

        if (sb.getDataType().equals("mass_all")) {
            return "mzlibview";
        }

        RConnection RC = sb.getRConnection();
        String analType = sb.getAnalType();

        RDataUtils.replaceMin(RC);
        if (!analType.equals("mf")) {
            sb.setMultiGroup(RDataUtils.getGroupNumber(RC) > 2);
        }
        //sb.setPageInit("sanity");
        sb.setIntegChecked(true);
        sb.setSmallSmplSize(RDataUtils.isSmallSampleSize(RC));
        //sb.setupDataOpts();

        if (analType.equals("mf")) {
            return "Metadata check";
        }

        //for targeted metabolomics, need to perform name check
        if (analType.equals("msetqea") || analType.equals("pathqea")) {
            if (sb.getFeatType().equals("lipid")) {
                SearchUtils.crossReferenceExactLipid(sb.getRConnection(), sb.getCmpdIDType());
            } else {
                SearchUtils.crossReferenceExact(sb.getRConnection(), sb.getCmpdIDType());
            }
            return "Name check";
        }
        //dspc
        if (analType.equals("network") & sb.getCmpdIDType().equals("name")) {
            SearchUtils.crossReferenceExact(sb.getRConnection(), sb.getCmpdIDType());
            return "Name check";
        }

        return toFilterView();
    }

    private String filterOpt = "iqr";

    public String getFilterOpt() {
        return filterOpt;
    }

    public void setFilterOpt(String filterOpt) {
        this.filterOpt = filterOpt;
    }

    public String filterProceed_action() {
        RConnection RC = sb.getRConnection();
        String doQC = "F";
        if (!filtered) {
            RDataUtils.filterVariable(RC, "none", -1, doQC, qcCutoff, sb.isPrivileged() ? "T" : "F");
        }
        return "Normalization";
    }

    public void filterButton_action() {
        RConnection RC = sb.getRConnection();
        String doQC = "F";
        if (doQCFiltering) {
            doQC = "T";
        }

        int res = RDataUtils.filterVariable(RC, filterOpt, filterCutoff, doQC, qcCutoff, sb.isPrivileged() ? "T" : "F");
        if (res == 0) {
            DataUtils.updateMsg("Error", RDataUtils.getErrMsg(RC));
        } else {
            DataUtils.updateMsg("OK", RDataUtils.getCurrentMsg(RC));
            int featureNum = RDataUtils.getFiltFeatureNumber(RC);
            sb.updateFeatureNum(featureNum);
            filtered = true;
        }
    }
    private boolean filtered = false;

    public boolean isFiltered() {
        return filtered;
    }

    public void setFiltered(boolean filtered) {
        this.filtered = filtered;
    }

    private String nmrAlignText = "";

    public String getNmrAlignText() {
        return nmrAlignText;
    }

    public void setNmrAlignText(String nmrAlignText) {
        this.nmrAlignText = nmrAlignText;
    }

    public String performNMRPeakAlignment() {
        RConnection RC = sb.getRConnection();
        RDataUtils.processPeakList(RC, 0.03);
        setNMRpeakProcTable();
        return null;
    }

    private void setNMRpeakProcTable() {
        String[] msgArray = RDataUtils.getPeaklistProcMessage(sb.getRConnection());
        String msg = "<table face=\"times\" size = \"3\">"
                + "<tr><th> NMR peak processing information </th></tr>";
        for (String msgArray1 : msgArray) {
            msg = msg + "<tr><td align=\"left\">" + msgArray1 + "</td></tr>";
        }
        msg = msg + "</table>";
        nmrAlignText = msg;
    }

    public String nmrNextBn_action() {
        sb.setDataProcessed(true);
        return "Data check";
    }

    private double mzThresh = 0.025;
    private double rtThresh = 30;

    public double getMzThresh() {
        return mzThresh;
    }

    public void setMzThresh(double mzThresh) {
        this.mzThresh = mzThresh;
    }

    public double getRtThresh() {
        return rtThresh;
    }

    public void setRtThresh(double rtThresh) {
        this.rtThresh = rtThresh;
    }

    private String msPeakText = "";

    public String getMsPeakText() {
        return msPeakText;
    }

    public void setMsPeakText(String msPeakText) {
        this.msPeakText = msPeakText;
    }

    private void setMSpeakProcTable() {
        String[] msgArray = RDataUtils.getPeaklistProcMessage(sb.getRConnection());
        String msg = "<table face=\"times\" size = \"3\">"
                + "<tr><th> MS peak processing information </th></tr>";
        for (String msgArray1 : msgArray) {
            msg = msg + "<tr><td align=\"left\">" + msgArray1 + "</td></tr>";
        }
        msg = msg + "</table>";
        msPeakText = msg;
    }

    public String msPeakAlignBn_action() {
        RDataUtils.processPeakList(sb.getRConnection(), mzThresh, rtThresh);
        setMSpeakProcTable();
        sb.setMsPeakAligned(true);
        return null;
    }

    public String msPeakNextBn_action() {
        sb.setDataProcessed(true);
        return "Data check";
    }

    private boolean removeMissing = true;

    public boolean isRemoveMissing() {
        return removeMissing;
    }

    public void setRemoveMissing(boolean removeMissing) {
        this.removeMissing = removeMissing;
    }

    private int missingPercent = 50;

    public int getMissingPercent() {
        return missingPercent;
    }

    public void setMissingPercent(int missingPercent) {
        this.missingPercent = missingPercent;
    }

    private String missingImputeOpt = "min";

    public String getMissingImputeOpt() {
        return missingImputeOpt;
    }

    public void setMissingImputeOpt(String missingImputeOpt) {
        this.missingImputeOpt = missingImputeOpt;
    }

    private String replaceVarOpt;

    public String getReplaceVarOpt() {
        return replaceVarOpt;
    }

    public void setReplaceVarOpt(String replaceVarOpt) {
        this.replaceVarOpt = replaceVarOpt;
    }

    private String imputeAlgOpt;

    public String getImputeAlgOpt() {
        return imputeAlgOpt;
    }

    public void setImputeAlgOpt(String imputeAlgOpt) {
        this.imputeAlgOpt = imputeAlgOpt;
    }

    public String performMissingImpute() {
        RConnection RC = sb.getRConnection();
        if (removeMissing) {
            double percent = missingPercent / 100.0;
            RDataUtils.removeVariableByPercent(RC, percent);
        }

        String method = missingImputeOpt;
        switch (missingImputeOpt) {
            case "replaceCol":
                method = replaceVarOpt;
                break;
            case "impute":
                method = imputeAlgOpt;
                break;
        }
        RDataUtils.imputeVariable(RC, method);
        sb.setIntegChecked(true);
        sb.setSmallSmplSize(RDataUtils.isSmallSampleSize(RC));

        String analType = sb.getAnalType();

        if (analType.equals("mf")) {
            return "Metadata check";
        }

        if (analType.equals("msetqea") || analType.equals("pathqea")) {
            if (sb.getFeatType().equals("lipid")) {
                SearchUtils.crossReferenceExactLipid(sb.getRConnection(), sb.getCmpdIDType());
            } else {
                SearchUtils.crossReferenceExact(sb.getRConnection(), sb.getCmpdIDType());
            }
            return "Name check";
        }

        return toFilterView();
    }

    private String toFilterView() {
        //filter is recommended, even default is 0 for advanced users
        int featureNum = sb.getFeatureNumber();
        if (featureNum > 250) {
            return "Data filter";
        } else if (!sb.getDataType().equalsIgnoreCase("conc")) {
            return "Data filter";
        } else {
            return "Normalization";
        }
    }

    public void prepareFilterView() {

        int featureNum = sb.getFeatureNumber();
        int defaultFilterCutoff;

        if (featureNum < 250) {
            defaultFilterCutoff = 5;
        } else if (featureNum < 500) {
            defaultFilterCutoff = 10;
        } else if (featureNum < 1000) {
            defaultFilterCutoff = 25;
        } else {
            defaultFilterCutoff = 40;
        }

        sb.setDefaultFilterCutoff(defaultFilterCutoff);

        //control for large data on public server
        if (!sb.isPrivileged()) {
            if (featureNum > 2500 & sb.getAnalType().equals("power")) {
                sb.setFilterMin(defaultFilterCutoff);
            } else if (featureNum > 5000) { // mandatory
                sb.setFilterMin(defaultFilterCutoff);
            } else {
                sb.setFilterMin(0);
            }
        } else {
            sb.setFilterMin(0);
        }

    }

}
