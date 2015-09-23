AssessEvent = TelemetryEvent.extend({
	qid: undefined,
	startTime: undefined,
	_isStarted: false,
    init: function(qid, subj, qlevel) {
        this._super();
        this.event.eid = this.name = "OE_ASSESS";
        this.qid = qid;
        this.startTime = this.createdTime;
        var eventStr = TelemetryService._config.events[this.name];
        this.event.edata.eks = {
            "subj": subj,
            "qid": qid,
            "qlevel": qlevel,
            "qtype": "",
            "mc": [],
            "mmc": [],
            "score": 0,
            "maxscore": 0,
            "exres": [],
            "exlength": 0,
            "length": 0,
            "atmpts": 0,
            "failedatmpts": 0
        };
        var messages = TelemetryService.validateEvent(eventStr, this.event.edata);
        if (messages.length == 0) {
        	this._isStarted = true;
            this.flush();
        } else {
            TelemetryService.logError(this.name, messages);
            throw 'validation failed: ' + JSON.stringify(messages);
        }
    },
    start: function() {
    	this._isStarted = true;
    	this.startTime = new Date().getTime();
        return this;
    },
    end: function(pass, score, res, uri) {
    	if(this._isStarted) {
    		this.event.edata.eks.length += Math.round((new Date().getTime() - this.startTime) / 1000);
            this.event.edata.eks.atmpts += 1;
            this.event.edata.eks.score = score || 0;
            if (pass) {
                this.event.edata.eks.pass = 'Yes';
                this.event.edata.eks.mmc = [];
            } else {
                this.event.edata.eks.pass = 'No';
                this.event.edata.eks.score = 0;
                this.event.edata.eks.failedatmpts += 1;
            }
            this.event.edata.eks.res = res || [];
            this.event.edata.eks.uri = uri || "";
            this._isStarted = false;
            return this;
    	} else {
    		throw "can't end assess event without starting.";
    	}
    },
    mmc: function(mmc) {
    	this.event.edata.eks.mmc = mmc;
        return this;
    },
    maxscore: function(maxscore) {
        this.event.edata.eks.maxscore = maxscore;
        return this;  
    }
})