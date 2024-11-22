/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package metaboanalyst.controllers.mummichog;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import metaboanalyst.controllers.SessionBean1;
import metaboanalyst.models.GseaBean;
import metaboanalyst.models.IntegBean;
import metaboanalyst.models.MetSetBean;
import metaboanalyst.models.MummiBean;
import metaboanalyst.rwrappers.RDataUtils;
import metaboanalyst.rwrappers.REnrichUtils;
import metaboanalyst.spectra.SpectraParamBean;
import metaboanalyst.utils.DataUtils;
import org.primefaces.PrimeFaces;
import org.primefaces.model.DefaultStreamedContent;
import org.rosuda.REngine.Rserve.RConnection;

import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.ListDataModel;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.Semaphore;

/**
 * @author jianguox
 */
@SessionScoped
@Named("mummiAnalBean")
@JsonIgnoreProperties(ignoreUnknown = true)
public class MummiAnalBean implements Serializable {
    
    @JsonIgnore
    private final SessionBean1 sb = (SessionBean1) DataUtils.findBean("sessionBean1");
    private String enrichOpt = "fisher";
    private String filterOpt = "filtered";
    private String[] algOpts = {"mum"}; //"mum" and/or "gsea"
    private String pvalCutoffOpt = "default";
    private boolean disabledMumPval = false;
    private boolean disabledMum = false;
    private boolean disabledGsea = false;
    private boolean multigroups = false;
    private boolean disabledV2 = false;
    private double defaultCutoff = 0.1; // need to be updated to p value cutoff with 10%
    private double pvalCutoff = 0.001;
    private boolean doMumFilter = true;
    private int minMsetNum = 3;
    private String mumVersion = "v2";
    private String libVersion = "current";
    private String pathDBOpt = "hsa_mfn";
    private ListDataModel<MummiBean> listModel = null;
    private ListDataModel<GseaBean> listGSEAModel = null;
    private ListDataModel<IntegBean> listIntegModel = null;
    private double instrumentOpt = 5;
    private String heatmapName = "";
    private String analOption = "scatter";
    private boolean moduleSwitch = false;

    public String getEnrichOpt() {
        return enrichOpt;
    }

    public void setEnrichOpt(String enrichOpt) {
        this.enrichOpt = enrichOpt;
    }

    public String getFilterOpt() {
        return filterOpt;
    }

    public void setFilterOpt(String filterOpt) {
        this.filterOpt = filterOpt;
    }

    public String[] getAlgOpts() {
        return algOpts;
    }

    public void setAlgOpts(String[] algOpts) {
        this.algOpts = algOpts;
    }

    public String getPvalCutoffOpt() {
        return pvalCutoffOpt;
    }

    public void setPvalCutoffOpt(String pvalCutoffOpt) {
        this.pvalCutoffOpt = pvalCutoffOpt;
    }

    public boolean isDisabledMumPval() {
        return disabledMumPval;
    }

    public void setDisabledMumPval(boolean disabledMumPval) {
        this.disabledMumPval = disabledMumPval;
    }

    public boolean isDisabledMum() {
        return disabledMum;
    }

    public void setDisabledMum(boolean disabledMum) {
        this.disabledMum = disabledMum;
    }

    public boolean isDisabledGsea() {
        return disabledGsea;
    }

    public void setDisabledGsea(boolean disabledGsea) {
        this.disabledGsea = disabledGsea;
    }

    public boolean isMultigroups() {
        return multigroups;
    }

    public void setMultigroups(boolean multigroups) {
        this.multigroups = multigroups;
    }

    public String proceed2Net() {
        if (pathDBOpt.endsWith("mset")) {
            DataUtils.updateMsg("Error", "The network has not been created for metabolite set libraries yet. Please wait for the next release");
            return null;
        }
        return "Metabolic network";
    }

    public void showPathDialog() {
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        String latt = params.get("pathname");
        //System.out.println(latt);
        sb.setCurrentPathName(latt);
        PrimeFaces.current().executeScript("PF('msetDialog').show()");
    }

    public boolean isDisabledV2() {
        return disabledV2;
    }

    public void setDisabledV2(boolean disabledV2) {
        this.disabledV2 = disabledV2;
    }

    public double getDefaultCutoff() {
        return defaultCutoff;
    }

    public void setDefaultCutoff(double defaultCutoff) {
        this.defaultCutoff = defaultCutoff;
    }

    public double getPvalCutoff() {
        return pvalCutoff;
    }

    public void setPvalCutoff(double pvalCutoff) {
        this.pvalCutoff = pvalCutoff;
    }

    public MetSetBean[] getCurrentPathSet() {

        String pathname = sb.getCurrentPathName();

        String[] details = REnrichUtils.getMummichogHTMLPathSet(sb.getRConnection(), pathname);
        ArrayList<MetSetBean> libVec = new ArrayList();
        libVec.add(new MetSetBean(details[0], details[1], ""));
        return libVec.toArray(MetSetBean[]::new);
    }

