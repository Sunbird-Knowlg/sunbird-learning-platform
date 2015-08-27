
var appObj;
if(typeof playerApp != 'undefined') {
    appObj = playerApp;
} else if(typeof app != 'undefined') {
    appObj = app;
}

app.filter('slice', function() {
  return function(arr, start, end) {
    return (arr || []).slice(start, end);
  };
});

appObj.controller('LoginCtrl', function($scope, $rootScope, $http, $location, $window, $timeout, $sce) {

    $scope.userName = "";
    $scope.passWord = "";
    $scope.success;
    $scope.environment = 'General';
    $rootScope.contextPath = $('#contextPath').val();

    var currPath = undefined;

    $scope.cleanUp = function() {
        $scope.userName = "";
        $scope.passWord = "";
        $scope.success = 1;
        $scope.showForgotPassword = false;
        $scope.loginFromAncmentPage = false;
        $scope.resetPasswordMailSent = false;
        console.log("cleanUp:",$scope.loginFromAncmentPage);
        $("#forgotPwdBlock").css("display","none");

        $('#loginModal').on('shown.bs.modal', function () {
            $('#loginUserName').focus();
        });
    }

    $scope.signIn = function() {
        var contextName = ($('#appContext').val())? '/'+$('#appContext').val() : '';
        var request = $http({
            method: "POST", // define the type of HTTP verb we want to use (POST for our form)
            url: contextName + "/private/v1/login/", // the url where we want to POST
            data: {
                email: $scope.userName,
                password: $scope.passWord
            }
        }).
        success(function(data) {
            if (data.status == 'success') {
                $scope.success = 1;
                $window.location.href = contextName + '/private/player';
            } else {
                $scope.success = 0;
            }
        }).
        error(function(err) {
            $scope.success = 0;
        });
    }


    $scope.resetPassword = {};

    $scope.openForgotPassworBlock = function() {
        $scope.showForgotPassword = true;
        $scope.resetPassword = {};
        $("#forgotPwdAlert").html("").removeClass("alert-danger alert-success").addClass("hide");
    }

    $scope.forgotPasswordEmail = function() {
        var errorHandler = function(msg, timeout) {
            if(!timeout) timeout = 5000;
            $("#forgotPwdAlert").html(msg).addClass("alert-danger").removeClass("hide");
            setTimeout(function() {
                $("#forgotPwdAlert").html("").removeClass("alert-danger").addClass("hide");
            }, timeout);
        };
        if($scope.resetPassword.email) {
            var regex = /^(([^<>()[\]\\.,;:\s@\"]+(\.[^<>()[\]\\.,;:\s@\"]+)*)|(\".+\"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
            if(regex.test($scope.resetPassword.email)) {
                $http.post("/public/v1/user/forgotPassword", {"email": $scope.resetPassword.email}).success(function(data) {
                    if(data && data.STATUS) {
                        if(data.STATUS == "ERROR") {
                            errorHandler(data.errorMessage);
                        } else if(data.STATUS == "SUCCESS") {
                            $("#forgotPwdAlert").html("The password reset link has been sent to <b>"+$scope.resetPassword.email+"</b>. The link is valid for 24 hours. Please check your email and follow the instructions to reset your password. You can click on forgot password again to resend the email.").addClass("alert-success").removeClass("hide");
                            $scope.resetPassword = {};
                            $scope.resetPasswordMailSent = true;
                        }
                    } else {
                        errorHandler("Error while processing. Please contact Admin.");
                    }
                });
            } else {
                errorHandler("Please enter valid Email.");
            }
        } else {
            errorHandler("Please enter Email.");
        }
    }

});

function endsWith(str, suffix) {
    if (!str || str == null) {
        return false;
    }
    return str.indexOf(suffix, str.length - suffix.length) !== -1;
}
