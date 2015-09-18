MTFEvaluator = {
	evaluate: function(item) {
		var result = {};
		var pass = true;
		var score = 0;
		if (item) {
			var options = item.rhs_options;
			if (_.isArray(options)) {
				options.forEach(function(opt) {
					if (opt.answer === opt.selected) {
						score += opt.score || 1;
					} else {
						pass = false;
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