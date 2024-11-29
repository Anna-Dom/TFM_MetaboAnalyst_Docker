/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package metaboanalyst.controllers.stats;

import java.io.File;
import org.rosuda.REngine.RList;
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
@Named("uploader")
public class UploadBean implements Serializable {

    private final ApplicationBean1 ab = (ApplicationBean1) DataUtils.findBean("applicationBean1");
    private final SessionBean1 sb = (SessionBean1) DataUtils.findBean("sessionBean1");
    private static final Logger LOGGER = LogManager.getLogger(UploadBean.class);
    /*
     * Handle file upoad (.csv or .txt)
     */
    private String dataType = "conc";

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    private String dataFormat = "rowu";

    public String getDataFormat() {
        return dataFormat;
    }

    public void setDataFormat(String dataFormat) {
        this.dataFormat = dataFormat;
    }

    private UploadedFile dataFile;

    public UploadedFile getDataFile() {
        return dataFile;
    }

    public void setDataFile(UploadedFile dataFile) {
        this.dataFile = dataFile;
    }

    /*
    Data upload for statistics module
     */
    public String handleFileUpload() {

        if (dataFile == null || dataFile.getSize() == 0) {
            DataUtils.updateMsg("Error", "File is empty");
            return null;
        }

        boolean paired = false;
        if (dataFormat.endsWith("p")) {
            paired = true;
        }

        if (sb.doLogin(dataType, "stat", false, paired)) {
            try {
                RConnection RC = sb.getRConnection();
                String fileName = DataUtils.uploadFile(dataFile, sb.getCurrentUser().getHomeDir(), null, ab.isOnPublicServer());
                if (fileName == null) {
                    return null;
                }

                if (RDataUtils.readTextData(RC, fileName, dataFormat, "disc")) {
                    sb.setDataUploaded();
                    return "Data check";
                } else {
                    String err = RDataUtils.getErrMsg(RC);
                    DataUtils.updateMsg("Error", "Failed to read in the CSV file." + err);
                    return null;
                }
            } catch (Exception e) {
                //e.printStackTrace();
                LOGGER.error("handleFileUpload", e);
            }
        }
        DataUtils.updateMsg("Error", "Log in failed. Please check errors in your R codes or the Rserve permission setting!");
        return null;
    }

    /*
     * Handle zip file examples (containing csv or txt files)
     */
    private UploadedFile zipFile;

    public UploadedFile getZipFile() {
        return zipFile;
    }

    public void setZipFile(UploadedFile zipFile) {
        this.zipFile = zipFile;
    }

    private String zipDataType = "nmrpeak";

    public String getZipDataType() {
        return zipDataType;
    }

    public void setZipDataType(String zipDataType) {
        this.zipDataType = zipDataType;
    }

    private String zipFormat;

    public String getZipFormat() {
        return zipFormat;
    }

    public void setZipFormat(String zipFormat) {
        this.zipFormat = zipFormat;
    }

    private UploadedFile pairFile;

    public UploadedFile getPairFile() {
        return pairFile;
    }

    public void setPairFile(UploadedFile file) {
        this.pairFile = file;
    }

    public String handleZipFileUpload() {

        boolean paired = false;
        if (pairFile != null && pairFile.getSize() > 0) {
            paired = true;
        }

        if (sb.doLogin(zipDataType, "stat", false, paired)) {
            try {
                RConnection RC = sb.getRConnection();
                String homDir = sb.getCurrentUser().getHomeDir();
                DataUtils.uploadFile(zipFile, homDir, null, ab.isOnPublicServer());
                if (paired) {
                    DataUtils.uploadFile(pairFile, homDir, "pairs.txt", ab.isOnPublicServer());
                }
                String homeDir = sb.getCurrentUser().getHomeDir();
                String inPath = zipFile.getFileName();
                String outPath = "upload";

                if (RDataUtils.readZipData(RC, inPath, outPath, zipDataType, homeDir)) {

                    sb.setDataUploaded();
                    sb.initNaviTree("stat-peak");
                    return zipDataType;
                } else {
                    DataUtils.updateMsg("Error", RDataUtils.getErrMsg(RC));
                    return null;
                }
            } catch (Exception e) {
                //e.printStackTrace();
                LOGGER.error("handleZipFileUpload", e);
                return null;
            }
        }

        DataUtils.updateMsg("Error", "Log in failed. Please check errors in your R codes or the Rserve permission setting!");
        return null;
    }

