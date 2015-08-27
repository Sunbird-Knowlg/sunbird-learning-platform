TelemetryService = {
    _gameData: undefined,
    _config: {
        isActive: true,
        outputFile: "genie_PACKAGE_output.json",
        errorFile: "PACKAGE_error.json",
        nameResetKey: 'PACKAGE',
        events: {
            "OE_START": {
                "eks": [],
                "ext": []
            },
            "OE_END": {
                "eks": [
                    "length"
                ],
                "ext": []
            },
            "OE_LEARN": {
                "eks": [],
                "ext": []
            },
            "OE_ASSESS": {
                "eks": [],
                "ext": []
            },
            "OE_LEVEL_SET": {
                "eks": [],
                "ext": []
            },
            "OE_INTERACT": {
                "eks": [],
                "ext": []
            },
            "OE_INTERRUPT": {
                "eks": [],
                "ext": []
            },
            "OE_MISC": {
                "eks": [],
                "ext": []
            }
        },
        timeout: 2000,
        retryLimit: 10,
        minDataToWrite: 10,
        _eventVersion: 1.0
    },
    _user: undefined,
    _baseDir: 'QuizApp',
    _gameOutputFile: undefined,
    _gameErrorFile: undefined,
    _events: {
        'OE_START': [],
        'OE_END': [],
        'OE_ASSESS': [],
        'OE_INTERACT': []
    },
    init: function(user, gameData, config) {
        if (config) {
            for (key in config) {
                TelemetryService._config[key] = config[key];
            }
        }
        if (user && gameData) {
            if(gameData.id && gameData.ver) {
                TelemetryService._parentGameData = gameData;
                TelemetryService._gameData = gameData;
            } else {
                TelemetryService.exitWithError('Invalid game data.');
            }
            if(user.sid && user.uid && user.did) {
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
        if(error) message += ' Error: '+error;
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
        if (TelemetryService._config.isActive) {
            var eventName = 'OE_START';
            if(game && game.id && game.ver) {
                TelemetryService._gameData = game;
            }
            var eventData = {"eks": {},"ext": {}};
            var event = TelemetryService.createEventObject(eventName, eventData);
            TelemetryService._events[eventName].push({"id": TelemetryService._gameData.id, "data": event, "time": new Date().getTime(), "ended": false});
            console.log('Game: '+ TelemetryService._gameData.id + ' start event created...');
        } else {
            console.log('TelemetryService is inActive.');
        }
    },
    endGame: function() {
        if (TelemetryService._config.isActive) {
            var eventName = 'OE_END';
            var oeStarts = _.where(TelemetryService._events['OE_START'], {"id": TelemetryService._gameData.id, "ended": false});
            if(oeStarts && oeStarts.length > 0) {
                _.each(oeStarts, function(oeStart) {
                    var time = new Date().getTime();
                    var length = (time - oeStart.time)/1000;
                    var eventData = {"eks": {"length": length},"ext": {}};
                    var event = TelemetryService.createEventObject(eventName, eventData);
                    TelemetryService._events[eventName].push(event);
                    var oeStartIndex = _.indexOf(TelemetryService._events['OE_START'], oeStart);
                    TelemetryService._events['OE_START'][oeStartIndex].ended = true;
                    console.log('Game: '+ TelemetryService._gameData.id + ' end event created...');
                    TelemetryService._gameData = TelemetryService._parentGameData;
                });
            } else {
                console.log('There is no game to end.');
            }
        } else {
            console.log('TelemetryService is inActive.');
        }
    },
    interact: function(eventData) {

    },
    startAssess: function(eventData) {
    },
    endAssess: function(eventData) {
    },
    levelSet: function(eventData) {

    },
    interrupt: function(eventData) {

    },
    createFiles: function() {
        if (TelemetryService._config.isActive) {
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
        if (TelemetryService._config.isActive) {
            var data = _.pluck(_.where(TelemetryService._events['OE_START'], {"ended": true}), 'data');
            data = _.union(data, TelemetryService._events['OE_ASSESS'], TelemetryService._events['OE_INTERACT'], TelemetryService._events['OE_END']);
            if(data && data.length > 0) {
                data = JSON.stringify(data);
                data = data.substring(1, data.length - 1);
                filewriterService.getFileLength(TelemetryService._gameOutputFile)
                .then(function(fileSize) {
                    if(fileSize == 0) {
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
                    console.log('events after clear: ',TelemetryService._events);
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
        for(eventName in TelemetryService._events) {
            if(eventName == 'OE_START') {
                TelemetryService._events[eventName] = _.where(TelemetryService._events[eventName], {"ended": false});
            } else {
                TelemetryService._events[eventName] = [];
            }
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
String.prototype.insert = function (index, string) {
  var ind = index < 0 ? this.length + index  :  index;
  return  this.substring(0, ind) + string + this.substring(ind, this.length);
};

// Generate Genie format ts as per Telemetry wiki
// https://github.com/ekstep/Common-Design/wiki/Telemetry
// YYYY-MM-DDThh:mm:ss+/-nn:nn
function toGenieDateTime(ms){
    var v = dateFormat(new Date(ms), "yyyy-mm-dd'T'HH:MM:ssZ").replace('GMT', '');
    return v.insert(-2, ':');
}

var filewriterService = new CordovaFilewriterService();
