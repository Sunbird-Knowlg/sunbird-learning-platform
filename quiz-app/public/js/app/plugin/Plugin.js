var Plugin = Class.extend({
	_isContainer: true,
	_theme: undefined,
	_parent: undefined,
	_stage: undefined,
	_data: undefined,
	_currIndex: 0,
	_index: 0,
	_self: undefined,
	_dimensions: undefined,
	init: function(data, parent, stage, theme) {
		this._theme = theme;
		this._stage = stage;
		this._parent = parent;
		this._data = data;
		this.initPlugin(data);
		if(this._isContainer) {
			this.containerEvents(data);
		} else {
			this.assetEvents(data);
		}
	},
	setIndex: function(idx) {
		this._index = idx;
	},
	addChild: function(child, childPlugin) {
		var nextIdx = this._currIndex++;
		this._self.addChildAt(child, nextIdx);
		childPlugin.setIndex(nextIdx);
	},
	removeChild: function(idx) {
		this._self.removeChildAt(idx);
	},
	render: function() {
		this._parent.addChild(this._self, this);
	},
	update: function() {
		this._theme.update();
	},
	dimensions: function() {
		return this._dimensions;
	},
	relativeDims: function() {
		var parentDims = this._parent.dimensions();
		this._dimensions = {
            x: parseFloat(parentDims.w * (this._data.x || 0)/100),
            y: parseFloat(parentDims.h * (this._data.y || 0)/100),
            w: parseFloat(parentDims.w * (this._data.w || 0)/100),
            h: parseFloat(parentDims.h * (this._data.h || 0)/100)
        }
        return this._dimensions;
	},
	initPlugin: function(data) {
		throw "Subclasses of plugin should implement this function";
	},
	registerEvent: function(instance, eventData) {
		throw "Subclasses of plugin should implement this function";
	},
	assetEvents: function(asset) {
		var instance = this;
		//console.log('Registering asset events', 'Plugin Type -', instance._type);
		if(asset.onclick) {
	    	instance._self.cursor = "pointer";
	    	var arr = asset.onclick.split(':');
	    	//raise click event
	    	if(arr[0] == 'theme') {
	    		instance._self.on('click', function() {
	    			instance._theme.dispatchEvent(arr[1]);
	    		});
	    	} else if(arr[0] == 'stage') {
	    		instance._self.on('click', function() {
	    			instance._stage.dispatchEvent(arr[1]);
	    		});
	    	} else {
	    		//default to parent
	    		instance._self.on('click', function() {
	    			instance._parent.dispatchEvent(arr[1] || arr[0]);
	    		});
	    	}
	    }
	},
	containerEvents: function(data) {
		var instance = this;
		//console.log('Registering container events', 'Plugin Type -', instance._type);
		if(data.events && data.events.event) {
            if(_.isArray(data.events.event)) {
                data.events.event.forEach(function(e) {
                    instance.registerEvent(instance, e);
                });
            } else {
                instance.registerEvent(instance, data.events.event);
            }
        }
	}
})