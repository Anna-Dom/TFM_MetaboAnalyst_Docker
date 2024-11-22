/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package metaboanalyst.controllers.enrich;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import metaboanalyst.controllers.ApplicationBean1;
import metaboanalyst.controllers.SessionBean1;
import metaboanalyst.controllers.mummichog.MummiAnalBean;
import metaboanalyst.rwrappers.RDataUtils;
import metaboanalyst.rwrappers.REnrichUtils;
import metaboanalyst.rwrappers.RIntegUtils;
import metaboanalyst.utils.DataUtils;
import org.primefaces.model.file.UploadedFile;
import org.rosuda.REngine.Rserve.RConnection;

/**
 *
 * @author qiang
 */
@SessionScoped
@Named("integProcesser")
@JsonIgnoreProperties(ignoreUnknown = true)
public class IntegProcessBean implements Serializable {

    // Section I: Find other bean
    @JsonIgnore
    private final ApplicationBean1 ab = (ApplicationBean1) DataUtils.findBean("applicationBean1");
    @JsonIgnore
    private final SessionBean1 sb = (SessionBean1) DataUtils.findBean("sessionBean1");

    // Section II: variable
    private String datatype = "cmp";
    private double pvalCutoff = 0.01;
    private boolean disabledMumPval = false;
    private boolean loggedIn = false;
    private boolean cmpdMapped = false;
    private boolean geneMapped = false;
    private String integOrg = "NA";
    private String geneList;
    private String cmpdList;
    private boolean useExample = false;
    private String msModeOpt = "negative";
    private double ppmVal = 5.0;
    private String RTOpt = "no";
    private String RankOpt = "pvalue";
    private boolean ECOpt = false;
    private UploadedFile peakFile;
    private String geneIDType = "NA";
    private double rtFrac = 0.02;
    private String version = "v1";
    private final String libVersion = "current";

    //Section II: General Data Process
    public String doJointSubmit1() {

        if (!(geneList == null | geneList.trim().length() == 0)) {
            if (geneIDType.equals("NA")) {
                DataUtils.updateMsg("Error", "Please specify gene ID type!");
                return null;
            }
            handleGeneListUpload();
            if (!geneMapped) {
                DataUtils.updateMsg("Error", "No gene hits!");
                return null;
            }
        }

        if (!(cmpdList == null | cmpdList.trim().length() == 0)) {
            if (sb.getCmpdIDType().equals("na")) {
                DataUtils.updateMsg("Error", "Please specify compound ID type!");
                return null;
            }
            handleCmpdListUpload();
        }

        if (!geneMapped && !cmpdMapped) {
            DataUtils.updateMsg("Error", "Please enter valid input!");
            return null;
        }

        if (!geneMapped && cmpdMapped) {
            DataUtils.updateMsg("Error", "Please use Pathway Analysis module for only metabolite list!");
            return null;
        }
        sb.setDataUploaded();
        return "ID map";

    }

