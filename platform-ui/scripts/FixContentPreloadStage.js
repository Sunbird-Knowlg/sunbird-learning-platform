/**

node ContentBodyUpdater.js domain_4024
**/

var _ = require('underscore'),
	async = require('async'),
	Client = require('node-rest-client').Client;

var client = new Client();

var API_ENDPOINT = "https://api.ekstep.in/learning";
//var API_ENDPOINT = "http://lp-sandbox.ekstep.org:8080/taxonomy-service";

var GET_CONTENT_URL = "/v2/content/${id}?fields=body";
var UPDATE_CONTENT_URL = "/v2/content/${id}";

//var contentId = "domain_4024";
var contentId = process.argv[2];
var contentBody = "";

async.waterfall([
    function(callback) {
        getContentBody(callback);
    },
    function(arg1, callback) {
        arg1 = arg1.split('"preload":true,"var":"item"').join('"var":"item"');
    	contentBody = arg1;
    	callback(null, 'ok');
    },
    function(arg1, callback) {
    	updateContent(callback);
    }
], function (err, result) {
    if (err) {
		console.log('Error: ' + err);
    } else {
    	console.log(result);
    }
});


function getContentBody(callback) {
	var args = {
		path: {id:contentId},
        headers: {
            "Content-Type": "application/json",
            "user-id": 'csv-import'
        },
        requestConfig:{
        	timeout: 240000
    	},
        responseConfig:{
        	timeout: 240000
    	}
	};
	var url = API_ENDPOINT + GET_CONTENT_URL;
	client.get(url, args, function(data, response) {
        var response = parseResponse(data);
        if (response.error) {
        	callback(response, 'error');	
        } else {
        	var body = response.result.content.body;
        	callback(null, body);	
        }
    }).on('error', function(err) {
        callback(err, 'error');
    });
}

function updateContent(callback) {
	var reqBody = {"request": {"content": {}}};
	reqBody.request.content.body = contentBody;
    reqBody.request.content.editorState = null;
	var args = {
		path: {id:contentId},
        headers: {
            "Content-Type": "application/json",
            "user-id": 'csv-import'
        },
        data: reqBody,
        requestConfig:{
        	timeout: 240000
    	},
        responseConfig:{
        	timeout: 240000
    	}
	};
	var url = API_ENDPOINT + UPDATE_CONTENT_URL;
	client.patch(url, args, function(data, response) {
        var response = parseResponse(data);
        if (response.error) {
        	callback(response, 'error');	
        } else {
        	callback(null, response);	
        }
    }).on('error', function(err) {
        callback(err, 'error');
    });
}

function parseResponse(data) {
	var responseData;
    if(typeof data == 'string') {
        try {
            responseData = JSON.parse(data);
        } catch(err) {
        }
    } else {
    	responseData = data;
    }
    if (responseData) {
    	if (responseData.params) {
    		if (responseData.params.status == 'failed') {
	    		var error = {'error': responseData.params.errmsg};
	    		if (responseData.result && responseData.result.messages) {
	    			error.messages = responseData.result.messages;
	    		}
	    		return error;
	    	} else {
	    		return responseData;
	    	}
    	}
    } else {
		var error = {'error': 'Invalid API response'};
	    return error;
    }
}