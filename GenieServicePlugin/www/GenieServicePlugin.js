var exec = require('cordova/exec');

function GenieServicePlugin() {
    console.log("GenieServicePlugin.js: is created");
}
GenieServicePlugin.prototype.showToast = function(aString) {
    console.log("GenieServicePlugin.js: showToast");
    exec(function(result) {
            console.log("OK: " + result);
        },
        function(error) {
            console.log("Error: " + error);
        },
        "GenieServicePlugin", "showToast", [aString]);
}

GenieServicePlugin.prototype.getContentList = function() {
    return new Promise(function(resolve, reject) {
        exec(function(result) {
            resolve(result);
        }, function(error) {
            reject(error);
        }, "GenieServicePlugin", "getContentList", ["stories", "worksheets"]);
    });
}

var genieServicePlugin = new GenieServicePlugin();
module.exports = genieServicePlugin;