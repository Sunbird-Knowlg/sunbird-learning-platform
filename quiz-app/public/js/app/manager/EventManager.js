EventManager = {
	appEvents: ['enter', 'exit', 'remove', 'add', 'replace', 'show', 'hide'],
	registerEvents: function(plugin, data) {
		var events = undefined;
		if(data.events) {
			events = data.events.event
		} else {
			events = data.event;
		}
		if(_.isArray(events)) {
			events.forEach(function(e) {
				EventManager.registerEvent(e, plugin);
			});
		} else if(events) {
			EventManager.registerEvent(events, plugin);
		}
	},
	registerEvent: function(evt, plugin) {
		plugin.events.push(evt.type);
		if(_.contains(EventManager.appEvents, evt.type) || _.contains(plugin.appEvents, evt.type)) { // Handle app events
			plugin.on(evt.type, function() {
				EventManager.handleActions(evt, plugin);
			});
		} else { // Handle mouse events
			plugin._self.cursor = 'pointer';
			plugin._self.on(evt.type, function(event) {
				EventManager.processMouseTelemetry(evt, event, plugin);
				EventManager.handleActions(evt, plugin);
			});
		}
	},
	dispatchEvent: function(id, event) {
		var plugin = PluginManager.getPluginObject(id);
		if(_.contains(EventManager.appEvents, event) || _.contains(plugin.appEvents, event)) { // Dispatch app events
			plugin.dispatchEvent(event);
		} else { // Dispatch touch events
			plugin._self.dispatchEvent(event);
		}
	},
	handleActions: function(evt, plugin) {
		if(_.isArray(evt.action)) {
			evt.action.forEach(function(action) {
				EventManager.handleAction(action, plugin);
			});
		} else if(evt.action) {
			EventManager.handleAction(evt.action, plugin);
		}
	},
	handleAction: function(action, plugin) {
		var stage = plugin._stage;
		if (!stage || stage == null) {
			stage = plugin;
		}
		if (stage && stage._type === 'stage') {
			if(action.param) {
				action.value = stage.params[action.param] || '';
			}
			if (action.asset_param) {
				action.asset = stage.params[action.asset_param] || '';
			} else if (action.asset_model) {
				action.asset = stage.getModelValue(action.asset_model) || '';
			}
		}
		if(action.type === 'animation') {
			AnimationManager.handle(action, plugin);
		} else {
			CommandManager.handle(action);
		}
	},
	processMouseTelemetry: function(action, event, plugin) {
		var ext = {
			type: event.type,
			x: event.stageX,
			y: event.stageY
		}
		var type = TelemetryService.mouseEventMapping[action.type];
		EventManager.processAppTelemetry(action, type, plugin, ext);
	},
	processAppTelemetry: function(action, type, plugin, ext) {
		if(!plugin) {
			plugin = {_data: {id: '', asset: ''}};
		}
		if(!action) {
			action = {disableTelemetry: true};
		}
		if(action.disableTelemetry !== true) {
			if(type) {
				var id = plugin._data.id || plugin._data.asset;
				if (!id) {
					id = action.asset;
				}
				if (!id) {
					var actionObj = action.action;
					if (actionObj)
						id = actionObj.asset;
				}
				if (!id) {
					id = plugin._type || 'none';
				}
				if (id) {
					TelemetryService.interact(type, id, type).ext(ext).flush();
				}
			}
		}
	}
}