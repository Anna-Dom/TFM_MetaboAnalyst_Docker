/*
 * SessionBean1.java
 *
 * Created on Oct 21, 2008, 9:37:17 AM
 */
package metaboanalyst.controllers;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import metaboanalyst.controllers.mnet.MnetResBean;
import metaboanalyst.controllers.multifac.MultifacBean;
import metaboanalyst.models.ColorBean;
import metaboanalyst.models.SampleBean;
import metaboanalyst.models.User;
import metaboanalyst.project.UserLoginModel;
import metaboanalyst.rwrappers.RCenter;
import metaboanalyst.rwrappers.RDataUtils;
import metaboanalyst.rwrappers.UniVarTests;
import metaboanalyst.utils.DataUtils;
import metaboanalyst.utils.NaviUtils;
import org.primefaces.PrimeFaces;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.rosuda.REngine.Rserve.RConnection;

import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.inject.Named;
import java.io.File;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import org.omnifaces.util.Faces;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SessionScoped
@Named("sessionBean1")
@JsonIgnoreProperties(ignoreUnknown = true)

public class SessionBean1 implements Serializable {
    private static final Logger LOGGER = LogManager.getLogger(SessionBean1.class);

    @JsonIgnore
    private static final long serialVersionUID = 3520685098167757691L;
    @JsonIgnore
    private final ApplicationBean1 ab = (ApplicationBean1) DataUtils.findBean("applicationBean1");

    //****************user defined methods**************
    private final LinkedHashMap<String, String> naviTrack = new LinkedHashMap<>();
    /**
     * To record all commands that produce the images
     */
    private final HashMap<String, String> graphicsMap = new HashMap();
    private final int ACQUIRE_TIMEOUT = 10;
    /**
     * *****************************************
     * Methods for users managements *****************************************
     */
    private boolean loggedIn = false;
    @JsonIgnore
    private User currentUser = null;
    @JsonIgnore
    private RConnection RC = null;
    private String dataType = "unknown";
    private String analType = "unknown";
    private boolean paired = false;
    private boolean regression = false;
    private boolean keepClsOrder = true; //for multiple groups using PLS-DA and othogonal PLS-DA
    private boolean dataUploaded = false;
    private boolean dataProcessed = false;
    private boolean integChecked = false;
    private boolean dataNormed = false;
    @JsonIgnore
    private TreeNode naviTree = null;
    private String naviType = "NA";
    private LinkedHashMap<String, String> traceTrack = new LinkedHashMap<>();
    private boolean showRcmdPane = false;
    private String moduleURL = null;
    private boolean switchMode = false;
    private String sigSource = "";
    private String imgSource = null;
    private String currentPathName, currentCmpdName;
    private boolean linMod = false;
    //central place for user access control
    private boolean privileged = false;
    private String sessionToken;
    /**
     * To remember all image names in order to update immediately to avoid
     * caching problem. And remove images left previously
     */
    private HashMap<String, Integer> imgMap = new HashMap();
    private int fileCount = 0;
    private String currentPageID = "";
    private String cmpdIDType = "na";
    private String featType = "none";
    private boolean multiGroup = false;
    private boolean smallSmplSize = false;
    private int defaultFilterCutoff = 0;
    private int filterMin = 0;
    //record whether ttests or anova give significant features
    private boolean anovaSig = false;
    private boolean lmSig = false;
    private boolean ttSig = false;
    private boolean covSig = false;
    //need to remember some states cross pages
    private boolean msPeakAligned = false;
    private boolean msSpecAligned = false;
    //For graph regeneration
    private String formatOpt = "png";
    private int dpiOpt = 300;
    private String sizeOpt = "NA";
    private String imgDownloadTxt = "";
    private boolean bigFeature = false;
    private int featureNum;
    private int sampleNum;
    private boolean roc1Col = false;
    private String org = "NA";
    private String partialId = "";
    private String naviString;
    //project saving
    @JsonIgnore
    private UserLoginModel currentLoginUser = null;
    private boolean registeredLogin = false;
    private boolean saveEnabled = false;
    private String resetToken;
    private boolean dspcNet = false;
    private List<ColorBean> colorBeanLists;
    private SelectItem[] metaInfo = null;
    private String expFac;
    private String heatmapType = "NA";
    private List<String> hideRcmdPages = Arrays.asList("upload", "upset diagram");
    private List<SampleBean> sampleBeans = null;
    private String boxplotUrl;
    //temp session storage for some request bean
    private boolean jobDone = false;
    private boolean partialLinkValide = false;
    private String tsDesign = "multi";
    private int imgCount = 0;
    private String cmpdSummaryNm;
    private boolean showResumable = false;
    private String currentNaviUrl = "";

