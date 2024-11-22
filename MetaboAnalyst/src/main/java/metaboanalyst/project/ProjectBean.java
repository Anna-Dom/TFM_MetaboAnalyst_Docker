/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package metaboanalyst.project;

import metaboanalyst.controllers.ApplicationBean1;

import metaboanalyst.controllers.SessionBean1;
import metaboanalyst.utils.DataUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.faces.model.SelectItem;
import metaboanalyst.models.User;
import metaboanalyst.rwrappers.RCenter;
import metaboanalyst.rwrappers.RDataUtils;
import metaboanalyst.rwrappers.RSpectraUtils;
import metaboanalyst.spectra.SpectraControlBean;
import metaboanalyst.spectra.SpectraParamBean;
import metaboanalyst.spectra.SpectraProcessBean;
import metaboanalyst.spectra.SpectraUploadBean;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.RList;
import org.rosuda.REngine.Rserve.RConnection;

/**
 *
 * @author soufanom
 */
@SessionScoped
@Named("projectBean")
public class ProjectBean implements Serializable {

    private final ApplicationBean1 ab = (ApplicationBean1) DataUtils.findBean("applicationBean1");
    private final SessionBean1 sb = (SessionBean1) DataUtils.findBean("sessionBean1");
    private final UserLoginBean ulb = (UserLoginBean) DataUtils.findBean("userLoginBean");

    public UserLoginBean getUlb() {
        return ulb;
    }

    private final static int PROJECT_LIMITS = 10;

    //Project details
    private String title;
    private String description;
    private String projectType = "raw";

    //Project data table
    private List<ProjectModel> projectTable = null;
    private HashMap<Long, ProjectModel> projectMap = null;
    private ProjectModel selectedProject = null;
    private String dataPlace = "";

    private ProjectModel currentProject = null;

    public ProjectModel getCurrentProject() {
        return currentProject;
    }

    public void setCurrentProject(ProjectModel currentProject) {
        this.currentProject = currentProject;
    }

    // initialize mysql - from ulb bean
    private MariaDBController mdbb = ulb.getMdbb();

    public MariaDBController getMdbb() {
        return mdbb;
    }

    // Section I --- Project operation section
    public void prepareProject() {
        //System.out.println("preparing selectedProject: " + selectedProject.getId());

        if (selectedProject == null) {
            DataUtils.showMessage(FacesMessage.SEVERITY_ERROR, "No project is selected!", "Please select a project to perform action.");
        } else {

            String reDirectLink;
            String folderNum = selectedProject.getFolder().substring(5, 25);
            long UserIden = selectedProject.getUserNM();

            switch (selectedProject.getHostname()) {
                case "dev":
                    reDirectLink = "https://www.metaboanalyst.ca/MetaboAnalyst/faces/LoadProject?ID=" + UserIden + "_" + folderNum;
                    break;
                case "genap":
                    reDirectLink = "https://genap.metaboanalyst.ca/MetaboAnalyst/faces/LoadProject?ID=" + UserIden + "_" + folderNum;
                    //  if (ab.isOnGenapPublicDev()) {
                    //      reDirectLink = "http://206.12.89.229:8080/MetaboAnalyst/faces/LoadProject?ID=" + UserIden + "_" + folderNum;
                    //  }
                    break;
                default:
                    reDirectLink = "http://localhost:8080/MetaboAnalyst/faces/LoadProject?ID=" + UserIden + "_" + folderNum;
                    break;
            }
            DataUtils.doRedirect(reDirectLink);
        }
    }

    public String initializeProject(String guestFolder) {
        try {
            // Here is used to restore the selected project class after jump into the Nodes
            selectedProject = obtainCertainProject(ulb.getUserNM(), guestFolder);
            if (selectedProject != null) {
                return "done";
            }
        } catch (SQLException ex) {
            DataUtils.showMessage(FacesMessage.SEVERITY_ERROR, "Internal error!",
                    "Project managing server is down, report this code: pxx00046 to administrator !");
        }

        return null;
    }

