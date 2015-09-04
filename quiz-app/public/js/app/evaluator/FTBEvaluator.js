FTBEvaluator = {
	evaluate: function(item) {
		var result = {};
		var pass = true;
		var score = 0;
		if (item) {
			var answer = item.answer;
			var model = item.model;
			for (var ans in answer) {
				if (model[ans] && answer[ans].value == model[ans]) {
					score += answer[ans].score;
				} else {
					pass = false;
				}
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
}