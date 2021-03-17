package org.sunbird.graph.writer;

import java.io.OutputStream;
import java.util.List;

import org.sunbird.common.exception.ClientException;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.graph.dac.model.Relation;
import org.sunbird.graph.enums.ImportType;
import org.sunbird.graph.exception.GraphEngineErrorCodes;

public class GraphWriterFactory {

    public static OutputStream getData(String format, List<Node> nodes, List<Relation> relations) throws Exception {
        GraphWriter graphWriter = null;
        if (ImportType.JSON.name().equals(format.toUpperCase())) {
            graphWriter = new JsonGraphWriter(nodes, relations);
        } else if (ImportType.CSV.name().equals(format.toUpperCase())) {
            graphWriter = new CSVGraphWriter(nodes, relations);
        } else if (ImportType.RDF.name().equals(format.toUpperCase())) {
            graphWriter = new RDFGraphWriter(nodes, relations);
        } else {
            throw new ClientException(GraphEngineErrorCodes.ERR_GRAPH_EXPORT_INVALID_FORMAT.name(), "Format:" + format + " is invalid.");
        }
        return graphWriter.getData();
    }
}