    public String loadnewProject() {
        //Process Naive status
        sb.doLogin("spec", "raw", false, false);
        //Here we get the guest folder name
        String guestProFolder = sb.getCurrentUser().getName();
        selectedProject.setFolder(guestProFolder);

        //Here we get the server name
        if (ab.isOnMainServer()) {
            dataPlace = "dev";
        } else if (ab.isOnGenap()) {
            dataPlace = "genap";
        } else if (ab.isOnQiangPc()) {
            dataPlace = "qiang";
        } else {
            dataPlace = "unk"; // an unknown place, maybe genap private
        }

        //Here we udpate mysql db                    
        String updateQury = "update devUsers.projects set status = 'started', projectFolderNM = '"
                + guestProFolder + "', dataplace = '" + dataPlace + "' where userNM = "
                + ulb.getUserNM() + " AND title = '" + selectedProject.getTitle() + "';";
        try {
            mdbb.runUpdate(updateQury);
            DataUtils.showMessage(FacesMessage.SEVERITY_INFO, "Project Started Successfully!", "");
        } catch (SQLException ex) {
            DataUtils.showMessage(FacesMessage.SEVERITY_ERROR, "Internal error!",
                    "Project managing server is down, report this code: pxx00014 to administrator !");
            return null;
        }

        return "spec";
    }

