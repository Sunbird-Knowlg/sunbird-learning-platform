package com.ilimi.orchestrator.dac.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.ekstep.contentstore.util.CassandraConnector;
import org.ekstep.contentstore.util.ContentStoreParams;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.ilimi.common.Platform;
import com.ilimi.common.exception.ServerException;
import com.ilimi.common.logger.PlatformLogger;
import com.ilimi.orchestrator.dac.model.OrchestratorScript;
import com.ilimi.orchestrator.dac.model.ScriptTypes;
import com.ilimi.orchestrator.dac.service.IOrchestratorDataService;

public class OrchestratorDataServiceImpl implements IOrchestratorDataService {

	private ObjectMapper mapper = new ObjectMapper();
	
	private static String keyspaceName = "";
	private static String keyspaceTable = "";
	
	public static String getKeyspaceName() {
		if (StringUtils.isBlank(keyspaceName) && Platform.config.hasPath("orchestrator.keyspace.name")) 
			return Platform.config.getString("orchestrator.keyspace.name");
		else 
			return keyspaceName;
	}
	
	public static String getKeyspaceTable() {
		if (StringUtils.isBlank(keyspaceTable) && Platform.config.hasPath("orchestrator.keyspace.table")) 
			return Platform.config.getString("orchestrator.keyspace.table");
		else 
			return keyspaceTable;
	}

	public OrchestratorDataServiceImpl() {}

	@Override
	public OrchestratorScript getScript(String name) {
		if (StringUtils.isNotBlank(name)) {
			
			Session session = CassandraConnector.getSession();
			OrchestratorScript orchestratorScript = null;
			List<String> properties = new ArrayList<>();
			properties.add("reqmap");
			Map<String, String> conditions = new HashMap<>();
			conditions.put("name", name);
			String query = getSelectQueryByConditions(properties, conditions);
			
			try {
				ResultSet rs = session.execute(query);
				if (null != rs) {
					Row row = rs.one();
					if(null != row)
						orchestratorScript = mapper.readValue(row.getString("reqmap"), OrchestratorScript.class);
				}
			} catch (Exception e) {
				PlatformLogger.log("Error! Executing Get Script.", e.getMessage(), e);
				throw new ServerException(ContentStoreParams.ERR_SERVER_ERROR.name(), "Error fetching script from Orchestrator Store.");
			}
			return orchestratorScript;
		}
		return null;
	}
	
	

	@Override
	public String createScript(OrchestratorScript script) {
		if (null != script) {
			Session session = CassandraConnector.getSession();
			script.setType(ScriptTypes.SCRIPT.name());
			
			Map<String, String> scriptMap = new HashMap<>();
			try {
				scriptMap.put("name", script.getName());
				scriptMap.put("type", script.getType());
				scriptMap.put("reqmap", mapper.writeValueAsString(script));
				
				String query = getInsertQuery(scriptMap);
				session.execute(query);
				
				return script.getName();
			} catch (Exception e) {
				PlatformLogger.log("Error! Executing create Script.", e.getMessage(), e);
				throw new ServerException(ContentStoreParams.ERR_SERVER_ERROR.name(), "Error creating script in Orchestrator Store.");
			}
		}
		return null;
	}
	
	@Override
	public String createCommand(OrchestratorScript command) {
		if (null != command) {
			Session session = CassandraConnector.getSession();
			command.setType(ScriptTypes.COMMAND.name());
			
			Map<String, String> scriptMap = new HashMap<>();
			try {
				scriptMap.put("name", command.getName());
				scriptMap.put("type", command.getType());
				scriptMap.put("reqmap", mapper.writeValueAsString(command));
				
				String query = getInsertQuery(scriptMap);
				session.execute(query);
				
				return command.getName();
			} catch (Exception e) {
				PlatformLogger.log("Error! Executing create Command.", e.getMessage(), e);
				throw new ServerException(ContentStoreParams.ERR_SERVER_ERROR.name(), "Error creating command in Orchestrator Store.");
			}
		}
		return null;
	}

