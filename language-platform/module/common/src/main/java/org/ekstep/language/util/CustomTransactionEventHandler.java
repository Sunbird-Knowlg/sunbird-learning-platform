package org.ekstep.language.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.event.PropertyEntry;
import org.neo4j.graphdb.event.TransactionData;
import org.neo4j.graphdb.event.TransactionEventHandler;

import com.ilimi.common.exception.ServerException;
import com.ilimi.graph.dac.enums.SystemProperties;
import com.ilimi.graph.dac.exception.GraphDACErrorCodes;

public class CustomTransactionEventHandler implements TransactionEventHandler<Void>{

	@Override
	public Void beforeCommit(TransactionData data) throws Exception {
		Iterable<Node> nodeIterable = data.createdNodes();
		Iterator<Node> nodeIterator = nodeIterable.iterator();
		while (nodeIterator.hasNext()) {
			final Node node = nodeIterator.next();
			if (null == node)
	            throw new ServerException(GraphDACErrorCodes.ERR_GRAPH_NULL_DB_NODE.name(),
	                    "Failed to create node object. Node from database is null.");
			try{
				System.out.println(node.getId());
	        Iterable<String> keys = node.getPropertyKeys();
	        if (null != keys && null != keys.iterator()) {
	            Map<String, Object> metadata = new HashMap<String, Object>();
	            for (String key : keys) {
	            	String identifier;
					if (StringUtils.equalsIgnoreCase(key, SystemProperties.IL_UNIQUE_ID.name())){
						identifier = node.getProperty(key).toString();
						System.out.println(identifier);
					}
	                else if (StringUtils.equalsIgnoreCase(key, SystemProperties.IL_SYS_NODE_TYPE.name())){
	                	identifier = node.getProperty(key).toString();
	                	System.out.println(identifier);
	                }
	                else if (StringUtils.equalsIgnoreCase(key, SystemProperties.IL_FUNC_OBJECT_TYPE.name())){
	                	identifier = node.getProperty(key).toString();
	                	System.out.println(identifier);
	                }
	                else{
	                    metadata.put(key, node.getProperty(key));
	                    System.out.println(node.getProperty(key));
	                }
	            }
	            System.out.println("true");
	        }
			}
			catch(Exception e){
				e.printStackTrace();
			}
			System.out.println("True");
		}
		Iterable<PropertyEntry<Node>> nodeProperties = data.assignedNodeProperties();
		Iterator<PropertyEntry<Node>> nodePropertiesIterator = nodeProperties.iterator();
		while (nodePropertiesIterator.hasNext()) {
			PropertyEntry<Node> propertyNode = nodePropertiesIterator.next();
			Object propertyEntry = propertyNode.value();
		}
		System.out.println("Committed transaction");
		return null;
	}

	@SuppressWarnings("unused")
	@Override
	public void afterCommit(final TransactionData data, Void state) {
		
	}

	@Override
	public void afterRollback(TransactionData data, Void state) {
		System.out.println("Transaction rolled back");
	}

	public void convertToIlimiNode(String graphId, org.neo4j.graphdb.Node neo4jNode) {
        
    }
	
}
