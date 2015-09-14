var DataGenerator = {
	_loaderMap: {},
	loadData: function(baseDir, type, id, controller) {
		var folder = type;
		var filename = id + '.json';
		var fullPath = baseDir + "/" + folder + "/" + filename;
		$.getJSON(fullPath, function(data) {
			DataGenerator._onLoad(data, controller);
		}).fail(function() {
			console.log("error while fetching json: "+fullPath);
		});
	},
	_onLoad: function(data, controller) {
		controller.onLoad(data);
	}
}
