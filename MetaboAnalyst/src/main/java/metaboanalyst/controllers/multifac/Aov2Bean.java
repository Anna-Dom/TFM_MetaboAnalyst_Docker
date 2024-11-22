/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package metaboanalyst.controllers.multifac;

import java.io.Serializable;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import metaboanalyst.controllers.DetailsBean;
import metaboanalyst.controllers.SessionBean1;
import metaboanalyst.rwrappers.RDataUtils;
import metaboanalyst.rwrappers.TimeSeries;
import metaboanalyst.utils.DataUtils;

/**
 *
 * @author xia
 */
@ViewScoped
@Named("aov2Bean")
public class Aov2Bean implements Serializable {

    private final SessionBean1 sb = (SessionBean1) DataUtils.findBean("sessionBean1");
    private final MultifacBean tb = (MultifacBean) DataUtils.findBean("multifacBean");
    private final String pageID = "ANOVA2";

    private double pThresh = 0.05;

    public double getPthresh() {
        return pThresh;
    }

    public void setPthresh(double pThresh) {
        this.pThresh = pThresh;
    }

    private String pvalOpt = "fdr";

    public String getPvalOpt() {
        return pvalOpt;
    }

    public void setPvalOpt(String pvalOpt) {
        this.pvalOpt = pvalOpt;
    }
    
    private String phenOpt = "between";

    public String getPhenOpt() {
        return phenOpt;
    }

    public void setPhenOpt(String phenOpt) {
        this.phenOpt = phenOpt;
    }    

    private String[] selectedMetasAnova = null;

    public String[] getSelectedMetasAnova() {
        return selectedMetasAnova;
    }

    public void setSelectedMetasAnova(String[] selectedMetasAnova) {
        this.selectedMetasAnova = selectedMetasAnova;
    }

    public void doDefaultAov2() {
        if (!sb.isAnalInit(pageID)) {
            sb.registerPage(pageID);
        }
        
        if (selectedMetasAnova == null) {
            selectedMetasAnova = tb.getDiscMetaOpts();
        }

    }

    public void aov2Bn_action() {

        if (selectedMetasAnova.length != 2) {
            DataUtils.updateMsg("Error", "Please select exactly two meta-data classes!");
            return;
        }

        int res = TimeSeries.initANOVA2(sb.getRConnection(), pThresh, pvalOpt, sb.getTsDesign(), phenOpt, selectedMetasAnova);
        switch (res) {
            case 0:
                DataUtils.updateMsg("Error", RDataUtils.getErrMsg(sb.getRConnection()));
                break;
            case -1:
                DataUtils.updateMsg("Error", "Selected metadata must be categorical!");
                break;
            default:
                DetailsBean db = (DetailsBean) DataUtils.findBean("detailsBean");
                db.update2CompModel("aov2");
                TimeSeries.plotAOV2(sb, sb.getNewImage("aov2"), "png", 72);
                tb.setAov2Performed(true);
                break;
        }
    }
}
