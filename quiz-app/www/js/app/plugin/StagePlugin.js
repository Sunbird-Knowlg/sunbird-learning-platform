var StagePlugin = Plugin.extend({
    _type: 'stage',
    _repeat: 1,
    _datasource: undefined,
	initPlugin: function(data) {
        if (data.repeat) {
            this._repeat = data.repeat;
        }
        if (data.datasource) {
            this._datasource = data.datasource;
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
        } else if(eventData.on == 'eval') {
            instance.on(eventData.on, function(event) {
                // randomly generating success and failure events
                // should actually evaluate the values in eventData.values
                var i = Math.floor(Math.random() * (2)) + 1;
                if (i%2 == 0) {
                    instance.dispatchEvent(eventData.success);
                } else {
                    //TODO: objects hide is not working properly
                    //instance.dispatchEvent(eventData.failure);
                    instance.dispatchEvent(eventData.success);
                }
            });
        } else if(eventData.on == 'correct_answer' || eventData.on == 'wrong_answer' || eventData.on == 'try_again') {
            instance.on(eventData.on, function(event) {
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
                    //TODO: objects hide is not working properly
                    //this logic is not working properly
                    var hideIds = eventData.hide.split(",");
                    hideIds.forEach(function(id) {
                        var plugIn = pluginManager.getPluginObject(id);
                        if (plugIn && plugIn._parent) {
                            plugIn._parent.removeChild(plugIn._self);
                        }
                    });
                }
            });
        }
    }
});
pluginManager.registerPlugin('stage', StagePlugin);