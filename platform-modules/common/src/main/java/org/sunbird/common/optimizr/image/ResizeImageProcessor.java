package org.sunbird.common.optimizr.image;
///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//package org.sunbird.ecml.optimizr.image;
//
//import java.io.File;
//import java.util.Iterator;
//import org.sunbird.ecml.optimizr.FileUtils;
//import org.sunbird.ecml.optimizr.ImageProcessor;
//import org.openimaj.image.FImage;
//import org.openimaj.image.ImageUtilities;
//import org.openimaj.image.MBFImage;
//import org.openimaj.image.processing.resize.ResizeProcessor;
//import org.openimaj.image.processing.resize.filters.HanningFilter;
//
///**
// *
// * @author feroz
// */
//public class ResizeImageProcessor extends ImageProcessor {
//
//    @Override
//    public void process(File file) {
//        try {
//            // MBFImage image = ImageUtilities.readMBF(file);
//            //FImage fimage = image.flatten();
//            FImage image = ImageUtilities.readF(file);
//            
////            int index = 0;
////            Iterator<FImage> itr = image.iterator();
////            while (itr.hasNext()) {
////                FImage fimg = itr.next();
////                index++;
////                
////                String bandFileName = file.getName().replaceAll("\\.", "\\.band " + index + "\\.");
////                String bandFileNameFolder = file.getParent();
////                String bandFile = bandFileNameFolder + "/" + bandFileName;
////                
////                ImageUtilities.write(fimg, new File(bandFile));
////                System.out.println(file.getAbsoluteFile() + " - band - " + index);
////            }
//
//            int h = image.getHeight();
//            int w = image.getWidth();
//
//            int maxsize;
//
//            if ((h > 800) || (w > 800)) {
//                // background image
//                maxsize = 960;
//            } else if ((h > 400) || (w > 400)) {
//                // object image
//                maxsize = 960 / 2;
//            } else {
//                // icon image
//                maxsize = 128;
//            }
//
//            ResizeProcessor proc = new ResizeProcessor(0.5f, new HanningFilter());
//            FImage outputImage = image.process(proc);
//
//            // FImage outputImage = ResizeProcessor.resizeMax(fimage, maxsize);
//            String outputFileName = FileUtils.getOutputFileName(file);
//            File output = new File(outputFileName);
//            ImageUtilities.write(outputImage, output);
//
//            FileUtils.replace(file, output);
//
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//    }
//}
