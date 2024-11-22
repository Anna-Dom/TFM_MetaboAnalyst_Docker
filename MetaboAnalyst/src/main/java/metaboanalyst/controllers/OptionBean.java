/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package metaboanalyst.controllers;

import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import javax.faces.model.SelectItem;

/**
 *
 * @author xia
 */
@RequestScoped
@Named("optBean")
public class OptionBean {

    public SelectItem[] getDistMeasureOpts() {
        SelectItem[] distMeasureOpts = new SelectItem[3];
        distMeasureOpts[0] = new SelectItem("pearson", "Pearson r");
        distMeasureOpts[1] = new SelectItem("spearman", "Spearman rank correlation");
        distMeasureOpts[2] = new SelectItem("kendall", "Kendall rank correlation");
        return distMeasureOpts;
    }

    public SelectItem[] getTableTypeOpts() {
        SelectItem[] tableTypeOpts = new SelectItem[3];
        tableTypeOpts[0] = new SelectItem("conc", "Concentrations");
        tableTypeOpts[1] = new SelectItem("specbin", "Spectral bins");
        tableTypeOpts[2] = new SelectItem("pktable", "Peak intensities");
        return tableTypeOpts;
    }

    public SelectItem[] getCsvFormatOpts() {
        SelectItem[] csvFormatOpts = new SelectItem[4];
        csvFormatOpts[0] = new SelectItem("rowu", "Samples in rows (unpaired)");
        csvFormatOpts[1] = new SelectItem("colu", "Samples in columns (unpaired)");
        csvFormatOpts[2] = new SelectItem("rowp", "Samples in rows (paired)");
        csvFormatOpts[3] = new SelectItem("colp", "Samples in columns (paired)");
        return csvFormatOpts;
    }

    public SelectItem[] getTableFormatOpts() {
        SelectItem[] rocFormatOpts = new SelectItem[2];
        rocFormatOpts[0] = new SelectItem("rowu", "Samples in rows");
        rocFormatOpts[1] = new SelectItem("colu", "Samples in columns");
        return rocFormatOpts;
    }

    public SelectItem[] getClsOpts() {
        SelectItem[] clsOpts = new SelectItem[2];
        clsOpts[0] = new SelectItem("disc", "Categorical (Classification)");
        clsOpts[1] = new SelectItem("cont", "Continuous (Regression)");
        return clsOpts;
    }

    public SelectItem[] getClsShortenedOpts() {
        SelectItem[] clsOpts = new SelectItem[2];
        clsOpts[0] = new SelectItem("disc", "Categorical");
        clsOpts[1] = new SelectItem("cont", "Continuous");
        return clsOpts;
    }

    public SelectItem[] getGeneIDOpts() {
        SelectItem[] genericGeneIDOpts = new SelectItem[4];
        genericGeneIDOpts[0] = new SelectItem("NA", "--- Not Specified ---");
        genericGeneIDOpts[1] = new SelectItem("entrez", "Entrez ID");
        genericGeneIDOpts[2] = new SelectItem("symbol", "Official Gene Symbol");
        genericGeneIDOpts[3] = new SelectItem("uniprot", "Uniprot Protein ID");
        return genericGeneIDOpts;
    }

    //those with pathway annotations
    public SelectItem[] getPathCmpdIDOpts() {
        SelectItem[] cmpdIDOpts = new SelectItem[4];
        cmpdIDOpts[0] = new SelectItem("na", "--- Not Specified ---");
        cmpdIDOpts[1] = new SelectItem("name", "Compound Name");
        cmpdIDOpts[2] = new SelectItem("hmdb", "HMDB ID");
        cmpdIDOpts[3] = new SelectItem("kegg", "KEGG ID");
        return cmpdIDOpts;
    }

