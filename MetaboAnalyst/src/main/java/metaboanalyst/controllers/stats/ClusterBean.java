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
import metaboanalyst.rwrappers.Clustering;
import metaboanalyst.rwrappers.RDataUtils;
import metaboanalyst.utils.DataUtils;
import org.primefaces.PrimeFaces;

/**
 *
 * @author jianguox
 */
@RequestScoped
@Named("clusterBean")
public class ClusterBean implements Serializable {

    private final SessionBean1 sb = (SessionBean1) DataUtils.findBean("sessionBean1");

    private String clustMethodOpt;
    private String clustDistOpt;
    private String scaleOpt = "row";
    private String hmDistOpt;
    private String hmMethodOpt;
    private String hmColorOpt;
    private boolean noReorg = false;
    private boolean useTopFeature = false;
    private int topThresh = 25;
    private String selectMethodOpt;
    private String noOrgOpt;
    private String kmColPal;
    private String somColPal;
    private boolean kmLabel = false;
    private boolean somLabel = false;
    private String somFacet = "overlay";
    private String kmFacet = "overlay";
    private String dataOpt = "norm";
    private String fontSizeOpt = "8";
    private boolean showLegend = true;
    private boolean showAnnotLegend = true;

    public boolean isShowAnnotLegend() {
        return showAnnotLegend;
    }

    public void setShowAnnotLegend(boolean showAnnotLegend) {
        this.showAnnotLegend = showAnnotLegend;
    }

    public boolean isShowLegend() {
        return showLegend;
    }

    public void setShowLegend(boolean showLegend) {
        this.showLegend = showLegend;
    }

    public String getFontSizeOpt() {
        return fontSizeOpt;
    }

    public void setFontSizeOpt(String fontSizeOpt) {
        this.fontSizeOpt = fontSizeOpt;
    }

    public String getScaleOpt() {
        return scaleOpt;
    }

    public void setScaleOpt(String scaleOpt) {
        this.scaleOpt = scaleOpt;
    }

    public String getDataOpt() {
        return dataOpt;
    }

    public void setDataOpt(String dataOpt) {
        this.dataOpt = dataOpt;
    }

    public String getClustMethodOpt() {
        return clustMethodOpt;
    }

    public void setClustMethodOpt(String clustMethodOpt) {
        this.clustMethodOpt = clustMethodOpt;
    }

    public String getClustDistOpt() {
        return clustDistOpt;
    }

    public void setClustDistOpt(String clustDistOpt) {
        this.clustDistOpt = clustDistOpt;
    }
    
    private boolean includeRowNames = true;

    public boolean isIncludeRowNames() {
        return includeRowNames;
    }

    public void setIncludeRowNames(boolean includeRowNames) {
        this.includeRowNames = includeRowNames;
    }

    private String viewOpt = "overview";

    public String getViewOpt() {
        return viewOpt;
    }

    public void setViewOpt(String viewOpt) {
        this.viewOpt = viewOpt;
    }

    public String treeButton_action() {
        String imgName = sb.getNewImage("tree");
        Clustering.plotClustTree(sb, imgName, "png", 72, clustDistOpt, clustMethodOpt);
        PrimeFaces.current().scrollTo("form1:treePane");
        return null;
    }

    public String getSomFacet() {
        return somFacet;
    }

    public void setSomFacet(String somFacet) {
        this.somFacet = somFacet;
    }

    public String getKmFacet() {
        return kmFacet;
    }

    public void setKmFacet(String kmFacet) {
        this.kmFacet = kmFacet;
    }

    public boolean isKmLabel() {
        return kmLabel;
    }

    public void setKmLabel(boolean kmLabel) {
        this.kmLabel = kmLabel;
    }

    public boolean isSomLabel() {
        return somLabel;
    }

    public void setSomLabel(boolean somLabel) {
        this.somLabel = somLabel;
    }

    public String getKmColPal() {
        return kmColPal;
    }

    public void setKmColPal(String kmColPal) {
        this.kmColPal = kmColPal;
    }

