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

PlatformService.prototype.setAPIEndpoint = function(aString) {
    console.log("PlatformService.js: setAPIEndpoint");
    exec(function(result) {
            console.log("OK: " + result);
        },
        function(error) {
            console.log("Error: " + error);
        },
        "PlatformService", "setAPIEndpoint", [aString]);
}

PlatformService.prototype.getContentList = function() {
    return new Promise(function(resolve, reject) {
        exec(function(resp) {
            console.log("REST response:", resp);
            var result = {};
            for(key in resp) {
                var contentResponse = resp[key];
                console.log("contentResponse response:", contentResponse);
                console.log("contentResponse status:", contentResponse.status);
                if(contentResponse.status == "success") {
                    var data = (typeof contentResponse.data == 'string') ? JSON.parse(contentResponse.data) : contentResponse.data;
                    if(result.data == null) result.data = [];
                    for(i=0;i<data.result.content.length; i++) {
                        var item = data.result.content[i];
                        item.type = key.toLowerCase();
                        result.data.push(item);
                    }
                } else {
                    result["status"] = "error";
                    result["errorCode"] = contentResponse.errorCode;
                    result["errorParam"] = contentResponse.errorParam;
                }
            }
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