    public boolean isDoMumFilter() {
        return doMumFilter;
    }

    public void setDoMumFilter(boolean doMumFilter) {
        this.doMumFilter = doMumFilter;
    }

    public int getMinMsetNum() {
        return minMsetNum;
    }

    public void setMinMsetNum(int minMsetNum) {
        this.minMsetNum = minMsetNum;
    }

    public String getMumVersion() {
        return mumVersion;
    }

    public void setMumVersion(String mumVersion) {
        this.mumVersion = mumVersion;
    }

    public String getLibVersion() {
        return libVersion;
    }

    public void setLibVersion(String libVersion) {
        this.libVersion = libVersion;
    }

    public String getPathDBOpt() {
        return pathDBOpt;
    }

    public void setPathDBOpt(String pathDBOpt) {
        this.pathDBOpt = pathDBOpt;
    }

    public boolean isShowMumNetwork() {
        return !pathDBOpt.endsWith("mset");
    }

    public ListDataModel<MummiBean> getMummiBeans() {
        return listModel;
    }

    public void setListModel(ListDataModel<MummiBean> listModel) {
        this.listModel = listModel;
    }

    public ListDataModel<GseaBean> getGseaBeans() {
        return listGSEAModel;
    }

    public void setListGSEAModel(ListDataModel<GseaBean> listGSEAModel) {
        this.listGSEAModel = listGSEAModel;
    }

    public ListDataModel<IntegBean> getIntegBeans() {
        return listIntegModel;
    }

    public double getInstrumentOpt() {
        return instrumentOpt;
    }

    public void setInstrumentOpt(double instrumentOpt) {
        this.instrumentOpt = instrumentOpt;
    }

