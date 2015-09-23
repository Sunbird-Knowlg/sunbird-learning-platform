IntentService = {
	sendResult: function(param, value) {
		return new Promise(function(resolve, reject) {
			resolve(true);
		});
	},
	sendTelemetryEvents: function(telemetry) {
		return new Promise(function(resolve, reject) {
			console.log('sending telemetry: ', telemetry);
			resolve(true);
		});
	}
}