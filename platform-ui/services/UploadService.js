var AWS = require('aws-sdk'),
    fs = require('fs'),
    util = require('../commons/Util');

AWS.config.region = 'ap-southeast-1';
AWS.config.update({ accessKeyId: appConfig.AWS_ACCESSKEYID, secretAccessKey: appConfig.AWS_SECRETACCESSKEY });

exports.upload = function(folderPath , file, bucketName, cb) {	
	var fileStream = fs.createReadStream(file);
	var keyVal = folderPath + '/' + file;
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
	    ACL: 'public-read'
	  }, function (err , data) {
	    if (err) { 
	    	cb(err);
	    } else {
	    	cb(null,data)
	    }
	  });
	});
}

exports.deleteUpload = function(folderPath , file, bucketName, cb){
	var s3 = new AWS.S3();
	var keyId = folderPath + '/' + file;
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