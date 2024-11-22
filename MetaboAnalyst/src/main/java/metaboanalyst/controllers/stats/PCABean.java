/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package metaboanalyst.controllers.stats;

import java.io.Serializable;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import javax.faces.model.SelectItem;
import metaboanalyst.controllers.DetailsBean;
import metaboanalyst.controllers.SessionBean1;
import metaboanalyst.rwrappers.ChemoMetrics;
import metaboanalyst.utils.DataUtils;
import org.primefaces.PrimeFaces;

/**
 *
 * @author jianguox
 */
@RequestScoped
@Named("pcaBean")
public class PCABean implements Serializable {

    private final SessionBean1 sb = (SessionBean1) DataUtils.findBean("sessionBean1");
    private int pcaPairNum = 5;

    public int getPcaPairNum() {
        return pcaPairNum;
    }

    public void setPcaPairNum(int pcaPairNum) {
        this.pcaPairNum = pcaPairNum;
    }

    //for feature label on loading plot
    private String loadOpt = "all";

    public String getLoadOpt() {
        return loadOpt;
    }

    public void setLoadOpt(String plsLoadOpt) {
        this.loadOpt = plsLoadOpt;
    }

    public SelectItem[] getPcaPCs() {
        int pcNums = ChemoMetrics.getMaxPCACompNumber(sb) - 2;
        SelectItem[] items = new SelectItem[pcNums];
        for (int i = 0; i < pcNums; i++) {
            int pcNum = i + 2;
            items[i] = new SelectItem(pcNum, pcNum + "");
        }
        return items;
    }

    public SelectItem[] getPcaAllPCs() {
        int pcNums = ChemoMetrics.getMaxPCACompNumber(sb) - 1;
        SelectItem[] items = new SelectItem[pcNums];
        for (int i = 0; i < pcNums; i++) {
            int pcNum = i + 1;
            items[i] = new SelectItem(pcNum, pcNum + "");
        }
        return items;
    }

    public String pcaPairBtn_action() {
        ChemoMetrics.plotPCAPairSummary(sb, sb.getNewImage("pca_pair"), "png", 72, pcaPairNum);
        PrimeFaces.current().scrollTo("ac:form1:pairPane");
        return null;
    }

    private boolean greyScale = false;

    public boolean isGreyScale() {
        return greyScale;
    }

    public void setGreyScale(boolean greyScale) {
        this.greyScale = greyScale;
    }

    private boolean diffShapes = true;

    public boolean isDiffShapes() {
        return diffShapes;
    }

    public void setDiffShapes(boolean diffShapes) {
        this.diffShapes = diffShapes;
    }

    private int pcaScreeNum = 5;

    public int getPcaScreeNum() {
        return pcaScreeNum;
    }

    public void setPcaScreeNum(int pcaScreeNum) {
        this.pcaScreeNum = pcaScreeNum;
    }

    public String pcaScreeBtn_action() {
        ChemoMetrics.plotPCAScree(sb, sb.getNewImage("pca_scree"), "png", 72, pcaScreeNum);
        PrimeFaces.current().scrollTo("ac:form2:screePane");
        return null;
    }

    private boolean displayNames = false;

    public boolean isDisplayNames() {
        return displayNames;
    }

    public void setDisplayNames(boolean displayNames) {
        this.displayNames = displayNames;
    }

    private boolean displayFeatNames = true;

    public boolean isDisplayFeatNames() {
        return displayFeatNames;
    }

    public void setDisplayFeatNames(boolean displayFeatNames) {
        this.displayFeatNames = displayFeatNames;
    }

    private boolean displayConfs = true;

    public boolean isDisplayConfs() {
        return displayConfs;
    }

    public void setDisplayConfs(boolean displayConfs) {
        this.displayConfs = displayConfs;
    }

    private int pcaScoreX = 1;
    private int pcaScoreY = 2;

    public int getPcaScoreX() {
        return pcaScoreX;
    }

    public void setPcaScoreX(int pcaScoreX) {
        this.pcaScoreX = pcaScoreX;
    }

    public int getPcaScoreY() {
        return pcaScoreY;
    }

    public void setPcaScoreY(int pcaScoreY) {
        this.pcaScoreY = pcaScoreY;
    }

    public String pcaScore2dBtn_action() {
        if (pcaScoreX == pcaScoreY) {
            DataUtils.updateMsg("Error", "X and Y axes are of the same PC");
        } else {
            double conf = 0.95;
            if (!displayConfs) {
                conf = 0;
            }
            int showNames = 0;
            if (displayNames) {
                showNames = 1;
            }

            int useGreyScale = 0;
            if (greyScale) {
                useGreyScale = 1;
            }
            ChemoMetrics.plotPCA2DScore(sb, sb.getNewImage("pca_score2d"), "png", 72, pcaScoreX, pcaScoreY, conf, showNames, useGreyScale);
            PrimeFaces.current().scrollTo("ac:form3:score2dPane");
        }
        return null;
    }

