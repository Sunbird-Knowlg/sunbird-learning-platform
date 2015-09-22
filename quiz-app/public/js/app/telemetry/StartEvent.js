StartEvent = TelemetryEvent.extend({
    init: function(id, ver) {
        if (id && ver) {
            TelemetryService._gameData = {
                "id": id,
                "ver": ver
            };
        }
        this._super();
        this.event.eid = this.name = "OE_START";
        this.flush();
    },
    flush: function() {
        this._super();
    	TelemetryService._data[TelemetryService._gameData.id] = [];
    	TelemetryService._data[TelemetryService._gameData.id].push(this);
    }
})