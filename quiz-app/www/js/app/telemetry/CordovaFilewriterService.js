CordovaFilewriterService = FilewriterService.extend({
    _root: undefined,
    initWriter: function() {
        var _resolve;
        var _reject;
        return new Promise(function(resolve, reject) {
            _resolve = resolve;
            _reject = reject;
            document.addEventListener("deviceready", function() {
                window.requestFileSystem(LocalFileSystem.PERSISTENT, 0, function(fileSystem) {
                    _root = fileSystem.root;
                    _resolve(fileSystem.root);
                }, function(e) {
                    console.log('[ERROR] Problem setting up root filesystem for running! Error to follow.');
                    console.log(JSON.stringify(e));
                    _reject(e);
                });
            });
        });
    },
    createBaseDirectory: function(dirName, error) {
        _root.getDirectory(dirName, {
            create: true
        }, function(dirEntry) {
            console.log(dirEntry.name + ' created successfully.');
        }, error);
    },
    createFile: function(fileName, success, error) {
        _root.getFile(fileName, {
            create: true
        }, success, error);
    },
    writeFile: function(fileName, data, revSeek) {
        return new Promise(function(resolve, reject) {
            _root.getFile(fileName, {
                create: false
            }, function(fileEntry) {
                fileEntry.createWriter(function(fileWriter) {
                    if (fileWriter.length > 0 && revSeek) fileWriter.seek(fileWriter.length - revSeek);
                    fileWriter.write(data);
                    fileWriter.onwriteend = function(e) {
                        console.log(fileName + ' write completed...');
                        resolve(true);
                    };
                    fileWriter.onerror = function(e) {
                        reject(e);
                    };
                }, function(e) {
                    reject(e);
                });
            }, function(e) {
                reject(e);
            });
        });
    },
    getFileLength: function(fileName) {
        return new Promise(function(resolve, reject) {
            _root.getFile(fileName, {
                create: false
            }, function(fileEntry) {
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