TelemetryService = {
    isActive: false,
    _eventsVersion: "1.0",
    _config: undefined,
    _gameData: undefined,
    _user: undefined,
    _baseDir: 'EkStep Content App',
    _gameOutputFile: undefined,
    _gameErrorFile: undefined,
    _events: [],
    _data: {},
    _assessData: {},
    mouseEventMapping: {
        click: 'TOUCH',
        dblclick: 'CHOOSE',
        mousedown: 'DROP',
        pressup: 'DRAG'
    },
    init: function(user, gameData) {
        return new Promise(function(resolve, reject) {
            if (user && gameData) {
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
                TelemetryServiceUtil.getConfig().then(function(config) {
                    TelemetryService._config = config;
                    if (TelemetryService._config.isActive) TelemetryService.isActive = TelemetryService._config.isActive;
                    resolve(true);
                }).catch(function(err) {
                    console.log('Error in init of TelemetryService:', err);
                    reject(err);
                });

            } else {
                reject('User or Game data is empty.');
            }
        });
    },
    exitWithError: function(error) {
        var message = '';
        if (error) message += ' Error: ' + JSON.stringify(error);
        TelemetryService.exitApp();
    },
    start: function(id, ver) {
        if (TelemetryService.isActive) {
            return new StartEvent(id, ver);
        } else {
            return new InActiveEvent();
        }
    },
    end: function(gameId) {
        if (TelemetryService.isActive) {
            return new EndEvent(gameId);
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
            var eventObj = TelemetryService._assessData[TelemetryService._gameData.id][qid];
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
    interrupt: function(type, id) {
        if (TelemetryService.isActive) {
            return new InterruptEvent(type, id);
        } else {
            return new InActiveEvent();
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
    writeFile: function() {
        return new Promise(function(resolve, reject) {
            if (TelemetryService.isActive) {
                if (TelemetryService._events && TelemetryService._events.length > 0) {
                    var data = JSON.stringify(TelemetryService._events);
                    data = data.substring(1, data.length - 1);
                    data = '{"events":[' + data + ']}';
                    TelemetryService.ws.write(TelemetryService._gameOutputFile, data)
                    .then(function(status) {
                        TelemetryService.clearEvents();
                        resolve(true);
                    })
                    .catch(function(err) {
                        console.log('Error:', err);
                        resolve(true);
                    });
                } else {
                    resolve(true);
                }
            } else {
                resolve(true);
            }
        });
        
    },
    sendIntentResult: function() {
        return new Promise(function(resolve, reject) {
            if (TelemetryService.isActive) {
                if (TelemetryService._events && TelemetryService._events.length > 0) {
                    var data = JSON.stringify(TelemetryService._events);
                    IntentService.sendTelemetryEvents(data).then(function() {
                        resolve(true);
                    }).catch(function(err) {
                        console.log('Error:', err);
                        resolve(true);
                    });
                } else {
                    resolve(true);
                }
            } else {
                resolve(true);
            }
        });
        
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
        // change this to write to file??
        console.log('TelemetryService Error:', JSON.stringify(data));
    },
    exitApp: function() {
        setTimeout(function() {
            navigator.app.exitApp();
        }, 5000);
    }
}

