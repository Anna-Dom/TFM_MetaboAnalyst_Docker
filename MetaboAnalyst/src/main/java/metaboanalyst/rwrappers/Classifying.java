/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package metaboanalyst.rwrappers;

import java.util.Arrays;
import metaboanalyst.controllers.SessionBean1;
import org.rosuda.REngine.Rserve.RConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Jeff
 */
public class Classifying {

    private static final Logger LOGGER = LogManager.getLogger(Classifying.class);

    public static void initRF(SessionBean1 sb, int treeNum, int tryNum, int randomOn) {
        try {
            RConnection RC = sb.getRConnection();
            String rCommand = "RF.Anal(NA" + ", " + treeNum + "," + tryNum + "," + randomOn + ")";
            RCenter.recordRCommand(RC, rCommand);
            RC.voidEval(rCommand);
        } catch (Exception rse) {
            LOGGER.error("InitRF", rse);
        }
    }

    public static int initRFMeta(SessionBean1 sb, int treeNum, int tryNum, int randomOn, String primary, String[] predictedMeta) {
        try {
            RConnection RC = sb.getRConnection();
            RC.assign("meta.vec.rf", predictedMeta);
            RCenter.recordRCommand(RC, "meta.vec.rf <- " + Arrays.toString(predictedMeta));
            String rCommand = "RF.AnalMeta(NA, " + treeNum + "," + tryNum + "," + randomOn + ", \"" + primary + "\")";
            RCenter.recordRCommand(RC, rCommand);
            return RC.eval(rCommand).asInteger();
        } catch (Exception e) {
            // e.printStackTrace();
            LOGGER.error("InitRFMeta", e);
        }
        return 0;
    }

    public static void plotRFClassication(SessionBean1 sb, String imgName, String format, int dpi) {
        try {
            RConnection RC = sb.getRConnection();
            String rCommand = "PlotRF.Classify(NA" + ", \"" + imgName + "\", \"" + format + "\", " + dpi + ", width=NA)";
            RCenter.recordRCommand(RC, rCommand);
            sb.addGraphicsCMD("rf_cls", rCommand);
            RC.voidEval(rCommand);
        } catch (Exception rse) {
            LOGGER.error("PlotRFClassication", rse);
        }
    }

    public static void plotRFCmpd(SessionBean1 sb, String imgName, String format, int dpi) {
        try {
            RConnection RC = sb.getRConnection();
            String rCommand = "PlotRF.VIP(NA" + ", \"" + imgName + "\", \"" + format + "\", " + dpi + ", width=NA)";
            RCenter.recordRCommand(RC, rCommand);
            sb.addGraphicsCMD("rf_imp", rCommand);
            RC.voidEval(rCommand);
        } catch (Exception rse) {
            LOGGER.error("PlotRFCmpd", rse);
        }
    }

    public static void plotRFClassicationMeta(SessionBean1 sb, String imgName, String format, int dpi) {
        try {
            RConnection RC = sb.getRConnection();
            String rCommand = "PlotRF.ClassifyMeta(NA" + ", \"" + imgName + "\", \"" + format + "\", " + dpi + ", width=NA)";
            RCenter.recordRCommand(RC, rCommand);
            sb.addGraphicsCMD("rf_cls", rCommand);
            RC.voidEval(rCommand);
        } catch (Exception rse) {
            LOGGER.error("PlotRFClassicationMeta", rse);
        }
    }

    public static void plotRFCmpdMeta(SessionBean1 sb, String imgName, String format, int dpi) {
        try {
            RConnection RC = sb.getRConnection();
            String rCommand = "PlotRF.VIPMeta(NA" + ", \"" + imgName + "\", \"" + format + "\", " + dpi + ", width=NA)";
            RCenter.recordRCommand(RC, rCommand);
            sb.addGraphicsCMD("rf_imp", rCommand);
            RC.voidEval(rCommand);
        } catch (Exception rse) {
            LOGGER.error("PlotRFCmpdMeta", rse);
        }
    }

    public static void plotRFOutlier(SessionBean1 sb, String imgName, String format, int dpi) {
        try {
            RConnection RC = sb.getRConnection();
            String rCommand = "PlotRF.Outlier(NA" + ", \"" + imgName + "\", \"" + format + "\", " + dpi + ", width=NA)";
            RCenter.recordRCommand(RC, rCommand);
            sb.addGraphicsCMD("rf_outlier", rCommand);
            RC.voidEval(rCommand);
        } catch (Exception rse) {
            LOGGER.error("PlotRFOutlier", rse);
        }
    }

    public static Double getRFOOB(SessionBean1 sb) {
        try {
            String rCommand = "GetRFOOB(NA)";
            return sb.getRConnection().eval(rCommand).asDouble();
        } catch (Exception rse) {
            LOGGER.error("GetRFOOB", rse);
        }
        return null;
    }

