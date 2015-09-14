var ItemDataGenerator = {
	_loaderMap: {},
	loadData: function(baseDir, type, id, controller) {
		var folder = type;
		var filename = id + '.json';
		var fullPath = baseDir + "/" + folder + "/" + filename;
		$.getJSON(fullPath, function(data) {
			ItemDataGenerator._onLoad(data, controller);
		}).fail(function() {
			console.log("error while fetching json: "+ fullPath);
		});
	},
	_onLoad: function(data, controller) {
		var model = ItemDataGenerator._getItems(data);
		data = _.omit(data, 'items');
		controller.onLoad(data, model);
	},
    _getItems: function(data) {
    	var list = [];
    	if (_.isObject(data)) {
    		var total_items = data.total_items;
			var item_sets = data.item_sets;
			var items = data.items;
			if (item_sets && items) {
				item_sets.forEach(function(map) {
					list = ItemDataGenerator._addItems(map.id, map.count, items, list);
				});
				if (total_items && list.length > total_items) {
					list = _.first(list, total_items);
				}
			}
    	}
		return list;
	},
	_addItems: function(id, count, items, list) {
		var set = items[id];
		if (_.isArray(set)) {
			var indexArr = [];
			for(var i = 0; i < set.length; i++)
				indexArr[i] = i;
			if(set.length < count)
				count = set.length;

			var pick = [];
			for(var i = 0; i < count; i++) {
				var randNum = _.random(0,indexArr.length-1);					
				pick[i] = set[indexArr[randNum]];
				indexArr[randNum] = indexArr[indexArr.length - 1];
				indexArr.splice(indexArr.length - 1, 1);
			}
			list = _.union(list, pick);
		}
		return list;
	}
}
