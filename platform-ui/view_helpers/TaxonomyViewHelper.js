/*
 * Copyright (c) 2013-2014 Canopus Consulting. All rights reserved.
 *
 * This code is intellectual property of Canopus Consulting. The intellectual and technical
 * concepts contained herein may be covered by patents, patents in process, and are protected
 * by trade secret or copyright law. Any unauthorized use of this code without prior approval
 * from Canopus Consulting is prohibited.
 */

/**
 * View Helper for Taxonomy/Learning Map UX.
 *
 * @author Santhosh
 */
var async = require('async')
	, service = require('../services/TaxonomyService')
	, util = require('../commons/Util');

exports.getAllTaxonomies = function(req, res) {
	service.getAllTaxonomies(util.responseCB(res));
}

exports.getTaxonomyDefinitions = function(req, res) {
	service.getTaxonomyDefinitions(req.params.id, util.responseCB(res));
}

exports.getTaxonomyGraph = function(req, res) {
	service.getTaxonomyGraph(req.params.id, util.responseCB(res));
}