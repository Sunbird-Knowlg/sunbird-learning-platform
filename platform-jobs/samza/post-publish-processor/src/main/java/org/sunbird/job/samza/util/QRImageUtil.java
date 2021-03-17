package org.sunbird.job.samza.util;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.Clause;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.cassandra.connector.util.CassandraConnector;
import org.sunbird.cassandra.connector.util.CassandraConnectorStoreParam;
import org.sunbird.common.Platform;
import org.sunbird.common.exception.ResourceNotFoundException;
import org.sunbird.common.exception.ServerException;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.jobs.samza.util.JobLogger;
import org.sunbird.learning.util.CloudStore;
import org.sunbird.qrimage.generator.QRImageGenerator;
import org.sunbird.qrimage.request.QRImageRequest;

import java.io.File;
import java.util.Arrays;

public class QRImageUtil {

    private static JobLogger LOGGER = new JobLogger(QRImageUtil.class);
    private static String DIAL_BASE_URL = Platform.config.hasPath("dial.base.url") ?
           Platform.config.getString("dial.base.url"):"https://dev.sunbirded.org/dial/";
    private static String DIAL_KEYSPACE_NAME = "dialcodes";
    private static String DIAL_TABLE_NAME = "dialcode_images";
    private static String BASE_DIR = Platform.config.hasPath("lp.tempfile.location") ?
            Platform.config.getString("lp.tempfile.location"): "/tmp";
    private static QRImageRequest qrImageRequest = new QRImageRequest(BASE_DIR);

    public static String getQRImageUrl(Node node, String dial, String channel) {
        String[] urlArray = null;;
        File file = null;
        try {
            //create a qr image
            String dialUrl = DIAL_BASE_URL + dial;
            qrImageRequest.setFileName("0_" + dial);
            qrImageRequest.setText(dial);
            qrImageRequest.setData(Arrays.asList(dialUrl));
            file = QRImageGenerator.generateQRImage(qrImageRequest);
            //upload the qr image and return the url.
            if (null != file) {
                urlArray = CloudStore.uploadFile(channel, file, false);
                LOGGER.info("Dial Image File Uploaded for " + node.getIdentifier() + " |Url Array is: " + urlArray);
            } else {
                LOGGER.info("Got Null Dial Image File Object for " + node.getIdentifier() + " |So Skipping upload of dial image.");
            }
            return (null != urlArray) ? urlArray[1] : null;
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("Exception Occurred While generating dial image for " + node.getIdentifier() + " | Exception is: " , e);
        } finally {
            try {
                if (null != file && file.exists())
                    file.delete();
            } catch (Exception ex) {
                LOGGER.error("Exception Occurred While deleting QR Image File from Base Directory : " + BASE_DIR + " | Exception is: ", ex);
            }
        }
        return null;
    }

    public static void createQRImageRecord(String channel, String dial, String imageUrl) {
        String fileName = "0_" + dial;
        String query = "insert into " + DIAL_KEYSPACE_NAME + "." + DIAL_TABLE_NAME + "(filename, channel, dialcode, publisher, status, url, created_on, config)" +
                "values ('" + fileName + "', '" + channel + "', '" + dial + "', null, 2, '" + imageUrl + "',toTimestamp(now()), null)";
        Session session = CassandraConnector.getSession("sunbird");
        session.execute(query);
    }

    public static String getQRImageRecord(String dial) {
        String imageUrl = null;
        if (StringUtils.isBlank(dial)) {
            throw new ServerException(CassandraConnectorStoreParam.ERR_SERVER_ERROR.name(), "Invalid DIAL Code To Read.");
        }
        String fileName = "0_" + dial;
        try {
            Select selectQuery = QueryBuilder.select().column("url").from(DIAL_KEYSPACE_NAME, DIAL_TABLE_NAME);
            Select.Where selectWhere = selectQuery.where();
            Clause clause = QueryBuilder.eq("filename", fileName);
            selectWhere.and(clause);
            ResultSet results = CassandraConnector.getSession("sunbird").execute(selectQuery);
            if (null != results) {
                while (results.iterator().hasNext()) {
                    Row row = results.iterator().next();
                    imageUrl = row.getString("url");
                }
            }
        } catch (Exception e) {
            LOGGER.error("Exception Occurred While Reading QR Image from Cassandra for DIAL : " + dial + " |Exception is: " , e);
            throw new ServerException(CassandraConnectorStoreParam.ERR_SERVER_ERROR.name(), "Exception Occurred While Reading QR Image from Cassandra for DIAL : " + dial);
        }

        if (StringUtils.isNotBlank(imageUrl))
            return imageUrl;
        else
            throw new ResourceNotFoundException("ERR_GET_QR_IMAGE_URL", "QR Image Url Not Found For DIAL Code : " + dial);

    }
}
