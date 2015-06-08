package org.ekstep.ilimi.analytics.util

import org.jets3t.service.security.AWSCredentials
import org.ekstep.ilimi.analytics.conf.AppConf
import org.jets3t.service.impl.rest.httpclient.RestS3Service
import org.jets3t.service.model.S3Object
import java.nio.file.Files
import java.io.File

object S3Util {

    private val awsCredentials = new AWSCredentials(AppConf.getConfig("s3_aws_key"), AppConf.getConfig("s3_aws_secret"));
    private val s3Service = new RestS3Service(awsCredentials);

    def upload(bucketName: String, filePath: String, key: String) {
        Console.println("### Uploading file to S3. Bucket - " + bucketName + " | FilePath - " + filePath + " | Key - " + key + " ###")
        val s3Object = new S3Object(new File(filePath));
        s3Object.setKey(key)
        val fileObj = s3Service.putObject(bucketName, s3Object);
        Console.println("ETag - " + fileObj.getETag);
    }

}