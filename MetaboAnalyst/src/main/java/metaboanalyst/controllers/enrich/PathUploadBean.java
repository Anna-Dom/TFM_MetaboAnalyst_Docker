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
@Named("pathLoader")
public class PathUploadBean implements Serializable {

    private final ApplicationBean1 ab = (ApplicationBean1) DataUtils.findBean("applicationBean1");
    private final SessionBean1 sb = (SessionBean1) DataUtils.findBean("sessionBean1");
    private static final Logger LOGGER = LogManager.getLogger(PathUploadBean.class);

    private boolean usePathListExample;

    public boolean isUsePathListExample() {
        return usePathListExample;
    }

    public void setUsePathListExample(boolean usePathListExample) {
        this.usePathListExample = usePathListExample;
    }

    private String pathOraList;

    public String getPathOraList() {
        return pathOraList;
    }

    public void setPathOraList(String pathOraList) {
        this.pathOraList = pathOraList;
    }

    public void updatePathListArea() {
        if (usePathListExample) {
            pathOraList = "Acetoacetic acid\nBeta-Alanine\nCreatine\nDimethylglycine\nFumaric acid\nGlycine\nHomocysteine\nL-Cysteine\n"
                    + "L-Isolucine\nL-Phenylalanine\nL-Serine\nL-Threonine\nL-Tyrosine\nL-Valine\nPhenylpyruvic acid\nPropionic acid\nPyruvic acid\nSarcosine";
            sb.setCmpdIDType("name");
        } else {
            pathOraList = "";
            sb.setCmpdIDType("na");
        }
    }

    public String handlePathListUpload() {
        if (!sb.doLogin("conc", "pathora", false, false)) {
            DataUtils.updateMsg("Error", "Log in failed. Please check errors in your R codes or the Rserve permission setting!");
            return null;
        }
        if (pathOraList == null || pathOraList.trim().length() == 0) {
            DataUtils.updateMsg("Error", "Empty input!");
            return null;
        } else {
            if (sb.getCmpdIDType().equals("na")) {
                DataUtils.updateMsg("Error", "Please specify the ID type for your data input!");
                return null;
            } else {
                RConnection RC = sb.getRConnection();
                String[] qVec = DataUtils.getQueryNames(pathOraList, null);
                RDataUtils.setMapData(RC, qVec);
                SearchUtils.crossReferenceExact(RC, sb.getCmpdIDType());
                sb.setDataUploaded();
                return "Name check";
            }
        }
    }

    private UploadedFile csvFile;

    public UploadedFile getCsvFile() {
        return csvFile;
    }

    public void setCsvFile(UploadedFile csvFile) {
        this.csvFile = csvFile;
    }

    private String clsOpt = "disc";

    public String getClsOpt() {
        return clsOpt;
    }

    private String dataFormat;

    public String getDataFormat() {
        return dataFormat;
    }

    public void setDataFormat(String dataFormat) {
        this.dataFormat = dataFormat;
    }

    public void setClsOpt(String clsOpt) {
        this.clsOpt = clsOpt;
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

    public String pathQea_workbench() {
        if (sb.doLogin("conc", "pathqea", false, false)) {
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
                        sb.initNaviTree("pathway-qea");
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
                LOGGER.error("pathQea_workbench", e);
            }
        }
        return null;
    }

    public String pathQeaExampleBn_action() {
        if (!sb.doLogin("conc", "pathqea", false, false)) {
            DataUtils.updateMsg("Error", "Failed to log in!");
            return null;
        }
        sb.setDataUploaded();
        sb.setCmpdIDType("name");
        return processPathQeaData(ab.getResourceByAPI("human_cachexia.csv"), "rowu", "disc");
    }

    public String pathQeaBn_action() {

        if (sb.getCmpdIDType().equalsIgnoreCase("na")) {
            DataUtils.updateMsg("Error", "Please specify the ID type for your data input!");
            return null;
        }

        if (!sb.doLogin("conc", "pathqea", clsOpt.equals("cont"), false)) {
            DataUtils.updateMsg("Error", "Failed to log in!");
            return null;
        }
        try {

            if (csvFile == null) {
                DataUtils.updateMsg("Error", "Please upload your file!");
                return null;
            }

            if (csvFile.getSize() == 0) {
                DataUtils.updateMsg("Error", "File is empty!");
                return null;
            }

            String fileName = DataUtils.getJustFileName(csvFile.getFileName());
            DataUtils.uploadFile(csvFile, sb.getCurrentUser().getHomeDir(), null, ab.isOnPublicServer());
            sb.setDataUploaded();
            //sb.setCmpdIDType(qeaCmpdIDType);
            return processPathQeaData(fileName, dataFormat, clsOpt);
        } catch (Exception e) {
            //e.printStackTrace();
            LOGGER.error("pathQeaBn_action", e);
            return null;
        }

    }

    private String processPathQeaData(String fileName, String dataFormat, String lblType) {
        RConnection RC = sb.getRConnection();
        if (RDataUtils.readTextData(RC, fileName, dataFormat, lblType)) {
            if (RDataUtils.getGroupNumber(RC) > 2) {
                DataUtils.updateMsg("Error", "Enrichment analysis for multiple-group data is "
                        + "not well-defined. Please subset your data to two groups to proceed!");
                return null;
            }
            //SearchUtils.crossReferenceExact(sb.getRConnection(), cmpdType);
            sb.initNaviTree("pathway-qea");
            return "Data check";
        } else {
            String err = RDataUtils.getErrMsg(sb.getRConnection());
            DataUtils.updateMsg("Error", "Failed to read in the CSV file." + err);
            return null;
        }
    }
}
