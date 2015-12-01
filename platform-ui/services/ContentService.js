
/**
 * Content Service - Invoke MW API's, transform data for UI and viceversa
 *
 * @author Mohammad Azharuddin
 */
var async = require('async')
	, mwService = require('../commons/MWServiceProvider')
	, util = require('../commons/Util')
	, urlConstants = require('../commons/URLConstants')
	, _ = require('underscore');

exports.getContentDefinition = function(cb, taxonomyId, contentType) {
	var args = {
		path: {
			id: taxonomyId,
      contentType: contentType
		}
	}
	mwService.getCall(urlConstants.GET_CONTENT_TAXONOMY_DEFS, args, function(err, data) {
		if(err) {
			cb(err);
		} else {
			cb(null, data.result.definition_node);
		}
	});
}

exports.getContents = function(cb, contentType, taxonomyId, offset, limit) {
	var args = {
		path: {type: contentType,
					tid: taxonomyId},
		data: {
			request: {
			}
		}
	}
	mwService.postCall(urlConstants.GET_CONTENTS, args, function(err, data) {
		if(err) {
			cb(err);
		} else {
			var contents = data.result.content;
			var result = {};
			result.contents = contents;
			cb(null, result);
		}
	});
}

exports.getContent = function(cb, contentId, taxonomyId, contentType) {
	// TODO: Since the metadata is not coming from middleware as of now we are using getGame API code and commenting the getContent API Code
  // var args = {
  //   path: {id: contentId},
  //   parameters: {taxonomyId: taxonomyId,
  //               type: contentType}
  // }
  // mwService.getCall(urlConstants.GET_CONTENT, args, function(err, results) {
	// 	if(err) {
	// 		cb(err);
	// 	} else {
	// 		var content = results.result.content;
	// 		content.relatedConcepts = util.getRelatedObjects(content, 'Concept');
	// 		content.relatedGames = util.getRelatedObjects(content, 'Game');
	// 		console.log('ContentService :: getContent() -- Final Content Object - ', content.metadata);
	// 		cb(null, content);
	// 	}
	// });
	// console.log('ContentService :: getContent() -- IN - ');
		async.parallel({
			game: function(callback) {
				var args = {
					path: {id: contentId},
					parameters: {taxonomyId: taxonomyId}
				}
				mwService.getCall(urlConstants.GET_GAME, args, callback);
			},
			auditHistory: function(callback) {
				var args = {
					path: {id: contentId},
					parameters: {taxonomyId: taxonomyId}
				}
				mwService.getCall(urlConstants.AUDIT_HISTORY, args, callback);
			},
			comments: function(callback) {
				var args = {
					path: {id: contentId},
					parameters: {taxonomyId: taxonomyId}
				}
				mwService.getCall(urlConstants.GET_COMMENTS, args, callback);
			}
		}, function(err, results) {
			if(err) {
				cb(err);
			} else {
				var game = results.game.result.learning_object;
				game.relatedConcepts = util.getRelatedObjects(game, 'Concept');
				game.relatedGames = util.getRelatedObjects(game, 'Game');
				game.parent = util.getParent(game);
				game.auditHistory = results.auditHistory.result.audit_records;
				game.comments = results.comments.result.comments;
				cb(null, game);
			}
		});
}

exports.updateContent = function(data, cb) {
	var args = {
    path: {id: data.identifier,tid: data.taxonomyId,
		type: data.selectedContentType},
		data: {
		  "request": {
		    "content": {
		      "metadata": data.metadata // Change to data.propertires once middleware allow partial Update for Update Content API
		    }
		  }
		}
  }
	if (data.tags && data.tags.length > 0) {
		args.data.request.content.tags = [];
		_.each(data.tags, function (tag) {
			args.data.request.content.tags.push(tag);
		})
	}
	// if (data.outRelations && data.outRelations.length > 0) {
	// 	_.each(data.outRelations, function (relation) {
	// 		args.data.request.content.outRelations.push(relation);
	// 	})
	// }
	mwService.patchCall(urlConstants.UPDATE_CONTENT, args, function(err, data) {
		if(err) {
			cb(err);
		} else if(util.validateMWResponse(data, cb)) {
			cb(null, 'OK');
		}
	});
}

exports.createContent = function(data, cb) {
	var args = {
    path: {tid: data.taxonomyId,
          contentType: data.contentType},
		data: {
		  "request": {
		    "content": {
					"identifier": data.code,
		      "metadata": {
						"name": data.name,
					  "code": data.code,
					  "appIcon": data.appIcon,
					  "owner": data.owner,
					  "body": data.body,
						"status": data.status
					},
		      "outRelations": [],
		      "tags": []
		    }
		  }
		}
	}
	if (data.outRelations && data.outRelations.length > 0) {
		_.each(data.outRelations, function (relation) {
			args.data.outRelations.push(relation);
		})
	}console.log(args.data.request.content.metadata);
	mwService.postCall(urlConstants.SAVE_CONTENT, args, function(err, data) {console.log(data);
		if(err) {
			cb(err);
		} else if(util.validateMWResponse(data, cb)) {
			cb(null, 'OK');
		}
	});
}
