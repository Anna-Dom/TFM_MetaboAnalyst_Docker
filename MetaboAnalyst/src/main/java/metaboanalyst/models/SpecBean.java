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
 * @author jianguox
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SpecBean implements Serializable {

    private String name;
    private String format;
    private String size;

    private String group;
    private String intensity;
    private boolean include = true;
    private boolean disabled = false;

    @JsonCreator
    @ConstructorProperties({"name", "format", "size", "group", "intensity", "include", "disabled"})
    public SpecBean(String name, String format, String size, String group, String intensity, boolean include, boolean disabled){
        this.name = name;
        this.format = format;
        this.size = size;
        this.group = group;
        this.intensity = intensity;
        this.include = include;
        this.disabled = disabled;
    }
    
    
    public String getIntensity() {
        return intensity;
    }

    public void setIntensity(String intensity) {
        this.intensity = intensity;
    }
        
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public boolean isInclude() {
        return include;
    }

    public void setInclude(boolean include) {
        this.include = include;
    }
    
    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }
    
}
