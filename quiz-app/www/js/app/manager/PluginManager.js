PluginManager = {
	pluginMap: {},
	pluginObjMap: {},
	errors: [],
	defaultResWidth: 1920,
	defaultResHeight: 1200,
	registerPlugin: function(id, plugin) {
		PluginManager.pluginMap[id] = plugin;
		createjs.EventDispatcher.initialize(plugin.prototype);
	},
	isPlugin: function(id) {
		if(PluginManager.pluginMap[id]) {
			return true;
		} else {
			return false;
		}
	},
	invoke: function(id, data, parent, stage, theme) {
		var p,
			pluginMap = PluginManager.pluginMap;
		if(!pluginMap[id]) {
			PluginManager.addError('No plugin found for - ' + id);
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
	registerPluginObject: function(pluginObj) {
		PluginManager.pluginObjMap[pluginObj._id] = pluginObj;
	},
	getPluginObject: function(id) {
		return PluginManager.pluginObjMap[id];
	},
	addError: function(error) {
		PluginManager.errors.push(error);
	},
	getErrors: function() {
		return PluginManager.errors;
	},
	cleanUp: function() {
		PluginManager.pluginObjMap = {};
		PluginManager.errors = [];
	}
}