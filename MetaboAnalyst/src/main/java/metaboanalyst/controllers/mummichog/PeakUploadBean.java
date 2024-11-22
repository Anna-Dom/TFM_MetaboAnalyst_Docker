/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package metaboanalyst.controllers.mummichog;

import java.io.Serializable;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import javax.faces.model.SelectItem;
import metaboanalyst.controllers.ApplicationBean1;
import metaboanalyst.controllers.SessionBean1;
import metaboanalyst.rwrappers.RDataUtils;
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
@Named("peakLoader")
public class PeakUploadBean implements Serializable {

    private final ApplicationBean1 ab = (ApplicationBean1) DataUtils.findBean("applicationBean1");
    private final SessionBean1 sb = (SessionBean1) DataUtils.findBean("sessionBean1");
    private static final Logger LOGGER = LogManager.getLogger(PeakUploadBean.class);
    private final MummiAnalBean mb = (MummiAnalBean) DataUtils.findBean("mummiAnalBean");

    private UploadedFile peakFile;
    private UploadedFile peakFileTable;
    private double instrumentOpt = 5;
    private int permNum = 100;
    private String msModeOpt = "negative";
    private String orgOpt = "hsa_recon";
    private String listDataOpt = "ibd";
    private String tableDataOpt = "table_ibd";
    private String dataFormat = "mpt"; // mpt, mp, mt, rmp, rmt
    private String dataFormatTable = "table"; // mpt, mp, mt, rmp, rmt
    private String dataRankedBy = "pvalue";
    private String containsRT = "true";
    private boolean primaryIon = true;
    private double rtFrac = 0.02;
    private String dataSource = "generic";
    private String rtIncluded = "no";

    public String getRtIncluded() {
        return rtIncluded;
    }

