/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package metaboanalyst.controllers.metapath;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import metaboanalyst.models.User;
import metaboanalyst.controllers.ApplicationBean1;
import metaboanalyst.controllers.SessionBean1;
import metaboanalyst.models.MetaPathModel;
import metaboanalyst.rwrappers.RDataUtils;
import metaboanalyst.rwrappers.RMetaPathUtils;
import metaboanalyst.rwrappers.RMetaUtils;
import metaboanalyst.utils.DataUtils;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.file.UploadedFile;
import org.rosuda.REngine.Rserve.RConnection;

/**
 *
 * @author jianguox
 */
@SessionScoped
@Named("pLoadBean")
@JsonIgnoreProperties(ignoreUnknown = true)
public class MetaPathLoadBean implements Serializable {

    @JsonIgnore
    private final ApplicationBean1 ab = (ApplicationBean1) DataUtils.findBean("applicationBean1");
    @JsonIgnore
    private final SessionBean1 sb = (SessionBean1) DataUtils.findBean("sessionBean1");
    /**
     * Record the currently selected data
     */

    // Section I --- variables, setter and getter <--------------
    private int dataNum = 1;

    public int getDataNum() {
        return dataNum;
    }

    public void setDataNum(int dataNum) {
        this.dataNum = dataNum;
    }

    private String dataFormat = "colu";

    public String getDataFormat() {
        return dataFormat;
    }

    public void setDataFormat(String dataFormat) {
        this.dataFormat = dataFormat;
    }

    private String dataIon = "positive";

    public String getDataIon() {
        return dataIon;
    }

    public void setDataIon(String dataIon) {
        this.dataIon = dataIon;
    }

    private String dataType = "massPeaks";

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    private String selectedTestData = "Test1";

    public String getSelectedTestData() {
        return selectedTestData;
    }

    public void setSelectedTestData(String selectedTestData) {
        this.selectedTestData = selectedTestData;
    }

    private MetaPathModel selectedData;

    public MetaPathModel getSelectedData() {
        return selectedData;
    }

    public void setSelectedData(MetaPathModel selectedData) {
        this.selectedData = selectedData;
    }

    public MetaPathModel getData4Vis() {
        return selectedData;
    }

    public void setData4Vis(MetaPathModel selectedData) {
        this.selectedData = selectedData;
        selectedData.PlotPathDataProfile();
    }

    private boolean allDataConsistent = true;

    public boolean isAllDataConsistent() {
        return allDataConsistent;
    }

    public void setAllDataConsistent(boolean allDataConsistent) {
        this.allDataConsistent = allDataConsistent;
    }

    private String integCheckMsg = "No data set uploaded yet!";

    public String getIntegCheckMsg() {
        return integCheckMsg;
    }

    private String integCheckMsg2 = "";

    public String getIntegCheckMsg2() {
        return integCheckMsg2;
    }

    private String width = "400px";

    public String getWidth() {
        if (selectedData.getName2() != null) {
            width = "575px";
        } else {
            width = "400px";
        }
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    // Section II --- Handle File uploading  <--------------
    private UploadedFile fileNeg;

    private UploadedFile fileReg;

    private UploadedFile filePos;

    public UploadedFile getFileNeg() {
        return fileNeg;
    }

    public void setFileNeg(UploadedFile fileNeg) {
        this.fileNeg = fileNeg;
    }

    public UploadedFile getFileReg() {
        return fileReg;
    }

    public void setFileReg(UploadedFile fileReg) {
        this.fileReg = fileReg;
    }

    public UploadedFile getFilePos() {
        return filePos;
    }

    public void setFilePos(UploadedFile filePos) {
        this.filePos = filePos;
    }

    private int count;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void increment() {
        count++;
    }

    private void checkLogIn() {
        if (!loggedIn) {
            sb.doLogin("conc", "metapaths", false, false);
            //need to update all dataSets created for meta and multi
            dataSets = getDataSets();
            for (int i = 0; i < dataSets.size(); i++) {
                dataSets.get(i).setRC(sb.getRConnection());
            }
            loggedIn = true;
        }

    }
    private boolean loggedIn = false;

    public boolean handleFileUpload() {

        UploadedFile file = null;
        UploadedFile file2 = null;

        if ("mixed".equals(dataIon)) {
            file = filePos;
            file2 = fileNeg;
        } else {
            file = fileReg;
        }

        if (file == null || file.getSize() == 0) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Empty file?"));
            return false;
        }

        if ("mixed".equals(dataIon) && (file2 == null || file2.getSize() == 0)) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Empty file?"));
            return false;
        }

