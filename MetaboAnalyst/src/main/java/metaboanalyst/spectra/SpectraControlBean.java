/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package metaboanalyst.spectra;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.Writer;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Scanner;
import javax.annotation.PreDestroy;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import metaboanalyst.controllers.ApplicationBean1;
import metaboanalyst.controllers.SessionBean1;
import metaboanalyst.project.MariaDBController;
import metaboanalyst.project.ProjectBean;
import metaboanalyst.project.SchedulerUtils;
import metaboanalyst.project.UserLoginBean;
import metaboanalyst.rwrappers.RCenter;
import metaboanalyst.rwrappers.RDataUtils;
import metaboanalyst.utils.DataUtils;
import org.omnifaces.util.Faces;
import org.primefaces.PrimeFaces;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.Rserve.RConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author qiang
 */
@SessionScoped
@Named("spectraController")
public class SpectraControlBean implements Serializable {

    private final ApplicationBean1 ab = (ApplicationBean1) DataUtils.findBean("applicationBean1");
    private final SessionBean1 sb = (SessionBean1) DataUtils.findBean("sessionBean1");
    private final SpectraParamBean sparam = (SpectraParamBean) DataUtils.findBean("spectraParamer");
    private static final Logger LOGGER = LogManager.getLogger(SpectraControlBean.class);

    private boolean createdShareLink = false;

    public boolean isCreatedShareLink() {
        return createdShareLink;
    }

    private LinkedHashMap<String, String> javaHistory = new LinkedHashMap<>();

    public LinkedHashMap<String, String> getJavaHistory() {
        return javaHistory;
    }

    public void setJavaHistory(LinkedHashMap<String, String> javaHistory) {
        this.javaHistory = javaHistory;
    }

    public void setCreatedShareLink(boolean createdShareLink) {
        this.createdShareLink = createdShareLink;
    }

    // Section 0 : Variable Section - count--------------
    private int count = 0;

