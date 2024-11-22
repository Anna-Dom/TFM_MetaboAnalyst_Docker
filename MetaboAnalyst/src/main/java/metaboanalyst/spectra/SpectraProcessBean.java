/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package metaboanalyst.spectra;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.model.ListDataModel;
import javax.inject.Named;
import metaboanalyst.controllers.ApplicationBean1;
import metaboanalyst.controllers.SessionBean1;
import metaboanalyst.models.MetSetBean;
import metaboanalyst.models.RawFeatureBean;
import metaboanalyst.models.RawResBean;
import metaboanalyst.models.SpecBean;
import metaboanalyst.rwrappers.RCenter;
import metaboanalyst.rwrappers.RDataUtils;
import metaboanalyst.rwrappers.RSpectraUtils;
import metaboanalyst.utils.DataUtils;
import org.primefaces.model.StreamedContent;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.Rserve.RConnection;

/**
 *
 * @author qiang
 */
@SessionScoped
@Named("spectraProcessor")
public class SpectraProcessBean implements Serializable {

    private final SpectraControlBean sc = (SpectraControlBean) DataUtils.findBean("spectraController");
    private final SessionBean1 sb = (SessionBean1) DataUtils.findBean("sessionBean1");
    private final SpectraParamBean sparam = (SpectraParamBean) DataUtils.findBean("spectraParamer");
    private final ApplicationBean1 ab = (ApplicationBean1) DataUtils.findBean("applicationBean1");

    // Section 1 : Plotting function -----------------------
    private int figureCount = 0;

    public int getFigureCount() {
        return figureCount;
    }

    public void setFigureCount(int figureCount) {
        this.figureCount = figureCount;
    }

    /// 1.1 TIC -singal
    private String ticName;

    public String getTicName() {
        return ticName;
    }

    public void setTicName(String ticName) {
        this.ticName = ticName;
    }

    public void plotSingleTIC() {
        RSpectraUtils.plotSingleTIC(sb.getRConnection(), ticName, -1, "png", 72, "NA");
    }

    public String plotTIC() {
        String imageName = RSpectraUtils.plotSingleTIC(
                sb.getRConnection(),
                ticName,
                figureCount,
                sb.getFormatOpt(),
                sb.getDpiOpt(),
                sb.getSizeOpt());
        return imageName;
    }

    public String getTicImgName() {
        return ticName + ".png";
    }

    /// 1.2 XIC
    private int featureNum;
    private int featureNum0;
    private boolean showlabel = true;

    public int getFeatureNum() {
        return featureNum;
    }

    public void setFeatureNum(int featureNum) {
        this.featureNum = featureNum;
    }

    public int getFeatureNum0() {
        return featureNum0;
    }

    public void setFeatureNum0(int featureNum0) {
        setFeatureNum(RSpectraUtils.convertFTno2Num(sb.getRConnection(), featureNum0));
    }

    public boolean isShowlabel() {
        return showlabel;
    }

    public void setShowlabel(boolean showlabel) {
        this.showlabel = showlabel;
    }

    /// plot summary of the single feature
    public String plotMSfeature(String type) {
        if (type.equals("png")) {
            featureimageNM = RSpectraUtils.plotMSfeature(sb.getRConnection(), featureNum, "png", 72, "NA");
            internalizeImage(featureimageNM);
            return featureimageNM;
        } else if (type.equals("svg")) {
            svgStringBox = RSpectraUtils.plotMSfeature(sb.getRConnection(), featureNum, "svg2", 72, "NA");
            internalizeEIC(featureNum);
            return featureNum + "";
        } else {
            return ""; //return nothing for now. TODO: to optimize this point.
        }
    }

    public String plotMSfeatureUpdate() {
        String featureimageNew = RSpectraUtils.plotMSfeature(
                sb.getRConnection(),
                featureNum,
                sb.getFormatOpt(),
                sb.getDpiOpt(),
                sb.getSizeOpt());
        internalizeImage(featureimageNew);
        return featureimageNew;
    }

    private String featureimageNM;

    public String getFeatureimageNM() {
        return featureimageNM;
    }

    public void setFeatureimageNM(String featureimageNM) {
        this.featureimageNM = featureimageNM;
    }

    private String XICSImgName;
    private String XICGImgName;

