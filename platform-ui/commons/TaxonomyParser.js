/*
 * Copyright (c) 2013-2014 Canopus Consulting. All rights reserved.
 *
 * This code is intellectual property of Canopus Consulting. The intellectual and technical
 * concepts contained herein may be covered by patents, patents in process, and are protected
 * by trade secret or copyright law. Any unauthorized use of this code without prior approval
 * from Canopus Consulting is prohibited.
 */

/**
 * Utility class to parse an existing taxonomy csv to required graph json
 *
 * @author Santhosh
 */
var csv = require('csv');
var fs = require('fs');
var _ = require('underscore');

var headerFields = {
    "broad concept": "concept",
    "sub concepts": "subconcept",
    "microconcept": "microconcept"
}

function parseTaxonomyFile(fileName, outputJsonFile) {
	var header = {};
	var csvArray = [];
	csv()
	.from.stream(fs.createReadStream(fileName))
	.on('record', function(row, index){
		if(index == 0) {
			header = row;
		} else {
			var object = new Object();
			for(k in row) {
				if(headerFields[header[k].toLowerCase()]) {
					object[headerFields[header[k].toLowerCase()]] = row[k];
				}
			}
			csvArray.push(object);
		}
	})
	.on('end', function(count) {
		outputAsJson(csvArray, outputJsonFile);
	})
	.on('error', function(error){
	  	console.log(error);
	});
}

var json = {
    "name": "Numeracy",
    "conceptTitle": "Numeracy",
    "conceptId": "c1",
    "description": "",
    "level": 0,
    "children": [],
    "size": 1,
    "concepts": 12,
    "subConcepts": 847,
    "microConcepts": 123
}

var concepts = {}, subconcepts = {};

function outputAsJson(csvArray, outputJsonFile) {
	var index = 2;
	json.concepts = _.uniq(_.pluck(csvArray, 'concept')).length;
	json.subConcepts = _.uniq(_.pluck(csvArray, 'subconcept')).length;
	json.microConcepts = _.uniq(_.pluck(csvArray, 'microconcept')).length;
	json.sum = json.concepts + json.subConcepts + json.microConcepts;
	_.each(csvArray, function(obj) {
		if(!concepts[obj.concept]) {
			concepts[obj.concept] = {
				name: obj.concept,
				conceptTitle: obj.concept,
			    conceptId: "c" + index++,
			    description: "",
			    level: 1,
			    children: [],
			    size: 1,
			    subConcepts: 0,
			    microConcepts: 0
			}
			concepts[obj.concept].subConcepts = _.uniq(_.pluck(_.where(csvArray, {concept: obj.concept}), 'subconcept')).length;
			concepts[obj.concept].microConcepts = _.uniq(_.pluck(_.where(csvArray, {concept: obj.concept}), 'microconcept')).length;
			concepts[obj.concept].sum = concepts[obj.concept].subConcepts + concepts[obj.concept].microConcepts;
			json.children.push(concepts[obj.concept]);
		}
		var concept = concepts[obj.concept];
		if(!subconcepts[obj.subconcept]) {
			subconcepts[obj.subconcept] = {
				name: obj.subconcept,
				conceptTitle: obj.subconcept,
			    conceptId: "c" + index++,
			    description: "",
			    level: 2,
			    children: [],
			    size: 1,
			    subConcepts: 0,
			    microConcepts: 0
			}
			subconcepts[obj.subconcept].microConcepts = _.uniq(_.pluck(_.where(csvArray, {subconcept: obj.subconcept}), 'microconcept')).length;
			subconcepts[obj.subconcept].sum = subconcepts[obj.subconcept].microConcepts;
			concept.children.push(subconcepts[obj.subconcept]);
		}
		var subconcept = subconcepts[obj.subconcept];
		subconcept.children.push({
			name: obj.microconcept,
			conceptTitle: obj.microconcept,
		    conceptId: "c" + index++,
		    description: "",
		    level: 3,
		    size: 1,
		    sum: 0
		});
	});
	fs.writeFileSync(outputJsonFile, JSON.stringify(json))
	console.log('Output written to json file - ', outputJsonFile);
}

parseTaxonomyFile('/Users/santhosh/Downloads/Numeracy_learning_map.csv', 'fixtures/taxonomy_graph.json');