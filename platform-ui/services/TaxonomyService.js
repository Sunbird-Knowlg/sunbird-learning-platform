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
	var url = urlConstants.GET_TAXONOMIES.replace(':id', id);
	var params = {
		tfields: 'identifier,name,conceptsCount',
		subGraph: false
	}
	mwService.getCall(url, params, function(err, data) {
		if(err) {
			cb(err);
		} else {
			cb(null, data.result.TAXONOMY_LIST);
		}
	});
}

exports.getTaxonomyDefinitions = function(id, cb) {
	var url = urlConstants.GET_CONCEPT_TAXONOMY_DEFS.replace(':id', id);
	mwService.getCall(url, {}, function(err, data) {
		if(err) {
			cb(err);
		} else {
			cb(null, data.result.DEFINITION_NODE);
		}
	});
}

exports.getTaxonomyGraph = function(id, cb) {

	async.waterfall([
		function(next) {
			var url = urlConstants.GET_TAXONOMY.replace(':id', id);
			var params = {
				tfields: 'identifier,name,conceptsCount',
				cfields: 'identifier,name,gamesCount,description',
				subGraph: true
			}
			mwService.getCall(url, params, next)
		},
		function(response, next) {
			var subGraph = response.result.SUBGRAPH;
			var nodes = {}, rootNode = undefined;
			_.each(subGraph.nodes, function(node) {
				nodes[node.identifier] = node;
				if(node.objectType == 'taxonomy') {
					rootNode = node;
				}
			});

			var graph = getNode(nodes, rootNode, 0);
			next(null, graph);
		},
		function(graph, next) {
			var clonedGraph = JSON.parse(JSON.stringify(graph));
			paginateConcepts(clonedGraph, 10);
			next(null, graph, clonedGraph)
		}
	], function(err, graph, clonedGraph) {
		if(err) {
			cb(err);
		} else {
			var data = {
				graph: graph,
				paginatedGraph: clonedGraph
			}
			cb(null, data);
		}
	});
}

function getNode(nodes, graphNode, level) {
	var node = {
		name: rootNode.metadata.name,
	    conceptId: rootNode.identifier,
	    gamesCount: rootNode.metadata.gamesCount,
	    children: [],
	    level: level,
	    size: 1,
	    sum: 0,
	    concepts: 0,
	    subConcepts: 0,
	    microConcepts: 0
	}
	var children = _.filter(graphNode.outRelations, {relationType: 'parentOf'});
	if(children && children.length > 0) {
		_.each(children, function(relation) {
			var childNode = nodes[relation.endNodeId];
			node.children.push(getNode(childNode), (level + 1));
		});
	}
	node.sum = node.children.length;
	switch(level) {
		case 0:
			node.concepts = node.sum;
			break;
		case 1:
			node.subConcepts = node.sum;
			break;
		case 2:
			node.microConcepts = node.sum;
			break;
	}
	node.children.forEach(function(childNode) {
		node.sum += childNode.sum;
		node.concepts += childNode.concepts;
		node.subConcepts += childNode.subConcepts;
		node.microConcepts += childNode.microConcepts;
	});
	return node;
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