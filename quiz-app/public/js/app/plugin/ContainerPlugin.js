var ContainerPlugin = Plugin.extend({
	initPlugin: function(data) {
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
	}
});
pluginManager.registerPlugin('g', ContainerPlugin);