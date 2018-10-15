package org.ekstep.jobs.samza.service.util;

import org.ekstep.graph.model.node.DefinitionDTO;
import org.ekstep.learning.util.ControllerUtil;

import java.util.HashMap;
import java.util.Map;

public class TaskUtils {

    private static ControllerUtil util = new ControllerUtil();
    private static Map<String, DefinitionDTO> definitions = new HashMap<>();

    public static void getAllDefinitions() {
        util.getAllDefinitions("domain").stream().
                forEach(definitionDTO ->
                        definitions.put(definitionDTO.getObjectType(), definitionDTO));
    }

    public static DefinitionDTO getDefinition(String objectType) { return definitions.get(objectType); }
}
