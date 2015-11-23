/*
 * Copyright (c) 2013-2014 Canopus Consulting. All rights reserved.
 *
 * This code is intellectual property of Canopus Consulting. The intellectual and technical
 * concepts contained herein may be covered by patents, patents in process, and are protected
 * by trade secret or copyright law. Any unauthorized use of this code without prior approval
 * from Canopus Consulting is prohibited.
 */

/**
 * Helper class to invoke MW API's
 *
 * @author Santhosh
 */
var Client = require('node-rest-client').Client;
var client = new Client();
var baseUrl = appConfig.ORCHESTRATOR_URL;

exports.postCall = function(url, arguments, callback) {
    var args = {
        path: arguments.path,
        headers: {
            "Content-Type": "application/json",
            "user-id": process.domain.userId
        },
        data: arguments.data
    };
    client.post(baseUrl + url, args, function(data, response) {
        parseResponse(data, callback);
    }).on('error', function(err) {
        callback(err);
    });
}

exports.patchCall = function(url, arguments, callback) {
    var args = {
        path: arguments.path,
        headers: {
            "Content-Type": "application/json",
            "user-id": process.domain.userId
        },
        data: arguments.data
    };
    client.patch(baseUrl + url, args, function(data, response) {
        parseResponse(data, callback);
    }).on('error', function(err) {
        callback(err);
    });
}

function parseResponse(data, callback) {
    if(typeof data == 'string') {
        try {
            data = JSON.parse(data);
            callback(null, data);
        } catch(err) {
            console.log('MWServiceProvider.parseResponse(). Err', err);
            callback(err);
        }
    } else {
        callback(null, data);
    }
}

exports.getCall = function(url, arguments, callback) {
    var args = {
        path: arguments.path,
        headers: {
            "accept": "application/json",
            "user-id": process.domain.userId
        },
        parameters: arguments.parameters
    };
    client.get(baseUrl + url, args, function(data, response) {
        parseResponse(data, callback);
    }).on('error', function(err) {
        callback(err);
    });
}

exports.putCall = function(url, arguments, callback) {
    var args = {
        path: arguments.path,
        headers: {
            "Content-Type": "application/json",
            "user-id": process.domain.userId
        },
        parameters: arguments.data
    };
    client.put(baseUrl + url, args, function(data, response) {
        parseResponse(data, callback);
    }).on('error', function(err) {
        callback(err);
    });
}

exports.deleteCall = function(url, arguments, callback) {

    var args = {
        path: arguments.path,
        headers: {
            "accept": "application/json",
            "user-id": process.domain.userId
        },
        data: arguments.data
    };
    client.delete(baseUrl + url, args, function(data, response) {
        callback(null, data);
    }).on('error', function(err) {
        callback(err);
    });
}
