AnimationPlugin = Class.extend({
	_data: undefined,
	init: function(data, plugin) {
	    this._data = data;
	    this._id = data.id || _.uniqueId('animation');
		this.initPlugin(data, plugin);
		AnimationManager.registerPluginObject(this);
	},
	initPlugin: function(data, plugin) {
		PluginManager.addError('Subclasses of AnimationPlugin should implement this function');
	},
	animate: function(plugin) {
		PluginManager.addError('Subclasses of AnimationPlugin should implement play()');
	}
});