    public String getSomColPal() {
        return somColPal;
    }

    public void setSomColPal(String somColPal) {
        this.somColPal = somColPal;
    }

    public String getSelectMethodOpt() {
        return selectMethodOpt;
    }

    public void setSelectMethodOpt(String selectMethod) {
        this.selectMethodOpt = selectMethod;
    }

    public String getNoOrgOpt() {
        return noOrgOpt;
    }

    public void setNoOrgOpt(String noOrgOpt) {
        this.noOrgOpt = noOrgOpt;
    }

    public int getTopThresh() {
        return topThresh;
    }

    public void setTopThresh(int topThresh) {
        this.topThresh = topThresh;
    }

    public boolean isNoReorg() {
        return noReorg;
    }

    public void setNoReorg(boolean noReorg) {
        this.noReorg = noReorg;
    }

    public boolean isUseTopFeature() {
        return useTopFeature;
    }

    public void setUseTopFeature(boolean useTopFeature) {
        this.useTopFeature = useTopFeature;
    }

    public String getHmDistOpt() {
        return hmDistOpt;
    }

    public void setHmDistOpt(String hmDistOpt) {
        this.hmDistOpt = hmDistOpt;
    }

    public String getHmMethodOpt() {
        return hmMethodOpt;
    }

    public void setHmMethodOpt(String hmMethodOpt) {
        this.hmMethodOpt = hmMethodOpt;
    }

    public String getHmColorOpt() {
        return hmColorOpt;
    }

    public void setHmColorOpt(String hmColorOpt) {
        this.hmColorOpt = hmColorOpt;
    }

    public void hmButton_action() {

        String rowV = "T";
        String colV = "T";

        if (noReorg) {
            if (noOrgOpt.equalsIgnoreCase("row")) {
                rowV = "F";
            } else if (noOrgOpt.equalsIgnoreCase("col")) {
                colV = "F";
            } else {
                rowV = "F";
                colV = "F";
            }
        }

        if (useTopFeature) {
            if (RDataUtils.getNormFeatureNumber(sb.getRConnection()) <= topThresh) {
                DataUtils.updateMsg("Error", "The number of top features cannot be bigger than total feature number!");
                return;
            }

            if (viewOpt.equals("detail") && topThresh > 1000) {
                DataUtils.updateMsg("Warning", "Too many features for detail view (max 1000) - reset to 1000.");
                topThresh = 1000;
            }
        }

        int fontSize = Integer.parseInt(fontSizeOpt);
        if (useTopFeature) {
            Clustering.plotSubHeatMap(sb, sb.getNewImage("heatmap"), "png", 72, dataOpt, scaleOpt, hmDistOpt, hmMethodOpt, hmColorOpt, fontSize, selectMethodOpt, topThresh, viewOpt, rowV, colV, (drawBorders) ? "T" : "F", (grpAves) ? "T" : "F", (showLegend) ? "T" : "F", (showAnnotLegend) ? "T" : "F", includeRowNames ? "T" : "F");
        } else {
            Clustering.plotHeatMap(sb, sb.getNewImage("heatmap"), "png", 72, dataOpt, scaleOpt, hmDistOpt, hmMethodOpt, hmColorOpt, fontSize, viewOpt, rowV, colV, (drawBorders) ? "T" : "F", (grpAves) ? "T" : "F", (showLegend) ? "T" : "F", (showAnnotLegend) ? "T" : "F", includeRowNames ? "T" : "F");
        }
    }

    private int kmClustNm = 3;

    public int getKmClustNm() {
        return kmClustNm;
    }

    public void setKmClustNm(int kmClustNm) {
        this.kmClustNm = kmClustNm;
    }

