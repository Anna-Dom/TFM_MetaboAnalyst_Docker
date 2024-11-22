/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package metaboanalyst.controllers.mummichog;

import java.io.Serializable;
import java.util.Arrays;
import javax.inject.Named;
import javax.faces.view.ViewScoped;
import metaboanalyst.controllers.ApplicationBean1;
import metaboanalyst.controllers.SessionBean1;
import metaboanalyst.rwrappers.REnrichUtils;
import metaboanalyst.rwrappers.RDataUtils;
import metaboanalyst.utils.DataUtils;
import org.primefaces.event.TransferEvent;
import org.primefaces.model.DualListModel;
import org.rosuda.REngine.Rserve.RConnection;

/**
 *
 * @author jasmine
 */
@ViewScoped
@Named("peakcBean")
public class PeakCustomBean implements Serializable {

    private final ApplicationBean1 ab = (ApplicationBean1) DataUtils.findBean("applicationBean1");
    private final SessionBean1 sb = (SessionBean1) DataUtils.findBean("sessionBean1");
    private final MummiAnalBean mb = (MummiAnalBean) DataUtils.findBean("mummiAnalBean");

    private DualListModel<String> currItems, adductItems;

    public void customButton_action() {

        String sourcenames = DataUtils.readTextFile(ab.getInternalData("source_currency.txt"));
        String[] newsnames = sourcenames.split("\n");

        String targetnames = DataUtils.readTextFile(ab.getInternalData("target_currency.txt"));
        String[] newtnames = targetnames.split("\n");

        currItems = new DualListModel(Arrays.asList(newsnames), Arrays.asList(newtnames));

        RConnection RC = sb.getRConnection();
        String mode = REnrichUtils.getMummiMSMode(RC);
        String adductSPath;
        String adductTPath;

        switch (mode) {
            case "positive":
                adductSPath = ab.getInternalData("source_pos_add_list.txt");
                adductTPath = ab.getInternalData("target_pos_add_list.txt");
                break;
            case "negative":
                adductSPath = ab.getInternalData("source_neg_add_list.txt");
                adductTPath = ab.getInternalData("target_neg_add_list.txt");
                break;
            default:
                // need to deal w. mixed mode
                adductSPath = ab.getInternalData("source_mixed_add_list.txt");
                adductTPath = ab.getInternalData("target_mixed_add_list.txt");
                break;
        }

        String allSAdducts = DataUtils.readTextFile(adductSPath);
        String[] newsadducts = allSAdducts.split("\n");

        String allTAdducts = DataUtils.readTextFile(adductTPath);
        String[] newtadducts = allTAdducts.split("\n");

        adductItems = new DualListModel(Arrays.asList(newsadducts), Arrays.asList(newtadducts));

        if (sb.getDataType().equals("mass_all") || sb.getDataType().equals("mass_table")) {

            Double cutoff = REnrichUtils.getDefaultPvalCutoff(RC);
            if (cutoff == -1) {
                cutoff = 0.001;
            }
            mb.setPvalCutoff(cutoff);
        }

        int lvls = REnrichUtils.getPeakDataLevels(RC);
        if (sb.getDataType().equals("mass_table") && lvls > 2) {
            mb.setDisabledGsea(true);
            mb.setMultigroups(true);
        }

        if (lvls > 2) {
            DataUtils.updateMsg("warning",
                    "Total of " + lvls + " groups found - ANOVA will be used for selecting significant peaks! Using 'Data Editor' to include <b>2</b> groups is recommended for easy interpretation.");
        }

    }

    public DualListModel<String> getCurrItems() {
        return currItems;
    }

    public void setCurrItems(DualListModel<String> currItems) {
        this.currItems = currItems;
    }

    public DualListModel<String> getAdductItems() {
        return adductItems;
    }

    public void setAdductItems(DualListModel<String> adductItems) {
        this.adductItems = adductItems;
    }

    private String adductList;

    public String getAdductList() {

        if (adductList == null) {
            adductList = DataUtils.readTextFile(ab.getInternalData("target_pos_add_list.txt"));
        }
        return adductList;
    }

