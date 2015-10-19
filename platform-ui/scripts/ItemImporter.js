
/**
Usage:
node ItemImporter.js <question_type> <taxonomy_id> <item-csv> <mapping-json> <concepts-csv>

<question_type> - ftb, mcq, mtf, etc
<taxonomy_id> - numeracy, literacy_v2
<item-csv> - Items to be imported in CSV format
<mapping-json> - JSON mapping file to map columns in csv to metadata
<concepts-csv> - micro concepts csv

Sample CSV and JSON files are checked in itemImport folder.
Errors are written to itemImport/output.json file

Example:
node ItemImporter.js mcq literacy_v2 itemImport/Item-Template-mcq.csv itemImport/mcq_mapping.json itemImport/Micro-Concepts.csv
**/


var csv = require('csv');
var fs = require('fs'),
	_ = require('underscore'),
	async = require('async'),
	Client = require('node-rest-client').Client;

var client = new Client();
var API_ENDPOINT = "http://localhost:9090";
var CREATE_ITEM_URL = "/taxonomy-service/assessmentitem";
var questionType = process.argv[2];
var taxonomyId = process.argv[3];
var inputFilePath = process.argv[4];
var mappingFile = process.argv[5];
var conceptsFile = process.argv[6];

var mapping = fs.readFileSync(mappingFile);
var mappingJson = JSON.parse(mapping);

var startRow = mappingJson['start_row'];
var startCol = mappingJson['start_col'];
var conceptMap = {};
var items = [];
var resultMap = {};
var errorMap = {};

var default_qlevel = 'MEDIUM';

async.waterfall([
    function(callback) {
        loadConcepts(callback);
    },
    function(arg1, callback) {
    	importItems(callback);
    },
    function(arg1, callback) {
    	createAssessmentItems(callback);
    }		
], function (err, result) {
    if (err) {
		console.log('Error: ' + err);
    } else {
    	console.log('Assessment Items created: ROW No -- Identifier');
    	for (var s in resultMap) {
    		console.log(s + ' -- ' + JSON.stringify(resultMap[s]));
    	}
    	console.log('----------------------------------------------------------------');
		console.log('');
		console.log('');
    	console.log('Failed Items: ROW No -- Error');
    	for (var e in errorMap) {
    		console.log(e + ' -- ' + JSON.stringify(errorMap[e]));
    	}
    	console.log('----------------------------------------------------------------');
    	var fd = fs.openSync('itemImport/output.json', 'w');
    	fs.writeSync(fd, JSON.stringify(errorMap));
    }
});

function loadConcepts(callback) {
	csv()
	.from.stream(fs.createReadStream(conceptsFile))
	.on('record', function(row, index) {
		if (index > 0) {
			var code = row[0];
			var value = row[4];
			if (!isEmpty(value)) {
				if (!isEmpty(code)) {
					conceptMap[value.trim()] = code.trim();	
				}
			}
		}
	})
	.on('end', function(count){
		callback(null, 'ok');
	})
	.on('error', function(error){
		console.log('concept csv error', error);
		callback('concept csv error: ' + error);
	});	
}


function importItems(callback) {
	csv()
	.from.stream(fs.createReadStream(inputFilePath))
	.on('record', function(row, index) {
		if (index >= startRow) {
			var item = {};
			getItemRecord(row, startCol, mappingJson.data, item);
			var conceptIds = [];
			if (item['rel:associatedTo']) {
				var concepts = item['rel:associatedTo'];
				if (!_.isArray(concepts)) {
					concepts = [concepts];
				}
				concepts.forEach(function(concept) {
					if (conceptMap[concept.trim()]) {
						conceptIds.push(conceptMap[concept.trim()]);
					}
				});
				delete item['rel:associatedTo'];
			}
			item['type'] = questionType;
			items.push({'index': index, 'row': row, 'metadata': item, 'conceptIds': conceptIds});
		}
	})
	.on('end', function(count){
		console.log('End Count: ' + count);
		callback(null, 'ok');
	})
	.on('error', function(error){
		console.log('import item error', error);
		callback('import item error: ' + error);
	});
}

function createAssessmentItems(callback) {
	if (items.length > 0) {
		var asyncFns = [];
		items.forEach(function(item) {
			var metadata = item.metadata;
			if (isEmpty(metadata.code)) {
				errorMap[item.index + 1] = 'Code is required';
			} else {
				if (isEmpty(item.identifier)) {
					item.identifier = metadata.code;
				}
				if (isEmpty(metadata.qlevel)) {
					metadata.qlevel = default_qlevel;
				}
				asyncFns.push(getMWAPICallfunction(item));
			}
		});
		if (asyncFns.length > 0) {
			async.parallel(asyncFns, 
				function (err, result) {
			    	if (err) {
						callback(err);
			    	} else {
			    		callback(null, 'ok');
			    	}
			});
		} else {
			callback(null, 'ok');
		}
	} else {
		callback(null, 'ok');
	}
}

