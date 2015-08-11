var PluginManager = Class.extend({
	_pluginMap: {},
	_errors: [],
	registerPlugin: function(id, plugin) {
		this._pluginMap[id] = plugin;
	},
	isPlugin: function(id) {
		if(this._pluginMap[id]) {
			return true;
		} else {
			return false;
		}
	},
	invoke: function(id, data, parent, theme) {
		var p,
			pluginMap = this._pluginMap;
		if(!pluginMap[id]) {
			this._errors.push('No plugin found for - ' + id);
			console.log('No plugin found for - ', id);
		} else {
			if(_.isArray(data)) {
				data.forEach(function(d) {
					new pluginMap[id](theme, parent, d);
				})
			} else {
				p = new pluginMap[id](theme, parent, data);
			}
		}
		return p;
	}
});

var pluginManager = new PluginManager();