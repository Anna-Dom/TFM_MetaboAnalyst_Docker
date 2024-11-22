/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package metaboanalyst.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import metaboanalyst.controllers.ApplicationBean1;
import metaboanalyst.controllers.mnet.MnetLoadBean;
import metaboanalyst.controllers.SessionBean1;
import metaboanalyst.controllers.meta.MetaLoadBean;
import metaboanalyst.models.CovidBean;
import metaboanalyst.models.DataModel;
import metaboanalyst.rwrappers.RCenter;
import metaboanalyst.rwrappers.RDataUtils;
import metaboanalyst.rwrappers.RMetaUtils;
import org.primefaces.PrimeFaces;
import org.rosuda.REngine.Rserve.RConnection;

/**
 *
 * @author qiang
 */
@SessionScoped
@Named("covids")
public class covid implements Serializable {

    private final ApplicationBean1 ab = (ApplicationBean1) DataUtils.findBean("applicationBean1");
    private final SessionBean1 sb = (SessionBean1) DataUtils.findBean("sessionBean1");
    private RConnection RC;
    
    private boolean covidLogedIn = false;

    public boolean isCovidLogedIn() {
        return covidLogedIn;
    }

    public void setCovidLogedIn(boolean covidLogedIn) {
        this.covidLogedIn = covidLogedIn;
    }

    public String doCovidTsne(){
        sb.doLogin("NA", "covid", false, false);
        RC = sb.getRConnection();
        sb.hideRcmdPane();
        RDataUtils.doCovidTsne(RC);
        return "covidtsne";
    }
    private CovidBean[] coviders;

    public CovidBean[] getCoviders() {
        return coviders;
    }
        
    public String getCovidURL() {
        if (ab.isOnPublicServer()) {
            return ("https://www.metaboanalyst.ca/MetaboAnalyst/");
        } else {
            return ab.getDomainURL();
        }
    }
    
    public void redirect2covid(){
        if(ab.isOnMainServer()){
            try {
                FacesContext.getCurrentInstance().getExternalContext().redirect("https://www.metaboanalyst.ca/covid/datasets.xhtml");
            } catch (IOException ex) {
                Logger.getLogger(covid.class.getName()).log(Level.SEVERE, null, ex);
            }
        }    
    }

    public void setupDataTable() throws IOException {
        boolean condi = false;
        File f = new File("/home/glassfish/projects/covid");
        if (f.isDirectory()) {
            System.out.println("COVID data set folder exists!");
            condi = true;
        } 

        if (condi) {
            CovidBean[] coviderstmp = new CovidBean[100];

            BufferedReader br = new BufferedReader(new FileReader(ab.getResourceByAPI("covid_dataset.csv")));
            String line;
            int i = -1;
            while ((line = br.readLine()) != null) //returns a Boolean value  
            {
                i++;
                String[] record = line.split("\",\"");
                if (i != 0) {
//                    File f0 = new File("/home/glassfish/projects/covid/" + record[5].replaceAll("\"", ""));
//                    String fileNMALink = "";
//                    try {
//                        fileNMALink = "/home/glassfish/projects/covid/" + record[5].replaceAll("\"", "") + "/" + f0.list()[0];
//                    } catch (Exception ex) {
//                        System.out.println("Files not found!");
//                    }
                    String pubs = record[1].replaceAll("\"", "");
                    pubs = "<a href=" + record[3].replaceAll("\"", "") + " target='_blank'>" + pubs + "</a>";
                    //                   System.out.println("pubs-->" + pubs);
                    String dataTypes = record[7].replaceAll("\"", "");
                    coviderstmp[i - 1] = new CovidBean(
                            record[1].replaceAll("\"", ""), //fileName
                            "<a href=" + record[9].replaceAll("\"", "") + " target='_blank'>" + dataTypes + "</a>",
                            //fileNMALink, //fileNameLink
                            pubs, //Publications
                            "publink", //PublicationsLink 
                            record[5].replaceAll("\"", ""), //descriptions
                            record[6].replaceAll("\"", ""), //countries
                            dataTypes, //datatypes
                            record[8].replaceAll("\"", ""), //foldernms
                            record[0].replaceAll("\"", ""), //Ids
                            record[4].replaceAll("\"", "") //platform
                    );

                }
            }
            coviders = new CovidBean[i];
            System.arraycopy(coviderstmp, 0, coviders, 0, i);

        }
    }

