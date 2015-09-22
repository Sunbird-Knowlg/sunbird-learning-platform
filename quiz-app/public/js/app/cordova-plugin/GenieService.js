GenieService = {
	sendTelemetry: function(string) {
		return new Promise(function(resolve, reject) {
			console.log(string);
			resolve(true);
		});
	}
}