    public String loadProject() throws SQLException {

        //confirm the project exists
        if (ab.isOnMainServer()) {
            dataPlace = "dev";
        } else if (ab.isOnGenap()) {
            dataPlace = "genap";
        } else if (ab.isOnQiangPc()) {
            dataPlace = "qiang";
        } else {
            dataPlace = "unk"; // an unknown place, maybe genap private
        }

        String ConfirmQuery = "SELECT EXISTS(select * from devUsers.projects where userNM="
                + ulb.getUserNM()
                + " AND title = '"
                + selectedProject.getTitle()
                + "' AND dataplace = '"
                + dataPlace
                + "');";
        ResultSet res;

        try {
            res = mdbb.runQuery(ConfirmQuery);
            if (res.next()) {
                if (res.getInt(1) != 1) {
                    DataUtils.showMessage(FacesMessage.SEVERITY_ERROR, "Project damaged!",
                            "The report this error code xxxp00015 and your project information to administrator!");
                    return null;
                }
            }
        } catch (SQLException ex) {
            DataUtils.showMessage(FacesMessage.SEVERITY_ERROR, "Internal error!",
                    "Project server is down, report this code: xxxp00016 to administrator !");
            return null;
        } finally {
            mdbb.disconnect();
        }

        //Initialize some variables and beans
        String status = null, proFolder = null;
        SpectraControlBean scb = null;
        long jobID = 0;
        String jobStatus = ""; //NOTE: status is the recorded 'status' from mysql db, while the 'jobStatus' is the real status of a certain job from slurm.
        SpectraProcessBean spb = (SpectraProcessBean) DataUtils.findBean("spectraProcessor");

        //read job status + guest folder
        String readQuery = "select status,projectFolderNM,dataplace from devUsers.projects where userNM ="
                + ulb.getUserNM()
                + " AND title = '"
                + selectedProject.getTitle()
                + "' AND dataplace = '"
                + dataPlace
                + "';";

        try {
            res = mdbb.runQuery(readQuery);
            if (res.next()) {
                status = res.getString(1);
                proFolder = res.getString(2);
                selectedProject.setFolder(proFolder);
                //dataPlace = res.getString(3);
            }
        } catch (SQLException ex) {
            DataUtils.showMessage(FacesMessage.SEVERITY_ERROR, "Internal error!",
                    "Project server is down, report this code: xxxp00016 to administrator !");
            return null;
        } finally {
            mdbb.disconnect();
        }

        //Set java variable for this status (started, uploaded, submitted, finished)
        if ("started".equals(status.toLowerCase()) || "uploaded".equals(status.toLowerCase()) || "submitted".equals(status.toLowerCase()) || "finished".equals(status.toLowerCase())) {
            //Basic Jave setting
            setCurrentProject(selectedProject);
            sb.setDataType("spec");
            User user = new User();
            user.setName(proFolder);
            if (ab.isOnMainServer()) {
                user.setHomeDir("/data/glassfish/projects/metaboanalyst/" + proFolder);
            } else {
                user.setHomeDir(ab.getRealUserHomePath() + File.separator + proFolder);
            }

            sb.setCurrentUser(user);
            sb.doProjectLogin("raw", proFolder, false);
        }

        if ("uploaded".equals(status.toLowerCase()) || "submitted".equals(status.toLowerCase()) || "finished".equals(status.toLowerCase())) {
            //File uploaded Setting
            sb.setDataUploaded();
            List<String> uploadedFiles = new ArrayList<>();
            File folder;

            if (ab.isOnMainServer()) {
                folder = new File("/data/glassfish/projects/metaboanalyst/" + proFolder + "/upload");
            } else {
                folder = new File(ab.getRealUserHomePath() + File.separator + proFolder + "/upload");
            }

            File[] listOfFiles = folder.listFiles();
            int groups = 0;

            for (File listOfFile : listOfFiles) {
                if (listOfFile.isFile()) {
                    //handle files directly shown in the folder
                    //System.out.println("File " + listOfFiles[i].getName());
                    if (listOfFile.getName().endsWith(".mzml") | listOfFile.getName().toLowerCase().endsWith(".mzxml") | listOfFile.getName().toLowerCase().endsWith(".cdf") | listOfFile.getName().toLowerCase().endsWith(".mzdata")) {
                        //System.out.println("FOund file: " + listOfFiles[i].getName());
                        uploadedFiles.add(listOfFile.getName());
                    }
                } else if (listOfFile.isDirectory()) {
                    //handle files shown in multiple subfolders
                    //System.out.println("FOund folder: " + listOfFiles[i].getName());
                    groups++;
                    File subFolder = listOfFile;
                    File[] newlistOfFile = subFolder.listFiles();
                    for (File newlistOfFile1 : newlistOfFile) {
                        if (newlistOfFile1.getName().toLowerCase().endsWith(".mzml") | newlistOfFile1.getName().toLowerCase().endsWith(".mzxml") | newlistOfFile1.getName().toLowerCase().endsWith(".cdf") | newlistOfFile1.getName().toLowerCase().endsWith(".mzdata")) {
                            //System.out.println("FOund file: " + newlistOfFile[j].getName());
                            uploadedFiles.add(newlistOfFile1.getName());
                        }
                    }
                }
            }

            SpectraUploadBean sub = (SpectraUploadBean) DataUtils.findBean("specLoader");
            sub.setUploadedFileNames(uploadedFiles);

            if (groups > 0) {
                sub.setContainsMeta(true);
            }

            try {
                sub.projectProcessing();
            } catch (REXPMismatchException ex) {
                DataUtils.showMessage(FacesMessage.SEVERITY_ERROR, "Internal error!",
                        "Project data processing failed, please create a new project and re-upload your data!");
            }
        }

        if ("submitted".equals(status.toLowerCase()) || "finished".equals(status.toLowerCase())) {

            scb = (SpectraControlBean) DataUtils.findBean("spectraController"); //grab this bean first
            scb.setDataConfirmed(true);

            // Obtain file inclusion info
            RConnection RC = sb.getRConnection();

            try {
                if (ab.isOnMainServer()) {
                    RC.voidEval("setwd('" + ab.getProjectsHome() + proFolder + "')");
                } else {
                    RC.voidEval("setwd('" + ab.getRealUserHomePath() + File.separator + proFolder + "')");
                }

                String ModeInfo = RSpectraUtils.retrieveModeInfo(RC);
                if ("auto".equals(ModeInfo)) {
                    SpectraParamBean sppb = (SpectraParamBean) DataUtils.findBean("spectraParamer");
                    sppb.setMeth("auto");
                }

                RList reslist = RDataUtils.readFilesInclusion(RC);
                scb.setIncludedFileNamesString(reslist.at(0).asString());
                scb.setTotalNumberOfSamples(reslist.at(1).asInteger());
            } catch (Exception ex) {
                scb.setIncludedFileNamesString("");
                DataUtils.showMessage(FacesMessage.SEVERITY_ERROR, "Internal error!",
                        "Rserve seems not working correctly. The result might be influenced !");
            }

            // Obtain job ID from mysql db
            String retrieveQuery = "select jobID from devUsers.jobs where jobFolder = '"
                    + proFolder
                    + "' AND jobPosition = '"
                    + dataPlace
                    + "';";
            try {
                ResultSet ressub = mdbb.runQuery(retrieveQuery);
                while (ressub.next()) {
                    jobID = ressub.getLong(1);
                }
            } catch (Exception ex) {
                DataUtils.showMessage(FacesMessage.SEVERITY_ERROR, "Internal error!",
                        "Project server is down, report this code: xxxp00018 to administrator !");
            } finally {
                mdbb.disconnect();
            }

            // Obtain job status from slurm and progress (if progress = 100, finished : read from slurm[Can be slurmdb future])
            double progress = scb.updateProgress();

            if (progress == 100) {
                jobStatus = "Finished";

                scb.setCurrentJobStatus(jobStatus);
                scb.setCurrentJobId(jobID);

                scb.setJobSubmitted(true);
                scb.setPerformedPlan(true);
                scb.setKilled(false);
                scb.setStopStatusCheck(false);
                scb.setProgress2(progress);
            }

            if (progress < 100) {
                if (jobID == 0) {
                    DataUtils.showMessage(FacesMessage.SEVERITY_ERROR, "Internal error!",
                            "Project server is down, report this code: xxxp00019 to administrator !");
                } else {
                    jobStatus = SchedulerUtils.getJobStatus(jobID);
                }

                scb.setCurrentJobStatus(jobStatus);
                scb.setCurrentJobId(jobID);

                scb.setJobSubmitted(true);
                scb.setPerformedPlan(true);
                scb.setProgress2(progress);

                if ("running".equals(jobStatus.toLowerCase())) {
                    // if jobs are running
                    scb.setKilled(false);
                    scb.setStopStatusCheck(true);

                } else {
                    // if jobs are killed or failed
                    scb.setKilled(true);
                    scb.setStopStatusCheck(false);
                }
            }
        }

        if ("finished".equals(status.toLowerCase()) || "finished".equals(jobStatus.toLowerCase())) {

            try {
                spb.internalizeRes(0);
                spb.populateRawResBeans();
                spb.populateRawFeatureBeans();
            } catch (FileNotFoundException ex) {
                DataUtils.showMessage(FacesMessage.SEVERITY_ERROR, "Internal error!",
                        "Your previous result is missing, please re-do the process !");
            }

            System.out.println("Processing has been finished already!");
            sb.setDataUploaded();
            scb.setStopStatusCheck(false);
            scb.setFinishedJobId(jobID);
            scb.setFinishedProgress2(100);
            scb.setFinishedJobStatus(jobStatus);
        }

        //add navii tree & redirect the corresponding page according to the status
        sb.initNaviTree("spec");
        if ("started".equals(status)) {
            sb.registerPage("Upload");
            return "spec";
        } else if ("uploaded".equals(status)) {
            sb.registerPage("Upload");
            sb.registerPage("Spectra check");
            return "Spectra check";
        } else if ("submitted".equals(status)) {
            sb.registerPage("Upload");
            sb.registerPage("Spectra check");
            sb.registerPage("Spectra processing");
            sb.registerPage("Job status");
            return "Job status";
        } else if ("finished".equals(status.toLowerCase())) {
            sb.registerPage("Upload");
            sb.registerPage("Spectra check");
            sb.registerPage("Spectra processing");
            sb.registerPage("Job status");
            sb.registerPage("Spectra result");
            return "Spectra result";
        } else {
            DataUtils.showMessage(FacesMessage.SEVERITY_ERROR, "Internal error!",
                    "Something weird happened, report this code: xxxp00017 to administrator !");
            return null;
        }
    }

