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
                resolve({"status": "ready", "baseDir": content.launchPath, "error": ""});
            });
        } else {
            console.log("progress content:", content);
            return new Promise(function(resolve, reject) {
                DownloaderService.getZipFile(content)
                .then(function(data) {
                    if(data.status = "success") {
                        return DownloaderService.extract(data.zipFile, DownloaderService.appDataDirectory + content.id);
                    } else {
                        return data;
                    }
                })
                .then(function(data) {
                    resolve(data);
                });
            });
        }
	},
    getZipFile: function(content) {
        return new Promise(function(resolve, reject) {
                var contentId = content.id;
                var basePath = DownloaderService.appDataDirectory;
                var downloadUrl = content.downloadUrl || "https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/addition_by_grouping_1441865816499.zip";
                var zipName = downloadUrl.substring(downloadUrl.lastIndexOf("/")+1, downloadUrl.length);
                var expZipPath = basePath.replace(DownloaderService._root.nativeURL, "") + zipName;
                DownloaderService._root.getFile(expZipPath, {create: false}, function(fileEntry) {
                    console.log("fileEntry: ", fileEntry);
                    resolve({"status": "success", "zipFile": fileEntry, "error": ""});
                }, function(err) {
                    console.log("zip file not found at "+ expZipPath + ". So, downloading...");
                    var fileTransfer = new FileTransfer();
                    var downloadPath = basePath + contentId + '.zip';
                    console.log("started downloading to "+ downloadPath);
                    fileTransfer.download(downloadUrl, downloadPath, function(theFile) {
                        console.log("download complete: " + theFile.toURL());
                        resolve({"status": "success", "zipFile": theFile, "error": ""});
                    }, function(error) {
                        console.log("download error: ", JSON.stringify(error));
                        resolve({"status": "error", "zipFilePath": "", "error": "error while downloading: error:"+ JSON.stringify(error)});
                    }, true);
                });
            });
    },
    extract: function(zipFile, extractPath) {
        return new Promise(function(resolve, reject) {
            zip.unzip(zipFile.toURL(), extractPath, function(status) {
                if(status == 0) {
                    console.log("unzip successful.");
                    resolve({"status": "ready", "baseDir": extractPath, "error": "", "appIcon": extractPath + "/logo.png"});
                    zipFile.remove();
                } else if(stauts == -1) {
                    console.log("error while unzipping.");
                    resolve({"status": "error", "baseDir": "", "error": "error while unzipping."});
                }
            }, function (progressEvent) {
              // console.log(progressEvent.loaded + " of " + progressEvent.total);
            });
        });
    }
}

DownloaderService.init();
