PlatformService = {
	getJSON: function(jsonFile) {
		return new Promise(function(resolve, reject) {
			$.getJSON('json/' + jsonFile, function(data) {
				resolve(data);
			})
			.fail(function(err) {
				reject(err);
			});
		});
	},
	getContentList: function() {
		var content = {};
		return new Promise(function(resolve, reject) {
			PlatformService.getJSON('stories.json')
			.then(function(stories) {
				content['Story'] = stories;
			})
			.then(function() {
				return PlatformService.getJSON('worksheets.json')
			})
			.then(function(worksheets) {
				content['Worksheet'] = worksheets;
				resolve(content);
			})
			.catch(function(err) {
				reject(err);
			})
		})
	}
}