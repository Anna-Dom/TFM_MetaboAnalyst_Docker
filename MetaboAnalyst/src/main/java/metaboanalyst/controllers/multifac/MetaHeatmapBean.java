/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package metaboanalyst.controllers.multifac;

import java.io.Serializable;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import metaboanalyst.controllers.SessionBean1;
import metaboanalyst.rwrappers.RDataUtils;
import metaboanalyst.utils.DataUtils;

/**
 *
 * @author xia
 */
@RequestScoped
@Named("mhmBean")
public class MetaHeatmapBean implements Serializable{

    private final SessionBean1 sb = (SessionBean1) DataUtils.findBean("sessionBean1");

    private final String pageID = "Metadata";
    
    private String metaDistOpt = "euclidean";
    private String metaViewOpt = "overview";
    private String metaClusterOpt = "ward.D";
    private String metaClusterSelOpt = "both";
    private String metaColorOpt = "bwm";

    public String getMetaViewOpt() {
        return metaViewOpt;
    }

    public void setMetaViewOpt(String metaViewOpt) {
        this.metaViewOpt = metaViewOpt;
    }

    public String getMetaClusterSelOpt() {
        return metaClusterSelOpt;
    }

    public void setMetaClusterSelOpt(String metaClusterSelOpt) {
        this.metaClusterSelOpt = metaClusterSelOpt;
    }

    public String getMetaClusterOpt() {
        return metaClusterOpt;
    }

    public void setMetaClusterOpt(String metaClusterOpt) {
        this.metaClusterOpt = metaClusterOpt;
    }

    public String getMetaColorOpt() {
        return metaColorOpt;
    }

    public void setMetaColorOpt(String metaColorOpt) {
        this.metaColorOpt = metaColorOpt;
    }

    public String getMetaDistOpt() {
        return metaDistOpt;
    }

    public void setMetaDistOpt(String metaDistOpt) {
        this.metaDistOpt = metaDistOpt;
    }

    private boolean includeRowNamesMeta = true;
    private boolean drawBordersMeta = false;

    public boolean isIncludeRowNamesMeta() {
        return includeRowNamesMeta;
    }

    public void setIncludeRowNamesMeta(boolean includeRowNamesMeta) {
        this.includeRowNamesMeta = includeRowNamesMeta;
    }

    public boolean isDrawBordersMeta() {
        return drawBordersMeta;
    }

    public void setDrawBordersMeta(boolean drawBordersMeta) {
        this.drawBordersMeta = drawBordersMeta;
    }

    private String corOpt = "pearson";

    public String getCorOpt() {
        return corOpt;
    }

    public void setCorOpt(String corOpt) {
        this.corOpt = corOpt;
    }

    public void doDefaultMetaHeatmap() {
        if (!sb.isAnalInit(pageID)) {
            sb.registerPage(pageID);
            RDataUtils.plotMetaCorrHeatmap(sb, corOpt, sb.getNewImage("metaCorrHeatmap"), "png", 72);
            RDataUtils.plotMetaHeatmap(sb, "overview", "both", "euclidean", "ward.D", "bwm", drawBordersMeta ? "T" : "F",
                    includeRowNamesMeta ? "T" : "F", sb.getNewImage("metaHeatmap"), "png", 72);
        }
    }

    public void metaHmBn_action() {
        int res = RDataUtils.plotMetaHeatmap(sb, metaViewOpt, metaClusterSelOpt, metaDistOpt, metaClusterOpt, metaColorOpt,
                drawBordersMeta ? "T" : "F", includeRowNamesMeta ? "T" : "F", sb.getNewImage("metaHeatmap"), "png", 72);
        if (res == 0) {
            DataUtils.updateMsg("Error", "Please change standardization parameters and try again!");
        }

    }

    public void metaCorrHmBn_action() {

        int res = RDataUtils.plotMetaCorrHeatmap(sb, corOpt, sb.getNewImage("metaCorrHeatmap"), "png", 72);
        if (res == 0) {
            DataUtils.updateMsg("Error", "Unknown error occured in the image generation!");
        }

    }
}
