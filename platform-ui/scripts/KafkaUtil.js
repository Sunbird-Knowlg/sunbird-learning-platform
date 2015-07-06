var kafka = require('kafka-node'),
    Producer = kafka.Producer,
    client = new kafka.Client(),
    producer = new Producer(client);

exports.register = function(cb) {
	producer.on('ready', function () {
		console.log("## Ready...");
		cb();
	});
}

producer.on('error', function (err) {
	console.log("## Kafka Producer error - ", err);
});

exports.send = function(msgs, topic) {
	//console.log('pushing event to kafka...');
	var tp = topic || 'telemetry';
	producer.send([{topic: tp, messages: msgs, attributes: 1}], function(err, data) {
		if(err) {
			console.log('## Err pushing to kafka topic - ', err);
		}
	});
}

exports.closeClient = function() {
	setTimeout(function() {
		console.log("## Closing kafka client....");
		client.close();
	}, 30000);
}