    public String doJointSubmit2() {

        // Process Genes first        
        if (!(geneList == null | geneList.trim().length() == 0)) {
            if (geneIDType.equals("NA")) {
                DataUtils.updateMsg("Error", "Please specify gene ID type!");
                return null;
            }
            handleGeneListUpload();
            if (!geneMapped) {
                DataUtils.updateMsg("Error", "No gene hits!");
                return null;
            }
        }

        // Process Cmpds then
        String fileName;

        if (useExample) {
            fileName = ab.getInternalData("integ_peaks.txt");
        } else {
            try {
                if (peakFile == null) {
                    DataUtils.updateMsg("Error", "Please upload your peak file!");
                    return null;
                }
                if (peakFile.getSize() == 0) {
                    DataUtils.updateMsg("Error", "File is empty!");
                    return null;
                }
                fileName = DataUtils.getJustFileName(peakFile.getFileName());
                DataUtils.uploadFile(peakFile, sb.getCurrentUser().getHomeDir(), null, ab.isOnPublicServer());
            } catch (Exception e) {
                return null;
            }

        }

        sb.setDataUploaded();
        RConnection RC = sb.getRConnection();
        RDataUtils.setPeakFormat(RC, RankOpt);
        RDataUtils.setInstrumentParams(RC, ppmVal, msModeOpt, ECOpt ? "yes" : "no", rtFrac);

        if (RDataUtils.readPeakListData(RC, fileName)) {
            MummiAnalBean mb = (MummiAnalBean) DataUtils.findBean("mummiAnalBean");
            String format = RDataUtils.GetPeakFormat(RC);
            switch (format) {
                case "mpt":
                case "mprt":
                    mb.setDisabledGsea(false);
                    mb.setDisabledMum(false);
                    break;
                case "mp":
                case "mpr":
                    mb.setDisabledGsea(true);
                    mb.setDisabledMum(false);
                    break;
                case "rmp":
                    // single column
                    mb.setDisabledGsea(true);
                    mb.setDisabledMum(false);
                    mb.setDisabledMumPval(true);
                    break;
                default:
                    //rmt or mt or mtr
                    mb.setDisabledGsea(false);
                    mb.setDisabledMum(true);
                    mb.setDisabledMumPval(true);
                    break;
            }
            if (format.equals("mprt") || format.equals("mpr") || format.equals("mrt")) {
                mb.setDisabledV2(true);
            }
            return "ID map";
        } else {
            String err = RDataUtils.getErrMsg(sb.getRConnection());
            DataUtils.updateMsg("Error", "Failed to read in the peak list file." + err);
            return null;
        }
    }

    public String JointSubmit() {
        String returnPage = null;

        if (integOrg.equals("NA")) {
            DataUtils.updateMsg("Error", "Please choose an organism!");
            return null;
        }
        if (!loggedIn) {
            if (!sb.doLogin("conc", "pathinteg", false, false)) {
                DataUtils.updateMsg("Error", "Log in failed. Please check errors in your R codes or the Rserve permission setting!");
                return null;
            } else {
                loggedIn = true;
            }
        }

        RDataUtils.setOrganism(sb.getRConnection(), integOrg);
        if (datatype.equals("cmp")) {
            returnPage = doJointSubmit1();
        } else if (datatype.equals("peak")) {
            returnPage = doJointSubmit2();
        }
        return returnPage;
    }

    private void handleCmpdListUpload() {

        cmpdList = cmpdList.trim();
        cmpdMapped = false;
        RConnection RC = sb.getRConnection();
        int res = RIntegUtils.performCmpdMapping(RC, cmpdList, integOrg, sb.getCmpdIDType());

        String info[] = RDataUtils.getNameCheckMsgs(RC);
        // int state = Integer.parseInt(info[0]);
        String msg = info[1];

        if (res == 1) {
            DataUtils.updateMsg("OK", msg);
            cmpdMapped = true;
        } else {
            DataUtils.updateMsg("Error", msg);
        }
        cmpdList = null;
    }

    private void handleGeneListUpload() {

        geneList = geneList.trim();
        geneMapped = false;
        RConnection RC = sb.getRConnection();
        int res = RIntegUtils.performGeneMapping(RC, geneList, integOrg, geneIDType);
        if (res == 1) {
            DataUtils.updateMsg("OK", RDataUtils.getCurrentMsg(RC));
            geneMapped = true;
        } else {
            DataUtils.updateMsg("Error", RDataUtils.getErrMsg(RC));
        }
        geneList = null;
    }

    //Section III: Mummichog function
    public int PerformMummiInitPrediction() {
        if (ECOpt) {
            version = "v2";
        }
        return PerformMummiCore();
    }

