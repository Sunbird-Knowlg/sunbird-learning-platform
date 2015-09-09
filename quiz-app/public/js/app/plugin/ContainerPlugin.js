var ContainerPlugin = Plugin.extend({
    _type: 'g',
    _render: true,
	initPlugin: function(data) {
		this._self = new createjs.Container();
		var dims = this.relativeDims();
        this._self.x = dims.x;
        this._self.y = dims.y;
        if(data.hitArea) {
            var hit = new createjs.Shape();
            hit.graphics.beginFill("#000").r(0, 0, dims.w, dims.h);
            this._self.hitArea = hit;
        }
        for(k in data) {
        	if(PluginManager.isPlugin(k)) {
        		PluginManager.invoke(k, data[k], this, this._stage, this._theme);
        	}
        }
	}
});
PluginManager.registerPlugin('g', ContainerPlugin);