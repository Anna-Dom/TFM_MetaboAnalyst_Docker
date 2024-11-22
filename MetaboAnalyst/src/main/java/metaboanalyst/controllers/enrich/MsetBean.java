/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package metaboanalyst.controllers.enrich;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import metaboanalyst.controllers.ApplicationBean1;
import metaboanalyst.controllers.SessionBean1;
import metaboanalyst.models.MetSetBean;
import metaboanalyst.models.OraBean;
import metaboanalyst.models.QeaBean;
import metaboanalyst.rwrappers.RDataUtils;
import metaboanalyst.rwrappers.REnrichUtils;
import metaboanalyst.utils.DataUtils;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.charts.ChartData;
import org.primefaces.model.charts.axes.cartesian.CartesianScales;
import org.primefaces.model.charts.axes.cartesian.linear.CartesianLinearAxes;
import org.primefaces.model.charts.axes.cartesian.linear.CartesianLinearTicks;
import org.primefaces.model.charts.bar.BarChartOptions;
import org.primefaces.model.charts.hbar.HorizontalBarChartDataSet;
import org.primefaces.model.charts.hbar.HorizontalBarChartModel;
import org.primefaces.model.charts.optionconfig.title.Title;
import org.primefaces.model.charts.pie.PieChartDataSet;
import org.primefaces.model.charts.pie.PieChartModel;
import org.primefaces.model.file.UploadedFile;
import org.rosuda.REngine.Rserve.RConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author jianguox
 */
@SessionScoped
@Named("msetBean")
@JsonIgnoreProperties(ignoreUnknown = true)
public class MsetBean implements Serializable {
    @JsonIgnore
    private final ApplicationBean1 ab = (ApplicationBean1) DataUtils.findBean("applicationBean1");
    @JsonIgnore
    private final SessionBean1 sb = (SessionBean1) DataUtils.findBean("sessionBean1");
    @JsonIgnore
    private static final Logger LOGGER = LogManager.getLogger(MsetBean.class);

    private String msetOpt = "smpdb_pathway";

    public String getMsetOpt() {
        return msetOpt;
    }

    public void setMsetOpt(String msetOpt) {
        this.msetOpt = msetOpt;
    }

    private String libOpt = "all";

    public String getLibOpt() {
        return libOpt;
    }

    public void setLibOpt(String libOpt) {
        this.libOpt = libOpt;
    }

    private UploadedFile msetLibFile;

    public UploadedFile getMsetLibFile() {
        return msetLibFile;
    }

    public void setMsetLibFile(UploadedFile msetLibFile) {
        this.msetLibFile = msetLibFile;
    }

    private String checkMsg = "";

    public String getCheckMsg() {
        return checkMsg;
    }

    public void setCheckMsg(String checkMsg) {
        this.checkMsg = checkMsg;
    }

    public void msetUploadBn_action() {
        // TODO: Process the action. Return value is a navigation
        // case name where null will return to the same page.
        try {
            RConnection RC = sb.getRConnection();

            //check if data is uploaded
            if (msetLibFile == null) {
                DataUtils.updateMsg("Error", "Please upload your file");
                return;
            }
            if (msetLibFile.getSize() == 0) {
                DataUtils.updateMsg("Error", "File is empty");
                return;
            }

            if (!msetLibFile.getFileName().endsWith(".csv")) {
                DataUtils.updateMsg("Error", "Only comma separated format (*.csv) will be accepted!");
                return;
            }

            // File libFile = new File(homeDir + File.separatorChar + fileName);
            // uploadedFile.write(libFile);
            String fileName = DataUtils.uploadFile(msetLibFile, sb.getCurrentUser().getHomeDir(), null, false);
            boolean res = RDataUtils.readMsetLibData(RC, fileName);
            if (res) {
                checkMsg = RDataUtils.getMsetLibCheckMsg(RC);
                DataUtils.updateMsg("OK", checkMsg);
                msetOpt = "self";
            } else {
                DataUtils.updateMsg("Error:", RDataUtils.getErrMsg(RC));
            }
        } catch (Exception e) {
            //e.printStackTrace();
            LOGGER.error("msetUploadBn_action", e);
        }
    }

