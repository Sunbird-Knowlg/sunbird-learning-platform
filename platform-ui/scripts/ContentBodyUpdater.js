/**

Usage:
node ContentBodyUpdater.js <content_id>

-content_id  identifier of the content to be updated

This script is used to update the content with duplicate media defined in the manifest.
This script works with the following assumptions:
  1. that the content body is in JSON format.
  2. that the second media entry with the same id is the duplicate entry. the second and later media entries with a duplicate id are removed from the body.

Example:
node ContentBodyUpdater.js domain_3996

Dependencies:
- underscore
- async
- node-rest-client
**/

var _ = require('underscore'),
	async = require('async'),
	Client = require('node-rest-client').Client;

var client = new Client();

// API endpoint for production
var API_ENDPOINT = "https://api.ekstep.in/learning";

// API endpoint for QA
// var API_ENDPOINT = "https://qa.ekstep.in/api/learning";

// API endpoint for DEV
// var API_ENDPOINT = "https://dev.ekstep.in/api/learning";

var GET_CONTENT_URL = "/v2/content/${id}?fields=body";
var UPDATE_CONTENT_URL = "/v2/content/${id}";
var contentId = process.argv[2];
var contentBody = "";

async.waterfall([
    function(callback) {
        // Get content body
        getContentBody(callback);
    },
    function(arg1, callback) {
        // update the content body - assumes that body is in JSON format
        // this code needs to be updated to check if body format is XML or JSON and 
        // write code to support XML format.
        try {
            var bodyObj = JSON.parse(arg1);
            console.log('media count before removing duplicates: ' + bodyObj.theme.manifest.media.length);
            var mediaArr = bodyObj.theme.manifest.media;
            if (_.isArray(mediaArr)) {
                var mediaIds = [];
                var array = [];
                mediaArr.forEach(function(media) {
                    var idx = _.indexOf(mediaIds, media.id);
                    // remove the second media entry having the same id
                    if (idx == -1) {
                        mediaIds.push(media.id);
                        array.push(media);
                    } else {
                        console.log('duplicate id: ' + media.id);
                    }
                    // the above code is assuming that the second media entry is the invalid entry
                    // change the code for a different de-duplicatiob logic.
                });
                console.log('media count after removing duplicates: ' + array.length);
                bodyObj.theme.manifest.media = array;
            }
            arg1 = JSON.stringify(bodyObj);
        } catch(err) {
            console.log(err);
        }
    	contentBody = arg1;
    	callback(null, 'ok');
    },
    function(arg1, callback) {
        // update the content body in platform
    	updateContent(callback);
    }
], function (err, result) {
    if (err) {
		console.log('Error: ' + err);
    } else {
    	console.log(result);
    }
});

// Gets the content body from platform
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

// updates the content body in platform
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

// Parse the API response from platform
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