    public String getXICGImgName() {
        return XICGImgName;
    }

    public void setXICGImgName(String XICGImgName) {
        this.XICGImgName = XICGImgName;
    }

    public String getXICSImgName() {
        return XICSImgName;
    }

    public void setXICSImgName(String XICSImgName) {
        this.XICSImgName = XICSImgName;
    }

    public void plotSingleXIC() {
        String featureXICTitle = RSpectraUtils.plotXIC(sb.getRConnection(),
                featureNum,
                "png",
                72,
                "NA");
        this.XICSImgName = "EIC_" + featureXICTitle + "_sample_72.png";
        XICGImgName = "EIC_" + featureXICTitle + "_group_72.png";
    }

    public String plotXICUpdate() {
        int dpi = 72;
        if (sb.getFormatOpt().equals("png")) {
            dpi = sb.getDpiOpt();
        }
        String featureNAME = RSpectraUtils.plotXIC(sb.getRConnection(),
                featureNum,
                sb.getFormatOpt(),
                dpi,
                sb.getSizeOpt());
        return featureNAME;
    }

    /// 1.3 PCA -3D Json File
    String featureNM = "";

    public void setFeatureNM(String featureNM) {
        this.featureNM = featureNM;
    }

    public String getScoreJson() {
        return "/resources/users/" + sb.getCurrentUser().getName() + File.separator + "spectra_3d_score" + featureNM + ".json";
    }

    public String getLoadingJson() {
        return "/resources/users/" + sb.getCurrentUser().getName() + File.separator + "spectra_3d_loading" + featureNM + ".json";
    }

    /// 1.4 Plotting TIC - multiple
    public String plotTICs() {
        String imageName = RSpectraUtils.plotTICs(
                sb.getRConnection(),
                figureCount,
                sb.getFormatOpt(),
                sb.getDpiOpt(),
                sb.getSizeOpt());
        return imageName;
    }

    /// 1.5 Ploting BPI - multiple
    public String plotBPIs() {
        String imageName = RSpectraUtils.plotBPIs(
                sb.getRConnection(),
                figureCount,
                sb.getFormatOpt(),
                sb.getDpiOpt(),
                sb.getSizeOpt());
        return imageName;
    }

    /// 1.6 Ploting RTcor
    public String plotRTcor() {
        String imageName = RSpectraUtils.plotRTcor(
                sb.getRConnection(),
                figureCount,
                sb.getFormatOpt(),
                sb.getDpiOpt(),
                sb.getSizeOpt());
        return imageName;
    }

    /// 1.7 Plotting BPI - corrected
    public String plotBPIcor() {
        String imageName = RSpectraUtils.plotBPIcor(
                sb.getRConnection(),
                figureCount,
                sb.getFormatOpt(),
                sb.getDpiOpt(),
                sb.getSizeOpt());
        return imageName;
    }

    /// 1.8 Plotting Intensity Stats
    public String plotIntenStats() {
        String imageName = RSpectraUtils.plotIntenStats(
                sb.getRConnection(),
                figureCount,
                sb.getFormatOpt(),
                sb.getDpiOpt(),
                sb.getSizeOpt());
        return imageName;
    }

    // Section 2 : Table Model function -----------------------
    private List<SpecBean> specBeans;

    private List<String> spectraFiles;

    public List<String> getSpectraFiles() {
        return spectraFiles;
    }

    public void setSpectraFiles(List<String> spectraFiles) {
        this.spectraFiles = spectraFiles;
    }

    private int centroidFileCount = 0;

    public int getCentroidFileCount() {
        return centroidFileCount;
    }