    // Section 1 : General Controller -------------------    
    public void performPlan() throws REXPMismatchException {

        int pid;

        if (isJobSubmitted() || performedPlan || getCurrentJobId() != 0) {
            count = count + 1;
            return;
        }

        if (count == 0) {
            count = count + 1;
            return;
        }

        if (progress2 != 0 || isCreatedShareLink() || !isDataConfirmed()) { //used to strongly block the job resubmit
            return;
        }

        RConnection RC = sb.getRConnection();
        setJobSubmitted(true);

        boolean res = false;
        String params0_path = ab.getParams0();

        if ("customized".equals(sparam.getMeth())) {

            try {
                int res2 = RDataUtils.paramChangingDetection(RC, params0_path, sb.getCurrentUser().getHomeDir());
                if (res2 == 1) {
                    setParamCNothanged(true);
                } else {
                    setParamCNothanged(false);
                }
            } catch (REXPMismatchException ex) {
                LOGGER.error("performPlan-customized", ex);
                //Logger.getLogger(SpectraProcessBean.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

        if ("auto".equals(sparam.getMeth())) { // to make sure the previous running will not interfere the demo of auto

            setParamCNothanged(true);

            try {
                int res2 = RDataUtils.paramChangingDetection(RC, params0_path, sb.getCurrentUser().getHomeDir());
                if (res2 == 1) {
                    setParamCNothanged(true);
                } else {
                    setParamCNothanged(false);
                }
            } catch (REXPMismatchException ex) {
                LOGGER.error("performPlan-auto", ex);
                //Logger.getLogger(SpectraProcessBean.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if (executExample) {
            // Running the fast show - demo things
            RConnection RCDemo = RCenter.getRConnectionRawSharing(sb.getCurrentUser().getHomeDir(), ab.getRscriptLoaderPath(), sb.getAnalType());
            pid = RDataUtils.demoPlanProcessDefine(RC, RCDemo, sb.getCurrentUser().getName(), getIncludedFileNamesString(), sparam.getMeth(), ab, sb.getCurrentUser().getHomeDir(), paramNotChanged);
            setExamplePid(pid);
            res = pid != -1;
        } else {
            // Running the real datasets
            res = RDataUtils.planProcessDefine(RC, sb.getCurrentUser().getName(), getIncludedFileNamesString(), sparam.getMeth(), ab, sb.getCurrentUser().getHomeDir());
        }

        if (ab.shouldUseScheduler()) { // deal with the case ondev: 1. run real data or 2. example data with parameters or included files changed

            String fileusrNM = "c(\"QC_PREFA02.mzML\",\"QC_PREFB02.mzML\",\"CD-9WOBP.mzML\",\"CD-6KUCT.mzML\",\"CD-77FXR.mzML\",\"CD-9OS5Y.mzML\",\"HC-9SNJ4.mzML\",\"HC-AMR37.mzML\",\"HC-9X47O.mzML\",\"HC-AUP8B.mzML\")";

            if (getIncludedFileNamesString().length() != fileusrNM.length()) {
                paramNotChanged = false;
            }

            if (executExample && paramNotChanged) {
                setCurrentJobStatus("Demo Running");
            } else if (res) {
                String JobSubmission = "sbatch " + sb.getCurrentUser().getHomeDir() + "/ExecuteRawSpec.sh";
                int JobID = 0;
                try {
                    if (ab.isOnMainServer() || ab.isOnQiangPc() || ab.isInDocker()) {
                        Process proc = Runtime.getRuntime().exec(JobSubmission);
                        BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));

                        String JobString = null;
                        while ((JobString = stdInput.readLine()) != null) {
                            JobID = Integer.parseInt(JobString.replaceAll("[^0-9]", ""));
                            System.out.println("Raw spectra Job " + JobID + " has been submitted successfully !");
                        }
                    }
                } catch (Exception e) {
                    LOGGER.error("[JobSubmission: sbatch]" + JobSubmission, e);
                    //System.out.println("IOException while trying to execute [JobSubmission: sbatch]" + JobSubmission);
                }

                setCurrentJobId(JobID);
                if (JobID != 0) {
                    if (ab.isOnMainServer() || ab.isOnQiangPc() || ab.isInDocker()) {
                        String jobStatus = SchedulerUtils.getJobStatus(JobID);
                        setCurrentJobStatus(jobStatus);
                    } else {
                        String jobStatus = "Local submitted";
                        setCurrentJobStatus(jobStatus);
                    }
                }
                if (ab.isOnMainServer() || ab.isOnQiangPc()) {
                    recordJob(JobID);
                }
            }

        } else if (!executExample) {
            //TODO: activate cookie later
            //Faces.addResponseCookie("jobId", 1 + "", "localhost:8080", "/", -1);
            setCurrentJobStatus("Running");
        } else {
            setCurrentJobStatus("Demo Running");
        }

        if (ab.isOnGenap()) {
            //JobID cannot be obtained here, will be obtained later when checking the status
            File JobIDFile = new File(sb.getCurrentUser().getHomeDir() + "/JobID.txt");
            if (!JobIDFile.exists()) {
                try {
                    JobIDFile.createNewFile();
                } catch (IOException ex) {
                    LOGGER.error("performPlan-onGenAp", ex);
                    //Logger.getLogger(SpectraControlBean.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            //update projects status as submitted
            ProjectBean pb = (ProjectBean) DataUtils.findBean("projectBean");
            pb.updateProjectStatus("submitted", sb.getCurrentUser().getName());
            //record Job later when doing the status check because jobID is unknown now            
        }

        setPerformedPlan(true);
        //progress bar starts
        PrimeFaces.current().executeScript("PF('pbAjax1').start()");

        setFinishedJobId(0);
        setFinishedProgress2(0.0);
        setFinishedJobStatus("Not Started");

    }

    public void cancelPlan() {

        // just call scancel to cancel a certain job ID
        if (executExample && paramNotChanged) {

            if (examplePid == -1) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Unable to cancel !"));
            }

            String cmd = "kill " + examplePid;

            try {
                Runtime.getRuntime().exec(cmd);
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Current job is cancelled!"));

                setJobSubmitted(false);
                setKilled(true);
                setStopStatusCheck(false);

            } catch (IOException e) {
                // TODO Auto-generated catch block
                LOGGER.error("cancelPlan", e);
            }
            //PrimeFaces.current().executeScript("console.log(PF('pbAjax1'))");

        } else if (ab.shouldUseScheduler()) {

            String JobKill = "scancel " + getCurrentJobId();

            try {
                Process proc = Runtime.getRuntime().exec(JobKill);
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Current job is cancelled!"));

                setKilled(true);
                setCurrentJobStatus("Killed");
                setJobSubmitted(false);
                //setStopStatusCheck(false);
            } catch (Exception e) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Unable to terminate the current job!"));
            }

            /*
            boolean res = SchedulerUtils.killJob(getCurrentJobId());
            String status = SchedulerUtils.getJobStatus(getCurrentJobId());
             */
        } else {
            if (ab.isOnGenap()) {
                FileWriter myWriter;
                try {
                    myWriter = new FileWriter(sb.getCurrentUser().getHomeDir() + "/JobKill");
                    String cd = "1";
                    myWriter.write(cd);
                    myWriter.close();
                } catch (Exception e) {
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Something seems wrong!"));
                }
            }
            setKilled(true);
            setJobSubmitted(false);
            setCurrentJobStatus("Killed");
            setStopStatusCheck(false);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Current job is cancelled!"));
        }
    }

    public void refreshJob() throws IOException {
        if (ab.shouldUseScheduler()) {
            setCurrentJobStatus("Status refreshing...");
        } else {
            if (progress2 == 100) {
                setCurrentJobStatus("Running");
            }
        }
        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        ec.redirect(((HttpServletRequest) ec.getRequest()).getRequestURI());
    }

    public void checkStatus() throws IOException {

        if (ab.isOnGenap() && currentJobId == 0) {
            //this design is used to grasp JObID from Slurm in Genap.
            currentJobId = SchedulerUtils.getJobIDAlternative();
            if (currentJobId != 0) {
                recordJob(currentJobId);
            }
        }

        String jobStatus = null;
        if ("Running".equals(currentJobStatus) && count > 20) {
            if (count % 15 == 0) {
                // query status from slurm every 15 seconds when it is running & submitted 20 sec later
                jobStatus = SchedulerUtils.getJobStatus(currentJobId);
            } else {
                jobStatus = "RUNNING";
            }
        } else {
            // In other cases, keep querying until failed or finished
            jobStatus = SchedulerUtils.getJobStatus(currentJobId);
        }

        double jobProgress = getProgress2();

        if (jobProgress == 0) {
            currentJobStatus = "Pending";
        } else if (jobProgress == 100 || jobStatus.equals("COMPLETED")) {
            currentJobStatus = "Finished";
        } else if ("FAILED".equals(jobStatus)) {
            currentJobStatus = "Failed";
        } else if ("OUT_OF_ME+".equals(jobStatus)) {
            currentJobStatus = "Failed: Out OF MEMORY";
        } else {
            if (!isKilled() || "RUNNING".equals(jobStatus)) {
                currentJobStatus = "Running";
            } else if (isKilled() || "CANCELLED".equals(jobStatus)) {
                currentJobStatus = "Killed";
            } else {
                currentJobStatus = "Suspending";
            }
        }

        //setCurrentJobStatus(currentJobStatus);
        if (executExample && paramNotChanged) {
            setStopStatusCheck(true);
        } else if (ab.shouldUseScheduler() || ab.isOnGenap()) {

            if (currentJobStatus.contains("Failed")) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Processing Faild, unable to finish the job!"));
                setStopStatusCheck(false);
            }

            if (currentJobStatus.equals("Killed") || currentJobStatus.equals("Finished")) {
                setStopStatusCheck(false);
            }

        } else {
            setStopStatusCheck(true);
        }
    }