    /*
     * Handle test examples for statistics mode
     */
    private String testDataOpt = "conccancer";

    public String getTestDataOpt() {
        return testDataOpt;
    }

    public void setTestDataOpt(String testDataOpt) {
        this.testDataOpt = testDataOpt;
    }

    public String handleStatTestFileUpload() {
        String format = "";
        boolean paired = false;
        boolean isZip = false;
        boolean isMzTab = false;
        String testFile;
        //String testMetaFile = null;
        switch (testDataOpt) {
            case "conccancer":
                dataType = "conc";
                testFile = ab.getResourceByAPI("human_cachexia.csv");
                format = "rowu";
                break;
            case "conccow":
                dataType = "conc";
                testFile = ab.getResourceByAPI("cow_diet.csv");
                format = "rowu";
                break;
            case "nmrspecbin":
                dataType = "specbin";
                testFile = ab.getResourceByAPI("nmr_bins.csv");
                format = "rowu";
                break;
            case "concpair":
                dataType = "conc";
                paired = true;
                format = "colp";
                testFile = ab.getResourceByAPI("time_series.csv");
                break;
            case "mspkint":
                dataType = "pktable";
                testFile = ab.getResourceByAPI("lcms_table.csv");
                format = "colu";
                break;
            case "nmrpeaklist":
                dataType = "nmrpeak";
                sb.initNaviTree("stat-peak");
                isZip = true;
                testFile = ab.getResourceByAPI("nmr_peaks.zip");
                break;
            case "mspklist":
                dataType = "mspeak";
                sb.initNaviTree("stat-peak");
                isZip = true;
                testFile = ab.getResourceByAPI("lcms_3col_peaks.zip");
                break;
            case "mztabmouse":
                dataType = "mztab";
                isMzTab = true;
                testFile = ab.getResourceByAPI("MouseLiver_negative.mzTab");
                break;
            case "mztabgc":
                dataType = "mztab";
                isMzTab = true;
                testFile = ab.getResourceByAPI("gcms_tms_height.mzTab");
                break;
            default:
                DataUtils.updateMsg("Error", "Unknown data selected?");
                return null;
        }

        if (!sb.doLogin(dataType, "stat", false, paired)) {
            return null;
        }

        RConnection RC = sb.getRConnection();

        if (isZip) {

            String homeDir = sb.getCurrentUser().getHomeDir();
            //need to fetch here first from URL
            String name = DataUtils.getJustFileName(testFile);
            String outpath = homeDir + File.separator + name;
            DataUtils.fetchFile(testFile, new File(outpath));
            if (!RDataUtils.readZipData(RC, outpath, "upload", dataType, homeDir)) {
                DataUtils.updateMsg("Error", RDataUtils.getErrMsg(RC));
                return null;
            }
        } else if (isMzTab) {
            if (!RDataUtils.readMzTabData(RC, testFile, identifier)) {
                DataUtils.updateMsg("Error", RDataUtils.getErrMsg(RC));
                return null;
            }
        } else {
            if (!RDataUtils.readTextData(RC, testFile, format, "disc")) {
                DataUtils.updateMsg("Error", RDataUtils.getErrMsg(RC));
                return null;
            }
        }
        sb.setDataUploaded();

        if (dataType.equals("conc") || dataType.equals("pktable") || dataType.equals("specbin") || dataType.equals("mztab")) {
            return "Data check";
        }
        return dataType;
    }

    /*
    Handle data for power analysis
     */
    private boolean useExample = false;

    public boolean isUseExample() {
        return useExample;
    }

    public void setUseExample(boolean useExample) {
        this.useExample = useExample;
    }

