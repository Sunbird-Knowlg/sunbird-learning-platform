var exec = require('cordova/exec');

function DownloaderService() {
}
DownloaderService.prototype.showToast = function(aString) {
    console.log("DownloaderService.js: showToast");
    exec(function(result) {
            console.log("OK: " + result);
        },
        function(error) {
            console.log("Error: " + error);
        },
        "DownloaderService", "showToast", [aString]);
}

DownloaderService.prototype.process = function(content) {
    return new Promise(function(resolve, reject) {
        exec(function(result) {
            resolve(result);
        }, function(error) {
            reject(error);
        }, "DownloaderService", "process", [content.launchPath]);
    });
}

var downloaderService = new DownloaderService();
module.exports = downloaderService;