    public String finishProgress() throws FileNotFoundException {

        if (ab.shouldUseScheduler() && !(executExample && paramNotChanged)) {

            String jobStatus = getCurrentJobStatus();

            if (jobStatus.equals("Finished") && progress2 == 100) {

                SpectraProcessBean spb = (SpectraProcessBean) DataUtils.findBean("spectraProcessor");
                if (!spb.populateRawResBeans()) {
                    return null;
                }
                if (!spb.populateRawFeatureBeans()) {
                    return null;
                }

                spb.internalizeRes(0);
                if (!spb.summarizeProcessingMsg()) {
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Job is not totally ready yet! Please try again now."));
                    return null;
                }
                return "Spectra result";
            } else {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Job is not completed yet! Please wait until the Job Status becomes Finished."));
                return null;
            }

        } else {
            /*
            if (!getCurrentJobStatus().contains("Finished") && progress2 != 100) {
                DataUtils.updateMsg("Error", "Please wait until the job is finished!");
                return null;
            }
*/
            SpectraProcessBean spb = (SpectraProcessBean) DataUtils.findBean("spectraProcessor");
            if (!spb.populateRawResBeans()) {
                return null;
            }
            if (!spb.populateRawFeatureBeans()) {
                return null;
            }
            spb.internalizeRes(0);
            spb.summarizeProcessingMsg();
            return "Spectra result";
        }

    }

