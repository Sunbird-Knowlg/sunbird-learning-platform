package org.ekstep.sync.tool.mgr;


import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.ekstep.assessment.store.AssessmentStore;
import org.ekstep.common.exception.ClientException;
import org.ekstep.common.exception.ResourceNotFoundException;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.model.node.DefinitionDTO;
import org.ekstep.graph.model.node.MetadataDefinition;
import org.ekstep.learning.util.ControllerUtil;
import org.springframework.stereotype.Component;

@Component("assessmentItemSyncManager")
public class AssessmentItemSyncManager {
	
	private AssessmentStore assessmentStore = new AssessmentStore();
	private ControllerUtil util = new ControllerUtil();
	private static int batchSize = 50;
	
	public void syncAssessmentExternalProperties(String graphId, String objectType, Integer delay) throws InterruptedException {
		if (StringUtils.isBlank(graphId))
			throw new ClientException("BLANK_GRAPH_ID", "Graph Id is blank.");
		
		DefinitionDTO def;
		if (StringUtils.isNotBlank(objectType)) {
			def = util.getDefinition(graphId, objectType);
			if (null != def) {
				System.out.println("\nSyncing external data of " + objectType + " from neo4j to cassandta.\n");
				
				List<String> externalProperties = getExternalPropsList(def);
				int start = 0;
				boolean found = true;
				
				while (found) {
					List<Node> nodes = null;
					try {
						nodes = util.getNodes(graphId, def.getObjectType(), start, batchSize);
					}catch(ResourceNotFoundException e) {
						System.out.println("error while fetching neo4j records for objectType="+objectType+", start="+start+",batchSize="+batchSize);
						start += batchSize;
						continue;
					}
					if (CollectionUtils.isNotEmpty(nodes)) {
						start += batchSize;
						for(Node node: nodes) {
							Map<String, Object> metaData = node.getMetadata();
							Map<String, Object> extProp = new HashMap<>();
							for(String ext : externalProperties) {
								if(metaData.keySet().contains(ext))
									extProp.put(ext, metaData.get(ext));
							}
							// TODO: Add code for saving ext prop to cassandra
							System.out.println("Id: " + node.getIdentifier() + " *** extProp: " + extProp);
							assessmentStore.updateAssessmentProperties(node.getIdentifier(), extProp);
						}
						if (delay > 0) {
							Thread.sleep(delay);
						}
					} else {
						found = false;
						break;
					}
				}
			}
		}
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
