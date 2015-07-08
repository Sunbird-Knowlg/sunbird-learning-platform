/*
 * Copyright (c) 2013-2014 Canopus Consulting. All rights reserved.
 *
 * This code is intellectual property of Canopus Consulting. The intellectual and technical
 * concepts contained herein may be covered by patents, patents in process, and are protected
 * by trade secret or copyright law. Any unauthorized use of this code without prior approval
 * from Canopus Consulting is prohibited.
 */

/**
 * Script to simulate Telemetry data
 *
 * @author Santhosh
 *
 */
var fs = require('fs');
var events = [];

function addToEvents(idx, ltCode, loCode, ldCode) {
	events.push(['EK.L.KAN.' + ltCode + '.Q'+idx , ltCode, loCode, ldCode].join(','));
}

function generate(logFile) {
	for(var i=1; i <=20; i++) {
		addToEvents(i, 'LT1', 'LO1', 'LD1');
	}
	for(var i=1; i <=70; i++) {
		addToEvents(i, 'LT2', 'LO2', 'LD1');
	}
	for(var i=1; i <=5; i++) {
		addToEvents(i, 'LT3', 'LO3', 'LD2');
	}
	for(var i=1; i <=20; i++) {
		addToEvents(i, 'LT4', 'LO4', 'LD2');
	}
	for(var i=1; i <=80; i++) {
		addToEvents(i, 'LT5', 'LO5', 'LD3');
	}
	for(var i=1; i <=20; i++) {
		addToEvents(i, 'LT6', 'LO6', 'LD4');
	}
	for(var i=1; i <=72; i++) {
		addToEvents(i, 'LT7', 'LO7', 'LD4');
	}
	for(var i=1; i <=5; i++) {
		addToEvents(i, 'LT8', 'LO7', 'LD4');
	}
	for(var i=1; i <=10; i++) {
		addToEvents(i, 'LT9', 'LO8', 'LD4');
	}
	for(var i=1; i <=5; i++) {
		addToEvents(i, 'LT10', 'LO9', 'LD5');
	}
	for(var i=1; i <=5; i++) {
		addToEvents(i, 'LT11', 'LO9', 'LD5');
	}
	for(var i=1; i <=8; i++) {
		addToEvents(i, 'LT12', 'LO9', 'LD5');
	}
	for(var i=1; i <=11; i++) {
		addToEvents(i, 'LT13', 'LO10', 'LD5');
	}
	fs.appendFile(logFile, events.join('\n'));
}

generate(process.argv[2]);