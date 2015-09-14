var audiosprite = require('audiosprite');
var parser = require('xml2json');

var basePath = process.argv[2];
var fileArray = process.argv[3].split(',');//['file1.mp3', 'file2.mp3']
var files = [];
fileArray.forEach(function(file) {
	files.push(basePath + '/' + file);
});
console.log('files', files);
var opts = {output: process.argv[4], format: 'createjs'};

audiosprite(files, opts, function(err, obj) {
  	if (err) return console.error(err);
	obj.type = 'audiosprite';
	var json = {media: obj};
  	console.log(parser.toXml(json));
})