var DataGenerator = {
	_loaderMap: {},
	loadData: function(baseDir, type, id, controller) {
		var folder = type;
		var filename = id + '.json';
		var loaderMap = DataGenerator._loaderMap;
		var loader;
		if (loaderMap[baseDir]) {
			loader = loaderMap[baseDir];
		} else {
			loader = new createjs.LoadQueue(true, baseDir + "/" + folder + "/");
			loaderMap[baseDir] = loader;
		}
		if (!loader.getResult(filename)) {
			loader.loadManifest({"id": filename, "src" : filename, "type": "json"}, true);
			loader.addEventListener("complete", function() {
				DataGenerator._onLoad(loader, filename, controller);
			});
		} else {
			DataGenerator._onLoad(loader, filename, controller);
		}
	},
	_onLoad: function(loader, filename, controller) {
		var data = loader.getResult(filename);
		controller.onLoad(data);
	}
}
