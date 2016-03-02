/*
 * Copyright (c) 2015-2016 Canopus Consulting. All rights reserved.
 *
 * This code is intellectual property of Canopus Consulting. The intellectual and technical
 * concepts contained herein may be covered by patents, patents in process, and are protected
 * by trade secret or copyright law. Any unauthorized use of this code without prior approval
 * from Canopus Consulting is prohibited.
 */

/**
 * LangSearch Service - Invoke MW API's, transform data for UI and viceversa
 *
 * @author Mohammad Azharuddin
 */
var async = require('async')
	, mwService = require('../commons/MWServiceProvider')
	, util = require('../commons/Util')
	, fs = require('fs')
	, _ = require('underscore');

exports.getLangSearchDefinition = function(cb, languageId) {
	fs.readFile('fixtures/lang_search_definition.json', 'utf8', function (err, data) {
  		if (err) {
  			cb(err);
  		} else {
  			var obj = JSON.parse(data);
  			var defs = obj.definitionNodes;
  			var def = null;
  			for (var i=0; i<defs.length; i++) {
  				if (defs[i].objectType.toLowerCase() == 'langsearch') {
  					def = defs[i];
  				}
  			}
  			if (def != null) {
  				cb(null, def);
  			} else {
  				cb('Language Search definition not found');
  			}
  		}
	});
}
