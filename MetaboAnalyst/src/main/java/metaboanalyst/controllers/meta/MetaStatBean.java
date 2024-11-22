/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package metaboanalyst.controllers.meta;

import java.io.Serializable;
import metaboanalyst.rwrappers.RMetaUtils;
import javax.faces.application.FacesMessage;
import javax.inject.Named;
import javax.faces.view.ViewScoped;
import javax.faces.context.FacesContext;
import metaboanalyst.controllers.SessionBean1;
import metaboanalyst.utils.DataUtils;
import org.rosuda.REngine.Rserve.RConnection;

/**
 *
 * @author jianguox
 */
@ViewScoped
@Named("metaStatBean")
public class MetaStatBean implements Serializable {

    private final SessionBean1 sb = (SessionBean1) DataUtils.findBean("sessionBean1");
    private final MetaLoadBean mb = (MetaLoadBean) DataUtils.findBean("loadBean");

    RConnection RC = sb.getRConnection();
    private boolean resOK = false;

    public boolean isResOK() {
        return resOK;
    }
    /**
     * p value combination
     *
     * @return
     */

    private double metpSigLvl = 0.05;

    public double getMetpSigLvl() {
        return metpSigLvl;
    }

    public void setMetpSigLvl(double metpSigLvl) {
        this.metpSigLvl = metpSigLvl;
    }
    private String metapMethod = "fisher";

    public String getMetapMethod() {
        return metapMethod;
    }

    public void setMetapMethod(String metapMethod) {
        this.metapMethod = metapMethod;
    }

    public String performPvalCombination() {
        mb.setAnalMethod("metap");
        int res = RMetaUtils.performPvalCombination(RC, metapMethod, metpSigLvl);
        if (res > 0) {
            resOK = true;
            mb.setCurrentDeNum(res);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "OK", "A total of " + res + " significant features found. Click <b>Proceed</b> button to view the result. "));
        } else {
            resOK = false;
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning", "No significant features found. You may want to change the significance level threshold. "));

        }
        return null;
    }

    /**
     * Vote counts
     *
     * @return
     */
    private double vcSigLvl = 0.05;

    public double getVcSigLvl() {
        return vcSigLvl;
    }

    public void setVcSigLvl(double vcSigLvl) {
        this.vcSigLvl = vcSigLvl;
    }
    private int minVote;

    public int getMinVote() {
        //default half (+1) to ensure round to higher
        if (minVote == 0) {
            minVote = (mb.getDataSets().size() + 1) / 2;
        }
        return minVote;
    }

    public void setMinVote(int minVote) {
        this.minVote = minVote;
    }

    public String performVoteCounting() {
        mb.setAnalMethod("votecount");
        int res = RMetaUtils.performVoteCounting(RC, vcSigLvl, minVote);
        if (res > 0) {
            resOK = true;
            mb.setCurrentDeNum(res);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "OK", "A total of " + res + " significant features found. Click <b>Proceed</b> button to view the result. "));
        } else {
            resOK = false;
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning", "No significant features found. You may want to change the significance level threshold. "));

        }
        return null;
    }

    /**
     * Direct merging
     */
    private double dmSigLvl = 0.05;

    public double getDmSigLvl() {
        return dmSigLvl;
    }

    public void setDmSigLvl(double dmSigLvl) {
        this.dmSigLvl = dmSigLvl;
    }

    public String performDirectMerging() {
        mb.setAnalMethod("merge");
        int res = RMetaUtils.performMetaMerge(RC, dmSigLvl);
        if (res > 0) {
            resOK = true;
            mb.setCurrentDeNum(res);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "OK", "A total of " + res + " significant features found. Click <b>Proceed</b> button to view the result. ")); 
            return ("Result table");
        } else {
            resOK = false;
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning", "No significant features found. You may want to change the significance level threshold. "));

        }
        return null;
    }
}
