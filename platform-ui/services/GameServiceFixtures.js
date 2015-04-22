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
	, fs = require('fs')
	, _ = require('underscore');

exports.getGameCoverage = function(tid, cb) {
	async.parallel({
		games: function(callback) {
			fs.readFile('fixtures/all_games.json', 'utf8', function (err, data) {
		  		if (err) {
		  			callback(err);
		  		} else {
		  			var obj = JSON.parse(data);
		  			callback(null, obj);
		  		}
			});
		},
		concepts: function(callback) {
			fs.readFile('fixtures/concept_game_coverage.json', 'utf8', function (err, data) {
		  		if (err) {
		  			callback(err);
		  		} else {
		  			var obj = JSON.parse(data);
		  			callback(null, obj);
		  		}
			});
		}
	}, function(err, results) {
		var data = {
			games: results.games,
			concepts: results.concepts,
			rowLabel: _.pluck(results.concepts, 'name'),
			colLabel: _.pluck(results.games, 'name'),
			matrix: []
		}
		_.each(results.concepts, function(concept) {
			var conceptGames = _.pluck(concept.games, 'id');
			_.each(results.games, function(game) {
				data.matrix.push({
					row: data.rowLabel.indexOf(concept.name) + 1,
					col: data.colLabel.indexOf(game.name) + 1,
					value: (conceptGames.indexOf(game.id) == -1 ? 0 : (game.type == 'game' ? 1 : 2))
				});
			});
		});
		cb(null, data);
	});
}

exports.getGameDefinition = function(cb, taxonomyId) {
	fs.readFile('fixtures/game_definitions.json', 'utf8', function (err, data) {
  		if (err) {
  			cb(err);
  		} else {
  			var obj = JSON.parse(data);
  			var defs = obj.definitionNodes;
  			var def = null;
  			for (var i=0; i<defs.length; i++) {
  				if (defs[i].objectType == 'Game') {
  					def = defs[i];
  				}
  			}
  			if (def != null) {
  				cb(null, def);	
  			} else {
  				cb('Game definition not found');
  			}
  		}
	});
}

exports.getGames = function(cb, taxonomyId, offset, limit) {
	if (!offset) {
		offset = 0;
	}
	if (!limit) {
		limit = 10;
	}
	fs.readFile('fixtures/games.json', 'utf8', function (err, data) {
  		if (err) {
  			cb(err);
  		} else {
  			var obj = JSON.parse(data);
  			obj.offset = offset;
  			obj.limit = limit;
  			cb(null, obj);
  		}
	});
}

exports.getGame = function(cb, taxonomyId, gameId) {
	util.sendJSONResponse('game.json', cb);
}

exports.updateGame = function(data, cb) {
  	util.sendJSONResponse('game.json', cb);
}

exports.createGame = function(data, cb) {
  	util.sendJSONResponse('game.json', cb);
}