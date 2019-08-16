package org.ekstep.job.samza.util;

import com.datastax.driver.core.Session;
import org.ekstep.cassandra.connector.util.CassandraConnector;
import org.ekstep.common.Platform;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.jobs.samza.util.JobLogger;
import org.ekstep.learning.util.CloudStore;
import org.ekstep.qrimage.generator.QRImageGenerator;
import org.ekstep.qrimage.request.QRImageRequest;

import java.io.File;
import java.util.Arrays;

public class QRImageUtil {

    private static QRImageRequest qrImageRequest = new QRImageRequest();
    private static JobLogger LOGGER = new JobLogger(QRImageUtil.class);
    private static String DIAL_BASE_URL = Platform.config.hasPath("dial.base.url") ?
           Platform.config.getString("dial.base.url"):"https://dev.sunbirded.org/dial/";

    public static String getQRImageUrl(Node node, String dial, String channel) {
        String[] urlArray = null;
        try {
            //create a qr image
            String dialUrl = DIAL_BASE_URL + dial;
            qrImageRequest.setFileName("0_" + dial);
            qrImageRequest.setText(dial);
            qrImageRequest.setData(Arrays.asList(dialUrl));
            File file = QRImageGenerator.generateQRImage(qrImageRequest);
            //upload the qr image and return the url.
            if (null != file) {
                urlArray = CloudStore.uploadFile(channel, file, false);
            } else {
                LOGGER.info("Got Null Dial Image File Object for " + node.getIdentifier() + " |So Skipping upload of dial image.");
            }
            return (null != urlArray) ? urlArray[0] : null;
        } catch (Exception e) {
            LOGGER.info("Exception Occurred While generating dial image for " + node.getIdentifier() + " | Exception is: " + e);
        }
        return null;
    }

    public static void createQRImageRecord(String channel, String dial, String imageUrl) {
        String fileName = "0_" + dial;
        String query = "insert into dialcodes.dialcode_images(filename, channel, dialcode, publisher, status, url)" +
                "values ('" + fileName + "', '" + channel + "', '" + dial + "', null, 2, '" + imageUrl + "')";
        Session session = CassandraConnector.getSession("sunbird");
        session.execute(query);
    }
}
