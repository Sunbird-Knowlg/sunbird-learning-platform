package com.ilimi.graph.reader;

import java.io.InputStream;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;

import com.ilimi.graph.common.exception.ClientException;
import com.ilimi.graph.common.mgr.BaseGraphManager;
import com.ilimi.graph.enums.ImportType;
import com.ilimi.graph.exception.GraphEngineErrorCodes;
import com.ilimi.graph.importer.ImportData;
import com.ilimi.graph.model.node.MetadataDefinition;

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
