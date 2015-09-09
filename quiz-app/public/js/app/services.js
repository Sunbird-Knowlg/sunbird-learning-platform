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
                        story.type = "story";
                        this.saveContent(story);
                    }
                }
                if (contents.worksheets) {
                    var worksheets = (_.isString(contents.worksheets)) ? JSON.parse(contents.worksheets) : contents.worksheets;
                    worksheets = worksheets.result.games;
                    for (key in worksheets) {
                        var worksheet = worksheets[key];
                        worksheet.type = "worksheet";
                        this.saveContent(worksheet);
                    }
                }
            },
            saveContent: function(content) {
                this.contentList[content.id] = content;
                this.commit();
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
            remove: function(key) {
                $window.localStorage.removeItem(key);
            },
            clear: function() {
                $window.localStorage.clear();
            }
        };
    }]);