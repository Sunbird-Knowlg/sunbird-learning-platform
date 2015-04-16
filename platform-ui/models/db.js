/*
 * Copyright (c) 2013-2014 Canopus Consulting. All rights reserved.
 *
 * This code is intellectual property of Canopus Consulting. The intellectual and technical
 * concepts contained herein may be covered by patents, patents in process, and are protected
 * by trade secret or copyright law. Any unauthorized use of this code without prior approval
 * from Canopus Consulting is prohibited.
 */

/**
 * Initializes Mongo DB Connection
 *
 * @author Santhosh
 */

var mongoose = require('mongoose');

mongoose.connect(appConfig.MONGO_DB_URI, { db: { native_parser: true }});
//CONNECTION EVENTS
//When successfully connected
mongoose.connection.on('connected', function() {
    console.log('Mongoose default connection open to ' + appConfig.MONGO_DB_URI);
});

//If the connection throws an error
mongoose.connection.on('error', function(err) {
    console.log('Mongoose default connection error: ' + err);
});

//When the connection is disconnected
mongoose.connection.on('disconnected', function() {
    console.log('Mongoose default connection disconnected');
});

//If the Node process ends, close the Mongoose connection
process.on('SIGINT', function() {
    mongoose.connection.close(function() {
        console.log('Mongoose default connection disconnected through app termination');
        process.exit(0);
    });
});