/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package metaboanalyst.controllers.enrich;

import java.io.Serializable;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import metaboanalyst.controllers.ApplicationBean1;
import metaboanalyst.controllers.SessionBean1;
import metaboanalyst.rwrappers.RDataUtils;
import metaboanalyst.rwrappers.SearchUtils;
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
@Named("enrichLoader")
public class EnrichUploadBean implements Serializable {

    private final ApplicationBean1 ab = (ApplicationBean1) DataUtils.findBean("applicationBean1");
    private final SessionBean1 sb = (SessionBean1) DataUtils.findBean("sessionBean1");
    private static final Logger LOGGER = LogManager.getLogger(EnrichUploadBean.class);

    /*
     * ORA analysis
     */
    private String msetOraList;

    public String getMsetOraList() {
        return msetOraList;
    }

    public void setMsetOraList(String msetOraList) {
        this.msetOraList = msetOraList;
    }

    private String featType = "none";

    public String getFeatType() {
        return featType;
    }

    public void setFeatType(String featType) {
        this.featType = featType;
    }

    private String exampleType = "none";

    public String getExampleType() {
        return exampleType;
    }

    public void setExampleType(String exampleType) {
        this.exampleType = exampleType;
    }

    public String handleOraListUpload() {

        if (!sb.doLogin("conc", "msetora", false, false)) {
            DataUtils.updateMsg("Error", "Log in failed. Please check errors in your R codes or the Rserve permission setting!");
            return null;
        }

        if (msetOraList == null || msetOraList.trim().length() == 0) {
            DataUtils.updateMsg("Error", "Error: the input is empty!");
            return null;
        }

        if (featType.equals("none")) {
            DataUtils.updateMsg("Error", "Please specify Feature Type!");
            return null;
        }

        String[] qVec = DataUtils.getQueryNames(msetOraList, null);
        RDataUtils.setMapData(sb.getRConnection(), qVec);

        if (featType.equals("lipid")) {
            SearchUtils.crossReferenceExactLipid(sb.getRConnection(), sb.getCmpdIDType());
        } else {
            SearchUtils.crossReferenceExact(sb.getRConnection(), sb.getCmpdIDType());
        }

        sb.setDataUploaded();
        return "Name check";
    }

    public void updateOraArea() {
        switch (exampleType) {
            case "met":
                sb.setCmpdIDType("name");
                featType = "met";
                msetOraList = "Acetoacetic acid\nBeta-Alanine\nCreatine\nDimethylglycine\nFumaric acid\nGlycine\nHomocysteine\nL-Cysteine\n"
                        + "L-Isolucine\nL-Phenylalanine\nL-Serine\nL-Threonine\nL-Tyrosine\nL-Valine\nPhenylpyruvic acid\nPropionic acid\nPyruvic acid\nSarcosine";
                break;
            case "lipid":
                sb.setCmpdIDType("name");
                featType = "lipid";
                msetOraList = "CerP(d18:1/26:1)\nDG(18:0/15:0)\nDG(18:2/19:0)\nLysoPC(10:0)\nLysoPC(17:0)\nLysoPE(22:2)\nPA(18:1/18:0)\nPA(18:1/21:0)\n"
                        + "PA(20:4/20:0)\nPA(22:2/24:0)\nPA(22:6/18:1)\nPC(20:5/18:2)\nPC(P-18:0/18:1)\nPE(18:1/22:1)\nPE(18:2/16:0)\nPE(18:2/21:0)\n"
                        + "PE(18:2/22:1)\nPE(20:2/18:2)\nPE(20:3/20:2)\nPE(20:3/22:0)\nPE(20:4/18:0)\nPE(20:4/20:0)\nPE(P-16:0/18:0)\nPE(P-18:0/13:0)\n"
                        + "PE(P-18:0/17:0)\nPE(P-18:0/20:4)\nPE(P-18:0/20:5)\nPE(P-18:0/22:1)\nPE(P-20:0/22:6)\nPG(18:0/16:0)\nPG(18:1/18:0)\nPG(22:6/20:1)\n"
                        + "PI(18:2/18:1)\nPI(22:2/16:0)\nPS(18:0/21:0)\nPS(18:1/20:3)\nPS(18:1/22:0)\nPS(18:1/24:1)\nPS(18:2/22:1)\nPS(20:1/18:0)\nPS(20:3/21:0)\n"
                        + "PS(22:6/17:2)\nPS(22:6/18:0)\nSQDG(18:0/12:0)";
                break;
            default:
                featType = "none";
                msetOraList = "";
                break;
        }
    }

