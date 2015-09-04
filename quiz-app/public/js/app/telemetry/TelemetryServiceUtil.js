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

// use a index to insert relative to the end or middle of the string.
String.prototype.insert = function(index, string) {
    var ind = index < 0 ? this.length + index : index;
    return this.substring(0, ind) + string + this.substring(ind, this.length);
};

// Generate Genie format ts as per Telemetry wiki
// https://github.com/ekstep/Common-Design/wiki/Telemetry
// YYYY-MM-DDThh:mm:ss+/-nn:nn
function toGenieDateTime(ms) {
    var v = dateFormat(new Date(ms), "yyyy-mm-dd'T'HH:MM:ssZ").replace('GMT', '');
    return v.insert(-2, ':');
}