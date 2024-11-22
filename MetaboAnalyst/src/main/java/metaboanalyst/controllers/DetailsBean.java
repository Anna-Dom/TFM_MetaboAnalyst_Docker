/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package metaboanalyst.controllers;

import java.io.File;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import javax.faces.view.ViewScoped;
import javax.faces.model.ListDataModel;
import javax.inject.Named;
import metaboanalyst.controllers.multifac.MultifacBean;
import metaboanalyst.models.ColumnBean;
import metaboanalyst.models.FeatureBean;
import metaboanalyst.models.MetaValueBean;
import metaboanalyst.rwrappers.ChemoMetrics;
import metaboanalyst.rwrappers.Classifying;
import metaboanalyst.rwrappers.RDataUtils;
import metaboanalyst.rwrappers.RocUtils;
import metaboanalyst.rwrappers.SigVarSelect;
import metaboanalyst.rwrappers.TimeSeries;
import metaboanalyst.rwrappers.UniVarTests;
import metaboanalyst.utils.DataUtils;
import org.primefaces.event.CellEditEvent;
import org.primefaces.event.ItemSelectEvent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.charts.ChartData;
import org.primefaces.model.charts.data.NumericPoint;
import org.primefaces.model.charts.line.LineChartDataSet;
import org.primefaces.model.charts.scatter.ScatterChartModel;
import org.rosuda.REngine.Rserve.RConnection;

/**
 *
 * @author jianguox
 */
@ViewScoped
@Named("detailsBean")
public class DetailsBean implements Serializable {

    private final ApplicationBean1 ab = (ApplicationBean1) DataUtils.findBean("applicationBean1");
    private final SessionBean1 sb = (SessionBean1) DataUtils.findBean("sessionBean1");
    private String[] colnames = null;
    private String fileName = "";
    private ListDataModel<FeatureBean> listModel = null;
    private ListDataModel<MetaValueBean> listMetaModel = null;
    private boolean init = false;
    private boolean initMeta = false;
    private boolean extraVis = false;

    //for detail view table
    private final int maxColNum = 11;
    private ColumnBean[] colVis = new ColumnBean[maxColNum];

    public boolean isColVisible(int inx) {
        if (colVis.length > inx && colVis[inx] != null) {
            return colVis[inx].isVisible();
        } else {
            return false;
        }
    }

    public String getColHeader(int inx) {
        return colVis[inx].getName();
    }

    public String getFileName() {
        return fileName;
    }

    public ListDataModel<MetaValueBean> getListMetaModel() {
        setupMetaTable();
        return listMetaModel;
    }

    public ListDataModel<FeatureBean> getFeatureBeans() {
        return listModel;
    }

    public boolean isExtraColVisible() {
        return extraVis;
    }

    public void setupMetaTable() {
        if (!initMeta) {
            RConnection RC = sb.getRConnection();
            colnames = RDataUtils.getMetaDataGroups(RC);

            colVis = new ColumnBean[maxColNum];
            for (int i = 0; i < colVis.length; i++) {
                if (i < colnames.length) {
                    colVis[i] = new ColumnBean(true, colnames[i]);
                } else {
                    colVis[i] = new ColumnBean(false, "");
                }
            }

            ArrayList<MetaValueBean> metaBeans;
            MetaValueBean mb;
            String[] rownames = RDataUtils.getSampleNames(RC);
            metaBeans = new ArrayList<>();
            for (int i = 0; i < rownames.length; i++) {
                mb = new MetaValueBean();
                mb.addName(rownames[i]);

                for (String colname : colnames) {
                    String[] vec = RDataUtils.getMetaByCol(RC, colname);
                    mb.addValue(vec[i]);
                }

                metaBeans.add(mb);
            }
            listMetaModel = new ListDataModel(metaBeans);

            MultifacBean tb = (MultifacBean) DataUtils.findBean("multifacBean");
            if (tb.getIncludedMetaData() == null) {
                tb.setIncludedMetaData(colnames);
            }
        }
        initMeta = true;
        // PrimeFaces.current().executeScript("PF('editMetaDialog').show();");

    }

