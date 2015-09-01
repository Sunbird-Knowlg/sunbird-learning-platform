InteractEvent = TelemetryEvent.extend({
    init: function(type, id, extype) {
        this._super();
        this.event.eid = this.name = "OE_INTERACT";
        var eventStr = TelemetryService._config.events[this.name];
        if(!_.contains(eventStr.eks.type.values, type)) {
            this.event.edata.ext.type = type;
            type = "OTHER";
        }
        this.event.edata.eks = {
            "type": type,
            "id": id,
            "extype": extype
        };
        var messages = TelemetryService.validateEvent(eventStr, this.event.edata);
        if (messages.length == 0) {
            this.flush();
            console.log('Game: ' + TelemetryService._gameData.id + ' interact event created...');
        } else {
            TelemetryService.logError(this.name, messages);
            throw 'validation failed: ' + JSON.stringify(messages);
        }
    }
})