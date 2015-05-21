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
			var args = {
				path: {id: id},
				parameters: {taxonomyId: tid}
			}
			mwService.getCall(urlConstants.AUDIT_HISTORY, args, callback);
		},
		comments: function(callback) {
			var args = {
				path: {id: id},
				parameters: {taxonomyId: tid}
			}
			mwService.getCall(urlConstants.GET_COMMENTS, args, callback);
		}
	}, function(err, results) {
		if(err) {
			cb(err);
		} else {
			var concept = results.concept.result.concept;
			concept.relatedConcepts = util.getRelatedObjects(concept, 'Concept');
			concept.relatedGames = util.getRelatedObjects(concept, 'Game');
			concept.relations = util.getRelations(concept);
			concept.auditHistory = results.auditHistory.result.audit_records;
			concept.comments = results.comments.result.comments;
			cb(null, concept);
		}
	});
}

exports.updateConcept = function(data, cb) {
	var args = {
		path: {id: data.identifier, tid: data.taxonomyId},
		data: {
			request: {
				concept: {
					identifier: data.identifier,
	        		objectType: "Concept",
	        		metadata: data.properties,
	        		tags: data.tags
				},
				metadata_definitions: []
			},
			COMMENT: data.comment
		}
	}
	if(data.newMetadata && data.newMetadata.length > 0) {
		_.each(data.newMetadata, function(prop) {
			args.data.request.metadata_definitions.push(_.omit(prop, 'error'));
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
				concept: {
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
			cb(null, data.result.node_id);
		}
	});
}
