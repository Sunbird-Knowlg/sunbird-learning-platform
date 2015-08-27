CordovaFilewriterService = FilewriterService.extend({
	_root: undefined,
	initWriter: function() {
		document.addEventListener("deviceready", function() {
            window.requestFileSystem(LocalFileSystem.PERSISTENT, 0, function (fileSystem) {
                _root = fileSystem.root;
                console.log('this._root:', _root);
            }, onRequestFileSystemError);
        });
	},
	createBaseDirectory: function(dirName, error) {
		_root.getDirectory(dirName, {create: true}, function(dirEntry) {
			console.log(dirEntry.name + ' created successfully.');
		}, error);
	},
	createFile: function(fileName, success, error) {
        _root.getFile(fileName, {create : true}, success, error);
    },
    writeFile: function(fileName, data, seekDiff, success, error) {
		_root.getFile(fileName, {create : false}, function(fileEntry) {
			fileEntry.createWriter(function(fileWriter) {
				if(fileWriter.length > 0 && seekDiff) fileWriter.seek(fileWriter.length - seekDiff);
				fileWriter.write(data);
				success();
			}, error);	
		}, error);
    },
    getFileLength: function(fileName) {
    	return new Promise(function(resolve, reject) {
    		_root.getFile(fileName, {create : false}, function(fileEntry) {
				fileEntry.createWriter(function(fileWriter) {
					resolve(fileWriter.length);
				}, function() {
					reject('Error while creating writer.');
				});	
			}, function() {
				reject('Error while getting file.');
			});
    	});
    }
});

function onRequestFileSystemError(e) {
    console.log('[ERROR] Problem setting up root filesystem for running! Error to follow.');
    console.log(JSON.stringify(e));
};