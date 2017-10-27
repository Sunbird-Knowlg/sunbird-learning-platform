// Read Synchrously
var fs = require("fs");
var Client = require('node-rest-client').Client;
var client = new Client();

var fields = ['name', 'apiId', 'version', 'description', 'body', 'type', 'cmdClass', 'parameters', 'requestPath', 'actorPath'];
console.log("\n *START* \n");
var contents;
if(process.argv[2] == null){
	console.log("Please pass proper json file path/name.");
	console.log("Eg: node readsync.js /Users/amitpriyadarshi/software_application/node_js_application/read_json/commands.json");
	process.exit(1);
}else{
	if(process.argv[3] == null){
		console.log("Please pass host name as seocond argument. \n 1. dev : For Dev Environment \n 2. qa : For QA Environment \n 3. localhost : For localhost");
		process.exit(1);
	}else{
		if(process.argv[3] == 'dev'){
			console.log('Using Dev Environment.');
			client.registerMethod("registerCommand", "https://dev.ekstep.in/api/v1/orchestrator/register/command", "POST");
			client.registerMethod("registerScript", "https://dev.ekstep.in/api/v1/orchestrator/register/script", "POST");
		}else if(process.argv[3] == 'qa'){
			console.log('Using Qa Environment.');
			client.registerMethod("registerCommand", "https://qa.ekstep.in/api/v1/orchestrator/register/command", "POST");
			client.registerMethod("registerScript", "https://qa.ekstep.in/api/v1/orchestrator/register/script", "POST");
		}else if(process.argv[3] == 'localhost'){
			console.log('Using Localhost Environment.');
			client.registerMethod("registerCommand", "http://localhost:8080/learning-service/v1/orchestrator/register/command", "POST");
			client.registerMethod("registerScript", "http://localhost:8080/learning-service/v1/orchestrator/register/script", "POST");
		}else{
			console.log("Please pass host name as seocond argument. \n 1. dev : For Dev Environment \n 2. qa : For QA Environment \n 3. localhost : For localhost");
			process.exit(1);
		}
	}
	contents = fs.readFileSync(process.argv[2]);
}
var jsonContent = JSON.parse(contents);
var count = 0;
for(var eachJson in jsonContent){
	var stObj = "{\"name\" : \"\", \"apiId\": \"\", \"version\": \"\", \"description\": \"\", \"body\": \"\", \"type\": \"\", \"cmdClass\": \"\", \"parameters\":[], \"requestPath\":{}, \"actorPath\":{}}";
	var jsonObj = JSON.parse(stObj);

	for(var key in jsonContent[eachJson]){
		if(key == "name"){
			jsonObj.name = jsonContent[eachJson].name;
		}
		if(key == "api_id"){
			jsonObj.apiId = jsonContent[eachJson].api_id ;
		}
		if(key == "version"){
			jsonObj.version = jsonContent[eachJson].version;
		}
		if(key == "description"){
			jsonObj.description = jsonContent[eachJson].description;
		}
		if(key == "body"){
			jsonObj.body = jsonContent[eachJson].body;
		}
		if(key == "type"){
			jsonObj.type = jsonContent[eachJson].type;
		}
		if(key == "command_class"){
			jsonObj.cmdClass = jsonContent[eachJson].command_class;
		}
		if(key == "parameters"){
			var paraCount=0;
			for(var parameter in jsonContent[eachJson].parameters){
				var stPara = "{\"index\" : \"\", \"name\" : \"\", \"datatype\" : \"\", \"routingParam\" : \"\", \"routingId\" : \"\"}";
				var jsonPara = JSON.parse(stPara);
				for(var paraKey in jsonContent[eachJson].parameters[parameter]){
					if(paraKey == "index"){
						jsonPara.index = jsonContent[eachJson].parameters[parameter][paraKey];
					}
					if(paraKey == "name"){
						jsonPara.name = jsonContent[eachJson].parameters[parameter][paraKey];
					}
					if(paraKey == "datatype"){
						jsonPara.datatype = jsonContent[eachJson].parameters[parameter][paraKey];
					}
					if(paraKey == "routing_param"){
						jsonPara.routingParam = jsonContent[eachJson].parameters[parameter][paraKey];
					}
					if(paraKey == "routingId"){
						jsonPara.routingId = jsonContent[eachJson].parameters[parameter][paraKey];
					}
				}
				jsonObj.parameters[paraCount] = jsonPara;
				paraCount = paraCount + 1;
			}
		}
		if(key == "request_path"){
			var stReqPath = "{\"type\" : \"\", \"url\" : \"\", \"pathParams\" : [], \"requestParams\" : [], \"bodyParams\" : []}";
			var jsonReqPath = JSON.parse(stReqPath);
			for(var reqPathparameter in jsonContent[eachJson].request_path){
				if(reqPathparameter == "type"){
					jsonReqPath.type = jsonContent[eachJson].request_path[reqPathparameter];
				}
				if(reqPathparameter == "url"){
					jsonReqPath.url = jsonContent[eachJson].request_path[reqPathparameter];
				}
				if(reqPathparameter == "path_params"){
					var pathParamsCount = 0;
					for(var pathParamsParameter in jsonContent[eachJson].request_path[reqPathparameter]){
						jsonReqPath.pathParams[pathParamsCount] = jsonContent[eachJson].request_path[reqPathparameter][pathParamsParameter];
						pathParamsCount = pathParamsCount + 1;
					}
				}
				if(reqPathparameter == "request_params"){
					var reqParamsCount = 0;
					for(var requestParamsParameter in jsonContent[eachJson].request_path[reqPathparameter]){
						jsonReqPath.requestParams[reqParamsCount] = jsonContent[eachJson].request_path[reqPathparameter][requestParamsParameter];
						reqParamsCount = reqParamsCount + 1;
					}
				}
				if(reqPathparameter == "body_params"){
					var bodyParamsCount = 0;
					for(var bodyParamsParameter in jsonContent[eachJson].request_path[reqPathparameter]){
						jsonReqPath.bodyParams[bodyParamsCount] = jsonContent[eachJson].request_path[reqPathparameter][bodyParamsParameter];
						bodyParamsCount = bodyParamsCount + 1;
					}
				}
			}
			jsonObj.requestPath = jsonReqPath;
		}
		if(key == "actor_path"){
			var stActorPath = "{\"manager\" : \"\", \"operation\" : \"\", \"router\" : \"\"}";
			var jsonActorPath = JSON.parse(stActorPath);
			for(var actorPathparameter in jsonContent[eachJson].actor_path){
				if(actorPathparameter == "manager"){
					jsonActorPath.manager = jsonContent[eachJson].actor_path[actorPathparameter];
				}
				if(actorPathparameter == "operation"){
					jsonActorPath.operation = jsonContent[eachJson].actor_path[actorPathparameter];
				}
				if(actorPathparameter == "router"){
					jsonActorPath.router = jsonContent[eachJson].actor_path[actorPathparameter];
				}
			}
			jsonObj.actorPath = jsonActorPath;
		}
	}
	
 	var args = {
		data: jsonObj,
		headers: { "Content-Type": "application/json" }
	};
	if(jsonObj.type == "COMMAND"){
 		// console.log("args:", JSON.stringify(args));
		client.methods.registerCommand(args, function (data, response) {
    		// parsed response body as js object 
    		// console.log(data);
    	});
		//console.log("*****" + jsonObj.name + "*******");
	}
	if(jsonObj.type == "SCRIPT"){
 		// registering remote methods 
		client.methods.registerScript(args, function (data, response) {
    		// console.log(response);
    	});
	}
//console.log("************" + count + "*************" + jsonObj.name + "*********");
count++;
}
console.log("Total " + count + " scripts/commands have been registered.");
console.log("\n *EXIT* \n");