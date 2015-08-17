// Ionic Quiz App

// angular.module is a global place for creating, registering and retrieving Angular modules
// 'quiz' is the name of this angular module example (also set in a <body> attribute in index.html)
// the 2nd parameter is an array of 'requires'
angular.module('quiz', ['ionic', 'ngCordova', 'quiz.services'])
    .run(function($ionicPlatform, $cordovaFile, $cordovaToast, GameService, $localstorage) {
        $ionicPlatform.ready(function() {
            // Hide the accessory bar by default (remove this to show the accessory bar above the keyboard
            // for form inputs)
            if (window.cordova && window.cordova.plugins.Keyboard) {
                cordova.plugins.Keyboard.hideKeyboardAccessoryBar(true);
            }
            if (window.StatusBar) {
                StatusBar.styleDefault();
            }

            $ionicPlatform.onHardwareBackButton(function() {
                initBookshelf();
            });
        });
    })
    .config(function($stateProvider, $urlRouterProvider) {
        $urlRouterProvider.otherwise("/content/list");
        $stateProvider
            .state('loading', {
                url: "/loading",
                templateUrl: "templates/loading.html"
            })
            .state('contentList', {
                url: "/content/list",
                templateUrl: "templates/content-list.html",
                controller: 'ContentListCtrl'
            })
            .state('playWorksheet', {
                url: "/play/worksheet/:launchUrl",
                // templateUrl: "templates/worksheet-template.html",
                templateUrl: "worksheet1.html",
                controller: 'WorksheetCtrl'
            });
    })
    .controller('ContentListCtrl', function($scope, $http, $cordovaFile, $cordovaToast, $ionicPopover, $state, GameService, $localstorage) {
        $scope.load = {
            status: true,
            message: "Loading..."
        };
        setTimeout(function() {
            if (null == $localstorage.getObject('games')) {
                $scope.getGames();
            } else {
                $scope.load = {
                    status: true,
                    message: "Loading games..."
                };
                $scope.$apply(function() {
                    $scope.games = $localstorage.getObject('games');
                    $scope.screeners = $localstorage.getObject('screeners');
                    $scope.load = {
                        status: false,
                        message: "Loading..."
                    };
                });
                $scope.loadBookshelf();
            }
        }, 1000);

        $ionicPopover.fromTemplateUrl('templates/main-menu.html', {
            scope: $scope
        }).then(function(popover) {
            $scope.mainmenu = popover;
        });

        $scope.openMainMenu = function($event) {
            $scope.mainmenu.show($event);
        };
        $scope.closeMainMenu = function() {
            $scope.mainmenu.hide();
        };

        $scope.resetGameCache = function() {
            $localstorage.remove('games');
            $localstorage.remove('screeners');
            setTimeout(function() {
                $scope.getGames();
            }, 1000);
        }

        $scope.loadBookshelf = function() {
            initBookshelf();
        };

        $scope.getGames = function() {
            $scope.load = {
                status: true,
                message: "Loading games..."
            };
            GameService.getGamesLocal('screeners.json')
                .then(function(resp) {
                    $localstorage.setObject('screeners', resp);
                    $scope.screeners = $localstorage.getObject('screeners');
                    GameService.getGamesLocal('worksheets.json')
                        .then(function(gamesResp) {
                            $localstorage.setObject('games', gamesResp);
                            $scope.games = $localstorage.getObject('games');
                            $scope.load = {
                                status: false,
                                message: "Loading..."
                            };
                            $scope.loadBookshelf();
                        }, function(err) {
                            $scope.load = {
                                status: false,
                                message: "Loading..."
                            };
                        });
                }, function(err) {
                    $scope.load = {
                        status: false,
                        message: "Loading..."
                    };
                });

        }

        $scope.playWorksheet = function(launchUrl) {
            $state.go('playWorksheet', {
                'launchUrl': launchUrl
            });
        }

        $scope.gameClick = function(game) {
            window.location.href = game.launchUrl;
        }

        $scope.updateLog = function(game) {
            var logData = {
                identifier: game.identifier,
                name: game.name,
                timestamp: new Date()
            };
        }

    }).controller('WorksheetCtrl', function($scope, $http, $cordovaFile, $cordovaToast, $ionicPopover, $state, GameService, $localstorage, $stateParams) {
        if ($stateParams.launchUrl) {
            $http.get($stateParams.launchUrl)
                .then(function(data) {
                    Renderer.init(data.data, 'gameCanvas');
                }, function(err) {
                    alert("error");
                    $state.go('contentList');
                });
        } else {
            $state.go('contentList');
        }

    });


function initBookshelf() {
    setTimeout(function() {
        var widthToHeight = 16 / 9;
        var newWidth = window.innerWidth;
        var newHeight = window.innerHeight;
        var newWidthToHeight = newWidth / newHeight;
        if (newWidthToHeight > widthToHeight) {
            newWidth = newHeight * widthToHeight;
        } else {
            newHeight = newWidth / widthToHeight;
        }
        $.bookshelfSlider('#bookshelf_slider', {
            'item_width': newWidth, 
            'item_height': newHeight,
            'products_box_margin_left': 30,
            'product_title_textcolor': '#ffffff',
            'product_title_bgcolor': '#990000',
            'product_margin': 30,
            'product_show_title': true,
            'show_icons': true,
            'buttons_margin': 15,
            'buttons_align': 'center', // left, center, right
            'slide_duration': 800,
            'slide_easing': 'easeOutCirc',
            'arrow_duration': 800,
            'arrow_easing': 'easeInCirc',
            'folder': ''
        });
        $(".panel_slider").height($(".view-container").height() - $(".panel_title").height() - $(".panel_bar").height());
    }, 500); 
}
