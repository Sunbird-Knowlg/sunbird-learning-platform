package org.ekstep.ilimi.analytics.util

import org.jets3t.service.security.AWSCredentials
import org.ekstep.ilimi.analytics.conf.AppConf
import org.jets3t.service.impl.rest.httpclient.RestS3Service
import org.jets3t.service.model.S3Object
import java.nio.file.Files
import java.io.File
import org.jets3t.service.acl.AccessControlList
import org.jets3t.service.acl.GroupGrantee
import org.jets3t.service.acl.Permission
import org.jets3t.service.acl.GranteeInterface

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

    def uploadPublic(bucketName: String, filePath: String, key: String) {

        val bucketAcl = s3Service.getBucketAcl(bucketName);
        val acl = new AccessControlList();
        acl.setOwner(bucketAcl.getOwner);
        acl.grantPermission(GroupGrantee.ALL_USERS, Permission.PERMISSION_READ);
        Console.println("### Uploading file to S3. Bucket - " + bucketName + " | FilePath - " + filePath + " | Key - " + key + " ###")
        val s3Object = new S3Object(new File(filePath));
        s3Object.setKey(key)
        s3Object.setAcl(acl);
        val fileObj = s3Service.putObject(bucketName, s3Object);
        Console.println("ETag - " + fileObj.getETag);
    }

    def getMetadata(bucketName: String, key: String) {
        val bucket = s3Service.getBucket(bucketName);
        val s3Object = s3Service.getObjectDetails(bucket, key);
        Console.println("ContentLength - " + s3Object.getContentLength);
        Console.println("ContentLength - " + s3Object.getLastModifiedDate);
    }

    def getAllKeys(bucketName: String, prefix: String) : Array[String] = {
        val bucket = s3Service.getBucket(bucketName);
        val s3Objects = s3Service.listObjects(bucket, prefix, null);
        //s3Objects.foreach { x => Console.println(" " + x.getKey() + " (" + x.getContentLength() + " bytes)") }
        s3Objects.map { x => x.getKey }
    }
    
    def main(args: Array[String]): Unit = {
        getAllKeys("ep-production-backup", "logs/telemetry/*.gz");
    }

}