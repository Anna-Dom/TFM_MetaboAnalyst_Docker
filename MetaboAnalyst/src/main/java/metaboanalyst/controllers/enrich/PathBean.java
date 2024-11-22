/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package metaboanalyst.controllers.enrich;

import java.io.Serializable;
import java.util.ArrayList;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import metaboanalyst.controllers.SessionBean1;
import metaboanalyst.models.MetSetBean;
import metaboanalyst.models.PABean;
import metaboanalyst.rwrappers.RDataUtils;
import metaboanalyst.rwrappers.REnrichUtils;
import metaboanalyst.rwrappers.RGraphUtils;
import metaboanalyst.utils.DataUtils;
import org.primefaces.model.file.UploadedFile;
import org.rosuda.REngine.Rserve.RConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author jianguox
 */
@SessionScoped
@Named("pathBean")
@JsonIgnoreProperties(ignoreUnknown = true)
public class PathBean implements Serializable {
    @JsonIgnore
    private static final Logger LOGGER = LogManager.getLogger(PathBean.class);
    @JsonIgnore
    private final SessionBean1 sb = (SessionBean1) DataUtils.findBean("sessionBean1");
    @JsonProperty("libVersion")
    private String libVersion = "current";
    @JsonProperty("libOpt")
    private String libOpt = "hsa";
    @JsonProperty("refLibOpt")
    private String refLibOpt = "all";
    @JsonIgnore
    private UploadedFile refLibFile;
    @JsonProperty("topoCode")
    private String topoCode = "rbc";
    @JsonProperty("oraStatCode")
    private String oraStatCode = "hyperg";
    @JsonProperty("qeaStatCode")
    private String qeaStatCode = "gt";
    @JsonProperty("paBeans")
    private PABean[] paBeans;
    @JsonProperty("downloadMsg")
    private String downloadMsg = "";
    @JsonProperty("analOption")
    private String analOption = "scatter";
    @JsonProperty("heatmapName")
    private String heatmapName = "";
    @JsonProperty("enrType")
    private String enrType = "";
    @JsonProperty("showGrid")
    private boolean showGrid = false;
    @JsonProperty("xlim")
    private double xlim = 0;
    @JsonProperty("ylim")
    private double ylim = 0;

    public String getLibVersion() {
        return libVersion;
    }

    public void setLibVersion(String libVersion) {
        this.libVersion = libVersion;
    }

    public String getLibOpt() {
        return libOpt;
    }

    public void setLibOpt(String libOpt) {
        this.libOpt = libOpt;
    }

    public String getRefLibOpt() {
        return refLibOpt;
    }

    public void setRefLibOpt(String refLibOpt) {
        this.refLibOpt = refLibOpt;
    }

    public String libSubmitBn_action() {
        return "View result";
    }

    public UploadedFile getRefLibFile() {
        return refLibFile;
    }

    public void setRefLibFile(UploadedFile refLibFile) {
        this.refLibFile = refLibFile;
    }

    public String getTopoCode() {
        return topoCode;
    }

    public void setTopoCode(String topoCode) {
        this.topoCode = topoCode;
    }

    public String getOraStatCode() {
        return oraStatCode;
    }

    public void setOraStatCode(String oraStatCode) {
        this.oraStatCode = oraStatCode;
    }

    public String getQeaStatCode() {
        return qeaStatCode;
    }

    public void setQeaStatCode(String qeaStatCode) {
        this.qeaStatCode = qeaStatCode;
    }
    
    @JsonIgnore
    public MetSetBean[] getCurrentPathSet() {
        String[] details = REnrichUtils.getHTMLPathSet(sb.getRConnection(), sb.getCurrentPathName());
        ArrayList<MetSetBean> libVec = new ArrayList();
        libVec.add(new MetSetBean(details[0], details[1], ""));
        return libVec.toArray(new MetSetBean[0]);
    }

    public void handleRefLibUpload() {
        // TODO: Process the action. Return value is a navigation
        // case name where null will return to the same page.
        try {

            RConnection RC = sb.getRConnection();
            //check if data is uploaded
            if (refLibFile == null) {
                DataUtils.updateMsg("Error", "Please upload your file!");
                return;
            }
            if (refLibFile.getSize() == 0) {
                DataUtils.updateMsg("Error", "File is empty");
                return;
            }
            String fileName = DataUtils.uploadFile(refLibFile, sb.getCurrentUser().getHomeDir(), null, false);
            boolean res = RDataUtils.readKEGGRefLibData(RC, fileName);
            if (res) {
                DataUtils.updateMsg("OK", RDataUtils.getRefLibCheckMsg(RC));
                refLibOpt = "self";
            } else {
                DataUtils.updateMsg("Error", RDataUtils.getErrMsg(RC));
            }
        } catch (Exception e) {
            // e.printStackTrace();
            LOGGER.error("handleRefLibUpload", e);
        }
    }

    public String paBn_proceed() {
        String type = sb.getAnalType();
        if (analOption.equals("scatter")) {
            return (paBn_action());
        } else {
            return (paBn_heatmap(type));
        }
    }

