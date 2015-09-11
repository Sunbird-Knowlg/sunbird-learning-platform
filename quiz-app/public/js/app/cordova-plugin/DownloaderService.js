DownloaderService = {
	_root: undefined,
	init: function() {
		document.addEventListener("deviceready", function() {
            window.requestFileSystem(LocalFileSystem.PERSISTENT, 0, function(fileSystem) {
                DownloaderService._root = fileSystem.root;
            }, function(e) {
                console.log('[ERROR] Problem setting up root filesystem for running! Error to follow.');
                console.log(JSON.stringify(e));
            });
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
                console.log("progress in promise content:", content);
                var contentId = content.id;
                var downloadUrl = content.downloadUrl || "https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/addition_by_grouping_1441865816499.zip";
                var fileTransfer = new FileTransfer();
                var basePath = cordova.file.externalDataDirectory || cordova.file.dataDirectory;
                var downloadPath = basePath + contentId + '.zip';
                console.log("starting download. downloading to "+ downloadPath);
                fileTransfer.download(downloadUrl, downloadPath, function(theFile) {
                    console.log("download complete: " + theFile.toURL());
                    console.log("Type:", theFile.type);
                    zip.unzip(theFile.toURL(), basePath + contentId, function(status) {
                        if(status == 0) {
                            console.log("unzip successful.");
                            resolve({"status": "ready", "baseDir": basePath + contentId, "error": "", "appIcon": basePath + contentId+ "/logo.png"});
                        } else if(stauts = -1) {
                            console.log("error while unzipping.");
                            resolve({"status": "error", "baseDir": "", "error": "error while unzipping."});
                        }
                        theFile.remove();
                    }, function (progressEvent) {
                      // console.log(progressEvent.loaded + " of " + progressEvent.total);
                    });
                }, function(error) {
                    console.log("download error source " + error.source);
                    console.log("download error target " + error.target);
                    console.log("upload error code: " + error.code);
                    resolve({"status": "error", "baseDir": "", "error": "error while downloading: error code:"+ error.code});
                }, true);
            });
        }
	},
	test: function(content) {
        var basePath = cordova.file.externalDataDirectory;
		zip.unzip(basePath + 'test.tar.gz', basePath, function(status) {
            if(status == 0)
                console.log("unzip successful.");
            else if(stauts = 1)
                console.log("error while unzipping.");
            theFile.remove();
        }, function (progressEvent) {
          console.log(progressEvent.loaded + " of " + progressEvent.total);
        });
	}
}

DownloaderService.init();
