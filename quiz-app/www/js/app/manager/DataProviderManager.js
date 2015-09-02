DataProviderManager = {
	dataProviderMap: {},
	instanceMap: {},
	errors: [],
	registerDataProvider: function(type, dataProvider) {
		DataProviderManager.dataProviderMap[type] = dataProvider;
	},
	isDataProvider: function(type) {
		if(DataProviderManager.dataProviderMap[type]) {
			return true;
		} else {
			return false;
		}
	},
	get: function(id, baseDir) {
		var d,
			dataProviderMap = DataProviderManager.dataProviderMap;
		var tokens = id.split('.');
		if (tokens.length == 2) {
			var type = tokens[0];
			if(!dataProviderMap[type]) {
				DataProviderManager.addError('No Data Provider found for - ' + type);
			} else {
				d = DataProviderManager.getDataProviderInstance(id);
				if (!d) {
					d = new dataProviderMap[type](baseDir, id);
					while (!d._loaded || !d._error) {
						// repeat the loop
						if (d._error) {
							d = undefined;
							break;
						}
					}
				}
			}
		}
		return d;
	},
	registerDataProviderInstance: function(id, instance) {
		DataProviderManager.instanceMap[id] = instance;
	},
	getDataProviderInstance: function(id) {
		return DataProviderManager.instanceMap[id];
	},
	addError: function(error) {
		DataProviderManager.errors.push(error);
	},
	getErrors: function() {
		return DataProviderManager.errors;
	}
}