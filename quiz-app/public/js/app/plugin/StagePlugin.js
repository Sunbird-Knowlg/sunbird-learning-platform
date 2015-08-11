var StagePlugin = Plugin.extend({
    _type: 'stage',
    _repeat: 1,
	initPlugin: function(data) {
        if (data.repeat) {
            this._repeat = data.repeat;
        }
        var count = this._theme._stageRepeatCount[data.id] || 0;
        this._theme._stageRepeatCount[data.id] = count + 1;

        var instance = this;
		this._self = new createjs.Container();
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
                console.log('Stage Event invoked - ', eventData.on);
                var count = instance._theme._stageRepeatCount[instance._data.id];
                if (count >= instance._repeat) {
                    instance._theme.replaceStage(eventData.transition);    
                } else {
                    instance._theme.replaceStage(instance._data.id);
                }
            });
        }
    }
});
pluginManager.registerPlugin('stage', StagePlugin);