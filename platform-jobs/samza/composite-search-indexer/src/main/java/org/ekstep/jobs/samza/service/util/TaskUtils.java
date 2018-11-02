package org.ekstep.jobs.samza.service.util;

import org.ekstep.common.exception.ServerException;
import org.ekstep.graph.model.node.DefinitionDTO;
import org.ekstep.learning.util.ControllerUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TaskUtils {

    private static ControllerUtil util = new ControllerUtil();
    private static Map<String, Map<String, DefinitionDTO>> graphDefinitions= new HashMap<>();


    public static void getAllDefinitions(List<String> graphIds) {
        graphIds.forEach(graphId -> {
            Map<String, DefinitionDTO> definitions = new HashMap<>();
            util.getAllDefinitions(graphId).stream().
                    forEach(definitionDTO ->
                            definitions.put(definitionDTO.getObjectType(), definitionDTO));
            graphDefinitions.put(graphId, definitions);
        });
    }

    public static DefinitionDTO getDefinition(String graphId, String objectType) {
        return Optional.ofNullable(graphDefinitions.get(graphId)).map(definitions -> {
            return Optional.ofNullable(definitions.get(objectType)).
                    orElseThrow(() ->
                            new ServerException("ERROR_GETTING_DEFINITION_FOR_GRAPH", "Definition Not Available for " + objectType + "in " + graphId));
        }).orElseThrow(() -> new ServerException("ERROR_GETTING_DEFINITION_FOR_GRAPH", graphId + " Not Available "));
    }
}
