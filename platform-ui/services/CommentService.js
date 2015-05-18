/*
 * Copyright (c) 2013-2014 Canopus Consulting. All rights reserved.
 *
 * This code is intellectual property of Canopus Consulting. The intellectual and technical
 * concepts contained herein may be covered by patents, patents in process, and are protected
 * by trade secret or copyright law. Any unauthorized use of this code without prior approval
 * from Canopus Consulting is prohibited.
 */

/**
 * Comment Service - Invoke MW API's, transform data for UI and viceversa
 *
 * @author Mahesh
 */

var async = require('async')
	, mwService = require('../commons/MWServiceProvider')
	, util = require('../commons/Util')
	, urlConstants = require('../commons/URLConstants')
	, _ = require('underscore');

exports.saveComment = function(data, cb) {
	var args = {
		path: {tid: data.taxonomyId},
		data: {
			request: {
				comment: {
	        		comment: data.comment,
	        		objectId: data.objectId,
	        		threadId: data.threadId,
	        		replyTo: data.replyTo
				}
			}
		}
	}
	mwService.postCall(urlConstants.SAVE_COMMENT, args, function(err, data) {
		if(err) {
			cb(err);
		} else if(util.validateMWResponse(data, cb)) {
			cb(null, data.result.comment);
		}
	});
}

exports.getCommentThread = function(tid, id, threadId, cb) {
	var args = {
		path: {id: id, threadId: threadId},
		parameters: {taxonomyId: tid}
	}
	console.log('args:', args);
	mwService.getCall(urlConstants.GET_COMMENT_THREAD, args, function(err, data) {
		if(err) {
			console.log('Error:', err);
			cb(err);
		} else {
			console.log('Comments:', data);
			cb(null, data.result.comments);
		}
	});
}