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
			util.sendJSONResponse('game_mock_data.json', callback);
		},
		concepts: function(callback) {
			util.sendJSONResponse('concept_game_coverage.json', callback);
		}
	}, function(err, results) {
		var data = {
			games: results.games,
			concepts: results.concepts,
			rowLabel: [],
			colLabel: [],
			matrix: [],
			stats: {
				noOfGames: results.games.length,
				noOfConcepts: results.concepts.length,
				conceptsWithNoGame: 0,
				conceptsWithNoScreener: 0
			}
		}
		var gameMap = {};
		_.each(results.games, function(game) {
			game.conceptCount = 0;
			gameMap[game.identifier] = game;
			data.colLabel.push({id: game.identifier, name: game.name});
		});
		data.colUniqueIds = _.pluck(data.colLabel, 'id');
		data.rowUniqueIds = _.pluck(data.concepts, 'id');
		_.each(results.concepts, function(concept) {
			data.rowLabel.push({id: concept.id, name: concept.name});
			var conceptGames = _.pluck(concept.games, 'identifier');
			var gameCount = _.where(concept.games, {purpose: 'Game'}).length;
			var screenerCount = _.where(concept.games, {purpose: 'Screener'}).length;
			concept.gameCount = concept.games ? concept.games.length : 0;
			if(gameCount == 0) {
				data.stats.conceptsWithNoGame++;
			}
			if(screenerCount == 0) {
				data.stats.conceptsWithNoScreener++;
			}
			_.each(concept.games, function(game) {
				gameMap[game.identifier].conceptCount++;
			});
			_.each(results.games, function(game) {
				data.matrix.push({
					row: data.rowUniqueIds.indexOf(concept.id) + 1,
					rowId: concept.id,
					colId: game.identifier,
					col: data.colUniqueIds.indexOf(game.identifier) + 1,
					value: (conceptGames.indexOf(game.identifier) == -1 ? 0 : (game.purpose == 'Game' ? 1 : 2))
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