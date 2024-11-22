/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
//clean memory arraylist uploaded files...
//explicitly assign arraylist to null
package metaboanalyst.spectra;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import javax.annotation.PreDestroy;
import javax.inject.Named;
import metaboanalyst.utils.DataUtils;
import org.primefaces.model.file.UploadedFile;
import org.primefaces.event.FileUploadEvent;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.view.ViewScoped;
import metaboanalyst.controllers.ApplicationBean1;
import metaboanalyst.controllers.ResourceSemaphore;
import metaboanalyst.controllers.SessionBean1;
import metaboanalyst.project.ProjectBean;
import metaboanalyst.rwrappers.RDataUtils;
import metaboanalyst.rwrappers.RSpectraUtils;
import org.primefaces.PrimeFaces;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.Rserve.RConnection;

/**
 *
 * @author qiang
 */
@ViewScoped
@Named("specLoader")
public class SpectraUploadBean implements Serializable {

    private final ApplicationBean1 ab = (ApplicationBean1) DataUtils.findBean("applicationBean1");
    private final SessionBean1 sb = (SessionBean1) DataUtils.findBean("sessionBean1");
    private final SpectraProcessBean pkb = (SpectraProcessBean) DataUtils.findBean("spectraProcessor");
    private final SpectraControlBean pcb = (SpectraControlBean) DataUtils.findBean("spectraController");

    private String selectedExample = "fastdata";

    public String getSelectedExample() {
        return selectedExample;
    }

    public void setSelectedExample(String selectedExample) {
        this.selectedExample = selectedExample;
    }

    private List<String> uploadedFileNames = new ArrayList<>();

    public List<String> getUploadedFileNames() {
        return uploadedFileNames;
    }

    public void setUploadedFileNames(List<String> uploadedFileNames) {
        this.uploadedFileNames = uploadedFileNames;
    }

    private String[] fileNmsFromMeta;
    private String[] classNmsFromMeta;
    private String metaName;
    private HashMap<String, String> fileAndClass = new HashMap();

    private boolean zeroMeta = false;

    private int upLoadingCount = 0;

    private boolean containsMeta = false;

    public boolean isContainsMeta() {
        return containsMeta;
    }

    public void setContainsMeta(boolean containsMeta) {
        this.containsMeta = containsMeta;
    }

