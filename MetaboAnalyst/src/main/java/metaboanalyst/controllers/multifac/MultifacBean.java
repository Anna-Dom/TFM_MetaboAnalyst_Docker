/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package metaboanalyst.controllers.multifac;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import metaboanalyst.controllers.ApplicationBean1;
import metaboanalyst.controllers.SessionBean1;
import metaboanalyst.models.MetaDataBean;
import metaboanalyst.models.SampleBean;
import metaboanalyst.rwrappers.RDataUtils;
import metaboanalyst.rwrappers.TimeSeries;
import metaboanalyst.rwrappers.UniVarTests;
import metaboanalyst.utils.DataUtils;
import org.primefaces.PrimeFaces;
import org.primefaces.event.CellEditEvent;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.Rserve.RConnection;

import javax.enterprise.context.SessionScoped;
import javax.faces.model.SelectItem;
import javax.inject.Named;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author jianguox
 */
@SessionScoped
@Named("multifacBean")
@JsonIgnoreProperties(ignoreUnknown = true)
public class MultifacBean implements Serializable {

    //private final ClassificationBean cb = (ClassificationBean) DataUtils.findBean("classBean");
    @JsonIgnore
    private final ApplicationBean1 ab = (ApplicationBean1) DataUtils.findBean("applicationBean1");
    @JsonIgnore
    private final SessionBean1 sb = (SessionBean1) DataUtils.findBean("sessionBean1");

    private boolean disableMetaSelection = false;
    private List<MetaDataBean> metaDataBean = null;
    private MetaDataBean selectedMetaDataBean;
    private String selectedMetaData = "Class";
    private String[] metaOpts;
    private String[] discMetaOpts;
    private String covJsonName = "";
    private boolean ascaInit = false;
    private String mbImage = null;
    private int count = 0;
    private SelectItem[] discreteMetaOpts;
    private SelectItem[] boxMetaOpts;
    private SelectItem[] analysisMetaOpts;
    private String[] includedMetaData;
    private boolean covPerformed = false;
    private boolean corrPerformed = false;
    private boolean aov2Performed = false;
    private String boxplotUrl;
    private String boxMeta = "NA";
    private String boxId;
    private String defaultText = "The analysis has not been performed yet.";
    private SelectItem[] analysisMetaOptsAnova;
    private String covStyleOpt = "default";
    private List<SampleBean> uniqueMetaNames = null;
    private List<String> uniqueMetaList = null;

    public boolean isDisableMetaSelection() {
        return disableMetaSelection;
    }

    public void setDisableMetaSelection(boolean disableMetaSelection) {
        this.disableMetaSelection = disableMetaSelection;
    }

    public List<MetaDataBean> getMetaDataBeans() {
        if (metaDataBean == null) {
            initMetaDataBean();
        }

        return metaDataBean;
    }

    public void initMetaDataBean() {
        RConnection RC = sb.getRConnection();
        String[] metaDataGroups = RDataUtils.getMetaDataGroups(RC);
        String[] metaDataStatus = RDataUtils.getMetaDataStatus(RC);

        metaDataBean = new ArrayList();
        String[] metatypes = RDataUtils.getMetaTypes(RC);
        if (metaDataGroups == null || metaDataStatus == null || metatypes == null) {
            DataUtils.updateMsg("Error", "Failed to parse the metadata information!");
            return;
        }
        for (int i = 0; i < metaDataGroups.length; i++) {
            String metatype = metatypes[i];
            String metastatus = metaDataStatus[i];
            //whether disabled or not in metadatacheck page
            //if tsdesign equals time or time0, disable metadata if it's name "time"
            if (i == 0) {
                if (metaDataGroups[i].toLowerCase().equals("time") && !sb.getTsDesign().equals("multi")) {
                    metaDataBean.add(new MetaDataBean(metaDataGroups[i], metatype, i, true, true, metastatus));
                } else {
                    metaDataBean.add(new MetaDataBean(metaDataGroups[i], metatype, i, true, false, metastatus));
                }
            } else if ((metaDataGroups[i].toLowerCase().equals("time") || metaDataGroups[i].toLowerCase().equals("subject")) && !sb.getTsDesign().equals("multi")) {
                metaDataBean.add(new MetaDataBean(metaDataGroups[i], metatype, i, false, true, metastatus));
            } else {
                metaDataBean.add(new MetaDataBean(metaDataGroups[i], metatype, i, false, false, metastatus));
            }

        }
        if (selectedMetaDataBean == null) {
            selectedMetaDataBean = metaDataBean.get(0);
        }
    }

