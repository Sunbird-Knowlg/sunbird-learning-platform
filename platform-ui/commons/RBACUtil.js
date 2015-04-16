/*
 * Copyright (c) 2013-2014 Canopus Consulting. All rights reserved.
 *
 * This code is intellectual property of Canopus Consulting. The intellectual and technical
 * concepts contained herein may be covered by patents, patents in process, and are protected
 * by trade secret or copyright law. Any unauthorized use of this code without prior approval
 * from Canopus Consulting is prohibited.
 */

/**
 * Util class for orchestrator RBAC
 *
 * @author Mahesh
 */

var async = require('async')
    , mongoose = require('mongoose');

RoleModel = mongoose.model('RoleModel');
var roles = {};

exports.setRoles = function(approles) {
    roles = approles;
}

exports.getRoles = function() {
    return roles;
}

exports.initializeRoles = function(connectroles, callback) {
    async.waterfall([
        function(next){
            RoleModel.find().lean().exec(next);
        },
        function(roles, next){
            var approles = {};
            roles.forEach(function(role) {
                approles[ role.identifier ] = role.actions;
            });
            exports.setRoles(approles);
            next(null, connectroles)
        }
    ], function(err, result) {
        if(err) {
            console.log("Error caching roles: ", err);
        } else {
            exports.authoriseAction(connectroles);
            callback(result);
        }
    });
}

/**
 * This is the filter to authorise every private request.
 * @return null or boolean value.
 */

exports.authoriseAction = function(connectroles, approles) {
    connectroles.use(function(req, action) {
        var result = false;
        if (req.roles && req.roles.length > 0) {
            for (i = 0; i < req.roles.length; i++) {
                var actions = roles[req.roles[i]];
                if (actions && actions.indexOf(action) > -1) {
                    result = true;
                    break;
                }
            }
        }
        if (result) return true;

    });
    console.log("Registered roles with actions to authorize users. The roles and actions are: " + JSON.stringify(roles));
}