    public boolean isPrivileged() {
        return privileged;
    }

    public void setPrivileged(boolean privileged) {
        this.privileged = privileged;
    }

    public boolean doPartialLogin() {
        // Check if R scripts are compiled, if they are not, we compile them
        //  only compile them for rhistory needs
        if (!ab.isCompiled()) {
            ab.compileRScripts("rhistory");
        }

        //  If the currentUser variable is already assigned, we set it to null
        if (currentUser != null) {
            if (RC != null) {
                RC.close();
            }
            currentUser = null;
        }

        // create a new user
        currentUser = DataUtils.createTempUser(ab.getRealUserHomePath());

        // Get R connection
        RC = RCenter.getRConnection(currentUser.getHomeDir(), ab.getRscriptLoaderPath(), "rhistory");
        
        // Check R connection was successfull
        if (RC == null) {
            DataUtils.updateMsg("Error", "Cannot connect to Rserve, please start your Rserver with the right permission!");
            return false;
        }

        //  Get full path
        String bashPath = RCenter.getBashFullPath(RC);
        // Set home for R
        String rScriptHome = ab.getRscriptsPath();
        DataUtils.performResourceCleaning(bashPath, rScriptHome);

        return true;
    }

    /*
     * Log in and out
     * dataType: list, conc, specbin, pktable, nmrpeak, mspeak, msspec
     * analType: stat, pathora, pathqea, msetora, msetssp, msetqea, msetview, cmpdmap, peaksearch, smpmap
     * */
    public boolean doLogin(String dataType, String analType, boolean isRegression, boolean paired) {
        if (!ab.isCompiled()) {
            ab.compileRScripts(analType);
        }

        if (currentUser != null) {
            if (RC != null) {
                RC.close();
            }
            currentUser = null;
            if (analType.equals("roc")) {
                FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove("rocAnalBean");
            }
        }

        if (analType.equals("raw") && ab.shouldUseScheduler()) {
            currentUser = DataUtils.createRawSpecUser(ab.getRaw_spec_folder());
        } else {
            currentUser = DataUtils.createTempUser(ab.getRealUserHomePath());
        }

        String myAnalType = analType;
        if (analType.equals("mummichog")) {
            myAnalType = dataType; //"mass_all" or "mass_table" for refined R function loading
        }

        RC = RCenter.getRConnection(currentUser.getHomeDir(), ab.getRscriptLoaderPath(), myAnalType);

        if (RC == null) {
            DataUtils.updateMsg("Error", "Cannot connect to Rserve, please start your Rserver with the right permission!");
            return false;
        }

        String bashPath = RCenter.getBashFullPath(RC);
        String rScriptHome = ab.getRscriptsPath();
        DataUtils.performResourceCleaning(bashPath, rScriptHome);
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("MA5_user", currentUser);

        //setDefaultTableView();
        this.setDataType(dataType);
        this.setAnalType(analType);
        this.setPaired(paired);
        this.setRegression(isRegression);

        traceTrack.clear();
        RDataUtils.initDataObjects(RC, dataType, analType, paired);

        if ("spec".equals(dataType)) {
            //check optilcms version
            String checkres = RDataUtils.checkOptiLCMS(RC, ab.getOptiLCMSversion());
            if (!checkres.equals("TRUE")) {
                System.out.println("MP Updating your local OPtiLCMS now....");
                int upres = RDataUtils.updateOptiLCMS(RC);
                System.out.println("MP Updating finished!" + upres);
            }

            //job scheduler init path for output files
            String path = "";
            if (ab.shouldUseScheduler()) {
                path = ab.getRaw_spec_folder();
            } else if (ab.isOnLocalServer()) {
                path = RDataUtils.getPathForScheduler(RC);
            }
            path = path + currentUser.getName() + "/";
            RDataUtils.setUserPathForScheduler(RC, path);
            RDataUtils.initDataPlan(RC); //record to R
        }

        this.setLoggedIn(true);

        if (ab.isInDocker() || ab.isOnLocalServer() || ab.isOnOmicSquareServer()) {
            //if (ab.isInDocker() || ab.isOnOmicSquareServer()) {
            privileged = true;
        }

        return true;
    }

