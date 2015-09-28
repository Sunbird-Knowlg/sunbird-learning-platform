ConsolewriterService = FilewriterService.extend({
    _root: undefined,
    _data: "",
    _error: "",
    _others: "",
    initWriter: function() {
        _data = _error = _others = "";
        return new Promise(function(resolve, reject) {
            resolve(true);
        });
    },
    createBaseDirectory: function(dirName, error) {},
    createFile: function(fileName, success, error) {},
    write: function(fileName, data, revSeek) {
        return new Promise(function(resolve, reject) {
            if (fileName.endsWith("output.json")) {
                if (_data.length > 0 && revSeek) _data = _data.substring(0, _data.length - revSeek);
                _data += data;
                console.log('Data:', _data);
            } else if (fileName.endsWith("output.json")) {
                if (_error.length > 0 && revSeek) _error = _error.substring(0, _error.length - revSeek);
                _error += data;
                console.log('Data:', _error);
            } else {
                if (_others.length > 0 && revSeek) _others = _data.substring(0, _others.length - revSeek);
                _others += data;
                console.log('Data:', _others);
            }
            console.log(fileName + ' write completed...');
            resolve(true);
        });
    },
    length: function(fileName) {
        return new Promise(function(resolve, reject) {
            resolve(_data.length);
        });
    },
    getData: function(fileName) {
        return new Promise(function(resolve, reject) {
            resolve(_data);
        });
    }
});

function onRequestFileSystemError(e) {
    console.log('[ERROR] Problem setting up root filesystem for running! Error to follow.');
    console.log(JSON.stringify(e));
};