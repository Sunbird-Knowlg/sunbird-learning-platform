FilewriterService = Class.extend({
	_root: undefined,
	init: function() {},
	initWriter: function(initObj) {
		throw "Subclasses of FilewriterService should implement this function";
	},
	createBaseDirectory: function(dirName, error) {
		throw "Subclasses of FilewriterService should implement this function";
	},
	createFile: function(fileName, success, error) {
        throw "Subclasses of FilewriterService should implement this function";
    },
    writeFile: function(fileName, data, success, error) {
		throw "Subclasses of FilewriterService should implement this function";
    }
});
