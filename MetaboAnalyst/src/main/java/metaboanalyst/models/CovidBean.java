/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package metaboanalyst.models;

import java.io.IOException;
import java.io.Serializable;
import metaboanalyst.utils.DataUtils;
import org.primefaces.model.StreamedContent;

/**
 *
 * @author qiang
 */
public class CovidBean implements Serializable {

    // two column result table
    private final String fileName;
    private String IDs;
    private String Publications;
    private String title;
    private String platform;
    private String PublicationsLink;
    private String descriptions;
    private String countries;
    private String datatypes;
    private String foldernms;
    private String fileNameLink;

    public String getIDs() {
        return IDs;
    }

    public void setIDs(String IDs) {
        this.IDs = IDs;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getFileName() {
        return fileName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPublications() {
        return Publications;
    }

    public void setPublications(String Publications) {
        this.Publications = Publications;
    }

    public String getPublicationsLink() {
        return PublicationsLink;
    }

    public void setPublicationsLink(String PublicationsLink) {
        this.PublicationsLink = PublicationsLink;
    }

    public String getDescriptions() {
        return descriptions;
    }

    public void setDescriptions(String descriptions) {
        this.descriptions = descriptions;
    }

    public String getCountries() {
        return countries;
    }

    public void setCountries(String countries) {
        this.countries = countries;
    }

    public String getDatatypes() {
        return datatypes;
    }

    public void setDatatypes(String datatypes) {
        this.datatypes = datatypes;
    }

    public String getFoldernms() {
        return foldernms;
    }

    public void setFoldernms(String foldernms) {
        this.foldernms = foldernms;
    }

    public String getFileNameLink() {
        return fileNameLink;
    }

    public void setFileNameLink(String fileNameLink) {
        this.fileNameLink = fileNameLink;
    }

    //get streamed file in getter method to avoid closed stream exception error
    public StreamedContent getFileNameAContent() throws IOException {
        StreamedContent ct = null;
        if (!fileNameLink.equals("")) {
            //System.out.println(" --- fileNameLink --- >" + fileNameLink);
            ct = DataUtils.getDownloadFile(fileNameLink);
        }
        return ct;
    }

    public CovidBean(String fileName, String fileNameLink, String Publications,
            String PublicationsLink, String descriptions, String countries,
            String datatypes, String foldernms, String Ids, String platform) {
        this.IDs = Ids;
        this.fileName = fileName;
        this.fileNameLink = fileNameLink;
        this.Publications = Publications;
        this.countries = countries;
        this.PublicationsLink = PublicationsLink;
        this.descriptions = descriptions;
        this.datatypes = datatypes;
        this.foldernms = foldernms;
        this.platform = platform;
    }

}
