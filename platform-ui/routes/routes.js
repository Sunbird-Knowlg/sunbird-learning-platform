/*
 * Copyright (c) 2013-2014 Canopus Consulting. All rights reserved.
 *
 * This code is intellectual property of Canopus Consulting. The intellectual and technical
 * concepts contained herein may be covered by patents, patents in process, and are protected
 * by trade secret or copyright law. Any unauthorized use of this code without prior approval
 * from Canopus Consulting is prohibited.
 */

/**
 * Defines all Rest Routes. This is a framework component that can be used to
 * configure deployable services at runtime from thei orchestrator. This can
 * also provide authentication, interceptor capabilities.
 *
 * @author Santhosh
 */
var userHelper = require('../view_helpers/UserViewHelper')
    , taxonomyHelper = require('../view_helpers/TaxonomyViewHelper')
    , conceptHelper = require('../view_helpers/ConceptViewHelper')
    , gameHelper = require('../view_helpers/GameViewHelper')
    , commentHelper = require('../view_helpers/CommentViewHelper')
    , uploadHelper = require('../view_helpers/UploadViewHelper');

module.exports = function(app, dirname, passport, connectroles) {

    app.all('/*', setUser);

    app.get('/', function(req, res) {
        res.render('web/homepage.ejs');
    });

    app.get('/home', function(req, res) {
        res.render('web/homepage.ejs');
    });

    app.get('/private/v1/logout', function(req, res) {
        req.logout();
        res.redirect('/home');
    });

    app.post('/private/v1/login/', function(req, res, next) {
        passport.authenticate('local-login', function(err, user, info) {
            if (err) {
                return next(err);
            }
            if (!user) {
                return res.send({
                    status: 'failed',
                    message: 'User not Found'
                });
            }
            req.logIn(user, function(err) {
                console.log(err);
                if (err) {
                    return next(err);
                }
                return res.send({
                    status: 'success',
                    message: ''
                });
            });
        })(req, res, next);
    });

    app.get('/aboutus', connectroles.can('public'), function(req, res) {
        res.render('web/aboutus.ejs');
    });

    app.get('/ilimi-platform', connectroles.can('public'), function(req, res) {
        res.render('web/ilimi-platform.ejs');
    });

    app.get('/howitworks', connectroles.can('public'), function(req, res) {
        res.render('web/howitworks.ejs');
    });

    app.get('/people', connectroles.can('public'), function(req, res) {
        res.render('web/people.ejs');
    });

    app.get('/analytics', connectroles.can('public'), function(req, res) {
        res.render('web/analytics.ejs');
    });

    app.get('/classact', connectroles.can('public'), function(req, res) {
        res.render('web/classact.ejs');
    });

    app.get('/learningobjects', connectroles.can('public'), function(req, res) {
        res.render('web/LearningObjects.ejs');
    });

    app.get('/faq', connectroles.can('public'), function(req, res) {
        res.render('web/FAQ.ejs');
    });

    app.get('/termsofuse', connectroles.can('public'), function(req, res) {
        res.render('web/termsofuse.ejs');
    });

    app.get('/privacypolicy', connectroles.can('public'), function(req, res) {
        res.render('web/privacypolicy.ejs');
    });

    // All below will be private calls
    app.all('/private/*', isLoggedIn);

    app.get('/private/player', function(req, res) {
        res.render('player/player.ejs');
    });

    app.get('/loginError', connectroles.can('public'), function(req, res) {
        res.render('web/loginError.ejs');
    });

    app.get('/user/google/login/request', passport.authenticate('google', {scope: appConfig.USER_LOGIN.GOOGLE_AUTH_SCOPES, accessType: 'offline'}));
    app.get('/user/google/login/response', function(req, res, next) {
        passport.authenticate('google', {failureRedirect: '/home'}, function(err, user, info) {

            if(err && err == 'INVALID_DOMAIN') {
                req.params.error = err;
                return next();
            }
            if (err) {
                return next(err);
            }
            if (!user) {
                return res.json({
                    status: 'failed',
                    message: 'User not Found'
                });
            }
            req.login(user, function(err) {
                if (err) { return next(err); }
                return next();
            });
        })(req, res, next);
    }, function(req, res) {
        if(req.params.error) {
            res.redirect("/loginError");
        } else {
            if (appConfig.CONTEXT_NAME && appConfig.CONTEXT_NAME != '') {
                res.redirect("/" + appConfig.CONTEXT_NAME + "/private/player");
            } else {
                res.redirect("/private/player");
            }
        }
    });

    app.get('/user/facebook/login/request', passport.authenticate('facebook', {scope: appConfig.USER_LOGIN.FACEBOOK_AUTH_SCOPES}));
    app.get('/user/facebook/login/response', function(req, res, next) {
        passport.authenticate('facebook', {failureRedirect: '/home'}, function(err, user, info) {
            if (err) {
                return next(err);
            }
            if (!user) {
                return res.json({
                    status: 'failed',
                    message: 'User not Found'
                });
            }
            req.login(user, function(err) {
                if (err) { return next(err); }
                return next();
            });
        })(req, res, next);
    }, function(req, res) {
        if (appConfig.CONTEXT_NAME && appConfig.CONTEXT_NAME != '') {
            res.redirect("/" + appConfig.CONTEXT_NAME + "/private/player");
        } else {
            res.redirect("/private/player");
        }
    });

    /** Dashboard Links Route */
    app.get('/private/v1/player/dashboard/links', userHelper.getDashboardLinks);

    /** Learning Map Routes */
    app.get('/private/v1/player/taxonomy', taxonomyHelper.getAllTaxonomies);
    app.get('/private/v1/player/taxonomy/:id/definitions', taxonomyHelper.getTaxonomyDefinitions);
    app.get('/private/v1/player/taxonomy/:id/graph', taxonomyHelper.getTaxonomyGraph);
    app.get('/private/v1/player/concept/:id/:tid', conceptHelper.getConcept);
    app.post('/private/v1/player/concept/create', conceptHelper.createConcept);
    app.post('/private/v1/player/concept/update', conceptHelper.updateConcept);

    app.get('/private/v1/player/gameVis/:tid', gameHelper.getGameCoverage);
    app.get('/private/v1/player/gamedefinition/:tid', gameHelper.getGameDefinition);
    app.get('/private/v1/player/games/:tid/:offset/:limit', gameHelper.getGames);
    app.get('/private/v1/player/game/:tid/:gid', gameHelper.getGame);
    app.post('/private/v1/player/game/create', gameHelper.createGame);
    app.post('/private/v1/player/game/update', gameHelper.updateGame);

    app.post('/private/v1/player/comment', commentHelper.saveComment);
    app.get('/private/v1/player/comment/thread/:tid/:id/:threadId',commentHelper.getCommentThread);

    app.post('/private/v1/player/fileupload', uploadHelper.uploadFile);

};

function setUser(req, res, next) {

    if (!req.roles) {
        req.roles = ["public"];
    }
    if (req.user) {
        res.locals.user = req.user;
        req.roles = req.user.roles;
        req.roles.push("public");
    }
    next();
}


function setCacheHeaders(req, res, next) {
    res.setHeader('Cache-Control', 'public, max-age=86400000');
    //res.setHeader("Expires", new Date(Date.now() + 86400000).toUTCString());
    next();
}

function setPublicCacheHeaders(req, res, next) {
    res.setHeader('Cache-Control', 'public, max-age=604800000');
    //res.setHeader("Expires", new Date(Date.now() + 604800000).toUTCString());
    next();
}

function isLoggedIn(req, res, next) {

    // if user is authenticated in the session, carry on
    if (req.isAuthenticated()) {
        return next();
    } else {
        console.log('User not authenticated');
    }

    // if they aren't redirect them to the home page
    res.redirect('/home');
}