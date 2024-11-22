/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package metaboanalyst.controllers.multifac;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import metaboanalyst.controllers.SessionBean1;
import metaboanalyst.models.MetaDataBean;
import metaboanalyst.rwrappers.RDataUtils;
import metaboanalyst.rwrappers.UniVarTests;
import metaboanalyst.utils.DataUtils;

import javax.enterprise.context.SessionScoped;
import javax.faces.model.SelectItem;
import javax.inject.Named;
import java.io.Serializable;
import java.util.List;

/**
 * @author xia
 */
@SessionScoped
@Named("lmBean")
@JsonIgnoreProperties(ignoreUnknown = true)
public class LimmaBean implements Serializable {

    @JsonIgnore
    private final SessionBean1 sb = (SessionBean1) DataUtils.findBean("sessionBean1");
    @JsonIgnore
    private final MultifacBean tb = (MultifacBean) DataUtils.findBean("multifacBean");

    private String analysisMeta;
    private String primaryType;

    private String[] adjustedMeta;
    private String referenceGroupFromAnalysisMeta = "NA";
    private String contrastFromAnalysisMeta = "anova";
    private String covPThresh = "0.05";
    @JsonIgnore

    private SelectItem[] referenceGroupFromAnalysisMetaOpts;
    @JsonIgnore
    private SelectItem[] contrastFromAnalysisMetaOpts;
    private String blockFac = "NA";
    private String covStyleOpt = "default";

    public String getAnalysisMeta() {
        if (analysisMeta == null) {
            List<MetaDataBean> beans = tb.getMetaDataBeans();
            analysisMeta = beans.get(0).getName();
            primaryType = RDataUtils.GetPrimaryType(sb.getRConnection(), analysisMeta);
        }
        return analysisMeta;
    }

    public void setAnalysisMeta(String analysisMeta) {
        this.analysisMeta = analysisMeta;
    }

    public String getCovPThresh() {
        return covPThresh;
    }

    public void setCovPThresh(String covPThresh) {
        this.covPThresh = covPThresh;
    }

    public String getPrimaryType() {
        return primaryType;
    }

    public void setPrimaryType(String primaryType) {
        this.primaryType = primaryType;
    }

    public String[] getAdjustedMeta() {
        return adjustedMeta;
    }

    public void setAdjustedMeta(String[] adjustedMeta) {
        this.adjustedMeta = adjustedMeta;
    }

    public String getReferenceGroupFromAnalysisMeta() {
        return referenceGroupFromAnalysisMeta;
    }

    public void setReferenceGroupFromAnalysisMeta(String referenceGroupFromAnalysisMeta) {
        this.referenceGroupFromAnalysisMeta = referenceGroupFromAnalysisMeta;
    }

    public SelectItem[] getReferenceGroupFromAnalysisMetaOpts() {
        String[] grpNames = RDataUtils.getUniqueMetaNames(sb.getRConnection(), analysisMeta);
        referenceGroupFromAnalysisMetaOpts = new SelectItem[grpNames.length];
        for (int i = 0; i < grpNames.length; i++) {
            referenceGroupFromAnalysisMetaOpts[i] = new SelectItem(grpNames[i], grpNames[i]);
        }
        return referenceGroupFromAnalysisMetaOpts;
    }

    public String getContrastFromAnalysisMeta() {
        return contrastFromAnalysisMeta;
    }

    public void setContrastFromAnalysisMeta(String contrastFromAnalysisMeta) {
        this.contrastFromAnalysisMeta = contrastFromAnalysisMeta;
    }

    public SelectItem[] getContrastFromAnalysisMetaOpts() {
        String[] grpNames = RDataUtils.getUniqueMetaNames(sb.getRConnection(), analysisMeta);
        contrastFromAnalysisMetaOpts = new SelectItem[grpNames.length + 1];
        contrastFromAnalysisMetaOpts[0] = new SelectItem("anova", "All contrasts (ANOVA-style)");
        for (int i = 0; i < grpNames.length; i++) {
            contrastFromAnalysisMetaOpts[i + 1] = new SelectItem(grpNames[i], grpNames[i]);
        }
        return contrastFromAnalysisMetaOpts;
    }

    public String getBlockFac() {
        return blockFac;
    }

    public void setBlockFac(String blockFac) {
        this.blockFac = blockFac;
    }

    public String getCovStyleOpt() {
        return covStyleOpt;
    }

    public void setCovStyleOpt(String covStyleOpt) {
        this.covStyleOpt = covStyleOpt;
    }

    public void analysisMetaChangeListener() {
        primaryType = RDataUtils.GetPrimaryType(sb.getRConnection(), analysisMeta);
        if (primaryType.equals("disc")) {
            String[] grpNames = RDataUtils.getUniqueMetaNames(sb.getRConnection(), analysisMeta);
            referenceGroupFromAnalysisMeta = grpNames[0];
        }
    }

    public void covScatterButton_action() {

        double sigThresh = Double.parseDouble(covPThresh);

        if (analysisMeta.equals(blockFac)) {
            DataUtils.updateMsg("Error", "Please make sure blocking factor is not the same as primary meta-data.");
            return;
        }

        if (referenceGroupFromAnalysisMeta.equals(contrastFromAnalysisMeta)) {
            DataUtils.updateMsg("Error", "Please make sure the reference group is not the same as the contrast group.");
            return;
        }

        for (String adjustedMeta1 : adjustedMeta) {
            if (adjustedMeta1.equals(analysisMeta)) {
                DataUtils.updateMsg("Error", "Please make sure primary data is not also selected in covariate options.");
                return;
            } else if (adjustedMeta1.equals(blockFac)) {
                DataUtils.updateMsg("Error", "Please make sure blocking factor is not also selected in covariate options.");
                return;
            }
        }

        String newName = sb.getNewImage("covariate_plot") + "dpi72";
        String imgName = newName + ".png";
        String covJsonName = newName + ".json";
        int res = UniVarTests.performCovariateAnal(sb, imgName, "png", 72, covStyleOpt, analysisMeta, adjustedMeta, referenceGroupFromAnalysisMeta, blockFac, sigThresh, contrastFromAnalysisMeta);
        if (res == -1) {
            DataUtils.updateMsg("Error", RDataUtils.getErrMsg(sb.getRConnection()));
            return;
        }

        if (res == 0) {
            sb.setLmSig(false);
        } else {
            sb.setLmSig(true);
        }

        String msg = RDataUtils.getCurrentMsg(sb.getRConnection());
        DataUtils.updateMsg("OK", msg);

        tb.setCovJsonName(covJsonName);
        tb.setCovPerformed(true);
    }
}
