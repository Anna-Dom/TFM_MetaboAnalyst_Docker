/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package metaboanalyst.project;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import metaboanalyst.controllers.ApplicationBean1;
import metaboanalyst.controllers.SessionBean1;
import metaboanalyst.utils.DataUtils;

/**
 *
 * @author zhiqiang
 */
public class SchedulerUtils {

    public static String getJobStatus(long id) {

        if (id == 0) {
            return "RUNNING";
        }

        String res = "Pending";

        String stateQuery = "sacct -j " + id + ".0 --format=state";

        try {
            Process proc = Runtime.getRuntime().exec(stateQuery);
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String rr = stdInput.readLine() + stdInput.readLine();
            String stateString;
            while ((stateString = stdInput.readLine()) != null) {
                res = stateString;
                //System.out.println("Raw spectra Job " + id + " has been checked for status successfully !");
            }
            if (res.equals("Pending") & rr.equals("nullnull")) {
                res = getJobStatusAlternative();
            }

        } catch (Exception e) {
            // When running on genap, there is impossible to communicate the Slurm directly, as a result, Exception appears!
            // In this case, using getJobStatusAlternative to get the status [Maybe slow and not quite sensitive]
            System.out.println("Exception or Genap Running while trying to execute [getJobStatus] " + id + res);
            res = getJobStatusAlternative();
        }

        res = res.replaceAll("\\s+", "");
        return res;
    }

