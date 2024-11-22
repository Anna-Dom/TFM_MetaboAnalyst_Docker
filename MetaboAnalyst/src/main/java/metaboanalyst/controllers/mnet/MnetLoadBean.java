/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package metaboanalyst.controllers.mnet;

import java.io.Serializable;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import metaboanalyst.controllers.ApplicationBean1;
import metaboanalyst.controllers.SessionBean1;
import metaboanalyst.rwrappers.RDataUtils;
import metaboanalyst.rwrappers.RIntegUtils;
import metaboanalyst.utils.DataUtils;
import org.primefaces.model.file.UploadedFile;
import org.rosuda.REngine.Rserve.RConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This bean is for tools of various ID mapping
 *
 * @author jianguox
 */
@RequestScoped
@Named("mnetLoader")
public class MnetLoadBean implements Serializable {

    private final ApplicationBean1 ab = (ApplicationBean1) DataUtils.findBean("applicationBean1");
    private final SessionBean1 sb = (SessionBean1) DataUtils.findBean("sessionBean1");
    private static final Logger LOGGER = LogManager.getLogger(MnetLoadBean.class);
    
    /*
    private String uploadListOpt = "genemetabo";

    public String getUploadListOpt() {
        return uploadListOpt;
    }

    public void setUploadListOpt(String uploadListOpt) {
        this.uploadListOpt = uploadListOpt;
    }
    **/
    private String geneList;

    public String getGeneList() {
        return geneList;
    }

    public void setGeneList(String geneList) {
        this.geneList = geneList;
    }

    private String dataFormat;

    public String getDataFormat() {
        return dataFormat;
    }

    public void setDataFormat(String dataFormat) {
        this.dataFormat = dataFormat;
    }

    private String clsOpt = "disc";

    public String getClsOpt() {
        return clsOpt;
    }

    public void setClsOpt(String clsOpt) {
        this.clsOpt = clsOpt;
    }

    private UploadedFile csvFile;

    public UploadedFile getCsvFile() {
        return csvFile;
    }

    public void setCsvFile(UploadedFile csvFile) {
        this.csvFile = csvFile;
    }

    private boolean useDspcDataExample;

    public boolean isUseDspcDataExample() {
        return useDspcDataExample;
    }

    public void setUseDspcDataExample(boolean useDspcDataExample) {
        this.useDspcDataExample = useDspcDataExample;
    }

    private String processDspcQeaData(String fileName, String cmpdType, String dataFormat, String lblType) {
        RConnection RC = sb.getRConnection();
        if (RDataUtils.readTextData(RC, fileName, dataFormat, lblType)) {
            if (cmpdType.equals("pklist")) {
                sb.initNaviTree("dspc-peak");
            } else {
                sb.initNaviTree("dspc");
            }
            return "Data check";
        } else {
            String err = RDataUtils.getErrMsg(sb.getRConnection());
            DataUtils.updateMsg("Error", "Failed to read in the CSV file." + err);
            return null;
        }
    }

    private String cmpdList;

    public String getCmpdList() {
        return cmpdList;
    }

    public void setCmpdList(String cmpdList) {
        this.cmpdList = cmpdList;
    }

    private boolean useExample = false;

    public boolean isUseExample() {
        return useExample;
    }

    public void setUseExample(boolean useExample) {
        this.useExample = useExample;
    }

    private String exampleInputList = "metabogene";

    public String getExampleInputList() {
        return exampleInputList;
    }

    public void setExampleInputList(String exampleInputList) {
        this.exampleInputList = exampleInputList;
    }

    private String netOrg = "hsa"; //only hsa is supported

    public String getNetOrg() {
        return netOrg;
    }

    public void setNetOrg(String netOrg) {
        this.netOrg = netOrg;
    }

    //this is for example data
    public void updateListArea() {
        if (exampleInputList.equals("metabogene")) {
            cmpdList = DataUtils.readTextFile(ab.getInternalData("network_cmpds.txt"));
            sb.setCmpdIDType("kegg");
            geneList = DataUtils.readTextFile(ab.getInternalData("network_genes.txt"));
            geneIDType = "entrez";
            useExample = true;
        } else if ((exampleInputList.equals("metabometag"))) {
            cmpdList = DataUtils.readTextFile(ab.getInternalData("network_cpd_name_example.txt"));
            sb.setCmpdIDType("hmdb");
            geneList = DataUtils.readTextFile(ab.getInternalData("network_kos_example.txt"));
            geneIDType = "kos";
            useExample = true;
        }
    }

    private String geneIDType = "NA";

    public String getGeneIDType() {
        return geneIDType;
    }

    public void setGeneIDType(String geneIDType) {
        this.geneIDType = geneIDType;
    }

    /*
        private String cmpdIDType = "NA";
    public String getCmpdIDType() {
        return cmpdIDType;
    }

    public void setCmpdIDType(String cmpdIDType) {
        this.cmpdIDType = cmpdIDType;
    }
     */
    private boolean loggedIn = false;
    private boolean cmpdMapped = false;

    public void handleCmpdListUpload() {

        cmpdList = cmpdList.trim();
        RConnection RC = sb.getRConnection();
        int res = RIntegUtils.performCmpdMapping(RC, cmpdList, netOrg, sb.getCmpdIDType());

        String info[] = RDataUtils.getNameCheckMsgs(RC);
        // int state = Integer.parseInt(info[0]);
        String msg = info[1];

        if (res == 1) {
            DataUtils.updateMsg("OK", msg);
            cmpdMapped = true;
        } else {
            DataUtils.updateMsg("Error", msg);
        }
    }

