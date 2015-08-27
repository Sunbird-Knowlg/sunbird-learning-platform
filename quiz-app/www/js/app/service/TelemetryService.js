TelemetryService = {
    isActive: false,
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
        return true;
    },
    createEventObject: function(eventName, eventData) {
        return {
            "eid": eventName.toUpperCase(),
            "ts": toGenieDateTime(new Date().getTime()),
            "ver": TelemetryService._eventVersion,
            "gdata": TelemetryService._gameData,
            "sid": TelemetryService._user.sid,
            "uid": TelemetryService._user.uid,
            "did": TelemetryService._user.did,
            "edata": eventData
        }
    },
    startGame: function(game) {
        if (TelemetryService.isActive) {
            var eventName = 'OE_START';
            if (game && game.id && game.ver) {
                TelemetryService._gameData = game;
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
    endGame: function() {
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
    interact: function(eventData) {
        if (TelemetryService.isActive) {
            var eventName = 'OE_INTERACT';
            var eventStr = TelemetryService._config.events[eventName];
            if (TelemetryService.validateEvent(eventStr, eventData)) {
                var event = TelemetryService.createEventObject(eventName, eventData);
                TelemetryService._data[TelemetryService._gameData.id].data.push(event);
            } else {
                console.log('Invalid EventData:', eventData);
            }
        } else {
            console.log('TelemetryService is inActive.');
        }
    },
    startAssess: function(eventData) {
        if (TelemetryService.isActive) {
            var eventName = 'OE_ASSESS';

        } else {
            console.log('TelemetryService is inActive.');
        }
    },
    endAssess: function(eventData) {
        if (TelemetryService.isActive) {
            var eventName = 'OE_ASSESS';

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
                        filewriterService.writeFile(TelemetryService._gameOutputFile, data, function() {
                            console.log('File write completed...');
                        }, function() {
                            console.log('Error writing file...');
                        });
                        TelemetryService.clearEvents();
                        console.log('events after clear: ', TelemetryService._events);
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