    private boolean doMetabolomeFilter = false;

    public boolean isDoMetabolomeFilter() {
        return doMetabolomeFilter;
    }

    public void setDoMetabolomeFilter(boolean doMetabolomeFilter) {
        this.doMetabolomeFilter = doMetabolomeFilter;
    }

    private boolean doMsetFilter = true;

    public boolean isDoMsetFilter() {
        return doMsetFilter;
    }

    public void setDoMsetFilter(boolean doMsetFilter) {
        this.doMsetFilter = doMsetFilter;
    }

    private int minMsetNum = 2;

    public int getMinMsetNum() {
        return minMsetNum;
    }

    public void setMinMsetNum(int minMsetNum) {
        this.minMsetNum = minMsetNum;
    }

    private UploadedFile metabolomeFile;

    public UploadedFile getMetabolomeFile() {
        return metabolomeFile;
    }

    public void setMetabolomeFile(UploadedFile metabolomeFile) {
        this.metabolomeFile = metabolomeFile;
    }

    public void uploadMetabolomeBn_action() {
        // TODO: Process the action. Return value is a navigation
        // case name where null will return to the same page.
        try {

            RConnection RC = sb.getRConnection();
            //check if data is uploaded
            if (metabolomeFile == null) {
                DataUtils.updateMsg("Error", "Please upload your file");
                return;
            }
            if (metabolomeFile.getSize() == 0) {
                DataUtils.updateMsg("Error", "File is empty");
                return;
            }
            String fileName = DataUtils.uploadFile(metabolomeFile, sb.getCurrentUser().getHomeDir(), null, false);
            boolean res = RDataUtils.readHMDBRefLibData(RC, fileName);
            if (res) {
                DataUtils.updateMsg("OK", RDataUtils.getRefLibCheckMsg(RC));
                libOpt = "self";
            } else {
                DataUtils.updateMsg("Error", RDataUtils.getErrMsg(RC));
            }
        } catch (Exception e) {
            // e.printStackTrace();
            LOGGER.error("uploadMetabolomeBn_action", e);
        }
    }

    public String submitBtn_action() {
        RConnection RC = sb.getRConnection();
        int excludeNm = 0;
        if (doMsetFilter) {
            excludeNm = minMsetNum;
        }

        if (libOpt.equals("self")) {
            RDataUtils.setMetabolomeFilter(RC, true);
        } else {
            RDataUtils.setMetabolomeFilter(RC, false);
        }

        //sb.setMsetLibType(msetOpt);
        RDataUtils.setCurrentMsetLib(sb.getRConnection(), msetOpt, excludeNm);
        if (sb.getAnalType().equals("msetqea")) {
            return doGlobalTest();
        } else {
            return doHyperGeom();

        }
    }

    private OraBean[] oraBeans;

    public OraBean[] getOraBeans() {
        return oraBeans;
    }

    public String doHyperGeom() {
        RConnection RC = sb.getRConnection();
        String imgName = sb.getNewImage("ora");
        String imgName2 = sb.getNewImage("ora_dot");

        if (REnrichUtils.hypergeomTest(RC)) {
            REnrichUtils.plotORA(sb, imgOpt, imgName, "png", 72);
            REnrichUtils.plotEnrichmentDotPlot(sb, "ora", imgName2, "png", 72);

            if (isShowPie()) {
                REnrichUtils.plotEnrichPieChart(sb, "ora", sb.getNewImage("ora_pie"), "png", 72);
                preparePieChart(RC);
            }

            String[] rownames = REnrichUtils.getORARowNames(RC);
            String[] rowStyles = REnrichUtils.getORAColorBar(RC);
            double[][] mat = REnrichUtils.getORAMat(RC);

            oraBeans = new OraBean[rownames.length];

            for (int i = 0; i < rownames.length; i++) {
                oraBeans[i] = new OraBean(rownames[i], "background-color:" + rowStyles[i], (int) mat[i][0], mat[i][1], (int) mat[i][2], mat[i][3], mat[i][4], mat[i][5]);
            }

            createBarModel(rownames, rowStyles, mat);

            return "oraview";
        } else {
            DataUtils.updateMsg("Error", RDataUtils.getErrMsg(RC));
            return null;
        }
    }

