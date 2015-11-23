/*
 * Copyright (c) 2014-2015 Canopus Consulting. All rights reserved.
 *
 * This code is intellectual property of Canopus Consulting. The intellectual and technical
 * concepts contained herein may be covered by patents, patents in process, and are protected
 * by trade secret or copyright law. Any unauthorized use of this code without prior approval
 * from Canopus Consulting is prohibited.
 */

/**
 * View Helper for Content UX.
 *
 * @author Mohammad Azharuddin
 */
var async = require('async')
	, demoservice = require('../services/ContentServiceFixtures')
	, service = (appConfig.APP_STATUS == 'DEMO' ? require('../services/ContentServiceFixtures') : require('../services/ContentService'))
	, util = require('../commons/Util');

exports.getContentDefinition = function(req, res) {
	service.getContentDefinition(util.responseCB(res), req.params.tid, req.params.contentType);
}

exports.getContents = function(req, res) {
	service.getContents(util.responseCB(res), req.params.contentType, req.params.offset, req.params.limit);
}

exports.getContent = function(req, res) {
	service.getContent(util.responseCB(res), req.params.cid, req.params.tid, req.params.type);
}

exports.createContent = function(req, res) {
	service.createContent(req.body, util.responseCB(res));
}

exports.updateContent = function(req, res) {
	service.updateContent(req.body, util.responseCB(res));
}
