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
import metaboanalyst.rwrappers.SigVarSelect;
import metaboanalyst.utils.DataUtils;

/**
 *
 * @author jianguox
 */
@RequestScoped
@Named("sigBean")
public class SigVarBean implements Serializable {

    private final SessionBean1 sb = (SessionBean1) DataUtils.findBean("sessionBean1");

    private boolean nonParSAM = false;

    public boolean isNonParSAM() {
        return nonParSAM;
    }

    public void setNonParSAM(boolean nonParSAM) {
        this.nonParSAM = nonParSAM;
    }

    private String pairedAnal = "FALSE";

    public String getPairedAnal() {
        return pairedAnal;
    }

    public void setPairedAnal(String pairedAnal) {
        this.pairedAnal = pairedAnal;
    }

    private String equalVar = "TRUE";

    public String getEqualVar() {
        return equalVar;
    }

    public void setEqualVar(String equalVar) {
        this.equalVar = equalVar;
    }

    private double delta = 0;
    private double deltaMin = 0;
    private double deltaMax = 0;
    private double step = 0;

    public double getDeltaMin() {
        return deltaMin;
    }

    public double getDeltaMax() {
        return deltaMax;
    }

    public double getStep() {
        return step;
    }


    public double getDelta() {
        return SigVarSelect.getSAMSuggestedDelta(sb);
    }

    public void setDelta(double delta) {
        this.delta = delta;
    }

    public void samBtn1_action() {
        String stat = "d.stat";
        if (!sb.isMultiGroup() && nonParSAM) {
            stat = "wilc.stat";
        }
        SigVarSelect.initSAM(sb, stat, pairedAnal, equalVar, delta, sb.getNewImage("sam_imp"));
        double myDelta = SigVarSelect.getSAMSuggestedDelta(sb);
        if(myDelta!= delta){
            DataUtils.updateMsg("Info", "The delta value has been updated.");
            delta = myDelta;
        }
        double[] deltas = SigVarSelect.getSAMDeltaRange(sb);
        deltaMin = deltas[0];
        deltaMax = deltas[1];
        step = deltas[2];

        SigVarSelect.plotSAM_FDR(sb, sb.getNewImage("sam_view"), "png", 72);
        //SigVarSelect.PlotSAM_Cmpd(sb, sb.getNewImage("sam_imp"), "png", 72);
        DataUtils.updateMsg("OK", "The result is updated!");
    }

    private double alpha = -99;

    public double getAlpha() {
        return SigVarSelect.getEBAMSuggestedA0(sb);
    }

    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    private boolean nonParEBAM = false;

    public boolean isNonParEBAM() {
        return nonParEBAM;
    }

    public void setNonParEBAM(boolean nonParEBAM) {
        this.nonParEBAM = nonParEBAM;
    }

    private double ebamDelta = 0.9;

    public double getEbamDelta() {
        return ebamDelta;
    }

    public void setEbamDelta(double ebamDelta) {
        this.ebamDelta = ebamDelta;
    }

    public void ebamBtn_action() {
        String nonPar = (nonParEBAM) ? "TRUE" : "FALSE";
        SigVarSelect.initEBAM(sb, pairedAnal, equalVar, nonPar, alpha, ebamDelta, sb.getNewImage("ebam_view"), sb.getNewImage("ebam_imp"));
        DataUtils.updateMsg("OK", "The <b>Result view</b> is updated!");
    }
}
