GenieService = {
	sendTelemetry: function(string) {
		return new Promise(function(resolve, reject) {
			console.log(string);
			resolve(true);
		});
	},
	getCurrentUser: function() {
		return new Promise(function(resolve, reject) {
			var result = {};
			result.status = "success";
			result.data = {"avatar":"resource1","gender":"male","handle":"handle1","uid":"8hjh3c4b7b47d570df0ec286bf7adc8ihhnjy","age":6,"standard":-1};
			resolve(result);
		});	
	}
}