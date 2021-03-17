package org.sunbird.sync.tool.shell;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.springframework.shell.Bootstrap;
import org.springframework.shell.core.JLineShellComponent;

//
public class SpringShellTest extends GraphEngineTestSetup{

	private static JLineShellComponent shell;
	
	@BeforeClass
	public static void startUp() throws InterruptedException {
		Bootstrap bootstrap = new Bootstrap();		
		shell = bootstrap.getJLineShellComponent();
	}
	
	@AfterClass
	public static void shutdown() {
		shell.stop();
	}

	public static JLineShellComponent getShell() {
		return shell;
	}

}