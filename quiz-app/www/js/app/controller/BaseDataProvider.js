var BaseDataProvider = Class.extend({
	_baseDir: '',
	_id: '',
	_folder: '',
	_data: undefined,
	_model: undefined,
	_repeat: 1,
	_index: -1,
	_loaded: false,
	_error: false,
	init: function(baseDir, id) {
		this._baseDir = baseDir;
		this._id = id;
		var tokens = id.split('.');
		this._folder = tokens[0];
		this._fileName = tokens[1] + ".json";
		$.get(this._baseDir + '/' + this._folder + '/' + this._fileName, function(data) {
			if (data) {
				this.initDataProvider(data);
			} else {
				this._error = true;
            	DataProviderManager.addError('Unable to load file: ' + id);
			}
        })
        .fail(function(err) {
        	this._error = true;
            DataProviderManager.addError('Unable to load file: ' + id);
        });
	},
	initDataProvider: function(data) {
		DataProviderManager.addError('Subclasses of DataProvider should implement initDataProvider()');
	},
	getModel: function() {
		if (_.isArray(this._model)) {
			var index = this._index;
			if (index < 0) {
				index = 0;
			}
			return this._model[index];
		} else {
			return this._model;
		}
    },
    getTemplate: function() {
    	var t;
    	if (this._model) {
    		var m = this.getModel();
    		if (m && m.template) {
    			t = m.template;
    		}
    	}
    	return t;
	},
    getModelValue: function(param) {
    	var val;
    	if (this._model && param) {
    		var m = this.getModel();
    		if (m) {
    			if (m.model) {
					m = m.model;
				}
	    		var tokens = param.split('.');
				val = m[tokens[0]];
				if (val) {
					for (var i=1; i<tokens.length; i++) {
	  					val = val[tokens[i]];
					}
				}
    		}
    	}
    	return val;
    },
    setModelValue: function(param, val) {
    	if (this._model && param) {
    		var m = this.getModel();
    		if (m) {
    			if (m.model) {
					m = m.model;
				}
	    		var expr = 'm.' + param + '=' + JSON.stringify(val);
	    		eval(expr);
    		}
    	}
    },
    getCount: function() {
    	return this._repeat;
    },
    hasNext: function() {
    	if (this._index < (this._repeat-1)) {
    		return true;
    	} else {
    		return false;
    	}
    },
    hasPrevious: function() {
    	if (this._index > 0) {
    		return true;
    	} else {
    		return false;
    	}
    },
    next: function() {
    	var d;
    	if (this.hasNext()) {
    		this._index += 1;
    		d = this._getCurrentModelItem();
    	}
    	return d;
    },
    previous: function() {
    	var d;
    	if (this.hasPrevious()) {
    		this._index -= 1;
    		d = this._getCurrentModelItem();
    	}
    	return d;
    },
    current: function() {
    	var d;
    	if (this._index >= 0 && this._index <= (this._repeat - 1)) {
    		d = this._getCurrentModelItem();
    	}
    	return d;
    },
    evalItem: function() {
		DataProviderManager.addError('evalItem() is not supported by this DataProvider');
	},
	eval: function() {
		DataProviderManager.addError('eval() is not supported by this DataProvider');
	},
	_getCurrentModelItem: function() {
		var item;
		if (_.isArray(this._model)) {
			item = this._model[this._index];
		} else {
			item = this._model;
		}
		if (item && item.model) {
			item = item.model;
		}
    	return item;
    }
})
