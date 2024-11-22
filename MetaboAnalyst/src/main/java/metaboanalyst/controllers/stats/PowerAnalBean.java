/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package metaboanalyst.controllers.stats;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.faces.model.SelectItem;
import metaboanalyst.controllers.SessionBean1;
import metaboanalyst.rwrappers.PowerUtils;
import metaboanalyst.rwrappers.RDataUtils;
import metaboanalyst.utils.DataUtils;
import org.primefaces.model.charts.ChartData;
import org.primefaces.model.charts.data.NumericPoint;
import org.primefaces.model.charts.line.LineChartDataSet;
import org.primefaces.model.charts.scatter.ScatterChartModel;

/**
 *
 * @author jianguox
 */
@SessionScoped
@Named("powerAnalBean")
@JsonIgnoreProperties(ignoreUnknown = true)
public class PowerAnalBean implements Serializable {
    @JsonIgnore
    private final SessionBean1 sb = (SessionBean1) DataUtils.findBean("sessionBean1");
    private ScatterChartModel lineModel;
    private double fdr = 0.1;
    private int smplSize = 200;
    private String selectedContrasts = "NA";
    private SelectItem[] grpContrasts;

    public ScatterChartModel getLineModel() {
        return lineModel;
    }

    public void setLineModel(ScatterChartModel lineModel) {
        this.lineModel = lineModel;
    }

    public double getFdr() {
        return fdr;
    }

    public void setFdr(double fdr) {
        this.fdr = fdr;
    }

    public int getSmplSize() {
        return smplSize;
    }

    public void setSmplSize(int smplSize) {
        if (smplSize < 60) {
            smplSize = 60;
        } else if (smplSize > 1000) {
            smplSize = 1000;
        }

        this.smplSize = smplSize;
    }

    public String getSelectedContrasts() {
        return selectedContrasts;
    }

    public void setSelectedContrasts(String selectedContrasts) {
        this.selectedContrasts = selectedContrasts;
    }

    public SelectItem[] getGroupContrastOpts() {
        if (grpContrasts == null) {
            String[] nms = RDataUtils.getGroupNames(sb.getRConnection(), "");
            int totNum = nms.length * (nms.length - 1) / 2;
            grpContrasts = new SelectItem[totNum];
            int pos = 0;
            for (int m = 0; m < nms.length - 1; m++) {
                String grpA = nms[m];
                for (int n = m + 1; n < nms.length; n++) {
                    String grpB = nms[n];
                    String target = grpA + " vs. " + grpB;
                    grpContrasts[pos] = new SelectItem(target, target);
                    pos++;
                }
            }
        }
        return grpContrasts;
    }

    public void doDefaultPowerAnal() {

        paramBtn_action();
        sb.registerPage("Set parameter");
    }

    public void paramBtn_action() {
        PowerUtils.initPowerAnal(sb.getRConnection(), selectedContrasts);
        PowerUtils.plotPowerStatDiagnostics(sb, sb.getNewImage("power_stat"), "png", 72);
    }

    public String profileBtn_action() {
        //PowerUtils.plotPowerProfile(sb, fdr, sb.getNewImage("power_profile"), "png", 72);
        fdr = 0.1;
        updateModel();
        return "powerview";
    }

    public void updateModel() {
        double fdrOld = fdr;
        fdr = PowerUtils.performPowerProfile(sb.getRConnection(), fdr, smplSize);
        if (fdrOld > fdr) {
            DataUtils.updateMsg("Warning", "FDR level has been re-adjusted in order to get meaningful result.");
        }

        double[] pwrs = PowerUtils.plotPowerProfile(sb, fdr, smplSize, sb.getNewImage("power_profile"), "png", 72);
        int[] smplNum = PowerUtils.getPowerValueX(sb.getRConnection()); //must be called after plotPowerProile
        lineModel = new ScatterChartModel();
        ChartData data = new ChartData();
        LineChartDataSet myDataSet = new LineChartDataSet();
        List<Object> myValues = new ArrayList<>();

        for (int i = 0; i < pwrs.length; i++) {
            myValues.add(new NumericPoint(smplNum[i], pwrs[i]));
        }
        myDataSet.setData(myValues);
        myDataSet.setBackgroundColor("rgba(153, 102, 255, 0.2)");
        myDataSet.setBorderColor("rgb(153, 102, 255)");
        myDataSet.setShowLine(true);
        data.addChartDataSet(myDataSet);
        lineModel.setData(data);
        lineModel.setExtender("extender");
    }
}
