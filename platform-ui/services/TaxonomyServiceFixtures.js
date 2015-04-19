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
	, urlConstants = require('../commons/URLConstants')
	, _ = require('underscore');

exports.getAllTaxonomies = function(cb) {
	util.sendJSONResponse('all_taxonomies.json', cb);
}

exports.getTaxonomyDefinitions = function(id, cb) {
	util.sendJSONResponse('taxonomy_definitions.json', cb);
}

exports.getTaxonomyGraph = function(id, cb) {

	async.waterfall([
		function(next) {
			util.sendJSONResponse('taxonomy_graph.json', next);
		},
		function(graph, next) {
			var clonedGraph = JSON.parse(JSON.stringify(graph));
			paginateConcepts(clonedGraph, 10);
			next(null, graph, clonedGraph)
		}
	], function(err, graph, clonedGraph) {
		var data = {
			graph: graph,
			paginatedGraph: clonedGraph
		}
		cb(null, data);
	});
}

function paginateConcepts(concept, paginationSize) {
	if(concept.children && concept.children.length > 0) {
		paginateConceptList(concept.children, paginationSize);
	}
	if(concept.children && concept.children.length > paginationSize) {
		var children = concept.children;
		var page1 = children.slice(0, paginationSize);
		var length = page1.length;
		var pageIndex = 1;
		concept.children = page1;
		concept.children[length] = {name: 'more', pages: [], pageIndex: pageIndex};
		while(children.length > paginationSize) {
			concept.children[length].pages.push({page: pageIndex++, nodes: children.splice(0, paginationSize)});
		}

		if(children.length > 0) {
			concept.children[length].pages.push({page: pageIndex++, nodes: children.splice(0, paginationSize)});
		}
	}
}

function paginateConceptList(concepts, paginationSize) {
	concepts.forEach(function(concept) {
		paginateConcepts(concept, paginationSize);
	});
}