/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package metaboanalyst.controllers;

import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import metaboanalyst.rwrappers.RDataUtils;
import metaboanalyst.utils.DataUtils;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.file.UploadedFile;
import org.rosuda.REngine.Rserve.RConnection;

/**
 *
 * @author qiang
 */
@RequestScoped
@Named("dpEstimator")
public class DuplicateETBean {

    private final ApplicationBean1 ab = (ApplicationBean1) DataUtils.findBean("applicationBean1");
    private final SessionBean1 sb = (SessionBean1) DataUtils.findBean("sessionBean1");

    // Class Function
    public String PerformDuplicateEstimation() {
        sb.setJobDone(false);

        if (duplicateTable == null) {
            DataUtils.updateMsg("Error", "Please upload your file");
            return null;
        }

        if (duplicateTable.getSize() == 0) {
            DataUtils.updateMsg("Error", "File is empty");
            return null;
        }

        if (duplicateTable.getSize() > ab.getMAX_UPLOAD_SIZE()) {
            DataUtils.updateMsg("Error", "File is too large!");
            return null;
        }

        if (!(duplicateTable.getFileName().endsWith(".csv") || duplicateTable.getFileName().endsWith(".txt"))) {
            DataUtils.updateMsg("Error", "Only comma separated format (*.csv) or tab delimited (.txt) will be accepted!");
            return null;
        }

        if (!sb.isLoggedIn() || !sb.getAnalType().equals("utils")) {
            sb.doLogin("NA", "utils", false, false);
        }

        RConnection RC = sb.getRConnection();

        String fileName = DataUtils.uploadFile(duplicateTable, sb.getCurrentUser().getHomeDir(), null, ab.isOnPublicServer());

        if (RDataUtils.readDPDataTB(RC, fileName, sampleCol)) {
            String msg = "Data <u>" + fileName + "</u> was uploaded successfully!";
            DataUtils.updateMsg("OK", msg);
        } else {
            String err = RDataUtils.getErrMsg(RC);
            DataUtils.updateMsg("Error", "Failed to read in the CSV file." + err);
            return null;
        }

        resCode = RDataUtils.performDuplicateEstimation(sb.getRConnection(), sampleCol, estiMethod, smoother);

        if (resCode == 1) {
            sb.setJobDone(true);
        }
        return null;
    }

    public String PerformDuplicateETDemo() {
        sb.setJobDone(false);
        if (!sb.isLoggedIn() || !sb.getAnalType().equals("utils")) {
            sb.doLogin("NA", "utils", false, false);
        }

        RConnection RC = sb.getRConnection();
        
        String fileName = ab.getResourceByAPI("Malaria_HILIC_duplicates.csv");

        if (RDataUtils.readDPDataTB(RC, fileName, sampleCol)) {
            String msg = "Example Data have been estimated successfully!";
            DataUtils.updateMsg("OK", msg);
        } else {
            String err = RDataUtils.getErrMsg(RC);
            DataUtils.updateMsg("Error", "Failed to read in the CSV file." + err);
            return null;
        }

        resCode = RDataUtils.performDuplicateEstimation(sb.getRConnection(), "col", estiMethod, smoother);
        if (resCode == 1) {
            sb.setJobDone(true);
        }
        return null;
    }

    public StreamedContent getDupMergeFile() {
        return DataUtils.getDownloadFile(sb.getCurrentUser().getHomeDir() + "/MetaboAnalyst_duplicates_estimated_data.csv");
    }

    // Class vairable definiation
    private UploadedFile duplicateTable;
    private boolean smoother = false;
    private String sampleCol = "col";
    private String estiMethod = "mean";
    private boolean exampleDemo = true;
    private int resCode = 0;

    public UploadedFile getDuplicateTable() {
        return duplicateTable;
    }

    public void setDuplicateTable(UploadedFile duplicateTable) {
        this.duplicateTable = duplicateTable;
    }

    public boolean isSmoother() {
        return smoother;
    }

    public void setSmoother(boolean smoother) {
        this.smoother = smoother;
    }

    public String getSampleCol() {
        return sampleCol;
    }

    public void setSampleCol(String sampleCol) {
        this.sampleCol = sampleCol;
    }

    public String getEstiMethod() {
        return estiMethod;
    }

    public void setEstiMethod(String estiMethod) {
        this.estiMethod = estiMethod;
    }

    public boolean isExampleDemo() {
        return exampleDemo;
    }

    public void setExampleDemo(boolean exampleDemo) {
        this.exampleDemo = exampleDemo;
    }

}
