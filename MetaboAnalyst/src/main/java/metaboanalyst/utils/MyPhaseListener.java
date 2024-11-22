/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package metaboanalyst.utils;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.concurrent.Semaphore;
import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.faces.context.ExternalContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.imageio.ImageIO;
import metaboanalyst.controllers.ApplicationBean1;
import metaboanalyst.controllers.ResourceSemaphore;
import metaboanalyst.controllers.enrich.MsetBean;
import metaboanalyst.controllers.SessionBean1;
import metaboanalyst.controllers.enrich.PathBean;
import metaboanalyst.controllers.mnet.MnetLoadBean;
import metaboanalyst.controllers.mnet.MnetResBean;
import metaboanalyst.controllers.mummichog.MummiAnalBean;
import metaboanalyst.project.ProjectBean;
import metaboanalyst.project.UserLoginBean;
import metaboanalyst.rwrappers.RDataUtils;
import metaboanalyst.rwrappers.REnrichUtils;
import metaboanalyst.rwrappers.RGraphUtils;
import metaboanalyst.controllers.metapath.MetaPathLoadBean;
import metaboanalyst.controllers.multifac.MultifacBean;
import metaboanalyst.rwrappers.RNetworkUtils;
import metaboanalyst.rwrappers.RSpectraUtils;
import metaboanalyst.spectra.SpectraControlBean;
import metaboanalyst.spectra.SpectraProcessBean;
import metaboanalyst.spectra.SpectraUploadBean;
import org.rosuda.REngine.Rserve.RConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.PrimeFaces;

/**
 *
 * @author Jeff
 */
public class MyPhaseListener implements PhaseListener {

    private static final String AJAX_VIEW_ID = "AjaxCall";
    public static final String USER_SESSION_KEY = "MA5_user";
    private static final String PartialPersistence = "Share";
    private static final String LoadingProject = "LoadProject";
    private static final String JobManager = "JobManager";
    private static final String resetToken = "ResetView";
    private static final String redirToken = "upload";


    private static final Logger LOGGER = LogManager.getLogger(MyPhaseListener.class);

    @Override
    public void afterPhase(PhaseEvent event) {

        FacesContext context = event.getFacesContext();
        String rootId = event.getFacesContext().getViewRoot().getViewId();
        FacesContext fc = FacesContext.getCurrentInstance();
        ELContext elc = fc.getELContext();
        ExpressionFactory ef = fc.getApplication().getExpressionFactory();
        ValueExpression ve = ef.createValueExpression(elc, "#{sessionBean1}", SessionBean1.class);
        SessionBean1 sb = (SessionBean1) ve.getValue(elc);

        if (rootId.contains(AJAX_VIEW_ID)) {
            handleAjaxRequest(event);
        } else if (rootId.contains(JobManager)) {
            ValueExpression ve2 = ef.createValueExpression(elc, "#{userLoginBean}", UserLoginBean.class);
            UserLoginBean ub = (UserLoginBean) ve2.getValue(elc);

            if (!(sb.isRegisteredLogin() && ub.isJobManager())) {
                context.getApplication().
                        getNavigationHandler().handleNavigation(context,
                                "*", "Exit");
            }

        } else if (rootId.contains(PartialPersistence)) {
            handlePartialRequest(event);
        } else if (rootId.contains(resetToken)) {
            handleResetRequest(event);
        } else if (rootId.contains(redirToken)) {
            handleRedirRequest(event);
        } else if (rootId.endsWith("loadingGenap")) {
            handleLoadingGenapProjectRequest(event);
        } else if (rootId.contains(LoadingProject)) {
            handleLoadingProjectRequest(event);
        } else if (!userExists(context)) {
            // send the user to the login view
            if (requestingSecureView(context)) {
                context.getApplication().
                        getNavigationHandler().handleNavigation(context,
                                "*", "Exit");
            }
        }
    }

    @Override
    public void beforePhase(PhaseEvent event) {
    }

    @Override
    public PhaseId getPhaseId() {
        return PhaseId.RESTORE_VIEW;
    }

    // --------------------------------------------------------- Private Methods       
    /**
     * <p>
     * Determine if the user has been authenticated by checking the session for
     * an existing <code>Wuser</code> object.</p>
     *
     * @param context the <code>FacesContext</code> for the current request
     * @return <code>true</code> if the user has been authenticated, otherwise
     * <code>false</code>
     */
    private boolean userExists(FacesContext context) {
        ExternalContext extContext = context.getExternalContext();
        return (extContext.getSessionMap().containsKey(USER_SESSION_KEY));
    }