    public void populateSpecBeans() throws REXPMismatchException {
        specBeans = new ArrayList<>();

        RConnection RC = sb.getRConnection();
        String[] groupnames;

        // Creates a new File instance by converting the given pathname string
        // into an abstract pathname
        String homeDir = sb.getCurrentUser().getHomeDir();
        if (sc.isExecutExample()) {
            if (Files.isDirectory(Paths.get("/Users/xia/Dropbox/"))) {
                homeDir = "/Users/xia/Dropbox/Current/Test/MetaboDemoRawData/"; //xia office
            } else if (Files.isDirectory(Paths.get("/Users/jeffxia/Dropbox/"))) {
                homeDir = "/Users/jeffxia/Dropbox/Current/Test/MetaboDemoRawData/"; //xia laptop
            } else { //public server
                homeDir = "/home/glassfish/projects/MetaboDemoRawData/";
            }
        }

        File f;
        f = new File(homeDir + File.separator + "upload");

        // Populates the array with names of files and directories
        groupnames = f.list();

        // For each pathname in the pathnames array
        String rCommand = "CentroidCheck(\'" + "REPLACE_WITH_YOUR_SINGLE_FILE_PATH" + "\')";
        RCenter.recordRCommand(RC, rCommand);

        spectraFiles = new ArrayList<>();
        centroidFileCount = 0;

        for (String groupname : groupnames) {
            File grp = new File(homeDir + File.separator + "upload/" + groupname);
            if (grp.isFile()) {
                continue;
            }
            String[] spectranames = grp.list();
            for (String spectraname : spectranames) {
                File spec = new File(homeDir + "/upload/" + groupname + "/" + spectraname);
                String fileSize = humanReadableByteCountBin(spec.length());
                String filepath = homeDir + "/upload/" + groupname + "/" + spectraname;

                spectraFiles.add(spectraname);
                
                boolean mode = RDataUtils.checkCentroid(RC, filepath);
                boolean include = true;
                boolean disabled = false;

                String modec = null;
                if (mode) {
                    modec = "True";
                    centroidFileCount = centroidFileCount + 1;
                } else {
                    modec = "<b><font color=\"red\">False</font></b>";
                    include = false;
                    disabled = true;
                }
                specBeans.add(new SpecBean(spectraname, modec, fileSize, groupname, "intensity", include, disabled));
            }
        }

        bnDisabled = centroidFileCount <= 2;
        setSpectraFiles(spectraFiles);
    }

    public List<SpecBean> getSpecBeans() {
        return Collections.unmodifiableList(specBeans);
    }

    public void setSpecBeans(List<SpecBean> specBeans) {
        this.specBeans = specBeans;
    }

    public static String humanReadableByteCountBin(long bytes) {
        long absB = bytes == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(bytes);
        if (absB < 1024) {
            return bytes + " B";
        }
        long value = absB;
        CharacterIterator ci = new StringCharacterIterator("KMGTPE");
        for (int i = 40; i >= 0 && absB > 0xfffccccccccccccL >> i; i -= 10) {
            value >>= 10;
            ci.next();
        }
        value *= Long.signum(bytes);
        return String.format("%.1f", value / 1024.0);
        //return String.format("%.1f %ciB", value / 1024.0, ci.current());
    }

    public String prepareSpecProc() {

        ArrayList<String> incFiles = new ArrayList<>();
        ArrayList<String> groupArr = new ArrayList<>();

        Set<String> uniqueClasses = new HashSet<>();
        for (SpecBean sample : specBeans) {
            if (sample.isInclude()) {
                String grp = sample.getGroup();
                if (!grp.equals("QC") && !grp.equals("BLANK")) {
                    groupArr.add(grp);
                    uniqueClasses.add(grp);
                } else if (grp.equals("BLANK")) {
                    sparam.setButtonEnableBS(false);
                    sparam.setBlksub(true);
                }
                incFiles.add(sample.getName());
            }
        }

        for (String grp : uniqueClasses) {
            int occurrences = Collections.frequency(groupArr, grp);
            if (occurrences < 3) {
                DataUtils.updateMsg("Error", "Please make sure that there are at least three samples per group!");
                return "";
            }
        }

        RConnection RC = sb.getRConnection();
        String FilesInclusion = DataUtils.createStringVector(incFiles.toArray(new String[0]));
        RDataUtils.saveFilesInclusion(RC, FilesInclusion, incFiles.size());

        sparam.initializeAdductList();

        sc.setIncludedFileNamesString(FilesInclusion);
        sc.setDataConfirmed(true);
        return "Spectra processing";
    }

    /// Select data files as inclusions
    private SpecBean selectedData;

    public SpecBean getSelectedData() {
        return selectedData;
    }

    public void setSelectedData(SpecBean selectedData) {
        this.selectedData = selectedData;
    }

    private SpecBean selectedDataSet;

    public SpecBean getSelectedDataSet() {
        return selectedDataSet;
    }

