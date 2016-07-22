package com.ilimi.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.ekstep.common.util.HealthCheckUtil;


public class LearningHealthCheckUtil extends HealthCheckUtil{

	//check AuditLog-DB(MySQL)
	public static Map<String, Object> checkMySQL(){
		Map<String, Object> check = new HashMap<String, Object>();
		check.put("name", "My SQL");
		
		try{  
			Class.forName("com.mysql.jdbc.Driver");  
			Connection con= DriverManager.getConnection(DatabasePropertiesUtil.getProperty("db.url"),
					DatabasePropertiesUtil.getProperty("db.username"), DatabasePropertiesUtil.getProperty("db.password"));  
			Statement stmt=(Statement) con.createStatement();  
			ResultSet rs=stmt.executeQuery("select 1");  
			  
			if(rs.next())  
				check.put("healthy", true);
			else{
				check.put("healthy", false);
	    		check.put("err", ""); // error code, if any
	            check.put("errmsg", "DB is not available"); // default English error message 

			}

			con.close();  
			  
		}catch(Exception e){
			check.put("healthy", false);
    		check.put("err", "503"); // error code, if any
            check.put("errmsg", e.getMessage()); // default English error message 
		}  

		return check;
	}
}
