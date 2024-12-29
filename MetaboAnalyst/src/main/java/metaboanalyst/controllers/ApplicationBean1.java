/*
 * ApplicationBean1.java
 *
 * Created on Oct 21, 2008, 9:37:17 AM
 */
package metaboanalyst.controllers;

import com.sun.management.OperatingSystemMXBean;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import metaboanalyst.rwrappers.RCenter;

@ApplicationScoped
@Named("applicationBean1")
public class ApplicationBean1 implements Serializable {

    private String app_url = "https://www.metaboanalyst.ca";
    // private final String apiResourcePath = "https://www.xialab.ca/api/download/metaboanalyst/";
    private final String apiResourcePath = "https://api.omicsquare.com/OmicSquareAPI/rest/download/metaboanalyst/";

    public String getApiResourcePath() {
        return apiResourcePath;
    }

    private boolean onPublicServer = true;
    private boolean onMainServer = false;
    private boolean onDevServer = false;
    private boolean onNewServer = false;
    private boolean onGenApServer = false;
    private boolean onLocalServer = false;
    private boolean onOmicSquareServer = false;
    private boolean onQiangPc = false;
    private boolean onZgyPc = false;
    private boolean inDocker = false;
    private boolean dockerAuthed = false;
    private String domainUrl;
    private String realPath;
    private String raw_spec_folder = "";

    // 50M
    private final int MAX_UPLOAD_SIZE = 50000000;
    private final int MAX_SPEC_SIZE = 214958080;
    private final int MAX_SPEC_NUM = 200;

    private boolean compiled = false;

    /*
    All relative paths below are below /resources
     */
    private static final String usr_home = "/users/";
    private String projectsHome = "/data/glassfish/projects/metaboanalyst/";

    public ApplicationBean1() {
        initDirectories();
    }