    // Section 2 : Job ID & pid section -------------------    
    private int examplePid = -1;

    public int getExamplePid() {
        return examplePid;
    }

    public void setExamplePid(int examplePid) {
        this.examplePid = examplePid;
    }

    private long currentJobId = 0;

    public long getCurrentJobId() {
        return currentJobId;
    }

    public void setCurrentJobId(long currentJobId) {
        this.currentJobId = currentJobId;
    }

    private long finishedJobId = 0;

    public long getFinishedJobId() {
        return finishedJobId;
    }

    public void setFinishedJobId(long finishedJobId) {
        this.finishedJobId = finishedJobId;
    }

    // Section 3 : Job status section -------------------  
    public String goToJobStatus() {

        if (!checkJobRunning()) {
            //update peak params
            updatePeakParam();
            //forcedly zero progress log
            Writer wr;
            try {
                wr = new FileWriter(sb.getCurrentUser().getHomeDir() + "/log_progress.txt");
                wr.write("0");
                wr.close();
            } catch (IOException ex) {
                LOGGER.error("goToJobStatus", ex);
                //  Logger.getLogger(SpectraControlBean.class.getName()).log(Level.SEVERE, null, ex);
            }

            //Initialize the controller status
            setStopStatusCheck(true);
            setJobSubmitted(false);
            setPerformedPlan(false);
            setKilled(false);
            setCurrentJobId(0);
            setCurrentJobStatus("Submitting...");
            setProgress2(0.0);
            setCreatedShareLink(false);
            //return to the job status web page
            return "Job status";
        } else {
            PrimeFaces.current().executeScript("PF('uploadSessionDialog').show()");
            return "";
        }
    }

    // KIll job on session destroy
    @PreDestroy
    public void onSessionLeaveJobKill() {
        if (!isCreatedShareLink() && !sb.isRegisteredLogin()) {
            long id = getCurrentJobId();
            if (id == 0) {
                return;
            }
            String status = SchedulerUtils.getJobStatus(id);
            if (status.equals("Pending") || status.equals("Running")) {
                if (ab.isOnGenap()) {
                    FileWriter myWriter;
                    try {
                        myWriter = new FileWriter(sb.getCurrentUser().getHomeDir() + "/JobKill");
                        String cd = "1";
                        myWriter.write(cd);
                        myWriter.close();
                    } catch (Exception e) {
                        LOGGER.error("onSessionLeaveJobKill", e);
                    }
                } else {
                    SchedulerUtils.killJob(getCurrentJobId());
                }
            }
        }
    }

    private String currentJobStatus = "Submitting...";

    public String getCurrentJobStatus() {
        return currentJobStatus;
    }

    public void setCurrentJobStatus(String currentJobStatus) {
        this.currentJobStatus = currentJobStatus;
    }

    private boolean stopStatusCheck = true;

    public boolean isStopStatusCheck() {
        return stopStatusCheck;
    }

    public void setStopStatusCheck(boolean stopStatusCheck) {
        this.stopStatusCheck = stopStatusCheck;
    }

    private boolean Killed = false;

    public boolean isKilled() {
        return Killed;
    }

    public void setKilled(boolean Killed) {
        this.Killed = Killed;
    }

    private boolean jobSubmitted = false;

    public boolean isJobSubmitted() {
        return jobSubmitted;
    }

    public void setJobSubmitted(boolean jobSubmitted) {
        this.jobSubmitted = jobSubmitted;
    }

    private String finishedJobStatus = "Not Started";

    public String getFinishedJobStatus() {
        return finishedJobStatus;
    }

    public void setFinishedJobStatus(String finishedJobStatus) {
        this.finishedJobStatus = finishedJobStatus;
    }

    // Section 4 : Job priority section -------------------
    private String currentJobPriority = "Level 1";

