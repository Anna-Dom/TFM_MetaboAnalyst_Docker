/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package metaboanalyst.controllers;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.inject.Named;
import javax.faces.view.ViewScoped;
import metaboanalyst.controllers.mummichog.MummiAnalBean;
import metaboanalyst.models.ResultBean;
import metaboanalyst.models.User;
import metaboanalyst.rwrappers.RCenter;
import metaboanalyst.rwrappers.RDataUtils;
import metaboanalyst.utils.DataUtils;
import org.primefaces.PrimeFaces;
import org.primefaces.model.StreamedContent;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author jianguox
 */
@ViewScoped
@Named("downloader")
public class DownloadBean implements Serializable {

    private final SessionBean1 sb = (SessionBean1) DataUtils.findBean("sessionBean1");
    private final ApplicationBean1 ab = (ApplicationBean1) DataUtils.findBean("applicationBean1");
    private static final Logger LOGGER = LogManager.getLogger(DownloadBean.class);

    private ResultBean[] downloads;

    public ResultBean[] getDownloads() {
        return downloads;
    }

    private boolean tableInit = false;

    public void setupDownloadTable() {

        if (tableInit) {
            //System.out.println("======= already init =====");
            return;
        }
        //System.out.println("======= preparing download table =====");
        User usr = sb.getCurrentUser();
        String usrName = usr.getName();
        RConnection RC = sb.getRConnection();
        RDataUtils.saveRCommands(RC);
        if (!"raw".equals(sb.getAnalType())) {
            RDataUtils.saveAllData(RC);
        }

        File folder = new File(usr.getHomeDir());
        DataUtils.deleteFile(usr, "Download.zip");
        File[] listOfFiles = folder.listFiles(new OnlyExt(true));

        if (listOfFiles.length == 0) {
            downloads = new ResultBean[1];
            downloads[0] = new ResultBean("Empty Folder", "", null, null);
        } else {
            // first create zip files
            DataUtils.createZipFile(listOfFiles,"Download.zip", usr.getHomeDir());

            ArrayList<String> fileNames = new ArrayList();
            fileNames.add("Download.zip");

            listOfFiles = folder.listFiles(new OnlyExt(false));

            for (File listOfFile : listOfFiles) {
                fileNames.add(listOfFile.getName());
            }

            reportURL = null;

            if (fileNames.contains("Rhistory.R")) {
                fileNames.remove("Rhistory.R");
                fileNames.add(1, "Rhistory.R");
            }
            int fileSize = fileNames.size();
            boolean added = false;
            if (fileSize % 2 > 0) {
                fileSize = fileSize + 1;
                added = true;
            }

            int rowNum = fileSize / 2;
            downloads = new ResultBean[rowNum];
            String fileNMA, fileNMB, fileNMBLink, fileNMALink;

            if (ab.shouldUseScheduler() && sb.getAnalType().equals("raw")) {

                for (int i = 0; i < rowNum; i++) {

                    fileNMA = fileNames.get(i);
                    fileNMALink = sb.getCurrentUser().getHomeDir() + "/" + fileNMA;
                    //fileNMA = "<a target='_blank' href = \"" + fileNMA_tmp + "\">" + fileNames.get(i) + "</a>";

                    if (i == rowNum - 1 && added) {
                        fileNMB = "";
                        fileNMBLink = null;
                    } else {
                        fileNMB = fileNames.get(rowNum + i);
                        fileNMBLink = sb.getCurrentUser().getHomeDir() + "/" + fileNMB;
                        //fileNMB = "<a target='_blank' href = \"" + fileNMB_tmp2 + "\">" + fileNames.get(rowNum + i) + "</a>";  

                    }
                    downloads[i] = new ResultBean(fileNMA, fileNMB, fileNMALink, fileNMBLink);
                }

            } else {

                for (int i = 0; i < rowNum; i++) {
                    fileNMA = "<a target='_blank' href = \"/MetaboAnalyst/resources/users/" + usrName + File.separator + fileNames.get(i) + "\">" + fileNames.get(i) + "</a>";
                    if (i == rowNum - 1 && added) {
                        fileNMB = "";
                    } else {
                        fileNMB = "<a target='_blank' href = \"/MetaboAnalyst/resources/users/" + usrName + File.separator + fileNames.get(rowNum + i) + "\">" + fileNames.get(rowNum + i) + "</a>";
                    }
                    downloads[i] = new ResultBean(fileNMA, fileNMB, "", "");
                }
            }
        }
        tableInit = true;
    }

    private String reportURL = null;

    public String getReportURL() {
        return reportURL;
    }

    public void setReportURL(String reportURL) {
        this.reportURL = reportURL;
    }

    private String mdlOpt = "stat";

    public String getMdlOpt() {
        return mdlOpt;
    }

    public void setMdlOpt(String mdlOpt) {
        this.mdlOpt = mdlOpt;
    }

