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
        }
    }
});
pluginManager.registerPlugin('stage', StagePlugin);