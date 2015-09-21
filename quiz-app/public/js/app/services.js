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
            content.processingStart = (new Date()).getTime();
            returnObject.saveContent(content);
            return new Promise(function(resolve, reject) {
                DownloaderService.process(content)
                .then(function(data) {
                    for (key in data) {
                        content[key] = data[key];
                    }
                    returnObject.saveContent(content);
                    if (content.status == 'ready') {
                        var message = AppMessages.CONTENT_LOAD_MSG.replace("{0}", "1 " + content.type);
                        $rootScope.$broadcast('show-message', {
                            "message": message,
                            "reload": true,
                            "timeout": 2000
                        });
                    }
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
                if (data && data != null) {
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
                if (_.isArray(list)) {
                    return list.length;
                }
                return 0;
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
            getContentCount: function(type) {
                var list = returnObject.getContentList(type);
                if (_.isArray(list)) {
                    return list.length;
                } else {
                    return 0;
                }
            },
            getContent: function(id) {
                return this.contentList[id];
            },
            processContent: function(content) {
                var promise = {};
                var localContent = returnObject.getContent(content.identifier);
                if (localContent) {
                    if (localContent.status == 'processing') {
                        var processStart = localContent.processingStart;
                        if (processStart) {
                            var timeLapse = (new Date()).getTime() - processStart;
                            if (timeLapse/60000 > AppConfig.PROCESSING_TIMEOUT) {
                                localContent.status = "error";
                            }
                        }
                    }
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
                returnObject.setSyncStart();
                return new Promise(function(resolve, reject) {
                    PlatformService.getContentList()
                    .then(function(contents) {
                        var promises = [];
                        if (contents.status == 'error') {
                            var errorCode = contents.errorCode;
                            var errorParam = contents.errorParam;
                            var errMsg = AppMessages[errorCode];
                            if (errorParam && errorParam != '') {
                                errMsg = errMsg.replace('{0}', errorParam);
                            }
                            returnObject.resetSyncStart();
                            $rootScope.$broadcast('show-message', {
                                message: errMsg,
                                "timeout": 10000
                            });
                        } else {
                            if(contents.data) {
                                for (key in contents.data) {
                                    var content = contents.data[key];
                                    promises.push(returnObject.processContent(content));
                                }
                            }
                            Promise.all(promises)
                            .then(function(result) {
                                returnObject.resetSyncStart();
                            });    
                        }
                        resolve(true);
                    })
                    .catch(function(err) {
                        returnObject.resetSyncStart();
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
            setSyncStart: function() {
                $window.localStorage["quizapp-syncstart"] = (new Date()).getTime();
            },
            getSyncStart: function() {
                return $window.localStorage["quizapp-syncstart"];
            },
            resetSyncStart: function() {
                $window.localStorage["quizapp-syncstart"] = undefined;
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

