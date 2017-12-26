package org.ekstep.graph.writer;

import java.io.OutputStream;
import java.util.List;

import org.ekstep.common.exception.ClientException;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.dac.model.Relation;
import org.ekstep.graph.enums.ImportType;
import org.ekstep.graph.exception.GraphEngineErrorCodes;

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