        if (ab.isOnPublicServer()) { // size limit will apply only on public server

            if (file.getSize() > ab.getMAX_UPLOAD_SIZE()) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "The file size exceeds limit: 50M"));
                return false;
            }

            if ("mixed".equals(dataIon) && (file2.getSize() > ab.getMAX_UPLOAD_SIZE())) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "The file size exceeds limit: 50M"));
                return false;
            }

        }

        String fileName = file.getFileName();
        if (!fileName.endsWith(".csv") && !fileName.endsWith(".txt")) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Only .txt or .csv file is acceptable!"));
            file = null;
            return false;
        }

        if ("mixed".equals(dataIon)) {
            String fileName2 = file2.getFileName();

            if (!fileName2.endsWith(".csv") && !fileName2.endsWith(".txt")) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Only .txt or .csv file is acceptable!"));
                return false;
            }
        }

        checkLogIn();

        fileName = DataUtils.uploadFile(file, sb.getCurrentUser().getHomeDir(), null, ab.isOnPublicServer());

        int res = 0;
        int res2 = 0;

        if ("mixed".equals(dataIon)) {
            String fileName2 = file2.getFileName();
            fileName2 = DataUtils.uploadFile(file2, sb.getCurrentUser().getHomeDir(), null, ab.isOnPublicServer());
            res2 = RMetaPathUtils.readMetaPathTableMix(sb.getRConnection(), fileName, fileName2, dataFormat, dataType);
        } else {
            res = RMetaPathUtils.readMetaPathTable(sb.getRConnection(), fileName, dataFormat, dataType);
        }

        // 2. Process the files
        if ((!"mixed".equals(dataIon) && res == 0) || ("mixed".equals(dataIon) && res2 == 0)) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", RDataUtils.getErrMsg(sb.getRConnection())));
            //remove the file
            file = null;
            return false;
        } else {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "OK", fileName + " is uploaded and parsed out." + RDataUtils.getCurrentMsg(sb.getRConnection())));

            if ("mixed".equals(dataIon)) {
                String fileName2 = file2.getFileName();

                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "OK", fileName2 + " is uploaded and parsed out." + RDataUtils.getCurrentMsg(sb.getRConnection())));

                selectedData.setName2(fileName2);
            }

            selectedData.setUploaded(true);
            selectedData.setName(fileName);
            selectedData.setDataformat(dataFormat);
            sb.setDataUploaded(true);

            selectedData.processMetaPathData();
            file = null;
            return true;
        }

    }

    // Section III --- selectedData Utilities  <--------------
    private MetaPathModel selectedDataSet;

    public MetaPathModel getSelectedDataSet() {
        return selectedDataSet;
    }

    public void setSelectedDataSet(MetaPathModel dm) {
        if (!selectedDataSet.getName().equals(dm.getName())) {
            this.selectedDataSet = dm;
            RMetaUtils.setCurrentData(sb.getRConnection(), selectedDataSet.getName());
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "OK",
                            "Current data is: " + selectedDataSet.getName() + ", ready for analysis."));
        }
    }

    private int currentDeNum = 0;

    public int getCurrentDeNum() {
        return currentDeNum;
    }

    public void setCurrentDeNum(int currentDeNum) {
        this.currentDeNum = currentDeNum;
    }

    private boolean statsOnlyMode = false;

    public boolean isStatsOnlyMode() {
        return statsOnlyMode;
    }

    public void setStatsOnlyMode(boolean statsOnlyMode) {
        this.statsOnlyMode = statsOnlyMode;
    }

    public void addData() {
        addNewData("Upload");
    }

    public void deleteData() {
        deleteData(selectedData);
    }

    private List<MetaPathModel> dataSets;

    private List<MetaPathModel> mDataSets; //the contains meta

    public void setmDataSets(List<MetaPathModel> mDataSets) {
        this.mDataSets = mDataSets;
    }

    public List<MetaPathModel> getDataSets() {
        if (dataSets == null) {
            dataSets = new ArrayList();
            dataSets.add(new MetaPathModel(sb.getRConnection(), "Upload"));
        }
        return dataSets;
    }

    private double pvalCutoff = 0.05;

    public double getPvalCutoff() {
        return pvalCutoff;
    }

    public void setPvalCutoff(double pvalCutoff) {
        this.pvalCutoff = pvalCutoff;
    }

    public List<MetaPathModel> getMetaDataSets() {
        if (mDataSets == null) {
            updateMetaDataSets();
        }
        return mDataSets;
    }

    public void updateMetaDataSets() {
        int[] res = RMetaPathUtils.performPathSum(sb.getRConnection(), pvalCutoff);
        currentDeNum = res.length;
        mDataSets = new ArrayList();
        int num = 0;

        for (int i = 0; i < dataSets.size(); i++) {
            if (dataSets.get(i).isInclude()) {
                dataSets.get(i).setDeNum(res[num]);
                num++;
                mDataSets.add(dataSets.get(i));
            }
        }
        MetaPathModel dm = new MetaPathModel(sb.getRConnection(), "meta_dat");
        dm.setDeNum(res[res.length - 1]);
        //dm.setDe
        mDataSets.add(dm);
    }

    public DefaultStreamedContent getMergedFile() {
        return DataUtils.getDownloadFile(sb.getCurrentUser().getHomeDir() + "/MetaboAnalyst_merged_data.csv");
    }

    public void setDataSets(List<MetaPathModel> dataSets) {
        this.dataSets = dataSets;
    }

    // Section IV --- Example Utilities  <--------------
    private int example = 1;

    public int getExample() {
        return example;
    }

    public void setExample(int example) {
        this.example = example;
    }

    private boolean allowContinue = false;

    public boolean isAllowContinue() {
        return allowContinue;
    }

    public void setAllowContinue(boolean allowContinue) {
        this.allowContinue = allowContinue;
    }

    public void performMetaIntegrityCheck() {
        // 1. clean the msgset
        integCheckMsg = "";

        // 2. get datasets
        ArrayList<String> selectedDataSets = getCurrentDataSets();
        int datasizenum = dataSets.size();
        if (!dataSets.get(0).isUploaded()) {
            datasizenum = datasizenum - 1;
        }

        integCheckMsg2 = "<b>" + selectedDataSets.size() + "</b> datasets (total: <b>" + datasizenum + "</b>) have been selected.";

        if (selectedDataSets.isEmpty()) {
            integCheckMsg = "No data was found. Please upload/select first !";
            allowContinue = false;
            return;
        } else if (selectedDataSets.size() == 1) {
            integCheckMsg = "At least two datasets are required for meta-analysis.";
            allowContinue = false;
        } else {
            allowContinue = true;
        }

        // 3. Define the global select datasets
        String datas = DataUtils.createStringVector(selectedDataSets.toArray(new String[0]));
        RMetaPathUtils.setInclusionDataSets(sb.getRConnection(), datas);
        //RDataUtils.setSelectedDataNames(sb.getRConnection(), selectedDataSets.toArray(new String[0]));
        setDataNum(selectedDataSets.size());
    }

    //do default analysis for testing purpose
    public void doDefaultMetaAnalysis() {

        checkLogIn();

        User currentUser = sb.getCurrentUser();
        List<MetaPathModel> metaDataSets = getDataSets();

        //reset data 
        metaDataSets.clear();

        switch (example) {
            case 1:
                System.out.println("Running Meta Path analysis example 1 @_@");
                //first data
                String inpath = ab.getResourceByAPI("A1_pos.csv");
                String name = DataUtils.getJustFileName(inpath);
                String outpath = currentUser.getHomeDir() + File.separator + name;
                DataUtils.fetchFile(inpath, new File(outpath));
                addNewData("Upload");
                RMetaPathUtils.readMetaPathTable(sb.getRConnection(), name, "colu", "massPeaks");
                selectedData = metaDataSets.get(0);
                selectedData.setUploaded(true);
                selectedData.setName(name);
                selectedData.processMetaPathData();
                perfromDefaultMetaProcess(selectedData);
                selectedData.setInclude(true);
                selectedData.setDisabledModify(true);
                //2nd data
                inpath = ab.getResourceByAPI("B1_pos.csv");
                name = DataUtils.getJustFileName(inpath);
                outpath = currentUser.getHomeDir() + File.separator + name;
                DataUtils.fetchFile(inpath, new File(outpath));
                addNewData("Upload");
                RMetaPathUtils.readMetaPathTable(sb.getRConnection(), name, "colu", "massPeaks");
                selectedData = metaDataSets.get(1);
                selectedData.setUploaded(true);
                selectedData.setName(name);
                selectedData.processMetaPathData();
                perfromDefaultMetaProcess(selectedData);
                selectedData.setInclude(true);
                selectedData.setDisabledModify(true);
                //3rd data
                inpath = ab.getResourceByAPI("C1_pos.csv");
                name = DataUtils.getJustFileName(inpath);
                outpath = currentUser.getHomeDir() + File.separator + name;
                DataUtils.fetchFile(inpath, new File(outpath));
                addNewData("Upload");
                RMetaPathUtils.readMetaPathTable(sb.getRConnection(), name, "colu", "massPeaks");
                selectedData = metaDataSets.get(2);
                selectedData.setUploaded(true);
                selectedData.setName(name);
                selectedData.processMetaPathData();
                perfromDefaultMetaProcess(selectedData);
                selectedData.setInclude(true);
                selectedData.setDisabledModify(true);
                break;
            case 2:
                System.out.println("Running Meta Path analysis example 2 T_T");
                //first data
                inpath = ab.getResourceByAPI("A1_pos.csv");
                String inpath2 = ab.getResourceByAPI("A1_neg.csv");
                name = DataUtils.getJustFileName(inpath);
                String name2 = DataUtils.getJustFileName(inpath2);
                outpath = currentUser.getHomeDir() + File.separator + name;
                String outpath2 = currentUser.getHomeDir() + File.separator + name2;
                DataUtils.fetchFile(inpath, new File(outpath));
                DataUtils.fetchFile(inpath2, new File(outpath2));
                addNewData("Upload");
                RMetaPathUtils.readMetaPathTableMix(sb.getRConnection(), name, name2, "colu", "massPeaks");
                selectedData = metaDataSets.get(0);
                selectedData.setUploaded(true);
                selectedData.setName(name);
                selectedData.setName2(name2);
                selectedData.processMetaPathData();
                perfromDefaultMetaProcess(selectedData);
                selectedData.setInclude(true);
                selectedData.setDisabledModify(true);
                //2nd data
                inpath = ab.getResourceByAPI("B1_pos.csv");
                inpath2 = ab.getResourceByAPI("B1_neg.csv");
                name = DataUtils.getJustFileName(inpath);
                name2 = DataUtils.getJustFileName(inpath2);
                outpath = currentUser.getHomeDir() + File.separator + name;
                outpath2 = currentUser.getHomeDir() + File.separator + name2;
                DataUtils.fetchFile(inpath, new File(outpath));
                DataUtils.fetchFile(inpath2, new File(outpath2));
                addNewData("Upload");
                RMetaPathUtils.readMetaPathTableMix(sb.getRConnection(), name, name2, "colu", "massPeaks");
                selectedData = metaDataSets.get(1);
                selectedData.setUploaded(true);
                selectedData.setName(name);
                selectedData.setName2(name2);
                selectedData.processMetaPathData();
                perfromDefaultMetaProcess(selectedData);
                selectedData.setInclude(true);
                selectedData.setDisabledModify(true);
                //3rd data
                inpath = ab.getResourceByAPI("C1_pos.csv");
                inpath2 = ab.getResourceByAPI("C1_neg.csv");
                name = DataUtils.getJustFileName(inpath);
                name2 = DataUtils.getJustFileName(inpath2);
                outpath = currentUser.getHomeDir() + File.separator + name;
                outpath2 = currentUser.getHomeDir() + File.separator + name2;
                DataUtils.fetchFile(inpath, new File(outpath));
                DataUtils.fetchFile(inpath2, new File(outpath2));
                addNewData("Upload");
                RMetaPathUtils.readMetaPathTableMix(sb.getRConnection(), name, name2, "colu", "massPeaks");
                selectedData = metaDataSets.get(2);
                selectedData.setUploaded(true);
                selectedData.setName(name);
                selectedData.setName2(name2);
                selectedData.processMetaPathData();
                perfromDefaultMetaProcess(selectedData);
                selectedData.setInclude(true);
                selectedData.setDisabledModify(true);

                break;
            default:
                //No more examples for now! Do nothing but report an error
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error:", "Something wrong with your example selection !"));
                return;
        }

        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "OK", "Three datasets were uploaded and processed on the server.");
        FacesContext.getCurrentInstance().addMessage(null, msg);

        sb.setDataUploaded(true);
    }

    private void perfromDefaultMetaProcess(MetaPathModel dm) {
        dm.setUploaded(true);
        dm.processMetaPathData();
        dm.setProcessed(true);
        dm.setNormOpt("log");
        dm.setAutoscaleOpt(false);
        dm.performNormalization();
        dm.setNormed(true);

        if (example == 1) {
            dataIon = "positive";
        } else {
            dataIon = "mixed";
        }

        dm.setSigLevel(0.05);
        dm.performPathAnalysis();
        dm.setAnalDone(true);
        dm.setAllDone();
        dm.performmSetQSclean();
    }

    // Section V ---- Functional Utilities  <--------------
    public void addNewData(String dataName) {
        dataSets.add(new MetaPathModel(sb.getRConnection(), dataName));
    }

    public void deleteData(MetaPathModel selectedData) {
        dataSets.remove(selectedData);
        //remove data from R inmex.vec 
        RMetaUtils.removeData(sb.getRConnection(), selectedData.getFullName());
        String homeDir = sb.getCurrentUser().getHomeDir();
        String fileName = selectedData.getFullName();
        File rmFile = new File(homeDir + File.separator + fileName);
        rmFile.delete();

        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "OK", "The selected data entry is deleted"));
    }

    public ArrayList<String> getCurrentDataSets() {

        ArrayList<String> currentDataNms = new ArrayList();
        for (int i = 0; i < dataSets.size(); i++) {
            if (dataSets.get(i).isInclude()) {
                currentDataNms.add(dataSets.get(i).getFullName());
            }
        }
        return currentDataNms;
    }

    public MetaPathModel getData(String dataName) {
        for (MetaPathModel dm : dataSets) {
            if (dm.getFullName().equals(dataName)) {
                System.out.println("----------------- Getting this data dataName ---> " + dataName);
                return dm;
            }
        }
        return null;
    }

    private ArrayList<String> selectedDataNms = new ArrayList();

    public ArrayList<String> getSelectedDataNms() {
        return selectedDataNms;
    }

    public void setSelectedDataNms(ArrayList<String> selectedDataNms) {
        this.selectedDataNms = selectedDataNms;
    }

    public String prepareMetaPathVennView() {

        selectedDataNms.clear();
        //System.out.println("--------mDataSets.size is ---->" + mDataSets.size());
        for (int i = 0; i < mDataSets.size(); i++) {
            MetaPathModel dm = mDataSets.get(i);

            if (dm.isVennInclude()) {
                if (dm.getName2() != null) {
                    String mixNM = dm.getName0().replaceAll(".csv", "") + "mixed";
                    //System.out.println("--------mixNM is ---->" + mixNM);
                    selectedDataNms.add(mixNM);
                } else {
                    String cleanNM = dm.getName().replaceAll(".csv", "");
                    //System.out.println("--------cleanNM is ---->" + cleanNM);
                    selectedDataNms.add(cleanNM);
                }
            }
        }
        /*
        if (selectedDataNms.size() > 4) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Venn diagram supports at most 4 datasets."));
            PrimeFaces.current().executeScript("PF('statusDialog').hide()");
            return null;
        }
         */
        //set selected data files
        RMetaPathUtils.setSelectedMetaPathNames(sb.getRConnection(), selectedDataNms.toArray(new String[0]));

        String fileName = sb.getNewImage("upset_path");
        int res = RMetaPathUtils.prepareMetaPathData(sb.getRConnection(), fileName);

        if (res == 1) {
            return "Upset diagram";
        } else {
            updateMetaDataSets();
            return prepareMetaPathVennView();
        }

        //PrimeFaces.current().executeScript("PF('statusDialog').hide()");
        //String msg = RDataUtils.getCurrentMsg(sb.getRConnection());
        //FacesContext.getCurrentInstance().addMessage(null,
        //        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", msg));
        //return null;
    }

    public String naiveDisPlot() {
        String fileRoot = ab.getRootContext() + sb.getCurrentUser().getRelativeDir() + File.separator;
        String imageNM = "";
        imageNM = selectedData.getName().split(" \\+ ")[0] + "_qc_boxdpi72.png";
        if (selectedData.getName2() != null) {
            imageNM = selectedData.getName().split(" \\+ ")[0] + "_"
                    + selectedData.getName2().substring(0, selectedData.getName2().length() - 4)
                    + "_dpi72_qc_box.png";
        }
        return fileRoot + imageNM;
    }

    public StreamedContent getImage() throws IOException {
        String filename = selectedData.getName().split(" \\+ ")[0];
        String filename2 = "";
        String filepath = sb.getCurrentUser().getHomeDir() + "/";

        if (selectedData.getName2() != null) {
            filename2 = selectedData.getName2().substring(0, selectedData.getName2().length() - 4);
            filename = filename + "_" + filename2 + "_dpi72_norm_box.png";
        } else {
            filename = filename + "_norm_boxdpi72.png";
        }

        StreamedContent FileImage = null;
        try {
            FileImage = DataUtils.getStreamedImage(filepath, filename);
        } catch (Exception e) {

        }
        return FileImage;
    }

    public void finishDataSet() {
        selectedData.setDisabledModify(true);
        selectedData.setAllDone();

    }

    public String confirmAllData() {
        RMetaPathUtils.restoreCmdHistory(sb.getRConnection());
        return "Meta-Analysis Params";
    }

}
