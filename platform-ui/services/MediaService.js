var async = require('async')
	, mwService = require('../commons/MWServiceProvider')
	, util = require('../commons/Util')
	, urlConstants = require('../commons/URLConstants')
	, _ = require('underscore');

exports.createMedia = function(data, cb) {
  	var args = {
		path: {tid: data.taxonomyId},
		data: {
			request: {
				media: {
	        		objectType: "Media",
	        		metadata: {
	 					"mediaUrl": data.url,
        				"mediaType": data.mediaType,
        				"mimeType": data.mimeType 
					},
	        		inRelations: [{
	        			"startNodeId" : data.gameId,
	        			"relationType" : "associatedTo"
	        		}],
	        		tags: []
				}
			}
		}
	}
	mwService.postCall(urlConstants.SAVE_MEDIA, args, function(err, data) {
		console.log(data);
		if(err) {
			cb(err);
		} else if(util.validateMWResponse(data, cb)) {
			cb(null, data.result.node_id);
		}
	});
}