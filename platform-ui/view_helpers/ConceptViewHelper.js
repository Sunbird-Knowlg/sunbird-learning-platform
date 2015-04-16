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
	, service = require('../services/ConceptService')
	, util = require('../commons/Util');

exports.getConcept = function(req, res) {
	service.getConcept(req.params.id, util.responseCB(res));
}