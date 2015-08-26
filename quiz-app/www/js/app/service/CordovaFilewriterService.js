CordovaFilewriterService = FilewriterService.extend({
	init: function() {},
	initWriter: function(initObj) {
		this._root = initObj.root;
	},
	createBaseDirectory: function(dirName, error) {
		this._root.getDirectory(dirName, {create: true}, function(dirEntry) {
			console.log(dirEntry.name + ' created successfully.');
		}, error);
	},
	createFile: function(fileName, success, error) {
        this._root.getFile(fileName, {create : true}, success, error);
    },
    writeFile: function(fileName, data, success, error) {
		this._root.getFile(fileName, {create : false}, function(fileEntry) {
			fileEntry.createWriter(function(fileWriter) {
				fileWriter.seek(fileWriter.length);
				fileWriter.write(data);
				success();
			}, error);	
		}, error);
    }
});