    public MetaDataBean getSelectedMetaDataBean() {
        if (selectedMetaDataBean == null) {
            selectedMetaDataBean = metaDataBean.get(0);

        }
        return selectedMetaDataBean;
    }

    public void setSelectedMetaDataBean(MetaDataBean selectedMetaDataBean) {
        this.selectedMetaDataBean = selectedMetaDataBean;
    }

    public void removeMeta() {
        if (selectedMetaDataBean.isPrimary()) {
            DataUtils.updateMsg("Error", "Cannot remove the primary metadata!");
            return;
        }

        if (metaDataBean.size() == 2) {
            DataUtils.updateMsg("Error", "Cannot remove the metadata, at least two is required to proceed!");
            return;
        }

        RDataUtils.removeSelectedMeta(sb.getRConnection(), selectedMetaDataBean.getName());
        metaDataBean.remove(selectedMetaDataBean);
        reinitVariables();
    }

    public void reinitVariables() {
        analysisMetaOpts = null;
        metaDataBean = null;
        boxMetaOpts = null;
        includedMetaData = null;
    }

    public List<MetaDataBean> getMetaDataBean() {
        return metaDataBean;
    }

    public void setMetaDataBean(List<MetaDataBean> metaDataBean) {
        this.metaDataBean = metaDataBean;
    }

    public void editMeta(MetaDataBean meta) {
        selectedMetaDataBean = meta;
        selectedMetaData = meta.getName();
        sb.setSampleBeans(RDataUtils.createOrigSampleBeans(sb.getRConnection(), selectedMetaData, false));

        if (!selectedMetaDataBean.getParam().equals("cont")) {
            String[] allMetas = RDataUtils.getUniqueMetaNames(sb.getRConnection(), selectedMetaDataBean.getName());
            int samSize = allMetas.length;
            uniqueMetaList = new ArrayList();
            uniqueMetaNames = new ArrayList<>();
            for (int i = 0; i < samSize; i++) {
                uniqueMetaList.add(allMetas[i]);
                uniqueMetaNames.add(new SampleBean(allMetas[i], allMetas[i]));
            }
        }

    }

    public String getSelectedMetaData() {
        return selectedMetaData;
    }

    public void setSelectedMetaData(String selectedMetaData) {
        this.selectedMetaData = selectedMetaData;
    }

    public String[] getMetaOpt() {
        if (metaOpts == null) {
            metaOpts = new String[2];
            for (int i = 0; i < 2; i++) {
                metaOpts[i] = getMetaDataBeans().get(i).getName();
            }
        }
        return metaOpts;
    }

    public String[] getDiscMetaOpts() {
        if (discMetaOpts == null) {
            discMetaOpts = new String[2];
            int discCount = 0;
            for (int i = 0; i < getMetaDataBeans().size(); i++) {
                if (!getMetaDataBeans().get(i).getParam().equals("cont")) {
                    discMetaOpts[discCount] = getMetaDataBeans().get(i).getName();
                    discCount++;
                    if (discCount == 2) {
                        break;
                    }
                }
            }
            if (discCount < 2) {
                DataUtils.updateMsg("Error", "This analysis can not be performed. At least two metadata of categorical type are required!");
                return null;
            }
        }

        return discMetaOpts;
    }

    public String getAov2Img() {
        return ab.getRootContext() + sb.getCurrentUser().getRelativeDir() + File.separator + sb.getCurrentImage("aov2") + "dpi72.png";
    }

    public String getCovJsonName() {
        return covJsonName;
    }

    public void setCovJsonName(String covJsonName) {
        this.covJsonName = covJsonName;
    }

    public boolean isAscaInit() {
        return ascaInit;
    }

    public void setAscaInit(boolean ascaInit) {
        this.ascaInit = ascaInit;
    }

    public void cmpdLnk_action() {
        mbImage = TimeSeries.plotMBTimeProfile(sb, sb.getCurrentCmpdName(), count, "png", 72 + "");
        count++;
    }

