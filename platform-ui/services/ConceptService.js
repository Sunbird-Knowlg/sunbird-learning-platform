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
	, util = require('../commons/Util')
	, urlConstants = require('../commons/URLConstants')
	, _ = require('underscore');

exports.getConcept = function(id, tid, cb) {
	async.parallel({
		concept: function(callback) {
			var args = {
				path: {id: id},
				parameters: {taxonomyId: tid}
			}
			mwService.getCall(urlConstants.GET_CONCEPT, args, callback);
		},
		auditHistory: function(callback) {
			callback(null, []);
		},
		comments: function(callback) {
			callback(null, []);
		}
	}, function(err, results) {
		if(err) {
			cb(err);
		} else {
			var concept = results.concept.result.CONCEPT;
			concept.auditHistory = results.auditHistory;
			concept.comments = results.comments;
			cb(null, concept);
		}
	});
}

exports.updateConcept = function(data, cb) {
	var args = {
		path: {id: data.identifier, tid: data.taxonomyId},
		data: {
			request: {
				CONCEPT: {
					identifier: data.identifier,
	        		objectType: "Concept",
	        		metadata: data.properties,
	        		tags: data.tags
				},
				METADATA_DEFINITIONS: []
			},
			COMMENT: data.comment
		}
	}
	if(data.newMetadata && data.newMetadata.length > 0) {
		_.each(data.newMetadata, function(prop) {
			args.data.request.METADATA_DEFINITIONS.push(_.omit(prop, 'error'));
		});
	}
	mwService.patchCall(urlConstants.UPDATE_CONCEPT, args, function(err, data) {
		if(err) {
			cb(err);
		} else if(util.validateMWResponse(data, cb)) {
			cb(null, 'OK');
		}
	});
}

exports.createConcept = function(data, cb) {
	var args = {
		path: {tid: data.taxonomyId},
		data: {
			request: {
				CONCEPT: {
	        		objectType: "Concept",
	        		metadata: {
	 					"name": data.name,
	 					"code": data.code,
	 					"description": data.description
					},
	        		inRelations: [{
	        			startNodeId: data.parent ? data.parent.id : data.taxonomyId,
	        			relationType: 'isParentOf'
	        		}],
	        		tags: [data.objectType.id == 'concept' ? 'Broad Concept' : (data.objectType.id == 'subConcept' ? 'Sub Concept' : 'Micro Concept')]
				}
			},
			COMMENT: data.comment
		}
	}
	mwService.postCall(urlConstants.SAVE_CONCEPT, args, function(err, data) {
		if(err) {
			cb(err);
		} else if(util.validateMWResponse(data, cb)) {
			cb(null, data.result.NODE_ID);
		}
	});
}