    public SelectItem[] getCmpdIDOpts() {
        SelectItem[] cmpdIDOpts = new SelectItem[7];
        cmpdIDOpts[0] = new SelectItem("name", "Compound names");
        cmpdIDOpts[1] = new SelectItem("hmdb", "HMDB ID");
        cmpdIDOpts[2] = new SelectItem("kegg", "KEGG ID");
        cmpdIDOpts[3] = new SelectItem("pubchem", "PubChem CID");
        cmpdIDOpts[4] = new SelectItem("chebi", "ChEBI ID");
        cmpdIDOpts[5] = new SelectItem("metlin", "METLIN");
        cmpdIDOpts[6] = new SelectItem("hmdb_kegg", "HMDB and KEGG ID");
        return cmpdIDOpts;
    }

    public SelectItem[] getNetIDOpts() {
        SelectItem[] netIDOpts = new SelectItem[5];
        netIDOpts[0] = new SelectItem("na", "--- Not Specified ---");
        netIDOpts[1] = new SelectItem("name", "Compound Name");
        netIDOpts[2] = new SelectItem("hmdb", "HMDB ID");
        netIDOpts[3] = new SelectItem("kegg", "KEGG ID");
        netIDOpts[4] = new SelectItem("pklist", "Peak List");
        return netIDOpts;
    }

    public SelectItem[] getPairAnalOpts() {
        SelectItem[] pairAnalOpts = new SelectItem[2];
        pairAnalOpts[0] = new SelectItem("FALSE", "Unpaired");
        pairAnalOpts[1] = new SelectItem("TRUE", "Paired");
        return pairAnalOpts;
    }

    public SelectItem[] getEqualVarOpts() {
        SelectItem[] equalVarOpts = new SelectItem[2];
        equalVarOpts[0] = new SelectItem("TRUE", "Equal");
        equalVarOpts[1] = new SelectItem("FALSE", "Unequal");
        return equalVarOpts;
    }

    public SelectItem[] getColorContrastOpts() {
        SelectItem[] colorContrastOpts = new SelectItem[5];
        colorContrastOpts[0] = new SelectItem("bwm", "Default");
        colorContrastOpts[1] = new SelectItem("gbr", "Red / Green");
        colorContrastOpts[2] = new SelectItem("heat", "Heat Color");
        colorContrastOpts[3] = new SelectItem("topo", "Topo Color");
        colorContrastOpts[4] = new SelectItem("gray", "Gray Scale");
        return colorContrastOpts;
    }

    public SelectItem[] getClustDistOpts() {
        SelectItem[] clustDistOpts = new SelectItem[3];
        clustDistOpts[0] = new SelectItem("euclidean", "Euclidean");
        clustDistOpts[1] = new SelectItem("spearman", "Spearman");
        clustDistOpts[2] = new SelectItem("pearson", "Pearson");
        return clustDistOpts;
    }

    public SelectItem[] getClustMethodOpts() {
        SelectItem[] clustMethodOpts = new SelectItem[4];
        clustMethodOpts[0] = new SelectItem("ward.D", "Ward");
        clustMethodOpts[1] = new SelectItem("average", "Average");
        clustMethodOpts[2] = new SelectItem("complete", "Complete");
        clustMethodOpts[3] = new SelectItem("single", "Single");
        return clustMethodOpts;
    }

    public SelectItem[] getMetaClustMethodOpts() {
        SelectItem[] metaClustMethodOpts = new SelectItem[3];
        metaClustMethodOpts[0] = new SelectItem("none", "None");
        metaClustMethodOpts[1] = new SelectItem("kmean", "K-means");
        metaClustMethodOpts[2] = new SelectItem("hierarchical", "Hierarchical");
        return metaClustMethodOpts;
    }

    public SelectItem[] getFeatOpts() {
        SelectItem[] featOpts = new SelectItem[3];
        featOpts[0] = new SelectItem("none", "--- Not Specified ---");
        featOpts[1] = new SelectItem("met", "Metabolites");
        featOpts[2] = new SelectItem("lipid", "Lipids");
        return featOpts;
    }
}