    public String paBn_action() {

        RConnection RC = sb.getRConnection();
        if (RDataUtils.setPathLib(RC, libOpt, libVersion)) {

            String nextpage;
            if (libOpt.startsWith("smpdb")) {
                libOpt = libOpt.split("-")[1];
                RDataUtils.setOrganism(sb.getRConnection(), libOpt);
                nextpage = "smpdbpathview";
            } else {
                nextpage = "pathview";
            }

            if (refLibOpt.equals("all")) {
                RDataUtils.setMetabolomeFilter(sb.getRConnection(), false);
            } else {
                RDataUtils.setMetabolomeFilter(sb.getRConnection(), true);
            }

            if (sb.getAnalType().equalsIgnoreCase("pathqea")) {

                if (REnrichUtils.doPathQeaTest(sb, topoCode, qeaStatCode)) {
                    RGraphUtils.plotPathSummary(sb, showGrid ? "T" : "F", sb.getNewImage("path_view"), "png", 72, 0, 0);
                    String[] pathnames = REnrichUtils.getQEApathNames(RC);
                    String[] keggLnks = REnrichUtils.getQEAKeggIDs(RC);
                    String[] smpdbLnks = null;
                    if (libOpt.equals("hsa") || libOpt.equals("mmu")) {
                        smpdbLnks = REnrichUtils.getQEASMPDBIDs(RC);
                    }

                    if (pathnames.length == 1 && pathnames[0].equals("NA")) {
                        return null;
                    }

                    double[][] mat = REnrichUtils.getQEAMat(RC);
                    paBeans = new PABean[pathnames.length];
                    for (int i = 0; i < pathnames.length; i++) {
                        paBeans[i] = new PABean("query", pathnames[i], keggLnks[i], smpdbLnks == null ? "" : smpdbLnks[i], (int) mat[i][0], 0, (int) mat[i][1],
                                mat[i][2], mat[i][3], mat[i][4], mat[i][5], mat[i][6], 0, 0);
                    }
                } else {
                    String msg = RDataUtils.getErrMsg(RC);
                    DataUtils.updateMsg("Error", "There is something wrong with the pathway enrichment analysis: " + msg);
                    return null;
                }
            } else if (REnrichUtils.doPathOraTest(sb, topoCode, oraStatCode)) {
                RGraphUtils.plotPathSummary(sb, showGrid ? "T" : "F", sb.getNewImage("path_view"), "png", 72, 0, 0);
                String[] rownames = REnrichUtils.getORApathNames(RC);
                String[] keggLnks = REnrichUtils.getORAKeggIDs(RC);
                String[] smpdbLnks = null;
                if (libOpt.equals("hsa") || libOpt.equals("mmu")) {
                    smpdbLnks = REnrichUtils.getORASMPDBIDs(RC);
                }
                double[][] mat = REnrichUtils.getORAMat(RC);
                paBeans = new PABean[rownames.length];
                for (int i = 0; i < rownames.length; i++) {
                    paBeans[i] = new PABean("query", rownames[i], keggLnks[i], smpdbLnks == null ? "" : smpdbLnks[i],
                            (int) mat[i][0], mat[i][1], (int) mat[i][2], mat[i][3], mat[i][4], mat[i][5], mat[i][6], mat[i][7], 0, 0);
                }
            } else {
                String msg = RDataUtils.getErrMsg(RC);
                DataUtils.updateMsg("Error", "There is something wrong with the pathway enrichment analysis: " + msg);
                return null;
            }
            return nextpage;
        } else {
            String msg = RDataUtils.getErrMsg(RC);
            DataUtils.updateMsg("Error", "There is something wrong with the pathway enrichment analysis: " + msg);
            return null;
        }
    }

    public String paBn_heatmap(String type) {

        RConnection RC = sb.getRConnection();
        if (RDataUtils.setPathLib(RC, libOpt, libVersion)) {

            if (libOpt.startsWith("smpdb")) {
                setEnrType(libOpt);
                libOpt = libOpt.split("-")[1];
                RDataUtils.setOrganism(sb.getRConnection(), libOpt);
            } else {
            }

            if (refLibOpt.equals("all")) {
                RDataUtils.setMetabolomeFilter(sb.getRConnection(), false);
            } else {
                RDataUtils.setMetabolomeFilter(sb.getRConnection(), true);
            }
            String fileNm = "metaboanalyst_heatmap_" + sb.getFileCount() + ".json";
            setHeatmapName(fileNm);
            if (REnrichUtils.computePathHeatmap(RC, libOpt, fileNm, type)) {
                sb.setHeatmapType("pathway");
                return "Heatmap viewer";
            } else {
                String msg = RDataUtils.getErrMsg(RC);
                DataUtils.updateMsg("Error", "There is something wrong with the pathway enrichment analysis: " + msg);
                return null;
            }

        } else {
            String msg = RDataUtils.getErrMsg(RC);
            DataUtils.updateMsg("Error", "There is something wrong with the pathway enrichment analysis: " + msg);
            return null;
        }
    }

    public PABean[] getPaBeans() {
        if(paBeans == null){
            paBn_action();
        }
        return paBeans;
    }

    public void setPaBeans(PABean[] pabs) {
        paBeans = pabs;
    }

    public String getDownloadMsg() {
        return downloadMsg;
    }

    public void setDownloadMsg(String downloadMsg) {
        this.downloadMsg = downloadMsg;
    }

    public String getAnalOption() {
        return analOption;
    }

    public void setAnalOption(String analOption) {
        this.analOption = analOption;
    }

    public String getHeatmapName() {
        return heatmapName;
    }

    public void setHeatmapName(String heatmapName) {
        this.heatmapName = heatmapName;
    }

    public String getEnrType() {
        return enrType;
    }

    public void setEnrType(String enrType) {
        this.enrType = enrType;
    }

    public boolean isShowGrid() {
        return showGrid;
    }

    public void setShowGrid(boolean showGrid) {
        this.showGrid = showGrid;
    }

    public double getXlim() {
        return xlim;
    }

    public void setXlim(double xlim) {
        this.xlim = xlim;
    }

    public double getYlim() {
        return ylim;
    }

    public void setYlim(double ylim) {
        this.ylim = ylim;
    }

    public void updatePathView() {
        RGraphUtils.plotPathSummary(sb, showGrid ? "T" : "F", sb.getNewImage("path_view"), "png", 72, xlim, ylim);
    }
}
