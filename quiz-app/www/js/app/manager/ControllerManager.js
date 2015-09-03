ControllerManager = {
	controllerMap: {},
	instanceMap: {},
	_loaderMap: {},
	errors: [],
	registerController: function(type, controller) {
		ControllerManager.controllerMap[type] = controller;
	},
	isController: function(type) {
		if(ControllerManager.controllerMap[type]) {
			return true;
		} else {
			return false;
		}
	},
	get: function(id, baseDir) {
		var d,
			controllerMap = ControllerManager.controllerMap;
		var tokens = id.split('.');
		if (tokens.length == 2) {
			var type = tokens[0];
			if(!controllerMap[type]) {
				ControllerManager.addError('No Controller found for - ' + type);
			} else {
				d = ControllerManager.getControllerInstance(id);
				if (!d) {
					d = new controllerMap[type](baseDir, id);
				}
			}
		}
		return d;
	},
	registerControllerInstance: function(id, instance) {
		ControllerManager.instanceMap[id] = instance;
	},
	getControllerInstance: function(id) {
		return ControllerManager.instanceMap[id];
	},
	loadFile: function(baseDir, folder, filename, instance) {
		var loaderMap = ControllerManager._loaderMap;
		var gameMap = {};
		if (loaderMap[baseDir]) {
			gameMap = loaderMap[baseDir];
		} else {
			loaderMap[baseDir] = {};
			gameMap = loaderMap[baseDir];
		}
		var loader;
		if (gameMap[folder]) {
			loader = gameMap[folder];
		} else {
			loader = new createjs.LoadQueue(true, baseDir + "/" + folder + "/");
			gameMap[folder] = loader;
		}
		if (!loader.getResult(filename)) {
			loader.loadManifest({"id": filename, "src" : filename, "type": "json"}, true);
			loader.addEventListener("complete", function() {
				var data = loader.getResult(filename);
				instance.initController(data);
			});
		}
	},
	addError: function(error) {
		ControllerManager.errors.push(error);
	},
	getErrors: function() {
		return ControllerManager.errors;
	}
}