    public void setupDetailsTable() {
        String from = sb.getSigSource();
        // if (!from.equals("anova") & init) { //anova allow threshold change for posthos
        //     return; //important, somehow called even in ajax 
        // }
        if (init) { //anova allow threshold change for posthos
            return; //important, somehow called even in ajax 
        }
        colnames = null;
        extraVis = false;
        String[] rownames = null;
        double[][] sigmat = null;
        String[] stringCol = null;
        fileName = "";
        RConnection RC = sb.getRConnection();
        if (from.equals("fc")) {
            rownames = UniVarTests.getFCSigRowNames(sb);
            colnames = UniVarTests.getFCSigColNames(sb);
            sigmat = UniVarTests.getFCSigMat(sb);
            fileName = "fold_change.csv";
        } else if (from.equals("tt")) {
            rownames = UniVarTests.getTTSigRowNames(sb);
            colnames = UniVarTests.getTTSigColNames(sb);
            sigmat = UniVarTests.getTTSigMat(sb);
            fileName = UniVarTests.getTtestSigFileName(sb);
        } else if (from.equals("volcano")) {
            rownames = UniVarTests.getVolcanoSigRowNames(sb);
            colnames = UniVarTests.getVolcanoSigColNames(sb);
            sigmat = UniVarTests.getVolcanoSigMat(sb);
            fileName = "volcano.csv";
        } else if (from.equals("anova")) {
            extraVis = true;
            rownames = UniVarTests.getAovSigRowNames(RC);
            colnames = UniVarTests.getAovSigColNames(RC);
            stringCol = UniVarTests.getAovPostHocSigNames(RC);
            sigmat = UniVarTests.getAovSigMat(RC);
            fileName = UniVarTests.getAnovaSigFileName(sb);
        } else if (from.equals("template")) {
            rownames = UniVarTests.getCorSigRowNames(sb);
            colnames = UniVarTests.getCorSigColNames(sb);
            sigmat = UniVarTests.getCorSigMat(sb);
            fileName = UniVarTests.getCorrSigFileName(sb);
        } else if (from.equals("ebam")) {
            sigmat = SigVarSelect.getEBAMSigMat(sb);
            rownames = SigVarSelect.getEBAMSigRowNames(sb);
            colnames = SigVarSelect.getEBAMSigColNames(sb);
            fileName = "ebam_sigfeatures.csv";
        } else if (from.equals("sam")) {
            sigmat = SigVarSelect.getSAMSigMat(sb);
            rownames = SigVarSelect.getSAMSigRowNames(sb);
            colnames = SigVarSelect.getSAMSigColNames(sb);
            fileName = "sam_sigfeatures.csv";
        } else if (from.equals("pca")) {
            sigmat = ChemoMetrics.getPCALoadings(sb);
            rownames = ChemoMetrics.getPCALoadingRowName(sb);
            colnames = ChemoMetrics.getPCALoadingColName(sb);
            fileName = "pca_loadings.csv";
        } else if (from.startsWith("pls")) {
            String spec = from.split("\\.")[1];
            sigmat = ChemoMetrics.getPLSSigMat(sb, spec);
            rownames = ChemoMetrics.getPLSSigRowNames(sb, spec);
            colnames = ChemoMetrics.getPLSSigColNames(sb, spec);
            fileName = "plsda_" + spec + ".csv";
        } else if (from.startsWith("opls")) {
            String spec = from.split("\\.")[1];
            sigmat = ChemoMetrics.getOPLSSigMat(RC, spec);
            rownames = ChemoMetrics.getOPLSSigCmpds(RC, spec);
            colnames = ChemoMetrics.getOPLSSigColNames(RC, spec);
            fileName = "oplsda_" + spec + ".csv";
        } else if (from.startsWith("spls")) {
            String spec = from.split("\\.")[1];
            sigmat = ChemoMetrics.getSPLSLoadMat(RC);
            rownames = ChemoMetrics.getSPLSLoadCmpds(RC);
            colnames = ChemoMetrics.getSPLSSigColNames(sb, spec);
            fileName = "splsda_" + spec + ".csv";
        } else if (from.equals("rf")) {
            sigmat = Classifying.getRFSigMat(sb);
            rownames = Classifying.getRFSigRowNames(sb);
            colnames = Classifying.getRFSigColNames(sb);
            fileName = "randomforests_sigfeatures.csv";
        } else if (from.equals("svm")) {
            sigmat = Classifying.getSVMSigMat(sb);
            rownames = Classifying.getSVMSigRowNames(sb);
            colnames = Classifying.getSVMSigColNames(sb);
            fileName = "svm_sigfeatures.csv";
        } else if (from.equals("anova2")) {
            rownames = TimeSeries.getAov2SigRowNames(sb.getRConnection());
            colnames = TimeSeries.getAov2SigColNames(sb.getRConnection());
            sigmat = TimeSeries.getAov2SigMat(sb.getRConnection());
            fileName = TimeSeries.getAov2SigFileName(sb);
        } else if (from.startsWith("asca")) {
            String spec = from.split("\\.")[1];
            rownames = TimeSeries.getAscaSigRowNames(sb, spec);
            colnames = TimeSeries.getAscaSigColNames(sb, spec);
            sigmat = TimeSeries.getAscaSigMat(sb, spec);
            fileName = TimeSeries.getAscaSigFileName(sb);
        } else if (from.startsWith("cov")) {
            rownames = TimeSeries.getCovSigRowNames(sb.getRConnection());
            colnames = TimeSeries.getCovSigColNames(sb.getRConnection());
            sigmat = TimeSeries.getCovSigMat(sb.getRConnection());
            fileName = TimeSeries.getCovSigFileName(sb);
        } else if (from.startsWith("roc.imp")) {
            rownames = RocUtils.getImpRowNames(sb.getRConnection());
            colnames = RocUtils.getImpColNames(sb.getRConnection());
            sigmat = RocUtils.getImpSigMat(sb.getRConnection());
            fileName = RocUtils.getRocSigFileName(sb);
        } else {
            //do nothing
        }
        //set up the view
        if (colnames == null || colnames.length == 0) {
            DataUtils.updateMsg("Error", "No results are available!");
            return;
        } else if (rownames == null || rownames.length == 0) {
            DataUtils.updateMsg("Error", "No significant features are detected using set parameters!");
            return;
        }
        colVis = new ColumnBean[maxColNum];
        for (int i = 0; i < colVis.length; i++) {
            if (i < colnames.length) {
                colVis[i] = new ColumnBean(true, colnames[i]);
            } else {
                colVis[i] = new ColumnBean(false, "");
            }
        }

        ArrayList<FeatureBean> featureBeans;
        //set up content
        if (rownames.length > 0) {
            // errTxt.setText("No result data was found!");
            //sb.setFeatureBeans(null);
            //return null;
            featureBeans = new ArrayList<>();
            FeatureBean fb;

            //This will make value pretty, but not meaningful for column sorting
            DecimalFormat formatter = new DecimalFormat("0.#####E0");

            for (int i = 0; i < rownames.length; i++) {
                fb = new FeatureBean();
                fb.addName(rownames[i]);

                for (int m = 0; m < colnames.length; m++) {
                    fb.addValue(Double.parseDouble(formatter.format(sigmat[i][m])));
                }
                if (stringCol != null) {
                    fb.addExtra(stringCol[i]);
                }
                featureBeans.add(fb);
            }
            listModel = new ListDataModel(featureBeans);
        }
        init = true;
    }

