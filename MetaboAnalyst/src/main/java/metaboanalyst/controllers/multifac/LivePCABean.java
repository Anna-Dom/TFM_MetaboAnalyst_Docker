/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package metaboanalyst.controllers.multifac;

import java.io.Serializable;
import java.util.List;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import metaboanalyst.controllers.SessionBean1;
import metaboanalyst.models.MetaDataBean;
import metaboanalyst.rwrappers.ChemoMetrics;
import metaboanalyst.rwrappers.TimeSeries;
import metaboanalyst.utils.DataUtils;
import org.primefaces.PrimeFaces;

/**
 *
 * @author xia
 */
@RequestScoped
@Named("livePcaBean")
public class LivePCABean implements Serializable {

    private final SessionBean1 sb = (SessionBean1) DataUtils.findBean("sessionBean1");
    private final MultifacBean tb = (MultifacBean) DataUtils.findBean("multifacBean");
    private final String pageID = "iPCA";
    private int pcaPairNum = 5;

    public int getPcaPairNum() {
        return pcaPairNum;
    }

    public void setPcaPairNum(int pcaPairNum) {
        this.pcaPairNum = pcaPairNum;
    }

    private String colOpt;

    public String getColOpt() {
        if (colOpt == null) {
            colOpt = tb.getMetaDataBeans().get(0).getName();
        }
        return colOpt;
    }

    public void setColOpt(String colOpt) {
        this.colOpt = colOpt;
    }

    private String expOpt = "score";

    public String getExpOpt() {
        return expOpt;
    }

    public void setExpOpt(String expOpt) {
        this.expOpt = expOpt;
    }

    private String shapeOpt;

    public String getShapeOpt() {
        if (shapeOpt == null) {
            List<MetaDataBean> beans = tb.getMetaDataBeans();
            for (int i = 1; i < beans.size(); i++) {
                if (!beans.get(i).getParam().equals("cont")) {
                    shapeOpt = tb.getMetaDataBeans().get(i).getName();
                    break;
                }
            }
        }
        return shapeOpt;
    }

    public void setShapeOpt(String shapeOpt) {
        this.shapeOpt = shapeOpt;
    }

    public void initPCA3D() {
        if (!sb.isAnalInit(pageID)) {
            sb.registerPage(pageID);
            ChemoMetrics.initPCA(sb);

            TimeSeries.plotPCAPairSummaryMeta(sb, sb.getNewImage("pca_pair_meta"), "png", 72, pcaPairNum, getColOpt(), getShapeOpt());
            TimeSeries.initIPCA(sb.getRConnection(), sb.getNewImage("ipca_3d") + ".json", colOpt, shapeOpt);
        }
    }

    public void updatePCA3D() {
        sb.registerPage(pageID);
        ChemoMetrics.initPCA(sb);
        TimeSeries.plotPCAPairSummaryMeta(sb, sb.getNewImage("pca_pair_meta"), "png", 72, pcaPairNum, getColOpt(), getShapeOpt());
        TimeSeries.initIPCA(sb.getRConnection(), sb.getNewImage("ipca_3d") + ".json", colOpt, shapeOpt);
    }

    public String pcaPairBtn_action() {
        List<MetaDataBean> beans = tb.getMetaDataBeans();
        for (int i = 0; i < beans.size(); i++) {
            String type = beans.get(i).getParam();
            String name = beans.get(i).getName();
            if (name.equals(shapeOpt)) {
                if (type.equals("cont")) {
                    DataUtils.updateMsg("Error", "Continuous meta-data can not be used for data point shapes.");
                    return null;
                }
            }
        }

        TimeSeries.plotPCAPairSummaryMeta(sb, sb.getNewImage("pca_pair_meta"), "png", 72, pcaPairNum, colOpt, shapeOpt);
        PrimeFaces.current().scrollTo("ac:form1:pairPane");
        return null;
    }
}