    public String getCurrentJobPriority() {
        return currentJobPriority;
    }

    public void setCurrentJobPriority(String currentJobPriority) {
        this.currentJobPriority = currentJobPriority;
    }

    // Section 5 : Job progress section -------------------
    private Double progress2 = 0.0;

    public Double getProgressProcessing() {
        Double myPro = updateProgress();

        if (myPro == null) {
            progress2 = 0.0;
            return progress2;
        }

        if (myPro > progress2) {
            progress2 = myPro;
        }

        if (progress2 == 100) {
            if (ab.shouldUseScheduler()) {
                setCurrentJobStatus("Finished");
            } else {
                setCurrentJobStatus("Finished");
                setJobSubmitted(false);
            }
            setStopStatusCheck(false);
            setFinishedJobId(currentJobId);
            setFinishedProgress2(progress2);
            setFinishedJobStatus(currentJobStatus);
            if (ab.isOnMainServer() || ab.isOnQiangPc()) {
                updateJobStatus(currentJobId);
            }
        }
        return progress2;
    }

    public Double updateProgress() {

        try {
            File myObj = new File(sb.getCurrentUser().getHomeDir() + "/log_progress.txt");
            if (!myObj.exists()) {
                return 0.0;
            }
            int i = 0;
            try (Scanner myReader = new Scanner(myObj)) {
                while (myReader.hasNextLine()) {
                    i++;
                    if(i>10){//to avoid infinite scanning
                        break;
                    }
                    double data = myReader.nextDouble();
                    return data;
                }
            }
        } catch (FileNotFoundException e) {
            //LOGGER.error("updateProgress", e);
            System.out.println("An error occurred: Progress Log FILE NOT found " + sb.getCurrentUser().getHomeDir() + "/log_progress.txt");
        }
        return null;
    }

    public void setProgress2(Double progress2) {
        this.progress2 = progress2;
    }

    public Double getProgress2() {
        return progress2;
    }

    private double finishedProgress2 = 100.0;

    public double getFinishedProgress2() {
        return finishedProgress2;
    }

    public void setFinishedProgress2(double finishedProgress2) {
        this.finishedProgress2 = finishedProgress2;
    }

    // Section 6 : sample number section -------------------    
    private int totalNumberOfSamples = 0;

    public int getTotalNumberOfSamples() {
        return totalNumberOfSamples;
    }

    public void setTotalNumberOfSamples(int totalNumberOfSamples) {
        this.totalNumberOfSamples = totalNumberOfSamples;
    }

    private String includedFileNamesString = "";

    public String getIncludedFileNamesString() {
        return includedFileNamesString;
    }

    public void setIncludedFileNamesString(String includedFileNamesString) {
        this.includedFileNamesString = includedFileNamesString;
    }

    /// Section 7 : Other utils section ---------------
    // data confirmed or not
    private boolean dataConfirmed = false;

    public boolean isDataConfirmed() {
        return dataConfirmed;
    }

    public void setDataConfirmed(boolean dataConfirmed) {
        this.dataConfirmed = dataConfirmed;
    }

    // job perform or not
    private boolean performedPlan = false;

    public boolean isPerformedPlan() {
        return performedPlan;
    }

    public void setPerformedPlan(boolean performedPlan) {
        this.performedPlan = performedPlan;
    }

    // use example or not
    private boolean executExample = false;

    public boolean isExecutExample() {
        return executExample;
    }

    public void setExecutExample(boolean executExample) {
        this.executExample = executExample;
    }

    // parameter changed or not
    private boolean paramNotChanged = true;

    public boolean getParamNotChanged() {
        return paramNotChanged;
    }

    public void setParamCNothanged(boolean paramNotChanged) {
        this.paramNotChanged = paramNotChanged;
    }

    // Metadata is right or not
    private boolean metaOk = false;

    public boolean isMetaOk() {
        return metaOk;
    }

    public void setMetaOk(boolean metaOk) {
        this.metaOk = metaOk;
    }

    public String timeStamp() {

        String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        return timeStamp;
    }