    private boolean geneMapped = false;

    public void handleGeneListUpload() {

        geneList = geneList.trim();
        RConnection RC = sb.getRConnection();
        int res = RIntegUtils.performGeneMapping(RC, geneList, netOrg, geneIDType);
        if (res == 1) {
            DataUtils.updateMsg("OK", RDataUtils.getCurrentMsg(RC));
            geneMapped = true;
        } else {
            DataUtils.updateMsg("Error", RDataUtils.getErrMsg(RC));
        }
    }

    public String integrityCheck() {

        if (!loggedIn) {
            if (!sb.doLogin("conc", "network", false, false)) {
                DataUtils.updateMsg("Error", "Log in failed. Please check errors in your R codes or the Rserve permission setting!");
                return null;
            } else {
                loggedIn = true;
            }
        }

        RDataUtils.setOrganism(sb.getRConnection(), netOrg);

       // if (uploadListOpt.equals("genemetabo") || uploadListOpt.equals("genes")) {
            if (!(geneList == null | geneList.trim().length() == 0)) {
                if (geneIDType.equalsIgnoreCase("na")) {
                    DataUtils.updateMsg("Error", "Please specify gene ID type!");
                    return null;
                }
                
                handleGeneListUpload();
            }
       // }

        //if (uploadListOpt.equals("genemetabo") || uploadListOpt.equals("metabo")) {
            if (!(cmpdList == null | cmpdList.trim().length() == 0)) {
                if (sb.getCmpdIDType().equalsIgnoreCase("na")) {
                    DataUtils.updateMsg("Error", "Please specify compound ID type!");
                    return null;
                }
                handleCmpdListUpload();
            }
        //}

        if (!geneMapped && !cmpdMapped) {
            DataUtils.updateMsg("Error", "Please enter valid input!");
            return null;
        }
        sb.initNaviTree("network");
        sb.setDataUploaded();
        return "MnetID map";
    }

    private String mnetTestDataOpt = "aminoacids";

    public String getMnetTestDataOpt() {
        return mnetTestDataOpt;
    }

    public void setMnetTestDataOpt(String mnetTestDataOpt) {
        this.mnetTestDataOpt = mnetTestDataOpt;
    }

    public String handleMnetDataUpload() {
        if (sb.getCmpdIDType().equals("na")) {
            DataUtils.updateMsg("Error", "Please specify the ID type for your data input!");
            return null;
        }
        if (!sb.doLogin("conc", "network", false, false)) {
            DataUtils.updateMsg("Error", "Failed to log in!");
            return null;
        }
        try {
            if (csvFile==null || csvFile.getSize() == 0) {
                DataUtils.updateMsg("Error", "File is empty!");
                return null;
            }
            String fileName = DataUtils.getJustFileName(csvFile.getFileName());
            DataUtils.uploadFile(csvFile, sb.getCurrentUser().getHomeDir(), null, ab.isOnPublicServer());
            RDataUtils.setOrganism(sb.getRConnection(), netOrg);
            sb.setDataUploaded();
            return processDspcQeaData(fileName, sb.getCmpdIDType(), dataFormat, clsOpt);
        } catch (Exception e) {
            //e.printStackTrace();
             LOGGER.error("handleMnetDataUpload", e);
            return null;
        }
    }

    public String performExampleDspc() {
        String fileName = ab.getResourceByAPI("aminoacids.csv");
        setDataFormat("rowu");
        sb.setCmpdIDType("name");
        if (!sb.doLogin("conc", "network", false, false)) {
            DataUtils.updateMsg("Error", "Log in failed. Please check errors in your R codes or the Rserve permission setting!");
            return null;
        }
        RDataUtils.setOrganism(sb.getRConnection(), netOrg);
        sb.setDataUploaded();
        return processDspcQeaData(fileName, sb.getCmpdIDType(), dataFormat, "disc");
    }

    private String nmdrStudyId = "ST001301";

    public String getNmdrStudyId() {
        return nmdrStudyId;
    }

    public void setNmdrStudyId(String nmdrStudyId) {
        this.nmdrStudyId = nmdrStudyId;
    }

    public String handleDSPCMetWorkbenchData() {

        if (!nmdrStudyId.startsWith("ST")) {
            DataUtils.updateMsg("Error", "Invalid Study ID!");
            return null;
        }

        if (sb.doLogin("conc", "network", false, false)) {
            try {
                RConnection RC = sb.getRConnection();

                if (RDataUtils.getMetabolomicsWorkbenchData(RC, nmdrStudyId)) {
                    if (RDataUtils.readMetabolomicsWorkbenchData(RC, nmdrStudyId, "rowu", "disc")) {
                        sb.setDataUploaded();
                        RDataUtils.setOrganism(sb.getRConnection(), netOrg);
                        sb.setCmpdIDType("name");
                        sb.initNaviTree("dspc");
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
                 LOGGER.error("handleDSPCMetWorkbenchData", e);
            }
        }

        DataUtils.updateMsg("Error", "Log in failed. Please check errors in your R codes or the Rserve permission setting!");
        return null;
    }

}
