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
		var result = {"data": []};
		return new Promise(function(resolve, reject) {
			PlatformService.getJSON('stories.json')
			.then(function(stories) {
				if (stories && stories.result && stories.result.content) {
					if (stories.result.content == null) {
						stories.result.content = [];
					}
					for(i=0;i < stories.result.content.length; i++) {
                    	var item = stories.result.content[i];
                    	item.type = 'story';
                    	result.data.push(item);
                	}
				}
			})
			.then(function() {
				return PlatformService.getJSON('worksheets.json')
			})
			.then(function(worksheets) {
				if (worksheets && worksheets.result && worksheets.result.content) {
					if (worksheets.result.content == null) {
						worksheets.result.content = [];
					}
					for(i=0;i<worksheets.result.content.length; i++) {
                    	var item = worksheets.result.content[i];
                    	item.type = 'worksheet';
                    	result.data.push(item);
                	}
                }
				resolve(result);
			})
			.catch(function(err) {
				reject(err);
			})
		})
	}
}