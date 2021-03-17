package org.sunbird.jobs.samza.model;

import java.util.List;

public class QRCodeGenerationRequest {

    private List<String> data;
    private String errorCorrectionLevel;
    private int pixelsPerBlock;
    private int qrCodeMargin;
    private List<String> text;
    private String textFontName;
    private int textFontSize;
    private double textCharacterSpacing;
    private int imageBorderSize;
    private String colorModel;
    private List<String> fileName;
    private String fileFormat;
    private int qrCodeMarginBottom;
    private int imageMargin;
    private String tempFilePath;

    public String getTempFilePath() { return tempFilePath; }

    public void setTempFilePath(String tempFilePath) { this.tempFilePath = tempFilePath; }

    public int getImageMargin() { return imageMargin; }

    public void setImageMargin(int imageMargin) { this.imageMargin = imageMargin; }

    public int getQrCodeMarginBottom() {
        return qrCodeMarginBottom;
    }

    public void setQrCodeMarginBottom(int qrCodeMarginBottom) {
        this.qrCodeMarginBottom = qrCodeMarginBottom;
    }

    public List<String> getData() {
        return data;
    }

    public void setData(List<String> data) {
        this.data = data;
    }

    public String getErrorCorrectionLevel() {
        return errorCorrectionLevel;
    }

    public void setErrorCorrectionLevel(String errorCorrectionLevel) {
        this.errorCorrectionLevel = errorCorrectionLevel;
    }

    public int getPixelsPerBlock() {
        return pixelsPerBlock;
    }

    public void setPixelsPerBlock(int pixelsPerBlock) {
        this.pixelsPerBlock = pixelsPerBlock;
    }

    public int getQrCodeMargin() {
        return qrCodeMargin;
    }

    public void setQrCodeMargin(int qrCodeMargin) {
        this.qrCodeMargin = qrCodeMargin;
    }

    public List<String> getText() {
        return text;
    }

    public void setText(List<String> text) {
        this.text = text;
    }

    public String getTextFontName() {
        return textFontName;
    }

    public void setTextFontName(String textFontName) {
        this.textFontName = textFontName;
    }

    public int getTextFontSize() {
        return textFontSize;
    }

    public void setTextFontSize(int textFontSize) {
        this.textFontSize = textFontSize;
    }

    public double getTextCharacterSpacing() {
        return textCharacterSpacing;
    }

    public void setTextCharacterSpacing(double textCharacterSpacing) {
        this.textCharacterSpacing = textCharacterSpacing;
    }

    public int getImageBorderSize() {
        return imageBorderSize;
    }

    public void setImageBorderSize(int imageBorderSize) {
        this.imageBorderSize = imageBorderSize;
    }

    public String getColorModel() {
        return colorModel;
    }

    public void setColorModel(String colorModel) {
        this.colorModel = colorModel;
    }

    public List<String> getFileName() {
        return fileName;
    }

    public void setFileName(List<String> fileName) {
        this.fileName = fileName;
    }

    public String getFileFormat() {
        return fileFormat;
    }

    public void setFileFormat(String fileFormat) {
        this.fileFormat = fileFormat;
    }
}
