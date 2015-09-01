TelemetryService = {
    isActive: false,
    ws: new ConsolewriterService(),
    _eventsVersion: "1.0",
    _gameData: undefined,
    _config: undefined,
    _user: undefined,
    _baseDir: 'QuizApp',
    _gameOutputFile: undefined,
    _gameErrorFile: undefined,
    _events: [],
    _data: {},
    mouseEventMapping: {
        click: 'TOUCH',
        dblclick: 'CHOOSE',
        mousedown: 'DROP',
        pressup: 'DRAG'
    },
    init: function(user, gameData) {
        return new Promise(function(resolve, reject) {
            if (user && gameData) {
                if (typeof cordova == 'undefined') {
                    TelemetryService.ws = new ConsolewriterService();
                } else {
                    TelemetryService.ws = new CordovaFilewriterService();
                }
                if (gameData.id && gameData.ver) {
                    TelemetryService._parentGameData = gameData;
                    TelemetryService._gameData = gameData;
                } else {
                    reject('Invalid game data.');
                }
                if (user.sid && user.uid && user.did) {
                    TelemetryService._user = user;
                } else {
                    reject('Invalid user session data.');
                }
                TelemetryService.ws.initWriter()
                    .then(function(root) {
                        return TelemetryServiceUtil.getConfig();
                    })
                    .then(function(config) {
                        TelemetryService._config = config;
                        if (TelemetryService._config.isActive) TelemetryService.isActive = TelemetryService._config.isActive;
                        TelemetryService._gameOutputFile = TelemetryService._baseDir + '/' + TelemetryService._config.outputFile.replace(TelemetryService._config.nameResetKey, gameData.id);
                        TelemetryService._gameErrorFile = TelemetryService._baseDir + '/' + TelemetryService._config.errorFile.replace(TelemetryService._config.nameResetKey, gameData.id);
                    })
                    .then(function(status) {
                        TelemetryService.createFiles();
                        console.log('TelemetryService init completed...');
                        resolve(true);
                    })
                    .catch(function(err) {
                        console.log('Error in init of TelemetryService:', err);
                        reject(err);
                    });
            } else {
                reject('User or Game data is empty.');
            }
        });
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
    start: function(id, ver) {
        if (TelemetryService.isActive) {
            return new StartEvent(id, ver);
        } else {
            return new InActiveEvent();
        }
    },
    end: function() {
        if (TelemetryService.isActive) {
            return new EndEvent();
        } else {
            return new InActiveEvent();
        }
    },
    interact: function(type, id, extype) {
        if (TelemetryService.isActive) {
            return new InteractEvent(type, id, extype);
        } else {
            return new InActiveEvent();
        }
    },
    assess: function(qid, subj, qlevel) {
        if (TelemetryService.isActive) {
            var eventObj = _.findWhere(TelemetryService._data[TelemetryService._gameData.id], {"qid": qid});
            if(eventObj) {
                return eventObj;
            } else {
                return new AssessEvent(qid, subj, qlevel);
            }
        } else {
            return new InActiveEvent();
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
            TelemetryService.ws.createBaseDirectory(TelemetryService._baseDir, function() {
                console.log('file creation failed...');
            });
            TelemetryService.ws.createFile(TelemetryService._gameOutputFile, function(fileEntry) {
                console.log(fileEntry.name + ' created successfully.');
            }, function() {
                console.log('file creation failed...');
            });
            TelemetryService.ws.createFile(TelemetryService._gameErrorFile, function(fileEntry) {
                console.log(fileEntry.name + ' created successfully.');
            }, function() {
                console.log('file creation failed...');
            });
        } else {
            // console.log('TelemetryService is inActive.');
        }
    },
    flush: function() {
        if (TelemetryService.isActive) {
            if (TelemetryService._events && TelemetryService._events.length > 0) {
                var data = JSON.stringify(TelemetryService._events);
                data = data.substring(1, data.length - 1);
                TelemetryService.ws.length(TelemetryService._gameOutputFile)
                    .then(function(fileSize) {
                        if (fileSize == 0) {
                            data = '{"events":[' + data + ']}';
                        } else {
                            data = ', ' + data + ']}';
                        }
                        return TelemetryService.ws.write(TelemetryService._gameOutputFile, data, 2);
                    })
                    .then(function(status) {
                        TelemetryService.clearEvents();
                    })
                    .catch(function(err) {
                        console.log('Error:', err);
                    });
            } else {
                console.log('No data to write...');
            }
        } else {
            // console.log('TelemetryService is inActive.');
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
            TelemetryService.ws.length(TelemetryService._gameErrorFile)
                .then(function(fileSize) {
                    data = JSON.stringify(data);
                    if (fileSize == 0) {
                        data = '{"errors":[' + data + ']}';
                    } else {
                        data = ', ' + data + ']}';
                    }
                    return TelemetryService.ws.write(TelemetryService._gameErrorFile, data, 2);
                })
                .catch(function(err) {
                    console.log('Error while writing error.json file.');
                    console.log('Error tried to log:', JSON.stringify(error));
                });
        } else {
            // console.log('TelemetryService is inActive. Error:', JSON.stringify(error));
        }
    },
    exitApp: function() {
        setTimeout(function() {
            navigator.app.exitApp();
        }, 5000);
    }
}

