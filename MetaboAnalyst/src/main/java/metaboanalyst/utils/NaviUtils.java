/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package metaboanalyst.utils;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javax.faces.context.FacesContext;
import metaboanalyst.controllers.SessionBean1;
import metaboanalyst.controllers.enrich.IntegProcessBean;
import metaboanalyst.controllers.metapath.MetaPathLoadBean;
import metaboanalyst.controllers.metapath.MetaPathStatBean;
import metaboanalyst.spectra.SpectraControlBean;
import org.primefaces.PrimeFaces;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

/**
 *
 * @author xia
 */
public class NaviUtils {

    private static final List<String> parentNodes = Arrays.asList(new String[]{"Processing", "Statistics", "Enrichment", "Pathway", "Multi-factors", "ID Conversion",
        "Batch Effect", "ROC Analysis", "Integrative Analysis", "Power Analysis", "Multivariate", "Univariate", "Tester", "View result", "Merging Replicates"});

    private static final List<String> twoGrpsMethods = Arrays.asList(new String[]{"T-test", "Volcano plot", "Fold change", "EBAM", "SVM", "OrthoPLSDA"});

    public static TreeNode createNaviTree(String type) {
        TreeNode naviTree = new DefaultTreeNode("Root", null);
        if (type.equalsIgnoreCase("utils")) {
            addUtilNodes(naviTree);
        } else {
            TreeNode upNode = new DefaultTreeNode("Upload", naviTree);
            //upNode.setSelectable(false);
            
            if (type.equalsIgnoreCase("config")) {
                // we add nothing
            }
            else if (type.startsWith("mummichog")) {
                addMummichogNodes(naviTree, type);
            } else if (type.equalsIgnoreCase("spec")) {
                addSpecNodes(naviTree);
            } else if (type.equalsIgnoreCase("specManager")) {
                addAdminNodes(naviTree);
            } else if (type.equalsIgnoreCase("network")) {
                addMNetworkNodes(naviTree);
            } else if (type.equalsIgnoreCase("metadata")) {
                addMetaAnalNodes(naviTree);
            } else if (type.equalsIgnoreCase("metaPath")) {
                addMetaPathNodes(naviTree);
            } else if (type.equalsIgnoreCase("pathinteg")) {
                addPathIntegNodes(naviTree);
            } else {
                addProcNodes(naviTree, type);
                if (type.startsWith("stat")) {
                    addStatNodes(naviTree);
                } else if (type.startsWith("dspc")) {
                    addDspcNodes(naviTree);
                } else if (type.startsWith("enrich")) {
                    addEnrichNodes(naviTree);
                } else if (type.startsWith("pathway")) {
                    addPathNodes(naviTree);
                } else if (type.equalsIgnoreCase("time")) {
                    addTimeNodes(naviTree);
                } else if (type.equalsIgnoreCase("power")) {
                    addPowerNodes(naviTree);
                } else if (type.equalsIgnoreCase("roc")) {
                    addRocNodes(naviTree);
                } else {
                    System.out.println("You need to define the navigation tree for this analysis type: " + type);
                }
            }

            TreeNode dn_node = new DefaultTreeNode("Download", naviTree);
        }
        TreeNode exitNd = new DefaultTreeNode("Exit", naviTree);
        return naviTree;
    }