	@Override
	public void updateScript(OrchestratorScript script) {
		if (null != script) {
			
			Session session = CassandraConnector.getSession();
			Map<String, String> scriptMap = new HashMap<>();
			try {
				scriptMap.put("name", script.getName());
				scriptMap.put("type", script.getType());
				scriptMap.put("reqmap", mapper.writeValueAsString(script));
				
				String query = getInsertQuery(scriptMap);
				session.execute(query);
			} catch (Exception e) {
				PlatformLogger.log("Error! Executing update Script.", e.getMessage(), e);
				throw new ServerException(ContentStoreParams.ERR_SERVER_ERROR.name(), "Error updating script in Orchestrator Store.");
			}
		}
	}

	@Override
	public List<OrchestratorScript> getAllScripts() {
		
		Session session = CassandraConnector.getSession();
		
		List<String> properties = new ArrayList<>();
		properties.add("reqmap");
		Map<String, String> conditions = new HashMap<>();
		conditions.put("type", ScriptTypes.SCRIPT.name());
		
		String query = getSelectQueryByConditions(properties, conditions);
		List<OrchestratorScript> list = null;
		
		try {
			ResultSet rs = session.execute(query);
			if (null != rs) {
				while (rs.iterator().hasNext()) {
					list = new ArrayList<>();
					Row row = rs.iterator().next();
					OrchestratorScript orchestratorScript = mapper.readValue(row.getString("reqmap"), OrchestratorScript.class);
					list.add(orchestratorScript);
				}
			}
		} catch (Exception e) {
			PlatformLogger.log("Error! Executing Get All Scripts.", e.getMessage(), e);
			throw new ServerException(ContentStoreParams.ERR_SERVER_ERROR.name(), "Error fetching all Scripts from Orchestrator Store.");
		}
		return list;
	}

	@Override
	public List<OrchestratorScript> getAllCommands() {
		
		Session session = CassandraConnector.getSession();
		
		List<String> properties = new ArrayList<>();
		properties.add("reqmap");
		Map<String, String> conditions = new HashMap<>();
		conditions.put("type", ScriptTypes.COMMAND.name());
		
		String query = getSelectQueryByConditions(properties, conditions);
		List<OrchestratorScript> list = null;
		
		try {
			ResultSet rs = session.execute(query);
			if (null != rs) {
				while (rs.iterator().hasNext()) {
					list = new ArrayList<>();
					Row row = rs.iterator().next();
					OrchestratorScript orchestratorScript = mapper.readValue(row.getString("reqmap"), OrchestratorScript.class);
					list.add(orchestratorScript);
				}
			}
		} catch (Exception e) {
			PlatformLogger.log("Error! Executing Get All Commands.", e.getMessage(), e);
			throw new ServerException(ContentStoreParams.ERR_SERVER_ERROR.name(), "Error fetching all Commands from Orchestrator Store.");
		}
		return list;
	}

	@Override
	public List<OrchestratorScript> getScriptsByRequestPath(String url, String type) {
		
		Session session = CassandraConnector.getSession();
		List<String> properties = new ArrayList<>();
		properties.add("reqmap");
		Map<String, String> conditions = null;
		
		String query = getSelectQueryByConditions(properties, conditions);
		List<OrchestratorScript> list = null;
		
		try {
			ResultSet rs = session.execute(query);
			if (null != rs) {
				while (rs.iterator().hasNext()) {
					list = new ArrayList<>();
					Row row = rs.iterator().next();
					OrchestratorScript orchestratorScript = mapper.readValue(row.getString("reqmap"), OrchestratorScript.class);
					if(orchestratorScript.getRequestPath().getUrl().equals(url) && orchestratorScript.getRequestPath().getType().equalsIgnoreCase(type))
						list.add(orchestratorScript);
				}
			}
		} catch (Exception e) {
			PlatformLogger.log("Error! Executing Get Scripts By RequestPath.", e.getMessage(), e);
			throw new ServerException(ContentStoreParams.ERR_SERVER_ERROR.name(), "Error fetching Scripts By RequestPath Orchestrator Store.");
		}
		return list;
	}
	
