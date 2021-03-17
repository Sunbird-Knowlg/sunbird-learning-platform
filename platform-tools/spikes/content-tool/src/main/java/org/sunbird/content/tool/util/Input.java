package org.sunbird.content.tool.util;

public class Input {
    private String id;
    private String name;
    private double pkgVersion;
    private String objectType;
    private String status;

    public Input(String id, String name, double pkgVersion, String objectType, String status) {
        this.id = id;
        this.name = name;
        this.pkgVersion = pkgVersion;
        this.objectType = objectType;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPkgVersion() {
        return pkgVersion;
    }

    public void setPkgVersion(double pkgVersion) {
        this.pkgVersion = pkgVersion;
    }

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return id  + ", " + name  + ", " + pkgVersion
 + ", " +  status;
    }

}
