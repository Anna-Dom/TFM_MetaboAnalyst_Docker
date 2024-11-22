/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package metaboanalyst.controllers.multifac;

import java.io.File;
import java.io.Serializable;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import metaboanalyst.controllers.SessionBean1;
import metaboanalyst.models.FeatureBean;
import metaboanalyst.rwrappers.TimeSeries;
import metaboanalyst.utils.DataUtils;
import org.rosuda.REngine.Rserve.RConnection;

/**
 *
 * @author xia
 */
@ViewScoped
@Named("mebaBean")
public class MebaBean implements Serializable{

    private final SessionBean1 sb = (SessionBean1) DataUtils.findBean("sessionBean1");
    private final MultifacBean tb = (MultifacBean) DataUtils.findBean("multifacBean");
    private final String pageID = "MEBA";

    private String[] mebaMetas = null;

    public String[] getMebaMetas() {
        return mebaMetas;
    }

    public void setMebaMetas(String[] mebaMetas) {
        this.mebaMetas = mebaMetas;
    }

    private FeatureBean[] featureBeans = null;
    private String downloadTxt;

    public FeatureBean[] getFeatureBeans() {
        return featureBeans;
    }

    public void setFeatureBeans(FeatureBean[] featureBeans) {
        this.featureBeans = featureBeans;
    }

    public String getDownloadTxt() {
        return downloadTxt;
    }
    
    private String mebaName = "Hotelling-T2";

    public String getMEBAStatName() {
        return mebaName;
    }

    public void doDefaultMEBA() {
        if (!sb.isAnalInit(pageID)) {
            sb.registerPage(pageID);
            if (mebaMetas == null) {
                mebaMetas = tb.getDiscMetaOpts();
                //System.out.println(mebaMetas.length);
            }

            RConnection RC = sb.getRConnection();
            int res = TimeSeries.performMB(RC, 10, mebaMetas);
            if (res == 0) {
                String msg = "Please make sure data are balanced for time-series analysis. In particular, "
                        + "for each time point, all experiments must exist and cannot be missing!";
                DataUtils.updateMsg("Error", msg);
                featureBeans = null;
                downloadTxt = "No results";
                return;
            }
            String[] rownames = TimeSeries.getMBSigRowNames(RC);
            String[] colnames = TimeSeries.getMBSigColNames(RC);
            mebaName = colnames[0];
            double[][] sigmat = TimeSeries.getMBSigMat(RC);

            //set up content
            if (rownames == null || rownames.length == 0) {
                featureBeans = null;
                downloadTxt = "No results";
                DataUtils.updateMsg("Error", "No result data was found!");
            } else {
                //now set up the feature beans
                featureBeans = new FeatureBean[rownames.length];
                FeatureBean fb;

                for (int i = 0; i < rownames.length; i++) {
                    fb = new FeatureBean();
                    fb.addName(rownames[i]);

                    for (int m = 0; m < colnames.length; m++) {
                        fb.addValue(sigmat[i][m]);
                    }
                    featureBeans[i] = fb;
                }

                downloadTxt = "<b>You can download the table:</b> <a href = \"/MetaboAnalyst/resources/users/"
                        + sb.getCurrentUser().getName()
                        + File.separator + "meba_sig_features.csv\" target=\"_blank\"><b>" + "here" + "</b></a>";
            }
        }
    }

    public void mbButton_action() {
        if (mebaMetas.length != 2) {
            DataUtils.updateMsg("Error", "Please select an experimental factor along with time-series meta-data.");
            return;
        }
        doDefaultMEBA();
    }
}
