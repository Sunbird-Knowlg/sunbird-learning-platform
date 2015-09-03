TelemetryServiceUtil = {
	_config: undefined,
	getConfig: function() {
		return new Promise(function(resolve, reject) {
			if(TelemetryServiceUtil._config) {
				resolve(TelemetryServiceUtil._config);
			} else {
				$.getJSON('json/telemetryConfig.json', {}, function(data) {
					if(data) {
						if (typeof data === 'string') {
							TelemetryServiceUtil._config = JSON.parse(data);
						} else {
							TelemetryServiceUtil._config = data;
						}
						console.log('Telemetry Config init completed...');
						resolve(TelemetryServiceUtil._config);
					} else {
						reject(null);
					}
				})
				.error(function(err) {
					console.log('Error:', err);
					reject(err);
				});
			}
		});
	}
}