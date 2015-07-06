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

function generate(logFile) {
	for(var i=1; i <=331; i++) {
		if(i <= 20) {
			events.push('Q'+i+',LT1,LO1,LD1');
		}
		if(i > 20 && i <= 90) {
			events.push('Q'+i+',LT2,LO2,LD1');
		}
		if(i > 90 && i <= 95) {
			events.push('Q'+i+',LT3,LO3,LD2');
		}
		if(i > 95 && i <= 115) {
			events.push('Q'+i+',LT4,LO4,LD2');
		}
		if(i > 115 && i <= 195) {
			events.push('Q'+i+',LT5,LO5,LD3');
		}
		if(i > 195 && i <= 215) {
			events.push('Q'+i+',LT6,LO6,LD4');
		}
		if(i > 215 && i <= 287) {
			events.push('Q'+i+',LT7,LO7,LD4');
		}
		if(i > 287 && i <= 292) {
			events.push('Q'+i+',LT8,LO7,LD4');
		}
		if(i > 292 && i <= 302) {
			events.push('Q'+i+',LT9,LO8,LD4');
		}
		if(i > 302 && i <= 307) {
			events.push('Q'+i+',LT10,LO9,LD5');
		}
		if(i > 307 && i <= 312) {
			events.push('Q'+i+',LT11,LO9,LD5');
		}
		if(i > 312 && i <= 320) {
			events.push('Q'+i+',LT12,LO9,LD5');
		}
		if(i > 320 && i <= 331) {
			events.push('Q'+i+',LT13,LO10,LD5');
		}
	}
	fs.appendFile(logFile, events.join('\n'));
}

generate(process.argv[2]);