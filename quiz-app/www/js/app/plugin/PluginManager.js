var PluginManager = Class.extend({
	_pluginMap: {},
	_pluginObjMap: {},
	_errors: [],
	_defaultResWidth: 1920,
	_defaultResHeight: 1200,
	registerPlugin: function(id, plugin) {
		this._pluginMap[id] = plugin;
		createjs.EventDispatcher.initialize(plugin.prototype);
	},
	isPlugin: function(id) {
		if(this._pluginMap[id]) {
			return true;
		} else {
			return false;
		}
	},
	invoke: function(id, data, parent, stage, theme) {
		var p,
			pluginMap = this._pluginMap;
		if(!pluginMap[id]) {
			this._errors.push('No plugin found for - ' + id);
			console.log('No plugin found for - ', id);
		} else {
			if(_.isArray(data)) {
				data.forEach(function(d) {
					new pluginMap[id](d, parent, stage, theme);
				})
			} else {
				p = new pluginMap[id](data, parent, stage, theme);
			}
		}
		return p;
	},
	registerPluginObject: function(id, pluginObj) {
		this._pluginObjMap[id] = pluginObj;
	},
	getPluginObject: function(id) {
		return this._pluginObjMap[id];
	}
});

var pluginManager = new PluginManager();