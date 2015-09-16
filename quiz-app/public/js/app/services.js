angular.module('quiz.services', ['ngResource'])
    .factory('ContentService', ['$window', '$rootScope', function($window, $rootScope) {
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
            return new Promise(function(resolve, reject) {
                DownloaderService.process(content)
                .then(function(data) {
                    for (key in data) {
                        content[key] = data[key];
                    }
                    returnObject.saveContent(content);
                    resolve(content);
                })
                .catch(function(data) {
                    for (key in data) {
                        content[key] = data[key];
                    }
                    returnObject.saveContent(content);
                    resolve(content);
                });
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
                var promise = {};
                var localContent = returnObject.getContent(content.identifier);
                if (localContent) {
                    if ((localContent.status == "ready" && localContent.pkgVersion != content.pkgVersion) || (localContent.status == "error")) {
                        promise = processContent(content);
                    } else {
                        if (localContent.status == "ready")
                            console.log("content: " + localContent.identifier + " is at status: " + localContent.status + " and there is no change in pkgVersion.");
                        else
                            console.log("content: " + localContent.identifier + " is at status: " + localContent.status);
                    }
                } else {
                    promise = processContent(content);
                }
                return promise;
            },
            sync: function() {
                return new Promise(function(resolve, reject) {
                    PlatformService.getContentList()
                    .then(function(contents) {
                        var promises = [];
                        if(contents.data) {
                            for (key in contents.data) {
                                var content = contents.data[key];
                                promises.push(returnObject.processContent(content));
                            }
                        }
                        Promise.all(promises)
                        .then(function(result) {
                            console.log("result:", result);
                            var message = "";
                            var storiesCount = _.where(result, {"type": "story", "status": "ready"}).length;
                            if(storiesCount > 0) message += storiesCount + " stories";
                            var worksheetCount = _.where(result, {"type": "worksheet", "status": "ready"}).length;
                            if(worksheetCount > 0) {
                                if(message.length > 0) message += " and ";
                                message += worksheetCount + " worksheets";
                            }
                            if(message.length > 0) message += " updated successfully";
                            var errorCount = _.where(result, {"status": "error"}).length;
                            if(errorCount > 0) {
                                if(message.length > 0) message += ", and";
                                message += errorCount + " items failed.";
                            }
                            if(message) {
                                $rootScope.$broadcast('show-message', {
                                    "message": message,
                                    "reload": true,
                                    "timeout": 10000
                                });
                            }
                        });
                        if(contents.error) {
                            $rootScope.$broadcast('show-message', {
                                message: contents.error,
                                "timeout": 10000
                            });
                        }
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