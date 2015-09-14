var exec = require('cordova/exec');

exports.extract = function(fileName, outputDirectory, callback) {
    var win = function(result) {
        console.log("extract success:", result);
        if(callback) {
            callback("success");
        }
    };
    var fail = function(result) {
        console.log("extract fail:", result);
        if(callback) {
            callback("error: "+result);
        }
    }
    exec(win, fail, 'DownloaderService', 'extract', [fileName, outputDirectory]);
}