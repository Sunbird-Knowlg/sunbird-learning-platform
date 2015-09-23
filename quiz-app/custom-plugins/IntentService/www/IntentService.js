var exec = require('cordova/exec');

function IntentService() {
}

IntentService.prototype.sendResult = function(param, value) {
    console.log("IntentService.js: sendResult");
    return new Promise(function(resolve, reject) {
        exec(function(result) {
                console.log("OK: " + result);
                resolve(true);
            },
            function(error) {
                console.log("Error: " + error);
                resolve(true);
            },
            "IntentService", "sendResult", [param, value]);
    });
}

IntentService.prototype.sendTelemetryEvents = function(telemetry) {
    console.log("IntentService.js: sendTelemetryEvents");
    var param = 'events';
    console.log('telemetry: ', telemetry);
    return this.sendResult(param, telemetry);
}


var intentService = new IntentService();
module.exports = intentService;