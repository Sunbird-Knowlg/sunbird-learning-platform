var CommandManager = Class.extend({
	play: function(id) {
		var plugin = pluginManager.getPluginObject(id);
		plugin.play();
	},
	pause: function(id) {
		var plugin = pluginManager.getPluginObject(id);
		plugin.pause();
	},
	toggle: function(id) {
		var plugin = pluginManager.getPluginObject(id);
		plugin.toggle();
	},
	stop: function(id) {
		var plugin = pluginManager.getPluginObject(id);
		plugin.stop();
	},
	raiseEvent: function(id, eventType) {
		var plugin = pluginManager.getPluginObject(id);
		plugin.dispatchEvent(eventType);
	}
});

var commandManager = new CommandManager();