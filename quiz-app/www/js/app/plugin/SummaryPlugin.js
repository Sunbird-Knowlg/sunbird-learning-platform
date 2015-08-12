var SummaryPlugin = Plugin.extend({
	_isContainer: false,
	initPlugin: function(data) {

		var total = 0;
		var score = 0;
		var assessmentData = this._theme._assessmentData;
		for (stage in assessmentData) {
			var scores = assessmentData[stage];
			for (index in scores) {
				total += 1;
				score += scores[index];
			}
		}
		if (total <= 0) {
			total = 1;
		}
		var percent = parseInt((score/total) * 100);
		var dataItems = this._theme.getAsset(data.datasource);
		var summary = dataItems.summary;
		var message = undefined;
		summary.forEach(function(range) {
			if (!message) {
				var min = 0;
				var max = 100;
				if (range.range) {
					if (range.range.min) {
						min = range.range.min;
					}
					if (range.range.max) {
						max = range.range.max;
					}
				}	
				if (percent >= min && percent <= max) {
					message = range.message;
				}
			}
		});
		if (message) {
			if (message.type == 'text') {
				this.renderTextSummary(message.asset, data);
			}
		}
	},
	renderTextSummary: function(text, data) {
		data.$t = text;
		pluginManager.invoke('text', data, this._parent, this._stage, this._theme);
	}
});
pluginManager.registerPlugin('summary', SummaryPlugin);