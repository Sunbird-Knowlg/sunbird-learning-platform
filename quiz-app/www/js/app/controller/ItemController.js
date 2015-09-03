var ItemController = DataController.extend({
    initController: function(data) {
    	if (data) {
    		ControllerManager.registerControllerInstance(this._id, this);
			this._data = data;
			this._loaded = true;
			var items = data.items;
			var model = [];
			if (items && _.isObject(items)) {
				for (var setId in items) {
					var set = items[setId];
					if (_.isArray(set)) {
						model = _.union(model, set);	
					}
				}
			}
			if (data.total_items && model.length > data.total_items) {
				model = _.first(model, data.total_items);
			}
			this._model = model;
			this._repeat = this._model.length;	
    	} else {
    		this._error = true;
    	}
	},
	evalItem: function() {
		var item = this.getModel();
		var result;
		if (item.type == 'ftb') {
			result = FTBEvaluator.evaluate(item);
		}
		if (result && result.score) {
			item.score = result.score;
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
