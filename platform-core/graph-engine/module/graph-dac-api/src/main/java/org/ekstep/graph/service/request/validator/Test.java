package org.ekstep.graph.service.request.validator;

import com.ilimi.graph.common.DateUtils;

public class Test {
public static void main(String[] args){
	String lastUpdatedOn = "2016-02-22T05:26:01.116+0000";
	String s = String.valueOf(DateUtils.parse(lastUpdatedOn).getTime());
	System.out.println(s);
}
}