    private void initDirectories() {
        String domain_url = ((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRe‌​quest()).getRequestURL().toString();

        ServletContext context = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
        realPath = context.getRealPath("/resources");

        if (domain_url.contains("www.metaboanalyst.ca")) {
            app_url = "https://www.metaboanalyst.ca";
            onMainServer = true;
            raw_spec_folder = "/data/glassfish/projects/metaboanalyst/";
        } else if (domain_url.contains("new.metaboanalyst.ca")) {
            app_url = "https://new.metaboanalyst.ca";
            onMainServer = true;
            onNewServer = true;
            onPublicServer = true;
            raw_spec_folder = "/data/glassfish/projects/metaboanalyst/";
        } else if (domain_url.contains("dev.metaboanalyst.ca")) {
            app_url = "https://dev.metaboanalyst.ca";
            onDevServer = true;
            raw_spec_folder = "/data/glassfish/projects/metaboanalyst/";
        } else if (domain_url.contains("omicsquare.com")) {
            app_url = "https://www.omicsquare.com/";
            onOmicSquareServer = true;
            raw_spec_folder = "/data/glassfish/projects/metaboanalyst/";
        } else if (Files.isDirectory(Paths.get("/tank/omics/payara5/glassfish/domains/domain1/logs"))) {
            app_url = "https://genap.metaboanalyst.ca";
            onGenApServer = true;
        } else if (Files.isRegularFile(Paths.get("/docker_marker"))) {
            //System.out.println("domain_url=======> " + domain_url);
            app_url = domain_url.replace("/MetaboAnalyst/", "");
            inDocker = true;
            onPublicServer = false;
        } else if (domain_url.contains("localhost:8080")) {
            app_url = "http://localhost:8080";
            onPublicServer = false;
            onLocalServer = true;
            if (Files.isDirectory(Paths.get("/home/qiang/Documents/Regular_commands"))) {
                onQiangPc = true;
                projectsHome = "/home/qiang/Downloads/results/"; //qiang's lab workstation
            } else if (Files.isDirectory(Paths.get("/Users/xia/Dropbox/projects/metaboanalyst/"))) {
                projectsHome = "/Users/xia/Dropbox/projects/metaboanalyst/"; //xia local
            } else if (Files.isDirectory(Paths.get("/home/zgy/"))) {
                onZgyPc = true;
            } else {
                projectsHome = "/resources/projects/metaboanalyst/"; //tmp folder for testing
            }
            if (realPath.contains("C:")) {
                realPath = realPath.replace("\\", "/");
            }
        }
        if (onQiangPc || onGenApServer || inDocker || onZgyPc) {
            raw_spec_folder = realPath + usr_home;
        }

        domainUrl = app_url + "/MetaboAnalyst";
    }

    //The compiling will be created upon application start up
    //@PostConstruct
    //The compiling will be created upon first user query
    public boolean isCompiled() {
        return compiled;
    }

    //need internal check for intialization due to concurrent access
    public synchronized void compileRScripts(String analType) {
        if (!compiled) {
            if (analType.equals("raw") && shouldUseScheduler()) {
                compiled = RCenter.compileRScripts(raw_spec_folder, realPath + "/rscripts/_script_loader.R");
            } else if (analType.equals("config")) {
                // we don't mark it as compiled so the next "correct" module can compile them
                RCenter.compileRScripts(realPath + usr_home, realPath + "/rscripts/_script_loader.R");
            } else {
                compiled = RCenter.compileRScripts(realPath + usr_home, realPath + "/rscripts/_script_loader.R");
            }
        }
    }

    public String getRaw_spec_folder() {
        return raw_spec_folder;
    }

    public int getMAX_UPLOAD_SIZE() {
        if (inDocker) {
            return 20 * MAX_UPLOAD_SIZE;
        }
        return MAX_UPLOAD_SIZE;
    }

    public int getMAX_SPEC_SIZE() {
        if (inDocker) {
            return 2145958080;
        }
        return MAX_SPEC_SIZE;
    }

    public int getMAX_SPEC_NUM() {
        if (inDocker) {
            return 1000000;
        }
        return MAX_SPEC_NUM;
    }

    //vmOpt, jobs burdn sharing by dispactching among www, new and genap
    // 0, use www only => compute canada (new and genap) down
    // 1, use www and new only => maintain genap node
    // 2, use www and genap only => maintain new node)
    // 3, use www, new and genap concurrently (basic mode)
    private int vmOpt = 0;
    private int myVMOpt = 0;
    
    public int getVmOpt() {
        return vmOpt;
    }

    public void setVmOpt(int vmOpt) {
        this.vmOpt = vmOpt;
    }
    
    
    //dispactch between cloud and dev
    //odd to cloud, even to dev
    private int jobCount = 0;
    private String devOpt = "cloud";
    private String myDevOpt = "cloud";

    public String getDevOpt() {
        return devOpt;
    }

    public void setDevOpt(String devOpt) {
        this.devOpt = devOpt;
    }

    //actual value returned to set prefix
    private String omicsBotUrl = "dev";

    public String getOmicsBotUrl() {
        return omicsBotUrl;
    }

    public void setOmicsBotUrl(String omicsBotUrl) {
        this.omicsBotUrl = omicsBotUrl;
    }

    private String myID;

    public String getMyID() {
        return myID;
    }

    public void setMyID(String myID) {
        this.myID = myID;
    }

    public void updateRedirection() {
        if (myID.equals("xxxxxx")) {
            myDevOpt = devOpt;
            myVMOpt = vmOpt;
        }
    }

    public String getAppUrlPath() {
        return app_url;
    }

    public String getRootContext() {
        return "/MetaboAnalyst";
    }

    public String getDomainURL() {
        return domainUrl + "/";
    }

    public String getDynamicsDomainURL() {
        OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(
                OperatingSystemMXBean.class);
        
        // the design to redirect to different instances
        // if running in other server : dev or genap or local. Keep it!
        // if running in main which is not busy. keep it!
        // if running in main, but this server is too busy (over 2/3 used, or Rserve number over 200). re-distribute jobs to other instances (dev/genap).
        jobCount++;
        if (!onMainServer || onNewServer) {
            return domainUrl + "/";
        }
        
        // For www only mode
        if(myVMOpt == 0){
            return domainUrl + "/";
        }
        // For non-www mode
        if(myVMOpt == 4){
            if (jobCount % 2 == 0) {
                return "https://new.metaboanalyst.ca/";
            } else {
                return "https://genap.metaboanalyst.ca/";
            }
        }
        
        // For sharing modes
        int rserve_num = 0;
        try {
            String line;
            Process p = Runtime.getRuntime().exec(new String[]{"/usr/bin/bash", "-c", "ps axu | grep Rserve | wc -l"});
            BufferedReader input = new BufferedReader(
                    new InputStreamReader(p.getInputStream()));
            while ((line = input.readLine()) != null) {
                rserve_num = Integer.parseInt(line);
            }
            input.close();
        } catch (IOException | NumberFormatException ex) {

        }

        if (osBean.getSystemCpuLoad() > 0.5 || osBean.getSystemLoadAverage() > 50.0 || rserve_num > 130) {
            // if the server is too busy, redistribute all
            if(myVMOpt == 1){
                return "https://new.metaboanalyst.ca/";
            }
            if(myVMOpt == 2){
                return "https://genap.metaboanalyst.ca/";
            }
            // for myVMOpt == 3
            if (jobCount % 2 == 0) {
                return "https://genap.metaboanalyst.ca/";
            } else {
                return "https://new.metaboanalyst.ca/";
            }
        }

        return domainUrl + "/";
    }

    public String getModuleURL() {
        jobCount++;
        return (domainUrl);
    }

    public boolean isOnPublicServer() {
        return onPublicServer;
    }

    public boolean isOnMainServer() {
        return onMainServer;
    }

    public boolean isOnDevServer() {
        return onDevServer;
    }

    public boolean isOnOmicSquareServer() {
        return onOmicSquareServer;
    }

    public boolean isOnZgyPc() {
        return onZgyPc;
    }

    public boolean isOnQiangPc() {
        return onQiangPc;
    }

    public boolean shouldUseScheduler() {
        return onMainServer || onDevServer || onZgyPc || onQiangPc || inDocker;
    }

    public boolean isOnLocalServer() {
        return onLocalServer;
    }

    public boolean isOnGenap() {
        return onGenApServer;
    }

    public boolean isOnNewServer() {
        return onNewServer;
    }

    public boolean isInDocker() {
        return inDocker;
    }

    public boolean isDockerAuthed() {
        return dockerAuthed;
    }

    public void setDockerAuthed(boolean dockerAuthed) {
        this.dockerAuthed = dockerAuthed;
    }

    public String getRealUserHomePath() {
        //return realUserHomePath;
        return realPath + usr_home;
    }

    public String getRscriptsPath() {
        return realPath + "/rscripts";
    }

    public String getAutoCache() {
        if (onMainServer || onDevServer || onQiangPc || inDocker || onZgyPc) {
            return "/home/glassfish/projects/spectra_example_cache/auto";
        } else {
            System.out.println("WARNING: If there is no cache file found in your local, please COPY it from dev server and add your path HERE!!!!");
        }
        return "/home/glassfish/projects/spectra_example_cache/auto";
    }

    public String getCustomizedCache() {
        if (onMainServer || onDevServer || onQiangPc || inDocker || onZgyPc) {
            return "/home/glassfish/projects/spectra_example_cache/customized";
        } else {
            System.out.println("WARNING: If there is no cache file found in your local, please COPY it from dev server and add your path HERE!!!!");
        }
        return "/home/glassfish/projects/spectra_example_cache/customized";
    }

    public String getProjectsHome() {
        return projectsHome;
    }

    private final String OptiLCMSversion = "1.1.0";

    public String getOptiLCMSversion() {
        return OptiLCMSversion;
    }

    public String getRscriptLoaderPath() {
        return realPath + "/rscripts/_script_loader.R";
    }

    public String getBgImgPath() {
        return realPath + "/images/background.png";
    }

    public String getParams0() {
        return realPath + "/data/params0.rda";
    }


    /*
    * Handling resources
     */
    public String getResourceByAPI(String fileName) {
        return apiResourcePath + fileName;
    }

    public String getInternalData(String fileName) {
        return realPath + "/data/" + fileName;
    }

    public String getRealPath() {
        return realPath;
    }


    public String getApp_url() {
        return app_url;
    }

    public String getTemplateUrl() {

            return "/frags";

    }
}
