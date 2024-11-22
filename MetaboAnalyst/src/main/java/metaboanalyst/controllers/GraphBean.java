/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package metaboanalyst.controllers;

import java.io.File;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import javax.inject.Named;
import javax.enterprise.context.RequestScoped;
import metaboanalyst.controllers.metapath.MetaPathStatBean;
import metaboanalyst.controllers.stats.RocAnalBean;
import metaboanalyst.controllers.multifac.MultifacBean;
import metaboanalyst.models.ColorBean;
import metaboanalyst.rwrappers.RCenter;
import metaboanalyst.rwrappers.RDataUtils;
import metaboanalyst.rwrappers.RGraphUtils;
import metaboanalyst.rwrappers.RocUtils;
import metaboanalyst.rwrappers.TimeSeries;
import metaboanalyst.rwrappers.UniVarTests;
import metaboanalyst.spectra.SpectraProcessBean;
import metaboanalyst.utils.DataUtils;
import org.rosuda.REngine.Rserve.RConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author jianguox
 */
@RequestScoped
@Named("graphBean")
public class GraphBean implements Serializable {

    private final SessionBean1 sb = (SessionBean1) DataUtils.findBean("sessionBean1");
    private static final Logger LOGGER = LogManager.getLogger(GenericControllers.class);

    public void updateColorScheme() {
        updateGraphSetting();
        DataUtils.updateMsg("OK", "You can now update the images to see the effect.");
    }

    private void updateGraphSetting() {
        List<ColorBean> colorBeanLists = sb.getColorBeanLists();
        int grpNum = colorBeanLists.size();
        if (grpNum > 0) {
            String[] cols = new String[grpNum];
            int[] sps = new int[grpNum];
            for (int i = 0; i < grpNum; i++) {
                String col = colorBeanLists.get(i).getColorPopup();
                int sp = colorBeanLists.get(i).getShapeType();
                if (col == null || col.trim().isEmpty()) {
                    DataUtils.updateMsg("Warning:", "Some group does not have a color specified. Default color will be applied.");
                    col = "NA";
                }
                cols[i] = "#" + col;

                if (sp < 0 | sp > 25) {
                    DataUtils.updateMsg("Warning:", "Some group shape is invalid. Default shape will be applied.");
                    sp = 0;
                }
                sps[i] = sp;
            }
            RDataUtils.updateGraphSettings(sb.getRConnection(), cols, sps);
        }
    }

