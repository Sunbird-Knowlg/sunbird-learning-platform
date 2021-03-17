package org.sunbird.graph.reader;

import java.io.InputStream;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.sunbird.common.exception.ClientException;
import org.sunbird.graph.common.mgr.BaseGraphManager;
import org.sunbird.graph.enums.ImportType;
import org.sunbird.graph.exception.GraphEngineErrorCodes;
import org.sunbird.graph.importer.ImportData;
import org.sunbird.graph.model.node.MetadataDefinition;

/**
 * 
 * @author mahesh
 * 
 */
public class GraphReaderFactory {

    private static ObjectMapper mapper = new ObjectMapper();

    public static ImportData getObject(BaseGraphManager manager, String format, String graphId, InputStream inputStream,
            Map<String, Map<String, MetadataDefinition>> propertyDataMap) throws Exception {
        GraphReader graphReader = null;
        if (ImportType.JSON.name().equals(format.toUpperCase())) {
            graphReader = new JsonGraphReader(manager, mapper, graphId, inputStream);
        } else if (ImportType.CSV.name().equals(format.toUpperCase())) {
            graphReader = new CSVGraphReader(manager, mapper, graphId, inputStream, propertyDataMap);
        } else if (ImportType.RDF.name().equals(format.toUpperCase())) {
            graphReader = new RDFGraphReader(manager, mapper, graphId, inputStream);
        } else {
            throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_IMPORT_INVALID_FORMAT.name(), "Format:" + format + " is invalid.");
        }
        if (graphReader.getValidations().size() > 0) {
            String validations = mapper.writeValueAsString(graphReader.getValidations());
            throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_IMPORT_VALIDATION_FAILED.name(), validations);
        }
        ImportData inputData = new ImportData(graphReader.getDefinitionNodes(), graphReader.getDataNodes(), graphReader.getRelations(),
                graphReader.getTagMembersMap());
        return inputData;
    }
}
