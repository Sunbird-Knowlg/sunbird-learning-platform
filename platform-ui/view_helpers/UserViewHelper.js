/*
 * Copyright (c) 2013-2014 Canopus Consulting. All rights reserved.
 *
 * This code is intellectual property of Canopus Consulting. The intellectual and technical
 * concepts contained herein may be covered by patents, patents in process, and are protected
 * by trade secret or copyright law. Any unauthorized use of this code without prior approval
 * from Canopus Consulting is prohibited.
 */

/**
 * View Helper for User registration/single singon using social network ids like facebook/google/twitter etc
 *
 * @author Santhosh
 */
var async = require('async')
	, S = require('string')
	, path = require('path')
	, mongoose = require('mongoose')
	, util = require('../commons/Util');

UserModel = mongoose.model('UserModel');

exports.createUser = function(profile, accessToken, refreshToken, cb) {
	console.log('profile', profile);
	async.waterfall([
		function(next) {
			if(profile.provider == 'facebook') {
				MongoHelper.update('FacebookAccessToken', {facebook_id: profile.id}, {
					facebook_id: profile.id,
					access_token: accessToken,
					refresh_token: refreshToken
				}, {upsert: true}, next);
			} else {
				MongoHelper.update('GoogleAccessToken', {email_id: profile.id}, {
					email_id: profile.id,
					tokens: {
						access_token: accessToken,
						token_type: 'Bearer',
						refresh_token: refreshToken,
						expiry_date: 0
					},
					type: 'userLogin'
				}, {upsert: true}, next);
			}
		},
		function(count, obj, next) {
			getUserObject(profile, accessToken, next);
		},
      	function(user, next) {
			upsertUser(user, next);
      	}
  	], cb);
}

exports.getDashboardLinks = function (req, res) {
	util.sendJSONFileResponse('config/dashboard_links.json', util.responseCB(res));
}

function getUserObject(profile, accessToken, cb) {

	var user = {
		metadata: {
    		gender: profile.gender == 'male' ? 'M':'F'
    	},
    	email: profile.emails[0].value,
    	is_deleted: false,
    	password: 'Il1m1Dot1n',
		role: 'user'
	};
	if(profile.provider == 'facebook') {
		user.facebookId = profile.id;
		user.metadata.facebook = profile.profileUrl;
	} else if(profile.provider == 'google') {
		user.googleId = profile.id;
		user.metadata.googleplus = profile._json ? profile._json.url : '';
	}

	async.waterfall([
		function(next) {
			MongoHelper.findOne('UserModel', {'email': user.email}, next);
		},
		function(userObj, next) {
			if(null == userObj) {
				if(profile.photos && profile.photos.length > 0) {
					user.metadata.image = profile.photos[0].value;
				}
				user.name = {
		    		givenName: profile.name.givenName,
		    		familyName: profile.name.familyName,
		    		middleName: profile.name.middleName
		    	}
		    	user.metadata.email = user.email;
				var identifier = generateIdentifier(user.name.givenName, undefined, user.name.familyName);
				if(identifier == '' || identifier.length == 0) {
					identifier = user.email.match(/^([^@]*)@/)[1];
					user.name.givenName = identifier;
					identifier = S(identifier).strip(' ', '_', '-', '.').s;
				}
			    MongoHelper.find('UserModel', {identifier: { $regex: new RegExp(identifier) }}, {identifier: 1}).toArray(function(err, users) {
			        if(err || users == null || typeof users == 'undefined' || users.length == 0) {
			            user.identifier = identifier;
						next(null, user);
			        } else {
			            var tmpArray = [];
			            users.forEach(function(user) {
			                tmpArray.push(user.identifier);
			            });
			            if(tmpArray.indexOf(identifier) != -1) {
			                while(tmpArray.indexOf(identifier) != -1) {
			                    identifier = identifier + '' + Math.floor((Math.random() * 1000) + 1);
			                }
			            }
			            user.identifier = identifier;
						next(null, user);
			        }
			    });
			} else {
				if((!user.metadata.image || user.metadata.image == null) && profile.photos && profile.photos.length > 0) {
					user.metadata.image = profile.photos[0].value;
				}
				user.identifier = userObj.identifier;
				next(null, user);
			}
		}
  	], cb);
}

function upsertUser(userData, cb) {

	var logger = LoggerUtil.getConsoleLogger();
    async.waterfall([
      	function(next) {
      		UserModel.findOne({email: userData.email}).exec(next);
      	},
      	function(userModel, next){
	        delete userData.metadata.identifier;
	        delete userData.metadata.password;
	        if (userModel == null) {
	            logger.debug('UserViewHelper:upsertUser() - User does not exist. Create - ' + userData.identifier);
	            userModel = new UserModel();
	            userModel.identifier = userData.identifier;
	            userModel.metadata = {};
	        } else {
	            logger.debug('UserViewHelper:upsertUser() - User exists. Update - ' + userModel.identifier);
	            delete userData.identifier;
	            delete userData.inboxEmailId;
	            if(!userModel.metadata) userModel.metadata = {};
	        }
	        for (var k in userData) {
	            if (k != "__v" && k != "_id" && k != 'identifier' && k != 'metadata') {
	                userModel[k] = userData[k];
	            }
	        }

	        for (var k in userData.metadata) {
	            userModel.metadata[k] = userData.metadata[k];
	        }
	        userModel.markModified('metadata');
	        userModel.displayName = userModel.name.givenName;
	        if (userModel.name.middleName && userModel.name.middleName != '') {
	            userModel.displayName += ' ' + userModel.name.middleName;
	        }
	        if (userModel.name.familyName && userModel.name.familyName != '') {
	            userModel.displayName += ' ' + userModel.name.familyName;
	        }

	        if (userModel.password !== undefined) {
	            userModel.password = userModel.generateHash(userModel.password);
	        }
	        userModel.is_deleted = false;
	        userModel.save(next);
      	}
  	], cb);
}

function generateIdentifier(givenName, middleName, familyName) {
    var userName = givenName.toLowerCase();
    if(middleName) {
        userName += middleName.toLowerCase().substr(0,1);
    }
    if(familyName) {
        userName += familyName.toLowerCase().substr(0,1);
    }
    userName = S(userName).strip(' ', '_', '-').s;
    return userName;
}
