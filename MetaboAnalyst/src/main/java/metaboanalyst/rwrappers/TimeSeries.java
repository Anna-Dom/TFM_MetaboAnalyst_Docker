/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package metaboanalyst.rwrappers;

import java.util.Arrays;
import metaboanalyst.controllers.SessionBean1;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

/**
 *
 * @author Jeff
 */
public class TimeSeries {

    public static void initIPCA(RConnection RC, String fileNm, String selMeta1, String selMeta2) {
        try {
            String rCommand = "iPCA.Anal(NA" + ", \"" + fileNm + "\", \"" + selMeta1 + "\", \"" + selMeta2 + "\")";
            RCenter.recordRCommand(RC, rCommand);
            RC.voidEval(rCommand);
        } catch (RserveException rse) {
            System.out.println(rse);
        }
    }

    public static void prepareLiveGraphics(RConnection RC, String urlPath, int inx) {
        try {
            String rCommand = "SetLiveGraphics(\"" + urlPath + "\", " + inx + ")";
            RCenter.recordRCommand(RC, rCommand);
            RC.voidEval(rCommand);
        } catch (RserveException rse) {
            System.out.println(rse);
        }
    }

    public static int plotHeatMap2(SessionBean1 sb, String imgName, String dataOpt, String scaleOpt, String format, int dpi, String smplDist, String clstDist, String colors, int fontSize, String viewOpt, String rankMethod, int topFeature, String[] sortNames, String useSigFeature, String drawBorder, String showLegend, String showAnnotLegend, String includeRowNames, String[] selectedMetas) {
        try {
            RConnection RC = sb.getRConnection();
            RC.assign("meta.vec.hm2", selectedMetas);
            RCenter.recordRCommand(RC, "meta.vec.hm2 <- " + Arrays.toString(selectedMetas));
            RC.assign("sort.vec.hm2", sortNames);
            RCenter.recordRCommand(RC, "sort.vec.hm2 <- " + Arrays.toString(sortNames));
            String rCommand = "PlotHeatMap2(NA" + ", \"" + imgName + "\", \"" + dataOpt + "\", \"" + scaleOpt + "\", \"" + format + "\", " + dpi + ", width=NA, \"" + smplDist + "\",\"" + clstDist + "\",\"" + colors + "\", " + fontSize +", \"" + viewOpt + "\",\"" + rankMethod + "\"," + topFeature + ", " + useSigFeature + ",  " 
                    + drawBorder + ", " + showLegend + ", " + showAnnotLegend  + ", " + includeRowNames + ")";
            RCenter.recordRCommand(RC, rCommand);
            sb.addGraphicsCMD("heatmap2", rCommand);
            return RC.eval(rCommand).asInteger();
        } catch (Exception rse) {
            System.out.println(rse);
        }
        return 0;
    }

    public static void plotPCAPairSummaryMeta(SessionBean1 sb, String imageName, String format, int dpi, int pcNum, String meta, String metaShape) {
        try {
            RConnection RC = sb.getRConnection();
            String rCommand = "PlotPCAPairSummaryMeta(NA" + ", \"" + imageName + "\", \"" + format + "\", " + dpi + ", width=NA, " + pcNum + ", \"" + meta + "\", \"" + metaShape + "\")";
            sb.addGraphicsCMD("pca_pair_meta", rCommand);
            RCenter.recordRCommand(RC, rCommand);
            RC.voidEval(rCommand);
        } catch (RserveException rse) {
            System.out.println(rse);
        }
    }

    public static int initANOVA2(RConnection RC, double thresh, String corType, String type, String phenOpt, String[] selectedMetas) {
        try {
            RC.assign("meta.vec.aov", selectedMetas);
            RCenter.recordRCommand(RC, "meta.vec.aov <- " + Arrays.toString(selectedMetas));
            String rCommand = "ANOVA2.Anal(NA, " + thresh + ", \"" + corType + "\", \"" + type + "\", \"" + phenOpt + "\")";
            RCenter.recordRCommand(RC, rCommand);
            return RC.eval(rCommand).asInteger();
        } catch (Exception rse) {
            System.out.println(rse);
        }
        return 0;
    }

    public static void plotAOV2(SessionBean1 sb, String imgName, String format, int dpi) {
        try {
            RConnection RC = sb.getRConnection();
            String rCommand = "PlotANOVA2(NA" + ", \"" + imgName + "\", \"" + format + "\", " + dpi + ", width=NA)";
            RCenter.recordRCommand(RC, rCommand);
            sb.addGraphicsCMD("aov2", rCommand);
            RC.voidEval(rCommand);
        } catch (RserveException rse) {
            System.out.println(rse);
        }
    }