    public static String getJobStatusAlternative() {
        SessionBean1 sb = (SessionBean1) DataUtils.findBean("sessionBean1");

        // Setting read several marker files as the status
        // 0. ExecuteRawSpec.sh as the initializing marker
        String Marker0 = sb.getCurrentUser().getHomeDir() + "/ExecuteRawSpec.sh";
        String Status0;
        File myObj0 = new File(Marker0);
        if (!myObj0.exists()) {
            Status0 = "FAILED";
        } else {
            Status0 = "";
        }

        // 1. log_progress (0, Non-zero, 100)
        String Marker1 = sb.getCurrentUser().getHomeDir() + "/log_progress.txt";
        String Status1 = "";

        try {
            File myObj = new File(Marker1);
            if (!myObj.exists()) {
                Status1 = "PENDING";
            } else {
                try (Scanner myReader = new Scanner(myObj)) {
                    if (myReader.hasNextLine()) {

                        double data = myReader.nextDouble();

                        if (data < 0.5) {
                            Status1 = "PENDING";
                        } else if (data > 99.9) {
                            Status1 = "FINISHED";
                        } else {
                            Status1 = "RUNNING";
                        }
                    }
                }
            }

        } catch (Exception e) {
            Status1 = "Unknown";
        }

        // 2. metaboanalyst_input.csv as the finished marker
        String Marker2 = sb.getCurrentUser().getHomeDir() + "/metaboanalyst_input.csv";
        String Status2;
        File myObj2 = new File(Marker2);
        if (myObj2.exists()) {
            Status2 = "FINISHED";
        } else {
            Status2 = "";
        }

        // 3. spectra_3d_loading.json as the finished marker2
        String Marker3 = sb.getCurrentUser().getHomeDir() + "/spectra_3d_loading.json";
        String Status3;
        File myObj3 = new File(Marker3);
        if (myObj3.exists()) {
            Status3 = "FINISHED";
        } else {
            Status3 = "";
        }

        // 4. metaboanalyst_spec_proc.txt as the finished marker4
        String Marker4 = sb.getCurrentUser().getHomeDir() + "/metaboanalyst_spec_proc.txt";
        String Status4;
        long bytes = 0;
        Path path = Paths.get(Marker4);
        try {
            bytes = Files.size(path);
        } catch (IOException ex) {
            Logger.getLogger(SchedulerUtils.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (bytes < 150.0) {
            Status4 = "Pending";
        } else {

            StringBuilder s = new StringBuilder();
            try {
                Process p = Runtime.getRuntime().exec("tail -" + 1 + " " + Marker4);
                java.io.BufferedReader input = new java.io.BufferedReader(new java.io.InputStreamReader(p.getInputStream()));
                String line;
                Status4 = "RUNNING";
                while ((line = input.readLine()) != null) {
                    if (line.contains("ERROR:")) {
                        Status4 = "FAILED";
                    } else if (line.contains("Everything has been finished Successfully !")) {
                        Status4 = "FINISHED";
                    }
                }
            } catch (java.io.IOException e) {
                Status4 = "";
            }
        }

        // 5. if this job has been killed or not
        String Marker5 = sb.getCurrentUser().getHomeDir() + "/JobKill";
        String Status5 = "";

        try {
            File myObj5 = new File(Marker5);
            if (!myObj5.exists()) {
                Status5 = "PENDING";
            } else {
                try (Scanner myReader5 = new Scanner(myObj5)) {
                    if (myReader5.hasNextLine()) {
                        String killStatus = myReader5.next();
                        if (!killStatus.equals("0") || killStatus.equals("1")) {
                            Status5 = "Killed";
                        }
                    }
                }
            }

        } catch (Exception e) {
            Status5 = "Unknown";
        }

        String Status;
        // Comprehensive identification on the final results
        if (Status4.equals("FAILED")
                || (Status0.equals("FAILED") && (Status1.equalsIgnoreCase("PENDING") || Status1.equalsIgnoreCase("Unknown")) && Status4.equalsIgnoreCase("Pending"))) {
            // this is logically complicated: status4 failed, means failed without problem; if Status1 failed, not necessary, have to occur with the other 3 markers
            Status = "FAILED";
        } else if ((Status2.equals("FINISHED") && Status3.equals("FINISHED")) || Status1.equals("FINISHED") || Status4.equals("FINISHED")) {
            Status = "FINISHED";
        } else if (Status1.equals("RUNNING") || Status4.equals("RUNNING")) {
            Status = "RUNNING";
        } else {
            Status = "PENDING";
        }

        if (Status5.equals("Killed")) {
            Status = "Killed";
        }

        System.out.println("==================== FINAL Status:" + Status);

        return Status;
    }

    public static int getJobIDAlternative() {

        int jobid = 0;
        SessionBean1 sb = (SessionBean1) DataUtils.findBean("sessionBean1");

        try {
            String JobIDtxt = sb.getCurrentUser().getHomeDir() + "/JobID.txt";
            File JobIDFile = new File(JobIDtxt);
            if (!JobIDFile.exists()) {
                JobIDFile.createNewFile();
            }
            try (Scanner myReader = new Scanner(JobIDFile)) {
                while (myReader.hasNextLine()) {
                    jobid = myReader.nextInt();
                    return jobid;
                }
            }
        } catch (Exception e) {
            System.out.println("An error occurred + getJobIDAlternative");
        }

        return jobid;
    }

    public static boolean killJob(long id) {

        String JobKill = "scancel " + id;

        try {
            Runtime.getRuntime().exec(JobKill);
            return true;
            //setStopStatusCheck(false);
        } catch (Exception e) {
            return false;
        }

    }

    public static int getJobProgress(String folder) {
        int progress = 0;

        ApplicationBean1 ab = (ApplicationBean1) DataUtils.findBean("applicationBean1");
        String spectraPath = ab.getRaw_spec_folder();

        String Marker1 = spectraPath + folder + "/log_progress.txt";

        try {
            File myObj = new File(Marker1);
            if (!myObj.exists()) {
                progress = 0;
                System.out.println(" progress1 -----> " + progress);
            } else {
                try (Scanner myReader = new Scanner(myObj)) {
                    if (myReader.hasNextLine()) {
                        double data = myReader.nextDouble();
                        progress = (int) data;
                        System.out.println(" progress2 -----> " + progress);
                    }
                }
            }

        } catch (Exception e) {
            progress = 0;
        }

        System.out.println(" progress_final -----> " + progress);
        return progress;
    }

}
