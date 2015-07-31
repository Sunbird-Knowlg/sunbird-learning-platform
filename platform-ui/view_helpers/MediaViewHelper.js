var async = require('async')
	, service = require('../services/MediaService')
	, util = require('../commons/Util');

exports.createMedia = function(req, res) {
	service.createMedia(req.body, util.responseCB(res));
}