    //to prevent multiple uploads in one session/browser
    public boolean checkJobRunning() {

        if (sb.isRegisteredLogin()) {
            return false;
        }

        String job = Faces.getRequestCookie("jobId");
        long jobId;
        if (job == null) {
            jobId = getCurrentJobId();
            //PrimeFaces.current().executeScript("console.log(000000000000000)");
        } else {
            jobId = Long.parseLong(job);
            //PrimeFaces.current().executeScript("console.log(111111111111111)");
        }

        String status = "";
        if (ab.shouldUseScheduler()) {
            if (jobId == 0) {
                status = "Submitting...";
            } else {
                status = SchedulerUtils.getJobStatus(jobId);
            }
        } else {
            if (getCurrentJobStatus().equals("Submitting...")) {
                status = "Submitting...";
            } else {
                double progress = 0.0;
                progress = getProgressProcessing();
                if (progress == 100) {
                    status = "Local Finished";
                } else if (progress > 0) {
                    status = "Running";
                } else if (progress == 0) {
                    status = "Submitting...";
                }
            }
        }

        return (status.contains("Running") || status.contains("Pending"));
    }

    public void killCurrentJob() {
        long id = getCurrentJobId();
        if (id == 0) {
            DataUtils.updateMsg("Error", "Unable to delete running job!");
            return;
        }
        String status = SchedulerUtils.getJobStatus(id);
        if (status.equals("Pending") || status.equals("Running")) {
            boolean res = SchedulerUtils.killJob(getCurrentJobId());
            if (res) {
                setKilled(true);
                setCurrentJobStatus("Killed");
                setJobSubmitted(false);
                DataUtils.updateMsg("Info", "Job (" + id + ") has been successfully terminated!");
            } else {
                DataUtils.updateMsg("Error", "Unable to delete running job!");
            }
        }
    }

    /// Section 8 : Parameters updating section ---------------
    // update parameters for processing
    public void updatePeakParam() {

        RConnection RC = sb.getRConnection();

        RDataUtils.updateParams(RC, sparam.getPeakmeth(), sparam.getRtmeth(), sparam.getPolarity(),
                sparam.getPpm(), sparam.getMin_peakwidth(), sparam.getMax_peakwidth(), sparam.getMzdiff(),
                sparam.getSnthresh(), sparam.getNoise(), sparam.getPrefilter(), sparam.getValue_of_prefilter(),
                sparam.getBw(), sparam.getMinFraction(), sparam.getMinSamples(),
                sparam.getMaxFeatures(), sparam.getIntegrate(), sparam.getExtra(), sparam.getSpan(),
                sparam.getProfStep(), sparam.getFwhm(), sparam.getSigma(), sparam.getSteps(),
                sparam.getMax(), sparam.getFwhmThresh(), sparam.getMzmabsmiso(),
                sparam.getMax_charge(), sparam.getMax_iso(), sparam.getCorr_eic_th(),
                sparam.getMz_abs_add(), sparam.getAdducts(), sparam.isRmConts(), sparam.isBlksub());
    }

    /// Section 9 : mysql db section
    // Initialize mysql connection
    private MariaDBController mdbb;

    public boolean MysqlDBAvailabilityCheck() throws Exception {

        if (ab.isOnNewServer()) {
            // For develping purpose
            mdbb = new MariaDBController("jdbc:mysql://xxxxxx:3306/xxxxxx", "xxxxxx", "xxxxxx");
        } else if (ab.isOnQiangPc()) {
            mdbb = new MariaDBController("jdbc:mysql://xxxxxx:3306/xxxxxx", "xxxxxx", "xxxxxx");
            //return true;
        } else if (ab.isOnMainServer()) {
            mdbb = new MariaDBController("jdbc:mysql://xxxxxx:3306/xxxxxx", "xxxxxx", "xxxxxx");
            //mdbb = new MariaDBController("jdbc:mysql://xxxxxx:3306/xxxxxx", "xxxxxx", "xxxxxx");
        } else if (ab.isInDocker()) {
            return(false);
        } else {
            //For real application (dev + genap public)
            mdbb = new MariaDBController("jdbc:mysql://xxxxxx:3306/xxxxxx", "xxxxxx", "xxxxxx");
        }

        boolean res = mdbb.connect();
        boolean res2 = mdbb.disconnect();
        System.out.println("MysqlDBAvailabilityCheck result: (double true is working)" + res + "|" + res2);
        return res;

    }

