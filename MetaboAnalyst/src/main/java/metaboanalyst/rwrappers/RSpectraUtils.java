/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package metaboanalyst.rwrappers;

import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author qiang
 */
public class RSpectraUtils {

    private static final Logger LOGGER = LogManager.getLogger(RSpectraUtils.class);

    /// 1. Plotting ---- A Bunch of Plotting functions below    
    //  1.1 PLOT TIC of the specific file
    public static String plotSingleTIC(RConnection RC, String fileName, int imageNm, String format, int dpi, String width) {
        try {
            if (!"png".equals(format)) {
                dpi = 72;
            }
            String imgName;
            imgName = RC.eval("plotSingleTIC(\"" + fileName + "\"," + imageNm + ",\"" + format + "\"," + dpi + "," + width + ")").asString();
            return imgName;
        } catch (REXPMismatchException | RserveException e) {

        }
        return null;
    }

    // 1.2 PLOT summary of specific feature - MS spec module
    public static String plotMSfeature(RConnection RC, int featureNm, String format, int dpi, String width) {
        /// Note: This function is not using the image count (imageNm) because the fixed format of images at spectral report module
        try {
            if (!"png".equals(format)) {
                dpi = 72;
            }
            String featureimageNM = RC.eval("plotMSfeature(" + featureNm + ",\"" + format + "\"," + dpi + "," + width + ")").asString();
            return featureimageNM;
        } catch (REXPMismatchException | RserveException e) {
            LOGGER.error("plotMSfeature", e);
        }
        return null;
    }

    // 1.3 PLOT (Update) EIC of a certain feature
    public static String plotXIC(RConnection RC, int featureNm, String format, int dpi, String width) {
        try {
            if (!"png".equals(format)) {
                dpi = 72;
            }
            String featureimageNM2 = RC.eval("PlotXICUpdate(" + featureNm + ",\"" + format + "\"," + dpi + "," + width + ")").asString();
            return featureimageNM2;
        } catch (REXPMismatchException | RserveException e) {
            LOGGER.error("plotXIC", e);
        }
        return null;
    }

    // 1.4 PLOT TIC of all files
    public static String plotTICs(RConnection RC, int imageNm, String format, int dpi, String width) {
        try {
            if (!"png".equals(format)) {
                dpi = 72;
            }
            String imageName = RC.eval("plotTICs(" + imageNm + ",\"" + format + "\"," + dpi + "," + width + ")").asString();
            return imageName;
        } catch (REXPMismatchException | RserveException e) {
            LOGGER.error("plotTICs", e);
        }
        return null;
    }

    // 1.5 PLOT BPI of all files
    public static String plotBPIs(RConnection RC, int imageNm, String format, int dpi, String width) {
        try {
            if (!"png".equals(format)) {
                dpi = 72;
            }
            String imageName = RC.eval("plotBPIs(" + imageNm + ",\"" + format + "\"," + dpi + "," + width + ")").asString();
            return imageName;
        } catch (REXPMismatchException | RserveException e) {
            LOGGER.error("plotBPIs", e);
        }
        return null;
    }

    // 1.6 PLOT Result of Retention Time correction of all files
    public static String plotRTcor(RConnection RC, int imageNm, String format, int dpi, String width) {
        try {
            if (!"png".equals(format)) {
                dpi = 72;
            }
            String imageName = RC.eval("PlotSpectraRTadj(" + imageNm + ",\"" + format + "\"," + dpi + "," + width + ")").asString();
            return imageName;
        } catch (Exception e) {
            LOGGER.error("plotRTcor", e);
        }
        return null;
    }

    // 1.7 PLOT Corrected BPI of all files
    public static String plotBPIcor(RConnection RC, int imageNm, String format, int dpi, String width) {
        try {
            if (!"png".equals(format)) {
                dpi = 72;
            }
            String imageName = RC.eval("PlotSpectraBPIadj(" + imageNm + ",\"" + format + "\"," + dpi + "," + width + ")").asString();
            return imageName;
        } catch (REXPMismatchException | RserveException e) {
            LOGGER.error("plotBPIcor", e);
        }
        return null;
    }

    public static String plotIntenStats(RConnection RC, int imageNm, String format, int dpi, String width) {
        try {
            if (!"png".equals(format)) {
                dpi = 72;
            }
            String imageName = RC.eval("PlotSpectraInsensityStatics(" + imageNm + ",\"" + format + "\"," + dpi + "," + width + ")").asString();
            return imageName;
        } catch (REXPMismatchException | RserveException e) {
            LOGGER.error("plotIntenStats", e);
        }
        return null;
    }