    public static String getAov2SigFileName(SessionBean1 sb) {
        try {
            String rCommand = "GetAov2SigFileName(NA)";
            return sb.getRConnection().eval(rCommand).asString();
        } catch (Exception rse) {
            System.out.println(rse);
        }
        return null;
    }

    public static String getAscaSigFileName(SessionBean1 sb) {
        try {
            String rCommand = "GetAscaSigFileName(NA)";
            //  RCenter.recordRCommand(RC, rCommand);
            return sb.getRConnection().eval(rCommand).asString();
        } catch (Exception rse) {
            System.out.println(rse);
        }
        return null;
    }

    //threshx is fc, threshy is pvalue
    public static double[][] getAov2SigMat(RConnection RC) {
        try {
            String rCommand = "GetAov2SigMat(NA)";
            double[][] cmpds = RC.eval(rCommand).asDoubleMatrix();
            return cmpds;
        } catch (Exception rse) {
            System.out.println(rse);
        }
        return null;
    }

    public static String[] getAov2SigRowNames(RConnection RC) {
        try {
            String rCommand = "GetAov2SigRowNames(NA)";
            String[] nms = RC.eval(rCommand).asStrings();
            return nms;
        } catch (Exception rse) {
            System.out.println(rse);
        }
        return null;
    }

    public static String[] getAov2SigColNames(RConnection RC) {
        try {
            String rCommand = "GetAov2SigColNames(NA)";
            String[] nms = RC.eval(rCommand).asStrings();
            return nms;
        } catch (Exception rse) {
            System.out.println(rse);
        }
        return null;
    }

    public static int performASCA(RConnection RC, int a, int b, int x, int res, String[] selectedMetas) {
        try {
            RC.assign("meta.vec.asca", selectedMetas);
            RCenter.recordRCommand(RC, "meta.vec.asca <- " + Arrays.toString(selectedMetas));
            String rCommand = "Perform.ASCA(NA" + ", " + a + ", " + b + ", " + x + ", " + res + ")";
            RCenter.recordRCommand(RC, rCommand);
            return RC.eval(rCommand).asInteger();
        } catch (Exception rse) {
            System.out.println(rse);
        }
        return 0;
    }

    public static void performASCAVarSelection(RConnection RC, double speThresh, double lvThresh) {
        try {
            String rCommand = "CalculateImpVarCutoff(NA" + ", " + speThresh + ", " + lvThresh + ")";
            RCenter.recordRCommand(RC, rCommand);
            RC.voidEval(rCommand);
        } catch (RserveException rse) {
            System.out.println(rse);
        }
    }

    public static void performASCAPermutation(RConnection RC, int permNum) {
        try {
            String rCommand = "Perform.ASCA.permute(NA" + ", " + permNum + ")";
            RCenter.recordRCommand(RC, rCommand);
            RC.voidEval(rCommand);
        } catch (RserveException rse) {
            System.out.println(rse);
        }
    }

    public static void plotASCAPermSummary(SessionBean1 sb, String imgName, String format, int dpi) {
        try {
            RConnection RC = sb.getRConnection();
            String rCommand = "PlotASCA.Permutation(NA" + ", \"" + imgName + "\", \"" + format + "\", " + dpi + ", width=NA)";
            RCenter.recordRCommand(RC, rCommand);
            sb.addGraphicsCMD("asca_perm", rCommand);
            RC.voidEval(rCommand);
        } catch (RserveException rse) {
            System.out.println(rse);
        }
    }

    public static void plotASCAscree(SessionBean1 sb, String imgName, String format, int dpi) {
        try {
            RConnection RC = sb.getRConnection();
            String rCommand = "PlotModelScree(NA" + ", \"" + imgName + "\", \"" + format + "\", " + dpi + ", width=NA)";
            RCenter.recordRCommand(RC, rCommand);
            sb.addGraphicsCMD("asca_scree", rCommand);
            RC.voidEval(rCommand);
        } catch (RserveException rse) {
            System.out.println(rse);
        }
    }

    public static void plotASCAModels(SessionBean1 sb, String imgName, String format, int dpi, String type, String colorBW) {
        try {
            RConnection RC = sb.getRConnection();
            String rCommand = "PlotASCAModel(NA" + ", \"" + imgName + "\", \"" + format + "\", " + dpi + ", width=NA, \"" + type + "\"," + colorBW + ")";
            RCenter.recordRCommand(RC, rCommand);
            sb.addGraphicsCMD("asca_f" + type, rCommand);
            RC.voidEval(rCommand);
        } catch (RserveException rse) {
            System.out.println(rse);
        }
    }

