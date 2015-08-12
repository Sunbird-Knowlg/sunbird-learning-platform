var parser = require('xml2json'),
    fs = require('fs');

exports.xml2json = function(input, output) {
    fs.readFile(input, function(err, data) {
        if (!err) {
            var json = parser.toJson(data, {sanitize: false});
            fs.writeFileSync(output, json);
        }
    });
}

exports.xml2json('../www/theme/sample.xml', '../www/json/sample3.json');