package org.sunbird.qrimage.request;

public class QRImageConfig {

    private String fileFormat;
    private String errorCorrectionLevel;
    private int pixelsPerBlock;
    private String colorModel;
    private String textFontName;
    private int textFontSize;
    private double textCharacterSpacing;
    private int qrCodeMargin;
    private int imageBorderSize;
    private int imageMargin;
    private int qrCodeMarginBottom;

    public String getFileFormat() {
        return fileFormat;
    }

    public void setFileFormat(String fileFormat) {
        this.fileFormat = fileFormat;
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

    public String getColorModel() {
        return colorModel;
    }

    public void setColorModel(String colorModel) {
        this.colorModel = colorModel;
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

    public int getQrCodeMargin() {
        return qrCodeMargin;
    }

    public void setQrCodeMargin(int qrCodeMargin) {
        this.qrCodeMargin = qrCodeMargin;
    }

    public int getImageBorderSize() {
        return imageBorderSize;
    }

    public void setImageBorderSize(int imageBorderSize) {
        this.imageBorderSize = imageBorderSize;
    }

    public int getImageMargin() {
        return imageMargin;
    }

    public void setImageMargin(int imageMargin) {
        this.imageMargin = imageMargin;
    }

    public int getQrCodeMarginBottom() {
        return qrCodeMarginBottom;
    }

    public void setQrCodeMarginBottom(int qrCodeMarginBottom) {
        this.qrCodeMarginBottom = qrCodeMarginBottom;
    }
}
