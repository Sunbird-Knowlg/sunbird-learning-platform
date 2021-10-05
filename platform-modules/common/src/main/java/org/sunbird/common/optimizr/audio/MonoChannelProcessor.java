/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sunbird.common.optimizr.audio;

import java.io.File;

import org.sunbird.common.optimizr.FileUtils;

import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;

/**
 *
 * @author feroz
 */
public class MonoChannelProcessor extends AudioProcessor {

    @Override
    public File process(File file) {
        
        String inputF = file.getAbsolutePath();
        String outputF = FileUtils.getOutputFileName(file);
        
        try {
            FFmpeg ffmpeg = new FFmpeg("/usr/local/bin/ffmpeg");
            FFprobe ffprobe = new FFprobe("/usr/local/bin/ffprobe");

            FFmpegBuilder builder = new FFmpegBuilder()
                    .setInput(inputF) // Filename, or a FFmpegProbeResult
                    .overrideOutputFiles(true) // Override the output if it exists

                    .addOutput(outputF) // Filename for the destination
                    .setFormat("mp3") // Format is inferred from filename, or can be set

                    .setAudioChannels(1) // Mono audio
                    .setAudioSampleRate(22050) // at 48KHz
                    .setAudioBitRate(16384) // at 32 kbit/s

                    .done();

            FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);

            // Run a one-pass encode
            executor.createJob(builder).run();
            
            // Remove the original file and rename output file.
            FileUtils.replace(file, new File(outputF));
            return file;
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        return null;
    }
}
