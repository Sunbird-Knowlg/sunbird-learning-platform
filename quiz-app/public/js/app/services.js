angular.module('quiz.services', ['ngResource'])
    .factory('GameService', function($q, $timeout, $http, $resource) {
        return {
            getGamesLocal: function(jsonFile) {
                var deferred = $q.defer();
                $http.get('json/' + jsonFile)
                    .then(function(data) {
                        deferred.resolve(data.data);
                    }, function(err) {
                        deferred.reject(err);
                    });
                return deferred.promise;
            },
            getGames: function() {
                var deferred = $q.defer();
                if (typeof GenieServicePlugin == 'undefined') {
                    var content = {};
                    var service = this;
                    service.getGamesLocal('stories.json')
                        .then(function(stories) {
                            content.stories = stories;
                        })
                        .then(function() {
                            return service.getGamesLocal('worksheets.json');
                        })
                        .then(function(worksheets) {
                            content.worksheets = worksheets;
                            console.log("Getting data from local...")
                            deferred.resolve(content);
                        })
                        .catch(function(err) {
                            deferred.reject('Error while getting local data.');
                        });
                } else {
                    GenieServicePlugin.getContentList()
                        .then(function(data) {
                            deferred.resolve(data);
                        })
                        .catch(function(err) {
                            deferred.reject(err);
                        });
                }
                return deferred.promise;
            }
        }
    })
    .factory('ContentService', ['$window', function($window) {
        // Content status values: "created", "updated", "processing", "ready".
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
        return {
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
            setContentList: function(contents) {
                if (contents.stories) {
                    var stories = (_.isString(contents.stories)) ? JSON.parse(contents.stories) : contents.stories;
                    stories = stories.result.games;
                    for (key in stories) {
                        var story = stories[key];
                        this.setContentObject(story, "story");
                    }
                }
                if (contents.worksheets) {
                    var worksheets = (_.isString(contents.worksheets)) ? JSON.parse(contents.worksheets) : contents.worksheets;
                    worksheets = worksheets.result.games;
                    for (key in worksheets) {
                        var worksheet = worksheets[key];
                        this.setContentObject(worksheet, "worksheet");
                    }
                }
            },
            setContentObject: function(content, contentType) {
                content.type = contentType;
                if (this.contentList[content.id]) {
                    if (this.contentList[content.id].status == "ready" && this.contentList[content.id].pkgVersion == content.pkgVersion) {
                        story.status = "ready";
                    } else {
                        content.status = "updated";
                    }
                } else {
                    content.status = "created";
                }
                this.contentList[content.id] = content;
                this.commit();
            },
            getContent: function(type) {
                if (type) {
                    var list = _.where(_.values(this.contentList), {
                        "type": type,
                        "status": "updated"
                    });
                    if (list.length == 0)
                        list = _.where(_.values(this.contentList), {
                            "type": type,
                            "status": "created"
                        });
                    return list;
                } else {
                    var list = _.where(_.values(this.contentList), {
                        "status": "updated"
                    });
                    if (list.length == 0)
                        list = _.where(_.values(this.contentList), {
                            "status": "created"
                        });
                    return list;
                }
            },
            remove: function(key) {
                $window.localStorage.removeItem(key);
            },
            clear: function() {
                $window.localStorage.clear();
            }
        };
    }]);