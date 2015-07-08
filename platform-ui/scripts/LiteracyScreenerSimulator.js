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
 * usage -
 *  node TelemetryDataSimulator.js <device-size> <users-per-device> <output-dir>/<file-name>
 * 	node TelemetryDataSimulator.js 10 10 /Users/santhosh/ekStep/spark_data_files/simulated_data_100.json
 */
var faker = require('faker');
var fs = require('fs');
require('date-format-lite');
faker.locale = 'en_IND';
var kafkaUtil = require('./KafkaUtil.js');

function getRandomInt(min, max) {
    return Math.floor(Math.random() * (max - min + 1)) + min;
}

function addMinutes(minutes) {
    baseDate = new Date(baseDate.getTime() + minutes * 60000);
}

function addSeconds(seconds) {
    baseDate = new Date(baseDate.getTime() + seconds * 1000);
}
/**
 * Steps to generate the telemetry data
 * Generate 100 devices with differation locations
 * For each device
 * 	Create 100 users
 * 	 For each user
 * 	   Genie signup
 * 	   Genie start
 * 	   Genie Events
 * 	   Game Events
 * 	   Genie End
 */
var sampleLatLong = ['12.9667,77.5667','12.3000,76.6500','12.8700,74.8800','17.3700,78.4800'];
var qlevels = ["EASY", "MEDIUM", "DIFFICULT"];
var passArr = ["Yes", "No"];
var events = [];
var baseDate = new Date();
baseDate.setDate(baseDate.getDate() - 2);

function generate1() {
	events.push(JSON.stringify({
        "did": "f0cc12f7-b8ff-4365-b9a2-94834e0f5483",
        "edata": {
            "eks": {
                "exres": [],
                "length": 6.917114,
                "maxscore": "1",
                "mc": "LO2\r",
                "mmc": "",
                "pass": "Yes",
                "qid": "EK.L.KAN.LT02.Q36",
                "qlevel": "",
                "qtype": "MCQ",
                "res": [],
                "score": 1,
                "subj": "LIT"
            },
            "ext": {
                "mode": "ASSESSMENT",
                "task": "LT02 Pick the correct picture (1)"
            }
        },
        "eid": "OE_ASSESS",
        "gdata": {
            "id": "org.ekstep.lit.scrnr.kan.basic",
            "ver": "1.30"
        },
        "sid": "cab1571f-eae9-43e8-8545-d6daadfd024a",
        "ts": "2015-07-07T17:59:14+05:30",
        "uid": "e0f34ffa3c10d2940937a2d499db16c5e72f5d58"
    }));
    pushEventsToKafka();
}

function generate() {

	var deviceLoc = sampleLatLong[getRandomInt(0,3)];
	var did = faker.random.uuid();
	var uid1 = faker.random.uuid();
	var sid1 = faker.random.uuid();
	var uid2 = faker.random.uuid();
	var sid2 = faker.random.uuid();
	var gid = 'org.ekstep.lit.scrnr.kan.basic';

	appendEvent({eventId: 'GE_SESSION_START', tmin: getRandomInt(0, 1), did: did, uid: uid1, sid: sid1, eksData: {ueksid: 'user1', loc: deviceLoc}});
	appendEvent({eventId: 'GE_SESSION_START', tmin: getRandomInt(0, 1), did: did, uid: uid2, sid: sid2, eksData: {ueksid: 'user2', loc: deviceLoc}});
	var t1 = baseDate.getTime();
	generateOEEvents(did, uid1, uid2, sid1, sid2, gid);
	var t2 = baseDate.getTime();
	appendEvent({eventId: 'GE_SESSION_END', tmin: getRandomInt(0, 30), dt: 'sec', did: did, uid: uid1, sid: sid1, eksData: {length: (t2-t1)/1000}});
	appendEvent({eventId: 'GE_SESSION_END', tmin: getRandomInt(0, 30), dt: 'sec', did: did, uid: uid2, sid: sid2, eksData: {length: (t2-t1)/1000}});
	pushEventsToKafka();
}

