/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sunbird.common.optimizr;

import java.text.DecimalFormat;

import org.sunbird.telemetry.logger.TelemetryManager;

/**
 *
 * @author feroz
 */
public class Statistics {

    private static DecimalFormat decimalFormat = new DecimalFormat("#0.00");
    private int audioAssets = 0;
    private int videoAssets = 0;
    private int imageAssets = 0;
    private long audioSizeRaw = 0;
    private long videoSizeRaw = 0;
    private long imageSizeRaw = 0;
    private long audioSizeRed = 0;
    private long videoSizeRed = 0;
    private long imageSizeRed = 0;
    private long rawSize = 0;
    private long redSize = 0;
    private long begin = 0;
    private long end = 0;

    public void print() {
        System.out.println("---- Summary ----");
        TelemetryManager.log("    Compressed Zip: before - " + toMB(rawSize) + ", after - " + toMB(redSize) + " %n");
        TelemetryManager.log("    Audio Files: " +audioAssets + ", before - " + toMB(audioSizeRaw) + ", after - " + toMB(audioSizeRed) + ", %n");
        TelemetryManager.log("    Audio Files: " +imageAssets + ", before - " + toMB(imageSizeRaw) + ", after - " + toMB(imageSizeRed) + ", %n");
        TelemetryManager.log("    Video Files: " +videoAssets + ", before - " + toMB(videoSizeRaw) + ", after - " + toMB(videoSizeRed) + ", %n");
        TelemetryManager.log("    Optimized in" + (end - begin) + " ms %n");
        System.out.printf("    Optimization Ratio: %.2f%% of original %n", (redSize * 100.0 / rawSize));
        System.out.printf("    Compressed Zip: before - %s, after - %s %n", toMB(rawSize), toMB(redSize));
        System.out.printf("    Audio Files: %d, before - %s, after - %s, %n", audioAssets, toMB(audioSizeRaw), toMB(audioSizeRed));
        System.out.printf("    Image Files: %d, before - %s, after - %s, %n", imageAssets, toMB(imageSizeRaw), toMB(imageSizeRed));
        System.out.printf("    Video Files: %d, before - %s, after - %s, %n", videoAssets, toMB(videoSizeRaw), toMB(videoSizeRed));
        System.out.printf("    Optimized in %d ms %n", (end - begin));
    }

    public void start(long rawSize) {
        begin = System.currentTimeMillis();
        this.rawSize = rawSize;
    }

    public void end(long redSize) {
        end = System.currentTimeMillis();
        this.redSize = redSize;
    }

    public void update(FileType type, long rawSize, long reducedSize) {
        switch (type) {
            case Audio: {
                audioAssets++;
                audioSizeRaw += rawSize;
                audioSizeRed += reducedSize;
                break;
            }
            case Video: {
                videoAssets++;
                videoSizeRaw += rawSize;
                videoSizeRed += reducedSize;
                break;
            }
            case Image: {
                imageAssets++;
                imageSizeRaw += rawSize;
                imageSizeRed += reducedSize;
                break;
            }
        }
    }

    private String toMB(long bytes) {
        double kilobytes = (bytes / 1024);
        double megabytes = (kilobytes / 1024);
        return decimalFormat.format(megabytes) + " MB";
    }
}
