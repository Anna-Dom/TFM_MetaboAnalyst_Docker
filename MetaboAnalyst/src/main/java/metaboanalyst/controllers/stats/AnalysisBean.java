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
import metaboanalyst.rwrappers.Classifying;
import metaboanalyst.rwrappers.Clustering;
import metaboanalyst.rwrappers.RDataUtils;
import metaboanalyst.rwrappers.SigVarSelect;
import metaboanalyst.rwrappers.UniVarTests;
import metaboanalyst.utils.DataUtils;

/**
 *
 * @author jianguox
 */
@RequestScoped
@Named("analBean")
public class AnalysisBean implements Serializable {

    private final SessionBean1 sb = (SessionBean1) DataUtils.findBean("sessionBean1");

    public void performDefaultAnalysis(String pageID) {
        if (!sb.isAnalInit(pageID)) {
            sb.registerPage(pageID);
            String pageURL="";
            switch (pageID) {
                case "PCA":
                    doDefaultPCA();
                    pageURL="/Secure/analysis/PCAView.xhtml";
                    break;
                case "ANOVA":
                    doDefaultANOVA();
                    pageURL="/Secure/analysis/AnovaView.xhtml";
                    break;
                case "Fold change":
                    doDefaultFC();
                    pageURL="/Secure/analysis/FoldChangeView.xhtml";
                    break;
                case "T-test":
                    doDefaultTT();
                    pageURL="/Secure/analysis/TtestView.xhtml";
                    break;
                case "Volcano plot":
                    doDefaultVC();
                    pageURL="/Secure/analysis/VolcanoView.xhtml";                    
                    break;
                case "Correlations":
                    doDefaultCorrelation();
                    pageURL="/Secure/analysis/CorrelationView.xhtml";                    
                    break;
                case "PLSDA":
                    doDefaultPLSDA();
                    pageURL="/Secure/analysis/PLSDAView.xhtml";                    
                    break;
                case "sPLSDA":
                    doDefaultSPLSDA();
                    pageURL="/Secure/analysis/SparsePLSDAView.xhtml";                    
                    break;
                case "OrthoPLSDA":
                    doDefaultOPLSDA();
                    pageURL="/Secure/analysis/OrthoPLSDAView.xhtml";                    
                    break;
                case "SAM":
                    doDefaultSAM();
                    pageURL="/Secure/analysis/SAMView.xhtml";                    
                    break;
                case "EBAM":
                    doDefaultEBAM();
                    pageURL="/Secure/analysis/EBAMView.xhtml";                    
                    break;
                case "Dendrogram":
                    doDefaultDendrogram();
                    pageURL="/Secure/analysis/TreeView.xhtml";                    
                    break;
                case "Heatmap":
                    pageURL="/Secure/analysis/StaticHeatmapView.xhtml";                    
                    doDefaultHmClust();
                    break;
                case "K-means":
                    pageURL="/Secure/analysis/KMView.xhtml";                    
                    doDefaultKmeanClust();
                    break;
                case "SOM":
                    pageURL="/Secure/analysis/SOMView.xhtml";                    
                    doDefaultSOMClust();
                    break;
                case "RandomForest":
                    pageURL="/Secure/analysis/RFView.xhtml";                    
                    doDefaultRF();
                    break;
                case "SVM":
                    pageURL="/Secure/analysis/RSVMView.xhtml";                    
                    doDefaultSVM();
                    break;
            }
            sb.setCurrentNaviUrl(pageURL);
        }
    }

    private void doDefaultANOVA() {
        int res = UniVarTests.performANOVA(sb, "F", 0.05);
        if (res == 0) {
            sb.setAnovaSig(false);
        } else {
            sb.setAnovaSig(true);
        }
        UniVarTests.plotAOV(sb, sb.getCurrentImage("aov"), "png", 72);
    }

    private void doDefaultFC() {
        UniVarTests.initFC(sb, 2, 0, "FALSE");
        UniVarTests.plotFC(sb, sb.getCurrentImage("fc"), "png", 72);
    }

    private void doDefaultTT() {
        //this is perform before intial data analysis
        int res = UniVarTests.performTtests(sb, "F", 0.05, "FALSE", "TRUE", "fdr");//default not paired and equal variance
        if (res == 0) {
            sb.setTtSig(false);
        } else {
            sb.setTtSig(true);
        }
        UniVarTests.plotTT(sb, sb.getCurrentImage("tt"), "png", 72);
    }

