var DataProvider = BaseDataProvider.extend({
	initDataProvider: function(data) {
		DataProviderManager.registerDataProviderInstance(id, this);
		this._data = data;
		this._loaded = true;
		this._model = data;
		if (_.isArray(data)) {
			this._repeat = data.length;
		}
	}
});
DataProviderManager.registerDataProvider('data', DataProvider);
