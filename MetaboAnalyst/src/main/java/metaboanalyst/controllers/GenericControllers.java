/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package metaboanalyst.controllers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import javax.faces.context.FacesContext;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.menu.DefaultMenuItem;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.MenuModel;
import org.rosuda.REngine.Rserve.RConnection;
import metaboanalyst.controllers.multifac.MultifacBean;
import metaboanalyst.models.RcmdBean;
import metaboanalyst.project.UserLoginBean;
import metaboanalyst.project.UserLoginModel;
import metaboanalyst.rwrappers.RCenter;
import metaboanalyst.rwrappers.RDataUtils;
import metaboanalyst.rwrappers.RNetworkUtils;
import metaboanalyst.utils.DataUtils;
import metaboanalyst.utils.NaviUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author xia
 */
@RequestScoped
@Named("ctl")
public class GenericControllers implements Serializable {

    private final ApplicationBean1 ab = (ApplicationBean1) DataUtils.findBean("applicationBean1");
    private final SessionBean1 sb = (SessionBean1) DataUtils.findBean("sessionBean1");
    private static final Logger LOGGER = LogManager.getLogger(GenericControllers.class);

    /*
     * navigation tree
     */
    public void onNodeSelect(NodeSelectEvent event) {
        NaviUtils.selectNaviTreeNode(sb, event.getTreeNode());
    }

    public ArrayList<RcmdBean> getCmdVec() {
        ArrayList<RcmdBean> myCmds = null;
        if (sb.isLoggedIn()) {
            myCmds = new ArrayList<>();
            RConnection RC = sb.getRConnection();
            String[] cmds = RCenter.getRCommandHistory(RC);
            if (cmds != null) {
                for (int i = 0; i < cmds.length; i++) {
                    myCmds.add(new RcmdBean(i + 1 + "", cmds[i]));
                }
            }
        }
        return myCmds;
    }

    private static final String origStyle = "color: #222222; text-decoration: underline";
    private static final String highlightStyle = "color: maroon; text-decoration: none";

    public MenuModel getSimpleMenuModel() {
        MenuModel simpleMenuModel = new DefaultMenuModel();
        DefaultMenuItem menuItem = new DefaultMenuItem();
        menuItem.setValue("Home");
        menuItem.setUrl("/MetaboAnalyst/home.xhtml");
        simpleMenuModel.getElements().add(menuItem);
        //menuItem = new DefaultMenuItem();
        //menuItem.setValue("Module");
        //menuItem.setUrl("/MetaboAnalyst/ModuleView.xhtml");
        //simpleMenuModel.getElements().add(menuItem);
        LinkedHashMap<String, String> naviTrack = sb.getNaviTrack();
        String currentPageID = sb.getCurrentPageID();
        for (String key : naviTrack.keySet()) {
            //System.out.println("========="+key);
            String url = naviTrack.get(key);
            menuItem = new DefaultMenuItem();
            menuItem.setValue(key);
            menuItem.setUrl(url);
            if (key.equals(currentPageID)) {
                menuItem.setStyle(highlightStyle);
            } else {
                menuItem.setStyle(origStyle);
            }
            simpleMenuModel.getElements().add(menuItem);
        }
        return simpleMenuModel;
    }

    public DefaultStreamedContent getRCmdFile() {
        try {
            RDataUtils.saveRCommands(sb.getRConnection());
            return DataUtils.getDownloadFile(sb.getCurrentUser().getHomeDir() + "/Rhistory.R");
        } catch (Exception e) {
            //e.printStackTrace();
            LOGGER.error("getRCmdFile", e);
        }
        return null;
    }

    public String toModuleView() {
        return ((ApplicationBean1) DataUtils.findBean("applicationBean1")).getDynamicsDomainURL() + "ModuleView.xhtml";
        //return ((ApplicationBean1) DataUtils.findBean("applicationBean1")).getDomainURL() + "ModuleView.xhtml";
    }

    public String goToLoginView() {
        if (ab.isOnPublicServer()) {
            DataUtils.doRedirect("https://www.metaboanalyst.ca/MetaboAnalyst/user/LoginView.xhtml");
        } else {
            return "login";
        }
        return null;
    }

    public String goToSigninView() {
        if (ab.isOnPublicServer()) {
            DataUtils.doRedirect("https://www.metaboanalyst.ca/MetaboAnalyst/user/LoginView.xhtml");
        } else {
            return "signin";
        }
        return null;
    }

    private String userType = "guest";

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    private String computingNode = "new";

    public String getComputingNode() {
        return computingNode;
    }

    public void setComputingNode(String computingNode) {
        this.computingNode = computingNode;
    }

    private final String genApURL = "https://genap.metaboanalyst.ca/MetaboAnalyst/upload/SpectraUpload.xhtml";
    private final String mainGuestURL = "https://www.metaboanalyst.ca/MetaboAnalyst/upload/SpectraUpload.xhtml";
    private final String mainLogInURL = "https://www.metaboanalyst.ca/MetaboAnalyst/user/LoginView.xhtml";
    private final String newGuestURL = "https://new.metaboanalyst.ca/MetaboAnalyst/upload/SpectraUpload.xhtml";
    private final String newLogInURL = "https://new.metaboanalyst.ca/MetaboAnalyst/user/LoginView.xhtml";

