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
	    this._data = this.setPaginationEvents(data);
		this.initPlugin(data);
		if(this._isContainer) {
			this.containerEvents(data);
		} else {
			this.assetEvents(data);
		}
		if(this._render) {
			this.render();
		}
		if (data.id) {
			pluginManager.registerPluginObject(data.id, this);
		}
	},
	setIndex: function(idx) {
		this._index = idx;
	},
	addChild: function(child, childPlugin) {
		var nextIdx = this._currIndex++;
		this._self.addChildAt(child, nextIdx);
		if (childPlugin) {
			childPlugin.setIndex(nextIdx);
		}
	},
	removeChildAt: function(idx) {
		this._self.removeChildAt(idx);
	},
	removeChild: function(child) {
		this._self.removeChild(child);
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
	getRelativeDims: function(data) {
		var parentDims = this._parent.dimensions();
		var relDimensions = {
            x: parseFloat(parentDims.w * (data.x || 0)/100),
            y: parseFloat(parentDims.h * (data.y || 0)/100),
            w: parseFloat(parentDims.w * (data.w || 0)/100),
            h: parseFloat(parentDims.h * (data.h || 0)/100)
        }
        return relDimensions;
	},
	initPlugin: function(data) {
		throw "Subclasses of plugin should implement this function";
	},
	registerEvent: function(instance, eventData) {
		throw "Subclasses of plugin should implement this function";
	},
	setPaginationEvents: function(data) {
		if(data.onclick) {
			var arr = data.onclick.split(':');
			var action = arr[1] || arr[0];
	    	if (action == 'paginate_prev') {
	    		var count = this._theme._stageRepeatCount[this._stage._data.id];
	    		if (count <= 1) {
	    			data.hide = true;
	    		} else {
	    			data.hide = false;
	    		}
	    	}
		}
		return data;
	},
	assetEvents: function(asset) {
		var instance = this;
		//console.log('Registering asset events', 'Plugin Type -', instance._type);
		if(asset.onclick) {
	    	instance._self.cursor = "pointer";
	    	var events = asset.onclick.split(',');
	    	events.forEach(function(ev) {
	    		var arr = ev.split(':');
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
	    	});
	    } else if(asset.onclick_command) {
	    	instance._self.cursor = "pointer";
	    	instance._self.on('click', function() {
	    		eval('commandManager.' + asset.onclick_command + '("'+asset.id+'")');
	    	});
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
	},
	getAnimationFn: function(animate, to) {
		var instance = this;
		if(!_.isArray(to)) {
			to = [to];
		}
		var fn = '(function() {return function(plugin){';
		fn += 'createjs.Tween.get(plugin, {override:true})';
		to.forEach(function(to) {
			var data = JSON.parse(to.__cdata);
			var relDims = instance.getRelativeDims(data);
			data.x = relDims.x;
			data.y = relDims.y;
			data.width = relDims.w;
			data.height = relDims.h;
			fn += '.to(' + JSON.stringify(data) + ',' + to.duration + ', createjs.Ease.' + to.ease + ')';
		});
		if(animate.widthChangeEvent) {
			fn += '.addEventListener("change", ' + instance.getWidthHandler() + ')';
		}
		fn += '}})()';
		return fn;
	}
})