    public static void plotASCAInteraction(SessionBean1 sb, String imgName, String format, int dpi, String colorBW) {
        try {
            RConnection RC = sb.getRConnection();
            String rCommand = "PlotInteraction(NA" + ", \"" + imgName + "\", \"" + format + "\", " + dpi + "," + colorBW + ", width=NA)";
            RCenter.recordRCommand(RC, rCommand);
            sb.addGraphicsCMD("asca_fab", rCommand);
            RC.voidEval(rCommand);
        } catch (RserveException rse) {
            System.out.println(rse);
        }
    }

    public static void plotASCAImpVar(SessionBean1 sb, String imgName, String format, int dpi, String type) {
        try {
            RConnection RC = sb.getRConnection();
            String rCommand = "PlotAscaImpVar(NA" + ", \"" + imgName + "\", \"" + format + "\", " + dpi + ", width=NA, \"" + type + "\")";
            RCenter.recordRCommand(RC, rCommand);
            sb.addGraphicsCMD("asca_imp" + type, rCommand);
            RC.voidEval(rCommand);
        } catch (RserveException rse) {
            System.out.println(rse);
        }
    }

    //threshx is fc, threshy is pvalue
    public static double[][] getAscaSigMat(SessionBean1 sb, String type) {
        try {
            String rCommand = "GetAscaSigMat(NA" + ", \"" + type + "\")";
            return sb.getRConnection().eval(rCommand).asDoubleMatrix();
        } catch (Exception rse) {
            System.out.println(rse);
        }
        return null;
    }

    public static String[] getAscaSigRowNames(SessionBean1 sb, String type) {
        try {
            String rCommand = "GetAscaSigRowNames(NA" + ", \"" + type + "\")";
            return sb.getRConnection().eval(rCommand).asStrings();
        } catch (Exception rse) {
            System.out.println(rse);
        }
        return null;
    }

    public static String[] getAscaSigColNames(SessionBean1 sb, String type) {
        try {
            String rCommand = "GetAscaSigColNames(\"" + type + "\")";
            return sb.getRConnection().eval(rCommand).asStrings();
        } catch (Exception rse) {
            System.out.println(rse);
        }
        return null;
    }

    public static int performMB(RConnection RC, int topPerc, String[] metas) {
        try {
            RC.assign("meta.vec.mb", metas);
            RCenter.recordRCommand(RC, "meta.vec.mb <- " + Arrays.toString(metas));
            String rCommand = "performMB(NA" + ", " + topPerc + ")";
            RCenter.recordRCommand(RC, rCommand);
            return (RC.eval(rCommand).asInteger());
        } catch (Exception rse) {
            System.out.println(rse);
        }
        return 0;
    }

    public static String plotMBTimeProfile(SessionBean1 sb, String cmpdName, int version, String format, String dpi) {
        try {
            RConnection RC = sb.getRConnection();
            String rCommand = "PlotMBTimeProfile(NA" + ", \"" + cmpdName + "\", " + version + ", \"" + format + "\", " + dpi + ", width=NA)";
            RCenter.recordRCommand(RC, rCommand);
            sb.addGraphicsCMD("mb", rCommand);
            return RC.eval(rCommand).asString();
        } catch (Exception rse) {
            System.out.println(rse);
        }
        return null;
    }

    //threshx is fc, threshy is pvalue
    public static double[][] getMBSigMat(RConnection RC) {
        try {
            String rCommand = "GetMBSigMat(NA)";
            double[][] cmpds = RC.eval(rCommand).asDoubleMatrix();
            return cmpds;
        } catch (Exception rse) {
            System.out.println(rse);
        }
        return null;
    }

    public static String[] getMBSigRowNames(RConnection RC) {
        try {
            String rCommand = "GetMBSigRowNames(NA)";
            String[] nms = RC.eval(rCommand).asStrings();
            return nms;
        } catch (Exception rse) {
            System.out.println(rse);
        }
        return null;
    }

    public static String[] getMBSigColNames(RConnection RC) {
        try {
            String rCommand = "GetMBSigColNames(NA)";
            String[] nms = RC.eval(rCommand).asStrings();
            return nms;
        } catch (Exception rse) {
            System.out.println(rse);
        }
        return null;
    }

