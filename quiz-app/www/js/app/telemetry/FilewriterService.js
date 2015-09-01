FilewriterService = Class.extend({
	init: function() {
		this.initWriter();
	},
	initWriter: function() {
		throw "Subclasses of FilewriterService should implement this function";
	},
	createBaseDirectory: function(dirName, error) {
		throw "Subclasses of FilewriterService should implement this function";
	},
	createFile: function(fileName, success, error) {
        throw "Subclasses of FilewriterService should implement this function";
    },
    write: function(fileName, data, success, error) {
		throw "Subclasses of FilewriterService should implement this function";
    },
    length: function(fileName) {
    	throw "Subclasses of FilewriterService should implement this function";
    },
    getData: function(fileName) {
    	throw "Subclasses of FilewriterService should implement this function";	
    }
});