    //2. Utilities
    //2.1 ID convert (mz@rt --> FTID)
    public static int mzrt2ID(RConnection RC, String mzrt) {
        try {

            int FTID = RC.eval("mzrt2ID(\"" + mzrt + "\")").asInteger();
            return FTID;
        } catch (REXPMismatchException | RserveException e) {
            LOGGER.error("mzrt2ID", e);
        }
        return 0;
    }

    public static int mzrt2ID2(RConnection RC, String mzrt) {
        try {

            int FTID = RC.eval("mzrt2ID2(\"" + mzrt + "\")").asInteger();
            return FTID;
        } catch (REXPMismatchException | RserveException e) {
            LOGGER.error("mzrt2ID2", e);
        }
        return 0;
    }

    public static int convertFTno2Num(RConnection RC, int FTno) {
        try {

            int FTnum = RC.eval("FTno2Num(" + FTno + ")").asInteger();
            return FTnum;
        } catch (REXPMismatchException | RserveException e) {
            LOGGER.error("FTno2Num", e);
        }
        return 0;
    }

    public static String[] readAdductList(RConnection RC, String polarity) {

        try {
            String[] AdductList = RC.eval("readAdductsList(\"" + polarity + "\")").asStrings();
            return AdductList;
        } catch (REXPMismatchException | RserveException e) {
            LOGGER.error("readAdductList", e);
        }

        return null;
    }

    public static String retrieveModeInfo(RConnection RC) {

        try {
            String modeInfo = RC.eval("retrieveModeInfo()").asString();
            return modeInfo;
        } catch (REXPMismatchException | RserveException e) {
            LOGGER.error("retrieveModeInfo", e);
            return null;
        }

    }

    //triggered by clicking on data point in box plot (3d pca)
    public static String plotSingleXIC(RConnection RC, String sample, String feature, boolean showlabel) {
        String showl = "FALSE";
        try {
            if (showlabel) {
                showl = "TRUE";
            }
            String imgName;
            //System.out.println("plotSingleXIC(NA" + ", " + sample + ",\"" + feature + "\")");
            imgName = RC.eval("plotSingleXIC(NA" + ", " + sample + ",\"" + feature + "\", showlabel = " + showl + ")").asString();
            return imgName;
        } catch (REXPMismatchException | RserveException e) {
            LOGGER.error("plotSingleXIC", e);
        }
        return null;
    }

    public static void cleanEIClayer(RConnection RC, int featureNum) {
        try {
            RC.voidEval("cleanEICLayer(" + featureNum + ");");
        } catch (Exception e) {
            LOGGER.error("cleanEICLayer", e);

        }
    }

    public static int getMalariaRawData(RConnection RC, String homedir) {
        try {
            int res = RC.eval("getMalariaRawData(\"" + homedir + "\");").asInteger();
            return res;
        } catch (Exception e) {
            LOGGER.error("getMalariaRawData", e);
            return 0;
        }
    }

    public static String[] getResSummaryMsg(RConnection RC) {
        try {
            return RC.eval("PerformResultSummary(mSet = NA)").asStrings();
        } catch (Exception e) {
            LOGGER.error("getResSummaryMsg", e);
        }
        return null;
    }

    public static String extractHMDBCMPD(RConnection RC, String formula, int featureOrder) {
        //PerformExtractHMDBCMPD
        try {
            return RC.eval("PerformExtractHMDBCMPD(formula =\"" + formula + "\", featureOrder = " + featureOrder + ")").asString();
        } catch (Exception e) {
            LOGGER.error("extractHMDBCMPD", e);
        }
        return null;
    }

    public static boolean ensureDataExits(RConnection RC, String fileNM) {
        try {
            int res = RC.eval("checkdataexits(fileNM =\"" + fileNM + "\")").asInteger();
            if (res == 1) {
                return true;
            }
        } catch (Exception e) {
            LOGGER.error("ensureDataExits", e);
        }
        return false;
    }

    public static boolean generateParamFile(RConnection RC) {
        try {
            int res = RC.eval("GenerateParamFile()").asInteger();
            if (res == 1) {
                return true;
            }
        } catch (Exception e) {
            LOGGER.error("GenerateParamFile", e);
        }
        return false;
    }
}