    public boolean updateProjectStatus(String status, String proFolder) {

        if (ab.isOnMainServer()) {
            dataPlace = "dev";
        } else if (ab.isOnQiangPc()) {
            dataPlace = "qiang";
        } else if (ab.isOnGenap()) {
            dataPlace = "genap";
        } else {
            dataPlace = "unk"; // an unknown place, maybe genap private
        }

        String UpQuery = "update devUsers.projects set status = '"
                + status + "' where projectFolderNM = '"
                + proFolder + "' AND dataplace = '" + dataPlace + "';";

        try {
            mdbb.runUpdate(UpQuery);
            DataUtils.showMessage(FacesMessage.SEVERITY_INFO, "Project status updated Successfully!", "");
            return true;
        } catch (SQLException ex) {
            DataUtils.showMessage(FacesMessage.SEVERITY_ERROR, "Internal error!",
                    "Project update failed: report this error code pxx00016 to administrator !");
        }

        return false;
    }

    public ProjectModel obtainCertainProject(long userNM, String projectFolderNum) throws SQLException {

        try {
            if (!ulb.MysqlDBAvailabilityCheck()) {
                DataUtils.showMessage(FacesMessage.SEVERITY_ERROR, "Internal error!",
                        "Project server down: report this error code pxx00088 to administrator !");
                return null;
            }
        } catch (Exception ex) {
            DataUtils.showMessage(FacesMessage.SEVERITY_ERROR, "Internal error!",
                    "Project server down: report this error code pxx00088 to administrator !");
        }

        // Retrieve the data from db
        String query = "select * from devUsers.projects where userNM=" + userNM + ";";
        ResultSet res;
        ProjectModel project = null;

        try {
            res = mdbb.runQuery(query);
            while (res.next()) {
                if (res.getString(7).contains(projectFolderNum)) {
                    project = new ProjectModel();

                    project.setUserNM(userNM);
                    project.setTitle(res.getString(2));
                    project.setDescription(res.getString(3));
                    project.setType(res.getString(4));
                    project.setCreationDate(res.getDate(5));
                    project.setStatus(res.getString(6));
                    project.setFolder(res.getString(7));
                    project.setHostname(res.getString(8));
                    break;
                }
            }

        } catch (SQLException ex) {
            Logger.getLogger(ProjectController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            mdbb.disconnect();
        }

        return project;
    }

    // Section II -----partial operation section
    public String loadPartial() throws SQLException {

        // initialize/grab some parameters and beans
        String partialID = sb.getPartialId();
        SpectraControlBean scb = (SpectraControlBean) DataUtils.findBean("spectraController");
        String jobStatus = null;
        String jobFolder;
        //verify partial link expiration
        if (ab.isInDocker()) {
            jobFolder = checkParitialLinkinDocker(partialID);
        } else {
            jobFolder = checkPartialvalid(partialID);
        }

        //String jobFolder = checkPartialvalid(partialID);
        if (jobFolder == null) { // A invalid/expired partial link
            System.out.println("This is an invalid link to job: " + jobFolder);
            return null;
        } else {
            System.out.println("This is a valid link to job: " + jobFolder);
        }

        //get parial job id and initialize sessionbean's current user
        String pJobID = partialID.split("_")[1];

        sb.setDataType("spec");
        User user = new User();
        user.setName(jobFolder);
        if (ab.isOnMainServer()) {
            user.setHomeDir("/data/glassfish/projects/metaboanalyst/" + jobFolder);
        } else {
            user.setHomeDir(ab.getRealUserHomePath() + File.separator + jobFolder);
        }

        sb.setCurrentUser(user);
        sb.doProjectLogin("raw", jobFolder, false);
        sb.setDataUploaded();
        RConnection RC = sb.getRConnection();

        //obtain the spectra file info
        List<String> uploadedFiles = new ArrayList<>();
        File folder;

        if (ab.isOnMainServer()) {
            folder = new File("/data/glassfish/projects/metaboanalyst/" + jobFolder + "/upload");
        } else {
            folder = new File(ab.getRealUserHomePath() + File.separator + jobFolder + "/upload");
        }

        File[] listOfFiles = folder.listFiles();
        int groups = 0;

        for (File listOfFile : listOfFiles) {
            if (listOfFile.isFile()) {
                //handle files directly shown in the folder
                //System.out.println("File " + listOfFile.getName());
                if (listOfFile.getName().endsWith(".mzml") | listOfFile.getName().toLowerCase().endsWith(".mzxml") | listOfFile.getName().toLowerCase().endsWith(".cdf") | listOfFile.getName().toLowerCase().endsWith(".mzdata")) {
                    //System.out.println("FOund file: " + listOfFiles[i].getName());
                    uploadedFiles.add(listOfFile.getName());
                }
            } else if (listOfFile.isDirectory()) {
                //handle files shown in multiple subfolders
                //System.out.println("FOund folder: " + listOfFiles[i].getName());
                groups++;
                File subFolder = listOfFile;
                File[] newlistOfFile = subFolder.listFiles();
                for (File newlistOfFile1 : newlistOfFile) {
                    if (newlistOfFile1.getName().toLowerCase().endsWith(".mzml") | newlistOfFile1.getName().toLowerCase().endsWith(".mzxml") | newlistOfFile1.getName().toLowerCase().endsWith(".cdf") | newlistOfFile1.getName().toLowerCase().endsWith(".mzdata")) {
                        //System.out.println("FOund file: " + newlistOfFile[j].getName());
                        uploadedFiles.add(newlistOfFile1.getName());
                    }
                }
            }
        }

        SpectraUploadBean sub = (SpectraUploadBean) DataUtils.findBean("specLoader");
        sub.setUploadedFileNames(uploadedFiles);
        scb.setDataConfirmed(true);
        if (groups > 0) {
            sub.setContainsMeta(true);
        }

        try {
            sub.projectProcessing();
        } catch (REXPMismatchException ex) {
            DataUtils.showMessage(FacesMessage.SEVERITY_ERROR, "Internal error!",
                    "Project data processing failed, please create a new project and re-upload your data!");
        }

        try {
            if (ab.isOnMainServer()) {
                RC.voidEval("setwd('" + ab.getProjectsHome() + jobFolder + "')");
            } else {
                RC.voidEval("setwd('" + ab.getRealUserHomePath() + File.separator + jobFolder + "')");
            }

            String ModeInfo = RSpectraUtils.retrieveModeInfo(RC);
            if ("auto".equals(ModeInfo)) {
                SpectraParamBean spb = (SpectraParamBean) DataUtils.findBean("spectraParamer");
                spb.setMeth("auto");
            }

            RList reslist = RDataUtils.readFilesInclusion(RC);

            scb.setIncludedFileNamesString(reslist.at(0).asString());
            scb.setTotalNumberOfSamples(reslist.at(1).asInteger());
        } catch (Exception ex) {
            scb.setIncludedFileNamesString("");
            DataUtils.showMessage(FacesMessage.SEVERITY_ERROR, "Internal error!",
                    "Rserve seems not working correctly. The result might be influenced !");
        }

        // Obtain job status from slurm and progress (if progress = 100, finished : read from slurm[Can be slurmdb future])
        double progress = scb.updateProgress();
        int jobID = Integer.parseInt(pJobID);

        if (progress == 100) {
            jobStatus = "Finished";

            scb.setCurrentJobStatus(jobStatus);
            scb.setCurrentJobId(jobID);

            scb.setJobSubmitted(true);
            scb.setPerformedPlan(true);
            scb.setKilled(false);
            scb.setStopStatusCheck(false);
            scb.setProgress2(progress);
        }

        if (progress < 100) {
            if (jobID == 0) {
                DataUtils.showMessage(FacesMessage.SEVERITY_ERROR, "Internal error!",
                        "Project server is down, report this code: xxxp00019 to administrator !");
            } else {
                jobStatus = SchedulerUtils.getJobStatus(jobID);
            }

            scb.setCurrentJobStatus(jobStatus);
            scb.setCurrentJobId(jobID);

            scb.setJobSubmitted(true);
            scb.setPerformedPlan(true);
            scb.setProgress2(progress);

            if ("running".equals(jobStatus.toLowerCase())) {
                // if jobs are running
                scb.setKilled(false);
                scb.setStopStatusCheck(true);

            } else {
                // if jobs are killed or failed
                scb.setKilled(true);
                scb.setStopStatusCheck(false);
            }
        }

        //done
        System.out.println(" --- Loading partial link finished! <----");
        return "Job status";
    }

    private String checkPartialvalid(String partialID) throws SQLException {
        String folder = null;
        long diff = 100;
        try {
            ulb.MysqlDBAvailabilityCheck();
        } catch (Exception ex) {
            DataUtils.showMessage(FacesMessage.SEVERITY_ERROR, "Internal error!",
                    "URL managing server is down, report this code: uxx00010 to administrator !");
        }

        String checkQuery = "select jobSubTime,jobFolder from devUsers.jobs where partialLink = '" + partialID + "';";

        mdbb = ulb.getMdbb();

        try {
            ResultSet jobRes = mdbb.runQuery(checkQuery);
            if (jobRes.next()) {
                Date creatDate = jobRes.getDate(1);
                Date currentDate = new Date();
                long timed = currentDate.getTime() - creatDate.getTime();
                diff = TimeUnit.DAYS.convert(timed, TimeUnit.MILLISECONDS);
                System.out.println("---------- time of days has pased since the url was created: " + diff);
                if (diff < 14) {
                    folder = jobRes.getString(2);
                }
            }
        } catch (Exception ex) {
            DataUtils.showMessage(FacesMessage.SEVERITY_ERROR, "Internal error!",
                    "URL managing server is down, report this code: uxx00011 to administrator !");
        } finally {
            mdbb.disconnect();
        }

        return folder;
    }

    public boolean checkLink() throws SQLException {

        // initialize/grab some parameters and beans
        String partialID = sb.getPartialId();

        //verify partial link expiration
        String jobFolder = checkPartialvalid(partialID);

        if (jobFolder == null) { // A invalid/expired partial link
            return false;
        } else {
            return true;
        }
    }

    public boolean checkLinkinDocker() {

        // initialize/grab some parameters and beans
        String partialID = sb.getPartialId();

        //verify partial link expiration
        String jobFolder = null;
        boolean tmpRCon = false;
        try {
            RConnection rxc = sb.getRConnection();
            if (rxc == null) {
                rxc = RCenter.getCleanRConnection();
            }
            jobFolder = RDataUtils.checkParitialLinkinDocker(rxc, partialID, 0);
            if (tmpRCon) {
                rxc.close();
            }
        } catch (Exception ex) {
            //Logger.getLogger(SeqProcesser.class.getName()).log(Level.SEVERE, null, ex);
        }

        return jobFolder != null; // A invalid/expired partial link
    }

    public String checkParitialLinkinDocker(String partialID) {
        String jobFolder = null;
        boolean tmpRCon = false;
        try {
            RConnection rxc = sb.getRConnection();
            if (rxc == null) {
                rxc = RCenter.getCleanRConnection();
                tmpRCon = true;
            }
            jobFolder = RDataUtils.checkParitialLinkinDocker(rxc, partialID, 0);
            if (tmpRCon) {
                rxc.close();
            }
        } catch (Exception ex) {
            //Logger.getLogger(SeqProcesser.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jobFolder;
    }

    // Section IV --- Project variable section
    public SelectItem[] getProjectTypes() {
        SelectItem[] projectTypes = new SelectItem[1];
        projectTypes[0] = new SelectItem("raw", "Spectra processing");
        return projectTypes;
    }

    public int getProjectLimit() {
        return PROJECT_LIMITS;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getProjectType() {
        return projectType;
    }

    public void setProjectType(String projectType) {
        this.projectType = projectType;
    }

    public HashMap<Long, ProjectModel> getProjectMap() {
        return projectMap;
    }

    public void setProjectMap(HashMap<Long, ProjectModel> projectMap) {
        this.projectMap = projectMap;
    }

    public ProjectModel getSelectedProject() {
        return selectedProject;
    }

    public void setSelectedProject(ProjectModel selectedProject) {
        setCurrentProject(selectedProject);
        this.selectedProject = selectedProject;
    }

    public List<ProjectModel> getProjectTable() {
        return projectTable;
    }

    public void setProjectTable(List<ProjectModel> projectTable) {
        this.projectTable = projectTable;
    }

    public String getProjectRelativeDir() {
        //set relative dir
        String relativeDir = ab.getProjectsHome() + File.separator + getCurrentProject().getFolder();
        return relativeDir;
    }

}