    public String uploadPilotData() {
        //check if data is uploaded
        if (useExample) {
            return handlePowerTestFileUpload();
        }

        if (dataFile.getSize() == 0) {
            DataUtils.updateMsg("Error", "File is empty");
            return null;
        }

        boolean paired = false;
        if (dataFormat.endsWith("p")) {
            paired = true;
        }
        if (sb.doLogin(dataType, "power", false, paired)) {
            RConnection RC = sb.getRConnection();
            String fileName = DataUtils.uploadFile(dataFile, sb.getCurrentUser().getHomeDir(), null, ab.isOnPublicServer());
            if (RDataUtils.readTextData(RC, fileName, dataFormat, "disc")) {
                sb.setDataUploaded();
                return "Data check";
            } else {
                DataUtils.updateMsg("Error:", RDataUtils.getErrMsg(RC));
                return null;
            }
        }
        return null;
    }

    public String handlePowerTestFileUpload() {
        if (!sb.doLogin("conc", "power", false, false)) {
            return null;
        }
        RConnection RC = sb.getRConnection();
        RDataUtils.readTextData(RC, ab.getResourceByAPI("human_cachexia.csv"), "rowu", "disc");
        sb.setDataUploaded();
        return "Data check";
    }

    /*
    ROC data upload
     */
    private String dataOpt = "data1";

    public String getDataOpt() {
        return dataOpt;
    }

    public void setDataOpt(String dataOpt) {
        this.dataOpt = dataOpt;
    }

    public String uploadRocData() {
        //check if data is uploaded
        if (useExample) {
            return handleRocTestFileUpload();
        }

        if (dataFile.getSize() == 0) {
            DataUtils.updateMsg("Error", "File is empty");
            return null;
        }

        if (sb.doLogin(dataType, "roc", false, false)) {
            RConnection RC = sb.getRConnection();
            String fileName = DataUtils.uploadFile(dataFile, sb.getCurrentUser().getHomeDir(), null, ab.isOnPublicServer());
            if (RDataUtils.readTextData(RC, fileName, dataFormat, "disc")) {
                sb.setDataUploaded();
                return "Data check";
            } else {
                DataUtils.updateMsg("Error:", RDataUtils.getErrMsg(RC));
                return null;
            }
        }
        return null;
    }

    public String handleRocTestFileUpload() {
        if (!sb.doLogin("conc", "roc", false, false)) {
            return null;
        }
        RConnection RC = sb.getRConnection();
        if (dataOpt.equals("data1")) {
            RDataUtils.readTextData(RC, ab.getResourceByAPI("plasma_nmr.csv"), "rowu", "disc");
        } else {
            RDataUtils.readTextData(RC, ab.getResourceByAPI("plasma_nmr_new.csv"), "rowu", "disc");
        }
        sb.setDataUploaded();
        return "Data check";
    }


    /*
     * Handle mzTab file examples (containing csv or txt files)
     */
    private UploadedFile mzTabFile;

    public UploadedFile getMzTabFile() {
        return mzTabFile;
    }

    public void setMzTabFile(UploadedFile mzTabFile) {
        this.mzTabFile = mzTabFile;
    }

    private String identifier = "name";

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String handleMzTabUpload() {

        if (mzTabFile.getSize() == 0) {
            DataUtils.updateMsg("Error", "File is empty!");
            return null;
        }

        dataType = "mztab";
        if (sb.doLogin(dataType, "stat", false, false)) {
            RConnection RC = sb.getRConnection();
            String fileName = DataUtils.uploadFile(mzTabFile, sb.getCurrentUser().getHomeDir(), null, ab.isOnPublicServer());

            if (RDataUtils.readMzTabData(RC, fileName, identifier)) {
                sb.setDataUploaded();
                return "Data check";
            } else {
                DataUtils.updateMsg("Error:", RDataUtils.getErrMsg(RC));
                return null;
            }
        }
        return null;
    }

    /*
     * Handle Metabolomics Workbench datasets
     */
    private String nmdrStudyId = "ST001301";

    public String getNmdrStudyId() {
        return nmdrStudyId;
    }

    public void setNmdrStudyId(String nmdrStudyId) {
        this.nmdrStudyId = nmdrStudyId;
    }

