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
	events: [],
	appEvents: [],
	init: function(data, parent, stage, theme) {
		this.events = [];
		this.appEvents = [];
		this._theme = theme;
		this._stage = stage;
		this._parent = parent;
	    this._data = data;
		this.initPlugin(data);
		if(data.appEvents) {
			this.appEvents.push.apply(this.appEvents, data.appEvents.list.split(','));
		}
		EventManager.registerEvents(this, data);
		if (data.id) {
			PluginManager.registerPluginObject(data.id, this);
		}
		if (data.visible === false) {
	    	this._self.visible = false;
		}
		if(this._render) {
			this.render();
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
	resolveParams: function(str) {
		var val;
		if (str) {
			var tokens = str.split('.');
	        val = '';
	        for (var i=0; i<tokens.length; i++) {
	            if (tokens[i].trim().startsWith('#')) {
	                if (this._stage.params[tokens[i].trim().substring(1)]) {
	                    val += this._stage.params[tokens[i].trim().substring(1)];     
	                } else {
	                    val += tokens[i];     
	                }
	            } else {
	                val += tokens[i]; 
	            }
	            if (i < tokens.length - 1) {
	                val += '.';
	            }
	        }
		}
		return val;
	},
	initPlugin: function(data) {
		PluginManager.addError('Subclasses of plugin should implement this function');
		throw "Subclasses of plugin should implement this function";
	},
	play: function() {
		PluginManager.addError('Subclasses of plugin should implement play()');
	},
	pause: function() {
		PluginManager.addError('Subclasses of plugin should implement pause()');
	},
	stop: function() {
		PluginManager.addError('Subclasses of plugin should implement stop()');
	},
	togglePlay: function() {
		PluginManager.addError('Subclasses of plugin should implement togglePlay()');
	},
	show: function(action) {
		if(_.contains(this.events, 'show')) {
			EventManager.dispatchEvent(this._data.id, 'show');
		} else {
			this._self.visible = true;
		}
		EventManager.processAppTelemetry(action, 'SHOW', this);
	},
	hide: function(action) {
		if(_.contains(this.events, 'hide')) {
			EventManager.dispatchEvent(this._data.id, 'hide');
		} else {
			this._self.visible = false;
		}
		EventManager.processAppTelemetry(action, 'HIDE', this);
	},
	toggleShow: function(action) {
		if(_.contains(this.events, 'toggleShow')) {
			EventManager.dispatchEvent(this._data.id, 'toggleShow');
		} else {
			this._self.visible = !this._self.visible;
		}
		EventManager.processAppTelemetry(action, this._self.visible ? 'SHOW': 'HIDE', this);
	},
	transitionTo: function() {
		PluginManager.addError('Subclasses of plugin should implement transitionTo()');
	},
	toggleShadow: function() {
		PluginManager.addError('Subclasses of plugin should implement toggleShadow()');
	},
	evaluate: function() {
		PluginManager.addError('Subclasses of plugin should implement evaluate()');
	},
	reload: function() {
		PluginManager.addError('Subclasses of plugin should implement reload()');
	}
})