    private static void addProcNodes(TreeNode parent, String mode) {
        TreeNode processNode = new DefaultTreeNode("Processing", parent);
        processNode.setSelectable(false);
        boolean listInput = false;
        switch (mode) {
            case "pathway":  //pathway-ora
            case "enrich": { //enrich-ora
                listInput = true;
                TreeNode nodep3 = new DefaultTreeNode("Name check", processNode);
                break;
            }
            case "enrich-ssp": {
                listInput = true;
                TreeNode nodep3 = new DefaultTreeNode("Name check", processNode);
                TreeNode nodep4 = new DefaultTreeNode("Conc. check", processNode);
                break;
            }
            case "dspc":
            case "enrich-qea":
            case "pathway-qea": {
                TreeNode nodep2 = new DefaultTreeNode("Data check", processNode);
                TreeNode nodep3 = new DefaultTreeNode("Name check", processNode);
                break;
            }
            case "time":
                listInput = true;
                TreeNode nodep211 = new DefaultTreeNode("Data check", processNode);                
                TreeNode nodep5 = new DefaultTreeNode("Missing value", processNode);
                TreeNode nodep212 = new DefaultTreeNode("Metadata check", processNode);
                TreeNode nodep6 = new DefaultTreeNode("Data filter", processNode);
                TreeNode nodep7 = new DefaultTreeNode("Data editor", processNode);
                TreeNode normNode = new DefaultTreeNode("Normalization", parent);
                break;
            case "stat-peak":
                TreeNode nodep3 = new DefaultTreeNode("Pre-process", processNode);
                TreeNode nodep2 = new DefaultTreeNode("Data check", processNode);
                break;
            default:
                TreeNode nodep21 = new DefaultTreeNode("Data check", processNode);
                break;
        }
        if (!listInput) {
            TreeNode nodep5 = new DefaultTreeNode("Missing value", processNode);
            TreeNode nodep6 = new DefaultTreeNode("Data filter", processNode);
            TreeNode nodep7 = new DefaultTreeNode("Data editor", processNode);
            TreeNode normNode = new DefaultTreeNode("Normalization", parent);
        }
    }

    private static void addStatNodes(TreeNode parent) {
        TreeNode analNode = new DefaultTreeNode("Statistics", parent);
        TreeNode nodea1 = new DefaultTreeNode("Fold change", analNode);
        TreeNode nodea2 = new DefaultTreeNode("T-test", analNode);
        TreeNode nodea3 = new DefaultTreeNode("Volcano plot", analNode);
        TreeNode nodea4 = new DefaultTreeNode("ANOVA", analNode);
        TreeNode nodea5 = new DefaultTreeNode("Correlations", analNode);
        TreeNode nodea51 = new DefaultTreeNode("DSPC network", analNode);
        TreeNode nodea6 = new DefaultTreeNode("PatternHunter", analNode);
        TreeNode nodea7 = new DefaultTreeNode("PCA", analNode);
        TreeNode nodea8 = new DefaultTreeNode("PLSDA", analNode);
        TreeNode nodea801 = new DefaultTreeNode("sPLSDA", analNode);
        TreeNode nodea81 = new DefaultTreeNode("OrthoPLSDA", analNode);
        TreeNode nodea9 = new DefaultTreeNode("SAM", analNode);
        TreeNode nodea10 = new DefaultTreeNode("EBAM", analNode);
        TreeNode nodea11 = new DefaultTreeNode("Dendrogram", analNode);
        TreeNode nodea12 = new DefaultTreeNode("Heatmap", analNode);
        TreeNode nodea13 = new DefaultTreeNode("SOM", analNode);
        TreeNode nodea14 = new DefaultTreeNode("K-means", analNode);
        TreeNode nodea15 = new DefaultTreeNode("RandomForest", analNode);
        TreeNode nodea16 = new DefaultTreeNode("SVM", analNode);
    }

    private static void addEnrichNodes(TreeNode parent) {
        TreeNode enrichNode = new DefaultTreeNode("Enrichment", parent);
        enrichNode.setSelectable(false);
        TreeNode nodee1 = new DefaultTreeNode("Set parameter", enrichNode);
        TreeNode nodee2 = new DefaultTreeNode("View result", enrichNode);
    }

    private static void addPathNodes(TreeNode parent) {
        TreeNode pathNode = new DefaultTreeNode("Pathway", parent);
        pathNode.setSelectable(false);
        TreeNode node_p1 = new DefaultTreeNode("Set parameter", pathNode);
        TreeNode node_p2 = new DefaultTreeNode("View result", pathNode);
    }

    private static void addDspcNodes(TreeNode parent) {
        TreeNode node_p1 = new DefaultTreeNode("Set parameter", parent);
        TreeNode node_p2 = new DefaultTreeNode("Network stats", parent);
        TreeNode node_p3 = new DefaultTreeNode("Network viewer", parent);
    }

