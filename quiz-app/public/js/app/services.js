angular.module('quiz.services', ['ngResource'])
    .factory('GameService', function($q, $timeout, $http, $resource) {
        return {
            getGamesLocal: function(jsonFile) {
                var deferred = $q.defer();
                $http.get('json/' + jsonFile)
                .then(function(data) {
                    // console.log('data:',data.data);
                    deferred.resolve(data.data.result.games);  
                }, function(err) {
                    deferred.reject(err);
                });
                return deferred.promise;
            },
            getGames: function() {
                var deferred = $q.defer();
                var req = {
                    method: 'POST',
                    url: 'http://lp-sandbox.ekstep.org:8080/taxonomy-service/v1/game/list',
                    headers: {
                        "user-id": "mahesh",
                        "content-type": "application/json"
                    },
                    data: {
                        'request': {}
                    }
                }
                $http(req).then(function(resp) {
                    if(resp.data.params.status == 'successful')
                        deferred.resolve(resp.data.result.games);
                    else
                        deferred.reject(resp.data.params);
                }, function(err) {
                    deferred.reject(err);
                });
                return deferred.promise;
            }
        }
    })
    .factory('$localstorage', ['$window', function($window) {
        return {
            set: function(key, value) {
                $window.localStorage[key] = value;
            },
            get: function(key, defaultValue) {
                return $window.localStorage[key] || defaultValue;
            },
            setObject: function(key, value) {
                $window.localStorage[key] = JSON.stringify(value);
            },
            getObject: function(key) {
                var data = $window.localStorage[key];
                if(null != data)
                    return JSON.parse(data);
                else 
                    return null;
            },
            remove: function(key) {
                $window.localStorage.removeItem(key);
            },
            clear: function() {
                $window.localStorage.clear();
            }
        };
    }]);