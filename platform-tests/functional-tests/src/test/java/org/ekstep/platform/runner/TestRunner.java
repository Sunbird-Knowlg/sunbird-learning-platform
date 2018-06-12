/**
 * 
 */
package org.ekstep.platform.runner;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

/**
 * @author gauraw
 *
 */
public class TestRunner {

	public static void main(String[] args) {
		
		Result result=null;
		
		switch(args[0]){
		
		case "Content":
			result = JUnitCore.runClasses(ContentRunner.class);
			break;
		case "DialCode":
			result = JUnitCore.runClasses(DialCodeRunner.class);
			break;
		
		}
		
		if(null!=result){
			System.out.println("--------------------------------Test Result--------------------------------------");
			System.out.println("Run: "+result.getRunCount()+" , Skipped: "+result.getIgnoreCount()+" , Failure: "+result.getFailureCount());
			System.out.println("--------------------------------Test Result--------------------------------------");
		}
		
		if(null!=result && !result.wasSuccessful()){
			System.out.println("Failure : ");
			System.out.println("------------------------------------------------------------------------");
			for(Failure failure : result.getFailures()){
				System.out.println(failure.toString());
				System.out.println("--------------------------------------------------------------------");
				System.out.println(failure.getTrace());
				
			}
		}
	}
	
}
