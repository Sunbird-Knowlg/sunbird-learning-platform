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
        ShowLabel = true;
        data = $scope.data;

        data = typeof data != 'object' ? JSON.parse(data) : data;

        if (ShowLabel) {
            var headerFields = [];
            var str = '';
            _.map(data.text_complexity.texts.text1, function(value, key) {
                _.map(value, function(value1, key1) {
                    if (headerFields.indexOf(key1) == -1) {
                        headerFields.push(key1);
                        str = str + key1 + ',';
                    }
                });
            });
            CSV += str + '\r\n';
        }
        _.map(data.text_complexity.texts.text1, function(value, key) {
            var rowData = '';
            for (var i = 0; i < headerFields.length; i++) {
                var val = value[headerFields[i]];
                if (val) {
                    rowData = rowData + val + ',';
                } else {
                    rowData = rowData + ',';
                }
            }
            CSV += rowData + '\r\n';
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
                $scope.url = "https://api.ekstep.in/language/v1/language/tools/textAnalysis";
                $scope.apiUrl = {
                        "request": {
                            "language_id": $scope.lang.id,
                            "texts": {
                                "text1": $scope.inputData
                            }
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
        var mockData = false;
        $scope.apiRequest(mockData).then(function(resp) {
            $scope.data = resp;
            $scope.show = true;
            var str = $scope.inputData;
            var inputArray = str.split(" ");
            var cutOffComplexity = 50;

            var wordMap = $scope.data.text_complexity.texts.text1;
            console.log("wordMap", wordMap);

            map = {};
            _.each(inputArray, function(k, i) {
                var wordVal = inputArray[i];
                wordVal = wordVal.replace(new RegExp('।|,|\\||\\.|;|\\?|!|\\*|।', 'g'), '');
                var wordObj = wordMap[wordVal];
                var tLevel = 10;
                var complexity = 0;
                if (wordObj) {
                    complexity = wordObj['total_complexity'];
                    if (!complexity) {
                        complexity = 0;
                    }
                    tLevel = wordObj['thresholdLevel'];
                    if (!tLevel) {
                        tLevel = 10;
                    }
                }
                if (complexity < cutOffComplexity) {
                    if (tLevel == 1) {
                        processedData = processedData + ' <span class="simpleWord">' + inputArray[i] + ' </span>';
                    } else {
                        processedData = processedData + inputArray[i] + ' ';
                    }
                } else {
                    processedData = processedData + ' <span class="complexWord">' + inputArray[i] + ' </span>';
                }
            });
            $scope.$apply(function() {
                $scope.processedData = processedData;
                $scope.showSubmitBtn = true;
            });
            console.log("processedData :", $scope.processedData);
        });
    }
});
