var StagePlugin = Plugin.extend({
	initPlugin: function(data) {
        var instance = this;
		this._self = new createjs.Container();
		var dims = this.relativeDims();
        this._self.x = dims.x;
        this._self.y = dims.y;
        this.render();

        for(k in data) {
        	if(pluginManager.isPlugin(k)) {
        		pluginManager.invoke(k, data[k], this, this._theme);
        	} else {
        		// Handle plugin specific data like animations, events
        	}
        }
        if(data.events && data.events.event) {
            if(_.isArray(data.events.event)) {
                data.events.event.forEach(function(e) {
                    instance.registerEvent(instance, e);
                });
            } else {
                instance.registerEvent(instance, e);
            }
        }
	},
    registerEvent: function(instance, eventData) {
        if(eventData.transition) {
            instance.on(eventData.on, function(event) {
                console.log('Event invoked - ', eventData.on);
                instance._theme.replaceStage(eventData.transition);
            });
        }
    }
});
pluginManager.registerPlugin('stage', StagePlugin);
createjs.EventDispatcher.initialize(StagePlugin.prototype);