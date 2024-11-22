/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package metaboanalyst.rwrappers;

import java.util.logging.Level;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Jeff
 */
public class RCenter {

    // public static final String RserveIP = "129.128.246.14";
    public static final String RserveIP = "127.0.0.1";
    public static final String RserveIP2 = "127.0.0.2";//to be edited if not local
    public static final int Rport = 6311;
    public static final int Rport2 = 6312; //define ports for Rserve, default 6311 is for MetaboAnalyst
    private static final Logger LOGGER = LogManager.getLogger(RCenter.class);

    //return an RConnection object and specify the home directory
    public static RConnection getRConnection(String homeDir, String scriptPath, String moduleNm) {
        try {
            RConnection RC = new RConnection(RCenter.RserveIP, RCenter.Rport);
            String rCommand = "setwd(\"" + homeDir + "\")\n"
                    + "unlink(dir(), recursive=T)\n"
                    + "file.create(\"Rhistory.R\")\n";
            RC.voidEval(rCommand);
            RC.voidEval("source(\"" + scriptPath + "\")");
            RC.voidEval("LoadRscripts(\"" + moduleNm + "\")");
            recordRserveConnection(RC, homeDir);
            return RC;
        } catch (RserveException e) {
            LOGGER.error("getRConnection", e);
            return null;
        }
    }

    //for switching module
    public static RConnection updateRConnection(RConnection RC1, RConnection RC2, String scriptPath, String moduleNm, String homeDir) {
        try {
            //String rCommand = "rm(list=setdiff(ls(), \"mSet\"))\n";
            String rCommand = "qs::qsave(mSet, file=\"mSet.qs\");";
            RC1.voidEval(rCommand);
            RC1.close();

            rCommand = "setwd(\"" + homeDir + "\");"
                    + "source(\"" + scriptPath + "\");"
                    + "LoadRscripts(\"" + moduleNm + "\");"
                    + "mSet <<- qs::qread(\"mSet.qs\")\n";
            RC2.voidEval(rCommand);
            return RC2;
        } catch (RserveException e) {
            LOGGER.error("updateRConnection", e);
            return null;
        }
    }

    public static RConnection getRConnectionRawSharing(String homeDir, String scriptPath, String moduleNm) {
        try {
            RConnection RC = new RConnection(RCenter.RserveIP, RCenter.Rport);
            String rCommand = "setwd(\"" + homeDir + "\")\n";
            RC.voidEval(rCommand);
            RC.voidEval("source(\"" + scriptPath + "\")");
            RC.voidEval("LoadRscripts(\"" + moduleNm + "\")");
            recordRserveConnection(RC, homeDir);
            return RC;
        } catch (RserveException e) {
            LOGGER.error("getRConnectionRawSharing", e);
            return null;
        }
    }

    //R connection without preloading anything
    public static RConnection getCleanRConnection() {

        try {
            RConnection RC = new RConnection(RCenter.RserveIP, RCenter.Rport);
            //System.out.println(RC);
            return RC;
        } catch (RserveException e) {
            LOGGER.error("getCleanRConnection", e);
            return null;
        }
    }