    private static void addTimeNodes(TreeNode parent) {
        TreeNode tsNode = new DefaultTreeNode("Multi-factors", parent);
        //tsNode.setSelectable(false);
        TreeNode tsNode9 = new DefaultTreeNode("Metadata", tsNode);
        TreeNode tsNode1 = new DefaultTreeNode("iPCA", tsNode);
        TreeNode tsNode2 = new DefaultTreeNode("Heatmap2", tsNode);
        TreeNode tsNode6 = new DefaultTreeNode("Linear Model", tsNode);
        TreeNode tsNode8 = new DefaultTreeNode("Correlations", tsNode);
        TreeNode tsNode3 = new DefaultTreeNode("ANOVA2", tsNode);
        TreeNode tsNode4 = new DefaultTreeNode("ASCA", tsNode);
        TreeNode tsNode5 = new DefaultTreeNode("MEBA", tsNode);
        TreeNode tsNode7 = new DefaultTreeNode("RandomForest", tsNode);
    }

    private static void addPowerNodes(TreeNode parent) {
        TreeNode pathNode = new DefaultTreeNode("Power Analysis", parent);
        pathNode.setSelectable(false);
        TreeNode node_p1 = new DefaultTreeNode("Set parameter", pathNode);
        TreeNode node_p2 = new DefaultTreeNode("View result", pathNode);
    }

    private static void addPathIntegNodes(TreeNode parent) {
        TreeNode pathNode = new DefaultTreeNode("Integrative Analysis", parent);
        TreeNode node_p0 = new DefaultTreeNode("ID map", pathNode);
        pathNode.setSelectable(false);
        TreeNode node_p1 = new DefaultTreeNode("Set parameter", pathNode);
        // TreeNode node_p2 = new DefaultTreeNode("Overview", pathNode);
        TreeNode node_p3 = new DefaultTreeNode("View result", pathNode);
    }

    private static void addMNetworkNodes(TreeNode parent) {
        TreeNode nodep4 = new DefaultTreeNode("Name check", parent);
        TreeNode node_p1 = new DefaultTreeNode("Set parameter", parent);
        TreeNode node_p2 = new DefaultTreeNode("Network stats", parent);
        TreeNode node_p3 = new DefaultTreeNode("Network viewer", parent);
    }

    private static void addSpecNodes(TreeNode parent) {
        TreeNode node_p0 = new DefaultTreeNode("Spectra check", parent);
        TreeNode node_p1 = new DefaultTreeNode("Spectra processing", parent);
        TreeNode node_p2 = new DefaultTreeNode("Job status", parent);
        TreeNode node_p3 = new DefaultTreeNode("Spectra result", parent);
    }

    private static void addAdminNodes(TreeNode parent) {
        TreeNode node_p0 = new DefaultTreeNode("Job manager", parent);
        TreeNode node_p1 = new DefaultTreeNode("Spectra check", parent);
        TreeNode node_p2 = new DefaultTreeNode("Spectra processing", parent);
        TreeNode node_p3 = new DefaultTreeNode("Job status", parent);
        TreeNode node_p4 = new DefaultTreeNode("Spectra result", parent);
    }

    private static void addMetaAnalNodes(TreeNode parent) {
        TreeNode nodep4 = new DefaultTreeNode("Meta analysis", parent);
        TreeNode node_p1 = new DefaultTreeNode("Result table", parent);
        TreeNode node_p2 = new DefaultTreeNode("Upset diagram", parent);
    }

    private static void addMetaPathNodes(TreeNode parent) {
        TreeNode node_p2 = new DefaultTreeNode("Set parameter", parent);
        TreeNode node_p3 = new DefaultTreeNode("View result", parent);
        node_p3.setSelectable(false);
        node_p3.setExpanded(true);
        TreeNode node_p42 = new DefaultTreeNode("Meta paths", node_p3);
        TreeNode node_p41 = new DefaultTreeNode("Pooling peaks", node_p3);
        TreeNode node_p5 = new DefaultTreeNode("Upset diagram", parent);
        TreeNode node_p6 = new DefaultTreeNode("Network viewer", parent);
    }

    private static void addMummichogNodes(TreeNode parent, String type) {
        TreeNode procNode = new DefaultTreeNode("Processing", parent);
        TreeNode nodep = new DefaultTreeNode("Data check", procNode);
        if (type.endsWith("table")) {
            TreeNode nodep5 = new DefaultTreeNode("Missing value", procNode);
            TreeNode nodep4 = new DefaultTreeNode("Data filter", procNode);
            TreeNode nodep6 = new DefaultTreeNode("Data editor", procNode);
            TreeNode normNode = new DefaultTreeNode("Normalization", parent);
        }
        TreeNode node_p1 = new DefaultTreeNode("Set parameter", parent);
        TreeNode node_p3 = new DefaultTreeNode("View result", parent);
        //TreeNode node_p4 = new DefaultTreeNode("Metabolic network", node_p3);
        //TreeNode node_p5 = new DefaultTreeNode("Heatmap viewer", node_p3);
    }

