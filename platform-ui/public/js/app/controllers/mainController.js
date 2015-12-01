var app = angular.module('playerApp', ['ui.router', 'readableTime', 'truncate', 'ngSanitize', 'sunburst.services', 'sunburst.directives', 'd3', 'file-model']);
var contextPath = $('#contextPath').val() || '';
app.config(function($stateProvider) {
    $stateProvider
    .state('learningMap', {
        url: "/learningMap/:id",
        views: {
            "contentSection": {
                templateUrl: contextPath + "/templates/player/learningMap.html",
                controller: 'LearningMapController'
            },
        },
        onEnter: function() {
            $('.rightBarConceptMenu').removeClass('hide');
            $('#relConId').removeClass('hide');
            $('#gameConCovId').addClass('hide');
        },
        onExit: function() {
            $('.rightBarConceptMenu').addClass('hide');
            $('#relConId').addClass('hide');
            $('#gameConCovId').removeClass('hide');
        }
    }).state('gameList', {
        url: "/gameList/:id",
        views: {
            "contentSection": {
                templateUrl: contextPath + "/templates/player/gameList.html",
                controller: 'GameListController'
            },
        },
        onEnter: function() {
            $(".RightSideBar").addClass('Effectsidebar').css('display','none');
            $(".mid-area").addClass('Effectside');
        },
        onExit: function() {
            $(".RightSideBar").removeClass('Effectsidebar').css('display','inline');;
            $(".mid-area").removeClass('Effectside');
        }
    }).state('gamePage', {
        url: "/gamePage/:id",
        views: {
            "contentSection": {
                templateUrl: contextPath + "/templates/player/game.html",
                controller: 'GameController'
            },
        },
        onEnter: function() {
            $('.rightBarGameMenu').removeClass('hide');
            $('#relConId').addClass('hide');
            $('#gameConCovId').removeClass('hide');
        },
        onExit: function() {
            $('.rightBarGameMenu').addClass('hide');
            $('#relConId').removeClass('hide');
            $('#gameConCovId').addClass('hide');
        }
    }).state('gameVisualization', {
        url: "/games/coverage/:tid",
        views: {
            "contentSection": {
                templateUrl: contextPath + "/templates/player/gameVisualization.html",
                controller: 'GameVisualizationController'
            }
        },
        onEnter: function() {
            $(".sideicon3").addClass("hide");
            $(".RightSideBar").addClass('Effectsidebar').css('display','none');
            $(".mid-area").addClass('Effectside');
        },
        onExit: function() {
            $(".sideicon3").removeClass("hide");
            $(".RightSideBar").removeClass('Effectsidebar').css('display','inline');
            $(".mid-area").removeClass('Effectside');
        }
    }).state('contentList', {
        url: "/contentList/:id",
        views: {
            "contentSection": {
                templateUrl: contextPath + "/templates/player/contentList.html",
                controller: 'ContentListController'
            },
        },
        onEnter: function() {
            $(".RightSideBar").addClass('Effectsidebar').css('display', 'none');
            $(".mid-area").addClass('Effectside');
        },
        onExit: function() {
            $(".RightSideBar").removeClass('Effectsidebar').css('display', 'inline');;
            $(".mid-area").removeClass('Effectside');
        }
    }).state('contentPage', {
        url: "/contentPage/:id",
        views: {
            "contentSection": {
                templateUrl: contextPath + "/templates/player/content.html",
                controller: 'ContentController'
            },
        },
        onEnter: function() {
            $('.rightBarGameMenu').removeClass('hide');
            $('#relConId').addClass('hide');
            $('#gameConCovId').removeClass('hide');
        },
        onExit: function() {
            $('.rightBarGameMenu').addClass('hide');
            $('#relConId').removeClass('hide');
            $('#gameConCovId').addClass('hide');
        }
    }).state('contentEditPage', {
        url: "/contentEditPage/:id",
        views: {
            "contentSection": {
                templateUrl: contextPath + "/templates/player/content_edit.html",
                controller: 'ContentController'
            },
        },
        onEnter: function() {
            $('.rightBarGameMenu').removeClass('hide');
            $('#relConId').addClass('hide');
            $('#gameConCovId').removeClass('hide');
        },
        onExit: function() {
            $('.rightBarGameMenu').addClass('hide');
            $('#relConId').removeClass('hide');
            $('#gameConCovId').addClass('hide');
        }
    })
});

app.service('PlayerService', ['$http', '$q', function($http, $q) {

    this.rhsSectionObjectsLimit = 5;
    this.postToService = function(url, data) {
        var deferred = $q.defer();
        $http.post(contextPath + url, data).success(function(resp) {
            if (!resp.error)
                deferred.resolve(resp);
            else{
                deferred.reject(resp);
                console.log("postToService Error");
            }

        });
        return deferred.promise;
    }

    this.getFromService = function(url, data) {
        var deferred = $q.defer();
        $http.get(contextPath + url, data).success(function(resp) {
            if (!resp.error)
                deferred.resolve(resp);
            else
                deferred.reject(resp);
        });
        return deferred.promise;
    }

    this.getAllTaxonomies = function() {
        return this.getFromService('/private/v1/player/taxonomy');
    }

    this.getTaxonomyDefinitions = function(taxonomyId) {
        return this.getFromService('/private/v1/player/taxonomy/' + taxonomyId + '/definitions');
    }

    this.getTaxonomyGraph = function(taxonomyId) {
        return this.getFromService('/private/v1/player/taxonomy/' + taxonomyId + '/graph');
    }

    this.getConcept = function(conceptId, taxonomyId) {
        return this.getFromService('/private/v1/player/concept/' + conceptId + '/' + taxonomyId);
    }

    this.updateConcept = function(data) {
        return this.postToService('/private/v1/player/concept/update', data);
    }

    this.createConcept = function(data) {
        return this.postToService('/private/v1/player/concept/create', data);
    }

    this.getGames = function(taxonomyId, offset, limit) {
        return this.getFromService('/private/v1/player/games/' + taxonomyId + '/' + offset + '/' + limit);
    }

    this.getGameDefinition = function(taxonomyId) {
        return this.getFromService('/private/v1/player/gamedefinition/' + taxonomyId);
    }

    this.getGame = function(taxonomyId, gameId) {
        return this.getFromService('/private/v1/player/game/' + taxonomyId + '/' + gameId);
    }

    this.updateGame = function(data) {
        return this.postToService('/private/v1/player/game/update', data);
    }

    this.createGame = function(data) {
        return this.postToService('/private/v1/player/game/create', data);
    }

    this.getGameCoverage = function(taxonomyId) {
        return this.getFromService('/private/v1/player/gameVis/' + taxonomyId);
    }

    this.getDashboardLinks = function() {
        return this.getFromService('/private/v1/player/dashboard/links');
    }

    this.saveComment = function(comment) {
        return this.postToService('/private/v1/player/comment', comment);
    }

    this.getCommentThread = function(taxonomyId, objId, threadId) {
        return this.getFromService('/private/v1/player/comment/thread/' + taxonomyId + '/' + objId + '/' + threadId);
    }

    this.uploadFile = function(fd) {
        var deferred = $q.defer();
        $http.post('/private/v1/player/fileupload', fd, {
            transformRequest: angular.identity,
            headers: {
                'Content-Type': undefined
            }
        }).success(function(resp) {
            if (!resp.error)
                deferred.resolve(resp);
            else
                deferred.reject(resp);
        });
        return deferred.promise;
    }

    this.getMedia = function(data){
        return this.postToService('/private/v1/player/media', data);
    }

    this.getContents = function(contentType, taxonomyId, offset, limit) {
        return this.getFromService('/private/v1/player/contents/' + contentType + '/' + taxonomyId + '/' + offset + '/' + limit);
    }

    this.getContentDefinition = function(taxonomyId, contentType) {
        return this.getFromService('/private/v1/player/contentdefinition/' + taxonomyId + '/' + contentType);
    }

    this.getContent = function(contentId, taxonomyId, contentType) {
        return this.getFromService('/private/v1/player/content/' + contentId + '/' + taxonomyId + '/' + contentType);
    }

    this.updateContent = function(data) {
        return this.postToService('/private/v1/player/content/update', data);
    }

    this.createContent = function(data) {
        return this.postToService('/private/v1/player/content/create', data);
    }

}]);

