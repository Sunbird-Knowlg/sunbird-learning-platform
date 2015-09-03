var audiosprite = require('audiosprite'),
	parser = require('xml2json'),
	fs = require('fs'),
	_ = require('underscore');

var basePath = process.argv[2].replace('markup.xml', '');

var markup = fs.readFileSync(process.argv[2]);
var markupJson = JSON.parse(parser.toJson(markup, {sanitize: false}));
var audios = _.where(markupJson.theme.manifest.media, {type: 'sound'});
var stageAudios = {};
_.each(markupJson.theme.stage, function(stage) {
	for(k in stage) {
		if(k == 'audio') {
			stageAudios[stage.id] = [];
			if(!_.isArray(stage[k])) stage[k] = [stage[k]];
			stage[k].forEach(function(audioAsset) {
				stageAudios[stage.id].push(_.findWhere(audios, {id:audioAsset.asset}));
			})
		}
	}
});
//console.info('stageAudios', stageAudios);

for(k in stageAudios) {
	var opts = {output: basePath + 'assets/sounds/' + k, format: 'createjs'};
	var files = [];
	var audioFiles = stageAudios[k];
	if(audioFiles.length > 0) {
		audioFiles.forEach(function(audioFile) {
			files.push(basePath + 'assets/' + audioFile.src);
		});
		audiosprite(files, opts, function(err, obj) {
		  	if (err) return console.error(err);
		  	obj.id = k + '_audio';
		  	obj.type = 'audiosprite';
		  	var json = {media: obj};
		  	console.log(parser.toXml(json));
		  	console.log('');
		})
	}
}