    private static void addRocNodes(TreeNode parent) {
        TreeNode rocNode = new DefaultTreeNode("ROC Analysis", parent);
        //rocNode.setSelectable(false);
        TreeNode node_p1 = new DefaultTreeNode("Univariate", rocNode);
        TreeNode node_p11 = new DefaultTreeNode("ROC detail", node_p1);

        TreeNode node_p2 = new DefaultTreeNode("Multivariate", rocNode);
        node_p2.setSelectable(false);
        TreeNode node_p3 = new DefaultTreeNode("Set parameter", node_p2);
        TreeNode node_p4 = new DefaultTreeNode("Explorer", node_p2);

        TreeNode node_p5 = new DefaultTreeNode("Tester", rocNode);
        node_p5.setSelectable(false);
        TreeNode node_p6 = new DefaultTreeNode("Builder", node_p5);
        TreeNode node_p7 = new DefaultTreeNode("Evaluator", node_p5);
    }

    private static void addUtilNodes(TreeNode parent) {
        //TreeNode uNode = new DefaultTreeNode("Utilities", parent);
        //uNode.setSelectable(false);

        TreeNode convertNode = new DefaultTreeNode("ID Conversion", parent);
        convertNode.setSelectable(false);
        TreeNode idInputNode = new DefaultTreeNode("ID Upload", convertNode);
        TreeNode resNode = new DefaultTreeNode("Map Result", convertNode);

        TreeNode batchNode = new DefaultTreeNode("Batch Effect", parent);
        batchNode.setSelectable(false);
        TreeNode inputNode = new DefaultTreeNode("Batch Upload", batchNode);
        TreeNode viewNode = new DefaultTreeNode("Batch View", batchNode);

        TreeNode dpInputNode = new DefaultTreeNode("Replicates Upload", parent);

        TreeNode powerNode = new DefaultTreeNode("Lipidomics Upload", parent);
    }

    //find node based on the ID from tree, note, only search for three layers
    public static TreeNode getSelectedNode(TreeNode naviTree, String pageID) {
        TreeNode myNode = null;
        Iterator<TreeNode> i = naviTree.getChildren().iterator();
        while (i.hasNext()) {
            TreeNode nd = i.next();
            if (nd.getData().toString().equals(pageID)) {
                myNode = nd;
            }
            if (!nd.isLeaf()) {
                Iterator<TreeNode> i2 = nd.getChildren().iterator();
                while (i2.hasNext()) {
                    TreeNode nd2 = i2.next();
                    if (nd2.getData().toString().equals(pageID)) {
                        myNode = nd2;
                    }
                    if (!nd2.isLeaf()) {
                        Iterator<TreeNode> i3 = nd2.getChildren().iterator();
                        while (i3.hasNext()) {
                            TreeNode nd3 = i3.next();
                            if (nd3.getData().toString().equals(pageID)) {
                                myNode = nd3;
                            }
                        }
                    }
                }
            }
        }
        if (myNode != null) {
            selectNode(myNode);
        }
        return null;
    }