    public String goToSpectraUpload() {
        String myURL;
        //
        if (ab.isOnNewServer()) {
            myURL = newGuestURL; //new server URL;

        } else if (ab.isOnMainServer() || ab.isOnDevServer() || ab.isOnGenap()) {
            //dev case
            if (userType.equals("guest")) {
                myURL = mainGuestURL;
            } else {
                myURL = mainLogInURL;
            }
        } else { // local
            myURL = ab.getDomainURL() + "upload/SpectraUpload.xhtml";
        }

        return myURL;
    }

    public String setUtilOpt(String utilOpt) {
        // if (doLogin("NA", "utils", false, false)) {
        switch (utilOpt) {
            case "convert":
                return "ID Upload";
            case "lipid":
                return "Lipidomics Upload";
            case "batch":
                // STRONG WARNING: should not use DataUtils.redirect here!
                if (ab.shouldUseScheduler()) {
                    return "Batch Upload";
                } else if (ab.isOnPublicServer()) {
                    try {
                        FacesContext.getCurrentInstance().getExternalContext().redirect("https://www.metaboanalyst.ca/MetaboAnalyst/upload/BatchUpload.xhtml");
                        return null;
                    } catch (Exception e) {
                        //e.printStackTrace();
                        LOGGER.error("setUtilOpt", e);
                    }
                } else { //local
                    return "Batch Upload";
                }
            case "replicates":
                return "Replicates Upload";
        }
        return null;
    }

    public String enterModule() {
        String analType = sb.getAnalType();
        switch (analType) {
            case "stat":
                return "Statistics";
            case "msetqea":
                return "enrichparam";
            case "pathqea":
                return "pathparam";
            case "network":
                if (!sb.getCmpdIDType().equals("pklist")) {
                    RConnection RC = sb.getRConnection();
                    int res = RNetworkUtils.prepareNetworkData(RC); // for concentration table input
                    if (res == 0) {
                        DataUtils.updateMsg("Error", RDataUtils.getErrMsg(RC));
                        return null;
                    }
                }
                // since called from Normalization page
                sb.setDspcNet(true);
                return "MnetParam";
            case "mf":
                MultifacBean tb = (MultifacBean) DataUtils.findBean("multifacBean");
                tb.setAscaInit(false);
                return "Multi-factors";
            case "power":
                return "powerparam";
            case "roc":
                return "ROC Analysis";
            case "mummichog":
                return "mzlibview";
        }
        return null;
    }

    public String getModuleURL(int num) {
        String moduleURL = sb.getCurrentModelURL();
        if (moduleURL == null) {
            moduleURL = ab.getModuleURL();
            sb.setCurrentModelURL(moduleURL);
        }
        String url1;

        switch (num) {
            case 0:
                url1 = "/upload/PeakUploadView.xhtml";
                break;
            case 1:
                url1 = "/upload/MetaPathLoadView.xhtml";
                break;
            case 2:
                url1 = "/upload/EnrichUploadView.xhtml";
                break;
            case 3:
                url1 = "/upload/PathUploadView.xhtml";
                break;
            case 4:
                url1 = "/upload/JointUploadView.xhtml";
                break;
            case 5:
                url1 = "/upload/MnetUploadView.xhtml";
                break;
            case 6:
                url1 = "/upload/StatUploadView.xhtml";
                break;
            case 7:
                url1 = "/upload/MultifacUploadView.xhtml";
                break;
            case 8:
                url1 = "/upload/RocUploadView.xhtml";
                break;
            case 9:
                url1 = "/upload/MetaLoadView.xhtml";
                break;
            case 10:
                url1 = "/upload/PowerUploadView.xhtml";
                break;
            case 11:
                url1 = "/upload/ConfigurationFile.xhtml";
            default:
                url1 = "/upload/StatUploadView.xhtml";
        }

 if (sb.isRegisteredLogin()) {
            UserLoginBean ulb = (UserLoginBean) DataUtils.findBean("userLoginBean");
            UserLoginModel currentLoginUser = sb.getCurrentLoginUser();
            String sessionToken = sb.getSessionToken();
            if (ulb.isJustloggedin()) {
                int id = (int) currentLoginUser.getId();
                sessionToken = ulb.keepLoggedinToken(id);
                ulb.setJustloggedin(false);
                //System.out.println("sessionToken --> " + sessionToken);
            } else if (sessionToken == null) {
                int id = (int) currentLoginUser.getId();
                sessionToken = ulb.keepLoggedinToken(id);
                ulb.setJustloggedin(false);
                //System.out.println("new sessionToken --> " + sessionToken);
            }
            //System.out.println("NOW sessionToken is --> " + sessionToken);
            String url2 = "/faces" + url1 + "?redirToken=" + sessionToken;
            //return moduleURL + url2;
            return "https://www.metaboanalyst.ca/MetaboAnalyst" + url2; //temp fix
        }

        return moduleURL + url1;
    }

    public void doExit() {
        sb.doLogout(1);
    }
}
