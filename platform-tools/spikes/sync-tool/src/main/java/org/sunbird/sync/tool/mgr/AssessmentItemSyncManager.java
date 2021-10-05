package org.sunbird.sync.tool.mgr;


import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.assessment.store.AssessmentStore;
import org.sunbird.common.dto.Response;
import org.sunbird.common.exception.ClientException;
import org.sunbird.common.exception.ResourceNotFoundException;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.graph.model.node.DefinitionDTO;
import org.sunbird.graph.model.node.MetadataDefinition;
import org.sunbird.learning.util.ControllerUtil;
import org.springframework.stereotype.Component;

@Component("assessmentItemSyncManager")
public class AssessmentItemSyncManager {
	
	private AssessmentStore assessmentStore = new AssessmentStore();
	private ControllerUtil util = new ControllerUtil();
	private static int batchSize = 500;
	
	public void syncAssessmentExternalProperties(String graphId, String objectType, List<String> ids, Integer delay) throws InterruptedException {
		if (StringUtils.isBlank(graphId))
			throw new ClientException("BLANK_GRAPH_ID", "Graph Id is blank.");
		if(!StringUtils.equals(objectType, "AssessmentItem")) {
			throw new ClientException("INVALID_OBJECT_TYPE", "ObjectType passed should be AssessmentItem.");
		}
		
		DefinitionDTO def;
		if (StringUtils.isNotBlank(objectType)) {
			def = util.getDefinition(graphId, objectType);
			if (null != def) {
				System.out.println("\nSyncing external data of " + objectType + " from neo4j to cassandta.\n");
				
				int start = 0;
				boolean found = true;
				
				
					List<Node> nodes = null;
					if(CollectionUtils.isNotEmpty(ids)) {
						try {
							Response response = util.getDataNodes(graphId, ids);
							nodes = (List<Node>) response.get("node_list");
							if (CollectionUtils.isNotEmpty(nodes)) {
								pushToCassandra(nodes, def);
							}
						}catch(ResourceNotFoundException e) {
							System.out.println("error while fetching neo4j records for ids: " + ids);
						}
					}else {
						while (found) {
							try {
									nodes = util.getNodes(graphId, def.getObjectType(), start, batchSize);
									if (CollectionUtils.isNotEmpty(nodes)) {
										pushToCassandra(nodes, def);
										if (delay > 0) {
											Thread.sleep(delay);
										}
										start += batchSize;
									} else {
										found = false;
										break;
									}
							
							}catch(ResourceNotFoundException e) {
								System.out.println("error while fetching neo4j records for objectType="+objectType+", start="+start+",batchSize="+batchSize);
								start += batchSize;
								continue;
							}
						}
					}
			}
		}
	}
	
	private void pushToCassandra(List<Node> nodes, DefinitionDTO def) {
		List<String> externalProperties = getExternalPropsList(def);
		System.out.println(def.getObjectType() + " *********** externalProperties ******** " + externalProperties);
		List<String> migratedQuestions = new ArrayList<>();
		List<String> notMigratedQuestions = new ArrayList<>();
		for(Node node: nodes) {
			if(StringUtils.equals(node.getObjectType(), def.getObjectType())) {
				Map<String, Object> metaData = node.getMetadata();
				Map<String, Object> extProp = new HashMap<>();
				for(String ext : externalProperties) {
					if(metaData.keySet().contains(ext))
						extProp.put(ext, metaData.get(ext));
				}
				//System.out.println("Id: " + node.getIdentifier() + " *** extProp: " + extProp);
				if(MapUtils.isNotEmpty(extProp)) {
					assessmentStore.updateAssessmentProperties(node.getIdentifier(), extProp);
					migratedQuestions.add(node.getIdentifier());
				}else {
					notMigratedQuestions.add(node.getIdentifier());
				}
			}
		}
		System.out.println("***** Questions migrated to cassandra *****: " + migratedQuestions);
		System.out.println("***** Questions not migrated to cassandra *****: " + notMigratedQuestions);
	}
	
	protected List<String> getExternalPropsList(DefinitionDTO definition) {
		List<String> list = new ArrayList<>();
		if (null != definition) {
			List<MetadataDefinition> props = definition.getProperties();
			if (null != props && !props.isEmpty()) {
				for (MetadataDefinition prop : props) {
					if (equalsIgnoreCase("external", prop.getDataType())) {
						list.add(prop.getPropertyName().trim());
					}
				}
			}
		}
		return list;
	}

}