    public static String[] getRFConfRowNames(SessionBean1 sb) {
        try {
            String rCommand = "GetRFConfRowNames(NA)";
            return sb.getRConnection().eval(rCommand).asStrings();
        } catch (Exception rse) {
            LOGGER.error("GetRFConfRowNames", rse);
        }
        return null;
    }

    public static String[] getRFConfColNames(SessionBean1 sb) {
        try {
            String rCommand = "GetRFConfColNames(NA)";
            return sb.getRConnection().eval(rCommand).asStrings();
        } catch (Exception rse) {
            LOGGER.error("GetRFConfColNames", rse);
        }
        return null;
    }

    public static double[][] getRFConfusionMat(SessionBean1 sb) {
        try {
            String rCommand = "GetRFConfMat(NA)";
            return sb.getRConnection().eval(rCommand).asDoubleMatrix();
        } catch (Exception rse) {
            LOGGER.error("GetRFConfusionMat", rse);
        }
        return null;
    }

    public static double[][] getRFSigMat(SessionBean1 sb) {
        try {
            String rCommand = "GetRFSigMat(NA)";
            return sb.getRConnection().eval(rCommand).asDoubleMatrix();
        } catch (Exception rse) {
            LOGGER.error("GetRFSigMat", rse);
        }
        return null;
    }

    public static String[] getRFSigRowNames(SessionBean1 sb) {
        try {
            String rCommand = "GetRFSigRowNames(NA)";
            return sb.getRConnection().eval(rCommand).asStrings();
        } catch (Exception rse) {
            LOGGER.error("GetRFSigRowNames", rse);
        }
        return null;
    }

    public static String[] getRFSigColNames(SessionBean1 sb) {
        try {
            String rCommand = "GetRFSigColNames(NA)";
            return sb.getRConnection().eval(rCommand).asStrings();
        } catch (Exception rse) {
            LOGGER.error("GetRFSigColNames", rse);
        }
        return null;
    }

    //-------------R-SVM-----------methods
    public static void initSVMAnal(SessionBean1 sb, String cvType, Double trainingSize, Integer numFolds) {
        try {
            RConnection RC = sb.getRConnection();
            String rCommand;
            if (cvType.equals("LOO") || cvType.equals("bootstrape")) { //10 fold validation
                rCommand = "RSVM.Anal(NA" + ", \"" + cvType + "\", CVnum=" + numFolds + ")";
            } else if (cvType.equals("carlo")) {
                rCommand = "RSVM.Anal(NA" + ", \"" + cvType + "\", tsize = " + trainingSize + ", CVnum=" + numFolds + ")";
            }else {
                rCommand = "RSVM.Anal(NA" + ", " + 10 + ")";
            }
            RCenter.recordRCommand(RC, rCommand);
            RC.voidEval(rCommand);
        } catch (Exception rse) {
            LOGGER.error("InitSVMAnal", rse);
        }
    }

    //frequencies of being selected in the best classifier
    public static double[][] getSVMSigMat(SessionBean1 sb) {
        try {
            //make into decreasing oder
            String rCommand = "GetSVMSigMat(NA)";
            return sb.getRConnection().eval(rCommand).asDoubleMatrix();
        } catch (Exception e) {
            LOGGER.error("GetSVMSigMat", e);
        }
        return null;
    }

    public static String[] getSVMSigRowNames(SessionBean1 sb) {
        try {
            String rCommand = "GetSVMSigRowNames(NA)";
            return sb.getRConnection().eval(rCommand).asStrings();
        } catch (Exception e) {
            LOGGER.error("GetSVMSigRowNames", e);
        }
        return null;
    }

    public static String[] getSVMSigColNames(SessionBean1 sb) {
        try {
            String rCommand = "GetSVMSigColNames(NA)";
            return sb.getRConnection().eval(rCommand).asStrings();
        } catch (Exception e) {
            LOGGER.error("GetSVMSigColNames", e);
        }
        return null;
    }

    public static void plotSVMClassification(SessionBean1 sb, String imgName, String format, int dpi) {
        try {
            RConnection RC = sb.getRConnection();
            String rCommand = "PlotRSVM.Classification(NA" + ", \"" + imgName + "\", \"" + format + "\", " + dpi + ", width=NA)";
            RCenter.recordRCommand(RC, rCommand);
            sb.addGraphicsCMD("svm_cls", rCommand);
            RC.voidEval(rCommand);
        } catch (Exception rse) {
            LOGGER.error("PlotSVMClassification", rse);
        }
    }

    public static void plotSVMSigCmpds(SessionBean1 sb, String imgName, String format, int dpi) {
        try {
            RConnection RC = sb.getRConnection();
            String rCommand = "PlotRSVM.Cmpd(NA" + ", \"" + imgName + "\", \"" + format + "\", " + dpi + ", width=NA)";
            RCenter.recordRCommand(RC, rCommand);
            sb.addGraphicsCMD("svm_imp", rCommand);
            RC.voidEval(rCommand);
        } catch (Exception rse) {
            LOGGER.error("PlotSVMSigCmpds", rse);
        }
    }
}
