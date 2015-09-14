var Controller = Class.extend({
	_id: '',
	_data: undefined,
	_model: undefined,
	_repeat: 0,
	_index: -1,
	_loaded: false,
	_error: false,
	init: function(baseDir, type, id) {
		this._id = type + '.' + id;
        this.initController(baseDir, type, id);
	},
	initController: function(baseDir, type, id) {
		ControllerManager.addError('Subclasses of Controller should implement initController()');
	},
    onLoad: function(data, model) {
        ControllerManager.addError('Subclasses of Controller should implement onLoad()');
    },
    setIndex: function(idx) {
        if (this._loaded) {
            if (idx) {
                this._index = idx;    
            }
            if (this._index < -1) {
                this._index = -1;
            }
            if (this._index >= this._repeat) {
                this._index = (this._repeat-1);
            }
        }
    },
    incrIndex: function(incr) {
        if (this._loaded) {
            if (!incr) {
                incr = 1;
            }
            this._index = this._index + incr;
            if (this._index >= this._repeat) {
                this._index = (this._repeat-1);
            }
        }
    },
    decrIndex: function(decr) {
        if (this._loaded) {
            if (!decr) {
                decr = 1;
            }
            this._index = this._index - decr;
            if (this._index < -1) {
                this._index = -1;
            }
        }
    },
	getModel: function() {
        var m;
		if (_.isArray(this._model)) {
			var index = this._index;
			if (index < 0) {
				index = 0;
			}
			m = this._model[index];
		} else {
			m = this._model;
		}
        return m;
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
        if (this._loaded) {
            if (this._index < (this._repeat-1)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    },
    hasPrevious: function() {
        if (this._loaded) {
            if (this._index > 0) {
                return true;
            } else {
                return false;
            }
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
    	if (this._loaded && this._index >= 0 && this._index <= (this._repeat - 1)) {
    		d = this._getCurrentModelItem();
    	}
    	return d;
    },
    evalItem: function() {
		ControllerManager.addError('evalItem() is not supported by this Controller');
	},
	feedback: function() {
		ControllerManager.addError('feedback() is not supported by this Controller');
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
