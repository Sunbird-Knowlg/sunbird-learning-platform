var ItemController = Controller.extend({
    initController: function(baseDir, type, id) {
    	ItemDataGenerator.loadData(baseDir, type, id, this);
	},
	onLoad: function(data, model) {
		if (_.isObject(data) && _.isArray(model)) {
			ControllerManager.registerControllerInstance(this._id, this);
			this._data = data;
			this._loaded = true;
			this._model = model;
			this._repeat = this._model.length;		
		} else {
			this._error = true;
		}
	},
	next: function() {
		var d;
    	if (this.hasNext()) {
    		this._index += 1;
    		var item = this._model[this._index];
    		if (item) {
    			d = item.model;
    			try {
    				TelemetryService.assess(item.identifier, this._data.subject, item.qlevel).start();	
    			} catch(e) {
    				ControllerManager.addError('ItemController.next() - OE_ASSESS_START error: ' + e);
    			}
			}
    	}
    	return d;
	},
	evalItem: function() {
		var item = this.getModel();
		var result;
		var pass = false;
		if (item.type == 'ftb') {
			result = FTBEvaluator.evaluate(item);
		}
		if (result) {
			pass = result.pass;
			item.score = result.score;
		}
		try {
    		var assessEnd = TelemetryService.assess(item.identifier).end(pass, item.score);
			if (_.isArray(item.mmc)) {
				assessEnd.mmc(item.mmc);
			}
    	} catch(e) {
    		ControllerManager.addError('ItemController.evalItem() - OE_ASSESS_END error: ' + e);
    	}
		return result;
    },
    feedback: function() {
    	var message;
    	var feedback = this._data.feedback;
    	if (feedback) {
    		var score = 0;
    		if (this._model) {
	    		if (_.isArray(this._model)) {
	    			this._model.forEach(function(item) {
	    				if (item.score) {
	    					score += item.score;
	    				}
	    			});
	    		} else {
	    			if (this._model.score) {
	    				score = this._model.score;
	    			}
	    		}
	    	}
	    	var percent = parseInt((score / this._data.max_score) * 100);
	    	feedback.forEach(function(range) {
                var min = 0;
                var max = 100;
                if (range.range) {
                    if (range.range.min) {
                        min = range.range.min;
                    }
                    if (range.range.max) {
                        max = range.range.max;
                    }
                }
                if (percent >= min && percent <= max) {
                    message = range.message;
                }
	        });
    	}
    	return message;
    }
});
ControllerManager.registerController('items', ItemController);
