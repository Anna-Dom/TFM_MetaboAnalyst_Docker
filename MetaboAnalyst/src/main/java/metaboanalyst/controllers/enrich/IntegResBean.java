/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package metaboanalyst.controllers.enrich;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.util.ArrayList;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import metaboanalyst.controllers.SessionBean1;
import metaboanalyst.models.MetSetBean;
import metaboanalyst.models.PABean;
import metaboanalyst.rwrappers.RDataUtils;
import metaboanalyst.rwrappers.REnrichUtils;
import metaboanalyst.rwrappers.RGraphUtils;
import metaboanalyst.rwrappers.RIntegUtils;
import metaboanalyst.utils.DataUtils;
import org.primefaces.PrimeFaces;
import org.primefaces.model.DefaultStreamedContent;
import org.rosuda.REngine.Rserve.RConnection;

/**
 *
 * @author jianguox
 */
@SessionScoped
@Named("integResBean")
@JsonIgnoreProperties(ignoreUnknown = true)
public class IntegResBean implements Serializable {
    @JsonIgnore
    private final SessionBean1 sb = (SessionBean1) DataUtils.findBean("sessionBean1");
    @JsonIgnore
    private final IntegProcessBean ipb = (IntegProcessBean) DataUtils.findBean("integProcesser");

    private String enrichOpt = "hyper";
    private String topoOpt = "dc";
    private String pathDBOpt = "integ";
    private String integOpt = "query";
    private String viewOpt = "path";
    private String pvalmethod = "fisher";
    private String netLibOpt = "allp"; // can be "global" or "allp" for network view

    public String getNetLibOpt() {
        return netLibOpt;
    }

    public void setNetLibOpt(String netLibOpt) {
        this.netLibOpt = netLibOpt;
    }

    public String getPvalmethod() {
        return pvalmethod;
    }

    public void setPvalmethod(String pvalmethod) {
        this.pvalmethod = pvalmethod;
    }

    public String getViewOpt() {
        return viewOpt;
    }

    public void setViewOpt(String viewOpt) {
        this.viewOpt = viewOpt;
    }

    public String getHitsHeader() {
        if (integOpt.equals("query")) {
            return "Match Status";
        }
        return "(M.,G.)/Total";
    }

    public String getIntegOpt() {
        if (ipb.getDatatype().equals("peak")) {
            return "pvali";
        }
        return integOpt;
    }

    public void setIntegOpt(String integOpt) {
        this.integOpt = integOpt;
    }

    public String getEnrichOpt() {
        return enrichOpt;
    }

    public void setEnrichOpt(String enrichOpt) {
        this.enrichOpt = enrichOpt;
    }

    public String getTopoOpt() {
        return topoOpt;
    }

    public void setTopoOpt(String topoOpt) {
        this.topoOpt = topoOpt;
    }

    public MetSetBean[] getCurrentPathSet() {
        ArrayList<MetSetBean> libVec = new ArrayList();
        String[] details = REnrichUtils.getIntegHTMLPathSet(sb.getRConnection(), sb.getCurrentPathName());
        libVec.add(new MetSetBean(details[0], details[1], ""));
        return libVec.toArray(MetSetBean[]::new);
    }

    public String doIntegPathwayAnanlysis() {
        
        if (viewOpt.equals("netw")) {
            PrimeFaces.current().executeScript("PF('netlib').show();");
        } else {
            return doIntegPahtwayAnalysis();
        }
        return null;
    }

    public String doIntegPahtwayAnalysis() {
        RConnection RC = sb.getRConnection();
        
        if (integOpt.startsWith("pval") & !ipb.getDatatype().equals("peak")) {
            if (pathDBOpt.equals("metab") || pathDBOpt.equals("genetic")) {
                DataUtils.updateMsg("Error", "Combining p values cannot be used for just one input type. Please use combing query instead.");
                return null;
            }
        }
        if (ipb.getDatatype().equals("peak")) {
            RIntegUtils.definePvalMethod(RC, pvalmethod);
            ipb.PerformMummiInitPrediction();
            integOpt = "pvali";
        }
        int res = RIntegUtils.performIntegPathwayAnalysis(RC, topoOpt, enrichOpt, pathDBOpt, integOpt);
        if (res == 1) {
            RGraphUtils.plotPathSummary(sb, showGrid ? "T" : "F", sb.getNewImage("path_view"), "png", 72, 0, 0);
            RIntegUtils.createIntegPathResults(RC);
            populateIntegResBeans();
            return "integview";
        } else {
            String err = RDataUtils.getErrMsg(RC);
            DataUtils.updateMsg("Error", err);
            return null;
        }  
    }
    