    public void setRtIncluded(String rtIncluded) {
        this.rtIncluded = rtIncluded;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public boolean isPrimaryIon() {
        return primaryIon;
    }

    public void setPrimaryIon(boolean primaryIon) {
        this.primaryIon = primaryIon;
    }

    public double getRtFrac() {
        return rtFrac;
    }

    public void setRtFrac(double rtFrac) {
        this.rtFrac = rtFrac;
    }

    public String getContainsRT() {
        return containsRT;
    }

    public void setContainsRT(String containsRT) {
        this.containsRT = containsRT;
    }

    public String getDataRankedBy() {
        return dataRankedBy;
    }

    public void setDataRankedBy(String dataRankedBy) {

        if (dataRankedBy.equals("pvalue")) {
            this.dataRankedBy = "rmp";
        } else {
            this.dataRankedBy = "rmt";
        }
    }

    public UploadedFile getPeakFileTable() {
        return peakFileTable;
    }

    public void setPeakFileTable(UploadedFile peakFileTable) {
        this.peakFileTable = peakFileTable;
    }

    public String getDataFormat() {
        return dataFormat;
    }

    public void setDataFormat(String dataFormat) {
        this.dataFormat = dataFormat;
    }

    private boolean useExample = false;

    public boolean isUseExample() {
        return useExample;
    }

    public void setUseExample(boolean useExample) {
        this.useExample = useExample;
    }

    public String getListDataOpt() {
        return listDataOpt;
    }

    public void setListDataOpt(String dataOpt) {
        this.listDataOpt = dataOpt;
    }

    public String getTableDataOpt() {
        return tableDataOpt;
    }

    public void setTableDataOpt(String dataOpt) {
        this.tableDataOpt = dataOpt;
    }

    public double getInstrumentOpt() {
        return instrumentOpt;
    }

    public void setInstrumentOpt(double instrumentOpt) {
        this.instrumentOpt = instrumentOpt;
    }

    public int getPermNum() {
        return permNum;
    }

    public void setPermNum(int permNum) {
        this.permNum = permNum;
    }

    public String getMsModeOpt() {
        return msModeOpt;
    }

    public void setMsModeOpt(String msModeOpt) {
        this.msModeOpt = msModeOpt;
    }

    public String getOrgOpt() {
        return orgOpt;
    }

    public void setOrgOpt(String orgOpt) {
        this.orgOpt = orgOpt;
    }

    public UploadedFile getPeakFile() {
        return peakFile;
    }

    public void setPeakFile(UploadedFile peakFile) {
        this.peakFile = peakFile;
    }

    private String examplePeakList = "ibd";

    public String getExamplePeakList() {
        return examplePeakList;
    }

    public void setExamplePeakList(String examplePeakList) {
        this.examplePeakList = examplePeakList;
    }

    private String dataType = "list";

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String processListExampleUpload() {
        String loginOpt = "mass_all";

        if (!sb.doLogin(loginOpt, "mummichog", false, false)) {
            DataUtils.updateMsg("Error", "Failed to log in!");
            return null;
        }

        String fileName;
        useExample = true;
        dataType = "list";
        RConnection RC = sb.getRConnection();

        switch (listDataOpt) {
            case "mixed":
                msModeOpt = "mixed";
                dataFormat = "mpt";
                fileName = ab.getInternalData("mummichog_mixed.txt");
                break;
            case "dc":
                msModeOpt = "positive";
                dataFormat = "mpt";
                fileName = ab.getInternalData("mummichog_dc.txt");
                break;
            case "dc1":
                msModeOpt = "positive";
                dataFormat = "rmp";
                mb.setDisabledGsea(true);
                mb.setDisabledMum(false);
                mb.setDisabledMumPval(true);
                fileName = ab.getInternalData("mummichog_dc1.txt");
                break;
            case "ibd1":
                msModeOpt = "negative";
                dataFormat = "rmt";
                mb.setDisabledGsea(false);
                mb.setDisabledMum(true);
                mb.setDisabledMumPval(true);
                fileName = ab.getInternalData("mummichog_ibd1.txt");
                break;
            case "ibd_rt":
                msModeOpt = "negative";
                dataFormat = "mprt";
                primaryIon = true;
                rtIncluded = "seconds";
                fileName = ab.getInternalData("mummichog_rt.txt");
                break;
            default:
                msModeOpt = "negative";
                dataFormat = "mpt";
                fileName = ab.getInternalData("mummichog_ibd.txt");
                break;
        }
        
        RDataUtils.setPeakFormat(RC, dataFormat);
        setMsModeOpt(msModeOpt);
        RDataUtils.setInstrumentParams(RC, instrumentOpt, msModeOpt, primaryIon ? "yes" : "no", rtFrac);
        sb.setDataUploaded();

        if (RDataUtils.readPeakListData(RC, fileName)) {

            RDataUtils.setRT(RC, rtIncluded);

            if (dataFormat.equals("mprt") || dataFormat.equals("mpr") || dataFormat.equals("mrt")) {
                mb.setDisabledV2(true);
            }
            return "Data check";

        } else {
            String err = RDataUtils.getErrMsg(sb.getRConnection());
            DataUtils.updateMsg("Error", "Failed to read in the peak list file." + err);
            return null;
        }
    }

    public String processTableExampleUpload() {
        String loginOpt = "mass_table";

        if (!sb.doLogin(loginOpt, "mummichog", false, false)) {
            DataUtils.updateMsg("Error", "Failed to log in!");
            return null;
        }

        String fileName;
        useExample = true;
        dataType = "list";
        RConnection RC = sb.getRConnection();

        switch (tableDataOpt) {
            case "table_covid":
                //COVID DATA
                msModeOpt = "positive";
                setDataType("table");
                dataFormatTable = "colu";
                RDataUtils.setPeakFormat(RC, dataFormatTable);
                fileName = ab.getResourceByAPI("A3_pos.csv");
                rtIncluded = "seconds";
                mb.setDisabledV2(true);
                break;
            case "table_malaria":
                //Malaria DATA
                msModeOpt = "positive";
                setDataType("table");
                dataFormatTable = "colu";
                RDataUtils.setPeakFormat(RC, dataFormatTable);
                fileName = ab.getResourceByAPI("malaria_feature_table.csv");
                rtIncluded = "seconds";
                instrumentOpt = 4.3;
                mb.setDisabledV2(true);
                break;
            default:
                //IBD DATA
                msModeOpt = "negative";
                setDataType("table");
                dataFormatTable = "colu";
                RDataUtils.setPeakFormat(RC, dataFormatTable);
                fileName = ab.getResourceByAPI("mummichog_immu_table.csv");
                rtIncluded = "no";
                mb.setDisabledV2(false);
                break;
        }
        sb.initNaviTree("mummichog-table");
        setMsModeOpt(msModeOpt);
        RDataUtils.setInstrumentParams(RC, instrumentOpt, msModeOpt, primaryIon ? "yes" : "no", rtFrac);
        sb.setDataUploaded();

        RDataUtils.setRT(RC, rtIncluded);

        if (!RDataUtils.readTextData(RC, fileName, dataFormat, "disc")) {
            DataUtils.updateMsg("Error", RDataUtils.getErrMsg(RC));
            return null;
        } else {
            return "Data check";
        }

    }

    public String getDataFormatTable() {
        return dataFormatTable;
    }

    public void setDataFormatTable(String dataFormatTable) {
        this.dataFormatTable = dataFormatTable;
    }

    public String handleMassAllUploadTable() {
        dataType = "table";
        sb.initNaviTree("mummichog-table");
        return (handleMassAllUpload());
    }

    public String handleMassAllUpload() {

        String loginOpt = "mass_all";
        if (dataType.equals("table")) {
            loginOpt = "mass_table";
            setDataType("table");
        }

        if (!sb.doLogin(loginOpt, "mummichog", false, false)) {
            DataUtils.updateMsg("Error", "Failed to log in!");
            return null;
        }

        String fileName;
        if (dataType.equals("table")) {
            peakFile = peakFileTable;
        }
        try {
            if (peakFile==null || peakFile.getSize() == 0) {
                DataUtils.updateMsg("Error", "File is empty!");
                return null;
            }
            fileName = DataUtils.getJustFileName(peakFile.getFileName());
            DataUtils.uploadFile(peakFile, sb.getCurrentUser().getHomeDir(), null, ab.isOnPublicServer());

        } catch (Exception e) {
            //e.printStackTrace();
              LOGGER.error("handleMassAllUpload", e);
            return null;
        }

        sb.setDataUploaded();
        RConnection RC = sb.getRConnection();
        RDataUtils.setPeakFormat(RC, dataRankedBy);
        RDataUtils.setInstrumentParams(RC, instrumentOpt, msModeOpt, primaryIon ? "yes" : "no", rtFrac);

        if (dataType.equals("table")) {            
            RDataUtils.setRT(RC, rtIncluded);
            if (rtIncluded.equals("minutes") || rtIncluded.equals("seconds")) {
                mb.setDisabledV2(true);
            }

            if (dataSource.equals("mzmine")) {
                if (!RDataUtils.readMzMineTable(RC, fileName, dataFormat)) {
                    DataUtils.updateMsg("Error", RDataUtils.getErrMsg(RC));
                    return null;
                } else {
                    fileName = "mzmine_peaktable_metaboanalyst.csv";
                }
            }

            if (!RDataUtils.readTextData(RC, fileName, dataFormat, "disc")) {
                DataUtils.updateMsg("Error", RDataUtils.getErrMsg(RC));
                return null;
            } else {
                return "Data check";
            }
        }

        if (RDataUtils.readPeakListData(RC, fileName)) {

            String format = RDataUtils.GetPeakFormat(RC);

            switch (format) {
                case "mpt":
                case "mprt":
                    mb.setDisabledGsea(false);
                    mb.setDisabledMum(false);
                    break;
                case "mp":
                case "mpr":
                    mb.setDisabledGsea(true);
                    mb.setDisabledMum(false);
                    break;
                case "rmp":
                    // single column
                    mb.setDisabledGsea(true);
                    mb.setDisabledMum(false);
                    mb.setDisabledMumPval(true);
                    break;
                default:
                    //rmt or mt or mtr
                    mb.setDisabledGsea(false);
                    mb.setDisabledMum(true);
                    mb.setDisabledMumPval(true);
                    break;
            }

            if (format.equals("mprt") || format.equals("mpr") || format.equals("mrt")) {
                mb.setDisabledV2(true);
            }

            return "Data check";
        } else {
            String err = RDataUtils.getErrMsg(sb.getRConnection());
            DataUtils.updateMsg("Error", "Failed to read in the peak list file." + err);
            return null;
        }
    }

}