app.controller('PlayerController', ['$scope', '$timeout', '$rootScope', '$stateParams', '$state', 'PlayerService', '$location', '$anchorScroll', '$sce', function($scope, $timeout, $rootScope, $stateParams, $state, service, $location, $anchorScroll, $sce) {

    // Structure of taxonomy is
    // taxonomyId: {
    //   name: 'Numeracy',
    //   identifier: '<id>',
    //   graph: {}
    //   definitions: {}
    // }
    $scope.taxonomies = {};
    $scope.dashboardLinks = {};
    $scope.allTaxonomies = [];
    $scope.selectedTaxonomyId = undefined;
    $scope.selectedConcept = undefined;
    $scope.newComment = "";
    $scope.remoteUploadFolder = "games";
    $scope.getAllTaxonomies = function() {
        service.getAllTaxonomies().then(function(data) {
            if(data && data.length > 0) {
                _.forEach(data, function(taxonomy) {
                    $scope.allTaxonomies.push({
                        id: taxonomy.identifier,
                        name: taxonomy.metadata.name
                    })
                    $scope.taxonomies[taxonomy.identifier] = taxonomy;
                })
                $state.go('learningMap', {id: data[0].identifier});
            }
        }).catch(function(err) {
            console.log('Error fetching taxonomies - ', err);
        });
    }

    $scope.getDashboardLinks = function() {
        service.getDashboardLinks().then(function(data) {
            if(data) {
                $scope.dashboardLinks = data;
            }
        }).catch(function(err) {
            console.log('Error fetching taxonomies - ', err);
        });
    }

    $scope.selectTaxonomy = function(taxonomyId) {
        $state.go('learningMap', {id: taxonomyId});
    }

    $scope.selectContent = function(taxonomyId) {
        $state.go('contentList', {id: taxonomyId});
    }

    $scope.viewGameList = function(taxonomyId) {
        $state.go('gameList', {id: taxonomyId});
    }

    $scope.showHeatMap = function(taxonomyId) {
        $state.go('gameVisualization', {tid: taxonomyId});
    }

    $scope.renderHtml = function(htmlCode, append) {
        if (append) {
            htmlCode = append + htmlCode;
        }
        return $sce.trustAsHtml(htmlCode);
    };

    $scope.renderHtmlTrim = function(htmlCode, length) {
        if (htmlCode) {
            var subtxt = htmlCode.substring(0, length);
            if (htmlCode.length > length) {
                subtxt = subtxt;
            }
            var txt = $sce.trustAsHtml(subtxt);
            return txt;
        }
        return $sce.trustAsHtml(htmlCode);
    };

    $scope.openSection = function(divId) {
        if ($('#'+divId).hasClass('in') === false) {
          $('#'+divId).collapse('show');
        }
    }

    $scope.categories = [
        {id: 'general', label: "General", editable: true, editMode: false},
        {id: 'tags', label: "Tags", editable: true, editMode: false},
        {id: 'relations', label: "Relations", editable: false, editMode: false},
        {id: 'lifeCycle', label: "Lifecycle", editable: true, editMode: false},
        {id: 'usageMetadata', label: "Usage Metadata", editable: true, editMode: false},
        {id: 'analytics', label: "Analytics", editable: false, editMode: false},
        {id: 'comments', label: "Comments", editable: false, editMode: true}
    ]

    $scope.resetCategories = function() {
        var newCategories = [];
        _.each($scope.categories, function(cat) {
            cat.editMode = false;
            // if(cat.id == 'relations') {
            //     _.each(_.keys($scope.selectedConcept.relations), function(relation) {
            //         var newCat = _.clone(cat);
            //         newCat.id = 'newRelations';
            //         newCat.dataKey = relation;
            //         newCat.label = relation;
            //         if(_.contains(newCategories, newCat) == false)
            //             newCategories.push(newCat);
            //     });
            // } else {
            //     newCategories.push(cat);
            // }
        });
        // $scope.categories = newCategories;
        $('form').removeClass('ng-dirty').addClass('ng-pristine');
    }

    $scope.taxonomyObjects = [
        {id: 'concept', label: "Broad Concept"},
        {id: 'subConcept', label: "Sub Concept"},
        {id: 'microConcept', label: "Micro Concept"}
    ]

    $scope.scrolltoHref = function (id) {
        $location.hash(id);
        $anchorScroll();
    }

    $scope.buttonLoading = function($event) {
        $($event.target).button('loading');
    }

    $scope.buttonReset = function($event) {
        $($event.target).button('reset');
    }

    $scope.showConformationMessage = function(className, message){
        var closeBtm = '<button type="button" class="close" id="conformMsgCloseBtn">&times;</button>';
        $('#conformMessage').removeClass('alert-success alert-danger');
        $('#conformMessage').html(closeBtm + message).removeClass('hide').addClass(className);
        window.setTimeout(function(){
            $('#conformMessage').html('').addClass('hide').removeClass(className);
        },5000);
        $('#conformMsgCloseBtn').click(function(){
           $('#conformMessage').html('').addClass('hide').removeClass(className);
        });
    }

    $scope.getAllTaxonomies();
    $scope.getDashboardLinks();

    $scope.deleteListValue = function(pname, index, formName) {
        $scope.selectedConcept.metadata[pname].splice(index, 1);
        $('form[name="'+formName+'"]').removeClass('ng-pristine').addClass('ng-dirty');
    }

    $scope.addListValue = function(pname) {
        if(!$scope.selectedConcept.metadata[pname]) $scope.selectedConcept.metadata[pname] = [];
        $scope.selectedConcept.metadata[pname].push("");
    }

    $scope.addNew = function(cat) {
        cat.newMetadataName = undefined;
        cat.newMetadataValue = undefined;
        cat.newTagType = 'system';
        cat.newTagValue = undefined;
        cat.addNew = true;
    }

    $scope.getSystemTags = function() {
        if(!$scope.selectedConcept.tags || $scope.selectedConcept.tags.length == 0) {
            return $scope.selectedTaxonomy.definitions.systemTags;
        }

        var stags = _.filter($scope.selectedTaxonomy.definitions.systemTags, function(tag) {
            return ($scope.selectedConcept.tags.indexOf(tag.name) == -1);
        });
        return stags;
    }

    $scope.addNewTag = function(cat) {
        if(!$scope.selectedConcept.tags) $scope.selectedConcept.tags = [];
        $scope.selectedConcept.tags.push(
            cat.newTagType == 'system' ? cat.newTagValue.name : cat.newTagValue
        );
        cat.addNew = false;
    }

    $scope.deleteTag = function(tags, index) {
        tags.splice(index, 1);
        $('form[name="tagsForm"]').removeClass('ng-pristine').addClass('ng-dirty');
    }

    $scope.addNewMetadata = function(cat) {
        var metadataName = S(cat.newMetadataName.toLowerCase()).camelize().s;
        var newProp = {
            "propertyName": metadataName,
            "title": cat.newMetadataName,
            "category": cat.id,
            "dataType": "Text",
            "displayProperty": "Editable"
        }
        $scope.selectedConcept.newMetadata.push(newProp);
        $scope.selectedTaxonomy.properties.push(newProp);
        $scope.selectedTaxonomy.definitions[cat.id].push(newProp);
        $scope.selectedConcept.metadata[metadataName] = cat.newMetadataValue;
        cat.addNew = false;
    }

    $scope.validateConcept = function() {

        var errors = [], valid = true;
        _.each($scope.categories, function(cat) {
            _.each($scope.selectedTaxonomy.definitions[cat.id], function(prop) {
                var currValue = $scope.selectedConcept.metadata[prop.propertyName];
                var valueExists = false;
                prop.error = false;
                if(prop.required) { // Required Validations
                    var valueExists = true;
                    if(_.isEmpty(currValue)) {
                        valueExists = false;
                        prop.error = true;
                        errors.push(prop.title + ' is required.');
                        valid = false;
                        cat.editMode = true;
                    }
                }
                if(valueExists && prop.dataType == 'Number' && !_.isFinite(currValue)) {
                    prop.error = true;
                    errors.push(prop.title + ' is Number and should contain only numeric value.');
                    valid = false;
                    cat.editMode = true;
                }
            });
        });

        if($rootScope.isNameExist($scope.selectedConcept.metadata.name, $scope.unmodifiedConcept.metadata.name)) {
            valid = false;
            errors.push('Name already in use. Try another.')
        }
        if($rootScope.isCodeExist($scope.selectedConcept.metadata.code, $scope.unmodifiedConcept.metadata.code)) {
            valid = false;
            errors.push('Code already in use. Try another.')
        }

        $scope.validationMessages = errors;
        return valid;
    }

    $scope.conceptToBeUpdated = undefined;
    $scope.confirmChanges = function() {

        if(!$scope.validateConcept()) {
            return;
        }

        $scope.conceptToBeUpdated = {
            taxonomyId: $scope.selectedTaxonomyId,
            identifier: $scope.selectedConcept.identifier,
            tags: $scope.selectedConcept.tags,
            properties: {},
            newMetadata: $scope.selectedConcept.newMetadata
        }
        var index = 1;
        $scope.commitMessage = "Following are the changes made:\n";

        // Check for tag changes
        var deletedTags = _.difference($scope.unmodifiedConcept.tags, $scope.selectedConcept.tags);
        var addedTags = _.difference($scope.selectedConcept.tags, $scope.unmodifiedConcept.tags);
        if(deletedTags && deletedTags.length > 0) {
            _.each(deletedTags, function(tag) {
                $scope.commitMessage += index++ + '. "' + tag + '" tag is removed\n';
            })
        }
        if(addedTags && addedTags.length > 0) {
            _.each(addedTags, function(tag) {
                $scope.commitMessage += index++ + '. "' + tag + '" tag is added\n';
            })
        }
        // Check for property changes
        if($scope.selectedConcept.newMetadata && $scope.selectedConcept.newMetadata.length > 0) {
            _.each($scope.selectedConcept.newMetadata, function(prop) {
                $scope.commitMessage += index++ + '. New metadata "' + prop.title + '" is added\n';
                $scope.conceptToBeUpdated.properties[prop.propertyName] = $scope.selectedConcept.metadata[prop.propertyName];
            })
        }
        for(k in $scope.selectedConcept.metadata) {
            var prop = _.where($scope.selectedTaxonomy.properties, {'propertyName': k})[0];
            var oldValue = $scope.unmodifiedConcept.metadata[k];
            var newValue = $scope.selectedConcept.metadata[k];
            if(_.isArray(newValue)) {
                oldValue = oldValue || [];
                if(_.difference(oldValue, newValue).length > 0 || _.difference(newValue, oldValue).length > 0) {
                    $scope.commitMessage += index++ + '. Metadata "' + prop.title + '" value is updated from "' + oldValue + '" to "' + newValue + '"\n';
                    $scope.conceptToBeUpdated.properties[k] = newValue;
                }
            } else {
                if(!_.isEqual(oldValue, newValue)) {
                    oldValue = oldValue || '';
                    $scope.commitMessage += index++ + '. Metadata "' + prop.title + '" value is updated from "' + oldValue + '" to "' + newValue + '"\n';
                    $scope.conceptToBeUpdated.properties[k] = newValue;
                }
            }
        }
        $('#saveChangesModal').modal('show');
    }

    $scope.resetRelations = function() {
        var relations = {};
        _.each($scope.selectedTaxonomy.definitions.inRelations, function(defRel) {
            var inRelObjects = _.map(_.where($scope.selectedConcept.inRelations, {"relationType": defRel.relationName}), function(inRel) {
                return {id: inRel.startNodeId, name: inRel.startNodeName, direction: "in", relationType: inRel.relationType, objectType: inRel.startNodeObjectType};
            });
            relations[defRel.title] = inRelObjects;
        });

        _.each($scope.selectedTaxonomy.definitions.outRelations, function(defRel) {
            var outRelObjects = _.map(_.where($scope.selectedConcept.outRelations, {"relationType": defRel.relationName}), function(outRel) {
                return {id: outRel.endNodeId, name: outRel.endNodeName, direction: "out", relationType: outRel.relationType, objectType: outRel.endNodeObjectType};
            });
            relations[defRel.title] = _.reject(outRelObjects, function(r) { return r.objectType == "Media";});
        });

        $scope.selectedConcept.relations = relations;
    }

    $scope.addComment = function() {
        $scope.showNewComment = true;
        $scope.currentComment = undefined;
        $('#commentModal').modal('show');
    }

    $scope.showComment = function(comment) {
        $scope.showNewComment = false;
        $scope.currentComment = comment;
        service.getCommentThread($scope.selectedTaxonomyId, $scope.selectedConcept.identifier, $scope.currentComment.id).then(function(data) {
            $scope.getReplies(data, $scope.currentComment, $scope.currentComment.id);
        });
        $('#commentModal').modal('show');
    }

    $scope.collapseIcon = true;

    $scope.showAllComments = function(auditHistory) {
        $scope.showNewComment = false;
        $scope.auditHistoryObjects = auditHistory;
        $('#auditLogModal').modal('show');

    }

    $scope.getCommentReplies = function(comment){
            $scope.currentComment = comment;
            service.getCommentThread($scope.selectedTaxonomyId, $scope.selectedConcept.identifier, $scope.currentComment.id).then(function(data) {
            $scope.getReplies(data, $scope.currentComment, $scope.currentComment.id);
        });

    }

    $scope.getReplies = function(data, comment, id) {
        var replies = _.where(data, {replyTo: id+''});
        comment.replies = replies;
        if(replies.length > 0) {
            replies.forEach(function(reply) {
                $scope.getReplies(data, reply, reply.id+'');
            });
        }
    }

    $scope.auditCommentReply = function(comment){
         var reply = $('#auditTextArea'+comment.id).val();
         $scope.sendComment(comment, reply);
         $('#auditTextArea'+comment.id).val("");
    }

    $scope.replyComment = function(comment) {
        var reply = $('#commentText'+comment.id).val();
         $scope.sendComment(comment, reply);
         $('#commentText'+comment.id).val("");
    }

    $scope.sendComment = function(comment, reply){
        if(reply) {
            var data = {
                taxonomyId: $scope.selectedTaxonomyId,
                comment: reply,
                objectId: $scope.selectedConcept.identifier,
                threadId: comment.threadId || comment.id,
                replyTo: comment.id
            };
            service.saveComment(data).then(function(resp) {
                comment.showForm = false;
                resp.replies = [];
                if(comment.replies == null) comment.replies = [];
                comment.replies.push(resp);
            });
        }
    }

    $scope.saveComment = function() {
        var newComment = $('#newCommentTextArea').val();
        if(newComment) {
            var data = {
                taxonomyId: $scope.selectedTaxonomyId,
                comment: newComment,
                objectId: $scope.selectedConcept.identifier
            };
            service.saveComment(data).then(function(resp) {
                $('#newCommentTextArea').val("");
                $('#commentsModal').modal('hide');
                resp.replies = [];
                $scope.selectedConcept.comments.push(resp);
            });
        }
    }

    $scope.selectSunburstConcept = function(cid) {
        $state.go('learningMap', {id: $scope.selectedTaxonomyId});
        $timeout(function(){
            var delay = 100;
            if(!$rootScope.sunburstLoaded) {
                delay = 2000;
            }
            $timeout(function(){
                selectSunburstConcept(cid);
            }, delay);
        }, 100);
    }

    $scope.viewGame = function(gameId) {
        $state.go('gamePage', {id: gameId});
    }

    $scope.updatePkgInformation = function(propName, fileObj) {
      if (propName.toLowerCase() === 'downloadurl') {
          // Update Package Version
          var currentVer = $scope.selectedConcept.metadata['pkgVersion'];
          console.log('Current Version: ', currentVer);
          if (currentVer == undefined || currentVer == null) {
            $scope.selectedConcept.metadata['pkgVersion'] = 1;
          }else {
            $scope.selectedConcept.metadata['pkgVersion'] = currentVer + 1;
          }

          if (fileObj) {
            // Update Package Size
            $scope.selectedConcept.metadata['size'] = fileObj.size;

            // Update Package Format
            if (fileObj.type.toLowerCase() === 'application/zip') {
              $scope.selectedConcept.metadata['format'] = "ZIP";
            }else {
              $scope.selectedConcept.metadata['format'] = fileObj.type;
            }
          }
          console.log('Updated Version: ', $scope.selectedConcept.metadata['pkgVersion']);
      }
    }

    $scope.setRemoteUploadFolder = function(propName) {
      if (propName.toLowerCase() === 'downloadurl') {
        $scope.remoteUploadFolder = "content";
      }else {
        $scope.remoteUploadFolder = "games";
      }
    }

    $scope.uploadFile = function(propName) {
        var fileObj = $scope.selectedConcept.filesToUpload[propName];
        if (fileObj && fileObj != null) {
            var type = fileObj.type;
            console.log("File oj type:" + type);
            if (type.indexOf('image') == 0 || type.indexOf('application/zip') == 0) {
                if (fileObj.size && fileObj.size > 0) {
                    $scope.setRemoteUploadFolder(propName);
                    var fd = new FormData();console.log('File Descriptor: ', fileObj);
                    fd.append('document', fileObj);
                    fd.append('folderName', $scope.remoteUploadFolder);
                    console.log('Uploading Content to: ', $scope.remoteUploadFolder);
                    $scope.selectedConcept.uploading[propName] = true;
                    service.uploadFile(fd).then(function(data) {
                        $scope.selectedConcept.uploading[propName] = false;
                        $scope.selectedConcept.metadata[propName] = data.url;
                        $scope.updatePkgInformation(propName, fileObj);
                        console.log(propName);
                    }).catch(function(err) {
                        console.log('Error While File Upload', err);
                        $scope.selectedConcept.uploading[propName] = false;
                        alert('File upload failed: ' + err.errorMsg);
                    });
                } else {
                    alert('Selected file size is 0 bytes. Please select another file');
                }
            } else {
                alert('Only images, audio, video and zipfiles are allowed');
            }
        } else {
            alert('Please select a file to upload');
        }
    }



}]);

