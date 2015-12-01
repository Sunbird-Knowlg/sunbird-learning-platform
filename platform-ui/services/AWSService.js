/*
 * Copyright (c) 2013-2014 Canopus Consulting. All rights reserved.
 *
 * This code is intellectual property of Canopus Consulting. The intellectual and technical
 * concepts contained herein may be covered by patents, patents in process, and are protected
 * by trade secret or copyright law. Any unauthorized use of this code without prior approval
 * from Canopus Consulting is prohibited.
 */

/**
 * AWS Service - Service methods to work with AWS Service
 * Contains methods to upload a file to S3 and delete a file from S3
 *
 * @author rayulu
 */
var AWS = require('aws-sdk'),
    fs = require('fs'),
    crypto = require("crypto"),
    util = require('../commons/Util');

AWS.config.region = appConfig.AWS_REGION;
AWS.config.update({ accessKeyId: appConfig.AWS_ACCESSKEYID, secretAccessKey: appConfig.AWS_SECRETACCESSKEY });

exports.uploadFile = function(bucketName, folderPath , fileName, file, cb) {
	var fileStream = fs.createReadStream(file.path);
	var keyVal = folderPath + '/' + fileName;
  var hex = "";
  var digest = crypto.createHash("sha1");
  fileStream.on("data", function(d) {digest.update(d);});
  fileStream.on("end", function() {
    hex = digest.digest("hex");
  });
	fileStream.on('error', function (err) {
	  if (err)
	  	throw err;
	});
	fileStream.on('open', function () {
	  var s3 = new AWS.S3();
	  s3.putObject({
	    Bucket: bucketName,
	    Key: keyVal,
	    Body: fileStream,
	    ACL: 'public-read',
	    ContentType: file.type
	  }, function (err , data) {
	    if (err) {
	    	cb(err);
	    } else {
	    	var objectURL = 'https://s3-' + appConfig.AWS_REGION + '.amazonaws.com/' + bucketName + '/' + keyVal;
	    	var resp = {url: objectURL, digest: hex};
	    	cb(null, resp);
	    }
	  });
	});
}

exports.deleteFile = function(bucketName, folderPath , fileName, cb){
	var s3 = new AWS.S3();
	var keyId = folderPath + '/' + fileName;
	s3.deleteObjects({
    Bucket: bucketName,
    Delete: {
        Objects: [
             { Key: keyId }
        ]
    }
	}, function(err, data) {
	    if (err)
	      cb(err);
	    cb(null, data)
	});
}
