package org.ekstep.jobs.samza.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.NotFoundException;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.apache.samza.config.Config;
import org.ekstep.jobs.samza.model.QRCodeGenerationRequest;

import javax.imageio.ImageIO;
import java.awt.FontMetrics;
import java.awt.Font;
import java.awt.Color;
import java.awt.RenderingHints;
import java.awt.Graphics2D;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class QRCodeImageGeneratorUtil {

    static QRCodeWriter qrCodeWriter = new QRCodeWriter();

    public static List<File> createQRImages(QRCodeGenerationRequest qrGenRequest, Config appConfig, String container, String path) throws WriterException, IOException, NotFoundException {

        List<File> fileList = new ArrayList<File>();

        List<String> dataList = qrGenRequest.getData();
        List<String> textList = qrGenRequest.getText();
        List<String> fileNameList = qrGenRequest.getFileName();

        String errorCorrectionLevel = qrGenRequest.getErrorCorrectionLevel();
        int pixelsPerBlock = qrGenRequest.getPixelsPerBlock();
        int qrMargin = qrGenRequest.getQrCodeMargin();
        String fontName = qrGenRequest.getTextFontName();
        int fontSize = qrGenRequest.getTextFontSize();
        double tracking = qrGenRequest.getTextCharacterSpacing();
        String imageFormat = qrGenRequest.getFileFormat();
        String colorModel = qrGenRequest.getColorModel();
        int borderSize = qrGenRequest.getImageBorderSize();

        for (int i = 0; i < dataList.size(); i++) {
            String data = dataList.get(i);
            String text = textList.get(i);
            String fileName = fileNameList.get(i);

            BufferedImage qrImage = generateBaseImage(data, errorCorrectionLevel, pixelsPerBlock, qrMargin, colorModel);

            if (null != text || "" != text) {
                BufferedImage textImage = getTextImage(text, fontName, fontSize, tracking, colorModel);
                qrImage = addTextToBaseImage(qrImage, textImage, colorModel, qrMargin, pixelsPerBlock);
            }

            if (borderSize > 0) {
                drawBorder(qrImage, borderSize);
            }

            File finalImageFile = new File(fileName + "." + imageFormat);
            ImageIO.write(qrImage, imageFormat, finalImageFile);
            fileList.add(finalImageFile);

            try {
                String imageDownloadUrl = CloudStorageUtil.uploadFile(appConfig, container, path, finalImageFile, true, false);
                QRCodeCassandraConnector.updateDownloadUrl(fileName, imageDownloadUrl);
            } catch(Exception e) {

            }
        }

        return fileList;

    }

    static BufferedImage addTextToBaseImage(BufferedImage qrImage, BufferedImage textImage, String colorModel, int qrMargin, int pixelsPerBlock) throws NotFoundException {
        BufferedImageLuminanceSource qrSource = new BufferedImageLuminanceSource(qrImage);
        HybridBinarizer qrBinarizer = new HybridBinarizer(qrSource);
        BitMatrix qrBits = qrBinarizer.getBlackMatrix();

        BufferedImageLuminanceSource textSource = new BufferedImageLuminanceSource(textImage);
        HybridBinarizer textBinarizer = new HybridBinarizer(textSource);
        BitMatrix textBits = textBinarizer.getBlackMatrix();

        if (qrBits.getWidth() > textBits.getWidth()) {
            BitMatrix tempTextMatrix = new BitMatrix(qrBits.getWidth(), textBits.getHeight());
            copyMatrixDataToBiggerMatrix(textBits, tempTextMatrix);
            textBits = tempTextMatrix;
        } else if (qrBits.getWidth() < textBits.getWidth()) {
            BitMatrix tempQrMatrix = new BitMatrix(textBits.getWidth(), qrBits.getHeight());
            copyMatrixDataToBiggerMatrix(qrBits, tempQrMatrix);
            qrBits = tempQrMatrix;
        }

        BitMatrix mergedMatrix = mergeMatricesOfSameWidth(qrBits, textBits, qrMargin, pixelsPerBlock);
        return getImage(mergedMatrix, colorModel);
    }

    static BufferedImage generateBaseImage(String data, String errorCorrectionLevel, int pixelsPerBlock, int qrMargin, String colorModel) throws WriterException {
        Map hintsMap = getHintsMap(errorCorrectionLevel, qrMargin);
        BitMatrix defaultBitMatrix = getDefaultBitMatrix(data, hintsMap);
        BitMatrix largeBitMatrix = getBitMatrix(data, defaultBitMatrix.getWidth() * pixelsPerBlock, defaultBitMatrix.getHeight() * pixelsPerBlock, hintsMap);
        BufferedImage qrImage = getImage(largeBitMatrix, colorModel);
        return qrImage;
    }

    //To remove extra spaces between text and qrcode, margin below qrcode is removed
    static BitMatrix mergeMatricesOfSameWidth(BitMatrix firstMatrix, BitMatrix secondMatrix, int qrMargin, int pixelsPerBlock) {
        int mergedWidth = firstMatrix.getWidth();
        int mergedHeight = firstMatrix.getHeight() + secondMatrix.getHeight();
        BitMatrix mergedMatrix = new BitMatrix(mergedWidth, mergedHeight - pixelsPerBlock * qrMargin);

        for (int x = 0; x < firstMatrix.getWidth(); x++) {
            for (int y = 0; y < firstMatrix.getHeight() - pixelsPerBlock * qrMargin; y++) {
                if (firstMatrix.get(x, y)) {
                    mergedMatrix.set(x, y);
                }
            }
        }
        for (int x = 0; x < secondMatrix.getWidth(); x++) {
            for (int y = 0; y < secondMatrix.getHeight(); y++) {
                if (secondMatrix.get(x, y)) {
                    mergedMatrix.set(x, y + firstMatrix.getHeight() - pixelsPerBlock * qrMargin);
                }
            }
        }
        return mergedMatrix;
    }

    static void copyMatrixDataToBiggerMatrix(BitMatrix fromMatrix, BitMatrix toMatrix) {
        int widthDiff = toMatrix.getWidth() - fromMatrix.getWidth();
        int leftMargin = widthDiff / 2;
        for (int x = 0; x < fromMatrix.getWidth(); x++) {
            for (int y = 0; y < fromMatrix.getHeight(); y++) {
                if (fromMatrix.get(x, y)) {
                    toMatrix.set(x + leftMargin, y);
                }
            }
        }
    }

    static void drawBorder(BufferedImage image, int borderSize) {
        image.createGraphics();
        Graphics2D graphics = (Graphics2D) image.getGraphics();
        graphics.setColor(Color.BLACK);
        for (int i = 0; i < borderSize; i++) {
            graphics.drawRect(i, i, image.getWidth() - 1 - (2 * i), image.getHeight() - 1 - (2 * i));
        }
        graphics.dispose();
    }

    static BufferedImage getImage(BitMatrix bitMatrix, String colorModel) {
        int imageWidth = bitMatrix.getWidth();
        int imageHeight = bitMatrix.getHeight();
        BufferedImage image = new BufferedImage(imageWidth, imageHeight, getImageType(colorModel));
        image.createGraphics();

        Graphics2D graphics = (Graphics2D) image.getGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, imageWidth, imageHeight);

        graphics.setColor(Color.BLACK);

        for (int i = 0; i < imageWidth; i++) {
            for (int j = 0; j < imageHeight; j++) {
                if (bitMatrix.get(i, j)) {
                    graphics.fillRect(i, j, 1, 1);
                }
            }
        }
        graphics.dispose();
        return image;
    }

    static BitMatrix getBitMatrix(String data, int width, int height, Map hintsMap) throws WriterException {
        BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, width, height, hintsMap);
        return bitMatrix;
    }

    static BitMatrix getDefaultBitMatrix(String data, Map hintsMap) throws WriterException {
        BitMatrix defaultBitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, 0, 0, hintsMap);
        return defaultBitMatrix;
    }

    static Map getHintsMap(String errorCorrectionLevel, int qrMargin) {
        Map hintsMap = new HashMap();
        switch (errorCorrectionLevel) {
            case "H":
                hintsMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
                break;
            case "Q":
                hintsMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.Q);
                break;
            case "M":
                hintsMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
                break;
            case "L":
                hintsMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
                break;
        }
        hintsMap.put(EncodeHintType.MARGIN, qrMargin);
        return hintsMap;
    }

    //Sample = 2A42UH , Verdana, 11, 0.1, Grayscale
    static BufferedImage getTextImage(String text, String fontName, int fontSize, double tracking, String colorModel) {

        BufferedImage image = new BufferedImage(1, 1, getImageType(colorModel));

        Font basicFont = new Font(fontName, Font.BOLD, fontSize);
        Map<TextAttribute, Object> attributes = new HashMap<TextAttribute, Object>();
        attributes.put(TextAttribute.TRACKING, tracking);
        Font font = basicFont.deriveFont(attributes);

        Graphics2D graphics2d = image.createGraphics();
        graphics2d.setFont(font);
        FontMetrics fontmetrics = graphics2d.getFontMetrics();
        int width = fontmetrics.stringWidth(text);
        int height = fontmetrics.getHeight();
        graphics2d.dispose();

        image = new BufferedImage(width, height, getImageType(colorModel));
        graphics2d = image.createGraphics();
        graphics2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        graphics2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        graphics2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        graphics2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

        graphics2d.setColor(Color.WHITE);
        graphics2d.fillRect(0, 0, image.getWidth(), image.getHeight());
        graphics2d.setColor(Color.BLACK);

        graphics2d.setFont(font);
        fontmetrics = graphics2d.getFontMetrics();
        graphics2d.drawString(text, 0, fontmetrics.getAscent());
        graphics2d.dispose();

        return image;
    }

    static int getImageType(String colorModel) {
        if (colorModel.equalsIgnoreCase("RGB")) {
            return BufferedImage.TYPE_INT_RGB;
        } else {
            return BufferedImage.TYPE_BYTE_GRAY;
        }
    }
}
