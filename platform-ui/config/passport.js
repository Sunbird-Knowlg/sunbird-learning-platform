/*
 * Copyright (c) 2013-2014 Canopus Consulting. All rights reserved.
 *
 * This code is intellectual property of Canopus Consulting. The intellectual and technical
 * concepts contained herein may be covered by patents, patents in process, and are protected
 * by trade secret or copyright law. Any unauthorized use of this code without prior approval
 * from Canopus Consulting is prohibited.
 */

/**
 * Passport.js - Authentication MW. Handles the following 3 types of authentication
 * 1. Login through Google
 * 2. Login through Facebook
 * 3. Local login using Email/Password
 *
 * @author Santhosh
 */
var async = require('async')
    , bcrypt = require('bcrypt-nodejs')
    , userRegHelper = require('../view_helpers/UserViewHelper')
    , LocalStrategy = require('passport-local').Strategy
    , GoogleStrategy = require('passport-google-oauth').OAuth2Strategy
    , FacebookStrategy = require('passport-facebook').Strategy;

// expose this function to our app using module.exports
module.exports = function(passport) {

    // =========================================================================
    // passport session setup ==================================================
    // =========================================================================
    // required for persistent login sessions
    // passport needs ability to serialize and unserialize users out of session

    // used to serialize the user for the session
    passport.serializeUser(function(user, done) {
        done(null, JSON.stringify(user));
    });

    // used to deserialize the user
    passport.deserializeUser(function(id, done) {
        done(null, JSON.parse(id));
    });

    var localResponse = function(req, email, password, done) {
        // callback with email and password from our form
        // find a user whose email is the same as the forms email
        // we are checking to see if the user trying to login already exists
        MongoHelper.findOne('UserModel', {'$or': [{identifier :  email}, {'email': email}]}, function(err, user) {
            // if there are any errors, return the error before anything else
            if (err)
                return done(err);
            // if no user is found, return the message
            if (!user || user.is_deleted)
                return done(null, false, req.flash('loginMessage', 'No user found.')); // req.flash is the way to set flashdata using connect-flash

            // if the user is found but the password is wrong
            if (!bcrypt.compareSync(password, user.password))
                return done(null, false, req.flash('loginMessage', 'Oops! Wrong password.')); // create the loginMessage and save it to session as flashdata
            // all is well, return successful user
            return done(null, user);
        });
    }

    var authResponse = function(accessToken, refreshToken, profile, done) {

        MongoHelper.findOne('UserModel', {googleId: profile.id}, function(err, user) {
            // if there are any errors, return the error before anything else
            if (err || !user || null == user) {
                userRegHelper.createUser(profile, accessToken, refreshToken, function(err, user) {
                    return done(err, user);
                });
            } else {
                // if no user is found, return the message
                if (user.is_deleted)
                    return done(null, false, req.flash('loginMessage', 'User is not active.')); // req.flash is the way to set flashdata using connect-flash
                // all is well, return successful user
                return done(null, user);
            }
        });
    }

    /** Local strategy - Login using email/password */
 	passport.use(
        'local-login',
        new LocalStrategy({
            usernameField : 'email',
            passwordField : 'password',
            passReqToCallback : true
        }, localResponse)
    );

    /** Google strategy - Login using google */
    passport.use(
        'google',
        new GoogleStrategy({
            clientID: appConfig.USER_LOGIN.GOOGLE_CLIENT_ID,
            clientSecret: appConfig.USER_LOGIN.GOOGLE_CLIENT_SECRET,
            callbackURL: appConfig.USER_LOGIN.GOOGLE_REDIRECT_URI,
            hostedDomain: appConfig.USER_LOGIN.HOSTED_DOMAIN
        }, authResponse)
    );

    /** Facebook strategy - Login using facebook */
    passport.use(
        'facebook',
        new FacebookStrategy({
            clientID: appConfig.USER_LOGIN.FACEBOOK_APP_ID,
            clientSecret: appConfig.USER_LOGIN.FACEBOOK_APP_SECRET,
            callbackURL: appConfig.USER_LOGIN.FACEBOOK_REDIRECT_URI,
            enableProof: false
        }, authResponse)
    );
};
