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
	exports.sendJSONFileResponse('fixtures/' + fileName, cb);
}

exports.sendJSONFileResponse = function(fileName, cb) {
	fs.readFile(fileName, 'utf8', function (err, data) {
  		if (err) {
  			cb(err);
  		} else {
  			var obj = JSON.parse(data);
  			cb(null, obj);
  		}
	});
}

exports.validateMWResponse = function(response, cb) {
	var statusObj = response.params, errors = [], valid = true;
	if(statusObj.status == 'failed') {
		valid = false;
		if(!_.isEmpty(response.result.messages)) {
			errors = response.result.messages;
		} else {
			errors.push(statusObj.errmsg);
		}
		cb(errors);
	}
	return valid;
}

exports.getRelatedObjects = function(object, objectType) {
	
	var	inRelObjects = _.map(_.reject(_.where(object.inRelations, {startNodeObjectType: objectType}), function(r) {return r.relationType == "isParentOf";}), function(obj) {
			return { id: obj.startNodeId, name: obj.startNodeName};
	});
	var outRelObjects = _.map(_.where(object.outRelations, {endNodeObjectType: objectType}), function(obj) {
		return { id: obj.endNodeId, name: obj.endNodeName};
	});
	return _.union(inRelObjects, outRelObjects);
}

exports.getParent = function(object) {
	var parentRelObject = _.find(object.inRelations, function(relation) {
		return relation.relationType == "isParentOf";
	});
	if(parentRelObject) 
		return {id: parentRelObject.startNodeId, name: parentRelObject.startNodeName};
	else
		return null;
}