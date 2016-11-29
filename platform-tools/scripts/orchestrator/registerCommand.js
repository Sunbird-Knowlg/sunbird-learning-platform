var fs = require('fs');
var readLine = require('readline');
var Client = require('node-rest-client').Client;
var client = new Client();
//Sandbox
//var host = "http://lp-sandbox.ekstep.org:8080/taxonomy-service";
//Old Prod
//var host = "http://52.221.228.121:8080/learning-service";
//var host = "http://localhost:8080/learning-service";
// PROD
//var host = "https://api.ekstep.in/learning";
// DEV
var host = "https://dev.ekstep.in/api/learning";
// QA
//var host = "https://qa.ekstep.in/api/learning";

process.env.NODE_TLS_REJECT_UNAUTHORIZED = "0";
console.log('');
var scriptAsString = fs.readFileSync(process.argv[2]);
var script = JSON.parse(scriptAsString);
var args = {
    headers: {
        "Content-Type": "application/json",
        "user-id": "analytics"
    },
    rejectUnauthorized: false, 
    data: script
};
if(process.argv[3] == 'register') {
	client.post(host + "/v1/orchestrator/register/command", args, function (data, response) {
		console.log('######## Register command - Response ########');
		console.log(data);
		console.log('############################################');
		console.log('');
		loadCommands();
	});
} else {
	client.patch(host + "/v1/orchestrator/update/command/" + script.name, args, function (data, response) {
		console.log('########  Update command - Response  ########');
		console.log(data);
		console.log('############################################');
		console.log('');
		loadCommands();
	});
}


function loadCommands() {
	var args = {
        headers: {
            "Content-Type": "application/json",
            "user-id": "analytics"
        }
    };
	client.get(host + "/v1/orchestrator/load/commands", args, function (data, response) {
		console.log('########  Load Commands - Response  ########');
		console.log(data);
		console.log('############################################');
		console.log('');
	});
}