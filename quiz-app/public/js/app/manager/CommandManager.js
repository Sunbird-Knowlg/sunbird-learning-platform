CommandManager = {
	audioActions: ['play', 'pause', 'stop', 'togglePlay'],
	handle: function(action) {
		var plugin = PluginManager.getPluginObject(action.asset);
		if(!_.contains(CommandManager.audioActions, action.command)) {
			if(!plugin) {
				PluginManager.addError('Plugin not found for action - ' + JSON.stringify(action));
				return;
			}
		}
		switch(action.command) {
			case 'play':
				AudioManager.play(action);
				break;
			case 'pause':
				AudioManager.pause(action);
				break;
			case 'stop':
				if(action.sound === true) {
					AudioManager.stopAll(action);
				} else {
					AudioManager.stop(action);
				}
				break;
			case 'togglePlay':
				AudioManager.togglePlay(action);
				break;
			case 'show':
				if (plugin) plugin.show(action);
				break;
			case 'hide':
				if (plugin) plugin.hide(action);
				break;
			case 'toggleShow':
				if (plugin) plugin.toggleShow(action);
				break;
			case 'transitionTo':
				if (plugin) plugin.transitionTo(action);
				break;
			case 'event':
				EventManager.dispatchEvent(action.asset, action.value);
				break;
			case 'toggleShadow':
				if (plugin) plugin.toggleShadow();
				break;
			case 'windowEvent':
				window.location.hash = action.href;
				break;
			case 'eval':
				if (plugin) plugin.evaluate(action);
				break;
			case 'reload':
				if (plugin) plugin.reload(action);
				break;
			case 'restart':
				if (plugin) plugin.restart(action);
				break;
			default:
		}
	}
}