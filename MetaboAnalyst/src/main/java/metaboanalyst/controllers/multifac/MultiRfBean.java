/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package metaboanalyst.controllers.multifac;

import java.io.Serializable;
import java.util.ArrayList;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import metaboanalyst.controllers.SessionBean1;
import metaboanalyst.rwrappers.Classifying;
import metaboanalyst.rwrappers.RDataUtils;
import metaboanalyst.utils.DataUtils;
import org.primefaces.PrimeFaces;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author xia
 */
@RequestScoped
@Named("mrfBean")
public class MultiRfBean implements Serializable {

    private final SessionBean1 sb = (SessionBean1) DataUtils.findBean("sessionBean1");
    private static final Logger LOGGER = LogManager.getLogger(MultiRfBean.class);
    private final String pageID = "RandomForest";
    private int treeNum = 500;
    private int tryNum = 7;
    private int rfRandom = 1;

    public int getTreeNum() {
        return treeNum;
    }

    public void setTreeNum(int treeNum) {
        this.treeNum = treeNum;
    }

    public int getTryNum() {
        return tryNum;
    }

    public void setTryNum(int tryNum) {
        this.tryNum = tryNum;
    }

    public int getRfRandom() {
        return rfRandom;
    }

    public void setRfRandom(int rfRandom) {
        this.rfRandom = rfRandom;
    }
    private String rfMeta = null;

    private String[] predictedMeta = null;

    public String getRfMeta() {
        return rfMeta;
    }

    public void setRfMeta(String rfMeta) {
        this.rfMeta = rfMeta;
    }

    public String[] getPredictedMeta() {
        return predictedMeta;
    }

    public void setPredictedMeta(String[] predictedMeta) {
        this.predictedMeta = predictedMeta;
    }

    public void doDefaultRf() {

        if (!sb.isAnalInit(pageID)) {
            sb.registerPage(pageID);
            //do something here
        }
    }

    public String getConfText() {
        String[] rownames = Classifying.getRFConfRowNames(sb);
        if (rownames == null || rownames.length == 0) {
            return ("The analysis has not been performed yet.");
        }
        double[][] confmat = Classifying.getRFConfusionMat(sb);
        String[] colnames = Classifying.getRFConfColNames(sb);
        Double rfOOB = Classifying.getRFOOB(sb);
        ArrayList<Integer> inxVec = new ArrayList();
        inxVec.add(0);
        inxVec.add(1);
        return (setConfTable(" ", confmat, rownames, colnames, inxVec, rfOOB));
    }

    //inx indicate which col of sigmat is int, since default all double
    private static String setConfTable(String lbl, double[][] sigmat, String[] rownames, String[] colnames, ArrayList<Integer> inxs, double err) {
        String str = "<b>The OOB error is " + err + "</b></br>";
        str = str + "<table border=\"1\" cellpadding=\"5\">";
        str = str + "<tr><th>" + lbl + "</th>";
        for (String colname : colnames) {
            str = str + "<th>" + colname + "</th>";
        }
        str = str + "</tr>";
        for (int i = 0; i < rownames.length; i++) {
            str = str + "<tr><td>" + rownames[i] + "</td>";
            for (int j = 0; j < colnames.length; j++) {
                if (inxs.contains(j)) {
                    str = str + "<td>" + Math.round(sigmat[i][j]) + "</td>";
                } else {
                    str = str + "<td>" + sigmat[i][j] + "</td>";
                }
            }
            str = str + "</tr>";
        }
        str = str + "</table>";
        return str;
    }

    public String rfBn_action_time() {
        try {

            // index cannot be more than total number of cluster
            if (treeNum < 1 || tryNum > RDataUtils.getNormFeatureNames(sb.getRConnection()).length) {

                DataUtils.updateMsg("Error", "Try number is not correct!");
                return null;
            }

            for (String predictedMeta1 : predictedMeta) {
                if (rfMeta.equals(predictedMeta1)) {
                    DataUtils.updateMsg("Error", "Please do not include primary meta-data in predictor meta-data!");
                    return null;
                }
            }

            int res = Classifying.initRFMeta(sb, treeNum, tryNum, rfRandom, rfMeta, predictedMeta);

            switch (res) {
                case 1:
                    Classifying.plotRFClassicationMeta(sb, sb.getNewImage("rf_cls"), "png", 72);
                    Classifying.plotRFCmpdMeta(sb, sb.getNewImage("rf_imp"), "png", 72);
                    Classifying.plotRFOutlier(sb, sb.getNewImage("rf_outlier"), "png", 72);
                    break;
                case 2:
                    DataUtils.updateMsg("Error", "The Random Forest module is only set up for classification. Please choose a "
                            + "categorical primary meta-data!");
                    break;
                default:
                    DataUtils.updateMsg("Error", "Something wrong occured. " + RDataUtils.getErrMsg(sb.getRConnection()));
                    return null;
            }

        } catch (Exception e) {
            //e.printStackTrace();
            LOGGER.error("rfBn_action_time", e);
        }
        PrimeFaces.current().scrollTo("ac:form1:sumPane");
        return null;
    }
}