app.controller('LearningMapController', ['$scope', '$timeout', '$rootScope', '$stateParams', '$state', 'PlayerService', function($scope, $timeout, $rootScope, $stateParams, $state, service) {
    $rootScope.sunburstLoaded = false;
    $scope.$parent.selectedTaxonomyId = $stateParams.id;
    $scope.sbConcept = undefined, $scope.$parent.selectedConcept = undefined, $scope.$parent.unmodifiedConcept = undefined, $scope.showSunburst = true, $scope.showTree = false;
    $scope.newConcept = {
        taxonomyId: $scope.$parent.selectedTaxonomyId,
        name: undefined,
        description: undefined,
        objectType: $scope.taxonomyObjects[0],
        parent: undefined,
        errorMessages: [],
        comment: undefined
    }

    $scope.$parent.categories = [
        {id: 'general', label: "General", editable: true, editMode: false},
        {id: 'tags', label: "Tags", editable: true, editMode: false},
        {id: 'relations', label: "Relations", editable: false, editMode: false},
        {id: 'lifeCycle', label: "Lifecycle", editable: true, editMode: false},
        {id: 'usageMetadata', label: "Usage Metadata", editable: true, editMode: false},
        {id: 'analytics', label: "Analytics", editable: false, editMode: false},
        {id: 'comments', label: "Comments", editable: false, editMode: true}
    ]

    $scope.$parent.selectedTaxonomy = $scope.$parent.taxonomies[$stateParams.id];
    $scope.getTaxonomyDefinitions = function(taxonomyId) {
        service.getTaxonomyDefinitions(taxonomyId).then(function(taxonomyDefs) {
            var categories = _.uniq(_.pluck(taxonomyDefs.properties, 'category'));
            var definitions = {

            }
            _.each(categories, function(category) {
                definitions[category] = _.where(taxonomyDefs.properties, {'category': category});
            });
            $scope.$parent.selectedTaxonomy.properties = taxonomyDefs.properties;
            $scope.$parent.selectedTaxonomy.definitions = definitions;
            $scope.$parent.selectedTaxonomy.definitions.relations = taxonomyDefs.relations;
            $scope.$parent.selectedTaxonomy.definitions.inRelations = taxonomyDefs.inRelations;
            $scope.$parent.selectedTaxonomy.definitions.outRelations = taxonomyDefs.outRelations;
            $scope.$parent.selectedTaxonomy.definitions.systemTags = taxonomyDefs.systemTags;
        }).catch(function(err) {
            console.log('Error fetching taxonomy definitions - ', err);
        });
    }

    $scope.getTaxonomyGraph = function(taxonomyId) {
        service.getTaxonomyGraph(taxonomyId).then(function(data) {
            $scope.conceptGraph = data.paginatedGraph;
            $scope.$parent.selectedTaxonomy.graph = data.graph;
            $scope.sbConcept = data.graph;
            $scope.getConcept();
            $timeout(function() {
                loadSunburst($scope);
                registerLeftMenu();
            }, 1000);
            $scope.setTaxonomyGroups(data.graph, data.nodes);
        }).catch(function(err) {
            console.log('Error fetching taxnomy graph - ', err);
        });
    }

    $scope.getConcept = function() {
        service.getConcept($scope.sbConcept.conceptId, $scope.$parent.selectedTaxonomyId).then($scope.setConceptResponse);
    }

    $scope.setConceptResponse = function(data) {
        $scope.$parent.selectedConcept = data;
        $scope.$parent.selectedConcept.newMetadata = [];
        $scope.$parent.selectedConcept.relatedConceptsLimit = service.rhsSectionObjectsLimit;
        $scope.$parent.selectedConcept.relatedGamesLimit = service.rhsSectionObjectsLimit;
        $scope.resetRelations();
        $scope.$parent.unmodifiedConcept = angular.copy($scope.$parent.selectedConcept);
        $scope.resetCategories();
        $scope.$parent.selectedConcept.filesToUpload = {};
    }

    $scope.getTaxonomyDefinitions($stateParams.id);
    $scope.getTaxonomyGraph($stateParams.id);

    $scope.$on('selectConcept', function(event, args) {
        $scope.sbConcept = args.concept;
        $scope.getConcept();
    });

    $scope.saveChanges = function($event) {
        $scope.buttonLoading($event);
        $scope.$parent.conceptToBeUpdated.comment = $scope.commitMessage;
        service.updateConcept($scope.$parent.conceptToBeUpdated).then(function(data) {
            $scope.setConceptResponse(data);
            $scope.buttonReset($event);
            $scope.getConcept();
            $('#saveChangesModal').modal('hide');
            $scope.showConformationMessage('alert-success','Concept updated successfully.');
        }).catch(function(err) {
            $scope.$parent.validationMessages = [];
            if(_.isArray(err.errorMsg)) {
                $scope.$parent.validationMessages.push.apply($scope.$parent.validationMessages, err.errorMsg);
            } else {
                $scope.$parent.validationMessages.push(err.errorMsg);
            }
            $scope.buttonReset($event);
            $('#saveChangesModal').modal('hide');
        });
    }

    $scope.onObjectTypeChange = function() {
        $scope.newConcept.parent = undefined;
    }

    $rootScope.isNameExist = function(conceptName, oldConceptName) {
        var allConcepts = $scope.allConcepts;
        if(oldConceptName) allConcepts = _.without(allConcepts, _.findWhere(allConcepts, {name: oldConceptName}));
        var concept = _.findWhere(allConcepts, {name: conceptName});
        if(concept) return true;
        else return false;
    }

    $rootScope.isCodeExist = function(code, oldCode) {
        var allConcepts = $scope.allConcepts;
        if(oldCode) allConcepts = _.without(allConcepts, _.findWhere(allConcepts, {code: oldCode}));
        var concept = _.findWhere(allConcepts, {code: code});
        if(concept) return true;
        else return false;
    }

    $scope.createConcept = function($event) {
        var objType = $scope.newConcept.objectType.id;
        $scope.newConcept.errorMessages = [];
        var valid = true;
        if(objType != 'concept') {
            if(_.isEmpty($scope.newConcept.parent)) {
                $scope.newConcept.errorMessages.push((objType == 'subConcept' ? 'Broad Concept' : 'Sub Concept') + ' is required');
                valid = false;
            }
        }
        if(_.isEmpty($scope.newConcept.name)) {
            $scope.newConcept.errorMessages.push('Name is required');
            valid = false;
        }
        if(_.isEmpty($scope.newConcept.code)) {
            $scope.newConcept.errorMessages.push('Code is required');
            valid = false;
        }
        if(!valid) {
            return;
        }
        if($rootScope.isNameExist($scope.newConcept.name)) {
            $scope.newConcept.errorMessages.push('Name already in use. Try another.');
            return;
        }
        if($rootScope.isCodeExist($scope.newConcept.code)) {
            $scope.newConcept.errorMessages.push('Code already in use.  Try another.');
            return;
        }

        $scope.buttonLoading($event);
        service.createConcept($scope.newConcept).then(function(data) {
            $scope.showConformationMessage('alert-success','Concept created successfully. Refreshing the visualization...');
            $scope.sbConcept = {conceptId: data.id, name: $scope.newConcept.name};
            service.getTaxonomyGraph($scope.$parent.selectedTaxonomyId).then(function(data) {
                $scope.conceptGraph = data.paginatedGraph;
                $scope.$parent.selectedTaxonomy.graph = data.graph;
                $scope.setTaxonomyGroups(data.graph, data.nodes);
                if($scope.showSunburst) {
                    $scope.showSunburst = false;
                    $timeout(function() {
                        $scope.selectVisualization('sunburst');
                    }, 1000);
                } else {
                    $scope.showTree = false;
                    $scope.selectVisualization('tree');
                }
                $scope.buttonReset($event);
                $("#writeIcon").trigger('click');
            }).catch(function(err) {
                $scope.newConcept.errorMessages = [];
                if(_.isArray(err.errorMsg)) {
                    $scope.newConcept.errorMessages.push.apply($scope.errorMessages, err.errorMsg);
                } else {
                    $scope.newConcept.errorMessages.push(err.errorMsg);
                }
                $scope.buttonReset($event);
                $scope.showConformationMessage('alert-danger','Error while fetching taxnomy graph.');
            });
        }).catch(function(err) {
            $scope.newConcept.errorMessages = [];
            if(_.isArray(err.errorMsg)) {
                $scope.newConcept.errorMessages.push.apply($scope.newConcept.errorMessages, err.errorMsg);
            } else {
                $scope.newConcept.errorMessages.push(err.errorMsg);
            }
            $scope.buttonReset($event);
        });
    }

    $scope.allConcepts = [];
    $scope.concepts = [];
    $scope.subConcepts = [];
    $scope.microConcepts = [];
    $scope.setTaxonomyGroups = function(graph, allNodes) {
        if(allNodes && allNodes.length > 0) {
            $scope.allConcepts = allNodes;
        }
        if(graph.children && graph.children.length > 0) {
            _.each(graph.children, function(node) {
                if(node.level == 1) {
                    $scope.concepts.push({name: node.name, id: node.conceptId});
                }
                if(node.level == 2) {
                    $scope.subConcepts.push({name: node.name, id: node.conceptId});
                }
                if(node.level == 3) {
                    $scope.microConcepts.push({name: node.name, id: node.conceptId});
                } else {
                    $scope.setTaxonomyGroups(node);
                }
            });
        }
    }

    $scope.selectVisualization = function(type) {
        if(type == 'tree') {
            $scope.showSunburst = false;
            $scope.showTree = true;
            if($scope.treeLoaded) {
                expandANode($scope.sbConcept.conceptId, $scope.sbConcept.name);
            } else {
                loadTree($scope);
            }
            $scope.treeLoaded = true;
        } else {
            $scope.showSunburst = true;
            $scope.showTree = false;
            loadSunburst($scope);
        }
    }

    $scope.selectConcept = function(conceptObj) {
        $scope.sbConcept = conceptObj;
        $scope.hoveredConcept = conceptObj;
        $scope.getConcept();
    }

}]);