    public String performPeaks2Fun() {

        if (algOpts.length == 0) {
            DataUtils.updateMsg("Error", "Select algorithm(s) to perform enrichment analysis!");
            return null;
        }

        //throttling
        Semaphore semphore = sb.getPermissionToStart();
        if (semphore == null) {
            return null;
        }

        RConnection RC = sb.getRConnection();
        String nextpage;

        String version = mumVersion;

        if (isModuleSwitch()) {
            SpectraParamBean sp = (SpectraParamBean) DataUtils.findBean("spectraParamer");
            String TmpMSModeOpt = sp.getPolarity();
            RDataUtils.setInstrumentParams(RC, instrumentOpt, TmpMSModeOpt, "yes", 0.02);
        }

        if (analOption.equals("heatmap")) {
            RDataUtils.setPeakEnrichMethod(RC, "mum", version);
            String[] libNmArr = pathDBOpt.split("_", 0);
            sb.setOrg(libNmArr[0]);
            RDataUtils.setOrganism(sb.getRConnection(), libNmArr[0]);
            heatmapName = "metaboanalyst_heatmap_" + sb.getFileCount() + ".json";
            sb.setHeatmapType("mummichog");
            if (sb.getDataType().equals("mass_table")) {
                REnrichUtils.createHeatmapJson(RC, pathDBOpt, libVersion, minMsetNum, heatmapName, filterOpt, version);
            } else {
                REnrichUtils.createListHeatmapJson(RC, pathDBOpt, libVersion, minMsetNum, heatmapName, filterOpt, version);
            }

            nextpage = "Heatmap viewer";

        } else if (algOpts.length > 1) {

            RDataUtils.setPeakEnrichMethod(RC, "integ", version);
            nextpage = "peakintegview";
            String imgName = sb.getNewImage("integ_peaks");

            if (sb.getDataType().equals("mass_table")) {
                boolean res = REnrichUtils.convertTableToPeakList(RC);
                if (!res) {
                    String msg = RDataUtils.getErrMsg(sb.getRConnection());
                    DataUtils.updateMsg("Error", "There is something wrong with the MS Peaks to Paths analysis: " + msg);
                }
            }

            if (REnrichUtils.setupMummichogPval(RC, pvalCutoff)) {

                if (REnrichUtils.performPSEA(RC, pathDBOpt, libVersion, minMsetNum)) {
                    // first create plot
                    REnrichUtils.plotPSEAIntegPaths(sb, imgName, "png", 72);

                    ArrayList<IntegBean> integBeans = new ArrayList();
                    String[] rownames = REnrichUtils.getMummiPathNames(RC);
                    String[] keggLnks = rownames;
                    double[][] mat = REnrichUtils.getMummiMat(RC);
                    IntegBean ib;
                    for (int i = 0; i < rownames.length; i++) {
                        ib = new IntegBean(rownames[i], keggLnks[i], (int) mat[i][0], (int) mat[i][1], (int) mat[i][2], mat[i][3], mat[i][4], mat[i][5]);
                        integBeans.add(ib);
                    }

                    listIntegModel = new ListDataModel(integBeans);

                } else {
                    String msg = RDataUtils.getErrMsg(sb.getRConnection());
                    DataUtils.updateMsg("Error", "There is something wrong with the MS Peaks to Paths analysis: " + msg);
                }
            }

        } else if (algOpts[0].equals("mum")) {

            RDataUtils.setPeakEnrichMethod(RC, "mum", version);
            nextpage = "mummires";
            String imgName = sb.getNewImage("peaks_to_paths");

            if (sb.getDataType().equals("mass_table")) {
                boolean res = REnrichUtils.convertTableToPeakList(RC);
                if (!res) {
                    String msg = RDataUtils.getErrMsg(sb.getRConnection());
                    DataUtils.updateMsg("Error", "There is something wrong with the MS Peaks to Paths analysis: " + msg);
                }
            }
            if (REnrichUtils.setupMummichogPval(RC, pvalCutoff)) {
                if (REnrichUtils.performPSEA(RC, pathDBOpt, libVersion, minMsetNum)) {

                    // first create plot
                    REnrichUtils.plotPeaks(sb, imgName, "png", 72);

                    ArrayList<MummiBean> mummiBeans = new ArrayList();
                    String[] rownames = REnrichUtils.getMummiPathNames(RC);
                    String[] keggLnks = rownames;
                    double[][] mat = REnrichUtils.getMummiMat(RC);
                    MummiBean mb;
                    for (int i = 0; i < rownames.length; i++) {
                        mb = new MummiBean(rownames[i], keggLnks[i], (int) mat[i][0], (int) mat[i][1], (int) mat[i][2], mat[i][5], mat[i][4], mat[i][8], mat[i][3], mat[i][9]);
                        mummiBeans.add(mb);
                    }
                    listModel = new ListDataModel(mummiBeans);
                } else {
                    String msg = RDataUtils.getErrMsg(sb.getRConnection());
                    DataUtils.updateMsg("Error", "There is something wrong with the MS Peaks to Paths analysis: " + msg);
                }
            } else {
                String msg = RDataUtils.getErrMsg(sb.getRConnection());
                DataUtils.updateMsg("Error", msg + "You can click the Submit button again to accept recommended p value.");
            }

        } else {
            RDataUtils.setPeakEnrichMethod(RC, algOpts[0], version);
            nextpage = "gseapkview";
            String imgName = sb.getNewImage("peaks_to_paths");

            if (sb.getDataType().equals("mass_table")) {
                boolean res = REnrichUtils.convertTableToPeakList(RC);
                if (!res) {
                    String msg = RDataUtils.getErrMsg(sb.getRConnection());
                    DataUtils.updateMsg("Error", "There is something wrong with the MS Peaks to Paths analysis: " + msg);
                }
            }

            if (REnrichUtils.performPSEA(RC, pathDBOpt, libVersion, minMsetNum)) {

                // first create plot
                REnrichUtils.plotPeaks(sb, imgName, "png", 72);

                ArrayList<GseaBean> gseaBeans = new ArrayList();
                String[] rownames = REnrichUtils.getMummiPathNames(RC);
                String[] keggLnks = rownames;
                double[][] mat = REnrichUtils.getMummiMat(RC);

                GseaBean gb;
                for (int i = 0; i < rownames.length; i++) {
                    gb = new GseaBean(rownames[i], keggLnks[i], (int) mat[i][0], (int) mat[i][1], mat[i][2], mat[i][3], mat[i][4]);
                    gseaBeans.add(gb);
                }
                listGSEAModel = new ListDataModel(gseaBeans);
            } else {
                String msg = RDataUtils.getErrMsg(sb.getRConnection());
                DataUtils.updateMsg("Error", "There is something wrong with the MS Peaks to Paths analysis: " + msg);
            }
        }

        //don't forget
        semphore.release();

        return nextpage;
    }

    public DefaultStreamedContent getPathEnrichFile() {
        return DataUtils.getDownloadFile(sb.getCurrentUser().getHomeDir() + "/mummichog_pathway_enrichment.csv");
    }

    public DefaultStreamedContent getGseaPathEnrichFile() {
        return DataUtils.getDownloadFile(sb.getCurrentUser().getHomeDir() + "/mummichog_fgsea_pathway_enrichment.csv");
    }

    public DefaultStreamedContent getIntegPathEnrichFile() {
        return DataUtils.getDownloadFile(sb.getCurrentUser().getHomeDir() + "/mummichog_integ_pathway_enrichment.csv");
    }

    public DefaultStreamedContent getCmpdHitFile() {
        return DataUtils.getDownloadFile(sb.getCurrentUser().getHomeDir() + "/mummichog_matched_compound_all.csv");
    }

    public String getHeatmapName() {
        return heatmapName;
    }

    public void setHeatmapName(String heatmapName) {
        this.heatmapName = heatmapName;
    }

    public String getAnalOption() {
        return analOption;
    }

    public void setAnalOption(String analOption) {
        this.analOption = analOption;
    }

    public boolean isModuleSwitch() {
        return moduleSwitch;
    }

    public void setModuleSwitch(boolean moduleSwitch) {
        this.moduleSwitch = moduleSwitch;
    }

}