    private void recordJob(long JobID) {

        String recordQuery;

        if (sb.isRegisteredLogin()) {
            // record for prjects module (project --> guestfolder <-- jobs)

            ProjectBean pb = (ProjectBean) DataUtils.findBean("projectBean");
            UserLoginBean ulb = pb.getUlb();

            long userNM = ulb.getUserNM();
            Date CurrentDate = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String CurDate = sdf.format(CurrentDate);
            String proFolder = pb.getSelectedProject().getFolder();
            String JobPos = "dev";
            if (ab.isOnMainServer()) {
                JobPos = "dev";
            } else if (ab.isOnQiangPc()) {
                JobPos = "qiang";
            } else if (ab.isOnGenap()) {
                JobPos = "genap";
            } else {
                JobPos = "unknown";
            }

            MariaDBController mdbbIm = pb.getMdbb();

            recordQuery = "insert into devUsers.jobs (userNM, jobID, jobSubTime, jobStatus, jobFolder, jobPosition, jobType) values ("
                    + userNM + ","
                    + JobID + ",'"
                    + CurDate + "','"
                    + "submitted" + "','"
                    + proFolder + "','"
                    + JobPos + "','"
                    + "mass');";

            String updateQuery = "update devUsers.projects set status = 'submitted' where projectFolderNM = '"
                    + proFolder
                    + "' AND userNM = "
                    + userNM + ";";

            try {
                mdbbIm.runUpdate(recordQuery);
                mdbbIm.runUpdate(updateQuery);
                DataUtils.showMessage(FacesMessage.SEVERITY_INFO, "OK", "Job saved and updated successfully!");
            } catch (SQLException ex) {
                DataUtils.showMessage(FacesMessage.SEVERITY_ERROR, "Internal error!",
                        "Job Saving server is down, report this code: xxj00001 to administrator !");
            }

        } else {
            // record for partial link or traffic stats
            try {
                boolean dbAviRes = MysqlDBAvailabilityCheck();
                if (!dbAviRes) {
                    DataUtils.showMessage(FacesMessage.SEVERITY_ERROR, "Internal error!",
                            "Job Saving server is down, report this code: xxj00002 to administrator !");
                }
            } catch (Exception ex) {
                DataUtils.showMessage(FacesMessage.SEVERITY_ERROR, "Internal error!",
                        "Job Saving server is down, report this code: xxj00003 to administrator !");
            }

            long userNM = -1;
            Date CurrentDate = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String CurDate = sdf.format(CurrentDate);
            String proFolder = sb.getCurrentUser().getName();
            String JobPos = "dev";
            if (ab.isOnMainServer()) {
                JobPos = "dev";
            } else if (ab.isOnGenap()) {
                JobPos = "genap";
            } else if (ab.isOnQiangPc()) {
                JobPos = "qiang";
            } else {
                JobPos = "unk";
            }

            recordQuery = "insert into devUsers.jobs (userNM, jobID, jobSubTime, jobStatus, jobFolder, jobPosition, jobType) values ("
                    + userNM + ","
                    + JobID + ",'"
                    + CurDate + "','"
                    + "submitted" + "','"
                    + proFolder + "','"
                    + JobPos + "','"
                    + "mass');";

            try {
                mdbb.runUpdate(recordQuery);
                DataUtils.showMessage(FacesMessage.SEVERITY_INFO, "OK", "Job saved successfully!");
            } catch (SQLException ex) {
                DataUtils.showMessage(FacesMessage.SEVERITY_ERROR, "Internal error!",
                        "Job Saving server is down, report this code: xxj00009 to administrator !");
            }
        }
    }

    private void updateJobStatus(long JobID) {

        String JobPos = "dev";
        if (ab.isOnMainServer()) {
            JobPos = "dev";
        } else if (ab.isOnGenap()) {
            JobPos = "genap";
        } else if (ab.isOnQiangPc()) {
            JobPos = "qiang";
        } else {
            JobPos = "unk";
        }

        if (JobID > 0) {

            ProjectBean pb = (ProjectBean) DataUtils.findBean("projectBean");
            MariaDBController mdbbIm = pb.getMdbb();

            String updateQuery = "update devUsers.jobs set jobStatus = '"
                    + currentJobStatus
                    + "' where jobID = "
                    + JobID
                    + " AND jobPosition = '"
                    + JobPos + "';";

            try {
                mdbbIm.runUpdate(updateQuery);
            } catch (Exception ex) {
                DataUtils.showMessage(FacesMessage.SEVERITY_ERROR, "Internal error!",
                        "Job Saving server is down, report this code: xxj00004 to administrator !");
            }
        }

        if (sb.isRegisteredLogin()) {
            updateProjectStatus(sb.getCurrentUser().getName());
        }
    }

