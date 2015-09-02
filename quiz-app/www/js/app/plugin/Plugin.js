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
	_id: undefined,
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
		this._id = data.id || data.asset || _.uniqueId('plugin');
		PluginManager.registerPluginObject(this);
		if (data.visible === false) {
	    	this._self.visible = false;
		}
		if(this._render) {
			this.render();
			//this._self.cache();
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
		Renderer.update = true;
	},
	hide: function(action) {
		if(_.contains(this.events, 'hide')) {
			EventManager.dispatchEvent(this._data.id, 'hide');
		} else {
			this._self.visible = false;
		}
		EventManager.processAppTelemetry(action, 'HIDE', this);
		Renderer.update = true;
	},
	toggleShow: function(action) {
		if(_.contains(this.events, 'toggleShow')) {
			EventManager.dispatchEvent(this._data.id, 'toggleShow');
		} else {
			this._self.visible = !this._self.visible;
		}
		EventManager.processAppTelemetry(action, this._self.visible ? 'SHOW': 'HIDE', this);
		Renderer.update = true;
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