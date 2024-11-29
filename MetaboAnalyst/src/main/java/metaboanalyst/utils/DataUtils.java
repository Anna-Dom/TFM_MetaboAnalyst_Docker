/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package metaboanalyst.utils;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Set;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;
import metaboanalyst.controllers.ApplicationBean1;
import metaboanalyst.models.NameMapBean;
import metaboanalyst.models.User;
import metaboanalyst.project.ProjectBean;
import org.apache.commons.io.FileUtils;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.file.UploadedFile;
import org.rosuda.REngine.Rserve.RConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
/**
 *
 * @author Jeff
 */
public class DataUtils {

    private static final Logger LOGGER = LogManager.getLogger(DataUtils.class);
    
    @SuppressWarnings("unchecked")
    public static <T> T findBean(String beanName) {
        FacesContext context = FacesContext.getCurrentInstance();
        return (T) context.getApplication().evaluateExpressionGet(context, "#{" + beanName + "}", Object.class);
    }

    public static String getDomainURL(String myurl) {
        try {
            URL url = new URL(myurl);
            return url.getProtocol() + "://" + url.getAuthority();
        } catch (Exception e) {
            LOGGER.error("getDomainURL", e);
            return null;
        }
    }
    
    // create a tempature user accoutn if user log in as a guest will not remember, only session only
    public static User createTempUser(String realUsrPath) {
        try {
            //String realUsrPath = realPath + usr_home;
            //try to clean the user folder to remove old files (more than 1 day)
            DataUtils.deleteFilesOlderThanNdays(realUsrPath);
            //first create a random user names
            User user = new User();
            String guestName = File.createTempFile("guest", "tmp").getName();
            //String guestDir = realUsrPath + File.separator + guestName;
            String guestDir = realUsrPath + guestName;
            File guestFolder = new File(guestDir);
            while (guestFolder.exists()) {
                guestName = File.createTempFile("guest", "tmp").getName();
                //guestDir = realUsrPath + File.separator + guestName;
                guestFolder = new File(realUsrPath + guestName);
            }
            guestFolder.mkdir();
            user.setName(guestName);
            user.setRelativeDir("/resources/users/" + guestName);
            user.setHomeDir(guestDir);
            return user;
        } catch (Exception e) {
             LOGGER.error("createTempUser", e);
        }
        return null;
    }

    // create a tempature user accoutn if user log in as a guest will not remember, only session only
    public static User createRawSpecUser(String realUsrPath) {

        ProjectBean pb = (ProjectBean) DataUtils.findBean("projectBean");

        try {
            DataUtils.deleteFilesOlderThanNdays(realUsrPath, 14);
            //first create a random user names
            User user = new User();
            String guestName;

            //check if it is project saving
            if (pb.getSelectedProject() != null) {
                guestName = File.createTempFile("guest", "project").getName();
            } else {
                guestName = File.createTempFile("guest", "tmp").getName();
            }

            String guestDir = realUsrPath + guestName;
            File guestFolder = new File(guestDir);

            while (guestFolder.exists()) {
                if (pb.getSelectedProject() != null) {
                    guestName = File.createTempFile("guest", "project").getName();
                } else {
                    guestName = File.createTempFile("guest", "tmp").getName();
                }
                guestFolder = new File(realUsrPath + guestName);
            }

            boolean created = guestFolder.mkdir();

            user.setName(guestName);
            user.setRelativeDir(realUsrPath + guestName);
            user.setHomeDir(guestDir);
            return user;
        } catch (Exception e) {
            LOGGER.error("createRawSpecUser", e);
        }
        return null;
    }