app.controller('GameListController', ['$scope', '$timeout', '$rootScope', '$stateParams', '$state', 'PlayerService', function($scope, $timeout, $rootScope, $stateParams, $state, service) {

    $scope.$parent.selectedTaxonomyId = $stateParams.id;
    $scope.offset = 0;
    $scope.limit = 10;
    $scope.games = [];
    $scope.seeMoreGames = false;
    $scope.showGames = false;

    $scope.newGame = {
        taxonomyId: $scope.$parent.selectedTaxonomyId,
        name: undefined,
        code: undefined,
        appIcon: undefined,
        owner: undefined,
        developer: undefined,
        description: undefined,
        errorMessages: [],
        comment: undefined
    }

    $scope.getGames = function() {
        $scope.showGames = false;
        var taxonomyId = $scope.$parent.selectedTaxonomyId;
        service.getGames(taxonomyId, $scope.offset, $scope.limit).then(function(data) {
            if (data.games && data.games.length > 0) {
                $scope.games.push.apply($scope.games, data.games);
                $scope.showGames = true;
                var count = data.count;
                if (count > ($scope.offset + $scope.limit)) {
                    $scope.seeMoreGames = true;
                } else {
                    $scope.seeMoreGames = false;
                }
            } else {
                $scope.seeMoreGames = false;
            }
        }).catch(function(err) {
            console.log('Error fetching games list - ', err);
        });
    }

    $scope.nextPage = function() {
        $scope.offset = $scope.offset + $scope.limit;
        $scope.getGames();
    }

    $scope.viewGame = function(gameId) {
        $state.go('gamePage', {id: gameId});
    }

    $scope.getGames();

    $scope.createGame = function($event) {
        var valid = true;
        if(_.isEmpty($scope.newGame.name)) {
            $scope.newGame.errorMessages.push('Name is required');
            valid = false;
        }
        if(_.isEmpty($scope.newGame.code)) {
            $scope.newGame.errorMessages.push('Code is required');
            valid = false;
        }
        if(_.isEmpty($scope.newGame.appIcon)) {
            $scope.newGame.errorMessages.push('Logo is required');
            valid = false;
        }
        if(_.isEmpty($scope.newGame.owner)) {
            $scope.newGame.errorMessages.push('Owner is required');
            valid = false;
        }
        if(_.isEmpty($scope.newGame.developer)) {
            $scope.newGame.errorMessages.push('Developer is required');
            valid = false;
        }

        if(!valid) {
            return;
        }

        $scope.buttonLoading($event);
        $scope.newGame.posterImage = $scope.newGame.appIcon;
        service.createGame($scope.newGame).then(function(data) {
            $scope.showConformationMessage('alert-success','Game created successfully.');
            $scope.buttonReset($event);
            $("#writeIcon").trigger('click');
            $scope.getGames();
        }).catch(function(err) {
            $scope.errorMessages = [];
            $scope.errorMessages.push(err.errorMsg);
            $scope.buttonReset($event);
            $scope.showConformationMessage('alert-danger','Error saving game.');
        })
    }
    $timeout(function() {
        selectLeftMenuTab('forumsTab');
    }, 1000);
}]);

