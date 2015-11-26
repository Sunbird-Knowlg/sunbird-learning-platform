/*
 * Copyright (c) 2014-2015 Canopus Consulting. All rights reserved.
 *
 * This code is intellectual property of Canopus Consulting. The intellectual and technical
 * concepts contained herein may be covered by patents, patents in process, and are protected
 * by trade secret or copyright law. Any unauthorized use of this code without prior approval
 * from Canopus Consulting is prohibited.
 */

/**
 * Content Service - Invoke MW API's, transform data for UI and viceversa
 *
 * @author Mohammad Azharuddin
 */
var async = require('async')
	, mwService = require('../commons/MWServiceProvider')
	, util = require('../commons/Util')
	, fs = require('fs')
	, _ = require('underscore');

exports.getContentDefinition = function(cb, taxonomyId, contentType) {
  if (contentType.toLowerCase() == "story") {
	fs.readFile('fixtures/content_definition_story.json', 'utf8', function (err, data) {
  		if (err) {
  			cb(err);
  		} else {
  			var obj = JSON.parse(data);
  			var defs = obj.definitionNodes;
  			var def = null;
  			for (var i=0; i<defs.length; i++) {
  				if (defs[i].objectType.toLowerCase() == 'story') {
  					def = defs[i];
  				}
  			}
  			if (def != null) {
  				cb(null, def);
  			} else {
  				cb('Content definition not found');
  			}
  		}
	});
}
else {
  fs.readFile('fixtures/content_definition_worksheet.json', 'utf8', function (err, data) {
  		if (err) {
  			cb(err);
  		} else {
  			var obj = JSON.parse(data);
  			var defs = obj.definitionNodes;
  			var def = null;
  			for (var i=0; i<defs.length; i++) {
  				if (defs[i].objectType.toLowerCase() == 'worksheet') {
  					def = defs[i];
  				}
  			}
  			if (def != null) {
  				cb(null, def);
  			} else {
  				cb('Content definition not found');
  			}
  		}
	});
}
}

exports.getGames = function(cb, taxonomyId, offset, limit) {
	if (!offset) {
		offset = 0;
	}
	if (!limit) {
		limit = 6;
	}
	if (offset > 0) {
		limit = 13;
	}
	fs.readFile('fixtures/games.json', 'utf8', function (err, data) {
  		if (err) {
  			cb(err);
  		} else {
  			var obj = JSON.parse(data);
  			var games = {};
  			games.games = obj.games.slice(offset, offset + limit);
  			games.count = obj.count;
  			games.offset = offset;
  			games.limit = limit;
  			cb(null, games);
  		}
	});
}

exports.getGame = function(cb, taxonomyId, gameId) {
	fs.readFile('fixtures/game.json', 'utf8', function (err, data) {
  		if (err) {
  			cb(err);
  		} else {
  			var obj = JSON.parse(data);
  			var game = obj[gameId];
  			if (!game) {
  				game = obj['G1'];
  			}
  			cb(null, game);
  		}
	});
}

exports.updateGame = function(data, cb) {
  	util.sendJSONResponse('game.json', cb);
}

exports.createGame = function(data, cb) {
  	util.sendJSONResponse('game.json', cb);
}
