angular.module('quiz.services', ['ngResource'])
    .factory('ContentService', ['$window', function($window) {
        var setObject = function(key, value) {
            $window.localStorage[key] = JSON.stringify(value);
        };
        var getObject = function(key) {
            var data = $window.localStorage[key];
            if (data)
                return JSON.parse(data);
            else
                return null;
        };
        var processContent = function(content) {
            content.status = "processing";
            returnObject.saveContent(content);
            DownloaderService.process(content)
                .then(function(data) {
                    for (key in data) {
                        content[key] = data[key];
                    }
                    returnObject.saveContent(content);
                })
                .catch(function(data) {
                    for (key in data) {
                        content[key] = data[key];
                    }
                    returnObject.saveContent(content);
                });
        };
        var returnObject = {
            contentKey: "quizapp-content",
            contentList: {},
            init: function() {
                var data = getObject(this.contentKey);
                if (data) {
                    this.contentList = data;
                } else {
                    this.commit();
                }
            },
            commit: function() {
                setObject(this.contentKey, this.contentList);
            },
            saveContent: function(content) {
                this.contentList[content.identifier] = content;
                this.commit();
            },
            getProcessCount: function() {
                var list = _.where(_.values(this.contentList), {
                        "status": "processing"
                    });
                return list.length;
            },
            getContentList: function(type) {
                if (type) {
                    var list = _.where(_.values(this.contentList), {
                        "type": type,
                        "status": "ready"
                    });
                    return list;
                } else {
                    var list = _.where(_.values(this.contentList), {
                        "status": "ready"
                    });
                    return list;
                }
            },
            getContent: function(id) {
                return this.contentList[id];
            },
            processContent: function(content) {
                var localContent = returnObject.getContent(content.identifier);
                if (localContent) {
                    if ((localContent.status == "ready" && localContent.pkgVersion != content.pkgVersion) || (localContent.status == "error")) {
                        processContent(content);
                    } else {
                        if (localContent.status == "ready")
                            console.log("content: " + localContent.identifier + " is at status: " + localContent.status + " and there is no change in pkgVersion.");
                        else
                            console.log("content: " + localContent.identifier + " is at status: " + localContent.status);
                    }
                } else {
                    processContent(content);
                }
            },
            sync: function() {
                return new Promise(function(resolve, reject) {
                    PlatformService.getContentList()
                    .then(function(contents) {

                        if(contents.data) {
                            for (key in contents.data) {
                                var content = contents.data[key];
                                returnObject.processContent(content);
                            }
                        }

                        if(contents.error) {
                            // TODO: handle error.
                        }

                        // var storyResponse =contents['Story'];
                        // if (storyResponse.status == "success") {
                        //     var stories = (_.isString(storyResponse.data)) ? JSON.parse(storyResponse.data) : storyResponse.data;
                        //     stories = stories.result.content;
                        //     for (key in stories) {
                        //         var story = stories[key];
                        //         story.type = "story";
                        //         returnObject.processContent(story);
                        //     }
                        // } else {
                        //     // TODO: handle error.
                        //     console.log("Story error:", storyResponse);
                        // }
                        // var worksheetResponse = contents['Worksheet'];
                        // if (worksheetResponse.status == "success") {
                        //     var worksheets = (_.isString(worksheetResponse.data)) ? JSON.parse(worksheetResponse.data) : worksheetResponse.data;
                        //     worksheets = worksheets.result.content;
                        //     for (key in worksheets) {
                        //         var worksheet = worksheets[key];
                        //         worksheet.type = "worksheet";
                        //         returnObject.processContent(worksheet);
                        //     }
                        // } else {
                        //     // TODO: handle error.
                        //     console.log("Worksheet error:", worksheetResponse);
                        // }
                        resolve(true);
                    })
                    .catch(function(err) {
                        console.log("Error while fetching content list: ", err);
                        reject("Error while fetching content list: " + err);
                    });
                });
            },
            setContentVersion: function(ver) {
                $window.localStorage["quizapp-contentversion"] = ver;
            },
            getContentVersion: function() {
                return $window.localStorage["quizapp-contentversion"];
            },
            remove: function(key) {
                $window.localStorage.removeItem(key);
            },
            clear: function() {
                $window.localStorage.clear();
            }
        };
        return returnObject;
    }]);