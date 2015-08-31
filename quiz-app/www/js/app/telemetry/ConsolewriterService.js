ConsolewriterService = FilewriterService.extend({
    _root: undefined,
    _data: "",
    initWriter: function() {
        _data = "";
    },
    createBaseDirectory: function(dirName, error) {
    },
    createFile: function(fileName, success, error) {
    },
    writeFile: function(fileName, data, revSeek) {
        return new Promise(function(resolve, reject) {
            if(_data.length > 0 && revSeek) _data = _data.substring(0, _data.length - revSeek);
            _data += data;
            console.log(fileName + ' write completed...');
            console.log('Data:', _data);
            resolve(true);
        });
    },
    getFileLength: function(fileName) {
        return new Promise(function(resolve, reject) {
            resolve(_data.length);
        });
    }
});

function onRequestFileSystemError(e) {
    console.log('[ERROR] Problem setting up root filesystem for running! Error to follow.');
    console.log(JSON.stringify(e));
};