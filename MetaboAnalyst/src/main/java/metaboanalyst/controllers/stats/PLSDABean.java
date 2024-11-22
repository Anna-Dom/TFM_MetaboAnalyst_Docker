/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package metaboanalyst.controllers.stats;

import java.io.File;
import java.io.Serializable;
import java.util.concurrent.Semaphore;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import javax.faces.model.SelectItem;
import metaboanalyst.controllers.ApplicationBean1;
import metaboanalyst.controllers.DetailsBean;
import metaboanalyst.controllers.SessionBean1;
import metaboanalyst.rwrappers.ChemoMetrics;
import metaboanalyst.rwrappers.RDataUtils;
import metaboanalyst.utils.DataUtils;
import org.primefaces.PrimeFaces;

/**
 *
 * @author jianguox
 */
@RequestScoped
@Named("plsBean")
public class PLSDABean implements Serializable {

    private final ApplicationBean1 ab = (ApplicationBean1) DataUtils.findBean("applicationBean1");
    private final SessionBean1 sb = (SessionBean1) DataUtils.findBean("sessionBean1");
    private int plsPairNum = 5;
    private int plsScore2dX = 1;
    private int plsScore2dY = 2;
    private boolean displayConfs = true;
    private boolean displayNames = false;
    private boolean displayFeatNames = true;

    private int plsScore3dX = 1;
    private int plsScore3dY = 2;
    private int plsScore3dZ = 3;
    private int rotationAngle = 40;
    private int plsLoadX = 1;
    private int plsLoadY = 2;
    private String loadOpt = "all";
    private String impOpt = "vip";
    private int searchCompNum = 0;
    private int cvFoldNum = 5;
    private String cvOpt = "5";
    private String perfMeasure = "Q2";
    private String vipOpt;
    private String coefOpt;
    private int impFeatNum = 15;
    private boolean grayScale = false; // for VIP
    private boolean greyScale = false;  //for Score plot
    private String permStat = "bw";
    private int permNum = 0;
    private String plsVarOpt = "xvar";

    public String getPlsVarOpt() {
        return plsVarOpt;
    }

    public void setPlsVarOpt(String plsVarOpt) {
        this.plsVarOpt = plsVarOpt;
    }

    public String getCvOpt() {
        return cvOpt;
    }

    public void setCvOpt(String cvOpt) {
        this.cvOpt = cvOpt;
        if (cvOpt.equals("loo")) {
            if (sb.getSampleNumber() > 60) {
                DataUtils.updateMsg("Warning", "Reset to 5-fold CV. LOOCV is permitted for small data (< 60) samples on our server.");
                this.cvOpt = "5";
            }
        } else {
            cvFoldNum = Integer.parseInt(cvOpt);
        }
    }

    public SelectItem[] getPlsPCs() {
        int maxNum = ChemoMetrics.getMaxPCACompNumber(sb);
        int pcNums = (maxNum > 2) ? maxNum - 2 : 3;
        SelectItem[] items = new SelectItem[pcNums];
        for (int i = 0; i < pcNums; i++) {
            int pcNum = i + 2;
            items[i] = new SelectItem(pcNum, pcNum + "");
        }
        return items;
    }

    public SelectItem[] getPlsAllPCs() {
        int pcNums = ChemoMetrics.getMaxPCACompNumber(sb) - 1;
        SelectItem[] items = new SelectItem[pcNums];
        for (int i = 0; i < pcNums; i++) {
            int pcNum = i + 1;
            items[i] = new SelectItem(pcNum, pcNum + "");
        }
        return items;
    }

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

    public int getPlsPairNum() {
        return plsPairNum;
    }

    public void setPlsPairNum(int plsPairNum) {
        this.plsPairNum = plsPairNum;
    }

    public int getPlsScore2dX() {
        return plsScore2dX;
    }

    public void setPlsScore2dX(int plsScore2dX) {
        this.plsScore2dX = plsScore2dX;
    }

    public int getPlsScore2dY() {
        return plsScore2dY;
    }