    private String cmpdImg = null;

    public String getCmpdImg() {
        if (cmpdImg == null) {
            cmpdImg = ab.getRootContext() + "/resources/images/background.png";
        }
        return cmpdImg;
    }

    public void onCellEdit(CellEditEvent<String> event) {
        String oldValue = event.getOldValue();
        String newValue = event.getNewValue();

        if (newValue != null && !newValue.equals(oldValue)) {
            //need to update feature 
            RDataUtils.updateFeatureName(sb.getRConnection(), oldValue, newValue);
            DataUtils.updateMsg("OK", "Updated from: " + oldValue + "to:" + newValue);
            //need to recompute, set states to naive
            sb.resetAnalysis();
        }
    }

    public StreamedContent getDetailFile() {
        return DataUtils.getDownloadFile(sb.getCurrentUser().getHomeDir() + File.separator + fileName);
    }

    //from user click a data point (dataInx, itemInx) to compound names
    private LinkedHashMap<String, String> pointMap;

    public void itemSelect(ItemSelectEvent event) {
        String cmpdID = pointMap.get(event.getDataSetIndex() + ":" + event.getItemIndex());
        if (!curType.equals("aov2")) {
            updateCmpdName(cmpdID);
        }
        //plotCmpd(cmpdID);
        sb.viewCmpdSummary(cmpdID);
    }