    private int PerformMummiCore() {
        RConnection RC = sb.getRConnection();
        RDataUtils.setPeakEnrichMethod(RC, "mum", version);
        int minMsetNum = 3;
        String pathDBOpt = integOrg + "_kegg";
        if (REnrichUtils.setupMummichogPval(RC, pvalCutoff)) {
            if (!REnrichUtils.performPSEA(RC, pathDBOpt, libVersion, minMsetNum)) {
                String msg = RDataUtils.getErrMsg(sb.getRConnection());
                DataUtils.updateMsg("Error", "There is something wrong with the MS Peaks to Paths analysis: " + msg);
                return 0;
            }
        } else {
            String msg = RDataUtils.getErrMsg(sb.getRConnection());
            DataUtils.updateMsg("Error", msg + "You can click the Submit button again to accept recommended p value.");
            return 0;
        }
        return 1;
    }

    public double findPvalue() {
        double pcutoff;
        pcutoff = REnrichUtils.getDefaultPvalCutoff(sb.getRConnection());
        if (pcutoff == -1) {
            pcutoff = 0.01;
        }
        return pcutoff;
    }

    public String getCmpTabTitle() {
        if (datatype.equals("peak")) {
            return "Peaks Processing";
        }
        return "Compound Name Mapping";
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public double getRtFrac() {
        return rtFrac;
    }

    public void setRtFrac(double rtFrac) {
        this.rtFrac = rtFrac;
    }

    public String getCmpdList() {
        return cmpdList;
    }

    public void setCmpdList(String cmpdList) {
        this.cmpdList = cmpdList;
    }

    public boolean isCmpdMapped() {
        return cmpdMapped;
    }

    public void setCmpdMapped(boolean cmpdMapped) {
        this.cmpdMapped = cmpdMapped;
    }

    public boolean isGeneMapped() {
        return geneMapped;
    }

    public void setGeneMapped(boolean geneMapped) {
        this.geneMapped = geneMapped;
    }

    public String getGeneIDType() {
        return geneIDType;
    }

    public void setGeneIDType(String geneIDType) {
        this.geneIDType = geneIDType;
    }

    public String getGeneList() {
        return geneList;
    }

    public void setGeneList(String geneList) {
        this.geneList = geneList;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }

    public String getIntegOrg() {
        return integOrg;
    }

    public void setIntegOrg(String integOrg) {
        this.integOrg = integOrg;
    }

    public String getMsModeOpt() {
        return msModeOpt;
    }

    public void setMsModeOpt(String msModeOpt) {
        this.msModeOpt = msModeOpt;
    }

    public double getPpmVal() {
        return ppmVal;
    }

    public void setPpmVal(double ppmVal) {
        this.ppmVal = ppmVal;
    }

    public String getRTOpt() {
        return RTOpt;
    }

    public void setRTOpt(String RTOpt) {
        this.RTOpt = RTOpt;
    }

    public String getRankOpt() {
        return RankOpt;
    }

    public void setRankOpt(String RankOpt) {
        this.RankOpt = RankOpt;
    }

    public boolean isECOpt() {
        return ECOpt;
    }

    public void setECOpt(boolean ECOpt) {
        this.ECOpt = ECOpt;
    }

    public UploadedFile getPeakFile() {
        return peakFile;
    }

    public void setPeakFile(UploadedFile peakFile) {
        this.peakFile = peakFile;
    }

    public boolean isUseExample() {
        return useExample;
    }

    public void setUseExample(boolean useExample) {
        this.useExample = useExample;
    }

    public boolean isDisabledMumPval() {
        return disabledMumPval; //TODO: disable working for mz single list
    }

    public void setDisabledMumPval(boolean disabledMumPval) {
        this.disabledMumPval = disabledMumPval;
    }

    public double getPvalCutoff() {
        pvalCutoff = findPvalue();
        return pvalCutoff;
    }

    public void setPvalCutoff(double pvalCutoff) {
        this.pvalCutoff = pvalCutoff;
    }

    public String getDatatype() {
        return datatype;
    }

    public void setDatatype(String datatype) {
        this.datatype = datatype;
    }

}