    public static boolean recordRserveConnection(RConnection RC, String homeDIR) {
        String record_cmd = "if(file.exists(\"/data/glassfish/projects/RServePID_History\")){write.table(paste0(Sys.getpid(), \" - \" , basename(\" " + homeDIR + "\"), \" ---> MetaboAnalyst <--- \", Sys.time()), file = \"/data/glassfish/projects/RServePID_History\", append = T, col.names = F, quote = F, row.names = F)}";
        
        try {
            RC.voidEval(record_cmd);
        } catch (RserveException ex) {
            java.util.logging.Logger.getLogger(RCenter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    public static boolean compileRScripts(String homeDir, String scriptPath) {
        try {
            //System.out.println("=============" + homeDir);
            RConnection RC = new RConnection(RCenter.RserveIP, RCenter.Rport);
            RC.voidEval("setwd(\"" + homeDir + "\"); source(\"" + scriptPath + "\")");
            recordRserveConnection(RC, homeDir);
            return RC.eval("CompileScripts()").asInteger() == 1;
        } catch (Exception e) {
            LOGGER.error("compileRScripts", e);
            return false;
        }
    }

    //this should be the better one
    public static void recordRCommand(RConnection RC, String rCommand, Boolean doEval) {
        try {
            rCommand = cleanRCmd(rCommand);
            if (doEval) {
                RC.voidEval("RecordRCommand(NA" + ", \"" + rCommand + "\")");
            } else {
                recordRCommand(RC, rCommand);
            }
        } catch (Exception e) {
            //e.printStackTrace();
            LOGGER.error("recordRCommand", e);
        }
    }

    //this should be the better one
    public static void saveCurrentSession(RConnection RC) {
        try {
            RC.voidEval("SaveCurrentSession()");
        } catch (Exception e) {
            //e.printStackTrace();
            LOGGER.error("saveCurrentSession", e);
        }
    }

    public static void recordRCommand(RConnection RC, String rCmd) {
        try {
            String rCommand = cleanRCmd(rCmd);
            //for local MetaboAnalystR package
            if (rCommand.contains("(NA")) {
                rCommand = rCommand.replace("(NA", "(mSet");
                rCommand = "mSet<-" + rCommand;
            }
            if (rCommand.contains("InitDataObjects")) {
                rCommand = "mSet<-" + rCommand;
            }
            RC.voidEval("RecordRCommand(NA" + ", \"" + rCommand + "\")");
        } catch (Exception e) {
            //e.printStackTrace();
            LOGGER.error("recordRCommand", e);
        }
    }

    public static String cleanRCmd(String rCommand) {
        int inx = 0;
        while (rCommand.indexOf("\"", inx) != -1) {
            inx = rCommand.indexOf("\"", inx);
            StringBuffer strB = new StringBuffer(rCommand).insert(inx, "\\");
            rCommand = strB.toString();
            inx = inx + 2; //move one step, plus the position of backslash
        }
        return rCommand;
    }

    public static String[] getRCommandHistory(RConnection RC) {
        try {
            return RC.eval("GetRCommandHistory(NA)").asStrings();
        } catch (Exception e) {
            LOGGER.error("getRCommandHistory", e);
        }
        return null;
    }

    public static void loadReporterFuns(RConnection RC, String module) {
        try {
            String rCommand = "LoadReporter(\"" + module + "\");";
            RC.voidEval(rCommand);
        } catch (Exception e) {
            LOGGER.error("loadReporterFuns", e);
        }
    }

    public static boolean prepareReport(RConnection RC, String usrName) {
        try {
            if (usrName.endsWith("tmp")) {
                usrName = usrName.substring(0, usrName.length() - 3);
            }
            String rCommand = "PreparePDFReport(NA" + ", \"" + usrName + "\")\n";
            RC.voidEval(rCommand);
            RCenter.recordRCommand(RC, rCommand);
            return true;
        } catch (Exception e) {//Catch exception if any
            LOGGER.error("prepareReport", e);
            return false;
        }
    }

    public static String getBashFullPath(RConnection RC) {
        try {
            //System.out.println("======= RC ++++----: " + RC);
            return RC.eval("GetBashFullPath()").asString();
        } catch (Exception rse) {
            LOGGER.error("getBashFullPath", rse);
            return null;
        }
    }

    public static void showMemoryUsage(RConnection RC) {
        try {
            String rCommand = "ShowMemoryUse();";
            RC.voidEval(rCommand);
            //compound names for hypergeometric test
            //RCenter.recordRCommand(RC, rCommand);
        } catch (Exception e) {
            LOGGER.error("showMemoryUsage", e);
        }
    }

    public static void loadRScripts(RConnection RC, String moduleNm) {
        try {
            RC.voidEval("LoadRscripts(\"" + moduleNm + "\")");
        } catch (Exception e) {
            LOGGER.error("loadRScripts", e);
        }
    }

    //this use a NEW Rconnection,  dat.in and my.fun 
    public static int performRserveMicro(String homeDir) {
        try {
            RConnection RC = new RConnection(RCenter.RserveIP, RCenter.Rport);
            //System.out.println("===== now peforming microservice .........");
            String rCommand = "setwd(\"" + homeDir + "\");"
                    + "dat.in <- qs::qread(\"dat.in.qs\");"
                    + "dat.in$my.res <- dat.in$my.fun();"
                    + "qs::qsave(dat.in, file=\"dat.in.qs\");";
            int res = RC.eval(rCommand).asInteger();
            recordRserveConnection(RC, homeDir);
            RC.close();
            return res;
        } catch (Exception e) {
            LOGGER.error("performRserveMicro", e);
            return 0;
        }
    }

    public static void saveRLoadImg(RConnection RC) {
        try {
            RC.voidEval("save(list = ls(all.names = TRUE), file = \"Rload.RData\")");
        } catch (Exception e) {
            LOGGER.error("saveRLoadImg", e);
        }
    }

    public static void loadHistory(RConnection RC) {
        try {
            String rCommand = "load(\"Rload.RData\")";
            RC.voidEval(rCommand);
        } catch (RserveException rse) {
            //System.out.println(rse);
            LOGGER.error("loadHistory", rse);
        }
    }
}
