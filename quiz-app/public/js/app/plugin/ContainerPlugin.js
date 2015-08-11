var ContainerPlugin = Plugin.extend({
    _type: 'g',
	initPlugin: function(data) {
		this._self = new createjs.Container();
		var dims = this.relativeDims();
        this._self.x = dims.x;
        this._self.y = dims.y;
        this.render();

        for(k in data) {
        	if(pluginManager.isPlugin(k)) {
        		pluginManager.invoke(k, data[k], this, this._stage, this._theme);
        	} else {
        		// Handle plugin specific data like animations, events
        	}
        }
	},
    registerEvent: function(instance, eventData) {
        if(eventData.isTest) {
            instance.on(eventData.on, function(event) {
                console.log('Container Event invoked - ', eventData.on);
            });
        }
    }
});
pluginManager.registerPlugin('g', ContainerPlugin);