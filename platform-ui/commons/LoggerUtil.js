/*
 * Copyright (c) 2013-2014 Canopus Consulting. All rights reserved.
 *
 * This code is intellectual property of Canopus Consulting. The intellectual and technical
 * concepts contained herein may be covered by patents, patents in process, and are protected
 * by trade secret or copyright law. Any unauthorized use of this code without prior approval
 * from Canopus Consulting is prohibited.
 */

/**
 * Util class for orchestrator logger
 *
 * @author Santhosh
 */

var fs = require('fs');
var util = require('util');
/** Define the loggers used in orchestrator */
var logger;
var errorLogger;
var loggerType = appConfig.DEFAULT_LOGGER; // TODO: Fetch from a configuration

// Logger configuration code
if(loggerType == 'fluentd') {
	logger = require('fluent-logger');
	logger.configure('performance', {
	   host: 'localhost',
	   port: 24224,
	   timeout: 3.0
	});
} else if(loggerType == 'log4js') {
	if (!fs.existsSync('logs/')) {
		fs.mkdirSync('logs/');
	}
	var log4js = require('log4js');
	log4js.configure({
	  	appenders: [
	  		{ type: 'file', filename: 'logs/error.log', category: 'error' },
		    { type: 'file', filename: 'logs/player.log', category: 'player' }
	  	]
	});
	logger = log4js.getLogger('player');
	errorLogger = log4js.getLogger('error');
	logger.setLevel(appConfig.LOG_LEVEL);
}

exports.getConsoleLogger = function() {
    return {
        info: function(msg) {
            console.log(msg);
        },
        error: function(msg) {
            console.log(msg);
        },
        debug: function(msg) {
            console.log(msg);
        }
    }
}

exports.getFileLogger = function(logFileName) {
	console.log('getFileLogger', logFileName, fs.existsSync('logs/file/'));
	if (!fs.existsSync('logs/file/')) {
		fs.mkdirSync('logs/file/');
	}
	var log4js2 = require('log4js');
	log4js2.configure({
	  	appenders: [
	  		{ type: 'file', filename: 'logs/file/' + logFileName, category: 'file' }
	  	]
	});
	return log4js2.getLogger('file');
}

exports.error = function(er) {
	errorLogger.error(er);
}

exports.log = function(level, data) {
	var msg = data;
	if(typeof data == 'object') msg = JSON.stringify(data);
	if(loggerType == 'fluentd') {
		logger.emit(level, msg);
	} else if(loggerType == 'log4js') {
		if(level == LogLevel.FATAL)
			logger.fatal(msg);
		else if(level == LogLevel.ERROR)
			logger.error(msg);
		else if(level == LogLevel.WARN)
			logger.warn(msg);
		else if(level == LogLevel.INFO)
			logger.info(msg);
		else if(level == LogLevel.DEBUG)
			logger.debug(msg);
		else if(level == LogLevel.TRACE)
			logger.trace(msg);
	}
}

function getLogger(service) {
	var mwLogger;
	switch(service) {
		case 'interactionService':
		mwLogger = log4js.getLogger('interactions_mw');
		break;
		case 'learnerService':
		mwLogger = log4js.getLogger('learner_info_mw');
		break;
		case 'assessmentService':
		mwLogger = log4js.getLogger('assessments_mw');
		break;
		case 'dashboardService':
		mwLogger = log4js.getLogger('dashboard_mw');
		break;
		default:
		mwLogger = log4js.getLogger('ontology_mgr_mw');
		break;
	}
	return mwLogger;
}

function logMiddleware(loggerObj, object) {
	loggerObj.info(JSON.stringify(object));
}

exports.logMwReq = function(service, command, req, transactionId) {
	logMiddleware(getLogger(service), {service: service, commmand: command, transactionId: transactionId, type: 'REQUEST', data: util.inspect(req)});
}

exports.logMwRes = function(service, command, res, transactionId, time) {
	logMiddleware(getLogger(service), {service: service, commmand: command, transactionId: transactionId, type: 'RESPONSE', data: util.inspect(res), requestTime: time});
}

exports.logMwErr = function(service, command, err, transactionId, time) {
	logMiddleware(getLogger(service), {service: service, commmand: command, transactionId: transactionId, type: 'ERROR', data: util.inspect(err), requestTime: time});
}

/* End */