	@Override
	public boolean doConnectionEstablish() {
		Session session = null;
		try {
			session = CassandraConnector.getSession();
			if(null != session)
				return true;
			else
				return false;
		}catch(Exception e) {
			PlatformLogger.log("Error! Executing do Establish Connection.", e.getMessage(), e);
			throw new ServerException(ContentStoreParams.ERR_SERVER_ERROR.name(), "Error in establishing connection with Orchestrator Store.");
		}
			
	}

	public void remove(String type, String name) {
		Session session = CassandraConnector.getSession();
		
		Map<String, String> primaryKeyConditions = new HashMap<>();
		primaryKeyConditions.put("name", name);
		Map<String, String> nonPrimaryKeyConditions = new HashMap<>();
		nonPrimaryKeyConditions.put("type", type);
		
		String query = getDeleteQueryByConditions(primaryKeyConditions, nonPrimaryKeyConditions);
		
		try {
			session.execute(query);
		}catch(Exception e) {
			PlatformLogger.log("Error! Executing Remove script.", e.getMessage(), e);
			throw new ServerException(ContentStoreParams.ERR_SERVER_ERROR.name(), "Error in removing script from Orchestrator Store.");
		}
	}
	
	private String getDeleteQueryByConditions(Map<String, String> primaryKeyConditions, Map<String, String> nonPrimaryKeyConditions) {
		StringBuilder sb = new StringBuilder();
		sb.append("DELETE FROM ").append(getKeyspaceName()).append(".").append(getKeyspaceTable());
		if(null != primaryKeyConditions && !primaryKeyConditions.isEmpty()) {
			StringBuilder primaryKeyConditionFields = new StringBuilder();
			primaryKeyConditionFields.append(" WHERE ");
			for(String field : primaryKeyConditions.keySet()) {
				if (null != field && StringUtils.isBlank(field))
					throw new ServerException(ContentStoreParams.ERR_SERVER_ERROR.name(), "Invalid property name. Please specify a valid property name");
				
				primaryKeyConditionFields.append(field).append(" = '").append(primaryKeyConditions.get(field)).append("' and ");
			}
			sb.append(StringUtils.removeEnd(primaryKeyConditionFields.toString(), " and "));
			
			StringBuilder nonPrimaryKeyConfitionFields = new StringBuilder();
			nonPrimaryKeyConfitionFields.append(" IF ");
			for(String field : nonPrimaryKeyConditions.keySet()) {
				if (null != field && StringUtils.isBlank(field))
					throw new ServerException(ContentStoreParams.ERR_SERVER_ERROR.name(), "Invalid property name. Please specify a valid property name");
				
				nonPrimaryKeyConfitionFields.append(field).append(" = '").append(nonPrimaryKeyConditions.get(field)).append("' and ");
			}
			sb.append(StringUtils.removeEnd(nonPrimaryKeyConfitionFields.toString(), " and "));
		}
		return sb.toString();
	}
	
	private String getSelectQueryByConditions(List<String> properties, Map<String, String> conditions) {

		StringBuilder sb = new StringBuilder();
		if (null != properties && !properties.isEmpty()) {
			sb.append("select ");
			StringBuilder selectFields = new StringBuilder();
			for (String property : properties) {
				if (null != property && StringUtils.isBlank(property))
					throw new ServerException(ContentStoreParams.ERR_SERVER_ERROR.name(), "Invalid property name. Please specify a valid property name");
				
				selectFields.append(property).append(", ");
			}
			sb.append(StringUtils.removeEnd(selectFields.toString(), ", "));
			sb.append(" from ").append(getKeyspaceName()).append(".").append(getKeyspaceTable());
			
			if(null != conditions && !conditions.isEmpty()) {
				StringBuilder conditionFields = new StringBuilder();
				conditionFields.append(" where ");
				for(String field : conditions.keySet()) {
					if (null != field && StringUtils.isBlank(field))
						throw new ServerException(ContentStoreParams.ERR_SERVER_ERROR.name(), "Invalid property name. Please specify a valid property name");
					
					conditionFields.append(field).append(" = '").append(conditions.get(field)).append("' and ");
				}
				sb.append(StringUtils.removeEnd(conditionFields.toString(), " and ")).append(" ALLOW FILTERING");
			}
		}
		return sb.toString();
	}
	
