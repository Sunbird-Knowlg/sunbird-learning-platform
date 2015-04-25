/*
 * Copyright (c) 2013-2014 Canopus Consulting. All rights reserved.
 *
 * This code is intellectual property of Canopus Consulting. The intellectual and technical
 * concepts contained herein may be covered by patents, patents in process, and are protected
 * by trade secret or copyright law. Any unauthorized use of this code without prior approval
 * from Canopus Consulting is prohibited.
 */

/**
 * View Helper for Concept UX.
 *
 * @author Santhosh
 */
var async = require('async')
	, demoservice = require('../services/GameServiceFixtures')
	, service = (appConfig.APP_STATUS == 'DEMO' ? require('../services/GameServiceFixtures') : require('../services/GameService'))
	, util = require('../commons/Util');

exports.getGameCoverage = function(req, res) {
	demoservice.getGameCoverage(req.params.tid, util.responseCB(res));
}

exports.getGameDefinition = function(req, res) {
	service.getGameDefinition(util.responseCB(res), req.params.tid);
}

exports.getGames = function(req, res) {
	service.getGames(util.responseCB(res), req.params.tid, req.params.offset, req.params.limit);
}

exports.getGame = function(req, res) {
	service.getGame(util.responseCB(res), req.params.tid, req.params.gid);
}

exports.createGame = function(req, res) {
	service.createGame(req.body, util.responseCB(res));
}

exports.updateGame = function(req, res) {
	service.updateGame(req.body, util.responseCB(res));
}