    public void setSelectedDataSet(SpecBean dm) {

        if (!selectedDataSet.getName().equals(dm.getName())) {
            this.selectedDataSet = dm;
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "OK",
                            "Current data is: " + selectedDataSet.getName() + ", ready for analysis."));
        }
    }

    ///Following part is used to handle the results display tasks
    /// 1. show the general statistics of the processing results
    public boolean populateRawResBeans() throws FileNotFoundException {
        resBeans = new ArrayList<>();

        String[] groupnames;

        // Creates a new File instance by converting the given pathname string
        // into an abstract pathname
        String homeDir = sb.getCurrentUser().getHomeDir();
        String path = "";

        if (ab.shouldUseScheduler() && !ab.isOnQiangPc() && !ab.isOnZgyPc() && !ab.isInDocker()) {
            path = ab.getProjectsHome() + sb.getCurrentUser().getName() + File.separator;
        } else if (ab.isOnLocalServer()) {
            path = homeDir + File.separator;
        } else if (ab.isInDocker()) {
            path = homeDir + File.separator;
        }

        if (ab.isOnGenap()) {
            path = homeDir + File.separator;
        }

        if (sc.isExecutExample()) {
            path = "/home/glassfish/projects/MetaboDemoRawData/";
        }

        if (Files.isDirectory(Paths.get("/Users/xia/Dropbox/"))) {
            // Handle Jeff's local case. Never change or remove !
            path = "/Users/xia/Dropbox/Current/Test/MetaboDemoRawData/"; //xia local
        }

        File f = new File(path + "upload/");
        // Populates the array with names of files and directories
        groupnames = f.list();

        int num = 0;

        // For each pathname in the pathnames array
        for (String groupname : groupnames) {
            File grp = new File(path + "upload/" + groupname);
            if (grp.isDirectory()) {
                num = num + grp.list().length;
            } else {
                num = groupnames.length;
            }
        }

        // Read in the peak_result_summary.txt
        String[][] peaksum = new String[num][];
        int index = 0;
        File file = new File(homeDir + "/peak_result_summary.txt");
        //File file = new File("/home/qiang/peak_result_summary.txt");
        if (!file.exists()) {
            DataUtils.updateMsg("Error", "The file (peak_result_summary.txt) is not found!");
            return false;
        }

        Scanner input = new Scanner(file);

        while (input.hasNextLine() && index < peaksum.length) {
            peaksum[index] = input.nextLine().split(" "); //split returns an array
            index++;
        }

        for (String[] a : peaksum) {
            try {
                resBeans.add(new RawResBean(a[0], a[1], a[2], a[3], Integer.parseInt(a[4]), Double.parseDouble(a[5])));
            } catch (Exception e) {
                System.out.println("certain file has been omitted for processing !");
            }
        }
        return true;
    }

    /// 2. show the details of feature of spectra processing
    public boolean populateRawFeatureBeans() {

        RConnection RC = sb.getRConnection();
        String homeDir = sb.getCurrentUser().getHomeDir();
        // create a new ArrayList to store the RawFeatureBean information
        ArrayList<RawFeatureBean> RawFeatureBeans = new ArrayList<>();
        int resSumNum = RDataUtils.generatePeakList(RC, homeDir);

        if (resSumNum == -10) {

            File file0 = new File(homeDir + "/peak_feature_summary.tsv");
            int index0 = 0;
            Scanner input0 = null;

            try {
                input0 = new Scanner(file0);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(SpectraProcessBean.class.getName()).log(Level.SEVERE, null, ex);
            }

            while (input0.hasNextLine()) {
                String str = input0.nextLine().split("\t")[0];
                if (str.equals("")) {
                    break;
                }
                index0++;
            }
            resSumNum = index0 - 1;
        }

        if (!RSpectraUtils.ensureDataExits(RC, "peak_feature_summary.tsv")) {
            DataUtils.updateMsg("Error", "Results writing not finished yet, please try again!");
            return false;
        }
        if (resSumNum == -10) {
            DataUtils.updateMsg("Error", "Results populating failed, please try again!");
            return false;
        }

        int feature_num = resSumNum;
        String[][] feature_sum = new String[feature_num + 1][];

        File file = new File(homeDir + "/peak_feature_summary.tsv");
        int index = 0;
        Scanner input = null;
        try {
            input = new Scanner(file);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SpectraProcessBean.class.getName()).log(Level.SEVERE, null, ex);
        }

        while (input.hasNextLine() && index < feature_sum.length + 1) {
            feature_sum[index] = input.nextLine().split("\t"); //split returns an array
            index++;
        }

        RawFeatureBean rfb;
        for (int i = 1; i < feature_sum.length; i++) {
            rfb = new RawFeatureBean(
                    i,
                    Double.parseDouble(feature_sum[i][0]), //mz
                    Double.parseDouble(feature_sum[i][1]), //rt
                    feature_sum[i][3] + " " + feature_sum[i][2], //iso + adduct
                    feature_sum[i][4], //formula
                    feature_sum[i][5], //cmpd
                    feature_sum[i][6], //HMDB
                    Double.parseDouble(feature_sum[i][7]), //intensity
                    Double.parseDouble(feature_sum[i][8]), //p val
                    Double.parseDouble(feature_sum[i][9]), //fdr
                    Double.parseDouble(feature_sum[i][10])); //cv
            RawFeatureBeans.add(rfb);
        }

        if (feature_sum[1][8].equals("-10")) {
            singlegroup = true;
        }
        FeatureModel = new ListDataModel(RawFeatureBeans);

        return true;
    }

    private List<RawResBean> resBeans;

    public List<RawResBean> getResBeans() {
        return resBeans;
    }

    public void setResBeans(List<RawResBean> resBeans) {
        this.resBeans = resBeans;
    }

    /// 3. Used to save raw feature information
    private ListDataModel<RawFeatureBean> FeatureModel = null;

    public ListDataModel<RawFeatureBean> getRawFeatureBean() {
        return FeatureModel;
    }

    private ListDataModel<MetSetBean> fomula2cmpdSet = null;

    public ListDataModel<MetSetBean> getFomula2cmpdSet() {
        RConnection RC = sb.getRConnection();
        Iterator<RawFeatureBean> it = FeatureModel.iterator();
        int fmls_len = 0;
        String[] formus = {""};
        ArrayList<MetSetBean> MetSetBeans = new ArrayList<>();
        MetSetBean msb;
        while (it.hasNext()) {
            RawFeatureBean thisIt = it.next();
            int fno = thisIt.getFeatureNo();
            if (featureOrder == 0) {
                break;
            }
            if (fno == featureOrder) {
                String fmls = thisIt.getFormulas();
                formus = fmls.split("; ");
                fmls_len = formus.length;
                break;
            }
        }
        if (fmls_len != 0) {
            for (int i = 0; i < fmls_len; i++) {
                String cmpds = RSpectraUtils.extractHMDBCMPD(RC, formus[i], featureOrder);
                String formu = "<a href=https://pubchem.ncbi.nlm.nih.gov/#query=" + formus[i] + " target='_blank'>" + formus[i] + "</a>" + "; ";
                msb = new MetSetBean(
                        formu, cmpds, "");
                MetSetBeans.add(msb);
            }
        }

        //ArrayList<MetSetBean> libVec = new ArrayList();
        //libVec.add(new MetSetBean(details[0], details[1], ""));
        //return libVec.toArray(new MetSetBean[0]);
        fomula2cmpdSet = new ListDataModel(MetSetBeans);
        return fomula2cmpdSet;
    }

    ///4. Used to save the compound matching results
    private int featureOrder = 0;

    public int getFeatureOrder() {
        return featureOrder;
    }

    public void setFeatureOrder(int featureOrder) {
        this.featureOrder = featureOrder;
    }

    // Section 3 : message section --------------------
    private String msgText;

    public String getMsgText() {
        return msgText;
    }

    private String errMsgText = "";

    public String getErrMsgText() {
        return errMsgText;
    }

    public void setErrMsgText(String errMsgText) {
        this.errMsgText = errMsgText;
    }

    public void clearErrorMsg() {
        errMsgText = "";
    }

    public void populateSpecBeansNoMeta() throws REXPMismatchException {
        specBeans = new ArrayList();

        RConnection RC = sb.getRConnection();

        // Creates a new File instance by converting the given pathname string
        // into an abstract pathname
        String homeDir = sb.getCurrentUser().getHomeDir();

        // Populates the array with names of files and directories
        // For each pathname in the pathnames array
        String rCommand = "CentroidCheck(\'" + "REPLACE_WITH_YOUR_SINGLE_FILE_PATH" + "\')";
        RCenter.recordRCommand(RC, rCommand);
        spectraFiles = new ArrayList<>();
        centroidFileCount = 0;

        File grp = new File(homeDir + File.separator + "upload");
        String[] spectranames = grp.list();
        for (String spectraname : spectranames) {

            File spec = new File(homeDir + "/upload/" + spectraname);
            String fileSize = humanReadableByteCountBin(spec.length());
            String filepath = homeDir + "/upload/" + spectraname;
            if (!spec.isFile()) {
                continue;
            }
            boolean mode = RDataUtils.checkCentroid(RC, filepath);

            spectraFiles.add(spectraname);

            String modec = null;
            boolean include = true;
            boolean disabled = false;
            if (mode) {
                modec = "True";
                centroidFileCount = centroidFileCount + 1;
            } else {
                modec = "<b><font color=\"red\">False</font></b>";
                include = false;
                disabled = true;
                //bnDisabled = true;
            }
            String classnm = "Sample";
            if (spectraname.startsWith("QC_")) {
                classnm = "QC";
            } else if (spectraname.startsWith("BLANK_")) {
                classnm = "BLANK";
            }
            specBeans.add(new SpecBean(spectraname, modec, fileSize, classnm, "intensity", include, disabled));
        }

        bnDisabled = centroidFileCount <= 2;
        setSpectraFiles(spectraFiles);

    }

    private List<String> msgList = new ArrayList<>();

    public List<String> getMsgList() {
        return msgList;
    }

    public void setMsgList(List<String> msgList) {
        this.msgList = msgList;
    }

    public String getJobStatusText() throws IOException {

        String filnm = sb.getCurrentUser().getHomeDir() + "/metaboanalyst_spec_proc.txt";
        StringBuilder s = new StringBuilder();
        try {
            Process p = Runtime.getRuntime().exec("tail -" + 12 + " " + filnm);
            java.io.BufferedReader input = new java.io.BufferedReader(new java.io.InputStreamReader(p.getInputStream()));
            String line = null;
            while ((line = input.readLine()) != null) {
                if (line.contains("ERROR:")) {
                    line = line.replace("ERROR:", "<b>ERROR:</b>");
                } else if (line.contains("Step")) {
                    line = line.replace(line, "<b><font color=\"blue\">" + line + "</font></b>");
                } else if (line.contains("Everything has been finished Successfully !")) {
                    line = line.replace("Everything has been finished Successfully !", "<b>Everything has been finished Successfully !</b>");
                }
                String newLine = line + "<br />";
                s.append(newLine);
            }
        } catch (java.io.IOException e) {
            System.out.println("An error occurred.");
        }
        return s.toString();
    }

    public void includeMessage(boolean val, String name) {
        // System.out.println(val + "===includemssg");
        String summary = val ? name + " is included." : name + " is excluded.";
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", summary));
    }

    // Section 4 : output files section
    public StreamedContent getDefaultParamFile() throws IOException {
        RConnection RC = sb.getRConnection();
        boolean resparam = RSpectraUtils.generateParamFile(RC);
        if (resparam) {
            return DataUtils.getDownloadFile(sb.getCurrentUser().getHomeDir() + "/param_file.txt");
        }
        return DataUtils.getDownloadFile(sb.getCurrentUser().getHomeDir() + "/param_default.txt");
    }

    public StreamedContent getOptimizedParamFile() throws IOException {
        return DataUtils.getDownloadFile(sb.getCurrentUser().getHomeDir() + "/param_optimized.txt");
    }

    public StreamedContent getTextOutputFile() throws IOException {
        return DataUtils.getDownloadFile(sb.getCurrentUser().getHomeDir() + "/metaboanalyst_spec_proc.txt");
    }

    public void internalizeRes(int num) {
        if (ab.isOnMainServer() || ab.isOnDevServer()) {
            String NmIdx = (num == 0)? "" : Integer.toString(num);

            //File ScoreJson = new File(sb.getCurrentUser().getHomeDir() + "/spectra_3d_score" + NmIdx + ".json");
            File LoadingJson = new File(sb.getCurrentUser().getHomeDir() + "/spectra_3d_loading" + NmIdx + ".json");
            File mSetObj = new File(sb.getCurrentUser().getHomeDir() + "/mSet.rda");

            if (LoadingJson.exists()) {
                String guestName = sb.getCurrentUser().getName();
                String myDir = ab.getRealUserHomePath() + guestName;

                File guestFolder = new File(myDir);
                if (!guestFolder.exists()) {
                    guestFolder.mkdir();
                }

                //String currentScoreJson = myDir + "/spectra_3d_score" + NmIdx + ".json";
                String currentLoadingJson = myDir + "/spectra_3d_loading" + NmIdx + ".json";
                String currentmSetObj = myDir + "/mSet.rda";

                //DataUtils.copyFile(ScoreJson, new File(currentScoreJson));
                DataUtils.copyFile(LoadingJson, new File(currentLoadingJson));
                if (num == 0) {
                    DataUtils.copyFile(mSetObj, new File(currentmSetObj));
                }
            }
        }
    }

    public void internalizecomputeEncasingRes() {
        if (ab.isOnMainServer() || ab.isOnDevServer()) {
            // encasing_mesh
            File encasingJson = new File(sb.getCurrentUser().getHomeDir() + "/");
            String[] fnames = encasingJson.list();
            String guestName = sb.getCurrentUser().getName();
            String myDir = ab.getRealUserHomePath() + guestName;

            for (String f : fnames) {
                if (f.startsWith("encasing_mesh") & f.endsWith(".json")) {
                    File meshJson = new File(sb.getCurrentUser().getHomeDir() + "/" + f);
                    if (meshJson.exists()) {
                        String currentmeshJson = myDir + "/" + f;
                        DataUtils.copyFile(meshJson, new File(currentmeshJson));
                    }
                }
            }            
        }
    }

    public boolean summarizeProcessingMsg() {

        RConnection RC = sb.getRConnection();
        ArrayList<String> msgVec = new ArrayList();
        String[] msgArray = RSpectraUtils.getResSummaryMsg(RC);
        if (msgArray == null) {
            return false;
        }
        msgVec.addAll(Arrays.asList(msgArray));
        String msg;
        msg = "<table face=\"times\" size = \"3\">";
        msg = msg + "<tr><th> Raw Spectra Processing Result Summary: " + "</th></tr>";

        for (int i = 0; i < msgVec.size(); i++) {
            msg = msg + "<tr><td align=\"left\">" + msgVec.get(i) + "</td></tr>";
        }
        msg = msg + "</table>";
        msgText = msg;

        return true;
    }

    public void internalizeImage(String imageNm) {
        if (ab.isOnMainServer() || ab.isOnDevServer()) {

            File ImageFile = new File(sb.getCurrentUser().getHomeDir() + "/" + imageNm);
            if (ImageFile.exists()) {
                String guestName = sb.getCurrentUser().getName();
                String myDir = ab.getRealUserHomePath() + guestName;

                File guestFolder = new File(myDir);
                if (!guestFolder.exists()) {
                    guestFolder.mkdir();
                }

                String InternalImageFile = myDir + "/" + imageNm;
                DataUtils.copyFile(ImageFile, new File(InternalImageFile));

            }
        }

    }

    private void internalizeEIC(int featureNum) {
        if (ab.isOnMainServer() || ab.isOnDevServer()) {
            File JsonFile = new File(sb.getCurrentUser().getHomeDir() + "/" + featureNum + ".json");
//            File svgFile = new File(sb.getCurrentUser().getHomeDir() + "/" + featureNum + ".svg");
//            File svgcoordFile = new File(sb.getCurrentUser().getHomeDir() + "/" + featureNum + ".svg.coords.js");
//            File svgmappingFile = new File(sb.getCurrentUser().getHomeDir() + "/" + featureNum + ".svg.mappings.js");
//            File svgutilsFile = new File(sb.getCurrentUser().getHomeDir() + "/" + featureNum + ".svg.utils.js");

            if (JsonFile.exists()) {
                String guestName = sb.getCurrentUser().getName();
                String myDir = ab.getRealUserHomePath() + guestName;

                File guestFolder = new File(myDir);
                if (!guestFolder.exists()) {
                    guestFolder.mkdir();
                }

                String InternalJsonFile = myDir + "/" + featureNum + ".json";
//                String InternalsvgFile = myDir + "/" + featureNum + ".svg";
//                String InternalsvgcoordFile = myDir + "/" + featureNum + ".svg.coords.js";
//                String InternalsvgmappingFile = myDir + "/" + featureNum + ".svg.mappings.js";
//                String InternalsvgutilsFile = myDir + "/" + featureNum + ".svg.utils.js";

                DataUtils.copyFile(JsonFile, new File(InternalJsonFile));
//                DataUtils.copyFile(svgFile, new File(InternalsvgFile));
//                DataUtils.copyFile(svgcoordFile, new File(InternalsvgcoordFile));
//                DataUtils.copyFile(svgmappingFile, new File(InternalsvgmappingFile));
//                DataUtils.copyFile(svgutilsFile, new File(InternalsvgutilsFile));
            }
        }
    }

    // Section 5 : data image and inspect ----------------------
    private StreamedContent image;

    public void setImage(StreamedContent image) {
        this.image = image;
    }

    public StreamedContent getImage() throws IOException {
        FacesContext context = FacesContext.getCurrentInstance();
        String filename = context.getExternalContext().getRequestParameterMap().get("filename");

        String filepath = sb.getCurrentUser().getHomeDir() + "/";

        StreamedContent FileImage = null;
        try {
            FileImage = DataUtils.getStreamedImage(filepath, filename);
        } catch (Exception e) {
        }
        return FileImage;
    }

    private String DataStrucImageNM = "";

    public String getDataStrucImageNM() {
        return DataStrucImageNM;
    }

    public void setDataStrucImageNM(String DataStrucImageNM) {
        this.DataStrucImageNM = DataStrucImageNM;
    }

    private String FileNameImage = null;

    public String getFileNameImage() {
        return FileNameImage;
    }

    public void setFileNameImage(String FileNameImage) {
        this.FileNameImage = FileNameImage;
    }

    private double rtl = 0.0;
    private double rth = 0.0;
    private double mzl = 0.0;
    private double mzh = 0.0;
    private int res = 100;
    private String dimen = "3D";

    public double getRtl() {
        return rtl;
    }

    public void setRtl(double rtl) {
        this.rtl = rtl;
    }

    public double getRth() {
        return rth;
    }

    public void setRth(double rth) {
        this.rth = rth;
    }

    public double getMzl() {
        return mzl;
    }

    public void setMzl(double mzl) {
        this.mzl = mzl;
    }

    public double getMzh() {
        return mzh;
    }

    public void setMzh(double mzh) {
        this.mzh = mzh;
    }

    public int getRes() {
        return res;
    }

    public void setRes(int res) {
        this.res = res;
    }

    public String getDimen() {
        return dimen;
    }

    public void setDimen(String dimen) {
        this.dimen = dimen;
    }

    public void plotMSData() {
        if (res > 1000) {
            res = 1000;
            setRes(1000);
        }
        RConnection RC = sb.getRConnection();
        DataStrucImageNM = RDataUtils.plotMSData(RC, FileNameImage, mzl, mzh, rtl, rth, res, dimen);
        setDataStrucImageNM(DataStrucImageNM);
    }

    private String svgStringBox = "";

    public String getSvgStringBox() {
        return svgStringBox;
    }

    public void setSvgStringBox(String svgStringBox) {
        this.svgStringBox = svgStringBox;
    }

    private String singleXICPlot = "/resources/images/EICalt.png";

    public String getSingleXICPlot() {
        return singleXICPlot;
    }

    public void setSingleXICPlot(String singleXICPlot) {
        this.singleXICPlot = singleXICPlot;
    }

    public void resetSingleXIC() {
        singleXICPlot = "/resources/images/EICalt.png";
        RSpectraUtils.cleanEIClayer(sb.getRConnection(), featureNum);
    }

    //// Section 6 : Other Utils - button's control ----------------------
    private boolean bnDisabled = false;

    public boolean isBnDisabled() {
        return bnDisabled;
    }

    public void setBnDisabled(boolean bnDisabled) {
        this.bnDisabled = bnDisabled;
    }

    public boolean getBnDisabled() {
        return bnDisabled;
    }

    private boolean singlegroup = false;

    public boolean isSinglegroup() {
        return singlegroup;
    }

    public void setSinglegroup(boolean singlegroup) {
        this.singlegroup = singlegroup;
    }

}
