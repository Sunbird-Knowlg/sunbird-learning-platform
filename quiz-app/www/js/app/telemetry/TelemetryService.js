TelemetryService = {
    isActive: false,
    _eventsVersion: "1.0",
    _gameData: undefined,
    _config: undefined,
    _user: undefined,
    _baseDir: 'QuizApp',
    _gameOutputFile: undefined,
    _gameErrorFile: undefined,
    _events: [],
    _data: {},
    init: function(user, gameData) {
        console.log('TelemetryService init called...');
        TelemetryService._config = TelemetryServiceUtil.getConfig();
        if (TelemetryService._config.isActive) TelemetryService.isActive = TelemetryService._config.isActive;
        if (user && gameData) {
            if (gameData.id && gameData.ver) {
                TelemetryService._parentGameData = gameData;
                TelemetryService._gameData = gameData;
            } else {
                TelemetryService.exitWithError('Invalid game data.');
            }
            if (user.sid && user.uid && user.did) {
                TelemetryService._user = user;
            } else {
                TelemetryService.exitWithError('Invalid user session data.');
            }
            TelemetryService._gameOutputFile = TelemetryService._baseDir + '/' + TelemetryService._config.outputFile.replace(TelemetryService._config.nameResetKey, gameData.id);
            TelemetryService._gameErrorFile = TelemetryService._baseDir + '/' + TelemetryService._config.errorFile.replace(TelemetryService._config.nameResetKey, gameData.id);
            TelemetryService.createFiles();
        } else {
            TelemetryService.exitWithError('User or Game data is empty.');
        }
    },
    exitWithError: function(error) {
        var message = 'Telemetry Service initialization faild. Please contact game developer.';
        if (error) message += ' Error: ' + error;
        alert(message);
        TelemetryService.exitApp();
    },
    validateEvent: function(eventStr, eventData) {
        var messages = [];
        if (eventStr && eventStr.eks && eventData && eventData.eks) {
            for (key in eventStr.eks) {
                var def = eventStr.eks[key];
                if (def.required) {
                    var val = eventData.eks[key];
                    if (typeof val == 'undefined' || val == '') {
                        messages.push('required field ' + key + ' is missing.');
                    } else if (def.values && (_.indexOf(def.values, val) == -1)) {
                        messages.push('field ' + key + ' value should be from ' + def.values + ' but given: ' + val);
                    }
                }
            }
        } else {
            messages.push('Invalid event data.');
        }
        return messages;
    },
    createEventObject: function(eventName, eventData, startTime) {
        var time = new Date().getTime();
        var event = {
            "eid": eventName.toUpperCase(),
            "ts": toGenieDateTime(time),
            "ver": TelemetryService._eventsVersion,
            "gdata": TelemetryService._gameData,
            "sid": TelemetryService._user.sid,
            "uid": TelemetryService._user.uid,
            "did": TelemetryService._user.did,
            "edata": eventData
        };
        if (startTime) event.startTime = time;
        return event;
    },
    start: function(id, ver) {
        if (TelemetryService.isActive) {
            var eventName = 'OE_START';
            if (id && ver) {
                TelemetryService._gameData = {
                    "id": id,
                    "ver": ver
                };
            }
            var eventData = {
                "eks": {},
                "ext": {}
            };
            var event = TelemetryService.createEventObject(eventName, eventData);
            TelemetryService._data[TelemetryService._gameData.id] = {
                "data": [],
                "time": new Date().getTime()
            };
            TelemetryService._data[TelemetryService._gameData.id].data.push(event);
            console.log('Game: ' + TelemetryService._gameData.id + ' start event created...');
        } else {
            console.log('TelemetryService is inActive.');
        }
    },
    end: function() {
        if (TelemetryService.isActive) {
            var eventName = 'OE_END';
            if (TelemetryService._data[TelemetryService._gameData.id]) {
                var startTime = TelemetryService._data[TelemetryService._gameData.id].time;
                var time = new Date().getTime();
                var length = Math.round((time - startTime) / 1000);
                var eventData = {
                    "eks": {
                        "length": length
                    },
                    "ext": {}
                };
                var event = TelemetryService.createEventObject(eventName, eventData);
                TelemetryService._data[TelemetryService._gameData.id].data.push(event);
                TelemetryService._events = _.union(TelemetryService._events, TelemetryService._data[TelemetryService._gameData.id].data);
                delete TelemetryService._data[TelemetryService._gameData.id];
                console.log('Game: ' + TelemetryService._gameData.id + ' end event created...');
                TelemetryService._gameData = TelemetryService._parentGameData;
                TelemetryService.flush();
            } else {
                console.log('There is no game to end.');
            }
        } else {
            console.log('TelemetryService is inActive.');
        }
    },
    interact: function(type, id, extype, uri, ext) {
        if (TelemetryService.isActive) {
            var eventName = 'OE_INTERACT';
            if (type && id && extype) {
                var eventStr = TelemetryService._config.events[eventName];
                var eventData = {
                    "eks": {
                        "type": type,
                        "id": id,
                        "extype": extype
                    },
                    "ext": {}
                };
                eventData.eks.uri = uri || "";
                eventData.ext = ext || {};
                var messages = TelemetryService.validateEvent(eventStr, eventData);
                if (messages.length == 0) {
                    var event = TelemetryService.createEventObject(eventName, eventData);
                    TelemetryService._data[TelemetryService._gameData.id].data.push(event);
                    console.log('Game: ' + TelemetryService._gameData.id + ' interact event created...');
                } else {
                    TelemetryService.logError(eventName, messages);
                }
            } else {
                var messages = ['reqired data is missing to create interact event.'];
                TelemetryService.logError(eventName, messages);
            }
        } else {
            console.log('TelemetryService is inActive.');
        }
    },
    startAssess: function(subj, qid, qlevel, qtype, mc, maxscore, exres, exlength) {
        if (TelemetryService.isActive) {
            var eventName = 'OE_ASSESS';
            if (subj && qid && qlevel) {
                var eventStr = TelemetryService._config.events[eventName];
                var assessEvents = _.filter(TelemetryService._data[TelemetryService._gameData.id].data, function(event) {
                    return (event.eid == eventName && event.edata.eks.qid == qid);
                });
                var event = null;
                if (typeof assessEvents == 'undefined' || assessEvents.length == 0) {
                    var eventData = {
                        "eks": {
                            "subj": subj,
                            "qid": qid,
                            "qlevel": qlevel,
                            "qtype": qtype || "",
                            "mc": mc || [],
                            "score": 0,
                            "maxscore": maxscore || 0,
                            "exres": exres || [],
                            "exlength": exlength || 0,
                            "length": 0,
                            "atmpts": 0,
                            "failedatmpts": 0
                        },
                        "ext": {}
                    };
                    var messages = TelemetryService.validateEvent(eventStr, eventData);
                    if (messages.length == 0) {
                        event = TelemetryService.createEventObject(eventName, eventData, true);
                        TelemetryService._data[TelemetryService._gameData.id].data.push(event);
                    } else {
                        TelemetryService.logError(eventName, messages);
                    }
                } else {
                    event = assessEvents[0];
                    event.startTime = new Date().getTime();
                }
            } else {
                var messages = ['reqired data is missing to start assess event.'];
                TelemetryService.logError(eventName, messages);
            }
        } else {
            console.log('TelemetryService is inActive.');
        }
    },
    endAssess: function(qid, pass, score, res, uri, mmc, ext) {
        if (TelemetryService.isActive) {
            var eventName = 'OE_ASSESS';
            var messages = [];
            if((typeof qid != 'undefined') && (typeof pass != 'undefined') && (typeof score != 'undefined')) {
                var eventStr = TelemetryService._config.events[eventName];
                var assessEvents = _.filter(TelemetryService._data[TelemetryService._gameData.id].data, function(event) {
                    return (event.eid == eventName && event.edata.eks.qid == qid);
                });
                if(assessEvents && assessEvents.length > 0) {
                    var event = assessEvents[0];
                    event.edata.eks.length += Math.round((new Date().getTime() - event.startTime)/1000);
                    event.edata.eks.atmpts += 1;
                    event.edata.eks.score = score || 0;
                    if(pass && pass.toUpperCase() == 'YES') {
                        event.edata.eks.pass = 'Yes';
                    } else {
                        event.edata.eks.pass = 'No';
                        event.edata.eks.failedatmpts += 1;
                    }
                    event.edata.eks.res = res || [];
                    event.edata.eks.uri = uri || "";
                    event.edata.ext = ext || {};
                    delete event.startTime;
                } else {
                    messages.push('invalid qid to end assess event.');
                }
            } else {
                messages.push('reqired data is missing to end assess event.');
            }
            if(messages.length > 0) TelemetryService.logError(eventName, messages);
        } else {
            console.log('TelemetryService is inActive.');
        }
    },
    levelSet: function(eventData) {
        if (TelemetryService.isActive) {
            var eventName = 'OE_LEVEL_SET';
        } else {
            console.log('TelemetryService is inActive.');
        }
    },
    interrupt: function(eventData) {
        if (TelemetryService.isActive) {
            var eventName = 'OE_INTERRUPT';

        } else {
            console.log('TelemetryService is inActive.');
        }
    },
    createFiles: function() {
        if (TelemetryService.isActive) {
            filewriterService.createBaseDirectory(TelemetryService._baseDir, function() {
                console.log('file creation failed...');
            });
            filewriterService.createFile(TelemetryService._gameOutputFile, function(fileEntry) {
                console.log(fileEntry.name + ' created successfully.');
            }, function() {
                console.log('file creation failed...');
            });
            filewriterService.createFile(TelemetryService._gameErrorFile, function(fileEntry) {
                console.log(fileEntry.name + ' created successfully.');
            }, function() {
                console.log('file creation failed...');
            });
        } else {
            console.log('TelemetryService is inActive.');
        }
    },
    flush: function() {
        if (TelemetryService.isActive) {
            // var data = _.pluck(_.where(TelemetryService._events['OE_START'], {"ended": true}), 'data');
            // data = _.union(data, TelemetryService._events['OE_ASSESS'], TelemetryService._events['OE_INTERACT'], TelemetryService._events['OE_END']);
            if (TelemetryService._events && TelemetryService._events.length > 0) {
                var data = JSON.stringify(TelemetryService._events);
                data = data.substring(1, data.length - 1);
                filewriterService.getFileLength(TelemetryService._gameOutputFile)
                    .then(function(fileSize) {
                        if (fileSize == 0) {
                            data = '{"events":[' + data + ']}';
                        } else {
                            data = ', ' + data + ']}';
                        }
                        return filewriterService.writeFile(TelemetryService._gameOutputFile, data, 2);
                    })
                    .then(function(status) {
                        TelemetryService.clearEvents();
                        // console.log('events after clear: ', TelemetryService._events);
                    })
                    .catch(function(err) {
                        console.log('Error:', err);
                    });
            } else {
                console.log('No data to write...');
            }
        } else {
            console.log('TelemetryService is inActive.');
        }
    },
    clearEvents: function() {
        TelemetryService._events = [];
    },
    logError: function(eventName, error) {
        var data = {
            'eventName': eventName,
            'message': error,
            'time': toGenieDateTime(new Date().getTime())
        }
        if (TelemetryService.isActive) {
            filewriterService.getFileLength(TelemetryService._gameErrorFile)
                .then(function(fileSize) {
                    data = JSON.stringify(data);
                    if (fileSize == 0) {
                        data = '{"errors":[' + data + ']}';
                    } else {
                        data = ', ' + data + ']}';
                    }
                    return filewriterService.writeFile(TelemetryService._gameErrorFile, data, 2);
                })
                .catch(function(err) {
                    console.log('Error while writing error.json file.');
                    console.log('Error tried to log:', JSON.stringify(error));
                });
        } else {
            console.log('TelemetryService is inActive. Error:', JSON.stringify(error));
        }
    },
    printAll: function() {
        console.log('gameData:', TelemetryService._gameData);
        console.log('user:', TelemetryService._user);
        console.log('_gameOutputFile:', TelemetryService._gameOutputFile);
        console.log('_gameErrorFile:', TelemetryService._gameErrorFile);
        console.log('config:', TelemetryService._config);
        console.log('events config (name):', Object.keys(TelemetryService._config.events));
        console.log('events data: ', TelemetryService._events);
    },
    exitApp: function() {
        setTimeout(function() {
            navigator.app.exitApp();
        }, 5000);
    }
}

// use a index to insert relative to the end or middle of the string.
String.prototype.insert = function(index, string) {
    var ind = index < 0 ? this.length + index : index;
    return this.substring(0, ind) + string + this.substring(ind, this.length);
};

// Generate Genie format ts as per Telemetry wiki
// https://github.com/ekstep/Common-Design/wiki/Telemetry
// YYYY-MM-DDThh:mm:ss+/-nn:nn
function toGenieDateTime(ms) {
    var v = dateFormat(new Date(ms), "yyyy-mm-dd'T'HH:MM:ssZ").replace('GMT', '');
    return v.insert(-2, ':');
}

var filewriterService = new CordovaFilewriterService();