    public void setPlsScore2dY(int plsScore2dY) {
        this.plsScore2dY = plsScore2dY;
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

    public int getPlsScore3dX() {
        return plsScore3dX;
    }

    public void setPlsScore3dX(int plsScore3dX) {
        this.plsScore3dX = plsScore3dX;
    }

    public int getPlsScore3dY() {
        return plsScore3dY;
    }

    public void setPlsScore3dY(int plsScore3dY) {
        this.plsScore3dY = plsScore3dY;
    }

    public int getPlsScore3dZ() {
        return plsScore3dZ;
    }

    public void setPlsScore3dZ(int plsScore3dZ) {
        this.plsScore3dZ = plsScore3dZ;
    }

    public int getRotationAngle() {
        return rotationAngle;
    }

    public void setRotationAngle(int plsRotationAngle) {
        this.rotationAngle = plsRotationAngle;
    }

    public int getPlsLoadX() {
        return plsLoadX;
    }

    public void setPlsLoadX(int plsLoadX) {
        this.plsLoadX = plsLoadX;
    }

    public int getPlsLoadY() {
        return plsLoadY;
    }

    public void setPlsLoadY(int plsLoadY) {
        this.plsLoadY = plsLoadY;
    }

    public String getLoadOpt() {
        return loadOpt;
    }

    public void setLoadOpt(String plsLoadOpt) {
        this.loadOpt = plsLoadOpt;
    }

    public boolean isDisplayFeatNames() {
        return displayFeatNames;
    }

    public void setDisplayFeatNames(boolean displayFeatNames) {
        this.displayFeatNames = displayFeatNames;
    }

    public String getImpOpt() {
        return impOpt;
    }

    public void setImpOpt(String impOpt) {
        this.impOpt = impOpt;
    }

    public int getSearchCompNum() {
        if (searchCompNum == 0) {
            searchCompNum = ChemoMetrics.getDefaultPLSCVNumber(sb);
        }
        return ChemoMetrics.getDefaultPLSCVNumber(sb);
    }

    public void setSearchCompNum(int searchCompNum) {
        this.searchCompNum = searchCompNum;
    }

    public int getCvFoldNum() {
        return cvFoldNum;
    }

    public void setCvFoldNum(int cvFoldNum) {
        this.cvFoldNum = cvFoldNum;
    }

    public String getPerfMeasure() {
        return perfMeasure;
    }

    public void setPerfMeasure(String perfMeasure) {
        this.perfMeasure = perfMeasure;
    }

    public String getVipOpt() {
        return vipOpt;
    }

    public void setVipOpt(String vipOpt) {
        this.vipOpt = vipOpt;
    }

    public String getCoefOpt() {
        return coefOpt;
    }

    public void setCoefOpt(String coefOpt) {
        this.coefOpt = coefOpt;
    }

    public int getImpFeatNum() {
        return impFeatNum;
    }

    public void setImpFeatNum(int vipFeatNum) {
        this.impFeatNum = vipFeatNum;
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

    public void updatePLSDA() {
        ChemoMetrics.initPLS(sb);
        ChemoMetrics.plotPLSPairSummary(sb, sb.getNewImage("pls_pair"), "png", 72, plsPairNum);
        ChemoMetrics.plotPLS2DScore(sb, sb.getNewImage("pls_score2d"), "png", 72, 1, 2, 0.95, 1, 0);
        ChemoMetrics.plotPLS3DScore(sb, sb.getNewImage("pls_score3d"), "json", 72, 1, 2, 3);
        ChemoMetrics.plotPLSLoading(sb, sb.getNewImage("pls_loading"), "png", 72, 1, 2);
        ChemoMetrics.plotPLSImp(sb, sb.getNewImage("pls_imp"), "png", 72, "vip", "Comp. 1", 15, "FALSE");
        //plsCVBtn_action();
        DataUtils.updateMsg("Info", "Successfully updated all tabs except Cross Validation and Permutation - you can update them separately.");
    }

    public String plsPairBtn_action() {
        ChemoMetrics.plotPLSPairSummary(sb, sb.getNewImage("pls_pair"), "png", 72, plsPairNum);
        PrimeFaces.current().scrollTo("ac:form1:pairPane");
        return null;
    }

    public String plsScore2dBtn_action() {
        if (plsScore2dX == plsScore2dY) {
            DataUtils.updateMsg("Error", "X and Y axes are of the same PC");
        } else {
            double conf = 0.95;
            if (!displayConfs) {
                conf = 0;
            }

            int useGreyScale = 0;
            if (greyScale) {
                useGreyScale = 1;
            }
            ChemoMetrics.plotPLS2DScore(sb, sb.getNewImage("pls_score2d"), "png", 72, plsScore2dX, plsScore2dY, conf, displayNames ? 1 : 0, useGreyScale);
            PrimeFaces.current().scrollTo("ac:form3:score2dPane");
        }
        return null;
    }

    private int activeTab = 0;

    public int getActiveTab() {
        return activeTab;
    }

    public void setActiveTab(int activeTab) {
        this.activeTab = activeTab;
    }

    public String plsScore3dBtn_action() {
        if (plsScore3dX == plsScore3dY || plsScore3dX == plsScore3dZ || plsScore3dY == plsScore3dZ) {
            DataUtils.updateMsg("Error", "Detected the same PC on two axes!");
        } else {
            ChemoMetrics.plotPLS3DScore(sb, sb.getNewImage("pls_score3d"), "json", 72, plsScore3dX, plsScore3dY, plsScore3dZ);
            ChemoMetrics.plotPLS3DLoading(sb, sb.getCurrentImage("pls_loading3d"), "json", 72, 1, 2, 3);

            activeTab = 2;
        }
        return null;
    }

    public void plsLoadBtn_action() {

        if (plsLoadX == plsLoadY) {
            DataUtils.updateMsg("Error", "Detected the same PC on two axes!");
        } else {
            ChemoMetrics.updatePLSLoadType(sb, loadOpt);
            ChemoMetrics.plotPLSLoading(sb, sb.getNewImage("pls_loading"), "png", 72, plsLoadX, plsLoadY);
            DetailsBean db = (DetailsBean) DataUtils.findBean("detailsBean");
            db.update1CompModel("pls");
            if (loadOpt.equals("custom")) {
                DataUtils.updateMsg("OK", "Please first click the points of interest and then re-gerenate the Splot in Image Dialog");
            } else {
                DataUtils.updateMsg("OK", "You can now re-generate the plot using the Image Dialog");
            }
        }
    }

    public String plsCVBtn_action() {
        if (searchCompNum == 0) {
            searchCompNum = ChemoMetrics.getDefaultPLSCVNumber(sb);
        }

        int minSize = RDataUtils.getMinGroupSize(sb.getRConnection());
        if (minSize < 11 & cvOpt.equals("10")) {
            DataUtils.updateMsg("Warning", "Set to 5-fold CV. The size of the small group is too small for 10-fold CV.");
            cvOpt = "5";
        }

        int res = ChemoMetrics.trainPLSClassifier(sb, cvOpt, cvFoldNum, searchCompNum, perfMeasure);
        if (res == 1) {
            ChemoMetrics.plotPLSClassification(sb, sb.getNewImage("pls_cv"), "png", 72);
            PrimeFaces.current().scrollTo("ac:form2:cvPane");
        } else {
            DataUtils.updateMsg("Error", "The parameters cause errors in cross validation. Make sure you have at least 20 samples");
        }
        return null;
    }

    public String getPlsCVImg() {
        return ab.getRootContext() + sb.getCurrentUser().getRelativeDir() + File.separator + sb.getCurrentImage("pls_cv") + "dpi72.png";
    }

    public void plsImpBtn_action() {
        String imp = impOpt.equals("vip") ? vipOpt : coefOpt;
        if (imp.equalsIgnoreCase("NA")) {
            DataUtils.updateMsg("Error", "Please perform cross validation first");
        } else {
            ChemoMetrics.plotPLSImp(sb, sb.getNewImage("pls_imp"), "png", 72,
                    impOpt, imp, impFeatNum, grayScale ? "TRUE" : "FALSE");
        }
    }

    public void plsPermBtn_action() {
        if (permNum == 0) {
            DataUtils.updateMsg("Error", "Please specify the number of permutations!");
            return;
        }

        //throttling 
        Semaphore semphore = sb.getPermissionToStart();
        if (semphore == null) {
            return;
        }

        permMsg = ChemoMetrics.performPLSPermute(sb, permNum, permStat);
        ChemoMetrics.plotPLSPermutation(sb, sb.getNewImage("pls_perm"), "png", 72);
        PrimeFaces.current().scrollTo("ac:form7:permPane");

        semphore.release();
    }

    public String getPerfTxt() {
        double[][] vals = ChemoMetrics.getPLS_CVMat(sb);
        String[] rownames = ChemoMetrics.getPLSCVRowNames(sb);
        String[] colnames = ChemoMetrics.getPLSCVColNames(sb);
        String str = "<h2>PLS-DA cross validation details: </h2><br/>";
        str = str + DataUtils.setupTable("Measure", vals, rownames, colnames);
        return str;
    }

    public String impDetailsLnk_action() {
        if (impOpt.equals("vip")) {
            return sb.detailsLnk_action("pls.vip");
        } else {
            return sb.detailsLnk_action("pls.coef");
        }
    }

    public SelectItem[] getVipItems() {
        return createSelectItems(ChemoMetrics.getPLSSigColNames(sb, "vip"));
    }

    public SelectItem[] getCoefItems() {
        return createSelectItems(ChemoMetrics.getPLSSigColNames(sb, "coef"));
    }

    private SelectItem[] createSelectItems(String[] names) {
        if (names == null || names.length == 0) {
            SelectItem[] nmBeans = new SelectItem[1];
            nmBeans[0] = new SelectItem("NA", "--Not available-");
            return nmBeans;
        }
        SelectItem[] nmBeans = new SelectItem[names.length];
        for (int i = 0; i < names.length; i++) {
            nmBeans[i] = new SelectItem(names[i], names[i]);
        }
        return nmBeans;
    }

}
