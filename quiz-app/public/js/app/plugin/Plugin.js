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
		var dims = this.relativeDims();
		if (dims && this._self) {
			this._self.origX = dims.x;
        	this._self.origY = dims.y;
        	this._self.width = dims.w;
        	this._self.height = dims.h;	
		}
        if (data.enableDrag) {
            this.enableDrag(this._self, data.snapTo);
        }
        var instance = this;
		if(data.appEvents) {
			this.appEvents.push.apply(this.appEvents, data.appEvents.list.split(','));	
		}
		EventManager.registerEvents(this, data);
		this._id = this._data.id || this._data.asset || _.uniqueId('plugin');
		PluginManager.registerPluginObject(this);
		if (data.visible === false) {
	    	this._self.visible = false;
		}
		if(this._render) {
			this.render();
			if(this._isContainer && this._type == 'stage') {
				this.cache();
			}
		}
	},
	cache: function() {
		this._self.cache(this._dimensions.x, this._dimensions.y, this._dimensions.w, this._dimensions.h);
	},
	uncache: function() {
		this._self.uncache();
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
		if (this._parent) {
			var parentDims = this._parent.dimensions();
			this._dimensions = {
	            x: parseFloat(parentDims.w * (this._data.x || 0)/100),
	            y: parseFloat(parentDims.h * (this._data.y || 0)/100),
	            w: parseFloat(parentDims.w * (this._data.w || 0)/100),
	            h: parseFloat(parentDims.h * (this._data.h || 0)/100)
	        }
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
	toggleShadow: function() {
        if (this._self.shadow) {
            this._self.shadow = undefined;
        } else {
            this._self.shadow = new createjs.Shadow(this._data.shadowColor, 0, 0, 30);
        }
        Renderer.update = true;
    },
    removeShadow: function() {
        this._self.shadow = undefined;
    },
    enableDrag: function(asset, snapTo) {
        asset.cursor = "pointer";
        asset.on("mousedown", function(evt) {
            this.parent.addChild(this);
            this.offset = {
                x: this.x - evt.stageX,
                y: this.y - evt.stageY
            };
        });
        asset.on("pressmove", function(evt) {
            this.x = evt.stageX + this.offset.x;
            this.y = evt.stageY + this.offset.y;
            Renderer.update = true;
        });
        if (snapTo) {
            asset.on("pressup", function(evt) {
                var plugin = PluginManager.getPluginObject(snapTo);
                var dims = plugin._dimensions;
                var xFactor = parseFloat(this.width * (50/100));
                var yFactor = parseFloat(this.height * (50/100));
                var x = dims.x - xFactor,
                    y = dims.y - yFactor,
                    maxX = dims.x + dims.w + xFactor,
                    maxY = dims.y + dims.h + yFactor;
                var snapSuccess = false;
                if (this.x >= x && (this.x + this.width) <= maxX) {
                    if (this.y >= y && (this.y + this.height) <= maxY) {
                        snapSuccess = true;
                    }
                }
                if (!snapSuccess) {
                    this.x = this.origX;
                    this.y = this.origY;
                } else {
                    if (plugin._data.snapX) {
                        this.x = dims.x + (dims.w * plugin._data.snapX / 100);
                    }
                    if (plugin._data.snapY) {
                        this.y = dims.y + (dims.w * plugin._data.snapY / 100);
                    }
                }
                Renderer.update = true;
            });
        }
    },
	transitionTo: function() {
		PluginManager.addError('Subclasses of plugin should implement transitionTo()');
	},
	evaluate: function() {
		PluginManager.addError('Subclasses of plugin should implement evaluate()');
	},
	reload: function() {
		PluginManager.addError('Subclasses of plugin should implement reload()');
	},
	restart: function() {
		PluginManager.addError('Subclasses of plugin should implement reload()');
	}
})