    public static void selectNaviTreeNode(SessionBean1 sb, TreeNode node) {

        String naviKey = node.getData().toString();
        String analType = sb.getAnalType();

        if (analType.equals("utils")) {
            FacesContext.getCurrentInstance().getApplication().getNavigationHandler()
                    .handleNavigation(FacesContext.getCurrentInstance(), "null", naviKey);
            return;
        }

        if (naviKey.equals("Exit")) {
            sb.doLogout(1);
            return;
        }
        if (!naviKey.equals("Upload") && !naviKey.equals("Exit")) {
            if (!sb.isDataUploaded()) {
                DataUtils.updateMsg("Error", "You need to upload a dataset first!");
                PrimeFaces.current().executeScript("PF('statusDialog').hide()");
                return;
            }
        }
        selectNode(node);
        String dataType = sb.getDataType();
        switch (naviKey) {
            case "Pre-process":
                if (dataType.equals("conc") || dataType.equals("specbin") || dataType.equals("pktable")) {
                    DataUtils.updateMsg("Error", "Your data type does not need this procedure!");
                    PrimeFaces.current().executeScript("PF('statusDialog').hide()");
                    return;
                }
                break;
            case "Data check":
                if (dataType.equals("conc") || dataType.equals("specbin") || dataType.equals("pktable") || dataType.equals("mass_all") || dataType.equals("mass_table")) {
                    break;
                } else if (!sb.isDataProcessed()) {
                    DataUtils.updateMsg("Error", "Your need to pre-process your data first!");
                    PrimeFaces.current().executeScript("PF('statusDialog').hide()");
                    return;
                }
            case "Name check":
                if (!dataType.equals("conc")) {
                    DataUtils.updateMsg("Error", "The procedure is only applicable to compound concentration data!");
                    PrimeFaces.current().executeScript("PF('statusDialog').hide()");
                    return;
                }
                break;
            case "Conc. check":
                if (!analType.equals("msetssp")) {
                    DataUtils.updateMsg("Error", "The procedure is only applicable to single sample profiling!");
                    PrimeFaces.current().executeScript("PF('statusDialog').hide()");
                    return;
                }
                break;
            case "Data editor":
                if (analType.startsWith("mummichog")) {
                    if (!sb.getDataType().equals("mass_table")) {
                        DataUtils.updateMsg("Error", "The option is only applicable for peak table, not peak list!");
                        PrimeFaces.current().executeScript("PF('statusDialog').hide()");
                        return;
                    }
                }

                if (!sb.isAnalInit("Normalization")) {
                    DataUtils.updateMsg("Error", "The data need to be further processed till normalization page for this procedure! ");
                    PrimeFaces.current().executeScript("PF('statusDialog').hide()");
                    return;
                }
                break;
            case "Missing value":
            case "Metadata check":
            case "Data filter":
            case "Normalization":
                if (analType.startsWith("mummichog")) {
                    if (!sb.getDataType().equals("mass_table")) {
                        DataUtils.updateMsg("Error", "The option is only applicable for peak table, not peak list!");
                        PrimeFaces.current().executeScript("PF('statusDialog').hide()");
                        return;
                    }
                } else if (!sb.isIntegChecked()) {
                    DataUtils.updateMsg("Error", "The data need to pass integrity check first!");
                    PrimeFaces.current().executeScript("PF('statusDialog').hide()");
                    return;
                }
                break;
            case "DSPC network":
                naviKey = sb.computeDspcNet();
            case "Set parameter":
                //need to work out unspecific case
                if (analType.startsWith("mset")) {
                    naviKey = "enrichparam";
                } else if (analType.startsWith("pathinteg")) {
                    IntegProcessBean ipb = (IntegProcessBean) DataUtils.findBean("integProcesser");
                    if (ipb.getDatatype().equals("peak")) {
                        naviKey = "IntegAnalPeak";
                    } else {
                        naviKey = "IntegAnal";
                    }
                } else if (analType.startsWith("path")) {
                    naviKey = "pathparam";
                } else if (analType.startsWith("power")) {
                    naviKey = "powerparam";
                } else if (analType.startsWith("network")) {
                    naviKey = "MnetParam";
                } else if (analType.startsWith("roc")) {
                    naviKey = "Multivariate";
                } else if (analType.startsWith("mummichog")) {
                    if (sb.getDataType().equals("mass_table")) {
                        naviKey = "mzlibview";
                    } else {
                        naviKey = "mzlibview";
                    }
                } else if (analType.startsWith("metapaths")) {
                    MetaPathStatBean pmb = (MetaPathStatBean) DataUtils.findBean("pMetaStatBean");
                    naviKey = "Meta-Analysis Params";
                    pmb.setResOK(false);
                    pmb.setResOK2(false);
                }
                break;
            case "View result":
                //need to work out unspecific case
                if (analType.equals("msetora") || analType.equals("msetssp")) {
                    naviKey = "oraview";
                } else if (analType.equals("msetqea")) {
                    naviKey = "qeaview";
                } else if (analType.startsWith("pathinteg")) {
                    naviKey = "integview";
                } else if (analType.startsWith("path")) {
                    naviKey = "pathview";
                } else if (analType.startsWith("power")) {
                    naviKey = "powerview";
                } else if (analType.startsWith("mummichog")) {
                    naviKey = "mummires";
                    node.setExpanded(true);
                } else if (analType.equals("gsea_peaks")) {
                    naviKey = "gseapkview";
                } else if (analType.equals("integ_peaks")) {
                    naviKey = "peakintegview";
                } else if (analType.startsWith("metapaths")) {
                    DataUtils.updateMsg("Error", "Select the result page ('Meta path' or 'Pooling peaks') you want to reach from the sub-nodes!");
                    PrimeFaces.current().executeScript("PF('statusDialog').hide()");
                    return;
                }
                break;
            case "Upload":
                if (sb.isSwitchMode()) {
                    DataUtils.updateMsg("Error", "The upload page is undefined after you switched to a new module. Please exit to restart again.");
                    PrimeFaces.current().executeScript("PF('statusDialog').hide()");
                    return;
                } else {
                    String naviPath = sb.getNaviTrack().get("Upload");
                    if (naviPath != null) {
                        DataUtils.doRedirect(naviPath);
                        return;
                    }
                }
                break;
            case "Correlations":
                if (analType.equals("mf")) {
                    naviKey = "CorrelationTest";
                }
                break;
            case "RandomForest":
                if (analType.equals("mf")) {
                    naviKey = "RandomForest2";
                }
                break;
            case "ID map":
            case "Download":
            case "Metabolic network":
                break;
            case "Spectra check":
            case "Spectra processing":
                SpectraControlBean pfb = (SpectraControlBean) DataUtils.findBean("spectraController");

                if (!pfb.isDataConfirmed() || pfb.getIncludedFileNamesString().equals("")) {
                    DataUtils.updateMsg("Error", "No right spectra files selected or confirmed!");
                    PrimeFaces.current().executeScript("PF('statusDialog').hide()");
                    return;
                }

                if (pfb.isJobSubmitted() && (pfb.getCurrentJobStatus().contains("Pending") || pfb.getCurrentJobStatus().contains("Running"))) {
                    DataUtils.updateMsg("Error", "Job is running, can not modify parameters again!");
                    PrimeFaces.current().executeScript("PF('statusDialog').hide()");
                    return;
                } else {                  
                    pfb.setStopStatusCheck(true);
                    pfb.setJobSubmitted(false);
                    pfb.setPerformedPlan(false);
                    pfb.setCurrentJobId(0);                 
                    pfb.setCurrentJobStatus("Submitting...");
                    pfb.setProgress2(0.0);
                }
                break;
            case "Spectra result":
                pfb = (SpectraControlBean) DataUtils.findBean("spectraController");

                if (!pfb.isDataConfirmed() || pfb.getIncludedFileNamesString().equals("")) {
                    DataUtils.updateMsg("Error", "No right spectra files selected or confirmed!");
                    PrimeFaces.current().executeScript("PF('statusDialog').hide()");
                    return;
                }

                if (!pfb.getCurrentJobStatus().contains("Finished") || !pfb.getFinishedJobStatus().contains("Finished")) {
                    DataUtils.updateMsg("Error", "Please wait until the job is finished!");
                    PrimeFaces.current().executeScript("PF('statusDialog').hide()");
                    return;
                }
                break;
            case "Job status":
                pfb = (SpectraControlBean) DataUtils.findBean("spectraController");

                if (!pfb.isDataConfirmed() || pfb.getIncludedFileNamesString().equals("")) {
                    DataUtils.updateMsg("Error", "No right spectra files selected or confirmed!");
                    PrimeFaces.current().executeScript("PF('statusDialog').hide()");
                    return;
                }

                String currentPage = FacesContext.getCurrentInstance().getViewRoot().getViewId();
                if (currentPage.contains("SpectraProcess")) {
                    DataUtils.updateMsg("Error", "Please click on submit job button located at the bottom of the page instead!");
                    PrimeFaces.current().executeScript("PF('statusDialog').hide()");
                    return;
                } else {
                    pfb.setCurrentJobId(pfb.getFinishedJobId());
                    pfb.setProgress2(pfb.getFinishedProgress2());
                    pfb.setCurrentJobStatus(pfb.getFinishedJobStatus());
                    pfb.setStopStatusCheck(false);
                    pfb.setJobSubmitted(true);
                    pfb.setPerformedPlan(true);
                }
                break;
            case "Meta-Analysis Params":
                break;
            case "Meta paths":
                naviKey = "Meta-Analysis Results";
                MetaPathStatBean pmb = (MetaPathStatBean) DataUtils.findBean("pMetaStatBean");

                if (pmb.isResOK() && pmb.isPlotOK()) {
                    break;
                } else {
                    PrimeFaces.current().executeScript("PF('statusDialog').hide()");
                    DataUtils.updateMsg("Error", "Your result is not ready! Click Submit button for processing from the 'Set parameter' page!");
                    return;
                }
            case "Pooling peaks":
                pmb = (MetaPathStatBean) DataUtils.findBean("pMetaStatBean");

                if (pmb.getPoolAlgOpt().equals("mummichog")) {
                    naviKey = "mummires";
                } else if (pmb.getPoolAlgOpt().equals("gsea_peaks")) {
                    naviKey = "gseapkview";
                }

                if (pmb.isResOK2()) {
                    break;
                } else {
                    PrimeFaces.current().executeScript("PF('statusDialog').hide()");
                    DataUtils.updateMsg("Error", "Your result is not ready! Click Submit button for processing from the 'Set parameter' page!");
                    return;
                }
            case "Upset diagram":
                //MetaPathLoadBean plb = (MetaPathLoadBean) DataUtils.findBean("pLoadBean");
                if (!sb.getTraceTrack().containsKey("Upset diagram")) {
                    PrimeFaces.current().executeScript("PF('statusDialog').hide()");
                    DataUtils.updateMsg("Error", "Please select datasets you wish to process by click 'UpSet diagram' button from Result page! ");
                    return;
                }
                break;
            case "Network viewer":
                MetaPathLoadBean plb = (MetaPathLoadBean) DataUtils.findBean("pLoadBean");
                if (plb.getSelectedData().getName() != null) {
                    DataUtils.updateMsg("Error", "Click 'Network Analysis' button from Results page to continue!");
                    PrimeFaces.current().executeScript("PF('statusDialog').hide()");
                    return;
                }
                break;
            case "Heatmap viewer":
                if (analType.startsWith("mummichog")) {
                    if (!sb.getDataType().equals("mass_table")) {
                        DataUtils.updateMsg("Error", "The option is only applicable for peak table, not peak list!");
                        PrimeFaces.current().executeScript("PF('statusDialog').hide()");
                        return;
                    }
                }
                break;
            default://all statisitcal 
                if (!sb.isDataNormed()) {
                    DataUtils.updateMsg("Error", "The data need to be normalized first!");
                    PrimeFaces.current().executeScript("PF('statusDialog').hide()");
                    return;
                }
                if (naviKey.equals("ASCA")) {
                    if (sb.isTimeOnly()) {
                        DataUtils.updateMsg("Error", "This method has not been tested for time-series only data!");
                        PrimeFaces.current().executeScript("PF('statusDialog').hide()");
                        return;
                    }
                } else if (naviKey.equals("MEBA")) {
                    if (!sb.isContainsTime()) {
                        DataUtils.updateMsg("Error", "This method only work on time-series data.");
                        PrimeFaces.current().executeScript("PF('statusDialog').hide()");
                        return;
                    }
                }
                if (sb.isMultiGroup()) {
                    if (twoGrpsMethods.contains(naviKey)) {
                        DataUtils.updateMsg("Error", "The method is only applicable for two-group data analysis! "
                                + "You can use <b>Data Editor</b> => <b>Edit Groups</b> to specify two groups of interest for analysis.");
                        PrimeFaces.current().executeScript("PF('statusDialog').hide()");
                        return;
                    }
                } else if (naviKey.equals("ANOVA")) {
                    DataUtils.updateMsg("Error", "The method is only for multi-group data analysis!");
                    PrimeFaces.current().executeScript("PF('statusDialog').hide()");
                    return;
                }

        }
        //System.out.println("end==========" + naviKey);

        FacesContext.getCurrentInstance().getApplication().getNavigationHandler()
                .handleNavigation(FacesContext.getCurrentInstance(), "null", naviKey);

    }

    public static void selectNode(TreeNode myNode) {
        myNode.setSelected(true);
        String parentNm = myNode.getParent().getData().toString();
        if (parentNodes.contains(parentNm)) {
            myNode.getParent().setExpanded(true);
        }
    }
}
