/*
 * Copyright (c) 2015-2016 Canopus Consulting. All rights reserved.
 *
 * This code is intellectual property of Canopus Consulting. The intellectual and technical
 * concepts contained herein may be covered by patents, patents in process, and are protected
 * by trade secret or copyright law. Any unauthorized use of this code without prior approval
 * from Canopus Consulting is prohibited.
 */

/**
 * View Helper for Language Search UI.
 *
 * @author Mohammad Azharuddin
 */
var async = require('async')
	, demoservice = require('../services/LangSearchServiceFixtures')
	, service = (appConfig.APP_STATUS == 'DEMO' ? require('../services/LangSearchServiceFixtures') : require('../services/LangSearchService'))
	, util = require('../commons/Util');

exports.getLangSearchDefinition = function(req, res) {
	demoservice.getLangSearchDefinition(util.responseCB(res), req.params.languageId);
}

exports.langSearch = function(req, res) {
	service.langSearch(util.responseCB(res), req.body);
}