    public void itemSelectMeta(ItemSelectEvent event) {
        String cmpdID = pointMap.get(event.getDataSetIndex() + ":" + event.getItemIndex());
        //System.out.println(cmpdID);
        if (!curType.equals("aov2")) {
            updateCmpdName(cmpdID);
        }
        MultifacBean tb = (MultifacBean) DataUtils.findBean("multifacBean");
        tb.setBoxId(cmpdID);
        tb.updateBoxplotMeta();
    }

    private void updateCmpdName(String cmpdID) {
        RConnection RC = sb.getRConnection();
        UniVarTests.updateLoadingCmpd(RC, cmpdID);
    }

    //Scatterplot models containing one, two or three datasets 
    private ScatterChartModel model1, model2, model3;
    private String mdl1Type = "pca";
    private String curType;

    public ScatterChartModel getModel1(String type) {
        if (model1 == null || !type.equals(mdl1Type)) {
            update1CompModel(type);
            mdl1Type = type;
        }
        curType = type;
        return model1;
    }

    public void update1CompModel(String type) {
        model1 = new ScatterChartModel();
        pointMap = new LinkedHashMap<>();

        RConnection RC = sb.getRConnection();
        double[][] myMat;
        String[] myIDs;
        switch (type) {
            case "opls":
                myMat = ChemoMetrics.getOPLSSigMat(RC, "splot");
                myIDs = ChemoMetrics.getOPLSSigCmpds(RC, "splot");
                break;
            case "pls":
                myMat = ChemoMetrics.getPLSLoadMat(RC);
                myIDs = ChemoMetrics.getPLSLoadCmpds(RC);
                break;
            default:
                //pca
                myMat = ChemoMetrics.getPCALoadMat(RC);
                myIDs = ChemoMetrics.getPCALoadCmpds(RC);
                break;
        }

        ChartData data = new ChartData();
        LineChartDataSet myDataSet = new LineChartDataSet();
        List<Object> myValues = new ArrayList<>();
        int dataInx = 0;
        int itemInx = 0;
        for (double[] row : myMat) {
            myValues.add(new NumericPoint(row[0], row[1]));
            pointMap.put(dataInx + ":" + itemInx, myIDs[itemInx]);
            itemInx += 1;
        }
        myDataSet.setData(myValues);
        myDataSet.setBackgroundColor("rgba(153, 102, 255, 0.2)");
        myDataSet.setBorderColor("rgb(153, 102, 255)");
        myDataSet.setShowLine(false);

        data.addChartDataSet(myDataSet);
        model1.setData(data);
        model1.setExtender("extender");
    }

    private String mdl2Type = "aov";

    public ScatterChartModel getModel2(String type) {
        if (model2 == null || !type.equals(mdl2Type)) {
            update2CompModel(type);
            mdl2Type = type;
        }
        curType = type;
        return model2;
    }