app.controller('GameController', ['$scope', '$timeout', '$rootScope', '$stateParams', '$state', 'PlayerService', function($scope, $timeout, $rootScope, $stateParams, $state, service) {

    $scope.showFullDesc = false;
     $scope.uploadScreenshots = function($event, propName) {
        var fileObj = $scope.selectedConcept.filesToUpload[propName];
        if (fileObj && fileObj != null) {
            var type = fileObj.type;
            if (type.indexOf('image') == 0 || type.indexOf('video') == 0) {
                if (fileObj.size && fileObj.size > 0) {
                    $scope.buttonLoading($event);
                    var fd = new FormData();
                    fd.append('document', fileObj);
                    fd.append('folderName', 'games');
                    $scope.selectedConcept.uploading[propName] = true;
                    var mediaval = (type.indexOf('image') == 0 ? "image" : "video");
                    service.uploadFile(fd).then(function(data) {
                            $scope.newMedia = {
                                taxonomyId: $scope.selectedTaxonomyId,
                                gameId: propName.identifier,
                                url: data.url,
                                mimeType : type,
                                mediaType : mediaval
                            }
                        service.getMedia($scope.newMedia).then(function(data){
                            var screenshot = {
                                              "identifier": data,
                                              "mediaUrl": $scope.newMedia.url,
                                              "mediaType": $scope.newMedia.mediaType,
                                              "mimeType": $scope.newMedia.mimeType,
                                              "posterImage": null,
                                              "title": null,
                                              "description": null,
                                              "metadata": {
                                                "identifier": data
                                              }
                                            };
                            $scope.$parent.selectedConcept.screenshots.push(screenshot);
                            $scope.showConformationMessage('alert-success','Media uploaded successfully.');
                            setTimeout(function() {
                                $scope.resetSlider();
                            }, 500);
                            $scope.buttonReset($event);
                        });
                    }).catch(function(err) {
                        // $scope.selectedConcept.uploading[propName] = false;
                        $scope.showConformationMessage('alert-danger','Media upload failed: ' + err.errorMsg);
                        $scope.buttonReset($event);
                    });
                } else {
                    $scope.showConformationMessage('alert-danger', 'Selected file size is 0 bytes. Please select another file');
                }
            } else {
                $scope.showConformationMessage('alert-danger', 'Only images, audio and video files are allowed');
            }
        } else {
            $scope.showConformationMessage('alert-danger', 'Please select a file to upload');
        }
    }

    $scope.moreDescription = function() {
        $scope.showFullDesc = true;
    }

    $scope.lessDescription = function() {
        $scope.showFullDesc = false;
    }

    $scope.viewGameList = function() {
        $state.go('gameList', {id: $scope.$parent.selectedTaxonomyId});
    }

    $scope.$parent.categories = [
        {id: 'general', label: "General", editable: true, editMode: false},
        {id: 'tags', label: "Tags", editable: true, editMode: false},
        {id: 'relations', label: "Relations", editable: false, editMode: false},
        {id: 'pedagogy', label: "Pedagogy", editable: true, editMode: false},
        {id: 'gameExperience', label: "Game Experience", editable: true, editMode: false},
        {id: 'analytics', label: "Analytics", editable: false, editMode: false},
        {id: 'technical', label: "Technical", editable: true, editMode: false},
        {id: 'ownership', label: "Ownership", editable: true, editMode: false},
        {id: 'lifeCycle', label: "Lifecycle", editable: true, editMode: false},
        {id: 'comments', label: "Comments", editable: false, editMode: false}
    ]

    $scope.$parent.selectedTaxonomy = $scope.$parent.taxonomies[$scope.$parent.selectedTaxonomyId];
    $scope.selectedGameId = $stateParams.id;

    $scope.getGameDefinition = function(taxonomyId) {
        service.getGameDefinition(taxonomyId).then(function(taxonomyDefs) {
            var categories = _.uniq(_.pluck(taxonomyDefs.properties, 'category'));
            var definitions = {

            }
            _.each(categories, function(category) {
                definitions[category] = _.where(taxonomyDefs.properties, {'category': category});
            });
            $scope.$parent.selectedTaxonomy.properties = taxonomyDefs.properties;
            $scope.$parent.selectedTaxonomy.definitions = definitions;
            $scope.$parent.selectedTaxonomy.definitions.relations = taxonomyDefs.outRelations;
            $scope.$parent.selectedTaxonomy.definitions.inRelations = taxonomyDefs.inRelations;
            $scope.$parent.selectedTaxonomy.definitions.outRelations = taxonomyDefs.outRelations;
            $scope.$parent.selectedTaxonomy.definitions.systemTags = taxonomyDefs.systemTags;
            $scope.getGame($scope.$parent.selectedTaxonomyId);
        }).catch(function(err) {
            console.log('Error fetching taxonomy definitions - ', err);
        });
    }

    $scope.getGame = function(taxonomyId) {
        service.getGame(taxonomyId, $scope.selectedGameId).then($scope.setGameResponse);
    }

    $scope.viewScreenShot = function(screenshot) {
        $scope.selectedScreenshot = screenshot;
        if (screenshot.mediaType == 'video') {
            if (screenshot.mimeType == 'video/youtube') {
                setTimeout(function() {
                    document.getElementById('selectedScreenshotIframe').src = screenshot.mediaUrl;
                }, 500);
            }
        }
    }

    $scope.setGameResponse = function(data) {
        $scope.$parent.selectedConcept = data;
        $scope.$parent.selectedConcept.newMetadata = [];
        $scope.$parent.selectedConcept.relatedConceptsLimit = service.rhsSectionObjectsLimit;
        $scope.$parent.selectedConcept.relatedGamesLimit = service.rhsSectionObjectsLimit;
        $scope.resetRelations();
        $scope.$parent.selectedConcept.filesToUpload = {};
        $scope.$parent.selectedConcept.uploading = {};
        $scope.$parent.unmodifiedConcept = angular.copy($scope.$parent.selectedConcept);
        $scope.resetCategories();
        setTimeout(function() {
            $('.tool-tip').tooltip();
            if ($scope.$parent.selectedConcept.screenshots && $scope.$parent.selectedConcept.screenshots.length > 0) {
                $scope.resetSlider();
            }
        }, 500);
    }

    $scope.resetSlider = function() {
        if ($scope.slider && $scope.slider != null) {
            $scope.slider.destroySlider();
        }
        $scope.slider = $('.bxslider').bxSlider({
            minSlides: 2,
            maxSlides: 5,
            slideWidth: 320,
            slideMargin: 20,
            pager: false,
            infiniteLoop: false,
            hideControlOnEnd: true,
            responsive: false,
            onSliderLoad: function() {
                $('ul.bxslider li').width('auto');
            }
        });
    }

    $scope.saveChanges = function($event) {
        $scope.buttonLoading($event);
        $scope.$parent.conceptToBeUpdated.comment = $scope.commitMessage;
        service.updateGame($scope.$parent.conceptToBeUpdated).then(function(data) {
            $scope.buttonReset($event);
            $scope.getGame($scope.$parent.selectedTaxonomyId);
            $('#saveChangesModal').modal('hide');
            $scope.showConformationMessage('alert-success','Game updated successfully.');
        }).catch(function(err) {
            $scope.$parent.validationMessages = [];
            $scope.$parent.validationMessages.push(err.errorMsg);
            $scope.buttonReset($event);
            $('#saveChangesModal').modal('hide');
            $scope.showConformationMessage('alert-danger','Error while updating game.');
        });
    }

    $scope.getGameDefinition($scope.$parent.selectedTaxonomyId);

}]);

