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
		return new Promise(function(resolve, reject) {
			resolve({"status": "ready", "baseDir": content.launchPath, "error": ""});
		});
	},
	test: function() {
		var contentId = "story.anual.haircut.day";
		var downloadUrl = "https://ekstep-public.s3-ap-southeast-1.amazonaws.com/content/addition_by_grouping_1441790765933.zip";
		DownloaderService._root.getDirectory(TelemetryService._baseDir, {
                create: false
            }, function(fileEntry) {
            	console.log("fileEntry.toURL(): ", fileEntry.toURL());
                var fileTransfer = new FileTransfer();
                var downloadPath = fileEntry.toURL() + contentId + '.zip';
                console.log("starting download. downloading to "+ downloadPath);
                fileTransfer.download(downloadUrl, downloadPath, function(theFile) {
                    console.log("download complete: " + theFile.toURL());
                    zip.unzip(theFile.toURL(), fileEntry.toURL() + contentId, function(status) {
                        if(status == 0)
                            console.log("unzip successful.");
                        else if(stauts = 1)
                            console.log("error while unzipping.");
                    }, function (progressEvent) {
                      console.log(progressEvent.loaded + " of " + progressEvent.total);
                    });
                }, function(error) {
                    console.log("download error source " + error.source);
                    console.log("download error target " + error.target);
                    console.log("upload error code: " + error.code);
                }, true);
            });

	}
}

DownloaderService.init();
