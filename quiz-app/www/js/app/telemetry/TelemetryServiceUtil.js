TelemetryServiceUtil = {
	_config: undefined,
	getConfig: function() {
		return TelemetryServiceUtil._config;
	},
	initConfig: function() {
		$.get('json/telemetryConfig.json', {}, function(data) {
			if(data) {
				if (typeof data === 'string') {
					TelemetryServiceUtil._config = JSON.parse(data);
				} else {
					TelemetryServiceUtil._config = data;
				}
			}
			console.log('Telemetry Config init completed...');
		});
	}
}

TelemetryServiceUtil.initConfig();