    //try to redo it using bash, assuming Analysis_Report.tex exists
    public void generateReport() {

        User usr = sb.getCurrentUser();
        RConnection RC = sb.getRConnection();

        RCenter.loadReporterFuns(sb.getRConnection(), sb.getAnalType());
        setupPDFImages();
        RCenter.prepareReport(RC, usr.getName());

        //now we always use Bash external rather than RSweave (due to fatigue reason?)
        RCenter.saveCurrentSession(sb.getRConnection());
        String cmdPath = RCenter.getBashFullPath(sb.getRConnection());
        String rScriptHome = ab.getRscriptsPath();
        String userDir = usr.getHomeDir();
        boolean res = DataUtils.generateReportCMD(cmdPath, rScriptHome, userDir);

        if (res) {
            if (sb.getAnalType().equals("raw")) {
                reportURL = "<a target='_blank' href = \"/data/glassfish/projects/metaboanalyst/" + usr.getName() + "/Analysis_Report.pdf" + "\">Analysis Report</a>";
            } else {
                reportURL = "<a target='_blank' href = \"/MetaboAnalyst/resources/users/" + usr.getName() + "/Analysis_Report.pdf" + "\">Analysis Report</a>";
            }

        } else {
            reportURL = "";
            DataUtils.updateMsg("Error", "Unknown error happened during report generation.");
        }
    }

    private void setupPDFImages() {
        HashMap<String, String> graphicsMap = sb.getGraphicsMap();
        Iterator keyIt = graphicsMap.keySet().iterator();
        String key, rcmd;
        RConnection RC = sb.getRConnection();
        while (keyIt.hasNext()) {
            key = keyIt.next().toString();
            rcmd = graphicsMap.get(key);
            rcmd = rcmd.replace("png", "pdf");
            rcmd = rcmd.replace("width=NA", "width=0");
            try {
                RC.voidEval(rcmd);
            } catch (Exception e) {
                //e.printStackTrace();
                LOGGER.error("setupPDFImages", e);
            }
        }
    }

    public StreamedContent getAnalysisReport() throws IOException {
        return DataUtils.getDownloadFile(sb.getCurrentUser().getHomeDir() + "/Analysis_Report.pdf");
    }

    public class OnlyExt implements FilenameFilter {

        private boolean showAll = false;

        public OnlyExt(boolean isall) {
            showAll = isall;
        }

        @Override
        public boolean accept(File dir, String name) {
            if (showAll) {
                return name.endsWith(".csv")
                        || name.endsWith(".png")
                        || (name.endsWith(".R") & !name.endsWith("ExecuteRawSpec.R"))
                        || name.endsWith(".json")
                        || name.endsWith(".pdf");
            } else {
                return name.endsWith(".csv")
                        || name.endsWith(".png")
                        || (name.endsWith(".R") & !name.endsWith("ExecuteRawSpec.R"))
                        || name.endsWith(".json")
                        || name.endsWith("Report.pdf");
            }
        }
    }

    private final List<String> untargetedDatas = Arrays.asList("spec", "specbin", "pktable", "nmrpeak", "mspeak");
    private final List<String> regresAnals = Arrays.asList("pathway", "enrich");
    private final List<String> compatibleDatas = Arrays.asList("conc", "spec", "specbin", "pktable", "nmrpeak", "mspeak", "mass_table");
    private final List<String> compatibleAnals = Arrays.asList("stat", "roc", "raw", "power", "mummichog", "pathqea", "msetqea", "pathway", "enrich");
    private final List<String> compatibleListAnals = Arrays.asList("pathora", "msetora", "pathway", "enrich");
    private final List<String> targetedAnals = Arrays.asList("pathora", "pathqea", "msetora", "msetqea", "pathway", "enrich");

