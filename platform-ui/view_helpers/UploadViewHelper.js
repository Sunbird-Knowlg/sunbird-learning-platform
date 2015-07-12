/*
 * Copyright (c) 2013-2014 Canopus Consulting. All rights reserved.
 *
 * This code is intellectual property of Canopus Consulting. The intellectual and technical
 * concepts contained herein may be covered by patents, patents in process, and are protected
 * by trade secret or copyright law. Any unauthorized use of this code without prior approval
 * from Canopus Consulting is prohibited.
 */

/**
 * View Helper for File Upload.
 *
 * @author rayulu
 */
var async = require('async')
	, fs = require('fs')
    , service = require('../services/AWSService')
	, util = require('../commons/Util');

exports.uploadFile = function(req, res) {
	var file = req.files.document;
    var folder = req.body.folderName;
    if (!folder || folder == '') {
        folder == 'games';
    }
    var name = file.name;
    if (!name || name == null) {
        name = '';
    }
    name = name.replace(/ /g, '_');
    var t = new Date();
    name = t.getTime() + '_' + name;
    service.uploadFile(appConfig.AWS_PUBLIC_BUCKET_NAME, folder, name, file, util.responseCB(res));
}