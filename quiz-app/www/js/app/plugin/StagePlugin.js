var StagePlugin = Plugin.extend({
    _type: 'stage',
    _repeat: 1,
    _datasource: undefined,
	initPlugin: function(data) {
        if (data.datasource) {
            this._datasource = data.datasource;
            var dataItems = this._theme.getAsset(this._datasource);
            if (dataItems && dataItems.items && dataItems.items.length > 0) {
                this._repeat = dataItems.items.length;
            }
        }
        var count = this._theme._stageRepeatCount[data.id] || 0;
        this._theme._stageRepeatCount[data.id] = count + 1;

        var instance = this;
		this._self = new creatine.Scene();;
		var dims = this.relativeDims();
        this._self.x = dims.x;
        this._self.y = dims.y;
        this.render();

        for(k in data) {
        	if(pluginManager.isPlugin(k)) {
        		pluginManager.invoke(k, data[k], this, this, this._theme);
        	} else {
        		// Handle plugin specific data like animations, events
        	}
        }
	},
    registerEvent: function(instance, eventData) {
        if(eventData.transition) {
            instance.on(eventData.on, function(event) {
                var count = instance._theme._stageRepeatCount[instance._data.id];
                if (count >= instance._repeat) {
                    instance._theme.replaceStage(this._self, eventData.transition, eventData.effect);
                } else {
                    instance._theme.replaceStage(this._self, instance._data.id);
                }
            });
        } else if(eventData.eval) {
            instance.on(eventData.on, function(event) {
                var count = instance._theme._stageRepeatCount[instance._data.id];
                count -= 1;
                if (count < 0 || count >= instance._repeat) {
                    count = 0;
                }
                var dataItems = instance._theme.getAsset(instance._datasource);
                var dataItem = dataItems.items[count];
                var valid = true;
                var evalFields = eventData.eval.split(',');
                evalFields.forEach(function(inputId) {
                    if (valid) {
                        var inputPlugIn = pluginManager.getPluginObject(inputId);
                        if (inputPlugIn) {
                            var ansParam = inputPlugIn._data.param;
                            var expected = dataItem.answer[ansParam];
                            var actual = document.getElementById(inputId).value; 
                            if (_.isObject(expected)) {
                                valid = _.isEqual(expected, actual);    
                            } else {
                                valid = (expected == actual);
                            }
                        } else {
                            valid = false;
                        }
                    }
                });
                if (valid) {
                    instance.dispatchEvent(eventData.success);
                } else {
                    instance.dispatchEvent(eventData.failure);
                }
            });
        } else if(eventData.show || eventData.hide) {
            instance.on(eventData.on, function(event) {
                instance._theme.disableInputs();
                var showIds = [];
                if (eventData.show) {
                    var showIds = eventData.show.split(",");
                    showIds.forEach(function(id) {
                        var plugIn = pluginManager.getPluginObject(id);
                        if (plugIn) {
                            plugIn.render();
                        }
                    });
                }
                if (eventData.hide) {
                    var hideIds = eventData.hide.split(",");
                    hideIds.forEach(function(id) {
                        var plugIn = pluginManager.getPluginObject(id);
                        if (plugIn && plugIn._parent) {
                            plugIn._parent.removeChild(plugIn._self);
                        }
                    });
                }
            });
        } else if(eventData.reload) {
            instance.on(eventData.on, function(event) {
                var count = instance._theme._stageRepeatCount[instance._data.id];
                count -= 1;
                instance._theme._stageRepeatCount[instance._data.id] = count;
                instance._theme.replaceStage(this._self, instance._data.id);
            }); 
        }
    }
});
pluginManager.registerPlugin('stage', StagePlugin);