function generateOEEvents(did, uid1, uid2, sid1, sid2, gid) {
	appendEvent({eventId: 'GE_LAUNCH_GAME', tmin: getRandomInt(0, 30), dt: 'sec', did: did, uid: uid1, sid: sid1, eksData: {gid: gid, err: ''}});
	appendEvent({eventId: 'GE_LAUNCH_GAME', tmin: getRandomInt(0, 30), dt: 'sec', did: did, uid: uid2, sid: sid2, eksData: {gid: gid, err: ''}});
	var t1 = baseDate.getTime();
	for(var i=1; i <=20; i++) {
		addOEAssessEvent(i, 'LT1', did, uid1, uid2, sid1, sid2, gid);
	}
	for(var i=1; i <=70; i++) {
		addOEAssessEvent(i, 'LT2', did, uid1, uid2, sid1, sid2, gid);
	}
	for(var i=1; i <=5; i++) {
		addOEAssessEvent(i, 'LT3', did, uid1, uid2, sid1, sid2, gid);
	}
	for(var i=1; i <=20; i++) {
		addOEAssessEvent(i, 'LT4', did, uid1, uid2, sid1, sid2, gid);
	}
	for(var i=1; i <=80; i++) {
		addOEAssessEvent(i, 'LT5', did, uid1, uid2, sid1, sid2, gid);
	}
	for(var i=1; i <=20; i++) {
		addOEAssessEvent(i, 'LT6', did, uid1, uid2, sid1, sid2, gid);
	}
	for(var i=1; i <=72; i++) {
		addOEAssessEvent(i, 'LT7', did, uid1, uid2, sid1, sid2, gid);
	}
	for(var i=1; i <=5; i++) {
		addOEAssessEvent(i, 'LT8', did, uid1, uid2, sid1, sid2, gid);
	}
	for(var i=1; i <=10; i++) {
		addOEAssessEvent(i, 'LT9', did, uid1, uid2, sid1, sid2, gid);
	}
	for(var i=1; i <=5; i++) {
		addOEAssessEvent(i, 'LT10', did, uid1, uid2, sid1, sid2, gid);
	}
	for(var i=1; i <=5; i++) {
		addOEAssessEvent(i, 'LT11', did, uid1, uid2, sid1, sid2, gid);
	}
	for(var i=1; i <=8; i++) {
		addOEAssessEvent(i, 'LT12', did, uid1, uid2, sid1, sid2, gid);
	}
	for(var i=1; i <=11; i++) {
		addOEAssessEvent(i, 'LT13', did, uid1, uid2, sid1, sid2, gid);
	}
	var t2 = baseDate.getTime();
	appendEvent({eventId: 'GE_GAME_END', tmin: getRandomInt(0, 30), dt: 'sec', did: did, uid: uid1, sid: sid1, eksData: {gid: gid, err: '', length: (t2-t1)/1000}});
	appendEvent({eventId: 'GE_GAME_END', tmin: getRandomInt(0, 30), dt: 'sec', did: did, uid: uid2, sid: sid2, eksData: {gid: gid, err: '', length: (t2-t1)/1000}});
}

function addOEAssessEvent(idx, ltCode, did, uid1, uid2, sid1, sid2, gid) {
	appendEvent({eventId: 'OE_ASSESS', tmin: getRandomInt(1, 60), dt: 'sec', did: did, uid: uid1, sid: sid1, gid:gid, eksData: {
		subj: 'LIT',
		"mc": ["C:2"],
        "qid": 'EK.L.KAN.' + ltCode + '.Q' + idx,
        "qtype": "WORD_PROBLEM",
        "qlevel": qlevels[getRandomInt(0, 2)],
        "pass": passArr[getRandomInt(0, 1)],
        "mmc": [],
        "score": getRandomInt(1, 9),
        "maxscore": 10,
        "length": getRandomInt(10, 20),
        "exlength": 13,
        "atmpts": getRandomInt(1, 5),
        "failedatmpts": getRandomInt(0, 2)
	}});
	appendEvent({eventId: 'OE_ASSESS', tmin: getRandomInt(1, 60), dt: 'sec', did: did, uid: uid2, sid: sid2, gid:gid, eksData: {
		subj: 'LIT',
		"mc": ["C:2"],
        "qid": 'EK.L.KAN.' + ltCode + '.Q' + idx,
        "qtype": "WORD_PROBLEM",
        "qlevel": qlevels[getRandomInt(0, 2)],
        "pass": passArr[getRandomInt(0, 1)],
        "mmc": [],
        "score": getRandomInt(1, 9),
        "maxscore": 10,
        "length": getRandomInt(10, 20),
        "exlength": 13,
        "atmpts": getRandomInt(1, 5),
        "failedatmpts": getRandomInt(0, 2)
	}});
}

function appendEvent(args) {
	if(args.dt == 'sec') {
		addSeconds(args.tmin);
	} else {
		addMinutes(args.tmin);
	}
	events.push(JSON.stringify({
		"eid": args.eventId, // unique event ID
		//"ts": baseDate.getTime(),
		"ts": baseDate.format("YYYY-MM-DD'T'hh:mm:ss+Z"),
		"ver": "1.0",
		"gdata": {
		 	"id": args.gid || "genie.android", // genie id since this is generated by genie
		 	"ver": "1.0" // genie app release version number
		},
		"sid": args.sid,
		"uid": args.uid,
		"did": args.did,
		"edata": {
			"eks": args.eksData
		}
	}));
}

function pushEventsToKafka() {
	// Send the event to kafka every second.
	console.log("## Events Size - ", events.length);
	console.log(' Current Time - ', new Date());
	events.forEach(function(event, idx) {
		setTimeout(function() {
			kafkaUtil.send([event]);
		}, idx * 100);
	});
	console.log("### Completed telemetry data simulation ###");
	setTimeout(function() {
		console.log(' Current Time - ', new Date());
		kafkaUtil.closeClient();
	}, events.length * 100);
}

kafkaUtil.register(generate);
/**/
//generate(deviceSize, studentDeviceRatio, fileName);
