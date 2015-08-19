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
	show: function(id) {
		var plugin = pluginManager.getPluginObject(id);
		plugin.show();
	},
	hide: function(id) {
		var plugin = pluginManager.getPluginObject(id);
		plugin.hide();
	},
	stop: function(ids) {
		if(ids) {
			var idArray = ids.split(',');
			for (var i = idArray.length - 1; i >= 0; i--) {
				var plugin = pluginManager.getPluginObject(idArray[i]);
				plugin.stop();
			};
		} else {
			createjs.Sound.stop();
		}
	},
	raiseEvent: function(id, eventType) {
		var plugin = pluginManager.getPluginObject(id);
		plugin.dispatchEvent(eventType);
	},
	toggleShadow: function(id) {
		var plugin = pluginManager.getPluginObject(id);
		plugin._stage._choices.forEach(function(choice) {
			choice.removeShadow();
		});
		plugin.toggleShadow();
	}
});

var commandManager = new CommandManager();