    public String handleMetWorkbenchData(String module) {

        if (!nmdrStudyId.startsWith("ST")) {
            DataUtils.updateMsg("Error", "Invalid Study ID!");
            return null;
        }

        if (module.equals("stat")) {
            if (sb.doLogin("conc", "stat", false, false)) {
                try {
                    RConnection RC = sb.getRConnection();

                    if (RDataUtils.getMetabolomicsWorkbenchData(RC, nmdrStudyId)) {
                        if (RDataUtils.readMetabolomicsWorkbenchData(RC, nmdrStudyId, "rowu", "disc")) {
                            sb.setDataUploaded();
                            return "Data check";
                        } else {
                            String err = RDataUtils.getErrMsg(RC);
                            DataUtils.updateMsg("Error", "Failed to read in the txt file." + err);
                            return null;
                        }
                    } else {
                        String err = RDataUtils.getErrMsg(RC);
                        DataUtils.updateMsg("Error", "Failed to retrieve study from Metabolomics Workbench!" + err);
                        return null;
                    }
                } catch (Exception e) {
                    //e.printStackTrace();
                    LOGGER.error("handleMetWorkbenchData-stat", e);
                    return null;
                }
            }
        } else if (module.equals("roc")) {
            if (sb.doLogin("conc", "roc", false, false)) {
                try {
                    RConnection RC = sb.getRConnection();

                    if (RDataUtils.getMetabolomicsWorkbenchData(RC, nmdrStudyId)) {
                        if (RDataUtils.readMetabolomicsWorkbenchData(RC, nmdrStudyId, "rowu", "disc")) {
                            sb.setDataUploaded();
                            return "Data check";
                        } else {
                            String err = RDataUtils.getErrMsg(RC);
                            DataUtils.updateMsg("Error", "Failed to read in the txt file." + err);
                            return null;
                        }
                    } else {
                        String err = RDataUtils.getErrMsg(RC);
                        DataUtils.updateMsg("Error", "Failed to retrieve study from Metabolomics Workbench!" + err);
                        return null;
                    }
                } catch (Exception e) {
                    //e.printStackTrace();
                    LOGGER.error("handleMetWorkbenchData-roc", e);
                    return null;
                }
            }
        }

        DataUtils.updateMsg("Error", "Log in failed. Please check errors in your R codes or the Rserve permission setting!");

        return null;
    }

    /*
     * Handle R Script Upload
     */

    // Declare the UploadedFile instance variable
    private UploadedFile rscriptfile;  // Define the property
    private UploadedFile undefineddatafile;
    private String analtype;

	public String getAnaltype() {
		return this.analtype;
	}

	public void setAnaltype(String analtype) {
		this.analtype = analtype;
	}


	public UploadedFile getRscriptfile() {
		return this.rscriptfile;
	}

	public void setRscriptfile(UploadedFile rscriptfile) {
		this.rscriptfile = rscriptfile;
	}

	public UploadedFile getUndefineddatafile() {
		return this.undefineddatafile;
	}

	public void setUndefineddatafile(UploadedFile undefineddatafile) {
		this.undefineddatafile = undefineddatafile;
	}



    public String handleRScriptUpload() {
        if (rscriptfile == null) {
            DataUtils.updateMsg("Error", "R script file is empty");
            return null;
        }

        if (undefineddatafile == null) {
            DataUtils.updateMsg("Error", "The data file is empty");
            return null;
        }

        try {
            // Compile R utils because this is always the first step
            // but we are not ready to do the Login yet
            if (sb.doPartialLogin()) {
                LOGGER.error(sb.getCurrentUser());
                String fileName = DataUtils.uploadFile(rscriptfile, sb.getCurrentUser().getHomeDir(), null, ab.isOnPublicServer());
                LOGGER.error(fileName);

                if (fileName == null) {
                    return null;
                }

                RConnection RC = sb.getRConnection();

                // Call the function that will run the RScript to proccess RHistory
                RList rconfiguration = RDataUtils.processConfigFile(RC, fileName);
                LOGGER.error(rconfiguration);

            }
            
            setDataType("conc");
            setAnaltype("stat");
            setDataFile(undefineddatafile);
            sb.initNaviTree(analtype);
            String result = handleFileUpload();

            return result;

            // Call the R function that reads the R script
        } catch (Exception e) {
            LOGGER.error("handleRScriptUpload", e);
            return null;
        }
        
    }


}