TelemetryServiceUtil = {
	_config: undefined,
	getConfig: function() {
		return TelemetryServiceUtil._config;
	},
	initConfig: function() {
		$.get('json/telemetryConfig.json', {}, function(data) {
			TelemetryServiceUtil._config = JSON.parse(data);
			console.log('Telemetry Config init completed...');
		});
	}
}

TelemetryServiceUtil.initConfig();