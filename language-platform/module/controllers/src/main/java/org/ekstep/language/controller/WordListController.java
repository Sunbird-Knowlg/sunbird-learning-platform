package org.ekstep.language.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ekstep.language.mgr.IWordListManager;
import org.springframework.beans.factory.annotation.Autowired;

import com.ilimi.common.controller.BaseController;
import com.ilimi.graph.engine.mgr.ICollectionManager;
import com.ilimi.taxonomy.mgr.IAuditLogManager;

public class WordListController extends BaseController {
	
	@Autowired
	private IWordListManager wordListManager;
	
	@Autowired
    private IAuditLogManager auditLogManager;
	
	private static Logger LOGGER = LogManager.getLogger(SearchController.class.getName());
	
	
}