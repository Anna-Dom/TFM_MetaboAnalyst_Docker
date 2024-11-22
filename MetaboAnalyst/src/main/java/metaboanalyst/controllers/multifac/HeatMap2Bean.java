/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package metaboanalyst.controllers.multifac;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import javax.faces.event.PhaseId;
import javax.faces.event.ValueChangeEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import metaboanalyst.controllers.SessionBean1;
import metaboanalyst.rwrappers.RDataUtils;
import metaboanalyst.rwrappers.TimeSeries;
import metaboanalyst.utils.DataUtils;

/**
 *
 * @author xia
 */
@ViewScoped
@Named("hm2Bean")
public class HeatMap2Bean implements Serializable {

    private final SessionBean1 sb = (SessionBean1) DataUtils.findBean("sessionBean1");
    private MultifacBean tb = (MultifacBean) DataUtils.findBean("multifacBean");
    private final String pageID = "Heatmap2";
    private boolean includeRowNames = false;
    private boolean showLegend = true;
    private boolean showAnnotLegend = true;
    private String scaleOpt = "row";
    private String dataOpt;
    private String fontSizeOpt = "8";

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

    private String distOpt;

    public String getDistOpt() {
        return distOpt;
    }

    public void setDistOpt(String distOpt) {
        this.distOpt = distOpt;
    }

    private String clusterOpt;

    public String getClusterOpt() {
        return clusterOpt;
    }

    public void setClusterOpt(String clusterOpt) {
        this.clusterOpt = clusterOpt;
    }

    private String colorOpt;

    public String getColorOpt() {
        return colorOpt;
    }

    public void setColorOpt(String colorOpt) {
        this.colorOpt = colorOpt;
    }

    private String[] selectedMetas;

    public String[] getSelectedMetas() {
        return selectedMetas;
    }

    public void setSelectedMetas(String[] selectedMetas) {
        this.selectedMetas = selectedMetas;
    }

    private String viewOpt = "overview";

    public String getViewOpt() {
        return viewOpt;
    }

    public void setViewOpt(String viewOpt) {
        this.viewOpt = viewOpt;
    }
    private int topThresh = 25;
    private String selectMethodOpt;
    private boolean useTopFeature = false;

    public boolean isUseTopFeature() {
        return useTopFeature;
    }

    public void setUseTopFeature(boolean useTopFeature) {
        this.useTopFeature = useTopFeature;
    }

    public int getTopThresh() {
        return topThresh;
    }

    public void setTopThresh(int topThresh) {
        this.topThresh = topThresh;
    }

    public String getSelectMethodOpt() {
        return selectMethodOpt;
    }

    public void setSelectMethodOpt(String selectMethodOpt) {
        this.selectMethodOpt = selectMethodOpt;
    }

    private String[] smplSortOpt;

    public String[] getSmplSortOpt() {
        return smplSortOpt;
    }

    public void setSmplSortOpt(String[] smplSortOpt) {
        this.smplSortOpt = smplSortOpt;
    }

    public boolean isIncludeRowNames() {
        return includeRowNames;
    }

    public void setIncludeRowNames(boolean includeRowNames) {
        this.includeRowNames = includeRowNames;
    }

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

    private boolean drawBorders = false;

    public boolean isDrawBorders() {
        return drawBorders;
    }

    public void setDrawBorders(boolean drawBorders) {
        this.drawBorders = drawBorders;
    }

    public void doDefaultHeatmap2() {

        if (selectedMetas == null) {
            int metaNum = tb.getMetaDataBeans().size();
            if (tb.getMetaDataBeans().size() > 4) {
                metaNum = 4;
            }
            String[] meta = new String[metaNum];

            for (int i = 0; i < metaNum; i++) {
                meta[i] = tb.getMetaDataBeans().get(i).getName();
            }
            selectedMetas = meta;
        }

        if (smplSortOpt == null) {
            String[] meta = new String[1];
            meta[0] = tb.getMetaDataBeans().get(0).getName();
            smplSortOpt = meta;
            if (!smplSortOptList.contains(meta[0])) {
                smplSortOptList.add(meta[0]);
            }
        }
        int fontSize = Integer.parseInt(fontSizeOpt);
        if (!sb.isAnalInit(pageID)) {
            sb.registerPage(pageID);
            TimeSeries.plotHeatMap2(sb, sb.getCurrentImage("heatmap2"), "norm", "row", "png", 72, "euclidean", "ward.D", "bwm", fontSize, "overview", "mean", 2000, smplSortOpt, "F", "F", "T", "T", "F", selectedMetas);
        }
    }

    ArrayList<String> smplSortOptList = new ArrayList();

    public ArrayList<String> getSmplSortOptList() {
        return smplSortOptList;
    }

    public void setSmplSortOptList(ArrayList<String> smplSortOptList) {
        this.smplSortOptList = smplSortOptList;
    }

    public void hm2Bn_action() {

        for (String smplSortOpt1 : smplSortOpt) {
            if (!Arrays.asList(selectedMetas).contains(smplSortOpt1)) {
                DataUtils.updateMsg("Error", "Please make sure that the \"Sample arrangement\" metadata are included in \"Metadata in annotation\"!");
                return;
            }
        }

        if (selectedMetas.length < 1) {
            DataUtils.updateMsg("Error", "Please select at least one meta-data for annotation!");
            return;
        }

        for (String metaNm : smplSortOpt) {
            if (!Arrays.asList(selectedMetas).contains(metaNm)) { // if sample sort
                DataUtils.updateMsg("Error", "Please make sure to include the \"Sample arrangement\" metadata in \"Metadata in annotation\"!");
                return;
            }
        }

        if (smplSortOpt.length > 4) { // if sample sort
            DataUtils.updateMsg("Error", "Only up to four metadata can be selected for \"Sample arrangement\".");
            return;
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
        String[] smplSortArray = smplSortOptList.toArray(String[]::new);
        int res = TimeSeries.plotHeatMap2(sb, sb.getNewImage("heatmap2"), dataOpt, scaleOpt, "png", 72, distOpt, clusterOpt, colorOpt, fontSize, viewOpt, selectMethodOpt, topThresh, smplSortArray, 
                useTopFeature ? "T" : "F", drawBorders ? "T" : "F", showLegend ? "T" : "F", showAnnotLegend ? "T" : "F", includeRowNames ? "T" : "F", selectedMetas);
        if (res == 0) {
            String err = RDataUtils.getErrMsg(sb.getRConnection());
            DataUtils.updateMsg("Error", "Failed to plot heatmap." + err);
        }
    }

    public void changeListener(ValueChangeEvent event) {
        if (event.getPhaseId() != PhaseId.INVOKE_APPLICATION) {
            event.setPhaseId(PhaseId.INVOKE_APPLICATION);
            event.queue();
            return;
        }

        Object oldValue = event.getOldValue();
        Object newValue = event.getNewValue();
        String[] oldArray = (String[]) oldValue;
        String[] newArray = (String[]) newValue;

        if (oldArray.length > newArray.length) { // remove element
            for (String metaNm : oldArray) {
                if (!Arrays.asList(newArray).contains(metaNm)) {
                    smplSortOptList.remove(metaNm);
                }
            }
        } else {
            for (String metaNm : newArray) { // add element
                if (!Arrays.asList(oldArray).contains(metaNm)) {
                    smplSortOptList.add(metaNm);
                }
            }
        }
    }
}
