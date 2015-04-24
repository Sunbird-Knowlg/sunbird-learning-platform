/*
 * Copyright (c) 2013-2014 Canopus Consulting. All rights reserved.
 *
 * This code is intellectual property of Canopus Consulting. The intellectual and technical
 * concepts contained herein may be covered by patents, patents in process, and are protected
 * by trade secret or copyright law. Any unauthorized use of this code without prior approval
 * from Canopus Consulting is prohibited.
 */

/**
 * File to contain all utility functions
 *
 * @author Santhosh
 */

var fs = require('fs')
	, _ = require('underscore');

exports.responseCB = function(res) {
	return function(err, data) {
		if(err) {
			res.json({error: true, errorMsg: err});
		} else {
			res.json(data);
		}
	}
}

exports.sendJSONResponse = function(fileName, cb) {
	fs.readFile('fixtures/' + fileName, 'utf8', function (err, data) {
  		if (err) {
  			cb(err);
  		} else {
  			var obj = JSON.parse(data);
  			cb(null, obj);
  		}
	});
}

exports.validateMWResponse = function(response, cb) {
	var statusObj = response.status, errors = [], valid = true;
	if(statusObj.status == 'ERROR') {
		valid = false;
		if(!_.isEmpty(response.result.MESSAGES) && !_.isEmpty(response.result.MESSAGES.valueObjectList)) {
			errors = _.pluck(response.result.MESSAGES.valueObjectList,  'id');
		} else {
			errors.push(statusObj.message);
		}
		cb(errors);
	}
	return valid;
}