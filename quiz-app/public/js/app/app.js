// Ionic Quiz App

// angular.module is a global place for creating, registering and retrieving Angular modules
// 'quiz' is the name of this angular module example (also set in a <body> attribute in index.html)
// the 2nd parameter is an array of 'requires'
var packageName = "org.ekstep.quiz.app";
var version = "1.0";

function backbuttonPressed() {
    if(Renderer.running) {
        initBookshelf();
    } else {
        TelemetryService.end(packageName, version);
    }
}
angular.module('quiz', ['ionic', 'ngCordova', 'quiz.services'])
    .run(function($ionicPlatform, $ionicModal, $cordovaFile, $cordovaToast, ContentService) {
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
                backbuttonPressed();
            });
            $ionicPlatform.on("pause", function() {
                Renderer.pause();
            });
            $ionicPlatform.on("resume", function() {
                Renderer.resume();
                initBookshelf();
            });

            GlobalContext.init(packageName, version).then(function() {
                if (!TelemetryService._gameData) {
                    TelemetryService.init(GlobalContext.user, GlobalContext.game).then(function() {
                        TelemetryService.start();    
                    }).catch(function(error) {
                        console.log('TelemetryService init failed');
                    });
                }
            }).catch(function(error) {
                alert('Please open this app from Genie.');
                navigator.app.exitApp();
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
            })
            .state('replayContent', {
                url: "/replay/content/:itemId",
                templateUrl: "templates/renderer.html",
                controller: 'ReplayContentCtrl'
            });
    })
    .controller('ContentListCtrl', function($scope, $rootScope, $http, $ionicModal, $cordovaFile, $cordovaToast, $ionicPopover, $state, $q, ContentService) {

        var currentContentVersion = "0.2";

        $ionicModal.fromTemplateUrl('about.html', {
            scope: $scope,
            animation: 'slide-in-up'
        }).then(function(modal) {
            $scope.aboutModal = modal;
        });

        $scope.environmentList = [
            { text: "Sandbox", value: "API_SANDBOX" },
            { text: "Production", value: "API_PRODUCTION" }
        ];
        $scope.selectedEnvironment = {value: "API_SANDBOX"};

        new Promise(function(resolve, reject) {
                if(currentContentVersion != ContentService.getContentVersion()) {
                    console.log("Clearing ContentService cache.");
                    ContentService.clear();
                    ContentService.setContentVersion(currentContentVersion);
                }
                ContentService.init();
                resolve(true);
            })
            .then(function() {
                $rootScope.$apply(function() {
                    $rootScope.loadBookshelf();
                });
                setTimeout(function() {
                    $scope.checkContentCount();
                }, 100);
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

        $rootScope.showMessage = false;
        $rootScope.$on('show-message', function(event, data) {
            if (data.message && data.message != '') {
                $rootScope.$apply(function() {
                    $rootScope.showMessage = true;
                    $rootScope.message = data.message;
                });
            }
            if(data.timeout) {
                setTimeout(function() {
                    $rootScope.$apply(function() {
                        $rootScope.showMessage = false;
                    });
                    if (data.callback) {
                        data.callback();
                    }
                }, data.timeout);
            }
            if(data.reload) {
                $rootScope.$apply(function() {
                    $rootScope.loadBookshelf();
                });
            }
        });

        $scope.resetContentListCache = function() {
            var syncStart = ContentService.getSyncStart();
            var reload = true;
            if (syncStart) {
                var timeLapse = (new Date()).getTime() - syncStart;
                if (timeLapse/60000 < 10) {
                    reload = false;
                }
            }
            if (reload) {
                $("#loadingDiv").show();
                $rootScope.showMessage = false;
                $rootScope.message = "";
                setTimeout(function() {
                    ContentService.sync()
                        .then(function() {
                            var processing = ContentService.getProcessCount();
                            if(processing > 0) {
                                $rootScope.$broadcast('show-message', {
                                    "message": AppMessages.DOWNLOADING_MSG.replace('{0}', processing)
                                });
                            }
                            $rootScope.loadBookshelf();
                            console.log('flushing telemetry in 2sec...');
                            setTimeout(function() {
                                TelemetryService.flush();
                            }, 2000);
                        });
                }, 100);   
            } else {
                setTimeout(function() {
                    $rootScope.$apply(function() {
                        $rootScope.showMessage = false;
                    });
                    var processing = ContentService.getProcessCount();
                    if(processing > 0) {
                        $rootScope.$broadcast('show-message', {
                            "message": AppMessages.DOWNLOADING_MSG.replace('{0}', processing)
                        });
                    }
                }, 100);
            }
        }

        $rootScope.loadBookshelf = function() {
            $rootScope.worksheets = ContentService.getContentList('worksheet');
            console.log("$scope.worksheets:", $rootScope.worksheets);
            $rootScope.stories = ContentService.getContentList('story');
            console.log("$scope.stories:", $rootScope.stories);
            initBookshelf();
        };

        $scope.checkContentCount = function() {
            var count = ContentService.getContentCount();
            if(count <= 0) {
                $rootScope.$broadcast('show-message', {
                    "message": AppMessages.NO_CONTENT_FOUND
                });
            }
        };

        $scope.playContent = function(content) {
            $state.go('playContent', {
                'item': JSON.stringify(content)
            });
        };

        $scope.showAboutUsPage = function() {
            $scope.aboutModal.show();
        };
        $scope.hideAboutUsPage = function() {
            $scope.aboutModal.hide();
        };

        $scope.changeEnvironment = function(item) {
            console.log('Env changed:' + $scope.selectedEnvironment.value);
            var env = AppConfig[$scope.selectedEnvironment.value];
            if (_.isString(env) && env.length > 0) {
                PlatformService.setAPIEndpoint(env);
            }
        };
        $scope.exitApp = function(){
            console.log("Enter");
            if(navigator.app)
                navigator.app.exitApp();
            if(navigator.device)
                navigator.device.exitApp();
            if(window)
                window.close();
        }

    }).controller('ContentCtrl', function($scope, $http, $cordovaFile, $cordovaToast, $ionicPopover, $state, ContentService, $stateParams) {
        if ($stateParams.item) {
            $scope.item = JSON.parse($stateParams.item);
            Renderer.start($scope.item.baseDir, 'gameCanvas', $scope.item.identifier);
        } else {
            alert('Name or Launch URL not found.');
            $state.go('contentList');
        }
        $scope.$on('$destroy', function() {
            setTimeout(function() {
                Renderer.cleanUp();
                initBookshelf();
            }, 100);
        });
    }).controller('ReplayContentCtrl', function($scope, $http, $cordovaFile, $cordovaToast, $ionicPopover, $state, ContentService, $stateParams) {
        var content = ContentService.getContent($stateParams.itemId);
        if (content) {
            $scope.item = content;
            Renderer.start($scope.item.baseDir, 'gameCanvas', $scope.item.identifier);
        } else {
            $state.go('contentList');
        }
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