/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package metaboanalyst.controllers.stats;

import java.io.Serializable;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import metaboanalyst.controllers.SessionBean1;
import metaboanalyst.rwrappers.ChemoMetrics;
import metaboanalyst.utils.DataUtils;
import org.primefaces.PrimeFaces;

/**
 *
 * @author jianguox
 */
@RequestScoped
@Named("oplsBean")
public class OPLSDABean implements Serializable {

    private final SessionBean1 sb = (SessionBean1) DataUtils.findBean("sessionBean1");

    private boolean displayConfs = true;
    private boolean displayNames = false;
    private boolean displayFeatNames = true;

    private String loadOpt = "all";
    private boolean grayScale = false; // for VIP
    private boolean greyScale = false;  //for Score plot
    private String permStat = "bw";
    private int permNum = 20;

    public boolean isGrayScale() {
        return grayScale;
    }

    public void setGrayScale(boolean grayScale) {
        this.grayScale = grayScale;
    }

    public boolean isGreyScale() {
        return greyScale;
    }

    public void setGreyScale(boolean greyScale) {
        this.greyScale = greyScale;
    }

    public boolean isDisplayConfs() {
        return displayConfs;
    }

    public void setDisplayConfs(boolean displayConfs) {
        this.displayConfs = displayConfs;
    }

    public boolean isDisplayNames() {
        return displayNames;
    }

    public void setDisplayNames(boolean displayNames) {
        this.displayNames = displayNames;
    }

    //loadOpt can be "all", "none", "custom"
    public String getLoadOpt() {
        return loadOpt;
    }

    public void setLoadOpt(String loadOpt) {
        this.loadOpt = loadOpt;
    }

    public boolean isDisplayFeatNames() {
        return displayFeatNames;
    }

    public void setDisplayFeatNames(boolean displayFeatNames) {
        this.displayFeatNames = displayFeatNames;
    }

    public String getPermStat() {
        return permStat;
    }

    public void setPermStat(String permStat) {
        this.permStat = permStat;
    }

    public int getPermNum() {
        return permNum;
    }

    public void setPermNum(int permNum) {
        this.permNum = permNum;
    }

    private String permMsg = "";

    public String getPermMsg() {
        return permMsg;
    }

    public String oplsScore2dBtn_action() {

        double conf = 0.95;
        if (!displayConfs) {
            conf = 0;
        }

        int useGreyScale = 0;
        if (greyScale) {
            useGreyScale = 1;
        }
        ChemoMetrics.plotOPLS2DScore(sb, sb.getNewImage("opls_score2d"), "png", 72, 1, 2, conf, displayNames ? 1 : 0, useGreyScale);
        PrimeFaces.current().scrollTo("ac:form3:score2dPane");

        return null;
    }

    private int activeTab = 0;

    public int getActiveTab() {
        return activeTab;
    }

    public void setActiveTab(int activeTab) {
        this.activeTab = activeTab;
    }

    public void updateOrthoPLSDA() {
        ChemoMetrics.initOPLS(sb);
        ChemoMetrics.plotOPLS2DScore(sb, sb.getNewImage("opls_score2d"), "png", 72, 1, 2, 0.95, 1, 0);
        ChemoMetrics.plotOplsSplot(sb, sb.getNewImage("opls_splot"), "all", "png", 72);
        ChemoMetrics.plotOPLSImp(sb, sb.getCurrentImage("opls_imp"), "png", 72, "vip", "tscore", 15, "FALSE");
        ChemoMetrics.plotOplsMdlView(sb, sb.getNewImage("opls_mdl"), "png", 72);
    }

    public String oplsPermBtn_action() {
        ChemoMetrics.performOPLSPermute(sb, permNum, permStat);
        permMsg = ChemoMetrics.plotOPLSPermutation(sb, sb.getNewImage("opls_perm"), "png", 72, permNum);
        PrimeFaces.current().scrollTo("ac:form7:permPane");
        return null;
    }

    public void updateSplot() {
        ChemoMetrics.updateOplsSplotType(sb, loadOpt);
        ChemoMetrics.plotOplsSplot(sb, sb.getNewImage("opls_splot"), loadOpt, "png", 72);
        if (loadOpt.equals("custom")) {
            DataUtils.updateMsg("OK", "Please first click the points of interest and then re-gerenate the Splot in Image Dialog");
        } else {
            DataUtils.updateMsg("OK", "You can now re-generate the plot using the Image Dialog");
        }
    }

    private String impOpt = "vip";

    public String getImpOpt() {
        return impOpt;
    }

    public void setImpOpt(String impOpt) {
        this.impOpt = impOpt;
    }
    
    private String compOpt = "tscore";

    public String getCompOpt() {
        return compOpt;
    }

    public void setCompOpt(String compOpt) {
        this.compOpt = compOpt;
    }

    private int impFeatNum = 15;

    public int getImpFeatNum() {
        return impFeatNum;
    }

    public void setImpFeatNum(int vipFeatNum) {
        this.impFeatNum = vipFeatNum;
    }

    public String oplsImpBtn_action() {

        ChemoMetrics.plotOPLSImp(sb, sb.getNewImage("opls_imp"), "png", 72,
                impOpt, compOpt, impFeatNum, grayScale ? "TRUE" : "FALSE");
        PrimeFaces.current().scrollTo("ac:form6:impPane");
        return null;
    }

    public String getDownloadLink() {
        return "<a target=\"_blank\" href = \"/MetaboAnalyst/resources/users/" + sb.getCurrentUser().getName() + "/oplsda_model.csv\"><b>" + "Details" + "</b></a>";
    }

}
