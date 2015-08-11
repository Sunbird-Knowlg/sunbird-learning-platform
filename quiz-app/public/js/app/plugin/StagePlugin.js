var StagePlugin = Plugin.extend({
    _type: 'stage',
	initPlugin: function(data) {
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
                instance._theme.replaceStage(eventData.transition);
            });
        }
    }
});
pluginManager.registerPlugin('stage', StagePlugin);