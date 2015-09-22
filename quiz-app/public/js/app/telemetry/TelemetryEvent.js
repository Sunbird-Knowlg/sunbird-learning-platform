TelemetryEvent = Class.extend({
    createdTime: undefined,
    name: undefined,
    event: undefined,
    init: function() {
        this.createdTime = new Date().getTime();
        this.event = {
            "ts": toGenieDateTime(this.createdTime),
            "ver": TelemetryService._eventsVersion,
            "sid": TelemetryService._user.sid,
            "uid": TelemetryService._user.uid,
            "did": TelemetryService._user.did,
            "edata": {
                "eks": {},
                "ext": {}
            }
        };
        this.event.gdata = TelemetryService._gameData;
    },
    flush: function() {
        if (this.event) {
            GenieService.sendTelemetry(JSON.stringify(this.event)).then(function() {
                
            }).catch(function(err) {
                TelemetryService.logError(this.name, err);
            });
        }
    },
    ext: function(ext) {
    	if(_.isObject(ext)) {
    		if(this.event.edata.ext) {
    			for(key in ext) 
    				this.event.edata.ext[key] = ext[key];
	    	} else {
	    		this.event.edata.ext = ext;
	    	}	
    	}
    	return this;
    }
});