    public void update2CompModel(String type) {
        RConnection RC = sb.getRConnection();
        double[][] upMat, dnMat;
        String[] upIDs, dnIDs;

        model2 = new ScatterChartModel();
        pointMap = new LinkedHashMap<>();
        ChartData data = new ChartData();
        LineChartDataSet upDataSet = new LineChartDataSet();
        List<Object> upValues = new ArrayList<>();
        LineChartDataSet dnDataSet = new LineChartDataSet();
        List<Object> dnValues = new ArrayList<>();

        switch (type) {
            case "aov":
                upMat = UniVarTests.getAnovaUpMat(RC);
                dnMat = UniVarTests.getAnovaDnMat(RC);
                upIDs = UniVarTests.getAovUpIDs(RC);
                dnIDs = UniVarTests.getAovDnIDs(RC);
                break;
            case "aov2":
                upMat = TimeSeries.getAnova2UpMat(RC);
                dnMat = TimeSeries.getAnova2DnMat(RC);
                upIDs = TimeSeries.getAnova2UpCmpds(RC);
                dnIDs = TimeSeries.getAnova2DnCmpds(RC);
                break;
            case "cov":
                upMat = TimeSeries.getCovUpMat(RC);
                dnMat = TimeSeries.getCovDnMat(RC);
                upIDs = TimeSeries.getCovUpIDs(RC);
                dnIDs = TimeSeries.getCovDnIDs(RC);
                break;
            default:
                upMat = UniVarTests.getTtUpMat(RC);
                dnMat = UniVarTests.getTtDnMat(RC);
                upIDs = UniVarTests.getTtUpIDs(RC);
                dnIDs = UniVarTests.getTtDnIDs(RC);
                break;
        }

        int dataInx = 0;
        if (upMat[0][0] != -1) {
            int itemInx = 0;
            for (double[] row : upMat) {
                upValues.add(new NumericPoint(row[0], row[1]));
                pointMap.put(dataInx + ":" + itemInx, upIDs[itemInx]);
                itemInx += 1;
            }
            upDataSet.setData(upValues);
            upDataSet.setBackgroundColor("rgba(153, 102, 255, 0.2)");
            upDataSet.setBorderColor("rgb(153, 102, 255)");
            upDataSet.setShowLine(false);
            upDataSet.setLabel("Significant" + " [" + upValues.size() + "]");

            data.addChartDataSet(upDataSet);
            dataInx += 1;
        }

        if (dnMat[0][0] != -1) {
            int itemInx = 0;
            for (double[] row : dnMat) {
                dnValues.add(new NumericPoint(row[0], row[1]));
                pointMap.put(dataInx + ":" + itemInx, dnIDs[itemInx]);
                itemInx += 1;
            }
            dnDataSet.setData(dnValues);
            dnDataSet.setBackgroundColor("rgba(201, 203, 207, 0.2)");
            dnDataSet.setBorderColor("rgb(201, 203, 207)");
            dnDataSet.setLabel("Unsignificant" + " [" + dnValues.size() + "]");
            dnDataSet.setShowLine(false);
            data.addChartDataSet(dnDataSet);
        }
        model2.setData(data);
        model2.setExtender("extender");
    }

    private String mdl3Type = "fc";

    public ScatterChartModel getModel3(String type) {
        if (model3 == null || !type.equals(mdl3Type)) {
            update3CompModel(type);
            mdl3Type = type;
        }
        curType = type;
        return model3;
    }

