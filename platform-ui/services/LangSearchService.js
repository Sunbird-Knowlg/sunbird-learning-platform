
/**
 * LangSearch Service - Invoke MW API's, transform data for UI and viceversa
 *
 * @author Mohammad Azharuddin
 */
var async = require('async')
	, mwService = require('../commons/MWServiceProvider')
	, util = require('../commons/Util')
	, urlConstants = require('../commons/URLConstants')
	, _ = require('underscore');

exports.getLangSearchDefinition = function(cb, languageId) {
	var args = {
		path: {
			id: languageId
		}
	}
	mwService.getCall(urlConstants.LANG_SEARCH, args, function(err, data) {
		if(err) {
			cb(err);
		} else {
			cb(null, data.result.definition_node);
		}
	});
}

exports.langSearch = function(cb, data) {
	var args = {
		path: {
			id: data.languageId
		},
		data: {
			request: {
				data.properties
			}
		}
	}
	mwService.getCall(urlConstants.LANG_SEARCH, args, function(err, data) {
		if(err) {
			cb(err);
		} else {
			cb(null, data.result.words);
		}
	});
}