    private void preparePieChart(RConnection RC) {
        String[] pieNames = REnrichUtils.getEnrichPieNames(RC);
        String[] pieStyles = REnrichUtils.getEnrichPieColors(RC);
        double[][] piemat = REnrichUtils.getEnrichPieHits(RC);
        createPieModel(piemat, pieNames, pieStyles);
    }

    private List<String> chemLibs = Arrays.asList("super_class", "main_class", "sub_class");

    public boolean isShowPie() {
        return chemLibs.contains(msetOpt);
    }

    private QeaBean[] qeaBeans;

    public QeaBean[] getQeaBeans() {
        return qeaBeans;
    }

    public String doGlobalTest() {
        RConnection RC = sb.getRConnection();
        if (REnrichUtils.performGlobalTest(sb) == 1) {
            String imgName = sb.getNewImage("qea");
            String imgName2 = sb.getNewImage("qea_dot");

            if (isShowPie()) {
                String imgName3 = sb.getNewImage("qea_pie");
                REnrichUtils.plotEnrichPieChart(sb, "qea", imgName3, "png", 72);
                preparePieChart(RC);
            }

            String[] rownames = REnrichUtils.getQEARowNames(RC);
            double[][] mat = REnrichUtils.getQEAMat(RC);
            qeaBeans = new QeaBean[rownames.length];
            String[] rowStyles = REnrichUtils.getQEAColorBar(RC);
            for (int i = 0; i < rownames.length; i++) {
                qeaBeans[i] = new QeaBean(rownames[i], "background-color:" + rowStyles[i], (int) mat[i][0], (int) mat[i][1],
                        mat[i][2], mat[i][3], mat[i][4], mat[i][5], mat[i][6]);
            }
            REnrichUtils.plotQEA(sb, imgOpt, imgName, "png", 72);
            REnrichUtils.plotEnrichmentDotPlot(sb, "qea", imgName2, "png", 72);
            createBarModel(rownames, rowStyles, mat);
            return "qeaview";
        } else {
            DataUtils.updateMsg("Error", RDataUtils.getErrMsg(RC));
            return null;
        }
    }

    private String msetNm;

    public void setMsetNm(String msetNm) {
        this.msetNm = msetNm;
    }

    public String getMsetImgPath() {
        String imgNm = REnrichUtils.plotQeaMset(sb, msetNm, "png", 72);
        return ab.getRootContext() + sb.getCurrentUser().getRelativeDir() + File.separator + imgNm;
    }

    public MetSetBean[] getCurrentMsetLib() {
        String[] details = REnrichUtils.getHTMLMetSet(sb.getRConnection(), msetNm);
        ArrayList<MetSetBean> libVec = new ArrayList();
        libVec.add(new MetSetBean(details[0], details[1], details[2]));
        return libVec.toArray(MetSetBean[]::new);
    }

    private String imgOpt = "net";

    public String getImgOpt() {
        return imgOpt;
    }

    public void setImgOpt(String imgOpt) {
        this.imgOpt = imgOpt;
    }

    public DefaultStreamedContent getSifFile() {
        REnrichUtils.prepareSifDownload(sb);
        return DataUtils.getDownloadFile(sb.getCurrentUser().getHomeDir() + "/metaboanalyst_enrich_sif.zip");
    }

    private HorizontalBarChartModel barModel;