app.controller('ContentListController', ['$scope', '$timeout', '$rootScope', '$stateParams', '$state', 'PlayerService', function($scope, $timeout, $rootScope, $stateParams, $state, service) {

    $scope.$parent.selectedTaxonomyId = $stateParams.id;
    $scope.selectedContentType = 'Story';
    $scope.$parent.selectedContentType = 'Story';
    $scope.offset = 0;
    $scope.limit = 10;
    $scope.contents = [];
    $scope.seeMoreContents = false;
    $scope.showContents = false;

    $scope.newContent = {
        contentType: $scope.$parent.selectedContentType,
        taxonomyId: $scope.$parent.selectedTaxonomyId,
        status: "Live",
        name: undefined,
        code: undefined,
        appIcon: undefined,
        owner: undefined,
        body: undefined,
        description: undefined,
        errorMessages: [],
        comment: undefined
    }

    $scope.getContents = function() {
        $scope.showContents = false;
        var contentType = $scope.selectedContentType;
        var taxonomyId = $scope.$parent.selectedTaxonomyId;
        service.getContents(contentType, taxonomyId, $scope.offset, $scope.limit).then(function(data) {
            if (data.contents && data.contents.length > 0) {
                $scope.contents = [];
                $scope.contents.push.apply($scope.contents, data.contents);
                $scope.showContents = true;
                var count = data.count;
                if (count > ($scope.offset + $scope.limit)) {
                    $scope.seeMoreContents = true;
                } else {
                    $scope.seeMoreContents = false;
                }
            } else {
                $scope.seeMoreContents = false;
            }
        }).catch(function(err) {
            console.log('Error fetching content list - ', err);
        });
    }

    $scope.nextPage = function() {
        $scope.offset = $scope.offset + $scope.limit;
        $scope.getContents();
    }

    $scope.viewContent = function(contentId) {
        $state.go('contentPage', {id: contentId});
    }

    $scope.getContents();
    console.log('ContentListController :: selectedContentType', $scope.$parent.selectedContentType);

    $("select.selectpicker").change(function(){
          var selectedContentType = $(".selectpicker option:selected").val();
          $scope.selectedContentType = selectedContentType;
          $scope.$parent.selectedContentType = selectedContentType;
          $scope.getContents();
    });

    $('.selectpicker').selectpicker({
        style: 'btn-info',
        size: 'false',
  			iconBase: 'fa'
    });

    $scope.createContent = function($event) {
        var valid = true;

        if(_.isEmpty($scope.newContent.name)) {
            $scope.newContent.errorMessages.push('Name is required');
            valid = false;
        }
        if(_.isEmpty($scope.newContent.code)) {
            $scope.newContent.errorMessages.push('Code is required');
            valid = false;
        }
        if(_.isEmpty($scope.newContent.appIcon)) {
            $scope.newContent.errorMessages.push('Logo is required');
            valid = false;
        }
        if(_.isEmpty($scope.newContent.owner)) {
            $scope.newContent.errorMessages.push('Owner is required');
            valid = false;
        }
        if(_.isEmpty($scope.newContent.body)) {
            $scope.newContent.errorMessages.push('Body(XML) is required');
            valid = false;
        }

        if(!valid) {
            return;
        }

        $scope.buttonLoading($event);
        $scope.newContent.posterImage = $scope.newContent.appIcon;
        service.createContent($scope.newContent).then(function(data) {
            $scope.showConformationMessage('alert-success','Content created successfully.');
            $scope.buttonReset($event);
            $("#writeIcon").trigger('click');
            $scope.getContents();
        }).catch(function(err) {
            $scope.errorMessages = [];
            $scope.errorMessages.push(err.errorMsg);
            $scope.buttonReset($event);
            $scope.showConformationMessage('alert-danger','Error saving content.');
        })
    }
    $timeout(function() {
        selectLeftMenuTab('courseTab');
    }, 1000);
}]);

