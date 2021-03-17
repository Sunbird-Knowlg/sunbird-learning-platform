package org.sunbird.qrimage.request;

import java.util.List;

public class QRImageRequest {

    private List<String> data;
    private String text;
    private String fileName;
    private QRImageConfig config;
    private String tempFileLocation;

    public QRImageRequest(){}

    public QRImageRequest(String tempFileLocation) {
        this.tempFileLocation = tempFileLocation;
    }

    public List<String> getData() {
        return data;
    }

    public void setData(List<String> data) {
        this.data = data;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public QRImageConfig getConfig() {
        return config;
    }

    public void setConfig(QRImageConfig config) {
        this.config = config;
    }

    public String getTempFileLocation() {
        return this.tempFileLocation;
    }
}
