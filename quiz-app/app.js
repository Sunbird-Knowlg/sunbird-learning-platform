/*
 * Copyright (c) 2013-2014 Canopus Consulting. All rights reserved.
 *
 * This code is intellectual property of Canopus Consulting. The intellectual and technical
 * concepts contained herein may be covered by patents, patents in process, and are protected
 * by trade secret or copyright law. Any unauthorized use of this code without prior approval
 * from Canopus Consulting is prohibited.
 */

/**
 * Bootstrap for the Orchestrator
 *
 * @author Santhosh
 */
var express = require('express')
    , http = require('http')
    , path = require('path')
    , fs = require('fs');

http.globalAgent.maxSockets = 100000;

    var app = express();

    // all environments
    app.set('port', 3000);
    app.set('views', __dirname + '/views');
    app.set('view engine', 'ejs');
    app.use(express.favicon());
    app.use(express.logger('dev'));
    app.use(express.compress());
    app.use(express.json());
    app.use(express.urlencoded());
    app.use(express.bodyParser());
    app.use(express.methodOverride());
    app.use(express.cookieParser());

    app.use(express.session({
        secret: '1234'
    }));

    app.use(app.router);
    app.locals({
        contextPath: ""
    });

    app.use(express.compress());
    app.use(express.static(path.join(__dirname, 'www')));
    app.use(express.static(path.join(__dirname, 'views')));

    // development only
    if ('development' == app.get('env')) {
        app.use(express.errorHandler());
    }

    //Bootstrap routes
    var routes = __dirname + '/routes', route_files = fs.readdirSync(routes);
    route_files.forEach(function (file) {
        require(routes + '/' + file)(app, __dirname);
    });

    // Workers can share any TCP connection
    // In this case its a HTTP server
    var server = http.createServer(app).listen(app.get('port'), 1500);
    server.timeout = 0;