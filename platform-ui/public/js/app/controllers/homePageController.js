var app = angular.module('homePageApp', ['sunburst.services', 'sunburst.directives', 'd3']);

app.directive('ngEnter', function() {
    return function(scope, element, attrs) {
        element.bind("keydown keypress", function(event) {
            if (event.which === 13) {
                scope.$apply(function() {
                    scope.$eval(attrs.ngEnter);
                });
                event.preventDefault();
            }
        });
    };
});

app.run(function($rootScope, $sce) {
    $rootScope.fedoraPrefix = "info:fedora/";
    $rootScope.removeFedoraPrefix = function(identifier) {
        if (identifier.indexOf($rootScope.fedoraPrefix) == 0) {
            return identifier.substring($rootScope.fedoraPrefix.length);
        } else {
            return identifier;
        }
    };

    $rootScope.addFedoraPrefix = function(identifier) {
        if (identifier.indexOf($rootScope.fedoraPrefix) == -1) {
            return $rootScope.fedoraPrefix + identifier;
        }
        return identifier;
    };

    $rootScope.renderHtml = function(htmlCode) {
        return $sce.trustAsHtml(htmlCode);
    };
});

app.controller('HomePageCtrl', function($scope, $http, $location, $window, $timeout, $sce, $anchorScroll) {

    $scope.renderHtml = function(htmlCode) {
        return $sce.trustAsHtml(htmlCode);
    };

    $scope.moveToLocation = function(id){
        $location.hash(id);
        $anchorScroll();
    }

});

app.controller('HowItWorksCtrl', function($scope, $http, $location, $window, $timeout, $sce) {
    $scope.showHowItWorks = true;
});

app.controller('TermsAndConditionsCtrl', function($scope, $http, $location, $window, $timeout, $sce, $anchorScroll) {
    $scope.accept = function() {
        $scope.tandcsubmitted = true;
        if($scope.termsofuse && $scope.honorcode) {
            $http.post('/private/v1/termsandconditions',{}).success(function(data) {
                console.log("data:",data);
                $http.get('/private/v1/player/currentCourse').success(function(data) {
                    if (data.error) {
                        $window.location.href = '/private/player/dashboard';
                    } else {
                        document.location.href = '/private/player/course/' + encodeURIComponent(data) + '#/myCourses';
                    }
                });
            });
        }
    }

    $scope.moveToLocation = function(id){
        $location.hash(id);
        $anchorScroll();
        $('.headerAsk').css('margin-top', 50);
        // windowHeight();
    }
});

app.controller('HomePageRegisterCtrl', function($scope, $http, $location, $window, $timeout, $sce) {
    $scope.contact = {};
    $scope.sendMail = function() {
        $("#sendMailBtn").html("Sending...").attr("disabled", true);
        $("#contactUsAlert").html("").addClass("hide").removeClass("alert-success").removeClass("alert-danger");
        if($scope.contact.name && $scope.contact.email) {
            $http.post('/public/v1/sendRegisterMail', $scope.contact).success(function(data) {
                console.log("Response:", data);
                if(data.status == "SUCCESS") {
                    $scope.contact = {};
                    $("#contactUsAlert").html("Thank you for registering. We will get back to you shortly.").removeClass("hide").addClass("alert-success");
                } else {
                    $("#contactUsAlert").html(data.errorMessage).removeClass("hide").addClass("alert-danger");
                }
                $("#sendMailBtn").html("REACH OUT").attr("disabled", false);
            });
        } else {
            $("#contactUsAlert").html("Please fill all required fields.").removeClass("hide").addClass("alert-danger");
            $("#sendMailBtn").html("REACH OUT").attr("disabled", false);
        }
    }
});


app.controller('RegisterCtrl', function($scope, $http, $location, $window, $timeout, $sce) {
    $scope.metadata = {};
    $scope.userId = $('#userId').val();
    $scope.getUserProfile = function() {
        $http.get('/private/v1/user/' + $scope.userId + '/profile').success(function(resp) {
            $scope.metadata = resp.metadata;
            $scope.metadata.givenName = resp.name.givenName;
            $scope.metadata.middleName = resp.name.middleName;
            $scope.metadata.familyName = resp.name.familyName;
        });
    }
    $scope.completeRegistration = function() {
        $("#contactUsAlert").html("").addClass("hide").removeClass("alert-success").removeClass("alert-danger");
        if($scope.metadata.givenName == "" || $scope.metadata.givenName == null || !$scope.metadata.givenName) {
            $("#contactUsAlert").html("First Name is required.").removeClass("hide").addClass("alert-danger");
            return;
        }
        $("#sendMailBtn").html("Completing registration...").attr("disabled", true);
        $http.post('/user/registration/complete', $scope.metadata).success(function(data) {
            if(data.registered) {
                $http.get('/private/v1/player/currentCourse').success(function(data) {
                    if (data.error) {
                        $window.location.href = '/private/player/dashboard';
                    } else {
                        document.location.href = '/private/player/course/' + encodeURIComponent(data) + '#/myCourses';
                    }
                });
            } else {
                $("#contactUsAlert").html("Sorry. Unable to register at this momemt. Please try again later.").removeClass("hide").addClass("alert-danger");
            }
            $("#sendMailBtn").html("Complete Registration").attr("disabled", false);
        });
    }
    $scope.getUserProfile();
});


function loadSunburst($scope, $http, courseId) {
    // Sunburst Code
    $scope.data;
    $scope.displayVis = false;
    $scope.currentnode;
    $scope.color;
    $scope.contentList = [];
    // Browser onresize event
    window.onresize = function() {
        $scope.$apply();
    };

    // Traverses the data tree assigning a color to each node. This is important so colors are the
    // same in all visualizations
    $scope.assignColors = function(node) {
        $scope.getColor(node);
        _.each(node.children, function(c) {
            $scope.assignColors(c);
        });
    };
    // Calculates the color via alphabetical bins on the first letter. This will become more advanced.
    $scope.getColor = function(d) {
        /*
        if(!d.contentCount) d.contentCount = 0;
        if(d.depth == 0) {
        } else if(d.contentCount <= 10) {
            d.color = $scope.color[0];
        } else if(d.contentCount > 10 && d.contentCount <= 20) {
            d.color = $scope.color[1];
        } else if(d.contentCount > 20 && d.contentCount <= 30) {
            d.color = $scope.color[2];
        } else if(d.contentCount > 30 && d.contentCount <= 40) {
            d.color = $scope.color[3];
        } else if(d.contentCount > 40 && d.contentCount <= 50) {
            d.color = $scope.color[4];
        } else if(d.contentCount > 50) {
            d.color = $scope.color[5];
        }*/
        d.color = $scope.color(d.name);
    };
    //$scope.color = ["#87CEEB", "#007FFF", "#72A0C1", "#318CE7", "#0000FF", "#0073CF"];
    $scope.color = d3.scale.ordinal().range(["#33a02c", "#1f78b4", "#b2df8a", "#a6cee3", "#fb9a99", "#e31a1c", "#fdbf6f", "#ff7f00", "#6a3d9a", "#cab2d6", "#ffff99"]);

    $http.post('/v1/course/fetchSunburstConceptMap/', {
        "courseId": courseId
    })
    .success(function(data, status, headers, config) {
        if (data && data.length > 0) {
            var root = data[0];
            /*root['categoryCounts'] = data[1];
        root['words'] = [];
        root['words'].push(data[0].name);*/
            $scope.assignColors(root);
            $scope.data = data;
        }
    })
    .error(function(data, status, headers, config) {
        console.log("Error loading data!" + status);
    });

}