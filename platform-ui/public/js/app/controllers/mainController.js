var app = angular.module('playerApp', ['ui.router', 'readableTime', 'truncate', 'ngSanitize', 'sunburst.services', 'sunburst.directives', 'd3']);

app.config(function($stateProvider) {
    $stateProvider
    .state('learningMap', {
        url: "/learningMap/:id",
        views: {
            "contentSection": {
                templateUrl: "/templates/player/learningMap.html",
                controller: 'LearningMapController'
            },
        }
    })
    .state('gameVisualization', {
            url: "/games/visualization",
            views: {
                "contentSection": {
                    templateUrl: "/templates/player/gameVisualization.html",
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
        })
});

app.service('PlayerService', ['$http', '$q', function($http, $q) {

    this.postToService = function(url, data) {
        var deferred = $q.defer();
        $http.post(url, data).success(function(resp) {
            if (!resp.error)
                deferred.resolve(resp);
            else
                deferred.reject(resp);
        });
        return deferred.promise;
    }

    this.getFromService = function(url, data) {
        var deferred = $q.defer();
        $http.get(url, data).success(function(resp) {
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

    this.getGameCoverage = function(taxonomyId) {
        return this.getFromService('/private/v1/player/gameVis/' + taxonomyId);
    }
}]);

app.controller('PlayerController', ['$scope', '$timeout', '$rootScope', '$stateParams', '$state', 'PlayerService', '$location', '$anchorScroll', function($scope, $timeout, $rootScope, $stateParams, $state, service, $location, $anchorScroll) {

    // Structure of taxonomy is
    // taxonomyId: {
    //   name: 'Numeracy',
    //   identifier: '<id>',
    //   graph: {}
    //   definitions: {}
    // }
    $scope.taxonomies = {};
    $scope.allTaxonomies = [];
    $scope.selectedTaxonomyId = undefined;
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

    $scope.selectTaxonomy = function(taxonomyId) {
        $state.go('learningMap', {id: taxonomyId});
    }

    $scope.showHeatMap = function(taxonomyId) {
        $state.go('gameVisualization');
    }

    $scope.categories = [
        {id: 'general', label: "General", editable: true, editMode: false},
        {id: 'tags', label: "Tags", editable: true, editMode: false},
        {id: 'relations', label: "Relations", editable: false, editMode: false},
        {id: 'lifeCycle', label: "Lifecycle", editable: true, editMode: false},
        {id: 'usageMetadata', label: "Usage Metadata", editable: true, editMode: false},
        {id: 'analytics', label: "Analytics", editable: false, editMode: false},
        {id: 'audit', label: "Audit", editable: false, editMode: false},
        {id: 'comments', label: "Comments", editable: false, editMode: false}
    ]

    $scope.resetCategories = function() {
        _.each($scope.categories, function(cat) {
            cat.editMode = false;
        });
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
}]);

app.controller('LearningMapController', ['$scope', '$timeout', '$rootScope', '$stateParams', '$state', 'PlayerService', function($scope, $timeout, $rootScope, $stateParams, $state, service) {
    $rootScope.sunburstLoaded = false;
    $scope.$parent.selectedTaxonomyId = $stateParams.id;
    $scope.sbConcept = undefined, $scope.selectedConcept = undefined, $scope.unmodifiedConcept = undefined, $scope.showSunburst = true, $scope.showTree = false;
    $scope.newConcept = {
        taxonomyId: $scope.selectedTaxonomyId,
        name: undefined,
        description: undefined,
        objectType: $scope.taxonomyObjects[0],
        parent: undefined,
        errorMessages: []
    }
    $scope.selectedTaxonomy = $scope.$parent.taxonomies[$stateParams.id];
    $scope.getTaxonomyDefinitions = function(taxonomyId) {
        service.getTaxonomyDefinitions(taxonomyId).then(function(taxonomyDefs) {
            var categories = _.uniq(_.pluck(taxonomyDefs.properties, 'category'));
            var definitions = {

            }
            _.each(categories, function(category) {
                definitions[category] = _.where(taxonomyDefs.properties, {'category': category});
            });
            $scope.selectedTaxonomy.properties = taxonomyDefs.properties;
            $scope.selectedTaxonomy.definitions = definitions;
            $scope.selectedTaxonomy.definitions.relations = taxonomyDefs.relations;
            $scope.selectedTaxonomy.definitions.systemTags = taxonomyDefs.systemTags;
        }).catch(function(err) {
            console.log('Error fetching taxonomy definitions - ', err);
        });
    }

    $scope.getTaxonomyGraph = function(taxonomyId) {
        service.getTaxonomyGraph(taxonomyId).then(function(data) {
            $scope.conceptGraph = data.paginatedGraph;
            $scope.selectedTaxonomy.graph = data.graph;
            $scope.sbConcept = data.graph;
            $scope.getConcept();
            $timeout(function() {
                loadSunburst($scope);
                registerLeftMenu();
            }, 1000);
            $scope.setTaxonomyGroups(data.graph);
        }).catch(function(err) {
            console.log('Error fetching taxnomy graph - ', err);
        });
    }

    $scope.getConcept = function() {
        service.getConcept($scope.sbConcept.conceptId, $scope.selectedTaxonomyId).then($scope.setConceptResponse);
    }

    $scope.setConceptResponse = function(data) {
        $scope.selectedConcept = data;
        $scope.selectedConcept.newMetadata = [];
        $scope.unmodifiedConcept = angular.copy($scope.selectedConcept);
        $scope.resetCategories();
    }

    $scope.getTaxonomyDefinitions($stateParams.id);
    $scope.getTaxonomyGraph($stateParams.id);

    $rootScope.$on('selectConcept', function(event, args) {
        $scope.sbConcept = args.concept;
        $scope.getConcept();
    });

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

    $scope.saveChanges = function($event) {
        $scope.buttonLoading($event);
        service.updateConcept($scope.conceptToBeUpdated).then(function(data) {
            $scope.setConceptResponse(data);
            $scope.buttonReset($event);
            $scope.getConcept();
            $('#saveChangesModal').modal('hide');
            $scope.showConformationMessage('alert-success','Concept updated successfully.');
        }).catch(function(err) {
            $scope.validationMessages = [];
            $scope.validationMessages.push(err.errorMsg);
            console.log('saveChanges() - err', err);
            $scope.buttonReset($event);
            $('#saveChangesModal').modal('hide');
            $scope.showConformationMessage('alert-danger','Error while updating concept.');
        });
    }

    $scope.onObjectTypeChange = function() {
        $scope.newConcept.parent = undefined;
    }

    $scope.isNameExist = function(conceptName) {
        var allConcepts = _.union($scope.allConcepts, $scope.allSubConcepts, $scope.allMicroConcepts);
        var concept = _.findWhere(allConcepts, {name: conceptName});
        if(concept) return true;
        else return false;
    }

    $scope.isCodeExist = function(code) {
        var allConcepts = _.union($scope.allConcepts, $scope.allSubConcepts, $scope.allMicroConcepts);
        var concept = _.findWhere(allConcepts, {id: code});
        console.log('code:', code);
        console.log('concept:', concept);
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
        if($scope.isNameExist($scope.newConcept.name)) {
            $scope.newConcept.errorMessages.push('Name already in use. Try another.');
            return;
        }
        if($scope.isCodeExist($scope.newConcept.code)) {
            $scope.newConcept.errorMessages.push('Code already in use.  Try another.');
            return;
        }

        $scope.buttonLoading($event);
        service.createConcept($scope.newConcept).then(function(data) {
            $scope.showConformationMessage('alert-success','Concept created successfully. Refreshing the visualization...');
            $scope.sbConcept = {conceptId: data.id, name: $scope.newConcept.name};
            service.getTaxonomyGraph($scope.selectedTaxonomyId).then(function(data) {
                $scope.conceptGraph = data.paginatedGraph;
                $scope.selectedTaxonomy.graph = data.graph;
                $scope.setTaxonomyGroups(data.graph);
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
                $scope.errorMessages = [];
                $scope.errorMessages.push(err.errorMsg);
                $scope.buttonReset($event);
                $scope.showConformationMessage('alert-danger','Error while fetching taxnomy graph.');
            });
        }).catch(function(err) {
            $scope.errorMessages = [];
            $scope.errorMessages.push(err.errorMsg);
            $scope.buttonReset($event);
            console.log('Error saving concept - ', err);
            $scope.showConformationMessage('alert-danger','Error saving concept.');
        });
    }

    $scope.allConcepts = [];
    $scope.allSubConcepts = [];
    $scope.allMicroConcepts = [];
    $scope.setTaxonomyGroups = function(graph) {
        if(graph.children && graph.children.length > 0) {
            _.each(graph.children, function(node) {
                if(node.level == 1) {
                    $scope.allConcepts.push({name: node.name, id: node.conceptId});
                }
                if(node.level == 2) {
                    $scope.allSubConcepts.push({name: node.name, id: node.conceptId});
                }
                if(node.level == 3) {
                    $scope.allMicroConcepts.push({name: node.name, id: node.conceptId});
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

    if ($scope.selectedTaxonomy.graph) {
        var root = $scope.selectedTaxonomy.graph;
        $scope.assignColors(root);
        $scope.data = [$scope.selectedTaxonomy.graph];
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