    public void graphBn_action() {

        String key = sb.getImageSource();
        String imgName;
        String mydpi = "72";
        String formatOpt = sb.getFormatOpt();
        if (formatOpt.equals("png") || formatOpt.equals("tiff")) {
            mydpi = sb.getDpiOpt() + "";
        }
        switch (key) {
            case "pathway":
                String currentPathName = sb.getCurrentPathName();
                if (currentPathName == null) {
                    DataUtils.updateMsg("Error", "No command found for plotting the image!");
                    return;
                }
                imgName = RGraphUtils.plotKEGGPath(sb, currentPathName, sb.getSizeOpt(), sb.getSizeOpt(), formatOpt, mydpi);
                break;
            case "mb": {
                String cmpdName = sb.getCurrentCmpdName();
                if (cmpdName == null) {
                    DataUtils.updateMsg("Error", "No command found for plotting the image!");
                    return;
                }
                imgName = TimeSeries.plotMBTimeProfile(sb, cmpdName, 100, formatOpt, mydpi + "");
                break;
            }
            case "cmpd": {
                String cmpdName = sb.getCurrentCmpdName();
                if (cmpdName == null) {
                    DataUtils.updateMsg("Error", "No command found for plotting the image!");
                    return;
                }
                imgName = UniVarTests.plotCmpdSummary(sb, cmpdName, "NA", 100, formatOpt, mydpi + "");
                break;
            }
            case "roc.univ": {
                RocAnalBean rb = (RocAnalBean) DataUtils.findBean("rocAnalBean");
                String cmpdName = rb.getCurrentCmpd();
                if (cmpdName == null) {
                    DataUtils.updateMsg("Error", "No command found for plotting the image!");
                    return;
                }
                String isAUC = rb.isShowCI() ? "T" : "F";
                String isOpt = rb.isShowOptPoint() ? "T" : "F";
                String optMtd = rb.getOptimalDD();
                String isPartial = rb.isPartialRoc() ? "T" : "F";
                String measure = rb.getUnivPerfOpt();
                double mycutoff = rb.getUnivThresh();
                imgName = RocUtils.performUnivROC(sb, cmpdName, 100, formatOpt, mydpi, isAUC, isOpt, optMtd, isPartial, measure, mycutoff);
                break;
            }
            case "roc.boxplot": {
                RocAnalBean rb = (RocAnalBean) DataUtils.findBean("rocAnalBean");
                String cmpdName = rb.getCurrentCmpd();
                if (cmpdName == null) {
                    DataUtils.updateMsg("Error", "No command found for plotting the image!");
                    return;
                }
                String isOpt = rb.isShowOptPoint() ? "T" : "F";
                imgName = RocUtils.plotUnivROCBP(sb, cmpdName, 100, formatOpt, mydpi, isOpt, "FALSE");
                break;
            }
            case "RTcor": {
                SpectraProcessBean spb = (SpectraProcessBean) DataUtils.findBean("spectraProcessor");
                int figureNM = spb.getFigureCount();
                int figureNMNew = figureNM + 1;
                spb.setFigureCount(figureNMNew);
                imgName = spb.plotRTcor();
                spb.internalizeImage(imgName);
                break;
            }
            case "BPIcor": {
                SpectraProcessBean spb = (SpectraProcessBean) DataUtils.findBean("spectraProcessor");
                int figureNM = spb.getFigureCount();
                int figureNMNew = figureNM + 1;
                spb.setFigureCount(figureNMNew);
                imgName = spb.plotBPIcor();
                spb.internalizeImage(imgName);
                break;
            }
            case "IntensitySpec": {
                SpectraProcessBean spb = (SpectraProcessBean) DataUtils.findBean("spectraProcessor");
                int figureNM = spb.getFigureCount();
                int figureNMNew = figureNM + 1;
                spb.setFigureCount(figureNMNew);
                imgName = spb.plotIntenStats();
                spb.internalizeImage(imgName);
                break;
            }
            case "TICs": {
                SpectraProcessBean spb = (SpectraProcessBean) DataUtils.findBean("spectraProcessor");
                int figureNM = spb.getFigureCount();
                int figureNMNew = figureNM + 1;
                spb.setFigureCount(figureNMNew);
                imgName = spb.plotTICs();
                spb.internalizeImage(imgName);
                break;
            }
            case "BPIs": {
                SpectraProcessBean spb = (SpectraProcessBean) DataUtils.findBean("spectraProcessor");
                int figureNM = spb.getFigureCount();
                int figureNMNew = figureNM + 1;
                spb.setFigureCount(figureNMNew);
                imgName = spb.plotBPIs();
                spb.internalizeImage(imgName);
                break;
            }
            case "TIC": {
                SpectraProcessBean spb = (SpectraProcessBean) DataUtils.findBean("spectraProcessor");
                int figureNM = spb.getFigureCount();
                int figureNMNew = figureNM + 1;
                spb.setFigureCount(figureNMNew);
                imgName = spb.plotTIC();
                spb.internalizeImage(imgName);
                break;
            }
            case "EIC_int": {
                SpectraProcessBean spb = (SpectraProcessBean) DataUtils.findBean("spectraProcessor");
                imgName = spb.plotMSfeatureUpdate();
                spb.internalizeImage(imgName);
                break;
            }
            case "EICs": {
                SpectraProcessBean spb = (SpectraProcessBean) DataUtils.findBean("spectraProcessor");
                String FeatureName = spb.plotXICUpdate();
                imgName = FeatureName;
                spb.internalizeImage(imgName);
                break;
            }
            case "EICg": {
                SpectraProcessBean spb = (SpectraProcessBean) DataUtils.findBean("spectraProcessor");
                int dpi = 72;
                if (sb.getFormatOpt().equals("png")) {
                    dpi = sb.getDpiOpt();
                }
                String FeatureName = spb.plotXICUpdate();
                imgName = "EIC_" + FeatureName + "_group_" + dpi + "." + sb.getFormatOpt();
                spb.internalizeImage(imgName);
                break;
            }
            case "metapath": {
                MetaPathStatBean mpsb = (MetaPathStatBean) DataUtils.findBean("pMetaStatBean");
                imgName = mpsb.updatePlotPathMetaHigRes();
                //System.out.println(imgName);
                break;
            }
            default:
                String rcmd = sb.getGraphicsMap().get(key);
                if (key.equals("cls_test_roc")) {
                    rcmd = sb.getGraphicsMap().get("cls_roc");
                } else if (key.equals("cls_test_prob")) {
                    rcmd = sb.getGraphicsMap().get("cls_prob");
                }
                if (rcmd == null) {
                    DataUtils.updateMsg("Error", "No command found for plotting the image!");
                    return;
                }
                if (key.equals("covariate_plot")) {
                    MultifacBean tb = (MultifacBean) DataUtils.findBean("multifacBean");
                    rcmd = rcmd.replace("default", tb.getCovStyleOpt());
                }
                rcmd = rcmd.replace("png", formatOpt);
                rcmd = rcmd.replace("72", mydpi + "");
                rcmd = rcmd.replace("width=NA", "width=" + sb.getSizeOpt());
                imgName = sb.getCurrentImage(key);
                imgName = imgName.replaceAll("\\/", "_");
                imgName = imgName + "dpi" + mydpi + "." + formatOpt;
                try {
                    RConnection RC = sb.getRConnection();
                    RCenter.recordRCommand(RC, rcmd);
                    RC.voidEval(rcmd);
                } catch (Exception e) {
                    // e.printStackTrace();
                    LOGGER.error("graphBn_action", e);
                }
                break;
        }

        String imgDownloadTxt = "<b>Download the image: </b> <a target='_blank' href = \"/MetaboAnalyst/resources/users/" + sb.getCurrentUser().getName()
                + File.separator + imgName + "\"><b>" + imgName + "</b></a>";
        sb.setImgDownloadTxt(imgDownloadTxt);
    }

    private final List<String> shapeColImgs = Arrays.asList(new String[]{"pca_pair", "pca_score2d", "pls_pair", "pls_score2d",
        "spls_pair", "spls_score2d", "opls_score2d", "cmpd", "roc.boxplot", "tree", "heatmap", "mb"});

    public boolean isRenderStatus() {
        String key = sb.getImageSource();
        return key != null && shapeColImgs.contains(key);
    }
}
