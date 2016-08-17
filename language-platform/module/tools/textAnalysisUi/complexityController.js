var app = angular.module('app', ['ngSanitize']);
app.controller('myCtrl', function($scope, $q, $http, $window, $sce) {
    $scope.lang = {};
    $scope.lang.id = "hi";
    $scope.showSubmitBtn = true;
    $scope.languages = [{
        id: "hi",
        name: "Hindi"
    }, {
        id: "ka",
        name: "Kannada"
    }, {
        id: "te",
        name: "Telugu"
    }];

    $scope.show = false;
    $scope.trustAsHtml = function(string) {
        return $sce.trustAsHtml(string);
    };
    var CSV = '';
    $scope.saveToPc = function(data, ShowLabel) {
        CSV = '';
        ShowLabel = true;
        data = $scope.data;

        data = typeof data != 'object' ? JSON.parse(data) : data;

        if (ShowLabel) {
            var headerFields = [];
            headerFields.push('word');
            var str = 'word,';
            _.map(data.text_complexity.wordMeasures, function(value, key) {
                _.map(value, function(value1, key1) {
                    if (headerFields.indexOf(key1) == -1) {
                        headerFields.push(key1);
                        str = str + key1 + ',';
                    }
                });
            });
            CSV += str + '\r\n';
        }
        _.map(data.text_complexity.wordMeasures, function(value, key) {
            var rowData = '';
            var skip = false;
            for (var i = 0; i < headerFields.length; i++) {
                var val;
                if (headerFields[i] == 'word') {
                    val = key;
                    if (val.trim() == '"' || val.trim() == ',') {
                        skip = true;
                    } else {
                        val = val.split('"').join('');
                    }
                } else {
                    val = value[headerFields[i]];
                    if (val === 0)
                        val = '0';
                }
                if (val) {
                    rowData = rowData + val + ',';
                } else {
                    rowData = rowData + ',';
                }
            }
            if (!skip) {
                CSV += rowData + '\r\n';
            }
        });
        if (CSV == '') {
            alert("Invalid data");
            return;
        }
        var link = document.createElement("a");
        link.id = "lnkDwnldLnk";
        document.body.appendChild(link);
        var csv = CSV;
        blob = new Blob([csv], { type: 'text/csv' });
        var csvUrl = window.webkitURL.createObjectURL(blob);
        var filename = 'UserExport.csv';
        $("#lnkDwnldLnk")
            .attr({
                'download': filename,
                'href': csvUrl
            });

        $('#lnkDwnldLnk')[0].click();

    };
    $scope.apiRequest = function(mockData) {
        return new Promise(function(resolve, reject) {
            if (mockData) {
                $scope.data = Input;
                var sampleData = $scope.data;
                return resolve(sampleData);
            }
            else
            {
                var inputData = $scope.inputData.split('\u2028').join('');
                inputData = inputData.split('\u2029').join('');
                $scope.url = "https://api.ekstep.in/language/v1/language/tools/complexityMeasures/text";
                $scope.apiUrl = {
                        "request": {
                            "language_id": $scope.lang.id,
                            "text": inputData
                        }
                }
                var config = {
                    headers: {
                        'Content-Type': 'application/json',
                        'user-id': 'rayuluv'
                    }
                }
                var data = $scope.apiUrl;
                var url = $scope.url;
                $http.post(url, data, [config]).then(function(resp) {
                    if (resp.data.responseCode == 'OK')
                        return resolve(resp.data.result);
                    else
                        return reject(resp);
                });
            }
        });
    }

    $scope.getData = function() {
        $scope.processedData = '';
        $scope.showSubmitBtn = false;
        var processedData = '';
        $scope.apiRequest(false).then(function(resp) {
            if (!resp || resp == null || !resp.text_complexity) {
                $scope.$apply(function() {
                    $scope.showSubmitBtn = true;
                });
                alert('Failed to process the text. Please remove any special characters from the text and try again.');
            } else {
                $scope.data = resp;
                $scope.show = true;
                var str = $scope.inputData;
                var inputArray = str.split(" ");
                var cutOffComplexity = 50;

                var wordMap = $scope.data.text_complexity.wordMeasures;
                console.log("wordMap", wordMap);

                map = {};
                _.each(inputArray, function(k, i) {
                    var wordVal = inputArray[i];
                    wordVal = wordVal.replace(new RegExp('।|,|\\||\\.|;|\\?|!|\\*|।', 'g'), '');
                    var wordObj = wordMap[wordVal];
                    var complexity = 0;
                    if (wordObj) {
                        complexity = wordObj['orthographic_complexity'] + wordObj['phonologic_complexity'];
                        if (!complexity) {
                            complexity = 0;
                        }
                    }
                    if (complexity < cutOffComplexity) {
                        processedData = processedData + inputArray[i] + ' ';
                    } else {
                        processedData = processedData + ' <span class="complexWord">' + inputArray[i] + ' </span>';
                    }
                });
                $scope.$apply(function() {
                    $scope.processedData = processedData;
                    $scope.showSubmitBtn = true;
                });
                console.log("processedData :", $scope.processedData);
            }
        });
    }
});