    //load existing folder
    public String doProjectLogin(String analType, String guestName, boolean isPartial) {

        if (!ab.isCompiled()) {
            ab.compileRScripts(analType);
        }
        if (currentUser != null) {
            if (RC != null) {
                RC.close();
            }
            currentUser = null;
        }

        if (!isPartial) {
            currentUser = DataUtils.loadRawUser(guestName);
            RC = RCenter.getRConnectionRawSharing(currentUser.getHomeDir(), ab.getRscriptLoaderPath(), analType);
        } else if (analType.equals("raw") && ab.shouldUseScheduler()) {
            currentUser = DataUtils.loadRawUser(guestName);
            RC = RCenter.getRConnectionRawSharing(currentUser.getHomeDir(), ab.getRscriptLoaderPath(), analType);
        } else {
            currentUser = DataUtils.loadUser(guestName);
            RC = RCenter.getCleanRConnection();
            RCenter.recordRserveConnection(RC, currentUser.getHomeDir() + guestName);
        }

        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("MA5_user", currentUser);
        if (RC == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Cannot connect to Rserver!",
                            "Please start your Rserver with the right permission!"));

            currentUser = null;
            return null;
        }

        String bashPath = RCenter.getBashFullPath(RC);
        String rScriptHome = ab.getRscriptsPath();
        DataUtils.performResourceCleaning(bashPath, rScriptHome);
        RDataUtils.initDataObjects(RC, dataType, analType, paired);
        loggedIn = true;
        if (registeredLogin) {
            saveEnabled = true;
        }

        //job scheduler init path for output files
        String path = "";
        if (ab.shouldUseScheduler()) {
            path = ab.getRaw_spec_folder();
        } else if (ab.isOnLocalServer()) {
            path = RDataUtils.getPathForScheduler(RC);
        }
        path = path + currentUser.getName() + "/";
        RDataUtils.setUserPathForScheduler(RC, path);
        if (isPartial) {
            RDataUtils.initDataPlan(RC); //record to R
        }

        setAnalType(analType);
        //PartialBean pb = ((PartialBean) DataUtils.findBean("partialBean"));
        //pb.getAllPartials();
        return analType;
    }

    public void doSpectraReset() {

        if (loggedIn) {
            if (RC != null) {
                RC.close();
            }
            loggedIn = false;
        }
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        FacesContext.getCurrentInstance().getViewRoot().getViewMap().clear();
        DataUtils.doRedirect("/MetaboAnalyst/upload/SpectraUpload.xhtml");
    }

    //1 logout and to homepage
    //0 logout only
    public void doLogout(int returnHome) {
        if (loggedIn) {
            if (RC != null) {
                if (ab.isOnLocalServer()) {
                    RCenter.showMemoryUsage(RC);
                }
                RC.close();
            }
            loggedIn = false;


            FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
            FacesContext.getCurrentInstance().getViewRoot().getViewMap().clear();

            if (returnHome == 1) {
                DataUtils.doRedirect(ab.getDomainURL());
            }
        } else {

            String rootId = FacesContext.getCurrentInstance().getViewRoot().getViewId();
            // Still need to destory the session beans once user click 'Exit' -- zhiqiang
            FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
            FacesContext.getCurrentInstance().getViewRoot().getViewMap().clear();

            if (!rootId.endsWith("home.xhtml") & returnHome == 1) {
                DataUtils.doRedirect(ab.getDomainURL());
            }
        }
        reset2DefaultState();
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }

    public boolean isPriviliedged() {
        return privileged;
    }

    public boolean isLinMod() {
        return linMod;
    }

    public void setLinMod(boolean linMod) {
        this.linMod = linMod;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public String getCurrentModelURL() {
        return moduleURL;
    }

    public void setCurrentModelURL(String currentModelURL) {
        this.moduleURL = currentModelURL;
    }

    /**
     * Record key steps
     */
    public void reset2DefaultState() {
        this.moduleURL = null;
        this.dataUploaded = false;
        this.dataProcessed = false;
        this.integChecked = false;
        this.dataNormed = false;
    }

    public void setDataUploaded() {
        this.dataUploaded = true;
        this.dataProcessed = false;
        this.integChecked = false;
        this.dataNormed = false;
    }

    public boolean isShowRcmdPane() {
        return showRcmdPane;
    }

    public void showRcmdPane() {
        this.showRcmdPane = true;
    }

    public void hideRcmdPane() {
        this.showRcmdPane = false;
    }

    public boolean isDataUploaded() {
        return dataUploaded;
    }

    public void setDataUploaded(boolean dataUploaded) {
        this.dataUploaded = dataUploaded;
    }

    public boolean isDataProcessed() {
        return dataProcessed;
    }

    public void setDataProcessed(boolean dataProcessed) {
        this.dataProcessed = dataProcessed;
        this.integChecked = false;
        this.dataNormed = false;
    }

    public boolean isIntegChecked() {
        return integChecked;
    }

    public void setIntegChecked(boolean integChecked) {
        this.integChecked = integChecked;
        this.dataNormed = false;
    }

    public boolean isDataNormed() {
        return dataNormed;
    }

    public void setDataNormed(boolean dataNormed) {
        this.dataNormed = dataNormed;
    }

    /*
     * record the pages that have been visited or is visiting
     */
    public void registerPage(String pageName) {
        if (naviTree == null) {
            return;
        }
        if (pageName.equals("Name check")) {
            String info[] = RDataUtils.getNameCheckMsgs(RC);
            int state = Integer.parseInt(info[0]);
            String msg = info[1];
            switch (state) {
                case 1:
                    break;
                case 2:
                    DataUtils.updateMsg("Warning", msg);
                    break;
                default:
                    DataUtils.updateMsg("Error", msg);
                    break;
            }
        }
//        PrimeFaces.current().executeScript("sendMsg("+ pageName +")");
        NaviUtils.getSelectedNode(naviTree, pageName);
        traceTrack.put(pageName, "0");
        currentPageID = pageName;
    }

    public String getCurrentImage(String key) {
        if (!imgMap.containsKey(key)) {
            imgMap.put(key, 0);
        }
        return key + "_" + imgMap.get(key) + "_";
    }

    public String getNewImage(String key) {
        if (!imgMap.containsKey(key)) {
            imgMap.put(key, 0);
        } else {
            imgMap.put(key, imgMap.get(key) + 1);
        }
        return key + "_" + imgMap.get(key) + "_";
    }

    public int getFileCount() {
        fileCount++;
        return fileCount;
    }

    /**
     * Get images for display
     *
     * @param name: the short image name
     * @return the image at the specified URL
     */
    //@return path to image
    public String getCurrentImageURL(String name) {
        return ab.getRootContext() + getCurrentUser().getRelativeDir() + File.separator + getCurrentImage(name) + "dpi72.png";
    }

    /**
     * get JSON files for interactive
     *
     * @param name: file name
     * @return the URL
     */
    public String getJsonDir(String name) {
        return currentUser.getRelativeDir() + "/" + getCurrentImage(name) + ".json";
    }

    public String getUserDir() {
        return "/MetaboAnalyst/" + currentUser.getRelativeDir();
    }

    public String getRawUserDir() {
        String guestName = getCurrentUser().getName();
        return "/MetaboAnalyst/resources/users/" + guestName;
    }

    public void addGraphicsCMD(String key, String rcmd) {
        graphicsMap.put(key, rcmd);
    }

    public HashMap<String, String> getGraphicsMap() {
        return graphicsMap;
    }

    //when data changed, reset to allow recomputing
    public void resetAnalysis() {
        Iterator<TreeNode> i = naviTree.getChildren().iterator();
        while (i.hasNext()) {
            TreeNode nd = i.next();
            if (nd.getData().toString().equals("Statistics")
                    || nd.getData().toString().equals("Multi-factors")
                    || nd.getData().toString().equals("Pathway")
                    || nd.getData().toString().equals("Enrichment")
                    || nd.getData().toString().equals("ROC Analysis")
                    || nd.getData().toString().equals("Power Analysis")) {
                Iterator<TreeNode> i2 = nd.getChildren().iterator();
                while (i2.hasNext()) {
                    TreeNode nd2 = i2.next();
                    traceTrack.remove(nd2.getData().toString());
                }
            }
        }
        colorBeanLists = null;
    }

    public boolean isAnalInit(String pageID) {
        return traceTrack.keySet().contains(pageID);
    }

    public String getCurrentPageID() {
        return currentPageID;
    }

    public void initNaviTree(String type) {
        if (!naviType.equals(type)) {
            LOGGER.info("Updating navigation tree: " + type);
            naviType = type;
            naviTree = NaviUtils.createNaviTree(type);
        }
    }

    public TreeNode getNaviTree() {
        if (naviTree == null) { //upload page
            TreeNode tmpTree = new DefaultTreeNode("Root", null);
            TreeNode upNode = new DefaultTreeNode("Upload", tmpTree);
            return tmpTree;
        }
        return naviTree;
    }

    public void setNaviTree(TreeNode naviTree) {
        this.naviTree = naviTree;
    }

    public String getCmpdIDType() {
        return cmpdIDType;
    }

    public void setCmpdIDType(String cmpdIDType) {
        this.cmpdIDType = cmpdIDType;
    }

    public String getFeatType() {
        return featType;
    }

    public void setFeatType(String featType) {
        this.featType = featType;
    }

    public String getNaviType() {
        return naviType;
    }

    public String getAnalType() {
        return analType;
    }

    public void setAnalType(String type) {
        analType = type;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public void setRegression(boolean regression) {
        this.regression = regression;
    }

    public boolean isPaired() {
        return paired;
    }

    public void setPaired(boolean paired) {
        this.paired = paired;
    }

    public boolean isRegresion() {
        return regression;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    public RConnection getRConnection() {
        return RC;
    }

    public void setRConnetion(RConnection RC) {
        this.RC = RC;
    }

    public boolean isMultiGroup() {
        return multiGroup;
    }

    public void setMultiGroup(boolean multiGroup) {
        this.multiGroup = multiGroup;
    }

    public boolean isKeepClsOrder() {
        return keepClsOrder;
    }

    public void setKeepClsOrder(boolean keepClsOrder) {
        this.keepClsOrder = keepClsOrder;
    }

    public boolean isSmallSmplSize() {
        return smallSmplSize;
    }

    public void setSmallSmplSize(boolean smallSmplSize) {
        this.smallSmplSize = smallSmplSize;
    }

    public int getDefaultFilterCutoff() {
        return defaultFilterCutoff;
    }

    public void setDefaultFilterCutoff(int defaultFilterCutoff) {
        this.defaultFilterCutoff = defaultFilterCutoff;
    }

    public int getFilterMin() {
        return filterMin;
    }

    public void setFilterMin(int filterMin) {
        this.filterMin = filterMin;
    }

    public boolean isLmSig() {
        return lmSig;
    }

    public void setLmSig(boolean lmSig) {
        this.lmSig = lmSig;
    }

    public boolean isCovSig() {
        return covSig;
    }

    public void setCovSig(boolean covSig) {
        this.covSig = covSig;
    }

    public boolean isAnovaSig() {
        return anovaSig;
    }

    public void setAnovaSig(boolean anovaSig) {
        this.anovaSig = anovaSig;
    }

    public boolean isTtSig() {
        return ttSig;
    }

    public void setTtSig(boolean ttSig) {
        this.ttSig = ttSig;
    }

    public String detailsLnk_action(String code) {
        sigSource = code;
        return "featuredetails";
    }

    public String getSigSource() {
        return sigSource;
    }

    public void graphicsLnk_action(String code) {
        setImgDownloadTxt("");
        imgSource = code;
    }

    public String getImageSource() {
        return imgSource;
    }

    public String getCurrentPathName() {
        return currentPathName;
    }

    public void setCurrentPathName(String currentPathName) {
        this.currentPathName = currentPathName;
    }

    public String getCurrentCmpdName() {
        return currentCmpdName;
    }

    public void setCurrentCmpdName(String currentCmpdName) {
        this.currentCmpdName = currentCmpdName;
    }

    public boolean isMsPeakAligned() {
        return msPeakAligned;
    }

    public void setMsPeakAligned(boolean msPeakAligned) {
        this.msPeakAligned = msPeakAligned;
    }

    public boolean isMsSpecAligned() {
        return msSpecAligned;
    }

    public void setMsSpecAligned(boolean msSpecAligned) {
        this.msSpecAligned = msSpecAligned;
    }

    public String getFormatOpt() {
        return formatOpt;
    }

    public void setFormatOpt(String formatOpt) {
        this.formatOpt = formatOpt;
    }

    public int getDpiOpt() {
        return dpiOpt;
    }

    public void setDpiOpt(int dpiOpt) {
        this.dpiOpt = dpiOpt;
    }

    public String getSizeOpt() {
        return sizeOpt;
    }

    public void setSizeOpt(String sizeOpt) {
        this.sizeOpt = sizeOpt;
    }

    public String getImgDownloadTxt() {
        return imgDownloadTxt;
    }

    public void setImgDownloadTxt(String imgDownloadTxt) {
        this.imgDownloadTxt = imgDownloadTxt;
    }

    public boolean isBigFeature() {
        return bigFeature;
    }

    public int getFeatureNumber() {
        return featureNum;
    }

    public int getSampleNumber() {
        return sampleNum;
    }

    public void setupDataSize(int featNum, int smplNum) {
        featureNum = featNum;
        sampleNum = smplNum;
    }

    public void updateFeatureNum(int featureNum) {
        this.featureNum = featureNum;
        bigFeature = featureNum >= 300;
    }

    public boolean isRoc1Col() {
        return roc1Col;
    }

    public void settingRoc1Col(int colNum) {
        roc1Col = colNum == 1;
    }

    public void addNaviTrack(String pageName, String naviCode) {
        currentNaviUrl = naviCode;
        naviTrack.put(pageName, "/MetaboAnalyst" + naviCode);


    }

    public void settingCurrentPage(String naviCode) {
        currentNaviUrl = naviCode;
    }

    public LinkedHashMap<String, String> getNaviTrack() {
        if (traceTrack.keySet().contains("Download")) {
            String path = traceTrack.get("Download");
            traceTrack.remove("Download");
            traceTrack.put("Download", path);
        }
        return naviTrack;
    }

    public String getOrg() {
        return org;
    }

    public void setOrg(String org) {
        this.org = org;
    }

    //There is difference in URL
    //public: https://www.metaboanalyst.ca/faces/ModuleView.xhtml          <= faces/
    //local: http://localhost:8080/MetaboAnalyst/faces/ModuleView.xhtml  <= MetaboAnalyst/faces/
    public String getUrlPrefix() {
        if (ab.isOnPublicServer()) {
            return "";
        } else {
            return "/MetaboAnalyst";
        }
    }

    public HashMap<String, Integer> getImgMap() {
        return imgMap;
    }

    public void setImgMap(HashMap<String, Integer> imgMap) {
        this.imgMap = imgMap;
    }

    public String getPartialId() {
        return partialId;
    }

    public void setPartialId(String partialId) {
        this.partialId = partialId;
    }

    public String getNaviString() {
        return naviString;
    }

    public void setNaviString(String naviString) {
        this.naviString = naviString;
    }

    public LinkedHashMap<String, String> getTraceTrack() {
        return traceTrack;
    }

    public void setTraceTrack(LinkedHashMap<String, String> traceTrack) {
        this.traceTrack = traceTrack;
    }

    public UserLoginModel getCurrentLoginUser() {
        return currentLoginUser;
    }

    public void setCurrentLoginUser(UserLoginModel currentLoginUser) {
        this.currentLoginUser = currentLoginUser;
    }

    public boolean isRegisteredLogin() {
        return registeredLogin;
    }

    public void setRegisteredLogin(boolean registeredLogin) {
        this.registeredLogin = registeredLogin;
    }

    public boolean isSaveEnabled() {
        return saveEnabled;
    }

    public void setSaveEnabled(boolean saveEnabled) {
        this.saveEnabled = saveEnabled;
    }

    public String getResetToken() {
        return resetToken;
    }

    public void setResetToken(String resetToken) {
        this.resetToken = resetToken;
    }

    public void visitHome() {
        if (registeredLogin) {
            PrimeFaces.current().executeScript("PF('logoutProjDialog').show()");
        } else {
            doLogout(1);
            DataUtils.doRedirect("/MetaboAnalyst/home.xhtml");
        }
    }

    public boolean isDspcNet() {
        return dspcNet;
    }

    public void setDspcNet(boolean dspcNet) {
        this.dspcNet = dspcNet;
    }

    public List<ColorBean> getColorBeanLists() {
        if (colorBeanLists == null) {
            setupColorPicker();
        }
        return colorBeanLists;
    }

    public void setColorBeanLists(List<ColorBean> colorBeanLists) {
        this.colorBeanLists = colorBeanLists;
    }

    public SelectItem[] getMetaInfo() {
        if (metaInfo == null) {
            setupMetaInfo();
        }
        return metaInfo;
    }

    public void setupMetaInfo() {
        if (analType.equals("mf")) {
            String[] metas = RDataUtils.getMetaInfo(RC);
            //setSelectedMetas(new String[]{metaInfo[0]});
            metaInfo = new SelectItem[metas.length];
            String meta = "";
            for (int i = 0; i < metas.length; i++) {
                meta = meta + " " + metas[i];
                metaInfo[i] = new SelectItem(metas[i], metas[i]);
            }
            setExpFac(metas[0]);
        } else {
            metaInfo = new SelectItem[]{new SelectItem("", "Group")};
        }
    }

    public String getExpFac() {
        return expFac;
    }

    public void setExpFac(String expFac) {
        this.expFac = expFac;
        setupColorPicker();
    }

    public void setupColorPicker() {
        colorBeanLists = new ArrayList<>();
        String[] grpNms = RDataUtils.getGroupNames(RC, expFac);
        if (grpNms != null && grpNms.length > 0) {
            for (String grpNm : grpNms) {
                ColorBean cb = new ColorBean(grpNm);
                cb.setShapeType(0);
                colorBeanLists.add(cb);
            }
        }
    }

    public String getHeatmapType() {
        return heatmapType;
    }

    public void setHeatmapType(String heatmapType) {
        this.heatmapType = heatmapType;
    }

    public boolean isRenderRcmdPane() {
        return loggedIn & !hideRcmdPages.contains(currentPageID.toLowerCase())
                & !analType.equals("utils") & !analType.equals("covid");
    }

    public List<SampleBean> getSampleBeans() {
        if (sampleBeans == null) {
            sampleBeans = RDataUtils.createOrigSampleBeans(RC, "Class", false);
        }
        return sampleBeans;
    }

    public void setSampleBeans(List<SampleBean> sampleBeans) {
        this.sampleBeans = sampleBeans;
    }

    public String getBoxplotUrl() {
        return boxplotUrl;
    }

    public void setBoxplotUrl(String boxplotUrl) {
        this.boxplotUrl = boxplotUrl;
    }

    public boolean isJobDone() {
        return jobDone;
    }

    public void setJobDone(boolean jobDone) {
        this.jobDone = jobDone;
    }

    public boolean isPartialLinkValide() {
        return partialLinkValide;
    }

    public void setPartialLinkValide(boolean partialLinkValide) {
        this.partialLinkValide = partialLinkValide;
    }

    public String getPartialLinkCheckingRes() {
        if (partialLinkValide) {
            return "OK. Your job link is valid! Retrieving your job. <br/>Please wait .....";
        } else {
            return "<font style='color:darkorange'>Oops! Your job link is invalid or expired. <br/>Please double check.</font>";
        }
    }

    public String getTsDesign() {
        return tsDesign;
    }

    public void setTsDesign(String tsDesign) {
        this.tsDesign = tsDesign;
    }

    public boolean isTimeOnly() {
        return tsDesign.equals("time0");
    }

    public boolean isContainsTime() {
        return tsDesign.equals("time0") || tsDesign.equals("time");
    }

    public boolean showMultiBoxView() {
        if (!analType.equals("mf")) {
            return false;
        } else {
            return tsDesign.equals("multi");
        }
    }

    public String getCmpdSummaryNm() {
        return cmpdSummaryNm;
    }

    public void setCmpdSummaryNm(String cmpdSummaryNm) {
        this.cmpdSummaryNm = cmpdSummaryNm;
    }

    public void viewCmpdSummary(String name) {
        if (getAnalType().equals("mf") && getTsDesign().equals("multi")) {
            MultifacBean tb = (MultifacBean) DataUtils.findBean("multifacBean");
            tb.setBoxId(name);
            tb.updateBoxplotMeta();
        } else {
            cmpdSummaryNm = UniVarTests.plotCmpdSummary(this, name, "NA", imgCount, "png", 72 + "");
            setCurrentCmpdName(name);
            PrimeFaces.current().executeScript("PF('FeatureView').show();");
        }
        imgCount++;
    }

    public String getCmpdSummaryImg() {
        String imgNm = getCurrentCmpdName();
        if (imgNm == null) {
            return ab.getRootContext() + "/resources/images/background.png";
        }
        //imgNm = sb.getCurrentCmpdName().replaceAll("\\/", "_");
        return ab.getRootContext() + getCurrentUser().getRelativeDir() + File.separator + cmpdSummaryNm;
    }

    public boolean isSwitchMode() {
        return switchMode;
    }

    public void setSwitchMode(boolean switchMode) {
        this.switchMode = switchMode;
    }

    public String computeDspcNet() {
        setDspcNet(true);
        int[] res = UniVarTests.computeDSPC(RC);
        if (res.length == 1 & res[0] == 0) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "DSPC failed - possible reason: some variables are highly collinear or sample size is too small."));
            return null;
        }
        MnetResBean mnb = (MnetResBean) DataUtils.findBean("mnetResBean");
        mnb.setVisMode("dspc");
        return (mnb.setupDspcNetwork(res));
    }

    public boolean isShowResumable() {
        return showResumable;
    }

    public void setShowResumable(boolean showResumable) {
        this.showResumable = showResumable;
    }

    //performPeaks2Fun;plsPermBtn_action;
    public Semaphore getPermissionToStart() {
        ResourceSemaphore resourceSemaphore = (ResourceSemaphore) DataUtils.findBean("semaphore");
        Semaphore semaphore = resourceSemaphore.getSemaphore();
        try {
            if (semaphore.tryAcquire(ACQUIRE_TIMEOUT, TimeUnit.SECONDS)) {
                // code for success
                // The user should call semaphore.release() 
                //after complete the task,otherwise, only 10 permit every 10 minutes
                return semaphore;
            } else {
                // code for timed-out
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "System busy!",
                                "Too many users are calling this function. Please try again later!"));
            }
        } catch (InterruptedException e) {
            // code for timed-out
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error!",
                            "Too many users are calling this function. Please try again later!"));
        }
        return null;
    }

    public String getCurrentNaviUrl() {
        return currentNaviUrl;
    }

    public void setCurrentNaviUrl(String currentNaviUrl) {
        this.currentNaviUrl = currentNaviUrl;
    }

}