app.controller('ContentController', ['$scope', '$timeout', '$rootScope', '$stateParams', '$state', 'PlayerService', function($scope, $timeout, $rootScope, $stateParams, $state, service) {
// TODO: UploadContent method needs to be generalized for all controller all the differences should be passed as parameter including callback functions
    $scope.showFullDesc = false;
    //  $scope.uploadContent = function($event, propName) {
    //     var fileObj = $scope.selectedConcept.filesToUpload[propName];
    //     if (fileObj && fileObj != null) {
    //         var type = fileObj.type;
    //         if (type.indexOf('application/zip') == 0) {
    //             if (fileObj.size && fileObj.size > 0) {
    //                 $scope.buttonLoading($event);
    //                 var fd = new FormData();
    //                 fd.append('document', fileObj);
    //                 fd.append('folderName', 'content');
    //                 $scope.selectedConcept.uploading[propName] = true;
    //                 var mediaval = (type.indexOf('application/zip') == 0 ? "zip" : "other");
    //                 service.uploadFile(fd).then(function(data) {debugger;
    //                   $scope.newContent = {
    //                       taxonomyId: $scope.selectedTaxonomyId,
    //                       contentId: propName.identifier,
    //                       url: data,
    //                       mimeType : type,
    //                       mediaType : mediaval
    //                   }
    //                   $scope.showConformationMessage('alert-success','Content uploaded successfully.');
    //                   $scope.buttonReset($event);
    //                 }).catch(function(err) {
    //                     $scope.showConformationMessage('alert-danger','Content upload failed: ' + err.errorMsg);
    //                     $scope.buttonReset($event);
    //                 });
    //             } else {
    //                 $scope.showConformationMessage('alert-danger', 'Selected file size is 0 bytes. Please select another file');
    //             }
    //         } else {
    //             $scope.showConformationMessage('alert-danger', 'Only zip files are allowed');
    //         }
    //     } else {
    //         $scope.showConformationMessage('alert-danger', 'Please select a file to upload');
    //     }
    // }

    $scope.moreDescription = function() {
        $scope.showFullDesc = true;
    }

    $scope.lessDescription = function() {
        $scope.showFullDesc = false;
    }

    $scope.viewContentList = function() {
        $state.go('contentList', {id: $scope.$parent.selectedTaxonomyId});
    }

    $scope.$parent.categories = [
        {id: 'general', label: "General", editable: true, editMode: false},
        {id: 'tags', label: "Tags", editable: true, editMode: false},
        {id: 'relations', label: "Relations", editable: false, editMode: false},
        {id: 'pedagogy', label: "Pedagogy", editable: true, editMode: false},
        {id: 'gameExperience', label: "Game Experience", editable: true, editMode: false},
        {id: 'analytics', label: "Analytics", editable: false, editMode: false},
        {id: 'audit', label: "Audit", editable: false, editMode: false},
        {id: 'technical', label: "Technical", editable: true, editMode: false},
        {id: 'ownership', label: "Ownership", editable: true, editMode: false},
        {id: 'lifeCycle', label: "Lifecycle", editable: true, editMode: false},
        {id: 'comments', label: "Comments", editable: false, editMode: false}
    ]

    $scope.$parent.selectedTaxonomy = $scope.$parent.taxonomies[$scope.$parent.selectedTaxonomyId];
    $scope.selectedContentId = $stateParams.id;

    $scope.getContentDefinition = function(taxonomyId, contentType) {
        service.getContentDefinition(taxonomyId, contentType).then(function(taxonomyDefs) {
            var categories = _.uniq(_.pluck(taxonomyDefs.properties, 'category'));
            var definitions = {

            }
            _.each(categories, function(category) {
                definitions[category] = _.where(taxonomyDefs.properties, {'category': category});
            });
            $scope.$parent.selectedTaxonomy.properties = taxonomyDefs.properties;
            $scope.$parent.selectedTaxonomy.definitions = definitions;
            $scope.$parent.selectedTaxonomy.definitions.relations = taxonomyDefs.outRelations;
            $scope.$parent.selectedTaxonomy.definitions.inRelations = taxonomyDefs.inRelations;
            $scope.$parent.selectedTaxonomy.definitions.outRelations = taxonomyDefs.outRelations;
            $scope.$parent.selectedTaxonomy.definitions.systemTags = taxonomyDefs.systemTags;
            $scope.getContent($scope.selectedContentId, $scope.$parent.selectedTaxonomyId, $scope.$parent.selectedContentType);
        }).catch(function(err) {
            console.log('Error fetching content definitions - ', err);
        });
    }

    $scope.getContent = function(contentId, taxonomyId, contentType) {
        service.getContent(contentId, taxonomyId, contentType).then($scope.setContentResponse);
    }

    $scope.viewScreenShot = function(screenshot) {
        $scope.selectedScreenshot = screenshot;
        if (screenshot.mediaType == 'video') {
            if (screenshot.mimeType == 'video/youtube') {
                setTimeout(function() {
                    document.getElementById('selectedScreenshotIframe').src = screenshot.mediaUrl;
                }, 500);
            }
        }
    }

    $scope.setContentResponse = function(data) {console.log(data);
        $scope.$parent.selectedConcept = data;
        $scope.$parent.selectedConcept.newMetadata = [];
        $scope.$parent.selectedConcept.relatedConceptsLimit = service.rhsSectionObjectsLimit;
        $scope.$parent.selectedConcept.relatedContentsLimit = service.rhsSectionObjectsLimit;
        $scope.resetRelations();
        $scope.$parent.selectedConcept.filesToUpload = {};
        $scope.$parent.selectedConcept.uploading = {};
        $scope.$parent.unmodifiedConcept = angular.copy($scope.$parent.selectedConcept);
        $scope.resetCategories();
        setTimeout(function() {
            $('.tool-tip').tooltip();
            if ($scope.$parent.selectedConcept.screenshots && $scope.$parent.selectedConcept.screenshots.length > 0) {
                $scope.resetSlider();
            }
        }, 500);
    }

    $scope.resetSlider = function() {
        if ($scope.slider && $scope.slider != null) {
            $scope.slider.destroySlider();
        }
        $scope.slider = $('.bxslider').bxSlider({
            minSlides: 2,
            maxSlides: 5,
            slideWidth: 320,
            slideMargin: 20,
            pager: false,
            infiniteLoop: false,
            hideControlOnEnd: true,
            responsive: false,
            onSliderLoad: function() {
                $('ul.bxslider li').width('auto');
            }
        });
    }

    $scope.saveChanges = function($event) {
        $scope.buttonLoading($event);
        $scope.$parent.conceptToBeUpdated.comment = $scope.commitMessage;
        $scope.$parent.conceptToBeUpdated.contentType = $scope.$parent.selectedContentType;
        // TODO: use 'conceptToBeUpdated' instead of 'selectedConcept' Once middleware APIs are ready with partial update
        $scope.$parent.selectedConcept.taxonomyId = $scope.$parent.selectedTaxonomyId;
        $scope.$parent.selectedConcept.selectedContentType = $scope.$parent.selectedContentType;
        service.updateContent($scope.$parent.selectedConcept).then(function(data) {
            $scope.buttonReset($event);
            $scope.getContent($scope.$parent.selectedTaxonomyId);
            $('#saveChangesModal').modal('hide');
            $scope.$parent.resetCategories();
            $scope.showConformationMessage('alert-success','Content updated successfully.');
        }).catch(function(err) {
            $scope.$parent.validationMessages = [];
            $scope.$parent.validationMessages.push(err.errorMsg);
            $scope.buttonReset($event);
            $('#saveChangesModal').modal('hide');
            $scope.$parent.resetCategories();
            $scope.showConformationMessage('alert-danger','Error while updating Content.');
        });
    }

    $scope.getContentDefinition($scope.$parent.selectedTaxonomyId, $scope.$parent.selectedContentType);

}]);