    public void update3CompModel(String type) {
        RConnection RC = sb.getRConnection();
        model3 = new ScatterChartModel();
        pointMap = new LinkedHashMap<>();
        ChartData data = new ChartData();
        LineChartDataSet sigLftDataSet = new LineChartDataSet();
        List<Object> sigLftValues = new ArrayList<>();
        LineChartDataSet sigRgtDataSet = new LineChartDataSet();
        List<Object> sigRgtValues = new ArrayList<>();
        LineChartDataSet unsigDataSet = new LineChartDataSet();
        List<Object> unsigValues = new ArrayList<>();

        double[][] sigDnMat, sigUpMat, unsigMat;
        String[] sigDnIDs, sigUpIDs, unsigIDs;

        if (type.equals("volcano")) {
            sigDnMat = UniVarTests.getVolcanoSigLftMat(RC);
            sigUpMat = UniVarTests.getVolcanoSigRgtMat(RC);
            unsigMat = UniVarTests.getVolcanoUnsigMat(RC);

            sigDnIDs = UniVarTests.getVolcanoSigLftIDs(RC);
            sigUpIDs = UniVarTests.getVolcanoSigRgtIDs(RC);
            unsigIDs = UniVarTests.getVolcanoUnsigIDs(RC);
        } else {
            sigDnMat = UniVarTests.getFcSigDnMat(RC);
            sigUpMat = UniVarTests.getFcSigUpMat(RC);
            unsigMat = UniVarTests.getFcUnsigMat(RC);

            sigDnIDs = UniVarTests.getFcSigDnIDs(RC);
            sigUpIDs = UniVarTests.getFcSigUpIDs(RC);
            unsigIDs = UniVarTests.getFcUnsigIDs(RC);
        }

        int dataInx = 0;
        if (sigDnMat[0][0] != -1) {
            int itemInx = 0;
            for (double[] row : sigDnMat) {
                sigLftValues.add(new NumericPoint(row[0], row[1]));
                pointMap.put(dataInx + ":" + itemInx, sigDnIDs[itemInx]);
                itemInx += 1;
            }
            sigLftDataSet.setData(sigLftValues);
            sigLftDataSet.setBackgroundColor("rgba(23, 107, 239, 0.2)");
            sigLftDataSet.setBorderColor("rgb(23, 107, 239)");
            sigLftDataSet.setShowLine(false);
            sigLftDataSet.setLabel("Sig.Down" + " [" + sigLftValues.size() + "]");

            data.addChartDataSet(sigLftDataSet);
            dataInx += 1;
        }

        if (sigUpMat[0][0] != -1) {
            int itemInx = 0;
            for (double[] row : sigUpMat) {
                sigRgtValues.add(new NumericPoint(row[0], row[1]));
                pointMap.put(dataInx + ":" + itemInx, sigUpIDs[itemInx]);
                itemInx += 1;
            }
            sigRgtDataSet.setData(sigRgtValues);
            sigRgtDataSet.setBackgroundColor("rgba(255, 62, 48, 0.2)");
            sigRgtDataSet.setBorderColor("rgb(255, 62, 48)");
            sigRgtDataSet.setShowLine(false);
            sigRgtDataSet.setLabel("Sig.Up" + " [" + sigRgtValues.size() + "]");

            data.addChartDataSet(sigRgtDataSet);
            dataInx += 1;
        }

        if (unsigMat[0][0] != -1) {
            int itemInx = 0;
            for (double[] row : unsigMat) {
                unsigValues.add(new NumericPoint(row[0], row[1]));
                pointMap.put(dataInx + ":" + itemInx, unsigIDs[itemInx]);
                itemInx += 1;
            }
            unsigDataSet.setData(unsigValues);
            unsigDataSet.setBackgroundColor("rgba(201, 203, 207, 0.2)");
            unsigDataSet.setBorderColor("rgb(201, 203, 207)");
            unsigDataSet.setShowLine(false);
            unsigDataSet.setLabel("Unsig." + " [" + unsigValues.size() + "]");

            data.addChartDataSet(unsigDataSet);
        }

        model3.setData(data);
        model3.setExtender("extender");
    }

    public LinkedHashMap<String, String> getPointMap() {
        return pointMap;
    }

    public void setPointMap(LinkedHashMap<String, String> pointMap) {
        this.pointMap = pointMap;
    }
}