    public static double[][] getAnova2DnMat(RConnection RC) {
        try {
            String rCommand = "GetAnova2DnMat(NA)";
            return RC.eval(rCommand).asDoubleMatrix();
        } catch (REXPMismatchException e) {
            System.out.println(e);
        } catch (RserveException rse) {
            System.out.println(rse);
        }
        return null;
    }

    public static double[][] getAnova2UpMat(RConnection RC) {
        try {
            String rCommand = "GetAnova2UpMat(NA)";
            return RC.eval(rCommand).asDoubleMatrix();
        } catch (REXPMismatchException e) {
            System.out.println(e);
        } catch (RserveException rse) {
            System.out.println(rse);
        }
        return null;
    }

    public static String[] getAnova2UpCmpds(RConnection RC) {
        try {
            String rCommand = "GetAov2UpIDs(NA)";
            return RC.eval(rCommand).asStrings();
        } catch (REXPMismatchException e) {
            System.out.println(e);
        } catch (RserveException rse) {
            System.out.println(rse);
        }
        return null;
    }

    public static String[] getAnova2DnCmpds(RConnection RC) {
        try {
            String rCommand = "GetAov2DnIDs(NA)";
            return RC.eval(rCommand).asStrings();
        } catch (REXPMismatchException e) {
            System.out.println(e);
        } catch (RserveException rse) {
            System.out.println(rse);
        }
        return null;
    }

    public static double[][] getCovDnMat(RConnection RC) {
        try {
            String rCommand = "GetCovDnMat(NA)";
            return RC.eval(rCommand).asDoubleMatrix();
        } catch (REXPMismatchException e) {
            System.out.println(e);
        } catch (RserveException rse) {
            System.out.println(rse);
        }
        return null;
    }

    public static double[][] getCovUpMat(RConnection RC) {
        try {
            String rCommand = "GetCovUpMat(NA)";
            return RC.eval(rCommand).asDoubleMatrix();
        } catch (REXPMismatchException e) {
            System.out.println(e);
        } catch (RserveException rse) {
            System.out.println(rse);
        }
        return null;
    }

    public static String[] getCovUpIDs(RConnection RC) {
        try {
            String rCommand = "GetCovUpIDs(NA)";
            String[] nms = RC.eval(rCommand).asStrings();
            return nms;
        } catch (Exception rse) {
            System.out.println(rse);
        }
        return null;
    }

    public static String[] getCovDnIDs(RConnection RC) {
        try {
            String rCommand = "GetCovDnIDs(NA)";
            String[] nms = RC.eval(rCommand).asStrings();
            return nms;
        } catch (Exception rse) {
            System.out.println(rse);
        }
        return null;
    }

    //threshx is fc, threshy is pvalue
    public static double[][] getCovSigMat(RConnection RC) {
        try {
            String rCommand = "GetCovSigMat(NA)";
            double[][] cmpds = RC.eval(rCommand).asDoubleMatrix();
            return cmpds;
        } catch (Exception rse) {
            System.out.println(rse);
        }
        return null;
    }

    public static String[] getCovSigRowNames(RConnection RC) {
        try {
            String rCommand = "GetCovSigRowNames(NA)";
            String[] nms = RC.eval(rCommand).asStrings();
            return nms;
        } catch (Exception rse) {
            System.out.println(rse);
        }
        return null;
    }

    public static String[] getCovSigColNames(RConnection RC) {
        try {
            String rCommand = "GetCovSigColNames(NA)";
            String[] nms = RC.eval(rCommand).asStrings();
            return nms;
        } catch (Exception rse) {
            System.out.println(rse);
        }
        return null;
    }

    public static String getCovSigFileName(SessionBean1 sb) {
        try {
            String rCommand = "GetCovSigFileName(NA)";
            return sb.getRConnection().eval(rCommand).asString();
        } catch (Exception rse) {
            System.out.println(rse);
        }
        return null;
    }

    public static boolean performCorrAnal(SessionBean1 sb, String algo, String tgtType, String tgtName, String[] adjustedVar) {
        try {
            RConnection RC = sb.getRConnection();
            RC.assign("cov.vec", adjustedVar);
            String rCommand = "FeatureCorrelationMeta(NA" + ", \"" + algo + "\", \"" + tgtType + "\", \"" + tgtName + "\")";
            RCenter.recordRCommand(RC, rCommand);
            return RC.eval(rCommand).asInteger() == 1;
        } catch (Exception rse) {
            System.out.println(rse);
        }
        return false;
    }

}
