/*
 * Copyright (c) 2013-2014 Canopus Consulting. All rights reserved.
 *
 * This code is intellectual property of Canopus Consulting. The intellectual and technical
 * concepts contained herein may be covered by patents, patents in process, and are protected
 * by trade secret or copyright law. Any unauthorized use of this code without prior approval
 * from Canopus Consulting is prohibited.
 */

/**
 * View Helper for Comment UX.
 *
 * @author Mahesh
 */
var async = require('async')
	, service = require('../services/CommentService')
	, util = require('../commons/Util');

exports.saveComment = function(req, res) {
	service.saveComment(req.body, util.responseCB(res));
}

exports.getCommentThread = function(req, res) {
	service.getCommentThread(req.params.tid, req.params.id, req.params.threadId, util.responseCB(res));
}