    /*
     * SSP analysis
     */
    private String msetSspData = "";

    public String getMsetSspData() {
        return msetSspData;
    }

    public void setMsetSspData(String msetSspData) {
        this.msetSspData = msetSspData;
    }

    private boolean useMsetSspExample = false;

    public boolean isUseMsetSspExample() {
        return useMsetSspExample;
    }

    public void setUseMsetSspExample(boolean useMsetSspExample) {
        this.useMsetSspExample = useMsetSspExample;
    }

    private String biofluidType;

    public String getBiofluidType() {
        return biofluidType;
    }

    public void setBiofluidType(String biofluidType) {
        this.biofluidType = biofluidType;
    }

    public void updateSspArea() {
        if (useMsetSspExample) {
            msetSspData = "L-Isolecine	0.34\nFumaric acid	0.47\nAcetone	0.58\nSuccinic acid	9.4\n1-Methylhistidine	9.6\n"
                    + "L-Asparagine	19.62\n3-Methylhistidine	9.7\nL-Threonine	93.19\nCreatine	720\ncis-Aconitic acid	14.39\n"
                    + "L-Tryptophan	35.78\nL-Carnitine	16.01\nL-Serine	17.32\nL-Tyrosine	67.51\nL-Alanine	219.02\n"
                    + "L-Fucose	20.37\nD-Glucose	23.92\nPyroglutamic acid	26.38\nFormic acid	26.72\nIndoxyl sulfate	34.21\n"
                    + "Dimethylamine	38.28\nEthanolamine	39.29\nGlycolic acid	41.39\nL-Glutamine	52.99\nL-Histidine	55.95\n"
                    + "Trigonelline	57.4\n3-Aminoisobutanoic acid	89.76\nTaurine	116\nGlycine	123.52\nTrimethylamine N-oxide	128.04\n"
                    + "Citric acid	225.31\nHippuric acid	278.53";
        } else {
            msetSspData = "";
        }
    }

    public String handleSspDataUpload() {
        if (!sb.doLogin("conc", "msetssp", false, false)) {
            DataUtils.updateMsg("Error", "Log in failed. Please check errors in your R codes or the Rserve permission setting!");
            return null;
        }
        if (msetSspData != null && msetSspData.trim().length() > 0) {
            if (!RDataUtils.setSample(sb.getRConnection(), msetSspData, biofluidType, null)) {
                return null;
            }
            SearchUtils.crossReferenceExact(sb.getRConnection(), sb.getCmpdIDType());
            sb.setDataUploaded();
            sb.initNaviTree("enrich-ssp");
            return "Name check";
        }
        return null;
    }

    /*
     * QEA analysis
     */
    private UploadedFile csvFile;

    public UploadedFile getCsvFile() {
        return csvFile;
    }

    public void setCsvFile(UploadedFile csvFile) {
        this.csvFile = csvFile;
    }

    private String qeaClsOpt = "disc";

    public String getQeaClsOpt() {
        return qeaClsOpt;
    }

    public void setQeaClsOpt(String qeaClsOpt) {
        this.qeaClsOpt = qeaClsOpt;
    }

    private String dataFormat;

    public String getDataFormat() {
        return dataFormat;
    }

    public void setDataFormat(String dataFormat) {
        this.dataFormat = dataFormat;
    }

    private String qeaTestDataOpt = "msetDis";

    public String getQeaTestDataOpt() {
        return qeaTestDataOpt;
    }