function getMWAPICallfunction(item) {
	var returnFn = function(callback) {
		var reqBody = {"request": {"assessment_item": {}}};
		reqBody.request.assessment_item.objectType = "AssessmentItem";
		reqBody.request.assessment_item.identifier = item.identifier;
		reqBody.request.assessment_item.metadata = item.metadata;
		var conceptIds = item.conceptIds;
		if (_.isArray(conceptIds) && conceptIds.length > 0) {
			reqBody.request.assessment_item.outRelations = [];
			conceptIds.forEach(function(cid) {
				reqBody.request.assessment_item.outRelations.push({"endNodeId": cid, "relationType": "associatedTo"});
			});
		}
		var args = {
	        parameters: {taxonomyId: taxonomyId},
	        headers: {
	            "Content-Type": "application/json",
	            "user-id": 'csv-import'
	        },
	        data: reqBody
	    };
	    var url = API_ENDPOINT + CREATE_ITEM_URL;
	    client.post(url, args, function(data, response) {
	        parseResponse(item, data, callback);
	    }).on('error', function(err) {
	    	errorMap[item.index + 1] = "Connection error";
	        callback(null, 'ok');
	    });
	};
	return returnFn;
}

function parseResponse(item, data, callback) {
	var responseData;
    if(typeof data == 'string') {
        try {
            responseData = JSON.parse(data);
        } catch(err) {
            errorMap[item.index + 1] = 'Invalid API response';
        }
    } else {
    	responseData = data;
    }
    if (responseData) {
    	if (responseData.params.status == 'failed') {
    		var error = {'error': responseData.params.errmsg};
    		if (responseData.result && responseData.result.messages) {
    			error.messages = responseData.result.messages;
    		}
    		errorMap[item.index + 1] = error;
    	} else {
    		resultMap[item.metadata.code] = responseData.result.node_id;
    	}
    } else {
    	errorMap[item.index + 1] = 'Invalid API response';
    }
    callback(null, 'ok');
}

function getItemRecord(row, startCol, mapping, item) {
	for (var x in mapping) {
		var data = mapping[x];
		if (_.isArray(data)) {
			item[x] = [];
			getArrayData(row, startCol, data, item[x]);
		} else {
			if (data['col-def']) {
				var val = getColumnValue(row, startCol, data['col-def']);
				if (null != val)
					item[x] = val;
			} else if (_.isObject(data)) {
				item[x] = {};
				getObjectData(row, startCol, data, item[x]);
			}
		}
	}
}

function getObjectData(row, startCol, obj, objData) {
	for (var k in obj) {
		var data = obj[k];
		if (data['col-def']) {
			var val = getColumnValue(row, startCol, data['col-def']);
			if (null != val)
				objData[k] = val;
		} else if (_.isObject(data)) {
			objData[k] = {};
			getObjectData(row, startCol, data, objData[k]);
		}
	}
}

function getArrayData(row, startCol, arr, arrData) {
	arr.forEach(function(data) {
		if (data['col-def']) {
			var val = getColumnValue(row, startCol, data['col-def']);
			if (null != val)
				arrData.push(val);
		} else if (_.isObject(data)) {
			var objData = {};
			getObjectData(row, startCol, data, objData);
			var add = false;
			for (var k in objData) {
				if (!isEmptyObject(objData[k])) {
					add = true;
				}
			}
			if (add) {
				arrData.push(objData);
			}
		}
	});
}

function isEmptyObject(obj) {
	if (_.isEmpty(obj)) {
		return true;
	} else {
		for (var k in obj) {
			if (_.isObject(obj[k])) {
				return isEmptyObject(obj[k]);
			} else {
				if (isEmpty(obj[k])) {
					return true;
				}
			}
		}
	}
	return false;
}

function getColumnValue(row, startCol, colDef) {
	var col = colDef.column;
	var result;
	if (_.isArray(col)) {
		var data = [];
		col.forEach(function(c) {
			var val = _getValueFromRow(row, startCol, c, colDef);
			if (null != val) {
				data.push(val);
			}
		});
		return data.length > 0 ? data : null;
	} else {
		return _getValueFromRow(row, startCol, col, colDef);
	}
}

function _getValueFromRow(row, startCol, col, def) {
	var index = col + startCol;
	var data = row[index];
	if (def.type == 'boolean') {
		if (data && data != null) {
			var val = data.trim().toLowerCase();
			if (val == 'yes' || val == 'true') {
				return true;
			} else {
				return false;
			}
		}	
	} else {
		if (data && data != null) {
			if (!isNaN(parseFloat(data))) {
				data = parseFloat(data);
				return data;
			}
		}
	}
	return (data && data != null) ? data.trim() : null;
}

function isEmpty(val) {
	if (!val || val == null || val.trim().length <= 0) {
		return true;
	}
	return false;
}
