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
				plugin.show(action);
				break;
			case 'hide':
				plugin.hide(action);
				break;
			case 'toggleShow':
				plugin.toggleShow(action);
				break;
			case 'transitionTo':
				plugin.transitionTo(action);
				break;
			case 'event':
				EventManager.dispatchEvent(action.asset, action.value);
				break;
			case 'toggleShadow':
				plugin.toggleShadow();
				break;
			case 'windowEvent':
				window.location.href = action.href;
				break;
			case 'eval':
				plugin.evaluate(action);
				break;
			case 'reload':
				plugin.reload(action);
				break;
			default:
		}
	}
}