    public String kmButton_action() {

        if (kmFacet.equalsIgnoreCase("separate")) {
            Clustering.plotKmeans(sb, sb.getNewImage("km"), "png", 72, kmClustNm, kmColPal, "T");
        } else {
            Clustering.plotKmeans(sb, sb.getNewImage("km"), "png", 72, kmClustNm, kmColPal, "F");
        }

        if (kmLabel) {
            Clustering.plotKmeansPCA(sb, sb.getNewImage("km_pca"), "png", 72, kmColPal, "T");
        } else {
            Clustering.plotKmeansPCA(sb, sb.getNewImage("km_pca"), "png", 72, kmColPal, "F");
        }

        PrimeFaces.current().scrollTo("ac:form1:kmPane");
        return null;
    }

    private boolean drawBorders = true;

    public boolean isDrawBorders() {
        return drawBorders;
    }

    public void setDrawBorders(boolean drawBorders) {
        this.drawBorders = drawBorders;
    }

    private boolean grpAves = false;

    public boolean isGrpAves() {
        return grpAves;
    }

    public void setGrpAves(boolean grpAves) {
        this.grpAves = grpAves;
    }

    private int somXdim = 1;
    private int somYdim = 3;
    private String somInitOpt = "linear";
    private String somNbOpt = "gaussian";

    public String getSomInitOpt() {
        return somInitOpt;
    }

    public void setSomInitOpt(String somInitOpt) {
        this.somInitOpt = somInitOpt;
    }

    public String getSomNbOpt() {
        return somNbOpt;
    }

    public void setSomNbOpt(String somNbOpt) {
        this.somNbOpt = somNbOpt;
    }

    public int getSomXdim() {
        return somXdim;
    }

    public void setSomXdim(int somXdim) {
        this.somXdim = somXdim;
    }

    public int getSomYdim() {
        return somYdim;
    }

    public void setSomYdim(int somYdim) {
        this.somYdim = somYdim;
    }

    public String somButton_action() {

        if (somFacet.equalsIgnoreCase("separate")) {
            Clustering.plotSOM(sb, sb.getNewImage("som"), "png", 72, somXdim, somYdim, somInitOpt, somNbOpt, somColPal, "T");
        } else {
            Clustering.plotSOM(sb, sb.getNewImage("som"), "png", 72, somXdim, somYdim, somInitOpt, somNbOpt, somColPal, "F");
        }

        if (somLabel) {
            Clustering.plotSOMPCA(sb, sb.getNewImage("som_pca"), "png", 72, somColPal, "T");
        } else {
            Clustering.plotSOMPCA(sb, sb.getNewImage("som_pca"), "png", 72, somColPal, "F");
        }

        PrimeFaces.current().scrollTo("ac:form1:somPane");
        return null;
    }

    //private String kmTxt = "";
    public String getKmTxt() {
        int clustNum = Clustering.getKMClusterNumber(sb);
        String str = "<h2>K-means clustering details: </h2><br/>";
        str = str + "<table width=\"500\", border=\"1\" cellpadding=\"5\">";
        str = str + "<tr><th width=\"80\">Cluster </th><th> Members </th></tr>";
        for (int i = 0; i < clustNum; i++) {
            String names = Clustering.getKMClusterMembers(sb, i + 1); //java start by 0, while R by 1
            str = str + "<tr><td>" + "Cluster " + (i + 1) + "</td><td>" + names + "</td></tr>";
        }
        str = str + "</table>";
        return str;
    }

    public String getSomTxt() {
        int xord = Clustering.getSOMXdimension(sb);
        int yord = Clustering.getSOMYdimension(sb);
        String str = "<h2>SOM clustering details: </h2><br/>";
        str = str + "<table width=\"500\", border=\"1\" cellpadding=\"5\">";
        str = str + "<tr><th width=\"80\">Cluster </th><th> Members </th></tr>";
        for (int i = 0; i < xord; i++) {
            for (int j = 0; j < yord; j++) {
                String names = Clustering.getSOMClusterMembers(sb, i, j);
                str = str + "<tr><td>" + "Cluster (" + i + ", " + j + ")</td><td>" + names + "</td></tr>";
            }
        }
        str = str + "</table>";
        return str;
    }
}
