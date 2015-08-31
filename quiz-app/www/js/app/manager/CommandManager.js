CommandManager = {
	handle: function(action) {
		var plugin = PluginManager.getPluginObject(action.asset);
		if(action.command !== 'stop' || action.sound !== true) {
			if(!plugin) {
				PluginManager.addError('Plugin not found for action - ' + JSON.stringify(action));
				return;
			}
		}
		switch(action.command) {
			case 'play':
				plugin.play(action);
				break;
			case 'pause':
				plugin.pause(action);
				break;
			case 'stop':
				if(action.sound === true) {
					createjs.Sound.stop();
					EventManager.processAppTelemetry(action, 'STOP_ALL_SOUNDS', plugin);
				} else {
					plugin.stop(action);
				}
				break;
			case 'togglePlay':
				plugin.togglePlay(action);
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