    public static void updateMsg(String type, String content) {
        if (type.equalsIgnoreCase("error")) {            
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", content));
        } else if (type.equalsIgnoreCase("warning")) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning", content));
        } else {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "OK", content));
        }
    }

    public static NameMapBean[] removeHyperlinks(NameMapBean[] nmaps) {
        for (NameMapBean nameMap : nmaps) {
            if (nameMap.getQuery().contains("<strong")) {
                nameMap.setQuery(nameMap.getQuery().replace("<strong style=\"background-color:yellow; font-size=125%; color=\"black\">", ""));
                nameMap.setQuery(nameMap.getQuery().replace("<strong style=\"background-color:red; font-size=125%; color=\"black\">", ""));
                nameMap.setQuery(nameMap.getQuery().replace("</strong>", ""));
            }
            if (nameMap.getHmdb_id().contains(">")) {
                nameMap.setHmdb_id(nameMap.getHmdb_id().split(">")[1].split("</a")[0]);
            }
            if (nameMap.getChebi_id().contains(">")) {
                nameMap.setChebi_id(nameMap.getChebi_id().split(">")[1].split("</a")[0]);
            }
            if (nameMap.getKegg_id().contains(">")) {
                nameMap.setKegg_id(nameMap.getKegg_id().split(">")[1].split("</a")[0]);
            }
            if (nameMap.getPubchem_id().contains(">")) {
                nameMap.setPubchem_id(nameMap.getPubchem_id().split(">")[1].split("</a")[0]);
            }
            if (nameMap.getMetlin_id().contains(">")) {
                nameMap.setMetlin_id(nameMap.getMetlin_id().split(">")[1].split("</a")[0]);
            }

        }
        return nmaps;
    }

    public static DefaultStreamedContent getDownloadFile(String filePath) {
        try {
            File file = new File(filePath);
            InputStream input = new FileInputStream(file);
            ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
            return DefaultStreamedContent.builder().contentType(externalContext.getMimeType(file.getName())).name(file.getName()).stream(() -> input).build();
            // return (new DefaultStreamedContent(input, externalContext.getMimeType(file.getName()), file.getName()));
        } catch (Exception e) {
           LOGGER.error("getDownloadFile", e);
        }
        return null;
    }

    public static StreamedContent getStreamedImage(String path, String fileName) throws IOException {
        if (FacesContext.getCurrentInstance().getCurrentPhaseId() == PhaseId.RENDER_RESPONSE) {
            // So, we're rendering the view. Return a stub StreamedContent so that it will generate right URL.
            return new DefaultStreamedContent();
        } else {
            // So, browser is requesting the image. Return a real StreamedContent with the image bytes. 
            // return new DefaultStreamedContent(new FileInputStream(new File(path, fileName)), "image/png");
            File file = new File(path, fileName);
            InputStream input = new FileInputStream(file);
            return DefaultStreamedContent.builder().contentType(FacesContext.getCurrentInstance().getExternalContext().getMimeType(file.getName())).name(file.getName()).stream(() -> input).build();
        }
    }

    public static void setupFileDownloadZip(User currentUser) {

        File folder = new File(currentUser.getHomeDir());

        //remove previous (if any) zip file
        DataUtils.deleteFile(currentUser, "Download.zip");

        File[] listOfFiles = folder.listFiles((File dir, String name) -> name.endsWith(".csv"));
        DataUtils.createZipFile(listOfFiles,"Download.zip", currentUser.getHomeDir());
    }

    public static String readTextFile(String filePath) {

        BufferedReader br = null;
        String text = "";
        String line;
        try {
            br = new BufferedReader(new FileReader(filePath));
            while ((line = br.readLine()) != null) {
                // use comma as separator
                //String[] country = line.split(splitBy);
                line = line.replace("\t", "  ");
                text = text + "\n" + line;
            }

        } catch (Exception e) {
             LOGGER.error("readTextFile", e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                        LOGGER.error("readTextFile", e);
                }
            }
        }
        return text;
    }

    //Note: for Class uploadedFile
    public static String getJustFileName(String uploadedFileName) {
        int index = uploadedFileName.lastIndexOf('/');
        String justFileName;
        if (index >= 0) {
            justFileName = uploadedFileName.substring(index + 1);
        } else {
            // Try backslash
            index = uploadedFileName.lastIndexOf('\\');
            if (index >= 0) {
                justFileName = uploadedFileName.substring(index + 1);
            } else { // No forward or back slashes
                justFileName = uploadedFileName;
            }
        }
        return justFileName;
    }

    public static void copyFile(File in, File out) {
        try {
            FileInputStream fis = new FileInputStream(in);
            FileOutputStream fos = new FileOutputStream(out);
            copyInputStream(fis, fos);
        } catch (IOException e) {
                LOGGER.error("copyFile", e);
        }
    }
    
    public static void fetchFile(String in, File out){
        try {
            InputStream fis = new URL(in).openStream();
            FileOutputStream fos = new FileOutputStream(out);
            copyInputStream(fis, fos);
        } catch (IOException e) {
            LOGGER.error("fetchFile", e);
        }
    }

    public static void copyDir(String source, String destination) {

        // String source = "C:/your/source";
        File srcDir = new File(source);

        // String destination = "C:/your/destination";
        File destDir = new File(destination);

        try {
            FileUtils.copyDirectory(srcDir, destDir, false);
        } catch (IOException e) {
           LOGGER.error("copyDir", e);
        }

    }

    private static void copyInputStream(InputStream in, OutputStream out) throws IOException {
        try (in) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) >= 0) {
                out.write(buffer, 0, len);
            }
        }
        out.close();
    }

    private static final int BUFFER_SIZE = 4096;

    public static void createZipFile(File[] files, String zipName, String path) {
        // Create a buffer for reading the files
        byte[] buf = new byte[18024];

        try {
            // Create the ZIP file
            String outFilename = path + File.separator + zipName;
            // Compress the files
            try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outFilename))) {
                // Compress the files
                for (File file : files) {
                    FileInputStream in = new FileInputStream(file);
                    // Add ZIP entry to output stream.
                    out.putNextEntry(new ZipEntry(file.getName()));
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    out.closeEntry();
                    in.close();
                }
            }
        } catch (IOException e) {
            LOGGER.error("createZipFile", e);
        }
    }

    public static String setupTable(String lbl, double[][] sigmat, String[] rownames, String[] colnames) {

        if (rownames == null || rownames.length == 0) {
            return ("No significant feature was identified");
        } else {
            String str = "<table border=\"1\" cellpadding=\"5\">";
            str = str + "<tr><th>" + lbl + "</th>";
            for (String colname : colnames) {
                str = str + "<th>" + colname + "</th>";
            }
            str = str + "</tr>";
            for (int i = 0; i < rownames.length; i++) {
                str = str + "<tr><td>" + rownames[i] + "</td>";
                for (int j = 0; j < colnames.length; j++) {
                    str = str + "<td>" + sigmat[i][j] + "</td>";
                }
                str = str + "</tr>";
            }
            str = str + "</table>";
            return str;
        }
    }

    // note since Rserver only return [][] for double, we can get double[][] and String[] and combine them here
    // note: the colnames include the last name for the extraCol
    public static String setupTable(String lbl, double[][] sigmat, String[] extraCol, String[] rownames, String[] colnames) {

        if (rownames == null || rownames.length == 0) {
            return ("No significant feature was identified");
        } else {
            String str = "<table border=\"1\" cellpadding=\"5\">";
            str = str + "<tr><th>" + lbl + "</th>";
            for (String colname : colnames) {
                str = str + "<th>" + colname + "</th>";
            }

            //remember colnames here is longer than matrix
            int col_len = colnames.length - 1;
            str = str + "</tr>";
            for (int i = 0; i < rownames.length; i++) {
                str = str + "<tr><td>" + rownames[i] + "</td>";
                for (int j = 0; j < col_len; j++) {
                    str = str + "<td>" + sigmat[i][j] + "</td>";
                }
                str = str + "<td>" + extraCol[i] + "</td></tr>";
            }
            str = str + "</table>";
            return str;
        }
    }

    public static ArrayList<String> getQueryNames(String text) {
        try {
            ArrayList<String> nmVec = new ArrayList();
            StringTokenizer st = new StringTokenizer(text, "\n");
            while (st.hasMoreTokens()) {
                String line = st.nextToken();
                line = line.trim();//remove both leading and end space
                if (line.length() == 0) { //empty line
                    continue;
                }

                if (line.indexOf(";") > 0) {
                    nmVec.addAll(Arrays.asList(line.split(";")));
                } else {
                    nmVec.add(line);
                }

            }
            return nmVec;
        } catch (NumberFormatException e) {
             LOGGER.error("getQueryNames", e);
            return null;
        }
    }

    //parse String between one layer of HTML tag
    public static String getStringHTMLTag(String htmlString) {
        Pattern pattern = Pattern.compile("<([A-Za-z][A-Za-z0-9]*)\\b[^>]*>(.*?)</\\1>");
        Matcher m = pattern.matcher(htmlString);
        if (m.find()) {
            return (m.group(2));
        }

        return htmlString;
    }

    public static String[] getQueryNames(String text, String sep) {
        return getNamesArray(text, sep);
    }

    public static String[] getQueryNames(UploadedFile uploadedFile, String sep) {
        try {
            return getNamesArray(convertStreamToString(uploadedFile.getInputStream()), sep);
        } catch (IOException e) {
            LOGGER.error("getQueryNames", e);
            return null;
        }
    }

    //compound names need to be  need to be one name per column
    public static String[] getNamesArray(String content, String sep) {
        ArrayList<String> nmVec = new ArrayList();
        if (sep == null) {
            sep = System.getProperty("line.separator");
        }
        //seperate by line separator or ";"
        StringTokenizer st = new StringTokenizer(content, sep);

        while (st.hasMoreTokens()) {
            String line = st.nextToken();
            line = cleanString(line);//remove both leading and end space
            if (line.length() == 0) { //empty line
                continue;
            }
            nmVec.add(line);
        }
        //remove duplicates, if any
        Set<String> mySet = new LinkedHashSet(nmVec);

        return mySet.toArray(String[]::new);
    }

    public static String convertStreamToString(java.io.InputStream is) {
        //java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return new java.util.Scanner(is).useDelimiter("\\A").next();
        //return s.hasNext() ? s.next() : "";
    }

    // clear string spaces and punctuations
    public static String cleanString(String s) {
        s = s.replaceAll("^\\s+", ""); //remove leading space
        s = s.replaceAll("\\s+$", ""); //remove trailing space
        s = s.replaceAll("[^a-zA-Z0-9)]$", ""); //remove last one if not character/number/) (i.e. punctuation)
        return s;
    }

    public static boolean cropImage(String convertPath, String imgPath, String targetPath, int x, int y, int width, int height, int quality) {

        if (quality < 0 || quality > 100) {
            quality = 75;
        }

        ArrayList command = new ArrayList(10);

        // note: CONVERT_PROG is a class variable that stores the location of ImageMagick's convert command
        // need to supply full path to the covert command
        command.add(convertPath);
        command.add("-crop");
        command.add(width + "x" + height + "+" + x + "+" + y);

        command.add("-quality");
        command.add("" + quality);
        command.add("+repage");
        command.add(imgPath);
        command.add(targetPath);

        return myExec((String[]) command.toArray(new String[1]));
    }

    /**
     * Tries to exec the command, waits for it to finish, logs errors if exit
     * status is nonzero, and returns true if exit status is 0 (success).
     *
     * @param command Description of the Parameter
     * @return Description of the Return Value
     */
    public static boolean myExec(String[] command) {
        Process proc;
        try {
            //System.out.println("Trying to execute command " + Arrays.asList(command));
            proc = Runtime.getRuntime().exec(command);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("IOException while trying to execute " + Arrays.toString(command));
            return false;
        }
        //System.out.println("Got process object, waiting to return.");
        int exitStatus;
        while (true) {
            try {
                exitStatus = proc.waitFor();
                break;
            } catch (java.lang.InterruptedException e) {
                 LOGGER.error("myExec", e);
                //System.out.println("Interrupted: Ignoring and waiting");
            }
        }
        if (exitStatus != 0) {
            
            System.out.println("Error executing command: " + Arrays.toString(command));
        }
        return (exitStatus == 0);
    }

    public static boolean myExec(String command) {
        Process proc;
        try {
            //System.out.println("Trying to execute command " + Arrays.asList(command));
            proc = Runtime.getRuntime().exec(command);
        } catch (Exception e) {
            LOGGER.error("myExec", e);
            System.out.println("IOException while trying to execute " + command);
            return false;
        }
        //System.out.println("Got process object, waiting to return.");
        int exitStatus;
        while (true) {
            try {
                exitStatus = proc.waitFor();
                break;
            } catch (java.lang.InterruptedException e) {
                System.out.println("Interrupted: Ignoring and waiting");
            }
        }
        if (exitStatus != 0) {
            System.out.println("Error executing command: " + exitStatus);
        }
        return (exitStatus == 0);
    }

    //do system call with return the result from sytem output
    public static String executeCommand(String command) {

        StringBuilder output = new StringBuilder();
        Process p;
        try {
            p = Runtime.getRuntime().exec(command);
            p.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        } catch (Exception e) {
           LOGGER.error("executeCommand", e);
        }
        return output.toString();
    }

    public static String uploadFile(UploadedFile file, String homeDir, String outFileNm, boolean onPublicServer) {

        if (file == null || file.getSize() == 0) {
            DataUtils.updateMsg("Error", "Empty file?");
            return null;
        }

        ApplicationBean1 ab = (ApplicationBean1) DataUtils.findBean("applicationBean1");
        if (onPublicServer & file.getSize() > ab.getMAX_UPLOAD_SIZE()) {
            DataUtils.updateMsg("Error", "The file size exceeds limit: 50M");
            return null;
        }

        String fileName = file.getFileName();

        if (fileName.endsWith(".csv") | fileName.endsWith(".txt") | fileName.endsWith(".zip") | fileName.endsWith(".mzTab") | fileName.endsWith(".mztab") | fileName.contains("R")) {
            try {
                OutputStream out;
                try (InputStream in = file.getInputStream()) {
                    if (outFileNm == null) {
                        outFileNm = fileName;
                    }   out = new FileOutputStream(new File(homeDir + File.separator + outFileNm));
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = in.read(buffer)) >= 0) {
                        out.write(buffer, 0, len);
                    }
                }
                out.close();

            } catch (IOException e) {
               LOGGER.error("uploadFile", e);
            }
        } else {
            DataUtils.updateMsg("Error", "Only tab delimited (.txt) or comma separated (.csv) files are accepted. If file is mzTab, ensure it has been validated!");
            return null;
        }
        return fileName;
    }

    public static void deleteFile(User usr, String filename) {
        File f1 = new File(usr.getHomeDir() + "/" + filename);
        if (f1.exists()) {
            boolean sucess = f1.delete();
            if (!sucess) {
                System.out.println("=== Delete file - " + filename + " failed.");
            }
        }
    }

    //a utility function to remove the old user folders
    //called everytime a new user folder is created, default 12 hours
    public static void deleteFilesOlderThanNdays(String dirWay) {

        File directory = new File(dirWay);
        //Calendar cal = Calendar.getInstance();
        //cal.add(Calendar.DAY_OF_MONTH, -1);
        //long purgeTime = cal.getTimeInMillis();
        long currentTime = new Date().getTime();
        long purgeTime = 12 * 60 * 60 * 1000;
        if (directory.exists()) {
            File[] listFiles = directory.listFiles();
            for (File listFile : listFiles) {
                if (listFile.getName().startsWith("guest")) {
                    if (currentTime - listFile.lastModified() > purgeTime) {
                        if (!deleteDir(listFile.getAbsolutePath())) {
                            System.err.println("Unable to delete file: " + listFile);
                        }
                    }
                }
            }
        }
    }

    //a utility function to remove the old user folders
    //called everytime a new user folder is created, default 1 day
    public static void deleteFilesOlderThanNdays(String dirWay, int n) {
        //System.out.println(dirWay + "===olderthanndays");
        File directory = new File(dirWay);

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -1 * n);
        long purgeTime = cal.getTimeInMillis();

        // for project saving folders
        Calendar cal2 = Calendar.getInstance();
        cal2.add(Calendar.DAY_OF_MONTH, -1 * 90);

        long purgeTime3Months = cal2.getTimeInMillis();
        if (directory.exists()) {
            File[] listFiles = directory.listFiles();
            for (File listFile : listFiles) {
                if (listFile.getName().endsWith("tmp")) {
                    if (listFile.lastModified() < purgeTime) {
                        if (!deleteDir(listFile.getAbsolutePath())) {
                            System.err.println("Unable to delete file: " + listFile);
                        }
                    }
                } else if (listFile.getName().endsWith("project")) {
                    if (listFile.lastModified() < purgeTime3Months) {
                        if (!deleteDir(listFile.getAbsolutePath())) {
                            System.err.println("Unable to delete file: " + listFile);
                        }
                    }
                }
            }
        }        
    }

    //a utility function to kill long running Rserver process 
    //Note 1) spare the mother (the oldest one) 
    //     2) default 1 hour    
    //     3) if an Rserve process is 100% CPU (>99%) for all measured (at least 3) time points, then kill it (i.e. don't wait for 1 hour
    // Only works on Linux !!!! Mac does not recognize --sort=start_time
    // filter need to apply in order day filter, hour filter and minutes filter (not used)
    //for filter based on minutes (20min)
    //String[] rmMinLongCmd = new String[]{bashPath, "-c", "ps -eo pid,etime,args --sort=start_time | grep 'Rserve'| grep -v 'grep' | tail -n +2 | grep '[0-9][0-9]:[0-9][0-9]'| awk 'substr($2,1,(index($2,\":\")-1))-20>=0' | awk '{print $1}' | xargs kill -9"};
    // System.out.println("Process__ :: " + bashPath);
    // System.out.println(Arrays.toString(rmDayLongCmd));
    public static void performResourceCleaning(String bashPath, String RScriptHome) {

        try {

            String sysCmd = bashPath + " " + RScriptHome + "/_clean_jobs.sh";
            Process p = Runtime.getRuntime().exec(sysCmd);
            p.waitFor();
            System.out.println("Successfully performed resource cleaning!");

        } catch (Exception e) {
            System.out.println("Exception in resource cleaning -  ");
            //System.out.println("Exception in resource cleaning - here's what I know: ");
            //LOGGER.error("performResourceCleaning", e);
        }
    }

    //use Unix command to remvoe (non-empty) folder
    // '"curl -H "C-H Content-Type: application/json" -X POST -d "{\"name\": \"prof_website\",  \"entities\":{\"page\": \"entity_value\"}}" https://www.omicsbot.ca/conversations/19dcb8ae575447748cbdba384a22af51/trigger_intent?output_channel=latest"'

    public static boolean executeCurlCommand(String parameters, String url_sessionid) {
        //first make sure they are removable 
        ArrayList command = new ArrayList(8);
        command.add("curl");
        command.add("-H");
        command.add("\"C-H Content-Type: application/json\"");
        command.add("-X");
        command.add("POST");
        command.add("-d");
        command.add(parameters);
        command.add(url_sessionid);
        myExec((String[]) command.toArray(new String[1]));
        return true;
    }
    
    //use Unix command to remvoe (non-empty) folder
    public static boolean deleteDir(String fdPath) {
        //first make sure they are removable 
        ArrayList command = new ArrayList(4);
        command.add("chmod");
        command.add("-R");
        command.add("777");
        command.add(fdPath);
        boolean res = myExec((String[]) command.toArray(new String[1]));

        if (res) {
            command = new ArrayList(3);
            command.add("rm");
            command.add("-r");
            command.add(fdPath);
            return myExec((String[]) command.toArray(new String[1]));
        } else {
            return false;
        }
    }

    public static boolean generateReportCMD(String bashPath, String RScriptHome, String userDir) {
        try {
            //System.setProperty("user.dir", userDir);
            //String sysCmd = cmdPath + " " + userDir + "/Analysis_Report.tex";
            //System.out.println("===========" + sysCmd);
            String sysCmd = bashPath + " " + RScriptHome + "/_bash_to_r.sh" + " " + RScriptHome + "/_sweave_cmd.R" + " " + userDir;

            //System.out.println("=====sysCmd======" + sysCmd);
            Process p = Runtime.getRuntime().exec(sysCmd);
            p.waitFor();

        } catch (Exception e) {
            //System.out.println("exception happened - here's what I know: ");
             LOGGER.error("generateReportCMD", e);
            return false;
        }
        return true;
    }

    //give a string vector ["a", "b", "c"], return a single string 
    public static String createStringVector(String[] nms) {
        StringBuilder sb = new StringBuilder();
        for (String s : nms) {
            sb.append(s);
            sb.append("\",\"");
        }
        String res = sb.toString();
        //trim the last comma and quote
        //System.out.println(res + "====");
        res = res.substring(0, res.length() - 2);
        res = "c(\"" + res + ")";
        return (res);
    }

    public static boolean unzipData(String zipFilePath, String destDirectory) {
        File destDir = new File(destDirectory);
        if (!destDir.exists()) {
            destDir.mkdir();
        }
        try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath))) {
            ZipEntry entry = zipIn.getNextEntry();
            // iterates over entries in the zip file
            while (entry != null) {
                String name = entry.getName();
                String filePath = destDirectory + File.separator + name;
                if (!entry.isDirectory() && !(name.contains("MACOSX") | name.contains("DS_Store"))) {
                    // if the entry is a file, extracts it

                    extractFile(zipIn, filePath);
                } else {
                    // if the entry is a directory, make the directory
                    File dir = new File(filePath);
                    dir.mkdir();
                }
                zipIn.closeEntry();
                entry = zipIn.getNextEntry();
            }
            return true;
        } catch (Exception e) {
             LOGGER.error("unzipData", e);
            return false;
        }
    }

    /**
     * Extracts a zip entry (file entry)
     *
     * @param zipIn
     * @param filePath
     * @throws IOException
     */
    public static void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath))) {
            byte[] bytesIn = new byte[BUFFER_SIZE];
            int read;
            while ((read = zipIn.read(bytesIn)) != -1) {
                bos.write(bytesIn, 0, read);
            }
        }
    }

    public static void showMessage(FacesMessage.Severity type, String header, String body) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(type, header, body));
    }

    /**
     * *
     * Extract zipfile to outdir with complete directory structure
     *
     * @param zipfile Input .zip file
     * @param outdir Output directory
     */
    public static void extract(String zipfileName, String outdirName) {
        File zipfile = new File(zipfileName);
        File outdir = new File(outdirName);
        try {
            try (ZipInputStream zin = new ZipInputStream(new FileInputStream(zipfile))) {
                ZipEntry entry;
                String name, dir;
                while ((entry = zin.getNextEntry()) != null) {
                    name = entry.getName();
                    if (entry.isDirectory()) {
                        mkdirs(outdir, name);
                        continue;
                    }
                    /* this part is necessary because file entry can come before
                    * directory entry where is file located
                    * i.e.:
                    *   /foo/foo.txt
                    *   /foo/
                    */
                    dir = dirpart(name);
                    if (dir != null) {
                        mkdirs(outdir, dir);
                    }
                    
                    extractFile2(zin, outdir, name);
                }
            }
        } catch (IOException e) {
             LOGGER.error("extract", e);
        }
    }

    private static void extractFile2(ZipInputStream in, File outdir, String name) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(new File(outdir, name)))) {
            int count;
            while ((count = in.read(buffer)) != -1) {
                out.write(buffer, 0, count);
            }
        }
    }

    private static void mkdirs(File outdir, String path) {
        File d = new File(outdir, path);
        if (!d.exists()) {
            d.mkdirs();
        }
    }

    private static String dirpart(String name) {
        int s = name.lastIndexOf(File.separatorChar);
        return s == -1 ? null : name.substring(0, s);
    }
    private static String rawUploadErrorFile = "";

    public static String getRawUploadErrorFile() {
        return rawUploadErrorFile;
    }

    public static void setRawUploadErrorFile(String rawUploadErrorFile) {
        DataUtils.rawUploadErrorFile = rawUploadErrorFile;
    }

    public static int moveFileToSubDirectory(String fileName, String destDirectory) {
        File rawFile = new File(fileName);
        File destDir = new File(destDirectory);
        String name = rawFile.getName();
        if (!destDir.exists()) {
            destDir.mkdir();
        }
        try {
            if (name.endsWith("mzXML") || name.endsWith("mzML") || name.endsWith("mzData")) {
                rawFile.renameTo(new File(destDir.getName() + rawFile.getName()));
            } else {
                return 0;
            }
        } catch (Exception e) {
             LOGGER.error("moveFileToSubDirectory", e);
        }
        return 1;
    }

    public static int unzipDataRaw(String zipFilePath, String destDirectory) {
        File destDir = new File(destDirectory);
        if (!destDir.exists()) {
            destDir.mkdir();
        }
        try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath))) {
            ZipEntry entry = zipIn.getNextEntry();
            // iterates over entries in the zip file
            while (entry != null) {
                String name = entry.getName();
                String filePath = destDirectory + File.separator + name;
                //System.out.println(filePath + "===filepath");
                //if (!entry.isDirectory() && !(name.contains("MACOSX") || name.contains("DS_Store"))) {
                if (!entry.isDirectory()) {
                    // if the entry is a file, extracts it
                    //if (name.endsWith("mzXML") || name.endsWith("mzML") || name.endsWith("mzData")) {
                    extractFile(zipIn, filePath);
                    //} else {
                    //    setRawUploadErrorFile(name);
                    //    return -1;
                    //}
                } else {
                    // if the entry is a directory, make the directory
                    if (!name.contains("_MACOSX")) {
                        File dir = new File(filePath);
                        dir.mkdir();
                    }
                }
                zipIn.closeEntry();
                entry = zipIn.getNextEntry();
            }
            return 1;
        } catch (Exception e) {
            LOGGER.error("unzipDataRaw", e);
            return 0;
        }
    }

    public static void runRawSpecScript(RConnection RC, String RScriptHome, String bashPath, String userPath, String usrNm,
            String rCommandOpt, String rCommandProcess, String fileNms) {
        try {
            //String sysCmd = bashPath + " " + RScriptHome + "/_raw_spec_cmd.sh" + " " + userPath + "/ExecuteRawSpec.R";
            String Rcmd = null;
            if ("opt".equals(rCommandProcess)) {
                Rcmd = "library(OptiLCMS); "
                        + "setwd(\'" + userPath + "\'); "
                        + "mSet <- InitDataObjects('spec', 'raw', FALSE);\n "
                        + "mSet <- UpdateRawfiles(mSet," + fileNms.replaceAll("\"", "\'") + ");\n "
                        + "plan <- InitializaPlan('raw_opt');\n "
                        + rCommandOpt.replaceAll("\"", "\'") + ";\n"
                        + "res <- ExecutePlan(plan);\n Export.Annotation(res[['mSet']]);\n Export.PeakTable(res[['mSet']]);\n Export.PeakSummary(res[['mSet']])";
            } else if ("default".equals(rCommandProcess)) {
                Rcmd = "library(OptiLCMS); "
                        + "setwd(\'" + userPath + "\'); "
                        + "mSet <- InitDataObjects('spec', 'raw', FALSE);\n "
                        + "mSet <- UpdateRawfiles(mSet," + fileNms.replaceAll("\"", "\'") + ");\n "
                        + "plan <- InitializaPlan('raw_ms');\n "
                        + rCommandOpt.replaceAll("\"", "\'") + ";\n"
                        + "res <- ExecutePlan(plan);\n Export.Annotation(res[['mSet']]);\n Export.PeakTable(res[['mSet']]);\n Export.PeakSummary(res[['mSet']])";
            }

            try (FileWriter myWriter = new FileWriter(userPath + "/my_spec_cmds.R")) {
                myWriter.write(Rcmd);
            }
            String sysCmd = bashPath + " " + RScriptHome + "/_bash_to_r.sh" + " " + userPath + "/my_spec_cmds.R";

            Runtime.getRuntime().exec(sysCmd);

            System.out.println(sysCmd + "|||||Things are running !");
        } catch (Exception e) {
            //System.out.println("exception happened - here's what I know: ");
             LOGGER.error("runRawSpecScript", e);
        }
    }

    public static void doRedirect(String url) {
        try {
            FacesContext.getCurrentInstance().getExternalContext().redirect(url);
        } catch (IOException ioe) {
            LOGGER.error("doRedirect", ioe);
        }
    }

    public static User loadUser(String guestName) {
        try {
            ApplicationBean1 ab = (ApplicationBean1) DataUtils.findBean("applicationBean1");
            //try to clean the user folder to remove old files (more than 1 day)
            String realUserHomePath = ab.getRealUserHomePath();
            DataUtils.deleteFilesOlderThanNdays(realUserHomePath, 1);
            //first create a random user names
            User user = new User();
            String guestDir = realUserHomePath + File.separator + guestName;
            File guestFolder = new File(guestDir);
            if (!guestFolder.exists()) {
                guestFolder.mkdir();
            }
            user.setName(guestName);
            user.setRelativeDir("/resources/users/" + guestName);
            user.setHomeDir(guestDir);
            return user;
        } catch (Exception e) {
            //System.out.println("Error in creating users! ===============");
             LOGGER.error("loadUser", e);
        }
        return null;
    }

    public static User loadRawUser(String guestName) {
        try {
            ApplicationBean1 ab = (ApplicationBean1) DataUtils.findBean("applicationBean1");
            //try to clean the user folder to remove old files (more than 1 day)
            String userHomePath;
            if (ab.shouldUseScheduler()) {
                userHomePath = ab.getRaw_spec_folder();
            } else {
                userHomePath = ab.getRealUserHomePath();
            }
            DataUtils.deleteFilesOlderThanNdays(userHomePath, 14);
            //first create a random user names
            User user = new User();
            String guestDir = userHomePath + File.separator + guestName;
            File guestFolder = new File(guestDir);
            if (!guestFolder.exists()) {
                guestFolder.mkdir();
            }
            user.setName(guestName);
            user.setRelativeDir("/resources/users/" + guestName);
            user.setHomeDir(guestDir);
            return user;
        } catch (Exception e) {
            // System.out.println("Error in creating users! ===============");
              LOGGER.error("loadRawUser", e);
        }
        return null;
    }

    /**
     * Pings a HTTP URL. This effectively sends a HEAD request and returns
     * <code>true</code> if the response code is in the 200-399 range.
     *
     * @param url The HTTP URL to be pinged.
     * @param timeout The timeout in millis for both the connection timeout and
     * the response read timeout. Note that the total timeout is effectively two
     * times the given timeout.
     * @return <code>true</code> if the given HTTP URL has returned response
     * code 200-399 on a HEAD request within the given timeout, otherwise
     * <code>false</code>.
     */
    public static boolean pingURL(String url, int timeout) {
        url = url.replaceFirst("^https", "http"); // Otherwise an exception may be thrown on invalid SSL certificates.

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setConnectTimeout(timeout);
            connection.setReadTimeout(timeout);
            connection.setRequestMethod("HEAD");
            int responseCode = connection.getResponseCode();
            //System.out.println("=======" + url + " " + responseCode +"=======");
            return (200 <= responseCode && responseCode <= 399);
        } catch (IOException exception) {
            System.out.println("=======" + url + ":failed =======");
            return false;
        }
    }
    // '"curl -H "C-H Content-Type: application/json" -X POST -d "{\"name\": \"prof_website\",  \"entities\":{\"page\": \"entity_value\"}}" https://www.omicsbot.ca/conversations/19dcb8ae575447748cbdba384a22af51/trigger_intent?output_channel=latest"'


}