    private void doDefaultVC() {
        UniVarTests.performVolcano(sb, "FALSE", 2, 0, "F", 0.1, "TRUE", "raw");
        UniVarTests.plotVolcano(sb, sb.getCurrentImage("volcano"), 1, 0, "png", 72);
    }

    private void doDefaultCorrelation() {
        UniVarTests.plotCorrHeatMap(sb, sb.getCurrentImage("corr"), "png", 72, "col", "pearson", "bwm", "overview", "F", "F", 0.0);
    }

    private void doDefaultPCA() {
        ChemoMetrics.initPCA(sb);
        ChemoMetrics.plotPCAPairSummary(sb, sb.getCurrentImage("pca_pair"), "png", 72, 5);
        ChemoMetrics.plotPCAScree(sb, sb.getCurrentImage("pca_scree"), "png", 72, 5);
        ChemoMetrics.plotPCA2DScore(sb, sb.getCurrentImage("pca_score2d"), "png", 72, 1, 2, 0.95, 0, 0);
        ChemoMetrics.plotPCALoading(sb, sb.getCurrentImage("pca_loading"), "png", 72, 1, 2);  // setLoadingTable(pcImpInx);
        ChemoMetrics.plotPCABiplot(sb, sb.getCurrentImage("pca_biplot"), "png", 72, 1, 2);
        // ChemoMetrics.PlotPCA3DScore(sb, sb.getCurrentImage("pca_score3d"), "png", 72, 1, 2, 3, 40);
        ChemoMetrics.plotPCA3DScore(sb, sb.getCurrentImage("pca_score3d"), "json", 72, 1, 2, 3);
        ChemoMetrics.plotPCA3DLoading(sb, sb.getCurrentImage("pca_loading3d"), "json", 72, 1, 2, 3);
    }

    private void doDefaultPLSDA() {
        if (ChemoMetrics.initPLS(sb)) {
            ChemoMetrics.plotPLSPairSummary(sb, sb.getCurrentImage("pls_pair"), "png", 72, ChemoMetrics.getDefaultPLSPairNumber(sb));
            ChemoMetrics.plotPLS2DScore(sb, sb.getCurrentImage("pls_score2d"), "png", 72, 1, 2, 0.95, 0, 0);
            // ChemoMetrics.PlotPLS3DScore(sb, sb.getCurrentImage("pls_score3d"), "png", 72, 1, 2, 3, 40);
            ChemoMetrics.plotPLS3DScore(sb, sb.getCurrentImage("pls_score3d"), "json", 72, 1, 2, 3);
            ChemoMetrics.plotPLSLoading(sb, sb.getCurrentImage("pls_loading"), "png", 72, 1, 2);
            ChemoMetrics.plotPLS3DLoading(sb, sb.getCurrentImage("pls_loading3d"), "json", 72, 1, 2, 3);
            ChemoMetrics.plotPLSImp(sb, sb.getCurrentImage("pls_imp"), "png", 72, "vip", "Comp. 1", 15, "FALSE");
            
            // Disable the default analysis, which could take very long for large data
            // It should be done by user explicit selection
            
            //String cvMethod = "T";
            //int minSize = RDataUtils.getMinGroupSize(sb.getRConnection());
            //if (minSize < 11) {
            //    cvMethod = "L";
            //}
            //ChemoMetrics.trainPLSClassifier(sb, cvMethod, ChemoMetrics.getDefaultPLSCVNumber(sb), "Q2");
            //ChemoMetrics.plotPLSClassification(sb, sb.getCurrentImage("pls_cv"), "png", 72);

        } else {
            // Seems not working due the pre-render view will cause issue
            // TODO: to redesign to enable the ERROR SHOWING
            DataUtils.updateMsg("Error", RDataUtils.getErrMsg(sb.getRConnection()));
        }
    }