    /**
     * <p>
     * Determines if the requested view is one of the login pages which will
     * allow the user to access them without being authenticated.</p>
     *
     * <p>
     * Note, this implementation most likely will not work if the
     * <code>FacesServlet</code> is suffix mapped.</p>
     *
     * @param context the <code>FacesContext</code> for the current request
     * @return <code>true</code> if the requested view is allowed to be accessed
     * without being authenticated, otherwise <code>false</code>
     */
    private boolean requestingSecureView(FacesContext context) {
        ExternalContext extContext = context.getExternalContext();
        String path = extContext.getRequestPathInfo();
        if (path == null) {
            return false; //hone page?
        } else {
            return path.startsWith("/Secure");
        }
    }

    private void handleAjaxRequest(PhaseEvent event) {

        ApplicationBean1 ab = (ApplicationBean1) DataUtils.findBean("applicationBean1");
        SessionBean1 sb = (SessionBean1) DataUtils.findBean("sessionBean1");

        FacesContext context = event.getFacesContext();
        HttpServletResponse response = (HttpServletResponse) context.getExternalContext().getResponse();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        String funcName = request.getParameter("function");
        String res = "";
        String prefix = sb.getUrlPrefix(); //

        if (funcName.equalsIgnoreCase("redraw")) {
            //String analType = (String) request.getParameter("analType");
            double zoomPercent = Double.parseDouble(request.getParameter("zoompc"));
            double panXPercent = Double.parseDouble(request.getParameter("panXpc"));
            double panYPercent = Double.parseDouble(request.getParameter("panYpc"));
            int canvasDimX = Integer.parseInt(request.getParameter("width"));
            int canvasDimY = Integer.parseInt(request.getParameter("height"));
            //save the original value
            int canvasDimXo = canvasDimX;
            int canvasDimYo = canvasDimY;

            if (zoomPercent == 100) { //if no scaling needs to be done, pan is irrelevant
                panXPercent = 0;
                panYPercent = 0;
            } else {      //otherwise....
                panXPercent = ((100 - zoomPercent) * panXPercent) / (100 - zoomPercent);  //readjust X pan for larger image
                panYPercent = ((100 - zoomPercent) * panYPercent) / (100 - zoomPercent);  //readjust Y pan for larger image
            }

            canvasDimX = (int) ((zoomPercent / 100) * canvasDimX + 0.5);     //readjust canvas dim x based on scaling
            canvasDimY = (int) ((zoomPercent / 100) * canvasDimY + 0.5);   //readjust canvas dim y based on scaling

            long current = System.currentTimeMillis();
            String zoomImgName = "zoom" + current++ + ".png";

            res = "zoomImgURL=\'NA\'";
            RConnection RC = sb.getRConnection();

            boolean plotRes = RGraphUtils.drawMetPAGraph(RC, zoomImgName, canvasDimX, canvasDimY, zoomPercent);

            if (plotRes) {
                String convertPath = RGraphUtils.getConvertFullPath(RC);
                if (convertPath.equals("NA")) {

                } else {
                    int cropright = (int) ((canvasDimX - canvasDimXo) * panXPercent / 100);
                    int croptop = (int) ((canvasDimY - canvasDimYo) * panYPercent / 100);
                    if (croptop < 0) {
                        int wd = 0 - croptop;
                        croptop = 0;
                        cropright = cropright + wd;
                    }
                    if (cropright < 0) {
                        int wd = 0 - cropright;
                        cropright = 0;
                        croptop = croptop + wd;
                    }
                    String zmImgPath = sb.getCurrentUser().getHomeDir() + File.separator + zoomImgName;
                    String cropedImgName = zoomImgName.replace("zoom", "crop");
                    String targetPath = sb.getCurrentUser().getHomeDir() + File.separator + cropedImgName;
                    boolean cropOK = DataUtils.cropImage(convertPath, zmImgPath, targetPath, cropright, croptop, canvasDimXo, canvasDimYo, 100);
                    if (cropOK) {
                        String zmImgURL = "zoomImgURL=\'" + prefix + "/resources/users" + File.separator
                                + DataUtils.getJustFileName(sb.getCurrentUser().getHomeDir()) + File.separator + cropedImgName + "\'";
                        res = zmImgURL;
                    }
                }
            }
        } else if (funcName.equalsIgnoreCase("getusrdir")) {
            res = prefix + "/resources/users/" + DataUtils.getJustFileName(sb.getCurrentUser().getHomeDir()) + File.separator;
        } else if (funcName.equalsIgnoreCase("plotPathway")) {
            String pathName = request.getParameter("pathname");
            int canvasDimX = Integer.parseInt(request.getParameter("width"));
            int canvasDimY = Integer.parseInt(request.getParameter("height"));
            String nodeTipInfo = RGraphUtils.plotKEGGPath(sb, pathName, canvasDimX + "", canvasDimY + "", "png", "NULL");

            String pathPrefix = prefix + "/resources/users/" + DataUtils.getJustFileName(sb.getCurrentUser().getHomeDir()) + File.separator;
            String imgURL = pathPrefix + pathName + ".png";

            String urlCode = "pathImgURL=\'" + imgURL + "\';";

            res = urlCode + "\n" + "pathPrefix=\'" + pathPrefix + "\';\n" + nodeTipInfo;

        } else if (funcName.equalsIgnoreCase("getImageMap")) {

            String leftImgURL = prefix + "/resources/users" + File.separator
                    + DataUtils.getJustFileName(sb.getCurrentUser().getHomeDir()) + File.separator + sb.getCurrentImage("path_view") + "dpi72.png";
            String urlCode = "leftImgURL=\"" + leftImgURL + "\"";

            String imgMapInfo = RGraphUtils.getOverviewImgMap(sb.getRConnection());

            res = sb.getAnalType() + "||" + urlCode + "\n" + imgMapInfo;
            //System.out.println("===" + res);
        } else if (funcName.equalsIgnoreCase("prepareMsetView")) {
            String id = request.getParameter("id");
            MsetBean msb = (MsetBean) DataUtils.findBean("msetBean");
            msb.setMsetNm(id);
            res = "1";
        } else if (funcName.equalsIgnoreCase("getmnetinfo")) {
            MnetLoadBean loadnetb = (MnetLoadBean) DataUtils.findBean("mnetLoader");
            MnetResBean mnetb = (MnetResBean) DataUtils.findBean("mnetResBean");
            String org = loadnetb.getNetOrg();
            res = prefix + "/resources/users/" + DataUtils.getJustFileName(sb.getCurrentUser().getHomeDir()) + File.separator + "||" + org;
            res = res + "||" + mnetb.getVisMode() + "||" + RNetworkUtils.getNetsNamesString(sb.getRConnection());

        } else if (funcName.equalsIgnoreCase("getUpsetInfo")) {
            if (sb.getAnalType().equals("metadata")) {// feature-meta
                res = prefix + "/resources/users/" + DataUtils.getJustFileName(sb.getCurrentUser().getHomeDir()) + File.separator + "||" + sb.getCurrentImage("upset_stat");
            } else {//path meta
                MetaPathLoadBean mplb = (MetaPathLoadBean) DataUtils.findBean("pLoadBean");
                res = prefix + "/resources/users/" + DataUtils.getJustFileName(sb.getCurrentUser().getHomeDir()) + File.separator + "||" + sb.getCurrentImage("upset_path") + "||"
                        + mplb.getSelectedDataNms().size() + "||" + String.join(";", mplb.getSelectedDataNms().toArray(new String[0]));
            }
        } else if (funcName.equalsIgnoreCase("getmummi")) {
            MummiAnalBean mbean = (MummiAnalBean) DataUtils.findBean("mummiAnalBean");
            String opts = Arrays.toString(mbean.getAlgOpts());
            res = prefix + "/resources/users/" + DataUtils.getJustFileName(sb.getCurrentUser().getHomeDir()) + File.separator + "||" + opts;
        } else if (funcName.equalsIgnoreCase("getheatmap")) {
            MummiAnalBean mb = (MummiAnalBean) DataUtils.findBean("mummiAnalBean");
            PathBean pb = (PathBean) DataUtils.findBean("pathBean");
            if (sb.getHeatmapType().equals("mummichog")) {
                res = prefix + "/resources/users/" + DataUtils.getJustFileName(sb.getCurrentUser().getHomeDir()) + File.separator
                        + "||" + sb.getOrg() + "||" + mb.getHeatmapName() + "||" + sb.getHeatmapType();
            } else { //pathway
                res = prefix + "/resources/users/" + DataUtils.getJustFileName(sb.getCurrentUser().getHomeDir()) + File.separator
                        + "||" + pb.getLibOpt() + "||" + pb.getHeatmapName() + "||" + sb.getHeatmapType() + "||" + pb.getEnrType();
            }

        } else if (funcName.equalsIgnoreCase("getheatmappath")) {
            PathBean pb = (PathBean) DataUtils.findBean("pathBean");
            res = prefix + "/resources/users/" + DataUtils.getJustFileName(sb.getCurrentUser().getHomeDir()) + File.separator
                    + "||" + sb.getOrg() + "||" + pb.getHeatmapName();

        } else if (funcName.equalsIgnoreCase("heatmapMummichog")) {
            String funDB = request.getParameter("funDB");
            String ids = request.getParameter("IDs");
            String nm = "mummichog_enrichment_" + sb.getFileCount();
            int myres = REnrichUtils.doHeatmapMummichogTest(sb.getRConnection(), nm, funDB, ids);
            //System.out.print(myres);
            if (myres == 1) {
                res = nm + ".json";
            } else {
                res = "ERROR!" + RDataUtils.getErrMsg(sb.getRConnection());
            }

        } else if (funcName.equalsIgnoreCase("heatmapPathway")) {
            String funDB = request.getParameter("funDB");
            String ids = request.getParameter("IDs");
            String nm = "pathway_enrichment_" + sb.getFileCount();
            int myres = REnrichUtils.doHeatmapPathwayTest(sb.getRConnection(), nm, funDB, ids);
            //System.out.print(myres);
            if (myres == 1) {
                res = nm + ".json";
            } else {
                res = "ERROR!" + RDataUtils.getErrMsg(sb.getRConnection());
            }
        } else if (funcName.equalsIgnoreCase("loadingSingleXICPlot")) {
            String featureNumber = request.getParameter("featureNumber");
            String sampleName = request.getParameter("sampleName");
            RConnection RC = sb.getRConnection();
            SpectraProcessBean spb = (SpectraProcessBean) DataUtils.findBean("spectraProcessor");
            String fileName = RSpectraUtils.plotSingleXIC(RC, featureNumber, sampleName, spb.isShowlabel());

            sb.getCurrentUser().setRelativeDir("/resources/users/" + sb.getCurrentUser().getName());
            spb.setSingleXICPlot(sb.getCurrentUser().getRelativeDir() + File.separator + fileName);
            spb.internalizeImage(fileName);
            res = fileName;
        } else if (funcName.equalsIgnoreCase("loadingXICPlot")) {
            String id = request.getParameter("id");
            RConnection RC = sb.getRConnection();
            int idnew;
            SpectraProcessBean spb = (SpectraProcessBean) DataUtils.findBean("spectraProcessor");
            spb.setSingleXICPlot("/resources/images/EICalt.png");
            if (!id.equals("na")) {
                idnew = RSpectraUtils.mzrt2ID2(RC, id);
                spb.setFeatureNum(idnew);
            } else {
                idnew = spb.getFeatureNum();
                //System.out.println("Now idnew equals: " + idnew);                
                spb.setFeatureNum(idnew);
            }

            String fileNm = spb.plotMSfeature("svg");
            res = fileNm;

        } else if (funcName.equalsIgnoreCase("loadingBoxPlot")) {
            String id = request.getParameter("id");
            sb.viewCmpdSummary(id);
            res = "1";

        } else if (funcName.equalsIgnoreCase("loadingTICPlot")) {
            String fileNM = request.getParameter("fileNM");

            /*Only work when Raw Info exists*/
            File mSetObj = new File(sb.getCurrentUser().getHomeDir() + File.separator + "mSet.rda");

            if (mSetObj.exists()) {
                SpectraProcessBean spb = (SpectraProcessBean) DataUtils.findBean("spectraProcessor");
                spb.setTicName(fileNM);
                spb.plotSingleTIC();
                res = spb.getTicImgName();
            } else {
                res = "";
            }

        } else if (funcName.equalsIgnoreCase("updateLoading")) {

            SpectraProcessBean spb = (SpectraProcessBean) DataUtils.findBean("spectraProcessor");
            spb.setFeatureNM(request.getParameter("number"));

            int nb = Integer.parseInt(request.getParameter("number"));
            int res1 = RDataUtils.update3DPCA(sb.getRConnection(), nb);

            if (res1 > 0) {
                spb.internalizeRes(nb);
                res = spb.getLoadingJson();
                spb.setFeatureNM("");
                res = res + "||" + spb.getLoadingJson();
            } else {
                res = "NOT okay";
            }
        } else if (funcName.equalsIgnoreCase("updateSpectraPCA")) {
            SpectraProcessBean spb = (SpectraProcessBean) DataUtils.findBean("spectraProcessor");
            spb.setFeatureNM(request.getParameter("number"));

            int nb = Integer.parseInt(request.getParameter("number"));
            if (nb == 0) {
                spb.setFeatureNM("");
            }
            res = spb.getLoadingJson();

        } else if (funcName.equalsIgnoreCase("iPCALoadingBoxPlot")) {
            String id = request.getParameter("id");
            MultifacBean tb = (MultifacBean) DataUtils.findBean("multifacBean");
            tb.setBoxId(id);
            tb.updateBoxplotMeta();
            res = "1";
        } else if (funcName.equalsIgnoreCase("multiFacBoxPlot")) {
            String id = request.getParameter("id");
            MultifacBean tb = (MultifacBean) DataUtils.findBean("multifacBean");
            tb.setBoxId(id);
            tb.updateMultiFacBoxplotMeta("default");
            res = "1";
        } else if (funcName.equalsIgnoreCase("getCovSvgInfo")) {
            MultifacBean tb = (MultifacBean) DataUtils.findBean("multifacBean");
            res = prefix + "/resources/users/" + DataUtils.getJustFileName(sb.getCurrentUser().getHomeDir()) + File.separator + "||" + tb.getCovJsonName();
        } else if (funcName.equalsIgnoreCase("getSpecSvgInfo")) {
            SpectraProcessBean sp = (SpectraProcessBean) DataUtils.findBean("spectraProcessor");
            res = prefix + "/resources/users/" + DataUtils.getJustFileName(sb.getCurrentUser().getHomeDir()) + File.separator + "||" + sp.getFeatureNum() + ".json";
        } else if (funcName.equalsIgnoreCase("decodeImageData")) {
            String data = request.getParameter("data");
            String fileNm = request.getParameter("name");
            String base64Image = data.split(",")[1];
            byte[] imageBytes = javax.xml.bind.DatatypeConverter.parseBase64Binary(base64Image);
            try {
                BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageBytes));
                String dir = sb.getCurrentUser().getHomeDir();
                File outputfile = new File(dir + "/" + fileNm + ".png");
                ImageIO.write(img, "png", outputfile);
            } catch (IOException ex) {
                // Logger.getLogger(SessionBean1.class.getName()).log(Level.SEVERE, null, ex);
                LOGGER.error("decodeImageData", ex);
            }
        } else if (funcName.equalsIgnoreCase("getnetinfo")) {
            res = RNetworkUtils.getNetsNamesString(sb.getRConnection());
        } else if (funcName.equalsIgnoreCase("performCommunityDetection")) {
            String method = request.getParameter("method");
            res = RNetworkUtils.detectCommunities(sb.getRConnection(), method);
        } else if (funcName.equalsIgnoreCase("getShortestPaths")) {
            String ndA = request.getParameter("source");
            String ndB = request.getParameter("target");
            res = RNetworkUtils.getShortestPaths(sb.getRConnection(), ndA, ndB);
        } else if (funcName.equalsIgnoreCase("updateNetworkLayout")) {
            String algo = request.getParameter("layoutalgo");
            MnetResBean mnetb = (MnetResBean) DataUtils.findBean("mnetResBean");
            res = mnetb.updateNetworkLayout(algo);
        } else if (funcName.equalsIgnoreCase("performNodesFilter")) {
            String nodeIDs = request.getParameter("nodes");
            MnetResBean mnetb = (MnetResBean) DataUtils.findBean("mnetResBean");
            res = mnetb.performNodesFilter(nodeIDs);
        } else if (funcName.equalsIgnoreCase("networkEnrichment")) {
            String ids = request.getParameter("IDs");
            String funDB = request.getParameter("funDB");
            MnetResBean mnetb = (MnetResBean) DataUtils.findBean("mnetResBean");
            String vismode = mnetb.getVisMode();
            String nm = "metaboanalyst_enrichment_" + sb.getFileCount();
            int myres;
            if (funDB.equals("kegg")) {
                //Run enrichment analysis using KEGG genes library
                if (vismode.equals("gene_phenotypes")) {
                    myres = RNetworkUtils.doNetEnrichmentTest(sb.getRConnection(), nm, funDB, ids);
                } else {
                    //Run enrichment analysis using KEGG compounds-genes library
                    myres = mnetb.doIntegPathwayAnanlysis(ids, nm);
                }
            } else {//GO, motif. reactiome
                myres = RNetworkUtils.doNetEnrichmentTest(sb.getRConnection(), nm, funDB, ids);
            }

            if (myres == 1) {
                res = nm + ".json";
            } else {
                res = "ERROR!" + RDataUtils.getErrMsg(sb.getRConnection());
            }

        } else if (funcName.equalsIgnoreCase("exportNetwork")) {
            String format = request.getParameter("format");
            MnetResBean mnetb = (MnetResBean) DataUtils.findBean("mnetResBean");
            res = mnetb.exportNetwork(format);

        } else if (funcName.equalsIgnoreCase("extractModule")) {
            String nodeIDs = request.getParameter("nodeIDs");
            MnetResBean mnetb = (MnetResBean) DataUtils.findBean("mnetResBean");
            res = mnetb.extractModule(nodeIDs);

        } else if (funcName.equalsIgnoreCase("viewNetwork")) {
            String name = request.getParameter("name");
            res = RDataUtils.genPathwayJSON(sb.getRConnection(), name);

        } else if (funcName.equalsIgnoreCase("download")) {
            DataUtils.setupFileDownloadZip(sb.getCurrentUser());
            res = prefix + "/resources/users/" + DataUtils.getJustFileName(sb.getCurrentUser().getHomeDir()) + File.separator + "/Download.zip";

        } else if (funcName.equalsIgnoreCase("getCurrentPageUrl")) {
            res = request.getRequestURL().toString();
        } else if (funcName.equalsIgnoreCase("getErrorMsg")) {
            res = RDataUtils.getErrMsg(sb.getRConnection());
        } else if (funcName.equalsIgnoreCase("prepareNetwork")) {
            String netName = request.getParameter("netName");
            MnetResBean mnetb = (MnetResBean) DataUtils.findBean("mnetResBean");
            res = mnetb.loadNetwork(netName);
        } else if (funcName.equalsIgnoreCase("getcmpdinfo")) {
            String id = request.getParameter("cid");
            res = RDataUtils.getCmpdInfo(sb.getRConnection(), id);
        } else if (funcName.equalsIgnoreCase("savePartialRaw")) {

            String naviString = request.getParameter("naviString");
            String uid = request.getParameter("uid");

            SpectraControlBean spcb = (SpectraControlBean) DataUtils.findBean("spectraController");
            spcb.setCreatedShareLink(true);
            sb.setPartialId(uid);
            sb.setNaviString(naviString);

            if (ab.isInDocker()) {
                spcb.updateJobPartialLink_docker();
            } else {
                spcb.updateJobPartialLink();
            }

            res = ab.getAppUrlPath() + "||" + spcb.getCurrentJobId();

        } else if (funcName.equalsIgnoreCase("doSpecLogin")) {
            if (!sb.isLoggedIn() || !sb.getAnalType().equals("raw")) {
                sb.doLogin("spec", "raw", false, false);
            }
            res = "true";
        } else if (funcName.equalsIgnoreCase("getMetaIntegrity")) {
            SpectraUploadBean su = (SpectraUploadBean) DataUtils.findBean("specLoader");
            SpectraControlBean pcb = (SpectraControlBean) DataUtils.findBean("spectraController");

            String filenms = request.getParameter("fileNms");
            boolean metaFormatOk = pcb.isMetaOk();
            if (metaFormatOk) {
                boolean isMetaOk = su.checkMetaIntegrity(filenms);
                if (!isMetaOk) {
                    //su.updateText();
                }
                res = isMetaOk + "";
            } else {
                res = "false";
            }

        } else if (funcName.equalsIgnoreCase("checkUploadPermission")) {

            ResourceSemaphore resourceSemaphore = (ResourceSemaphore) DataUtils.findBean("semaphore");
            Semaphore semaphore = resourceSemaphore.getUploadSemaphore();
            boolean canUpload = semaphore.availablePermits() > 0;
            res = canUpload + "";

        } else if (funcName.equalsIgnoreCase("checkSessionUploaded")) {
            SpectraControlBean pcb = (SpectraControlBean) DataUtils.findBean("spectraController");
            boolean resB = pcb.checkJobRunning();
            res = resB + "";

        } else if (funcName.equalsIgnoreCase("getPartialLinkInfo")) {

            SpectraControlBean pcb = (SpectraControlBean) DataUtils.findBean("spectraController");
            String url = ab.getAppUrlPath();
            res = url + "/MetaboAnalyst/faces/Share?ID=" + sb.getPartialId() + "||" + pcb.getCurrentJobId();

        } else if (funcName.equalsIgnoreCase("loadpartialLink")) {
            ProjectBean pjb = (ProjectBean) DataUtils.findBean("projectBean");
            try {
                res = pjb.loadPartial();
            } catch (SQLException ex) {
                res = null;
            }

            if (res == null) {
                res = "null";
            } else {
                res = "/MetaboAnalyst/Secure/spectra/JobStatusView.xhtml";
            }

        } else if (funcName.equalsIgnoreCase("generateColorArray")) {
            int n = Integer.parseInt(request.getParameter("number"));

            int jsonCount = sb.getFileCount();
            String filenm = "color_array_" + jsonCount + ".json";
            res = RDataUtils.generateColorArray(sb.getRConnection(), n, "green", filenm);

        } else if (funcName.equalsIgnoreCase("computeEncasing")) {
            String type = (String) request.getParameter("type");
            String names = (String) request.getParameter("names");
            String level = (String) request.getParameter("level");
            String omicstype = (String) request.getParameter("omics");

            int jsonCount = sb.getFileCount();
            String filenm = "encasing_mesh_" + jsonCount + ".json";
            res = RDataUtils.computeEncasing(sb.getRConnection(), filenm, type, names, level, omicstype);

            if (sb.getAnalType().equals("raw") || sb.getDataType().equals("spec")) {
                SpectraProcessBean spb = (SpectraProcessBean) DataUtils.findBean("spectraProcessor");
                spb.internalizecomputeEncasingRes();
            }

        } else if (funcName.equalsIgnoreCase("generatecoviddata")) {
            String platforms = (String) request.getParameter("platforms");
            String bloods = (String) request.getParameter("bloods");
            String polarities = (String) request.getParameter("polarities");
            String countries = (String) request.getParameter("countries");
            String populations = (String) request.getParameter("populations");
            String comparis = (String) request.getParameter("comparis");
            String url = ab.getAppUrlPath();

            String res0 = RDataUtils.generatecoviddata(sb.getRConnection(), platforms, bloods, polarities, countries, populations, comparis);

            res = url + "/MetaboAnalyst/resources/users/" + sb.getCurrentUser().getName() + "/" + res0;

        } else if (funcName.equalsIgnoreCase("gofromCOVID")) { //gofromCOVID
            covid cv = (covid) DataUtils.findBean("covids");
            String module = (String) request.getParameter("module");
            String datasets = (String) request.getParameter("datasets");
            String comparis = (String) request.getParameter("comparis");
            res = cv.gofromCOVID(module, datasets, comparis);
            if (res == null) {
                res = "null";
            }
        } else if (funcName.equalsIgnoreCase("dologout")) {
            System.out.println(" Reaching here dologout !!!!");
            sb.doLogout(0);
            res = "done";
        } else {
            //Nothing
        }

        //finally send back response
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        response.setDateHeader("Expires", 0);
        //Note: set plain text, not xml, since our response is basically free text & javascript code
        response.setContentType("text/plain");

        try {
            response.getWriter().write(res);
            context.responseComplete();
        } catch (Exception e) {
            LOGGER.error("handleAjaxRequest", e);
        }
    }

    private void handlePartialRequest(PhaseEvent event) {

        FacesContext context = event.getFacesContext();

        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();

        FacesContext fc = FacesContext.getCurrentInstance();
        ELContext elc = fc.getELContext();
        ExpressionFactory ef = fc.getApplication().getExpressionFactory();

        ValueExpression ve = ef.createValueExpression(elc, "#{sessionBean1}", SessionBean1.class);
        SessionBean1 sb = (SessionBean1) ve.getValue(elc);

        ve = ef.createValueExpression(elc, "#{applicationBean1}", ApplicationBean1.class);
        ApplicationBean1 ab = (ApplicationBean1) ve.getValue(elc);

        ve = ef.createValueExpression(elc, "#{projectBean}", ProjectBean.class);
        ProjectBean pb = (ProjectBean) ve.getValue(elc);

        ve = ef.createValueExpression(elc, "#{spectraController}", SpectraControlBean.class);
        SpectraControlBean spcb = (SpectraControlBean) ve.getValue(elc);

        try {
            String partialId = request.getParameter("ID");
            sb.setPartialId(partialId);

            boolean res = false;
            if (ab.isInDocker()) {
                res = pb.checkLinkinDocker();
            } else {
                res = pb.checkLink();
            }

            sb.setPartialLinkValide(res);
            if (res) {
                //Temporary, NEED TO FIX TRACETRACK SAVING/LOADING LATER
                sb.initNaviTree("spec");
                sb.registerPage("Upload");
                sb.registerPage("Spectra check");
                sb.registerPage("Spectra processing");
                sb.registerPage("Job status");

                spcb.setCreatedShareLink(true);
                sb.setPartialId(partialId);
            }

            context.getApplication().getNavigationHandler().handleNavigation(context,
                    "*", "sharelink");

        } catch (Exception e) {
            LOGGER.error("handlePartialRequest", e);
            context.getApplication().getNavigationHandler().handleNavigation(context,
                    "*", "logout");

        }
    }

    private void handleResetRequest(PhaseEvent event) {

        FacesContext context = event.getFacesContext();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        String token = request.getParameter("token");

        SessionBean1 sb = context.getApplication().evaluateExpressionGet(context, "#{sessionBean1}", SessionBean1.class);
        if (token != null) {
            sb.setResetToken(token);
        }

    }

    private void handleRedirRequest(PhaseEvent event) {
        FacesContext context = event.getFacesContext();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        String redirtoken = request.getParameter("redirToken");
        //System.out.println("Now the redir token is --> " + redirtoken);
        SessionBean1 sb = context.getApplication().evaluateExpressionGet(context, "#{sessionBean1}", SessionBean1.class);
        UserLoginBean ulb = context.getApplication().evaluateExpressionGet(context, "#{userLoginBean}", UserLoginBean.class);
        if (redirtoken != null) {
            boolean resbool = ulb.doKeepLoggedin(redirtoken);
            //System.out.println("Now the resbool is --> " + resbool);
            if (resbool) {
                sb.setRegisteredLogin(true);
                ulb.setLoggedin(true);
            }
        }
    }

    private void handleLoadingProjectRequest(PhaseEvent event) {
        PrimeFaces.current().executeScript("PF('statusDialog').show();");
        FacesContext context = event.getFacesContext();

        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();

        FacesContext fc = FacesContext.getCurrentInstance();
        ELContext elc = fc.getELContext();
        ExpressionFactory ef = fc.getApplication().getExpressionFactory();

        ValueExpression ve = ef.createValueExpression(elc, "#{sessionBean1}", SessionBean1.class);
        SessionBean1 sb = (SessionBean1) ve.getValue(elc);

        ve = ef.createValueExpression(elc, "#{applicationBean1}", ApplicationBean1.class);
        ApplicationBean1 ab = (ApplicationBean1) ve.getValue(elc);

        ve = ef.createValueExpression(elc, "#{projectBean}", ProjectBean.class);
        ProjectBean pb = (ProjectBean) ve.getValue(elc);

        ve = ef.createValueExpression(elc, "#{userLoginBean}", UserLoginBean.class);
        UserLoginBean ulb = (UserLoginBean) ve.getValue(elc);

        try {
            String projectLoadingCode = request.getParameter("ID");
            String userId = projectLoadingCode.split("_")[0];
            long UserNM = Long.parseLong(userId);

            String guestFolder = projectLoadingCode.split("_")[1];
            String pageFlag = "";
            boolean ready = false;
            boolean ready0 = false;

            if (guestFolder.startsWith("nv")) {

                //Several steps need to be done for this case:
                // 1. keep user signned in
                ulb.setUserNM(UserNM);
                ulb.doLoginKeep(guestFolder);

                // 2. Restore selected projects
                String initRes = pb.initializeProject(guestFolder);
                if (initRes == null) {
                    ready0 = false;
                } else {
                    ready0 = true;
                }

                // 3. load new project
                String loadRes = pb.loadnewProject();
                if (loadRes == null) {
                    ready = false;
                } else {
                    ready = true;
                    pageFlag = "spec";
                }

            } else {

                // 1. keep user signned in
                ulb.setUserNM(UserNM);
                ulb.doLoginKeep(guestFolder);

                // 2. Restore selected projects
                String initRes = pb.initializeProject(guestFolder);
                if (initRes == null) {
                    ready0 = false;
                } else {
                    ready0 = true;
                }

                // 3. load project
                String loadRes = pb.loadProject();
                if (loadRes == null) {
                    ready = false;
                } else {
                    ready = true;
                    pageFlag = loadRes;
                }
            }

            // 4. If everything ready (both readys equals true), jump into the corresponsing page
            PrimeFaces.current().executeScript("PF('statusDialog').hide();");
            if (ready0 && ready) {
                context.getApplication().getNavigationHandler().handleNavigation(context,
                        "*", pageFlag);
            }

        } catch (Exception e) {
            LOGGER.error("handleLoadingProjectRequest", e);
            context.getApplication().getNavigationHandler().handleNavigation(context,
                    "*", "logout");

        }
    }

    private void handleLoadingGenapProjectRequest(PhaseEvent event) {

        PrimeFaces.current().executeScript("PF('statusDialog').show();");
        FacesContext context = event.getFacesContext();

        // Prepare java beans        
        FacesContext fc = FacesContext.getCurrentInstance();
        ELContext elc = fc.getELContext();
        ExpressionFactory ef = fc.getApplication().getExpressionFactory();

        ValueExpression ve = ef.createValueExpression(elc, "#{sessionBean1}", SessionBean1.class);
        SessionBean1 sb = (SessionBean1) ve.getValue(elc);

        ve = ef.createValueExpression(elc, "#{applicationBean1}", ApplicationBean1.class);
        ApplicationBean1 ab = (ApplicationBean1) ve.getValue(elc);

        ve = ef.createValueExpression(elc, "#{projectBean}", ProjectBean.class);
        ProjectBean pb = (ProjectBean) ve.getValue(elc);

        ve = ef.createValueExpression(elc, "#{userLoginBean}", UserLoginBean.class);
        UserLoginBean ulb = (UserLoginBean) ve.getValue(elc);

        //get the work space        
        String domain = ab.getDomainURL();
        //domain = "meta-655-davidm.genap.ca";
        String workSpace = null;
        if (domain.endsWith("genap.ca")) {
            domain = domain.replace("genap.ca", "");
            String[] Info = domain.split("-");
            String WorkID = Info[1]; // For future use
            workSpace = Info[2]; // To retrieve the projects            
        }

        //get or create userNM based on workspace
        ulb.setWorkspace(workSpace);
        int userNM = 0;
        try {
            userNM = ulb.checkWorkSpaceExist();
        } catch (SQLException ex) {
            //do nothing!
        }

        if (userNM == 0) {
            ulb.insertWorkSpace();
        } else {
            try {
                userNM = ulb.checkWorkSpaceExist();
            } catch (SQLException ex) {
                //do nothing!
            }
        }

        //set as signed-in status
        if (userNM != 0) {
            ulb.SigninStatus(userNM);
            ulb.setUserNM(userNM);
        }

        //retrieve the projects info if any
        boolean ready = false;

        //return to projects page
        PrimeFaces.current().executeScript("PF('statusDialog').hide();");
        if ((userNM != 0) && ready) {
            context.getApplication().getNavigationHandler().handleNavigation(context,
                    "*", "projects");
        } else {
            context.getApplication().getNavigationHandler().handleNavigation(context,
                    "*", "logout");
        }

    }


}