    public void setQeaTestDataOpt(String qeaTestDataOpt) {
        this.qeaTestDataOpt = qeaTestDataOpt;
    }

    public String handleQeaDataUpload() {
        try {
            if (!sb.doLogin("conc", "msetqea", qeaClsOpt.equals("cont"), false)) {
                DataUtils.updateMsg("Error", "Log in failed. Please check errors in your R codes or the Rserve permission setting!");
                return null;
            }
            String fileName = DataUtils.uploadFile(csvFile, sb.getCurrentUser().getHomeDir(), null, ab.isOnPublicServer());
            sb.setDataUploaded();
            //sb.setCmpdIDType(cmpdIDType);
            sb.initNaviTree("enrich-qea");
            sb.setFeatType(featType);
            return processMsetQeaData(fileName, qeaClsOpt, dataFormat);
        } catch (Exception e) {
            //e.printStackTrace();
             LOGGER.error("handleQesDataUpload", e);
            return null;
        }
    }

    public String msetQeaTestBn_action() {
        // TODO: Process the action. Return value is a navigation
        // case name where null will return to the same page.
        String lblType, clsType, fileName, fileType;
        String useCachexia = "FALSE";
        switch (qeaTestDataOpt) {
            case "msetDis":
                lblType = "name";
                clsType = "disc";
                fileName = ab.getResourceByAPI("human_cachexia.csv");
                fileType = "rowu";
                useCachexia = "TRUE";
                break;
            case "conReq":
                lblType = "pubchem";
                clsType = "cont";
                fileName = ab.getResourceByAPI("cachexia_continuous.csv");
                fileType = "rowu";
                useCachexia = "TRUE";
                break;
            default:
                lblType = "name";
                clsType = "disc";
                fileName = ab.getResourceByAPI("multiple_sclerosis_test.csv");
                featType = "lipid";
                fileType = "colu";
                break;
        }

        if (!sb.doLogin("conc", "msetqea", clsType.equals("cont"), false)) {
            DataUtils.updateMsg("Error", "Log in failed. Please check errors in your R codes or the Rserve permission setting!");
            return null;
        }
        RDataUtils.setCachexiaTestSet(sb.getRConnection(), useCachexia);
        sb.setDataUploaded();
        sb.setCmpdIDType(lblType);
        sb.setFeatType(featType);

        return processMsetQeaData(fileName, clsType, fileType);
    }

    public String processMsetQeaData(String fileName, String clsType, String dataFormat) {
        RConnection RC = sb.getRConnection();
        if (RDataUtils.readTextData(RC, fileName, dataFormat, clsType)) {
            //double check for multiple class, this will be issue for large sample
            if (RDataUtils.getGroupNumber(RC) > 2) {
                DataUtils.updateMsg("Error", "Enrichment analysis for multiple-group data is not well-defined. Please subset your data to two groups to proceed!");
                return null;
            }
            sb.initNaviTree("enrich-qea");
            return "Data check";
        } else {
            String err = RDataUtils.getErrMsg(sb.getRConnection());
            DataUtils.updateMsg("Error", "Failed to read in the CSV file." + err);
            return null;
        }
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

    public String enrichQea_workbench() {
        if (sb.doLogin("conc", "msetqea", false, false)) {
            try {
                RConnection RC = sb.getRConnection();

                if (RDataUtils.getMetabolomicsWorkbenchData(RC, nmdrStudyId)) {
                    if (RDataUtils.readMetabolomicsWorkbenchData(RC, nmdrStudyId, "rowu", "disc")) {
                        sb.setDataUploaded();
                        sb.setCmpdIDType("name");

                        if (RDataUtils.getGroupNumber(RC) > 2) {
                            DataUtils.updateMsg("Error", "Enrichment analysis for multiple-group data is not well-defined. Please subset your data to two groups to proceed!");
                            return null;
                        }
                        sb.initNaviTree("enrich-qea");
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
               // e.printStackTrace();
                LOGGER.error("enrichQea_workbench", e);
            }
        }
        return null;
    }
}