    private void doDefaultSPLSDA() {

        ChemoMetrics.initSPLS(sb, 5, 10, "same", "Mfold", 5, "F");
        ChemoMetrics.plotSPLSPairSummary(sb, sb.getCurrentImage("spls_pair"), "png", 72, ChemoMetrics.getDefaultSPLSPairNumber(sb));
        ChemoMetrics.plotSPLS2DScore(sb, sb.getCurrentImage("spls_score2d"), "png", 72, 1, 2, 0.95, 0, 0);
        ChemoMetrics.plotSPLS3DScore(sb, sb.getCurrentImage("spls_score3d"), "json", 72, 1, 2, 3);
        ChemoMetrics.plotSPLSLoading(sb, sb.getCurrentImage("spls_loading"), "png", 72, 1, "overview");
        //ChemoMetrics.plotSPLSDAClassification(sb, sb.getCurrentImage("spls_cv"), "png", 72);
        ChemoMetrics.plotSPLS3DLoading(sb, sb.getCurrentImage("spls_loading3d"), "json", 72, 1, 2, 3);
    }

    private void doDefaultOPLSDA() {

        ChemoMetrics.initOPLS(sb);
        ChemoMetrics.plotOPLS2DScore(sb, sb.getCurrentImage("opls_score2d"), "png", 72, 1, 2, 0.95, 0, 0);
        ChemoMetrics.plotOplsSplot(sb, sb.getCurrentImage("opls_splot"), "all", "png", 72);
        ChemoMetrics.plotOPLSImp(sb, sb.getCurrentImage("opls_imp"), "png", 72, "vip", "tscore", 15, "FALSE");
        ChemoMetrics.plotOplsMdlView(sb, sb.getCurrentImage("opls_mdl"), "png", 72);
    }

    private void doDefaultSAM() {
        SigVarSelect.initSAM(sb, "d.stat", "FALSE", "TRUE", 0, sb.getCurrentImage("sam_imp"));
        //double delta = SigVarSelect.GetSAMSuggestedDelta(sb);
        //SigVarSelect.PlotSAM_Cmpd(sb, sb.getCurrentImage("sam_imp"), "png", 72);
        SigVarSelect.plotSAM_FDR(sb, sb.getCurrentImage("sam_view"), "png", 72);
    }

    private void doDefaultEBAM() {
        SigVarSelect.initEBAM(sb, "FALSE", "TRUE", "FALSE", -99, 0.9, sb.getCurrentImage("ebam_view"), sb.getCurrentImage("ebam_imp"));
        //SigVarSelect.PlotEBAM_A0(sb, sb.getCurrentImage("ebam_view"), "png", 72);
        //double a0 = SigVarSelect.GetEBAMSuggestedA0(sb);
        //SigVarSelect.InitEBAM_Cmpd(sb, "z.ebam", a0, "FALSE", "TRUE");
        //SigVarSelect.PlotEBAM_Cmpd(sb, sb.getCurrentImage("ebam_imp"), "png", 72, 0.9);
    }

    private void doDefaultDendrogram() {
        Clustering.plotClustTree(sb, sb.getCurrentImage("tree"), "png", 72, "euclidean", "ward.D");
    }

    private void doDefaultHmClust() {
        Clustering.plotHeatMap(sb, sb.getCurrentImage("heatmap"), "png", 72, "norm", "row", "euclidean", "ward.D", "bwm", 8, "overview", "T", "T", "T", "F", "T", "T", "T");
    }

    private void doDefaultKmeanClust() {
        Clustering.plotKmeans(sb, sb.getCurrentImage("km"), "png", 72, 3, "default", "F");
        Clustering.plotKmeansPCA(sb, sb.getCurrentImage("km_pca"), "png", 72, "default", "F");
    }

    private void doDefaultSOMClust() {
        Clustering.plotSOM(sb, sb.getCurrentImage("som"), "png", 72, 1, 3, "linear", "gaussian", "default", "F");
        Clustering.plotSOMPCA(sb, sb.getCurrentImage("som_pca"), "png", 72, "default", "F");
    }

    private void doDefaultRF() {
        Classifying.initRF(sb, 500, 7, 1);
        Classifying.plotRFClassication(sb, sb.getCurrentImage("rf_cls"), "png", 72);
        Classifying.plotRFCmpd(sb, sb.getCurrentImage("rf_imp"), "png", 72);
        Classifying.plotRFOutlier(sb, sb.getCurrentImage("rf_outlier"), "png", 72);
    }

    private void doDefaultSVM() {
        Classifying.initSVMAnal(sb, "10");
        Classifying.plotSVMClassification(sb, sb.getCurrentImage("svm_cls"), "png", 72);
        Classifying.plotSVMSigCmpds(sb, sb.getCurrentImage("svm_imp"), "png", 72);
    }

}
