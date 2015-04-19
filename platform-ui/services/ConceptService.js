/*
 * Copyright (c) 2013-2014 Canopus Consulting. All rights reserved.
 *
 * This code is intellectual property of Canopus Consulting. The intellectual and technical
 * concepts contained herein may be covered by patents, patents in process, and are protected
 * by trade secret or copyright law. Any unauthorized use of this code without prior approval
 * from Canopus Consulting is prohibited.
 */

/**
 * Concept Service - Invoke MW API's, transform data for UI and viceversa
 *
 * @author Santhosh
 */
var async = require('async')
	, mwService = require('../commons/MWServiceProvider')
	, util = require('../commons/Util');

exports.getConcept = function(id, cb) {
	var url = urlConstants.GET_CONCEPT.replace(':id', id);
	mwService.getCall(url, {}, function(err, data) {
		cb(null, data.result.concept);
	});
}

exports.updateConcept = function(data, cb) {
	var args = {
		//TODO: Transform the data appropriately
	}
	mwService.getCall(urlConstants.UPDATE_CONCEPT, args, function(err, data) {
		cb(null, data);
	});
}

exports.createConcept = function(data, cb) {
	var args = {
		//TODO: Transform the data appropriately
	}
	mwService.postCall(urlConstants.SAVE_CONCEPT, args, function(err, data) {
		cb(null, data);
	});
}