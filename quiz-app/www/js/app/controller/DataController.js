var DataController = Controller.extend({
	initController: function(data) {
		if (data) {
			ControllerManager.registerControllerInstance(this._id, this);
			this._data = data;
			this._loaded = true;
			this._model = data;
			if (_.isArray(data)) {
				this._repeat = data.length;
			} else {
				this._repeat = 1;
			}	
		} else {
			this._error = true;
		}
	}
});
ControllerManager.registerController('data', DataController);
