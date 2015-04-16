/*
 * Copyright (c) 2013-2014 Canopus Consulting. All rights reserved.
 *
 * This code is intellectual property of Canopus Consulting. The intellectual and technical
 * concepts contained herein may be covered by patents, patents in process, and are protected
 * by trade secret or copyright law. Any unauthorized use of this code without prior approval
 * from Canopus Consulting is prohibited.
 */

/**
 * User Model - Schema to store user documents in mongo
 *
 * @author Santhosh
 */
var mongoose = require('mongoose');
var bcrypt = require('bcrypt-nodejs');

var userSchema = new mongoose.Schema({
    identifier: {
        type: String,
        required: true,
        unique: true
    },
    googleId: String,
    linkedInId: String,
    facebookId: String,
    displayName: String,
    registered: Boolean,
    name: {
        familyName: String,
        givenName: String,
        middleName: String
    },
    metadata: mongoose.Schema.Types.Mixed,
    updatedOn: Date,
    image: String,
    email: String,
    password: String,
    roles: [],
    resetPassword: {
        token: String,
        expire: Date,
    },
    termsAndConditions: {
        accept: Boolean,
        acceptDate: Date,
    },
    is_deleted: {
        type: Boolean,
        default: false
    },
}, {
    collection: 'user'
}, {
    versionKey: false
});
// module.exports = mongoose.model('UserModel', userSchema);

// methods ======================
// generating a hash
userSchema.methods.generateHash = function(password) {
    return bcrypt.hashSync(password, bcrypt.genSaltSync(8), null);
};

// checking if password is valid
userSchema.methods.validPassword = function(password) {
    return bcrypt.compareSync(password, this.local.password);
};

// create the model for users and expose it to our app
module.exports = mongoose.model('UserModel', userSchema);