    public String switchModule() {
        //need to send to sanity check page directly 
        //prepare the new model
        sb.setSwitchMode(false);
        String dataType = sb.getDataType();
        String analType = sb.getAnalType();

        //some sanity check
        //no switching to itself
        if (analType.equals(mdlOpt)) {
            DataUtils.updateMsg("Error", "You are switching to the same module.");
            return null;
        }

        //switching to targeted analysis
        if (targetedAnals.contains(mdlOpt)) {
            //cannot from untargeted data
            if (untargetedDatas.contains(dataType)) {
                DataUtils.updateMsg("Error", "This module requires annotated metabolomics data");
                return null;
                //others need to do name mapping first
            } else if (sb.getCmpdIDType().equals("na")) {
                //DataUtils.updateMsg("Error", "Please specify compound ID type to enter this module.");
                PrimeFaces.current().executeScript("PF('idDialog').show();");
                return null;
            }
        }
        //only allow spec to mummichog
        boolean allowUsemumi = false;
        if (mdlOpt.equals("mummichog") & !dataType.equals("spec")) {
            if (!RDataUtils.checkDataGenericFormat(sb.getRConnection())) {
                DataUtils.updateMsg("Error", "Due to its requirements on RT and m/z infomation, you can only switch to Functional Analysis from Raw Spectral Processing module.");
                return null;
            } else {
                //now allow other module if data format is generic (e.g. feature is mz__rt)
                allowUsemumi = true;
            }
        }

        //dealing with regression
        if (sb.isRegresion() & !regresAnals.contains(mdlOpt)) {
            DataUtils.updateMsg("Error", "This module does not support continuous class labels.");
            return null;
        }

        //between two input compound lists. Note the module for pathway and enrich cannot distingusih ora or qea. It will
        //depends on the current analtype
        String newAnalOpt = mdlOpt;
        String newDataType = dataType;
        String treeOpt = mdlOpt;
        String naviCode;

        //list input
        if (dataType.equals("conc") && compatibleListAnals.contains(analType) && compatibleListAnals.contains(mdlOpt)) {

            if (analType.equals("msetora")) {
                newAnalOpt = "pathora";
                treeOpt = "pathway";
                naviCode = "pathparam";
            } else {
                newAnalOpt = "msetora";
                treeOpt = "enrich";
                naviCode = "enrichparam";
            }
            prepData(newDataType, newAnalOpt, treeOpt);
            return naviCode;

            //table input
        } else if (compatibleDatas.contains(dataType) && compatibleAnals.contains(analType) && compatibleAnals.contains(mdlOpt)) {

            //from raw spec, need to update something
            if (dataType.equals("spec")) {

                if (ab.isOnMainServer() || ab.isOnQiangPc()) {
                    //update the user dir to be under web application
                    User user = sb.getCurrentUser();
                    String guestName = user.getName();
                    String myDir = ab.getRealUserHomePath() + guestName;

                    File guestFolder = new File(myDir);
                    if (!guestFolder.exists()) {
                        guestFolder.mkdir();
                    }

                    user.setRelativeDir("/resources/users/" + guestName);
                    user.setHomeDir(myDir);

                    //copy the "metaboanalyst_input.csv" to the new user folder?    
                    String fileName = "metaboanalyst_input.csv";
                    String inPath = "/data/glassfish/projects/metaboanalyst/" + guestName + "/" + fileName;
                    String currentPath = sb.getCurrentUser().getHomeDir() + "/" + fileName;
                    DataUtils.copyFile(new File(inPath), new File(currentPath));

                    fileName = "params.rda"; //required for mummichog
                    inPath = "/data/glassfish/projects/metaboanalyst/" + guestName + "/" + fileName;
                    currentPath = sb.getCurrentUser().getHomeDir() + "/" + fileName;
                    DataUtils.copyFile(new File(inPath), new File(currentPath));

                    //set R working dirctory to new guestfolder
                    try {
                        sb.getRConnection().voidEval("setwd(\"" + sb.getCurrentUser().getHomeDir() + "\")");
                    } catch (RserveException ex) {
                        LOGGER.error("switchModule", ex);
                    }
                }

                //setup the mSet object
                RDataUtils.prepareSpec4Switch(sb.getRConnection());

                //update new data type
                if (newAnalOpt.equals("mummichog")) {
                    //update the data type
                    newDataType = "mass_table";
                    treeOpt = "mummichgo-table";
                    //mummi specif updates
                    MummiAnalBean mb = (MummiAnalBean) DataUtils.findBean("mummiAnalBean");
                    mb.setDisabledV2(true);
                    mb.setModuleSwitch(true);
                } else {
                    newDataType = "pktable";
                }
            }

            if (allowUsemumi && newAnalOpt.equals("mummichog")) {
                //update the data type
                newDataType = "mass_table";
                //mummi specif updates
                MummiAnalBean mb = (MummiAnalBean) DataUtils.findBean("mummiAnalBean");
                mb.setDisabledV2(true);
                mb.setModuleSwitch(true);
            }

            if (mdlOpt.equals("pathway")) {
                newAnalOpt = "pathqea";
            } else if (mdlOpt.equals("enrich")) {
                newAnalOpt = "msetqea";
            }

            //System.out.println("=====Data type:" + dataType + "======Anal Type:" + analType + "=========Switch to: " + mdlOpt + "+" + newAnalOpt);
            sb.setSwitchMode(true);
            prepData(newDataType, newAnalOpt, treeOpt);
            return "Data check";

        } else {
            //System.out.println("=====Data type:" + dataType + "======Anal Type:" + analType + "=========Switch to: " + mdlOpt);
            DataUtils.updateMsg("Error", "The two modules are not compatible due to different input requirements.");
            return null;
        }
    }

// String TmpMSModeOpt = "negative";
    private void prepData(String newDataType, String newAnalType, String treeType) {
        sb.setDataUploaded();
        sb.setDataProcessed(true);
        sb.setDataType(newDataType);
        sb.setAnalType(newAnalType);
        sb.setPaired(false);
        sb.setRegression(false);
        sb.getTraceTrack().clear();
        sb.initNaviTree(treeType);

        RConnection newRC = RCenter.updateRConnection(sb.getRConnection(), RCenter.getCleanRConnection(), ab.getRscriptLoaderPath(), newAnalType, sb.getCurrentUser().getHomeDir());
        RCenter.recordRserveConnection(newRC, sb.getCurrentUser().getHomeDir());
        sb.setRConnetion(newRC);

        RDataUtils.updateDataObjects(newRC, newDataType, newAnalType, false);
    }
}
