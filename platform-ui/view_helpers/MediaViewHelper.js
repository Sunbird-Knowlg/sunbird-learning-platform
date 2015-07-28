var async = require('async')
	, service = require('../services/MediaService')
	, util = require('../commons/Util');

exports.createMedia = function(req, res) {
	console.log("inside Media View Helper");
	service.createMedia(req.body, util.responseCB(res));
}