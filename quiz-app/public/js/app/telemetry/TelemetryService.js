TelemetryService = {
    isActive: false,
    _eventsVersion: "1.0",
    _config: undefined,
    _gameData: undefined,
    _user: undefined,
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
    interrupt: function(type, id) {
        if (TelemetryService.isActive) {
            return new InterruptEvent(type, id);
        } else {
            return new InActiveEvent();
        }
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