    public String getMEBACmpdImg() {
        return ab.getRootContext() + sb.getCurrentUser().getRelativeDir() + File.separator + mbImage;
    }

    public SelectItem[] getAnalysisMetaOpts() {

        if (analysisMetaOpts == null) {
            List<MetaDataBean> beans = getMetaDataBeans();
            analysisMetaOpts = new SelectItem[beans.size()];
            for (int i = 0; i < beans.size(); i++) {
                analysisMetaOpts[i] = new SelectItem(beans.get(i).getName(), beans.get(i).getName());
            }
        }

        return analysisMetaOpts;
    }

    public SelectItem[] getDiscreteMetaOpts() {

        if (discreteMetaOpts == null) {
            List<MetaDataBean> beans = getMetaDataBeans();
            int discCount = 0;
            for (int i = 0; i < beans.size(); i++) {
                if (!beans.get(i).getParam().equals("cont")) {
                    discCount++;
                }
            }
            int arrInx = 0;
            discreteMetaOpts = new SelectItem[discCount];
            for (int i = 0; i < beans.size(); i++) {
                if (!beans.get(i).getParam().equals("cont")) {
                    discreteMetaOpts[arrInx] = new SelectItem(beans.get(i).getName(), beans.get(i).getName());
                    arrInx++;
                }
            }
        }

        return discreteMetaOpts;
    }

    public void updateMetaData() {

        List<MetaDataBean> beans = getMetaDataBeans();
        String[] metas = new String[beans.size()];
        for (int i = 0; i < beans.size(); i++) {
            metas[i] = beans.get(i).getParam();
            if (metas[i].equals("NA")) {
                DataUtils.updateMsg("Error", "Please specify data type of meta-data classes.");
                return;
            }
        }
        RDataUtils.setMetaTypes(sb.getRConnection(), metas);

        MultifacBean tb = (MultifacBean) DataUtils.findBean("multifacBean");
        if (tb.getIncludedMetaData().length > 8) { // disable this requirement
            DataUtils.updateMsg("Error", "Please select at most eight different meta-data classes.");
            return;
        }
        DataUtils.updateMsg("Info", "Meta-data successfully updated!");
    }

    public SelectItem[] getBoxMetaOpts() {
        if (boxMetaOpts == null) {
            List<MetaDataBean> beans = getMetaDataBeans();
            boxMetaOpts = new SelectItem[beans.size() - 1];
            for (int i = 0; i < beans.size(); i++) {
                if (i > 0) {
                    boxMetaOpts[i - 1] = new SelectItem(beans.get(i).getName(), beans.get(i).getName());
                }
            }
        }
        return boxMetaOpts;
    }

    public void setBoxMetaOpts(SelectItem[] boxMetaOpts) {
        this.boxMetaOpts = boxMetaOpts;
    }

    public String[] getIncludedMetaData() {
        if (includedMetaData == null) {
            String[] metanms = RDataUtils.getMetaDataGroups(sb.getRConnection());
            includedMetaData = metanms;
        }
        return includedMetaData;
    }

    public void setIncludedMetaData(String[] includedMetaData) {
        this.includedMetaData = includedMetaData;
    }

    public boolean isCovPerformed() {
        return covPerformed;
    }

    public void setCovPerformed(boolean covPerformed) {
        this.covPerformed = covPerformed;
    }

    public boolean isCorrPerformed() {
        return corrPerformed;
    }

    public void setCorrPerformed(boolean corrPerformed) {
        this.corrPerformed = corrPerformed;
    }

    public boolean isAov2Performed() {
        return aov2Performed;
    }

    public void setAov2Performed(boolean aov2Performed) {
        this.aov2Performed = aov2Performed;
    }

    public String getBoxplotUrl() {
        return boxplotUrl;
    }

    public void setBoxplotUrl(String boxplotUrl) {
        this.boxplotUrl = boxplotUrl;
    }

    public String getBoxMeta() {
        return boxMeta;
    }

    public void setBoxMeta(String boxMeta) {
        this.boxMeta = boxMeta;
    }

