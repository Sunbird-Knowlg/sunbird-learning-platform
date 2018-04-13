package org.ekstep.sync.tool.shell;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.shell.core.CommandResult;


public class SyncCommandTest extends AbstractShellIntegrationTest{


	@BeforeClass
	public static void init() {
		
	}
	
	@Test
	public void testNodeNotFoundException() {
		CommandResult cr = new CommandResult(false);
		//Execute command
		try {
			cr = getShell().executeCommand("syncbyids --ids do_121212");
		}catch (Exception e) {
			Assert.assertEquals("ERR_COMPOSITE_SEARCH_SYNC_OBJECT_NOT_FOUND: Objects not found ", cr.getException());			
		}

	}

}
