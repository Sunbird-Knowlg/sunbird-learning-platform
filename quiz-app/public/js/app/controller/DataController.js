var DataController = Controller.extend({
	initController: function(baseDir, type, id) {
		DataGenerator.loadData(baseDir, type, id, this);
	},
	onLoad: function(data) {
		if (data) {
			ControllerManager.registerControllerInstance(this._id, this);
			this._data = data;
			this._loaded = true;
			if (data.model) {
				this._model = data.model;
			} else {
				this._model = data;
			}
			if (_.isArray(this._model)) {
				this._repeat = this._model.length;
			} else {
				this._repeat = 1;
			}
		} else {
			this._error = true;
		}
	}
});
ControllerManager.registerController('data', DataController);