    public void setAdductList(String adductList) {
        this.adductList = adductList;
    }

    private String currList;

    public String getCurrList() {

        if (currList == null) {
            currList = DataUtils.readTextFile(ab.getInternalData("target_currency.txt"));
        }
        return currList;
    }

    public void setCurrList(String currList) {
        this.currList = currList;
    }

    private boolean loggedIn = false;
    private boolean currUploaded = false;
    private boolean adductUploaded = false;

    public void handleAdductListUpload() {

        adductList = adductList.trim();
        adductUploaded = false;
        RConnection RC = sb.getRConnection();
        int res = REnrichUtils.readAdductData(RC, adductList);

        if (res == 1) {
            DataUtils.updateMsg("OK", RDataUtils.getCurrentMsg(RC));
            adductUploaded = true;
        } else {
            DataUtils.updateMsg("Error", RDataUtils.getErrMsg(RC));
        }
    }

    public String integrityCheckMum() {

        if (!(adductList == null | adductList.trim().length() == 0)) {
            handleAdductListUpload();
        }

        if (!adductUploaded && !currUploaded) {
            DataUtils.updateMsg("Error", "Please ensure that the currency and adduct lists are not empty!");
            return null;
        }

        sb.setDataUploaded();
        return "mzlibview";

    }

    public void doTransfer(TransferEvent event) {
        StringBuilder builder = new StringBuilder();
        for (Object item : event.getItems()) {
            builder.append(item.toString()).append("<br />");
        }
    }

    public String editCurrency() {
        String[] currVec = currItems.getTarget().toArray(String[]::new);
        if (currVec.length == 0) {
            currVec = new String[]{""};
        }
        RConnection RC = sb.getRConnection();

        RDataUtils.setCurrMapData(RC, currVec);
        int res = RDataUtils.performCurrencyMapping(RC);
        String msg = REnrichUtils.getCurrencyMsg(RC);

        if (res > 0) {
            DataUtils.updateMsg("OK", msg);

        } else {
            DataUtils.updateMsg("Error", msg);
            return null;
        }
        return "mzlibview";
    }

    public void editCurrencyvoid() {
        String[] currVec = currItems.getTarget().toArray(String[]::new);
        if (currVec.length == 0) {
            currVec = new String[]{""};
        }
        RConnection RC = sb.getRConnection();

        RDataUtils.setCurrMapData(RC, currVec);
        int res = RDataUtils.performCurrencyMapping(RC);
        String msg = REnrichUtils.getCurrencyMsg(RC);

        if (res > 0) {
            DataUtils.updateMsg("OK", msg);
        } else {
            DataUtils.updateMsg("Error", msg);
        }

    }

    public String editAdducts() {
        String[] addVec = adductItems.getTarget().toArray(String[]::new);
        if (addVec.length == 0) {
            addVec = new String[]{""};
        }
        RConnection RC = sb.getRConnection();
        String mode = REnrichUtils.getMummiMSMode(RC);

        RDataUtils.setAdductData(RC, addVec);
        int res = RDataUtils.performAdductMapping(RC, mode);
        String msg = REnrichUtils.getAdductMsg(RC);

        if (res > 0) {
            DataUtils.updateMsg("OK", msg);
        } else {
            DataUtils.updateMsg("Error", msg);
            return null;
        }
        return "mzlibview";
    }

    public void editAdductsvoid() {
        String[] addVec = adductItems.getTarget().toArray(String[]::new);
        if (addVec.length == 0) {
            addVec = new String[]{""};
        }
        RConnection RC = sb.getRConnection();
        String mode = REnrichUtils.getMummiMSMode(RC);

        RDataUtils.setAdductData(RC, addVec);
        int res = RDataUtils.performAdductMapping(RC, mode);
        String msg = REnrichUtils.getAdductMsg(RC);

        if (res > 0) {
            DataUtils.updateMsg("OK", msg);
        } else {
            DataUtils.updateMsg("Error", msg);
        }
    }

}