    public void updateBoxplotMeta() {
        String prefix = sb.getUrlPrefix(); //
        String cmpdName = UniVarTests.plotCmpdSummary(sb, boxId, boxMeta, 0, "png", 72 + "");
        String imgUrl = prefix + "/resources/users/" + DataUtils.getJustFileName(sb.getCurrentUser().getHomeDir()) + File.separator + cmpdName;
        sb.setBoxplotUrl(imgUrl);
        sb.setLinMod(false);
        PrimeFaces.current().executeScript("PF('FeatureView').show();");
    }

    public void updateMultiFacBoxplotMeta(String type) {
        LimmaBean lm = (LimmaBean) DataUtils.findBean("lmBean");
        String prefix = sb.getUrlPrefix(); //
        String cmpdName;
        if (type.equals("default")) {
            cmpdName = UniVarTests.plotMultiFacCmpdSummary(sb, boxId, lm.getAnalysisMeta(), 0, "png", 72 + "");
        } else {
            cmpdName = UniVarTests.plotMultiFacCmpdSummary(sb, boxId, boxMeta, 0, "png", 72 + "");
        }
        String imgUrl = prefix + "/resources/users/" + DataUtils.getJustFileName(sb.getCurrentUser().getHomeDir()) + File.separator + cmpdName;
        sb.setBoxplotUrl(imgUrl);
        sb.setLinMod(true);
        PrimeFaces.current().executeScript("PF('FeatureView').show();");
    }

    public void onCellEdit(CellEditEvent<String> event) {
        String oldValue = event.getOldValue();
        String newValue = event.getNewValue();

        if (newValue != null && !newValue.equals(oldValue)) {
            //need to update feature
            List<MetaDataBean> beans = getMetaDataBeans();
            int textcount = 0;
            for (int i = 0; i < beans.size(); i++) {

                if (beans.get(i).getName().equalsIgnoreCase(newValue)) {
                    textcount++;
                }
            }

            if (textcount > 1) {
                getMetaDataBeans().get(event.getRowIndex()).setName(oldValue);
                DataUtils.updateMsg("Error", "The new name must be unique among the other metadata names!");
                return;
            }

            RDataUtils.updateMetaColName(sb.getRConnection(), oldValue, newValue);
            DataUtils.updateMsg("OK", "Updated from: " + oldValue + " to:" + newValue);
            reinitVariables();

            //need to recompute, set states to naive
            sb.resetAnalysis();
        }
    }

    public String getBoxId() {
        return boxId;
    }

    public void setBoxId(String boxId) {
        this.boxId = boxId;
    }

    public String getDefaultText() {
        return defaultText;
    }

    public void setDefaultText(String defaultText) {
        this.defaultText = defaultText;
    }

    public SelectItem[] getAnalysisMetaOptsAnova() {
        if (analysisMetaOptsAnova == null) {
            List<MetaDataBean> beans = getMetaDataBeans();
            analysisMetaOptsAnova = new SelectItem[beans.size()];
            for (int i = 0; i < beans.size(); i++) {
                String metaname = beans.get(i).getName();
                if (metaname.equals("Time")) {
                    analysisMetaOptsAnova[i] = new SelectItem(beans.get(i).getName(), beans.get(i).getName(), null, true);
                } else {
                    analysisMetaOptsAnova[i] = new SelectItem(beans.get(i).getName(), beans.get(i).getName(), null, false);
                }
            }

        }
        return analysisMetaOptsAnova;
    }

    public String getCovStyleOpt() {
        return covStyleOpt;
    }

    public void setCovStyleOpt(String covStyleOpt) {
        this.covStyleOpt = covStyleOpt;
    }

    public List<SampleBean> getUniqueMetaNames() {
        return uniqueMetaNames;
    }

    public void setUniqueMetaNames(List<SampleBean> uniqueMetaNames) {
        this.uniqueMetaNames = uniqueMetaNames;
    }

    public List<String> getUniqueMetaList() {
        return uniqueMetaList;
    }

    public void setUniqueMetaList(List<String> uniqueMetaList) {
        this.uniqueMetaList = uniqueMetaList;
    }

    public void updateMetaOrder() throws REngineException {
        String[] metaOrderArray = uniqueMetaList.toArray(String[]::new);
        RDataUtils.updateMetaOrder(sb.getRConnection(), selectedMetaData, metaOrderArray);
        DataUtils.updateMsg("Info", "Successfully updated the order of metadata groups!");
    }

}
