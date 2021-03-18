package org.sunbird.platform.content;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

public class JUnitRuntTestSuite {
	public static void main(String args[]){
Result R = JUnitCore.runClasses(RunJunitTests.class);
for(Failure failure : R.getFailures()){
	System.out.println(failure.toString());
}
System.out.println(R.wasSuccessful());
}
}