    public void updateText() {

        if (pkb.getMsgList().isEmpty()) {
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null, new FacesMessage("Successful", "File upload is complete! Click the <b>Proceed</b> button to the next page."));
        } else {
            String msg = "<table face=\"times\" size = \"3\">";
            for (int i = 0; i < pkb.getMsgList().size(); i++) {
                msg = msg + "<tr><td align=\"left\">" + pkb.getMsgList().get(i) + "</td></tr>";
            }
            msg = msg + "</table>";
            if (!pkb.getMsgList().isEmpty()) {
                pkb.setErrMsgText(msg);
            }
            DataUtils.updateMsg("Error", pkb.getErrMsgText());
            // System.out.println("console.log(\"" + msg + "\")");
            // PrimeFaces.current().executeScript("console.log(\"" + msg + "\")");
        }
    }

    private final int ACQUIRE_TIMEOUT = 10;

    public void handleFileUpload(FileUploadEvent event) {

        if (ab.isOnDevServer()) {
            DataUtils.updateMsg("Error", "Wrong server instance");
            return;
        }

        ResourceSemaphore resourceSemaphore = (ResourceSemaphore) DataUtils.findBean("semaphore");
        Semaphore semaphore = resourceSemaphore.getUploadSemaphore();

        try {
            if (semaphore.tryAcquire(ACQUIRE_TIMEOUT, TimeUnit.SECONDS)) {
                // can upload
                UploadedFile file = event.getFile();

                String fileName = file.getFileName();
                if (file.getSize() == 0) {
                    pkb.getMsgList().add("File is empty + " + fileName);
                    DataUtils.updateMsg("Error", "File is empty + " + fileName);
                    return;
                }

                //max 200 MB per zip file
                if (file.getSize() > ab.getMAX_SPEC_SIZE()) {
                    pkb.getMsgList().add(fileName + " is too large. Use MetaboAnalystR for processing high-resolution files (over 200 MB).");
                    DataUtils.updateMsg("Error", fileName + " is too large. Use MetaboAnalystR for processing high-resolution files (over 200 MB).");
                    return;
                }

                FacesMessage msg = new FacesMessage("Successful", fileName + " is uploaded.");
                FacesContext.getCurrentInstance().addMessage(null, msg);

                String homeDir = sb.getCurrentUser().getHomeDir();
                if (fileName.contains(".txt") || fileName.contains(".csv")) {
                    pkb.getMsgList().removeAll(pkb.getMsgList());
                    pkb.setErrMsgText("");
                    pcb.setMetaOk(false);

                    metaName = DataUtils.uploadFile(file, homeDir, null, false);
                    int rawOK = RDataUtils.readRawMeta(sb.getRConnection(), metaName);

                    if (rawOK == 0) {
                        DataUtils.updateMsg("Error", "There are errors in reading you meta-data file!");
                        return;
                    }

                    fileNmsFromMeta = RDataUtils.getRawFileNames(sb.getRConnection());
                    classNmsFromMeta = RDataUtils.getRawClassNames(sb.getRConnection());

                    //map fileName to Class
                    for (int i = 0; i < fileNmsFromMeta.length; i++) {
                        fileAndClass.put(fileNmsFromMeta[i], classNmsFromMeta[i]);
                    }

                    File destDir = new File(homeDir + File.separator + "upload");
                    if (!destDir.exists()) {
                        destDir.mkdir();
                    }
                    pcb.setMetaOk(true);
                    containsMeta = true;

                } else {
                    if (containsMeta) {
                        if (metaName.equals("")) {
                            pkb.getMsgList().add("Please make sure that meta-data file in the .txt format is provided!");
                        }
                        String trimmedFileNm = fileName.replace(".zip", "");
                        if (!Arrays.asList(fileNmsFromMeta).contains(trimmedFileNm)) {
                            pkb.getMsgList().add("'<b>" + trimmedFileNm + "</b>' is not found in Meta-data!");
                        }
                        String className = fileAndClass.get(trimmedFileNm);
                        File classFolder = new File(homeDir + File.separator + "upload" + File.separator + className);
                        if (!classFolder.exists()) {
                            classFolder.mkdir();
                        }
                        fileName = DataUtils.uploadFile(file, homeDir, null, false);

                        int res = DataUtils.unzipDataRaw(homeDir + File.separator + fileName, homeDir + File.separator + "upload" + File.separator + className);
                        switch (res) {
                            case -1:
                                pkb.getMsgList().add("<b>'" + DataUtils.getRawUploadErrorFile() + "'</b> must contain .mzXML, .mzML or .mzData files only.");
                            case 0:
                                pkb.getMsgList().add("Unable to unzip the files.");
                            case 1:
                                File zipToBeDeleted = new File(homeDir + File.separator + file.getFileName());
                                zipToBeDeleted.delete();
                                break;
                            default:
                                break;
                        }
                    } else {
                        fileName = DataUtils.uploadFile(file, homeDir, null, false);
                        int res = DataUtils.unzipDataRaw(homeDir + File.separator + fileName, homeDir + File.separator + "upload");
                        switch (res) {
                            case -1:
                                pkb.getMsgList().add("<b>'" + DataUtils.getRawUploadErrorFile() + "'</b> must contain .mzXML, .mzML or .mzData files only.");
                            case 0:
                                pkb.getMsgList().add("Unable to unzip the files.");
                            case 1:
                                File zipToBeDeleted = new File(homeDir + File.separator + file.getFileName());
                                zipToBeDeleted.delete();
                                break;
                            default:
                                break;
                        }
                    }

                    uploadedFileNames.add(fileName);
                }

                if (upLoadingCount == 0) {
                    zeroMeta = containsMeta;
                } else {
                    if (zeroMeta != containsMeta) {
                        pkb.getMsgList().add("Uplaoding with metadata or not must be consistent for mutiple uploading. Click 'Reset' to re-upalod!");
                        return;
                    }
                }
                upLoadingCount++;

                //return permission
                semaphore.release();

            } else {
                // code for timed-out
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "System busy!",
                                "Too many users are uploading spectra at this time. Please try again later!"));

            }
        } catch (InterruptedException e) {
            // code for timed-out
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error!",
                            "Too many users are uploading spectra at this time. Please try again later!"));

        }

    }

    public String uploadExampleSpectra() throws Exception {

        if (pcb.checkJobRunning()) {
            PrimeFaces.current().executeScript("PF('uploadSessionDialog').show()");
            return null;
        }

        if (selectedExample.equals("malaria")) {
            return uploadMalariaExample();
        } else {
            return uploadIBDExample();
        }

    }

    private String uploadIBDExample() throws REXPMismatchException {
        //should always fresh log in
        if (!sb.isRegisteredLogin()) {
            sb.doLogin("spec", "raw", false, false);
        } else {
            // Forbid users to upload example as a project
            DataUtils.showMessage(FacesMessage.SEVERITY_ERROR, "Sorry!",
                    "You are not allowed to upload this example as a project!");
            return "";
        }

        sb.setDataUploaded();
        pcb.setExecutExample(true);
        pkb.populateSpecBeans();
        pcb.setTotalNumberOfSamples(8);
        sb.setSaveEnabled(true);
        SpectraParamBean spb = (SpectraParamBean) DataUtils.findBean("spectraParamer");
        spb.setPolarity("negative");
        return "Spectra check";
    }

    private String uploadMalariaExample() throws REXPMismatchException {
        if (!sb.isRegisteredLogin()) {
            sb.doLogin("spec", "raw", false, false);
        }
        String datadir = "/home/glassfish/projects/MetaboMalariaRawData/upload";
        String homeDir = sb.getCurrentUser().getHomeDir();

        File srcDir = new File(datadir);

        if (srcDir.exists()) {
            //DataUtils.copyDir(datadir, homeDir + File.separator + "upload");
            try {
                Path symbolic = Paths.get(datadir);
                Path target = Paths.get(homeDir + File.separator + "upload");
                Files.createSymbolicLink(target, symbolic);
            } catch (IOException ex) {
                System.out.println("This data is not found in your local, will start a downloading!");
            }
        }

        File markerfile = new File(homeDir + File.separator + "upload/QC/QC_001.mzML");
        if (!markerfile.exists()) {
            int res = RSpectraUtils.getMalariaRawData(sb.getRConnection(), homeDir);
            if (res == 0) {
                DataUtils.showMessage(FacesMessage.SEVERITY_ERROR, "Sorry!",
                        "Cannot find data! Please ask Zhiqiang Pang!");
            }
        }

        pcb.setMetaOk(true);
        sb.setDataUploaded();
        containsMeta = true;
        pkb.populateSpecBeans();
        pcb.setTotalNumberOfSamples(30);
        SpectraParamBean spb = (SpectraParamBean) DataUtils.findBean("spectraParamer");
        spb.setPolarity("positive");
        //Here is to update the status in mysql db
        if (sb.isRegisteredLogin()) {
            ProjectBean pb = (ProjectBean) DataUtils.findBean("projectBean");
            pb.updateProjectStatus("uploaded", sb.getCurrentUser().getName());
        }

        return "Spectra check";
    }

    public String goToProcessing() throws REXPMismatchException {

        pcb.OptiLCMSCheck();

        if (!sb.isLoggedIn() || uploadedFileNames.isEmpty()) {
            pkb.getMsgList().add("Please upload your data before proceeding!");
            updateText();
            return "";
        }

        if (uploadedFileNames.size() < 3) {
            DataUtils.updateMsg("Error", "At least 3 samples are required for processing! Please keep uploading!");
            return "";
        }

        if (sanityCheck()) {
            pcb.setTotalNumberOfSamples(uploadedFileNames.size());
            sb.setDataUploaded();
            if (containsMeta) {
                pkb.populateSpecBeans();
            } else {
                pkb.populateSpecBeansNoMeta();
            }
            sb.setSaveEnabled(true);

            //Here is to update the status in mysql db
            if (sb.isRegisteredLogin()) {
                ProjectBean pb = (ProjectBean) DataUtils.findBean("projectBean");
                pb.updateProjectStatus("uploaded", sb.getCurrentUser().getName());
            }

            return "Spectra check";
        } else {
            return "";
        }
    }

    public void projectProcessing() throws REXPMismatchException {

        pcb.OptiLCMSCheck();
        if (!sb.isLoggedIn() || uploadedFileNames.isEmpty()) {
            pkb.getMsgList().add("Please upload your data before proceeding!");
            updateText();
        }

        if (sanityCheck()) {
            pcb.setTotalNumberOfSamples(uploadedFileNames.size());
            sb.setDataUploaded();

            if (containsMeta) {
                pkb.populateSpecBeans();
            } else {
                pkb.populateSpecBeansNoMeta();
            }

            sb.setSaveEnabled(true);
        }
    }

    public int doSpecLogin() {
        pcb.OptiLCMSCheck();
        if (!sb.isLoggedIn() || !sb.getAnalType().equals("raw")) {
            sb.doLogin("spec", "raw", false, false);
        }
        return 1;
    }

    public void showMetaError() {

        String homeDir = sb.getCurrentUser().getHomeDir();
        File meta = new File(homeDir + File.separator + metaName);
        meta.delete();
        updateText();
        DataUtils.updateMsg("Error", "The content of meta-data file is not formatted correctly! Please check!");
    }

    public void moreThanOneMeta() {
        DataUtils.updateMsg("Error", "Only one meta-data file can be present!");
    }

    public boolean checkMetaIntegrity(String fileNms) {
        pkb.getMsgList().removeAll(pkb.getMsgList());
        containsMeta = true;
        boolean metaok = pcb.isMetaOk();

        boolean res = true;

        if (!metaok) {
            pkb.getMsgList().add("The content of meta-data file is not formatted correctly!");
            res = false;
        }

        fileNmsFromMeta = RDataUtils.getRawFileNames(sb.getRConnection());
        classNmsFromMeta = RDataUtils.getRawClassNames(sb.getRConnection());
        Set<String> uniqueClasses = new HashSet<String>();

        uniqueClasses.addAll(Arrays.asList(classNmsFromMeta));

        int classSize = uniqueClasses.size();

        List<String> classList = Arrays.asList(classNmsFromMeta);
        for (String grp : uniqueClasses) {
            if (grp.toUpperCase().equals("QC")) {
                classSize--;
            } else if (grp.toUpperCase().equals("BLANK")) {
                classSize--;
            } else {
                int occurrences = Collections.frequency(classList, grp);
                if (occurrences < 3) {
                    DataUtils.updateMsg("Error", "Please make sure that there are at least three samples per group:");
                    res = false;
                    break;
                }
            }
        }

        if (classNmsFromMeta.length < 6 || classNmsFromMeta.length / classSize < 3) {
            pkb.getMsgList().add("Please make sure at least 2 groups of 3 replicates are present!");
            res = false;
        }

        String[] nmsArr = fileNms.split("; ");

        if (nmsArr.length != fileNmsFromMeta.length) {
            pkb.getMsgList().add("Inconsistency between selected files and meta-data provided!");
            res = false;
        }

        for (int i = 0; i < nmsArr.length; i++) {
            nmsArr[i] = nmsArr[i].split(".zip")[0];
        }

        for (String fileNm : fileNmsFromMeta) {
            String fileNmTrim = fileNm.split(".zip|.mzXML|.mzML|.mzData")[0];
            if (!Arrays.asList(nmsArr).contains(fileNmTrim)) {
                pkb.getMsgList().add("'<b>" + fileNmTrim + "</b>' is missing from uploaded files!");
                res = false;
            }
        }

        for (String fileNm : nmsArr) {
            String fileNmTrim = fileNm.split(".zip|.mzXML|.mzML|.mzData")[0];
            if (!Arrays.asList(fileNmsFromMeta).contains(fileNmTrim)) {
                if (!fileNmTrim.equals("")) {
                    pkb.getMsgList().add("'<b>" + fileNmTrim + "</b>' is not present in meta-data file!");
                    res = false;
                }
            }
        }
        return res;
    }

    public boolean sanityCheck() {
        updateText();
        if (!containsMeta) {
            pkb.getMsgList().removeAll(pkb.getMsgList());
            return true;
        }
        //Check file one by one to see whether they match to names from meta-file

        return pkb.getMsgList().isEmpty();
    }

    int numOfUploadFiles = 0;

    public void minusUploadingCount() {
        numOfUploadFiles--;
        if (numOfUploadFiles == 0) {
            //ab.minusUploadCount();
            ResourceSemaphore resourceSemaphore = (ResourceSemaphore) DataUtils.findBean("semaphore");
            resourceSemaphore.getUploadSemaphore().release();
            sanityCheck();
        }
    }

    @PreDestroy
    public void onSessionLeaveUploadReset() {
        if (numOfUploadFiles > 0) {
            ResourceSemaphore resourceSemaphore = (ResourceSemaphore) DataUtils.findBean("semaphore");
            resourceSemaphore.getUploadSemaphore().release();
        }
    }

    public void addNumOfUploadFiles(ActionEvent e) {
        String length = e.getFacesContext().getExternalContext().getRequestParameterMap().get("size");
        if (length != null && Integer.parseInt(length) > 0) {
            numOfUploadFiles = numOfUploadFiles + Integer.parseInt(length);
        }
    }

    //// Section : Centroid MS Data
    public void doCentroidMSdata(String Name, String group) throws REXPMismatchException {

        RConnection RC = sb.getRConnection();
        File f = new File(sb.getCurrentUser().getHomeDir() + "/upload/" + group);
        int res = 0;

        if (f.isDirectory() && f.exists()) {
            containsMeta = true;
            res = RDataUtils.centroidMSData(RC, Name, sb.getCurrentUser().getHomeDir() + "/upload/" + group);
        } else {
            containsMeta = false;
            res = RDataUtils.centroidMSData(RC, Name, sb.getCurrentUser().getHomeDir() + "/upload/");
        }
        //pkb.getSelectedData().setFormat("True");

        switch (res) {
            case 1:
                DataUtils.updateMsg("OK", "Your data has been centroid now!");
                break;
            case -1:
                DataUtils.updateMsg("Error", "Sorry, This data cannot be centroid, please contact with administrator!");
                break;
            case -2:
                DataUtils.updateMsg("Error", "Sorry, only mzML and mzXML are supported for now. Other formats will be supported soon!");
                break;
        }

        if (containsMeta) {
            pkb.populateSpecBeans();
        } else {
            pkb.populateSpecBeansNoMeta();
        }

        if (pkb.getCentroidFileCount() >= 3) {
            pkb.setBnDisabled(false);
        } else {
            pkb.setBnDisabled(true);
        }
    }

}
