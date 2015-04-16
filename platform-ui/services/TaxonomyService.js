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
	, util = require('../commons/Util');

exports.getAllTaxonomies = function(cb) {
	util.sendJSONResponse('all_taxonomies.json', cb);
}

exports.getTaxonomyDefinitions = function(id, cb) {
	util.sendJSONResponse('taxonomy_definitions.json', cb);
}

exports.getTaxonomyGraph = function(id, cb) {
	util.sendJSONResponse('taxonomy_graph.json', cb);
}