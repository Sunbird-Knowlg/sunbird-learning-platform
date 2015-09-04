ControllerManager = {
	controllerMap: {},
	instanceMap: {},
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
	get: function(type, id, baseDir) {
		var d,
			controllerMap = ControllerManager.controllerMap;
		if (type && id) {
			if(!controllerMap[type]) {
				ControllerManager.addError('No Controller found for - ' + type);
			} else {
				var controllerId = type + '.' + id;
				d = ControllerManager.getControllerInstance(controllerId);
				if (!d) {
					d = new controllerMap[type](baseDir, type, id);
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
	addError: function(error) {
		ControllerManager.errors.push(error);
	},
	getErrors: function() {
		return ControllerManager.errors;
	}
}