    private int pcaScore3dX = 1;
    private int pcaScore3dY = 2;
    private int pcaScore3dZ = 3;
    private int rotationAngle = 40;

    public int getPcaScore3dX() {
        return pcaScore3dX;
    }

    public void setPcaScore3dX(int pcaScore3dX) {
        this.pcaScore3dX = pcaScore3dX;
    }

    public int getPcaScore3dY() {
        return pcaScore3dY;
    }

    public void setPcaScore3dY(int pcaScore3dY) {
        this.pcaScore3dY = pcaScore3dY;
    }

    public int getPcaScore3dZ() {
        return pcaScore3dZ;
    }

    public void setPcaScore3dZ(int pcaScore3dZ) {
        this.pcaScore3dZ = pcaScore3dZ;
    }

    public int getRotationAngle() {
        return rotationAngle;
    }

    public void setRotationAngle(int rotationAngle) {
        this.rotationAngle = rotationAngle;
    }

    public void pcaScore3dBtn_action(boolean onCurrentPage) {
        if (pcaScore3dX == pcaScore3dY || pcaScore3dX == pcaScore3dZ || pcaScore3dY == pcaScore3dZ) {
            DataUtils.updateMsg("Error", "Detected the same PC on two axes!");
        } else {
            //ChemoMetrics.PlotPCA3DScore(sb, sb.getNewImage("pca_score3d"), "png", 72, pcaScore3dX, pcaScore3dY, pcaScore3dZ, rotationAngle);
            ChemoMetrics.plotPCA3DScore(sb, sb.getNewImage("pca_score3d"), "json", 72, pcaScore3dX, pcaScore3dY, pcaScore3dZ);
            ChemoMetrics.plotPCA3DLoading(sb, sb.getCurrentImage("pca_loading3d"), "json", 72, pcaScore3dX, pcaScore3dY, pcaScore3dZ);
        }
    }

    private int pcaLoadX = 1;
    private int pcaLoadY = 2;

    public int getPcaLoadX() {
        return pcaLoadX;
    }

    public void setPcaLoadX(int pcaLoadX) {
        this.pcaLoadX = pcaLoadX;
    }

    public int getPcaLoadY() {
        return pcaLoadY;
    }

    public void setPcaLoadY(int pcaLoadY) {
        this.pcaLoadY = pcaLoadY;
    }

    private String loadPlotOpt = "scatter";

    public String getLoadPlotOpt() {
        return loadPlotOpt;
    }

    public void setLoadPlotOpt(String loadPlotOpt) {
        this.loadPlotOpt = loadPlotOpt;
    }

    public void pcaSaveViewBtn_action() {
        DataUtils.updateMsg("OK", "The current view is save for report generation. Please proceed to <b>Download</b> page to generate reports.");
    }

    public void pcaLoadBtn_action() {
        if (pcaLoadX == pcaLoadY) {
            DataUtils.updateMsg("Error", "Detected the same PC on two axes!");
        } else {
            ChemoMetrics.updatePCALoadType(sb, loadOpt);
            ChemoMetrics.plotPCALoading(sb, sb.getNewImage("pca_loading"), "png", 72, pcaLoadX, pcaLoadY);
            DetailsBean db = (DetailsBean) DataUtils.findBean("detailsBean");
            db.update1CompModel("pca");
            if (loadOpt.equals("custom")) {
                DataUtils.updateMsg("OK", "Please first click the points of interest and then re-gerenate the Splot in Image Dialog");
            } else {
                DataUtils.updateMsg("OK", "You can now re-generate the plot using the Image Dialog");
            }
        }
    }

    private int pcaBiplotX = 1;
    private int pcaBiplotY = 2;

    public int getPcaBiplotX() {
        return pcaBiplotX;
    }

    public void setPcaBiplotX(int pcaBiplotX) {
        this.pcaBiplotX = pcaBiplotX;
    }

    public int getPcaBiplotY() {
        return pcaBiplotY;
    }

    public void setPcaBiplotY(int pcaBiplotY) {
        this.pcaBiplotY = pcaBiplotY;
    }

    public String pcaBiplotBtn_action() {
        if (pcaBiplotX == pcaBiplotY) {
            DataUtils.updateMsg("Error", "Detected the same PC on two axes!");
        } else {
            ChemoMetrics.plotPCABiplot(sb, sb.getNewImage("pca_biplot"), "png", 72, pcaBiplotX, pcaBiplotY);
            PrimeFaces.current().scrollTo("ac:form6:biplotPane");
        }
        return null;
    }

    public void flipPCA() {
        ChemoMetrics.flipPCA(sb, flipOpt);
        pcaPairBtn_action();
        pcaScore2dBtn_action();
        pcaLoadBtn_action();
        pcaBiplotBtn_action();
        pcaScore3dBtn_action(false);
    }

    private String flipOpt = "y";

    public String getFlipOpt() {
        return flipOpt;
    }

    public void setFlipOpt(String flipOtp) {
        this.flipOpt = flipOtp;
    }
}
