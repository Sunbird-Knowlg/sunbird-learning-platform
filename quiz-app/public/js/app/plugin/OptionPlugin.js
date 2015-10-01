var OptionPlugin = Plugin.extend({
    _type: 'option',
    _isContainer: false,
    _render: false,
    _index: -1,
    _model: undefined,
    initPlugin: function(data) {
        var model = data.option;
        var value = undefined;
        if (this._parent._controller && model) {
            this._model = model;
            var controller = this._parent._controller;
            value = controller.getModelValue(model);
            this._index = parseInt(model.substring(model.indexOf('[') + 1, model.length - 1));
        }
        if (value && _.isFinite(this._index) && this._index > -1) {
        	this._self = new createjs.Container();
        	var dims = this.relativeDims();
        	this._self.x = dims.x;
        	this._self.y = dims.y;
            this._self.origX = dims.x;
            this._self.origY = dims.y;
            this._self.width = dims.w;
            this._self.height = dims.h; 
        	var hit = new createjs.Shape();
            hit.graphics.beginFill("#000").r(0, 0, dims.w, dims.h);
            this._self.hitArea = hit;
            if (value.value.type == 'image') {
                this.renderImage(value.value);
                if (this._parent._type == 'mcq') {
                    this.renderMCQOption();
                } else if (this._parent._type == 'mtf') {
                    this.renderMTFOption(value);
                }
            } else if(value.value.type == 'text') {
                this.renderText(value.value);
                if (this._parent._type == 'mcq') {
                    this.renderMCQOption();
                } else if (this._parent._type == 'mtf') {
                    this.renderMTFOption(value);
                }
            }
        }
    },
    renderMCQOption: function() {
        this._parent._options.push(this);
        this._self.cursor = 'pointer';
        var instance = this;
        this._self.on('click', function(event) {
            var ext = {
                type: event.type,
                x: event.stageX,
                y: event.stageY
            }
            EventManager.processAppTelemetry({}, 'CHOOSE', instance, ext);
            instance._parent.selectOption(instance);
        });

    },
    renderMTFOption: function(value) {
        var enableDrag = false;
        if (_.isFinite(value.index)) {
            this._index = value.index;
            this._parent._lhs_options.push(this);
        } else {
            this._parent._rhs_options.push(this);
            enableDrag = true;
        }
        if (enableDrag) {
            var instance = this;
            var asset = this._self;
            asset.cursor = 'pointer';
            asset.on("mousedown", function(evt) {
                this.parent.addChild(this);
                this.offset = {
                    x: this.x - evt.stageX,
                    y: this.y - evt.stageY
                };
                var ext = {
                    type: evt.type,
                    x: evt.stageX,
                    y: evt.stageY
                }
                EventManager.processAppTelemetry({}, 'DRAG', instance, ext);
            });
            asset.on("pressmove", function(evt) {
                this.x = evt.stageX + this.offset.x;
                this.y = evt.stageY + this.offset.y;
                Renderer.update = true;
            });
            asset.on("pressup", function(evt) {
                var snapTo;
                if (instance._parent._force === true) {
                    snapTo = instance._parent.getLhsOption(value.answer);
                } else {
                    snapTo = instance._parent._lhs_options;    
                }
                var plugin;
                var dims;
                var snapSuccess = false;
                if (_.isArray(snapTo)) {
                    for (var i=0; i<snapTo.length; i++) {
                        if (snapSuccess) {
                            break;
                        } else {
                            plugin = snapTo[i];
                            dims = plugin._dimensions;
                            var xFactor = parseFloat(this.width * (50/100));
                            var yFactor = parseFloat(this.height * (50/100));
                            var x = dims.x - xFactor,
                                y = dims.y - yFactor,
                                maxX = dims.x + dims.w + xFactor,
                                maxY = dims.y + dims.h + yFactor;
                            if (this.x >= x && (this.x + this.width) <= maxX) {
                                if (this.y >= y && (this.y + this.height) <= maxY) {
                                    snapSuccess = true;
                                }
                            }
                        }
                    }
                } else if (snapTo) {
                    plugin = snapTo;
                    dims = plugin._dimensions;
                    var xFactor = parseFloat(this.width * (50/100));
                    var yFactor = parseFloat(this.height * (50/100));
                    var x = dims.x - xFactor,
                        y = dims.y - yFactor,
                        maxX = dims.x + dims.w + xFactor,
                        maxY = dims.y + dims.h + yFactor;
                    if (this.x >= x && (this.x + this.width) <= maxX) {
                        if (this.y >= y && (this.y + this.height) <= maxY) {
                            snapSuccess = true;
                        }
                    }
                }
                if (!snapSuccess) {
                    this.x = this.origX;
                    this.y = this.origY;
                    instance._parent.setAnswer(instance);
                } else {
                    if (plugin._data.snapX) {
                        this.x = dims.x + (dims.w * plugin._data.snapX / 100);
                    }
                    if (plugin._data.snapY) {
                        this.y = dims.y + (dims.w * plugin._data.snapY / 100);
                    }
                    instance._parent.setAnswer(instance, plugin._index);
                }
                var ext = {
                    type: evt.type,
                    x: evt.stageX,
                    y: evt.stageY
                }
                EventManager.processAppTelemetry({}, 'DROP', instance, ext);
                Renderer.update = true;
            });
        }
    },
    renderImage: function(value) {
        var data = {};
        data.asset = value.asset;
        var padx = this._data.padX || 0;
        var pady = this._data.padY || 0;
        data.x = padx;
        data.y = pady;
        data.w = 100 - (2 * padx);
        data.h = 100 - (2 * pady);
        PluginManager.invoke('image', data, this, this._stage, this._theme);
        this._data.asset = value.asset;
        this._render = true;
    },
    renderText: function(data) {
        data.$t = data.asset;
        PluginManager.invoke('text', data, this, this._stage, this._theme);
        this._data.asset = data.asset;
        this._render = true;
    }
});
PluginManager.registerPlugin('option', OptionPlugin);
