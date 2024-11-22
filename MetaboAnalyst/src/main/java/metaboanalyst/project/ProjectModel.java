/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package metaboanalyst.project;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author soufanom
 */
public class ProjectModel {

    private int id;
    private long userNM; //this is the userNM number fron DB
    private String title = "";
    private String org = "";
    private String email = "";
    private String folder = "";
    private String type = "";
    private String description = "";
    private String status = "";
    private Date creationDate;
    private String note = "";
    private String ip = "";
    private String hostname = ""; //hostname is the data strage place: dev, qiang pc, qiang laptop etc.

    private final static int PROJECT_WARNING_DAYS = 21;

    public ProjectModel() {
    }

    ;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getUserNM() {
        return userNM;
    }

    public void setUserNM(long userNM) {
        this.userNM = userNM;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCreationDate() {
        //DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        DateFormat df = new SimpleDateFormat("yyyy/MM/dd");
        String mydateStr = df.format(creationDate);
        return mydateStr;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getStatus() {
        if (status.equals("naive")) {
            return "Start";
        } else {
            return "Load";
        }
    }

    public void setStatus(String status) {
        if ("naive".equals(status)) {
            folder = "";
        }
        this.status = status;
    }

    public String getOrg() {
        return org;
    }

    public void setOrg(String org) {
        this.org = org;
    }

    public String getReadableType() {
        switch (type) {
            case "onedata":
                return "Gene Expression Table";
            case "genelist":
                return "Gene List Input";
            case "network":
                return "Network File";
            case "metapaths":
                return "Functional Meta-Analysis of MS Peaks";
            case "metadata":
                return "Statistical Meta-Analysis";
            case "raw":
                return "LC-MS Spectra Processing";
            case "pathinteg":
                return "Joint Pathway Analysis";
            case "stat":
                return "Statistical Analysis [one factor]";
            case "mf":
                return "Statistical Analysis [metadata table]";
            case "path":
                return "Pathway Analysis";
            case "mset":
                return "Enrichment Analysis";
            case "roc":
                return "Biomarker Analysis";
            case "power":
                return "Power Analysis";
            case "utils":
                return "Other Utilities";
            case "mass_all":
            case "mummichog":
            case "mass_table":
                return "Functional Analysis of MS Peaks";
            default:
                return type;
        }
    }

    public String getReadableOrg() {
        switch (org) {
            case "hsa":
                return "H. sapiens (human)";
            case "mmu":
                return "M. musculus (mouse)";
            case "cel":
                return "C. elegans (roundworm)";
            case "rno":
                return "R. norvegicus (rat)";
            case "dme":
                return "D. melanogaster (fruitfly)";
            case "dre":
                return "D. rerio (zebrafish)";
            case "sce":
                return "S. cerevisiae (yeast)";
            case "bta":
                return "B. taurus (cow)";
            case "gga":
                return "G. gallus (chicken)";
            case "ath":
                return "A. thaliana (Arabidopsis)";
            case "eco":
                return "E. coli ";
            case "bsu":
                return "B. subtilis";
            case "tbr":
                return "T. brucei (trypanosoma)";
            case "smm":
                return "S. mansoni";
            case "pfa":
                return "P. falciparum (malaria)";
            case "cjo":
                return "C. japonica (japanese quail)";
            case "mtb":
                return "M. tuberculosis";
            case "pae":
                return "P. aeruginosa";
            default:
                return "Not available";
        }
    }

}
