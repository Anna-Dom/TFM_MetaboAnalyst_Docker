/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package metaboanalyst.controllers.multifac;

import java.io.Serializable;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import metaboanalyst.controllers.ApplicationBean1;
import metaboanalyst.controllers.SessionBean1;
import metaboanalyst.rwrappers.RDataUtils;
import metaboanalyst.utils.DataUtils;
import org.primefaces.model.file.UploadedFile;
import org.rosuda.REngine.Rserve.RConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author jianguox
 */
@RequestScoped
@Named("tsuploader")
public class TimeUploadBean implements Serializable {

    private final ApplicationBean1 ab = (ApplicationBean1) DataUtils.findBean("applicationBean1");
    private final SessionBean1 sb = (SessionBean1) DataUtils.findBean("sessionBean1");
    private static final Logger LOGGER = LogManager.getLogger(TimeUploadBean.class);
    private final MultifacBean tb = (MultifacBean) DataUtils.findBean("multifacBean");

    private UploadedFile csvFile;

    public UploadedFile getCsvFile() {
        return csvFile;
    }

    public void setCsvFile(UploadedFile csvFile) {
        this.csvFile = csvFile;
    }

    private UploadedFile metaFile;

    public UploadedFile getMetaFile() {
        return metaFile;
    }

    public void setMetaFile(UploadedFile metaFile) {
        this.metaFile = metaFile;
    }

    private String tsDataType = "conc";

    public String getTsDataType() {
        return tsDataType;
    }

    public void setTsDataType(String tsDataType) {
        this.tsDataType = tsDataType;
    }

    private String tsFormat = "colmf";

    public String getTsFormat() {
        return tsFormat;
    }

    public void setTsFormat(String tsFormat) {
        this.tsFormat = tsFormat;
    }

    private String timeDataOpt = "pkcovid";

    public String getTimeDataOpt() {
        return timeDataOpt;
    }

    public void setTimeDataOpt(String timeDataOpt) {
        this.timeDataOpt = timeDataOpt;
    }

    public String handleTsDataUpload() {

        if (csvFile == null || metaFile == null) {
            DataUtils.updateMsg("Error", "You must provide both data and metadata files in order to proceed!");
            return null;
        }
        if (csvFile.getSize() == 0 || metaFile.getSize() == 0) {
            DataUtils.updateMsg("Error", "Make sure both files are not empty!");
            return null;
        }
        if (sb.doLogin(tsDataType, "mf", false, false)) {
            try {
                String tsDesign = sb.getTsDesign();
                RConnection RC = sb.getRConnection();
                RDataUtils.setDesignType(RC, tsDesign);
                if (tsDesign.equals("time0") || tsDesign.equals("time")) {
                    tb.setDisableMetaSelection(true);
                }
                String fileName = DataUtils.uploadFile(csvFile, sb.getCurrentUser().getHomeDir(), null, ab.isOnPublicServer());
                if (fileName == null) {
                    return null;
                }
                if (RDataUtils.readTextDataTs(RC, fileName, tsFormat)) {
                    String metaName = DataUtils.uploadFile(metaFile, sb.getCurrentUser().getHomeDir(), null, ab.isOnPublicServer());
                    if (metaName == null) {
                        return null;
                    }
                    boolean res = RDataUtils.readMetaData(RC, metaName);
                    if (!res) {
                        String err = RDataUtils.getErrMsg(RC);
                        DataUtils.updateMsg("Error", "Failed meta-data integrity check." + err);
                        return null;
                    }
                    sb.setDataUploaded();
                    tb.reinitVariables();
                    return "Data check";
                } else {
                    DataUtils.updateMsg("Error", RDataUtils.getErrMsg(RC));
                    return null;
                }
            } catch (Exception e) {
                //e.printStackTrace();
                LOGGER.error("handleTsDataUpload", e);
            }
        }
        DataUtils.updateMsg("Error", "Log in failed. Please check errors in your R codes or the Rserve permission setting!");
        return null;
    }

    //note "mf" here indicate for metadata table module
    //whether it is time or not is based on tsDesign
    public String handleTestDataUpload() {
        String fileName;
        String testMetaFile;
        String tsDesign;
        switch (timeDataOpt) {
            case "time2":
                tsDataType = "pktable";
                tsDesign = "time";  //indicate whether this is actual time series
                tsFormat = "colmf"; // not ts here is for metadata module
                fileName = ab.getResourceByAPI("cress_time.csv");
                testMetaFile = ab.getResourceByAPI("cress_time_meta.csv");
                break;
            case "pkcovid":
                tsDataType = "pktable";
                tsDesign = "multi";
                tsFormat = "colmf";//colts
                fileName = ab.getResourceByAPI("covid_metabolomics_data.csv");
                testMetaFile = ab.getResourceByAPI("covid_metadata_multiclass.csv");
                break;
            case "tce":
                tsDataType = "pktable";
                tsDesign = "multi";
                tsFormat = "colmf";//colts
                fileName = ab.getResourceByAPI("TCE_feature_table.csv");
                testMetaFile = ab.getResourceByAPI("TCE_metadata.csv");
                break;
            case "diabetes":
                tsDataType = "pktable";
                tsDesign = "multi";
                tsFormat = "rowmf";
                fileName = ab.getResourceByAPI("diabetes_lipids.txt");
                testMetaFile = ab.getResourceByAPI("diabetes_metadata.csv");
                break;
            default:
                tsDataType = "pktable";
                tsDesign = "time0";
                tsFormat = "rowmf";
                fileName = ab.getResourceByAPI("cress_time1.csv");
                testMetaFile = ab.getResourceByAPI("cress_time1_meta.csv");
                break;
        }
        if (sb.doLogin(tsDataType, "mf", false, false)) {
            try {
                RConnection RC = sb.getRConnection();
                sb.setTsDesign(tsDesign);
                RDataUtils.setDesignType(RC, tsDesign);
                if (RDataUtils.readTextDataTs(RC, fileName, tsFormat)) {
                    sb.setDataUploaded();
                    boolean res = RDataUtils.readMetaData(RC, testMetaFile);
                    if (!res) {
                        DataUtils.updateMsg("Error", RDataUtils.getErrMsg(RC));
                        return null;
                    }
                    //System.out.println("var");
                    tb.reinitVariables();
                    return "Data check";
                } else {
                    DataUtils.updateMsg("Error", RDataUtils.getErrMsg(RC));
                    return null;
                }
            } catch (Exception e) {
                // e.printStackTrace();
                LOGGER.error("handleTestDataUpload", e);
            }
        }
        DataUtils.updateMsg("Error", "Log in failed. Please check errors in your R codes or the Rserve permission setting!");
        return null;
    }
}
