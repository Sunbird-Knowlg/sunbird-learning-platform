// Ionic Quiz App

// angular.module is a global place for creating, registering and retrieving Angular modules
// 'quiz' is the name of this angular module example (also set in a <body> attribute in index.html)
// the 2nd parameter is an array of 'requires'
angular.module('quiz', ['ionic', 'ngCordova', 'quiz.services'])
    .run(function($ionicPlatform, $cordovaFile, $cordovaToast, ContentService) {
        $ionicPlatform.ready(function() {
            // Hide the accessory bar by default (remove this to show the accessory bar above the keyboard
            // for form inputs)
            console.log('ionic platform is ready...');
            if (window.cordova && window.cordova.plugins.Keyboard) {
                cordova.plugins.Keyboard.hideKeyboardAccessoryBar(true);
            }
            if (window.StatusBar) {
                StatusBar.styleDefault();
            }

            $ionicPlatform.onHardwareBackButton(function() {
                // TelemetryService.end();
            });
        });
    })
    .config(function($stateProvider, $urlRouterProvider) {
        $urlRouterProvider.otherwise("/content/list");
        $stateProvider
            .state('contentList', {
                url: "/content/list",
                templateUrl: "templates/content-list.html",
                controller: 'ContentListCtrl'
            })
            .state('playContent', {
                url: "/play/content/:item",
                templateUrl: "templates/renderer.html",
                controller: 'ContentCtrl'
            });
    })
    .controller('ContentListCtrl', function($scope, $http, $cordovaFile, $cordovaToast, $ionicPopover, $state, $q, ContentService) {

        var currentContentVersion = "0.1";

        new Promise(function(resolve, reject) {
                if(currentContentVersion != ContentService.getContentVersion()) {
                    console.log("Clearing ContentService cache.");
                    ContentService.clear();
                    ContentService.setContentVersion(currentContentVersion);
                }
                ContentService.init();
                resolve(TelemetryService._gameData);
            })
            .then(function(game) {
                if (!game) {
                    var user = {
                        "sid": "de305d54-75b4-431b-adb2-eb6b9e546013",
                        "uid": "123e4567-e89b-12d3-a456-426655440000",
                        "did": "ff305d54-85b4-341b-da2f-eb6b9e5460fa"
                    };
                    var game = {
                        "id": "com.ilimi.quiz.app",
                        "ver": "1.0"
                    };
                    return TelemetryService.init(user, game);
                } else {
                    return true;
                }
            })
            .then(function() {
                return ContentService.sync();
            })
            .then(function() {
                var processing = ContentService.getProcessCount();
                if(processing > 0) {
                    PlatformService.showToast(processing +" stories/worksheets in processing...");
                }
                $scope.$apply(function() {
                    $scope.loadBookshelf();
                });
            })
            .catch(function(error) {
                TelemetryService.exitWithError(error);
            });

        // $ionicPopover.fromTemplateUrl('templates/main-menu.html', {
        //     scope: $scope
        // }).then(function(popover) {
        //     $scope.mainmenu = popover;
        // });

        // $scope.openMainMenu = function($event) {
        //     $scope.mainmenu.show($event);
        // };
        // $scope.closeMainMenu = function() {
        //     $scope.mainmenu.hide();
        // };

        $scope.resetContentListCache = function() {
            $("#loadingDiv").show();
            setTimeout(function() {
                ContentService.sync()
                    .then(function() {
                        var processing = ContentService.getProcessCount();
                        if(processing > 0) {
                            PlatformService.showToast(processing +" stories/worksheets in processing...");
                        }
                        $scope.loadBookshelf();
                        console.log('flushing telemetry in 2sec...');
                        setTimeout(function() {
                            TelemetryService.flush();
                        }, 2000);
                    });
            }, 100);
        }

        $scope.loadBookshelf = function() {
            $scope.worksheets = ContentService.getContentList('worksheet');
            console.log("$scope.worksheets:", $scope.worksheets);
            $scope.stories = ContentService.getContentList('story');
            console.log("$scope.stories:", $scope.stories);
            initBookshelf();
        };

        $scope.playContent = function(content) {
            $state.go('playContent', {
                'item': JSON.stringify(content)
            });
        }

    }).controller('ContentCtrl', function($scope, $http, $cordovaFile, $cordovaToast, $ionicPopover, $state, ContentService, $stateParams) {
        if ($stateParams.item) {
            $scope.item = JSON.parse($stateParams.item);
            Renderer.start($scope.item.baseDir, 'gameCanvas');
            TelemetryService.start($scope.item.id, "1.0");
        } else {
            alert('Name or Launch URL not found.');
            $state.go('contentList');
        }
        $scope.$on('$destroy', function() {
            setTimeout(function() {
                TelemetryService.end();
                Renderer.cleanUp();
            }, 100);
        });
    });


function initBookshelf() {
    setTimeout(function() {
        $(".product_title").remove();
        $(".fx_shadow").remove();
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
        console.log('Loading completed....');
        $("#loadingDiv").hide();
    }, 100);
}