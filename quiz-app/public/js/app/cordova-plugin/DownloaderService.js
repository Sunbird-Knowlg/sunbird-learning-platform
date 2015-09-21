DownloaderService = {
    appDataDirectory: undefined,
	_root: undefined,
	init: function() {
		document.addEventListener("deviceready", function() {
            window.requestFileSystem(LocalFileSystem.PERSISTENT, 0, function(fileSystem) {
                DownloaderService._root = fileSystem.root;
            }, function(e) {
                console.log('[ERROR] Problem setting up root filesystem for running! Error to follow.');
                console.log(JSON.stringify(e));
            });
            DownloaderService.appDataDirectory = cordova.file.externalDataDirectory || cordova.file.dataDirectory;
        });
	},
	process: function(content) {
		if(typeof cordova == 'undefined') {
            return new Promise(function(resolve, reject) {
                resolve({"status": "ready", "baseDir": content.launchPath, "errorCode": ""});
            });
        } else {
            return new Promise(function(resolve, reject) {
                DownloaderService.checkFile(content.identifier)
                .then(function(file) {
                    if(file) {
                        return file;
                    } else {
                        return DownloaderService.download(content);
                    }
                })
                .then(function(file) {
                    return DownloaderService.extract(file, DownloaderService.appDataDirectory + content.identifier);
                })
                .then(function(data) {
                    resolve(data);
                })
                .catch(function(data) {
                    resolve(data);  
                });
            });
        }
	},
    download: function(content) {
        return new Promise(function(resolve, reject) {
                var contentId = content.identifier;
                var basePath = DownloaderService.appDataDirectory;
                var downloadUrl = content.downloadUrl;
                var urlExt = downloadUrl.substring(downloadUrl.lastIndexOf(".")+1, downloadUrl.length);
                var extension = ".zip";
                if(urlExt == "gz") extension = ".tar.gz"; 
                var fileTransfer = new FileTransfer();
                var downloadPath = basePath + contentId + extension;
                console.log("started downloading to "+ downloadPath);
                fileTransfer.download(downloadUrl, downloadPath, function(theFile) {
                    console.log("download complete: " + theFile.toURL());
                    resolve(theFile);
                }, function(error) {
                    console.log("download error: ", JSON.stringify(error));
                    reject({"status": "error", "file": "", "errorCode": "DOWNLOAD_ERROR", "errorParam": JSON.stringify(error)});
                }, true);
            });
    },
    checkFile: function(contentId) {
        return new Promise(function(resolve, reject) {
            var basePath = DownloaderService.appDataDirectory;
            var expZipPath = basePath.replace(DownloaderService._root.nativeURL, "") + contentId + ".zip";
            DownloaderService._root.getFile(expZipPath, {create: false}, function(zipFile) {
                console.log("zip file already exist: "+ zipFile.fullPath);
                resolve(zipFile);
            }, function(zipErr) {
                expZipPath = basePath.replace(DownloaderService._root.nativeURL, "") + contentId + ".tar.gz";
                DownloaderService._root.getFile(expZipPath, {create: false}, function(tarFile) {
                    console.log("tar file already exist: "+ tarFile.fullPath);
                    resolve(tarFile);
                }, function(tarErr) {
                    resolve();
                });
            });
        })
    },
    extract: function(file, extractPath) {
        return new Promise(function(resolve, reject) {
            Extractor.extract(file.toURL(), extractPath, function(result) {
                if(result == "success") {
                    console.log("unzip successful.");
                    resolve({"status": "ready", "baseDir": extractPath, "appIcon": extractPath + "/logo.png"});
                    // file.remove();
                } else {
                    console.log("error while unzipping:", result);
                    resolve({"status": "error", "baseDir": "", "errorCode": result.errorCode, "errorParam": result.errorParam});
                }
            });
        });
    }
}

DownloaderService.init();
