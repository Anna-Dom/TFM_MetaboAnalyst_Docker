/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package metaboanalyst.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.beans.ConstructorProperties;
import java.io.Serializable;

/**
 *
 * @author Jeff
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class User implements Serializable {

    private String name,  homeDir,  relativeDir;

    public User() {
    }
    @JsonCreator
    @ConstructorProperties({"name", "homeDir", "relativeDir"})
    public User(String name, String homeDir, String relativeDir) {
        this.name = name;
        this.homeDir = homeDir;
        this.relativeDir = relativeDir;
    }



    public String getRelativeDir() {
        return relativeDir;
    }

    public void setRelativeDir(String relativeDir) {
        this.relativeDir = relativeDir;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHomeDir() {
        return homeDir;
    }

    public void setHomeDir(String dir) {
        this.homeDir = dir;
    }
}