function loadTree($scope) {
    var cid = $scope.sbConcept ? $scope.sbConcept.conceptId : null;
    showDNDTree($scope.conceptGraph, 'treeLayout', {}, $scope, cid);
}

function loadSunburst($scope) {
    // Sunburst Code
    $scope.data;
    $scope.displayVis = false;
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
        d.color = $scope.color(d.name);
    };
    //$scope.color = ["#87CEEB", "#007FFF", "#72A0C1", "#318CE7", "#0000FF", "#0073CF"];
    $scope.color = d3.scale.ordinal().range(["#33a02c", "#1f78b4", "#b2df8a", "#a6cee3", "#fb9a99", "#e31a1c", "#fdbf6f", "#ff7f00", "#6a3d9a", "#cab2d6", "#ffff99"]);

    if ($scope.$parent.selectedTaxonomy.graph) {
        var root = $scope.$parent.selectedTaxonomy.graph;
        $scope.assignColors(root);
        $scope.data = [$scope.$parent.selectedTaxonomy.graph];
        if($scope.sbConcept) {
            $scope.conceptId = $scope.sbConcept.conceptId;
        }
    }
}

// Auto select the concept id
function selectSunburstConcept(cid) {
    var nodes = d3.select("#sunburst").selectAll("#sunburst-path")[0];
    for(var i=0; i< nodes.length; i++) {
        var node = nodes[i];
        var nodeCid = $(node).attr('cid');
        if(nodeCid == cid) {
            var event = document.createEvent("SVGEvents");
            event.initEvent("click",true,true);
            node.dispatchEvent(event);
        }
    }
}

function openCreateArea(thisObj, className) {
    $("#il-Txt-Editor").slideToggle('slow');
    $(thisObj).toggleClass('fa-close');
    $(thisObj).toggleClass(className);
}
