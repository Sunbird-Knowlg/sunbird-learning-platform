var ItemDataProvider = BaseDataProvider.extend({
    initDataProvider: function(data) {
		DataProviderManager.registerDataProviderInstance(id, this);
		this._data = data;
		this._loaded = true;
		var items = data.items;
		var model = [];
		items.forEach(function(set) {
			if (_.isArray(set)) {
				model = _.union(model, set);	
			}
		});
		if (data.total_items && model.length > data.total_items) {
			model = _.first(model, data.total_items);
		}
		this._model = model;
		this._repeat = this._model.length;
	},
	evalItem: function() {

    },
    eval: function() {

    }
});
DataProviderManager.registerDataProvider('items', ItemDataProvider);
