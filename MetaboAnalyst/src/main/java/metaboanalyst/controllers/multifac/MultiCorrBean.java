/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package metaboanalyst.controllers.multifac;

import java.io.Serializable;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import metaboanalyst.controllers.SessionBean1;
import metaboanalyst.rwrappers.RDataUtils;
import metaboanalyst.rwrappers.TimeSeries;
import metaboanalyst.rwrappers.UniVarTests;
import metaboanalyst.utils.DataUtils;
import org.rosuda.REngine.Rserve.RConnection;

/**
 *
 * @author xia
 */
@ViewScoped
@Named("mCorrBean")
public class MultiCorrBean implements Serializable {

    private final SessionBean1 sb = (SessionBean1) DataUtils.findBean("sessionBean1");
    private final MultifacBean tb = (MultifacBean) DataUtils.findBean("multifacBean");
    private final String pageID = "Correlations";

    private String covFeatures;

    public String getCovFeatures() {
        return covFeatures;
    }

    public void setCovFeatures(String covFeatures) {
        this.covFeatures = covFeatures;
    }

    private String ptnMetaCov;

    public String getPtnMetaCov() {
        return ptnMetaCov;
    }

    public void setPtnMetaCov(String ptnMetaCov) {
        this.ptnMetaCov = ptnMetaCov;
    }

    private String ptnFeature = "";

    public String getPtnFeature() {
        return ptnFeature;
    }

    public void setPtnFeature(String ptnFeature) {
        this.ptnFeature = ptnFeature;
    }

    private String tgtType = "metaNm";

    public String getTgtType() {
        return tgtType;
    }

    public void setTgtType(String ptnType) {
        this.tgtType = ptnType;
    }

    private String covType = "none";

    public String getCovType() {
        return covType;
    }

    public void setCovType(String ptnTypeCov) {
        this.covType = ptnTypeCov;
    }

    private String ptnMeta;

    public String getPtnMeta() {
        return ptnMeta;
    }

    public void setPtnMeta(String ptnMeta) {
        this.ptnMeta = ptnMeta;
    }

    private String ptnDistMeasure = "pearson";

    public String getPtnDistMeasure() {
        return ptnDistMeasure;
    }

    public void setPtnDistMeasure(String ptnDistMeasure) {
        this.ptnDistMeasure = ptnDistMeasure;
    }

    private String[] covMetas;

    public String[] getCovMetas() {
        return covMetas;
    }

    public void setCovMetas(String[] covMetas) {
        this.covMetas = covMetas;
    }

    public void doDefaultCorr() {

        if (!sb.isAnalInit(pageID)) {
            sb.registerPage(pageID);
            //do something here
        }
    }

    public String corBtn_action() {
        String imgName = sb.getNewImage("ptn");
        RConnection RC = sb.getRConnection();

        String tgtNm;

        if (covMetas == null) {
            String[] meta = new String[1];
            meta[0] = tb.getMetaDataBeans().get(1).getName();
            covMetas = meta;
        }

        if (tgtType.equals("featNm")) {
            if (ptnFeature.isEmpty()) {
                DataUtils.updateMsg("Error", "Please specify a feature of interest!");
                return null;
            }
            tgtNm = ptnFeature;
        } else {
            for (String covMeta : covMetas) {
                if (covMeta.equals(ptnMeta)) {
                    DataUtils.updateMsg("Error", "Please make sure covariate is not the same as target.");
                    return null;
                }
            }
            tgtNm = ptnMeta;
        }

        if (TimeSeries.performCorrAnal(sb, ptnDistMeasure, tgtType, tgtNm, covMetas)) {
            UniVarTests.plotMatchedFeatures(sb, imgName, tgtType, "png", 72);
            tb.setCorrPerformed(true);
        } else {
            DataUtils.updateMsg("Error", RDataUtils.getErrMsg(RC));
        }
        return null;
    }
}