    public HorizontalBarChartModel getBarModel() {
        return barModel;
    }

    public void createBarModel(String[] myLabels, String[] myColor, double[][] mat) {
        barModel = new HorizontalBarChartModel();
        ChartData data = new ChartData();

        HorizontalBarChartDataSet hbarDataSet = new HorizontalBarChartDataSet();
        //hbarDataSet.setLabel("Overview of Enriched Metabolite Sets");

        List<String> labels = new ArrayList<>();
        List<Number> values = new ArrayList<>();
        List<String> bgColor = new ArrayList<>();
        List<String> borderColor = new ArrayList<>();

        String myCol;
        int myLen = 25; //best for bar plot view 
        if (myLabels.length < 25) {
            myLen = myLabels.length;
        }
        if (sb.getAnalType().equals("msetqea")) {
            for (int i = 0; i < myLen; i++) {
                values.add(mat[i][2] / mat[i][3]);
                labels.add(myLabels[i]);
                myCol = myColor[i];
                borderColor.add("rgb(211,211,211)");
                //myCol = myCol.substring(3);
                //myCol = "rgba" + myCol.substring(0, myCol.length()- 1) + ", 0.3)";
                bgColor.add(myCol);
            }
        } else {
            for (int i = 0; i < myLen; i++) {
                values.add(mat[i][2] / mat[i][1]);
                labels.add(myLabels[i]);
                myCol = myColor[i];
                borderColor.add("rgb(211,211,211)");
                //myCol = myCol.substring(3);
                //myCol = "rgba" + myCol.substring(0, myCol.length()- 1) + ", 0.3)";
                //System.out.println("==========" + myCol + "==========");
                bgColor.add(myCol);
            }
        }

        hbarDataSet.setData(values);
        hbarDataSet.setBackgroundColor(bgColor);
        hbarDataSet.setBorderColor(borderColor);
        hbarDataSet.setBorderWidth(1);

        data.addChartDataSet(hbarDataSet);
        data.setLabels(labels);
        barModel.setData(data);

        BarChartOptions options = new BarChartOptions();
        CartesianScales cScales = new CartesianScales();
        CartesianLinearAxes linearAxes = new CartesianLinearAxes();
        linearAxes.setBeginAtZero(true);
        CartesianLinearTicks ticks = new CartesianLinearTicks();
        linearAxes.setTicks(ticks);
        cScales.addXAxesData(linearAxes);
        options.setScales(cScales);

        Title title = new Title();
        title.setDisplay(true);
        title.setText("Overview of Enriched Metabolite Sets (Top 25)");
        options.setTitle(title);
        options.setTitle(title);
        barModel.setOptions(options);
        barModel.setExtender("extender");
    }

    private PieChartModel pieModel;

    public PieChartModel getPieModel() {
        return pieModel;
    }

    private void createPieModel(double[][] myHits, String[] myLabels, String[] myColor) {

        if (pieModel == null) {
            pieModel = new PieChartModel();
        }

        ChartData piedata = new ChartData();

        PieChartDataSet piedDataSet = new PieChartDataSet();

        List<String> labels = new ArrayList<>();
        List<Number> values = new ArrayList<>();
        List<String> bgColor = new ArrayList<>();
        List<String> borderColor = new ArrayList<>();

        String myCol;
        int myLen = 15;

        if (myLabels.length < 15) {
            myLen = myLabels.length;
        }

        for (int i = 0; i < myLen; i++) {
            values.add(myHits[i][0]);
            labels.add(myLabels[i]);
            myCol = myColor[i];
            borderColor.add(myCol);
            bgColor.add(myCol);
        }

        piedDataSet.setData(values);
        piedDataSet.setBackgroundColor(bgColor);
        piedDataSet.setBorderColor(borderColor);
        //dataSet.setBorderWidth(1);

        piedata.addChartDataSet(piedDataSet);
        piedata.setLabels(labels);

        pieModel.setData(piedata);
    }

}
