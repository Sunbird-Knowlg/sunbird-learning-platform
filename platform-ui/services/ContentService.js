
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

exports.getContents = function(cb, contentType, offset, limit) {
	var args = {
		path: {type: contentType},
		data: {
			request: {
			}
		}
	}
	mwService.postCall(urlConstants.GET_CONTENTS, args, function(err, data) {
		if(err) {
			cb(err);
		} else {
			// console.log('ContentService :: getContents() -- IN - ', data);
			var contents = data.result.content;
			var result = {};
			result.contents = contents;
			cb(null, result);
		}
	});
}

exports.getContent = function(cb, contentId, taxonomyId, contentType) {
  var args = {
    path: {id: contentId},
    parameters: {taxonomyId: taxonomyId,
                type: contentType}
  }
  mwService.getCall(urlConstants.GET_CONTENT, args, function(err, results) {
		if(err) {
			cb(err);
		} else {
			var content = results.result.content;
			content.relatedConcepts = util.getRelatedObjects(content, 'Concept');
			content.relatedGames = util.getRelatedObjects(content, 'Game');
			cb(null, content);
		}
	});
}

exports.updateContent = function(data, cb) {
	var args = {
    path: {id: data.identifier,tid: data.taxonomyId,
		type: 'Story'},
		data: {
		  "request": {
		    "content": {
		      "metadata": data.properties,
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
	}
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
    path: {taxonomyId: data.taxonomyId,
          type: data.contentType},
		data: {
		  "request": {
		    "worksheet": {
		      "metadata": {
		        "name": data.name,
		        "description": data.description,
		        "code": data.code,
		        "body": data.body,
		        "owner": data.owner
		      },
		      "outRelations": [],
		      "tags": []
		    }
		  }
		}
	}
	mwService.patchCall(urlConstants.CREATE_CONTENT, args, function(err, data) {
		if(err) {
			cb(err);
		} else if(util.validateMWResponse(data, cb)) {
			cb(null, 'OK');
		}
	});
}
