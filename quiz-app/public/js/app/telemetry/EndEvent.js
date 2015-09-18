EndEvent = TelemetryEvent.extend({
    init: function(gameId, ver) {
        if (gameId && ver) TelemetryService._gameData = {
            "id": gameId,
            "ver": ver
        };
        this._super();
        this.event.eid = this.name = "OE_END";
        var startEvent = _.findWhere(TelemetryService._data[TelemetryService._gameData.id], {
            "name": "OE_START"
        });
        if (startEvent) {
            this.event.edata.eks.length = Math.round((this.createdTime - startEvent.createdTime) / 1000);
            this.flush();
        } else {
            throw "Can't end game without starting...";
        }
    },
    flush: function() {
        this._super();
        TelemetryService._events = _.union(TelemetryService._events, _.pluck(TelemetryService._data[TelemetryService._gameData.id], "event"));
        delete TelemetryService._data[TelemetryService._gameData.id];
        console.log('Game: ' + TelemetryService._gameData.id + ' end event created...');
        TelemetryService._gameData = TelemetryService._parentGameData;
        TelemetryService.flush();
    }
})