    public String doIntegNetworkAnalysis(){
        
        RConnection RC = sb.getRConnection();

        if (netLibOpt.equals("allp")) {
            if (integOpt.startsWith("pval") & !ipb.getDatatype().equals("peak")) {
                if (pathDBOpt.equals("metab") || pathDBOpt.equals("genetic")) {
                    DataUtils.updateMsg("Error", "Combining p values cannot be used for just one input type. Please use combing query instead.");
                    return null;
                }
            }
            if (ipb.getDatatype().equals("peak")) {
                RIntegUtils.definePvalMethod(RC, pvalmethod);
                ipb.PerformMummiInitPrediction();
                integOpt = "pvali";
            }
            int res = RIntegUtils.performIntegPathwayAnalysis(RC, topoOpt, enrichOpt, pathDBOpt, integOpt);
            if (res == 1) {
                RGraphUtils.plotPathSummary(sb, showGrid ? "T" : "F", sb.getNewImage("path_view"), "png", 72, 0, 0);
                RIntegUtils.createIntegPathResults(RC);
            } else {
                String err = RDataUtils.getErrMsg(RC);
                DataUtils.updateMsg("Error", err);
                return null;
            }
        }
        
        int res2;
        try {
            RIntegUtils.definePvalMethod(RC, pvalmethod);
            if (netLibOpt.equals("global")) {
                ipb.PerformMummiInitPrediction();
            }
            res2 = RIntegUtils.performIntegNetworkAnal(RC, netLibOpt);
            if (res2 == 1) {
                return "MnetView";
            }
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to perform the Network analysis"));
            return null;
        }
    
        return null;
    }
    
    public String doTargetIntegNetwAna() {
        RConnection RC = sb.getRConnection();
        int res2 = RIntegUtils.performTarIntegNetAnal(RC, netLibOpt);
        if (res2 == 1) {
            return "MnetView";
        }

        return null;
    }
    
    private PABean[] paBeans;

    public PABean[] getPaBeans() {
        return paBeans;
    }

    public void setPaBeans(PABean[] pabs) {
        paBeans = pabs;
    }

    private void populateIntegResBeans() {
        RConnection RC = sb.getRConnection();

        String[] keggIds = RIntegUtils.getIntegPathIDs(RC);
        String[] rownames = RIntegUtils.getIntegPathNames(RC);
        double[][] mat = RIntegUtils.getIntegResMatrix(RC);

        paBeans = new PABean[rownames.length];

        if (ipb.getDatatype().equals("peak")) {
            for (int i = 0; i < rownames.length; i++) {
                paBeans[i] = new PABean(integOpt, rownames[i], "<a target=\"_blank\" href=\"https://www.genome.jp/kegg-bin/show_pathway?" + keggIds[i] + "\">KEGG</a>", "",
                        (int) mat[i][0], mat[i][2], (int) mat[i][1], mat[i][5], mat[i][6], mat[i][7], mat[i][8], 0, mat[i][3], mat[i][4]);
            }
        } else {
            for (int i = 0; i < rownames.length; i++) {
                paBeans[i] = new PABean(integOpt, rownames[i], "<a target=\"_blank\" href=\"https://www.genome.jp/kegg-bin/show_pathway?" + keggIds[i] + "\">KEGG</a>", "",
                        (int) mat[i][0], mat[i][1], (int) mat[i][2], mat[i][3], mat[i][4], mat[i][5], mat[i][6], mat[i][7], 0, 0);
            }
        }

    }

    public String getPathDBOpt() {
        if (ipb.getDatatype().equals("peak")) {
            return "mgenetic";
        }
        return pathDBOpt;
    }

    public void setPathDBOpt(String pathDBOpt) {
        this.pathDBOpt = pathDBOpt;
    }

    public DefaultStreamedContent getDownloadPathwayFile() {
        if (ipb.getDatatype().equals("peak")) {
            return DataUtils.getDownloadFile(sb.getCurrentUser().getHomeDir() + "/MetaboAnalyst_result_integ.csv");
        }
        return DataUtils.getDownloadFile(sb.getCurrentUser().getHomeDir() + "/MetaboAnalyst_result_pathway.csv");
    }

    public DefaultStreamedContent getDownloadPathwayMatchingFile() {
        return DataUtils.getDownloadFile(sb.getCurrentUser().getHomeDir() + "/jointpa_matched_features.csv");
    }

    private boolean showGrid = false;

    public boolean isShowGrid() {
        return showGrid;
    }

    public void setShowGrid(boolean showGrid) {
        this.showGrid = showGrid;
    }

    public void updatePathView() {
        RGraphUtils.plotPathSummary(sb, showGrid ? "T" : "F", sb.getNewImage("path_view"), "png", 72, 0, 0);
    }
}
