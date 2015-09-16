var exec = require('cordova/exec');

function PlatformService() {
}
PlatformService.prototype.showToast = function(aString) {
    console.log("PlatformService.js: showToast");
    exec(function(result) {
            console.log("OK: " + result);
        },
        function(error) {
            console.log("Error: " + error);
        },
        "PlatformService", "showToast", [aString]);
}

PlatformService.prototype.getContentList = function() {
    return new Promise(function(resolve, reject) {
        exec(function(result) {
            console.log("REST result:", result);
            resolve(result);
        }, function(error) {
            console.log("REST error:", result);
            reject(error);
        }, "PlatformService", "getContentList", ["Story", "Worksheet"]);
    });
}

var platformService = new PlatformService();
module.exports = platformService;