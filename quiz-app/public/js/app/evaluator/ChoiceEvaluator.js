MCQEvaluator = {
	evaluate: function(item) {
		var result = {};
		var pass = true;
		var score = 0;
		if (item) {
			var options = item.options;
			if (_.isArray(options)) {
				options.forEach(function(opt) {
					if (opt.answer === true) {
						if (!opt.selected) {
							pass = false;
						} else if (_.isFinite(opt.score)){
							score += opt.score || 1;
						}
					} else {
						if (opt.selected === true) {
							pass = false;	
						}
					}
				});
			}
			if (!pass) {
				result.feedback = item.feedback;
				if (!item.partial_scoring) {
					score = 0;
				}
			}
		}
		result.pass = pass;
		result.score = score;
		return result;
	}
};