package org.sunbird.common.dto;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * @author santhosh
 * 
 */
public class Property implements Serializable {

    private static final long serialVersionUID = -6502358168521170279L;

    private String propertyName;
    private Object propertyValue;
    private Date dateValue;

    public Property() {

    }

    public Property(String propertyName, Object propertyValue) {
        super();
        this.propertyName = propertyName;
        this.propertyValue = propertyValue;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public Object getPropertyValue() {
        if (null != this.dateValue) {
            return this.dateValue;
        }
        return propertyValue;
    }

    public void setPropertyValue(Object propertyValue) {
        this.propertyValue = propertyValue;
    }

    @Override
    public String toString() {
        return "Property [" + (propertyName != null ? "propertyName=" + propertyName + ", " : "")
                + (propertyValue != null ? "propertyValue=" + propertyValue : "") + "]";
    }

    public Date getDateValue() {
        return dateValue;
    }

    public void setDateValue(Date dateValue) {
        this.dateValue = dateValue;
    }

}
