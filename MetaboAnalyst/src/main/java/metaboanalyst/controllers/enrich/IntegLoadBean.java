/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package metaboanalyst.controllers.enrich;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import metaboanalyst.controllers.ApplicationBean1;
import metaboanalyst.controllers.SessionBean1;
import metaboanalyst.utils.DataUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
/**
 * This bean is for tools of various ID mapping
 *
 * @author jianguox
 */
@RequestScoped
@Named("integLoader")
public class IntegLoadBean implements Serializable {

    // Section I: find beans
    private final ApplicationBean1 ab = (ApplicationBean1) DataUtils.findBean("applicationBean1");
    private final SessionBean1 sb = (SessionBean1) DataUtils.findBean("sessionBean1");
    private static final Logger LOGGER = LogManager.getLogger(IntegLoadBean.class);
    private final IntegProcessBean ipb = (IntegProcessBean) DataUtils.findBean("integProcesser");
    
    // Section II: Update Area
    public void updateListArea(int num) {
        if(!useExample) {
            // will retrun a error msg
        }
        switch (num) {
            case 1:                
                ipb.setIntegOrg("hsa");                
                cmpdL = readTabTextFile(ab.getInternalData("integ_cmpds.txt"));
                ipb.setCmpdList(cmpdL);                
                sb.setCmpdIDType("name");
                geneL = DataUtils.readTextFile(ab.getInternalData("integ_genes_1.txt"));
                ipb.setGeneList(geneL);
                ipb.setGeneIDType("symbol");
                ipb.setDatatype("cmp");
                break;
            case 2:
                ipb.setIntegOrg("hsa");
                cmpdL = "";
                ipb.setCmpdList(cmpdL);
                sb.setCmpdIDType("name");
                geneL = DataUtils.readTextFile(ab.getInternalData("integ_genes_2.txt"));
                ipb.setGeneList(geneL);                
                ipb.setGeneIDType("symbol");
                ipb.setDatatype("peak");
                ipb.setMsModeOpt("positive");
                break;
            case 3:
                ipb.setIntegOrg("hsa");                
                cmpdL = readTabTextFile(ab.getInternalData("integ_cmpds_3.txt"));
                ipb.setCmpdList(cmpdL);                
                sb.setCmpdIDType("hmdb");
                geneL = DataUtils.readTextFile(ab.getInternalData("integ_genes_3.txt"));
                ipb.setGeneList(geneL);
                ipb.setGeneIDType("symbol");
                ipb.setDatatype("cmp");
                break;
            default:
                break;
        }
    }

    public static String readTabTextFile(String filePath) {

        BufferedReader br = null;
        String text = "";
        String line;
        try {
            br = new BufferedReader(new FileReader(filePath));
            while ((line = br.readLine()) != null) {
                text = text + "\n" + line;
            }

        } catch (FileNotFoundException e) {
            LOGGER.error("readTabTextFile", e);
        } catch (IOException e) {
            LOGGER.error("readTabTextFile", e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                  //  e.printStackTrace();
                       LOGGER.error("readTabTextFile", e);
                }
            }
        }
        return text;
    }
    
    public void prepareExample(){
        //System.out.println(" ---- exampleOrder ----> " + exampleOrder);
        useExample = true;
        switch (exampleOrder) {
            case "genecmp":
                updateListArea(1);
                break;
            case "genepeak":
                updateListArea(2);
                break;
            case "protcmp":
                updateListArea(3);
                break;
            default:
                break;
        }
        
    }
    
    //Section IV: Variables, setter and getter
    private boolean useExample = ipb.isUseExample();
    private String exampleOrder = "genecmp";
    private String cmpdL = ipb.getCmpdList();
    private String geneL = ipb.getGeneList();

    public String getExampleOrder() {
        return exampleOrder;
    }

    public void setExampleOrder(String exampleOrder) {
        this.exampleOrder = exampleOrder;
    }
}