	private static String getInsertQuery(Map<String, String> scriptMap) {
		StringBuilder sb = new StringBuilder();
		Set<String> properties = scriptMap.keySet();
		if (null != properties && !properties.isEmpty()) {
			sb.append("INSERT INTO ").append(getKeyspaceName()).append(".").append(getKeyspaceTable()).append(" ");
			StringBuilder insertFields = new StringBuilder();
			StringBuilder insertValues = new StringBuilder();
			insertFields.append("(");
			insertValues.append("(");
			for (String property : properties) {
				if (null != property && StringUtils.isBlank(property))
					throw new ServerException(ContentStoreParams.ERR_SERVER_ERROR.name(), "Invalid property name. Please specify a valid property name");
				
				if(null != (String)scriptMap.get(property)) {
					insertFields.append(property.trim()).append(", ");
					insertValues.append("'").append(scriptMap.get(property)).append("', ");
				}
			}
			sb.append(StringUtils.removeEnd(insertFields.toString(), ", ")).append(")")
				.append(" VALUES ")
				.append(StringUtils.removeEnd(insertValues.toString(), ", ")).append(")");
		}
		return sb.toString();
	}
	
	
	public static void main(String[] args) {
		OrchestratorScript os = null;
		String req = "{\n" + 
				"  \"name\" : \"getSetMembers4\",\n" + 
				"  \"type\" : \"SCRIPT\",\n" + 
				"  \"body\": \"package require java\\nset response [getCollectionMembers $graph_id $set_id \\\"SET\\\"]\\nreturn $response\",   \n" + 
				"  \"parameters\": [\n" + 
				"      {\n" + 
				"          \"name\": \"graph_id\",\n" + 
				"          \"index\": 0,\n" + 
				"          \"routingParam\": true\n" + 
				"        },\n" + 
				"        {\n" + 
				"          \"name\": \"set_id\",\n" + 
				"          \"index\": 1,\n" + 
				"          \"routingParam\": false\n" + 
				"        }\n" + 
				"    ],\n" + 
				"    \"requestPath\": {\n" + 
				"        \"type\" : \"GET\",\n" + 
				"    \"url\" : \"/v1/graph/*/getSetMembers2/*\",\n" + 
				"    \"pathParams\": [\"graph_id\", \"set_id\"]\n" + 
				"    }\n" + 
				"}";
		
		OrchestratorDataServiceImpl orchestratorDataServiceImpl = new OrchestratorDataServiceImpl();
		
		try {
			os = orchestratorDataServiceImpl.mapper.readValue(req, OrchestratorScript.class);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//System.out.println(orchestratorDataServiceImpl.doConnectionEstablish());
		//orchestratorDataServiceImpl.updateScript(os);
		//List<OrchestratorScript> list = orchestratorDataServiceImpl.getScriptsByRequestPath("/v1/graph/*/getSetMembers1/*", "GET");
		//List<OrchestratorScript> list = orchestratorDataServiceImpl.getAllCommands();
		//for(OrchestratorScript o : list) {
		//	System.out.println(o.getRequestPath().getUrl());
		//}
		List<OrchestratorScript> o = orchestratorDataServiceImpl.getAllScripts();//getScript("getSetMembers1");
		//System.out.println(o.getName() + "***" + o.getId()==null?"":o.getId());
		
		//orchestratorDataServiceImpl.remove("SCRIPT", "getSetMembers1");
		
		/*Map<String, String> primaryKeyConditions = new HashMap<>();
		primaryKeyConditions.put("name", name);
		
		Map<String, String> nonPrimaryKeyConditions = new HashMap<>();
		nonPrimaryKeyConditions.put("type", type);
		String s = orchestratorDataServiceImpl.getDeleteQueryByConditions(primaryKeyConditions, nonPrimaryKeyConditions);
		System.out.println(s);*/
		
		
	}
}