    public String gofromCOVID(String module, String datasets, String comparis) {
        String URL;
        if(covidDataGeneration(sb.getRConnection(), datasets, comparis)){
            System.out.println("COVID datageneration finished!");
            URL = prepare4ModuleSwitch(module);            
        } else {
            URL = null;
            DataUtils.updateMsg("Error", "Data Preparation Failed! Report this code 'X0004521' to Maintainer!");
        }
        return URL;
    }
    
    public String prepare4ModuleSwitch(String module) {
        RC = sb.getRConnection();
        String dirPath = sb.getCurrentUser().getHomeDir();
        String filePath = getCurrentCOVIDdata(RC, module);
        switch (module) {
            case "statsx":
                //This is regular statistical analysis module
                doCOVIDLogin("conc", "stat", false, false);
                RDataUtils.readTextData(RC, dirPath + "/" +filePath, "colu", "disc");
                sb.initNaviTree("stat");
                sb.addNaviTrack("Upload", "/ModuleView.xhtml");
                sb.setDataUploaded();
                return "/MetaboAnalyst/Secure/process/SanityCheck.xhtml";
            case "markerx":
                //This is Biomarker analysis module
                doCOVIDLogin("conc", "roc", false, false);
                RDataUtils.readTextData(RC, dirPath + "/" + filePath, "colu", "disc");
                sb.initNaviTree("roc");
                sb.addNaviTrack("Upload", "/ModuleView.xhtml");
                sb.setDataUploaded();
                return "/MetaboAnalyst/Secure/process/SanityCheck.xhtml";
            case "pathx":
                //This is Pathway analysis module
                doCOVIDLogin("conc", "pathqea", false, false);
                sb.initNaviTree("pathway-qea");
                sb.addNaviTrack("Upload", "/ModuleView.xhtml");
                sb.setCmpdIDType("hmdb");

                if (RDataUtils.readTextData(RC, dirPath + "/" + filePath, "colu", "disc")) {
                    if (RDataUtils.getGroupNumber(RC) > 2) {
                        DataUtils.updateMsg("Error", "Enrichment analysis for multiple-group data is "
                                + "not well-defined. Please subset your data to two groups to proceed!");
                        return "ERROR:MultipleGroupsNotAllowed!";
                    }
                } else {
                    String err = RDataUtils.getErrMsg(sb.getRConnection());
                    DataUtils.updateMsg("Error", "Failed to read in the CSV file." + err);
                    return null;
                }
                return "/MetaboAnalyst/Secure/process/SanityCheck.xhtml";
            case "enrichx":
                doCOVIDLogin("conc", "msetqea", false, false);
                sb.initNaviTree("enrich-qea");
                sb.addNaviTrack("Upload", "/ModuleView.xhtml");
                sb.setCmpdIDType("hmdb");
                sb.setFeatType("met");
                
                if (RDataUtils.readTextData(RC, dirPath + "/" + filePath, "colu", "disc")) {
                    //double check for multiple class, this will be issue for large sample
                    if (RDataUtils.getGroupNumber(RC) > 2) {                        
                        return "ERROR:MultipleGroupsNotAllowed!";
                    }
                    return "/MetaboAnalyst/Secure/process/SanityCheck.xhtml";
                } else {
                    String err = RDataUtils.getErrMsg(sb.getRConnection());
                    DataUtils.updateMsg("Error", "Failed to read in the CSV file." + err);
                    return null;
                }
            case "netx":
                doCOVIDLogin("conc", "network", false, false);
                sb.initNaviTree("network");
                sb.addNaviTrack("Upload", "/ModuleView.xhtml");
                sb.setCmpdIDType("hmdb");
                convert2List(RC);
                MnetLoadBean mnb = (MnetLoadBean) DataUtils.findBean("mnetLoader");
                RDataUtils.setOrganism(sb.getRConnection(), "hsa");
                String cmpdList = DataUtils.readTextFile(dirPath + "/cmpd_list.txt");
                mnb.setCmpdList(cmpdList);                
                mnb.setGeneList("");
                if("MnetID map".equals(mnb.integrityCheck())){
                    return "/MetaboAnalyst/Secure/network/MnetMapView.xhtml";
                } else {
                    return null;
                }                
            case "metax":
                doCOVIDLogin("conc", "metadata", false, false);
                sb.initNaviTree("metadata");
                sb.addNaviTrack("Upload", "/upload/MetaLoadView.xhtml");
                MetaLoadBean mlb = (MetaLoadBean) DataUtils.findBean("loadBean");
                mlb.setLoggedIn(true);
                mlb.setRC(RC);
                
                int resenough = dataReducing4meta(RC);                
                if(resenough == 0){
                    return "ERROR:NOenoughComoundInsected";
                } else {
                    PrimeFaces.current().executeScript("PF('statusDialog').show()");
                }
                int ffNum = filePath.split(",").length;
                String ffs[] = filePath.split(",");

                List<DataModel> metaDataSets = mlb.getDataSets();
                metaDataSets.clear();

                for (int f = 0; f < ffNum; f++) {
                    String inpath = dirPath + "/" + ffs[f];
                    performMetaSetting(inpath, mlb, metaDataSets, f, RC);                    
                }

                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "OK", ffNum + " datasets were uploaded and processed on the server.");
                FacesContext.getCurrentInstance().addMessage(null, msg);

                sb.setDataUploaded(true);
                sb.setDataNormed(true);
                return "/MetaboAnalyst/upload/MetaLoadView.xhtml";
            default:
                return null;
        }
    }
    
    private void performMetaSetting(String inpath, MetaLoadBean mlb, List<DataModel> metaDataSets, int f, RConnection RC){    
        String name = DataUtils.getJustFileName(inpath);
        //System.out.println("name --> " + name);
        mlb.addNewData("Upload");
        RMetaUtils.readIndExpressTable(RC, name, "colu");
        DataModel selectedData;        
        selectedData = metaDataSets.get(f);
        selectedData.setName(name);
        selectedData.setRC(RC);
        mlb.setSelectedData(selectedData);
        mlb.perfromDefaultMetaProcess(selectedData);    
    }
    
    private void doCOVIDLogin(String dataType, String analType, boolean isRegression, boolean paired){
        String bashPath = RCenter.getBashFullPath(RC);
        String rScriptHome = ab.getRscriptsPath();
        DataUtils.performResourceCleaning(bashPath, rScriptHome);
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("MA5_user", sb.getCurrentUser());

        //Need to load specific Rscript based on analType
        RCenter.loadRScripts(RC, analType);
        sb.setDataType(dataType);
        sb.setAnalType(analType);
        sb.setPaired(paired);
        sb.setRegression(isRegression);

        RDataUtils.initDataObjects(RC, dataType, analType, paired);    
    }
    
    public static boolean covidDataGeneration(RConnection RC, String datasets, String comparisons) {
        try {
            String rCommand = "datageneration(\"" + datasets + "\",\"" + comparisons + "\");";
            int res = RC.eval(rCommand).asInteger();
            return res == 1;
        } catch (Exception e) {
            System.out.println("covidDataGeneration Failed!");
        }
        return false;
    }
    
    public static String getCurrentCOVIDdata(RConnection RC, String module) {
        try {
            String rCommand = "getCurrentCOVIDdata(\"" + module + "\");";
            return RC.eval(rCommand).asString();
        } catch (Exception e) {
            System.out.println("getCurrentCOVIDdata Failed!");
        }
        return null;
    }
        
    private static int convert2List(RConnection RC){
        try {
            String rCommand = "prepareCMPDlist();";
            return RC.eval(rCommand).asInteger();
        } catch (Exception e) {
            System.out.println("prepareCMPDlist Failed!");
        }
        return 0;
    }
    
    private static int dataReducing4meta(RConnection RC){
            try {
            String rCommand = "dataReducing4meta();";
            return RC.eval(rCommand).asInteger();
        } catch (Exception e) {
            System.out.println("dataReducing4meta Failed!");
        }
        return 0;
    }

}
