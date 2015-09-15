AnimationManager = {
	animationsCache: {},
	pluginMap: {},
	pluginObjMap: {},
	handle: function(action, plugin) {
		var instance;
		if(action.asset) {
			instance = PluginManager.getPluginObject(action.asset);
		} else {
			instance = plugin;
		}
		for (k in action) {
            if (AnimationManager.isPlugin(k)) {
            	var data = action[k];
            	var pluginObj = undefined;
            	if(data.id) {
            		pluginObj = AnimationManager.getPluginObject(data.id);
            	}
            	if(pluginObj) {
            		console.info("Playing from cache...");
            		pluginObj.animate(instance);
            	} else {
            		AnimationManager.invokePlugin(k, action[k], instance);
            	}
            }
        }
	},
	widthHandler: function(event, plugin) {
		var sb = plugin.getBounds();
    	plugin.scaleY = plugin.height / sb.height;
    	plugin.scaleX = plugin.width / sb.width;
	},
	isPlugin: function(id) {
		if(AnimationManager.pluginMap[id]) {
			return true;
		} else {
			return false;
		}
	},
	registerPlugin: function(id, plugin) {
		AnimationManager.pluginMap[id] = plugin;
		createjs.EventDispatcher.initialize(plugin.prototype);
	},
	invokePlugin: function(id, data, plugin) {
		var p, pluginMap = AnimationManager.pluginMap;
		if(!pluginMap[id]) {
			AnimationManager.addError('No plugin found for - ' + id);
		} else {
			if(_.isArray(data)) {
				data.forEach(function(d) {
					new pluginMap[id](d, plugin);
				})
			} else {
				p = new pluginMap[id](data, plugin);
			}
		}
		return p;
	},
	registerPluginObject: function(pluginObj) {
		AnimationManager.pluginObjMap[pluginObj._id] = pluginObj;
	},
	getPluginObject: function(id) {
		return AnimationManager.pluginObjMap[id];
	},
	cleanUp: function() {
		AnimationManager.pluginObjMap = {};
	}
}