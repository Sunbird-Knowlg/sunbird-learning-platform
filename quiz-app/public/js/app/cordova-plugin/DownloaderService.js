DownloaderService = {
	// getContentService : function() {
	// 	return angular.injector(['quiz.services']).get('ContentService');
	// },
	process: function(content) {
		return new Promise(function(resolve, reject) {
			resolve({"status": "ready", "baseDir": content.launchPath, "error": ""});
		});
	}
}