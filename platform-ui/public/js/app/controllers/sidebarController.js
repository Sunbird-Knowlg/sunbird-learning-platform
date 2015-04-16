app.controller('SidebarController', ['$scope', '$http', '$timeout', '$rootScope', '$state', '$window', '$q', function($scope, $http, $timeout, $rootScope, $state, $window, $q) {

    $scope.categories = [
        {id: 'general', label: "General"},
        {id: 'lifecycle', label: "Lifecycle"},
        {id: 'usageMetadata', label: "Usage Metadata"},
        {id: 'relationships', label: "Relationships"},
        {id: 'analytics', label: "Analytics"},
        {id: 'games', label: "Games"},
        {id: 'audit', label: "Audit"},
        {id: 'comments', label: "Comments"},
        {id: 'tags', label: "Tags"}
    ]
}]);