    private void updateProjectStatus(String guestFolder) {

        if (guestFolder != null) {

            String dataPlace = "dev";
            if (ab.isOnMainServer()) {
                dataPlace = "dev";
            } else if (ab.isOnQiangPc()) {
                dataPlace = "qiang";
            } else if (ab.isOnGenap()) {
                dataPlace = "genap";
            } else {
                dataPlace = "unk";
            }

            ProjectBean pb = (ProjectBean) DataUtils.findBean("projectBean");
            MariaDBController mdbbIm = pb.getMdbb();

            String updateQuery = "update devUsers.projects set status = '"
                    + currentJobStatus
                    + "' where projectFolderNM = '"
                    + guestFolder
                    + "' AND dataplace= '"
                    + dataPlace + "';";

            try {
                mdbbIm.runUpdate(updateQuery);
            } catch (Exception ex) {
                DataUtils.showMessage(FacesMessage.SEVERITY_ERROR, "Internal error!",
                        "Project Saving server is down, report this code: xxp00014 to administrator !");
            }
        }
    }

    public void updateJobPartialLink() {

        if (currentJobId == 0) {
            DataUtils.showMessage(FacesMessage.SEVERITY_ERROR, "Save example as a URL job is not allowed!", "");
        } else {

            try {
                boolean dbAviRes = MysqlDBAvailabilityCheck();
                if (!dbAviRes) {
                    DataUtils.showMessage(FacesMessage.SEVERITY_ERROR, "Internal error!",
                            "Job Saving server is down, report this code: xxj00002 to administrator !");
                }
            } catch (Exception ex) {
                DataUtils.showMessage(FacesMessage.SEVERITY_ERROR, "Internal error!",
                        "Job Saving server is down, report this code: xxj00003 to administrator !");
            }

            String dataPlace = "dev";
            if (ab.isOnMainServer()) {
                dataPlace = "dev";
            } else if (ab.isOnQiangPc()) {
                dataPlace = "qiang";
            } else if (ab.isOnGenap()) {
                dataPlace = "genap";
            } else {
                dataPlace = "unk";
            }

            String partialID = sb.getPartialId();
            String updateQuery = "update devUsers.jobs set partialLink = '"
                    + partialID
                    + "' where jobID = "
                    + currentJobId
                    + " AND jobPosition ='"
                    + dataPlace + "';";

            try {
                mdbb.runUpdate(updateQuery);
                DataUtils.showMessage(FacesMessage.SEVERITY_INFO, "OK", "Job URL created successfully!");
            } catch (SQLException ex) {
                DataUtils.showMessage(FacesMessage.SEVERITY_ERROR, "Internal error!",
                        "Job Saving server is down, report this code: xxj00019 to administrator !");
            }
        }
    }

    
    public void updateJobPartialLink_docker() {
        if (currentJobId == 0) {
            DataUtils.showMessage(FacesMessage.SEVERITY_ERROR, "Please wait until job finially submitted!", "");
        } else {
            String partialID = sb.getPartialId();
            RDataUtils.recordPartialLinkinDocker(sb.getRConnection(), partialID, (int) currentJobId, sb.getCurrentUser().getName(), timeStamp());
        }
    }

    
    public void exampleJobPartialWarning() {
        if (currentJobId == 0) {
            DataUtils.showMessage(FacesMessage.SEVERITY_ERROR, "Save example as a URL job is not allowed!", "Try this with your own data.");
        }
    }

    ///Section 10: OptiLCMS auto update controller
    //set to enable auto update
    public void OptiLCMSCheck() {
        RConnection RC = sb.getRConnection();
        String res = RDataUtils.checkOptiLCMS(RC, ab.getOptiLCMSversion());
        if (!res.equals("TRUE")) {
            System.out.println("Updating your local OPtiLCMS now....");
            //RDataUtils.updateOptiLCMS(RC);
            System.out.println("Updating finished!");
        }
    }
}
