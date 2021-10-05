package org.sunbird.graph.writer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.graph.dac.enums.SystemNodeTypes;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.graph.dac.model.Relation;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;

public class RDFGraphWriter implements GraphWriter {

	private List<Node> nodes;
	private List<Relation> relations;

	public static final String PROPERTY_ID = "id";
	public static final String PROPERTY_NAME = "name";
	public static final String PROPERTY_TYPE = "type";
	public static final String PROPERTY_NS = "namespace";
	public static final String PROPERTY_RELATION_START = "relationStart";
	public static final String PROPERTY_RELATION_END = "relationEnd";
	private static final String PROPERTY_RELATION_TYPE = "relationType";

	public static final String PROPERTY_NODE_TYPE = "nodeType";
	public static final String PROPERTY_OBJECT_TYPE = "objectType";

	// public static final List<String> definitionMetadataKeys =
	// Arrays.asList("INDEXABLE_METADATA_KEY", "NON_INDEXABLE_METADATA_KEY",
	// "IN_RELATIONS_KEY", "OUT_RELATIONS_KEY");

	public static final String MIME_RDFXML = "application/rdf+xml";

	protected String defaultNamespaceUri = "http://www.ilimi.in";
	protected String defaultNamespacePrefix = "ili";

	protected Map<String, String> namespaceMap;
	protected Map<String, String> propertyNamespaceMap;

	public RDFGraphWriter(List<Node> nodes, List<Relation> relations) {
		this.nodes = nodes;
		this.relations = null == relations ? new ArrayList<Relation>() : relations;

		namespaceMap = new HashMap<String, String>();
		propertyNamespaceMap = new HashMap<String, String>();
		addNamespace(defaultNamespacePrefix, defaultNamespaceUri);
		addNamespace("owl", "http://www.w3.org/2002/07/owl");
		addNamespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns");
		addNamespace("rdfs", "http://www.w3.org/2000/01/rdf-schema");
		addPropertyNamespace("type", "rdf");
		addPropertyNamespace("Class", "owl");
		addPropertyNamespace("DataTypeProperty", "owl");

	}

	public RDFGraphWriter() {
		namespaceMap = new HashMap<String, String>();
		propertyNamespaceMap = new HashMap<String, String>();
		addNamespace(defaultNamespacePrefix, defaultNamespaceUri);
		addNamespace("owl", "http://www.w3.org/2002/07/owl");
		addNamespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns");
		addNamespace("rdfs", "http://www.w3.org/2000/01/rdf-schema");
		addPropertyNamespace("type", "rdf");
		addPropertyNamespace("Class", "owl");
		addPropertyNamespace("DataTypeProperty", "owl");
	}

	public InputStream getRDF(Node node) throws Exception {
		RDFFormat format = Rio.getWriterFormatForMIMEType(MIME_RDFXML, RDFFormat.RDFXML);
		StringWriter out = new StringWriter();
		RDFWriter writer = Rio.createWriter(format, out);
		Map<Object, URI> uriCache = new HashMap<Object, URI>();

		// Start writing the RDF content
		writer.startRDF();
		// Write all name-spaces to the output
		for (String prefix : namespaceMap.keySet()) {
			writer.handleNamespace(prefix, namespaceMap.get(prefix) + "#");
		}
		URI subject = getUri(node, uriCache);

		String idval = node.getIdentifier();
		URI idPredicate = getUri(PROPERTY_ID, uriCache);
		Value idObject = new LiteralImpl(idval);
		Statement st = new StatementImpl(subject, idPredicate, idObject);
		writer.handleStatement(st);
		Statement nodeTypeSt = new StatementImpl(subject, getUri(PROPERTY_NODE_TYPE, uriCache),
				new LiteralImpl(node.getNodeType()));
		writer.handleStatement(nodeTypeSt);
		if (StringUtils.isNotBlank(node.getObjectType())) {
			Statement objTypeSt = new StatementImpl(subject, getUri(PROPERTY_OBJECT_TYPE, uriCache),
					new LiteralImpl(node.getObjectType()));
			writer.handleStatement(objTypeSt);
		}

		for (Entry<String, Object> entry : node.getMetadata().entrySet()) {
			Object val = entry.getValue();
			URI predicate = getUri(entry.getKey(), uriCache);
			Value object = new LiteralImpl(val.toString());
			Statement stMeta = new StatementImpl(subject, predicate, object);
			writer.handleStatement(stMeta);
		}

		if (null != node.getInRelations()) {
			for (Relation relation : node.getInRelations()) {
				URI relSubject = getUri(relation, uriCache);

				if (null != relation.getMetadata()) {
					for (Entry<String, Object> entry : relation.getMetadata().entrySet()) {
						URI relPredicate = getUri(entry.getKey(), uriCache);
						Value relObject = new LiteralImpl(entry.getValue().toString());
						writer.handleStatement(new StatementImpl(relSubject, relPredicate, relObject));
					}
				}

				URI relEnd = getUri(relation.getEndNodeId(), uriCache);
				URI relPredicate = getUri(PROPERTY_RELATION_END, uriCache);
				writer.handleStatement(new StatementImpl(relSubject, relPredicate, relEnd));

				URI relFrom = getUri(relation.getStartNodeId(), uriCache);
				relPredicate = getUri(PROPERTY_RELATION_START, uriCache);
				writer.handleStatement(new StatementImpl(relSubject, relPredicate, relFrom));

				Literal label = new LiteralImpl(relation.getRelationType());
				relPredicate = getUri(PROPERTY_RELATION_TYPE, uriCache);
				writer.handleStatement(new StatementImpl(relSubject, relPredicate, label));
			}
		}

		if (null != node.getOutRelations()) {
			for (Relation relation : node.getOutRelations()) {
				URI outRelSubject = getUri(relation, uriCache);

				if (null != relation.getMetadata()) {
					for (Entry<String, Object> entry : relation.getMetadata().entrySet()) {
						URI relPredicate = getUri(entry.getKey(), uriCache);
						Value relObject = new LiteralImpl(entry.getValue().toString());
						writer.handleStatement(new StatementImpl(outRelSubject, relPredicate, relObject));
					}
				}

				URI relEnd = getUri(relation.getEndNodeId(), uriCache);
				URI relPredicate = getUri(PROPERTY_RELATION_END, uriCache);
				writer.handleStatement(new StatementImpl(outRelSubject, relPredicate, relEnd));

				URI relFrom = getUri(relation.getStartNodeId(), uriCache);
				relPredicate = getUri(PROPERTY_RELATION_START, uriCache);
				writer.handleStatement(new StatementImpl(outRelSubject, relPredicate, relFrom));

				Literal label = new LiteralImpl(relation.getRelationType());
				relPredicate = getUri(PROPERTY_RELATION_TYPE, uriCache);
				writer.handleStatement(new StatementImpl(outRelSubject, relPredicate, label));
			}
		}
		writer.endRDF();
		InputStream inputStream = new ByteArrayInputStream(out.toString().getBytes());
		return inputStream;
	}

	public InputStream getRDF(List<Node> nodes, List<Relation> relations) throws Exception {

		return null;
	}

	@Override
	public OutputStream getData() throws Exception {
		RDFFormat format = Rio.getWriterFormatForMIMEType(MIME_RDFXML, RDFFormat.RDFXML);
		StringWriter out = new StringWriter();
		RDFWriter writer = Rio.createWriter(format, out);
		Map<Object, URI> uriCache = new HashMap<Object, URI>();

		// Start writing the RDF content
		writer.startRDF();
		// Write all name-spaces to the output
		for (String prefix : namespaceMap.keySet()) {
			writer.handleNamespace(prefix, namespaceMap.get(prefix) + "#");
		}

		for (Node node : nodes) {
			URI subject = getUri(node, uriCache);

			String idval = node.getIdentifier();
			URI idPredicate = getUri(PROPERTY_ID, uriCache);
			Value idObject = new LiteralImpl(idval);
			Statement st = new StatementImpl(subject, idPredicate, idObject);
			writer.handleStatement(st);
			Statement nodeTypeSt = new StatementImpl(subject, getUri(PROPERTY_NODE_TYPE, uriCache),
					new LiteralImpl(node.getNodeType()));
			writer.handleStatement(nodeTypeSt);
			if (StringUtils.isNotBlank(node.getObjectType())) {
				Statement objTypeSt = new StatementImpl(subject, getUri(PROPERTY_OBJECT_TYPE, uriCache),
						new LiteralImpl(node.getObjectType()));
				writer.handleStatement(objTypeSt);
			}

			if (SystemNodeTypes.DEFINITION_NODE.name().equals(node.getNodeType())) {
				if (null != node.getMetadata().get("INDEXABLE_METADATA_KEY")) {
					URI predIndMeta = getUri("indexedMetadata", uriCache);
					Value indMeta = new LiteralImpl(node.getMetadata().get("INDEXABLE_METADATA_KEY").toString());
					Statement stIndMeta = new StatementImpl(subject, predIndMeta, indMeta);
					writer.handleStatement(stIndMeta);
				}
				if (null != node.getMetadata().get("NON_INDEXABLE_METADATA_KEY")) {
					URI preNonIndMeta = getUri("nonIndexedMetadata", uriCache);
					Value nonIndMeta = new LiteralImpl(node.getMetadata().get("NON_INDEXABLE_METADATA_KEY").toString());
					Statement stNonIndMeta = new StatementImpl(subject, preNonIndMeta, nonIndMeta);
					writer.handleStatement(stNonIndMeta);
				}

				if (null != node.getMetadata().get("IN_RELATIONS_KEY")) {
					URI preInRelMeta = getUri("inRelations", uriCache);
					Value inRelMeta = new LiteralImpl(node.getMetadata().get("IN_RELATIONS_KEY").toString());
					Statement stInRelMeta = new StatementImpl(subject, preInRelMeta, inRelMeta);
					writer.handleStatement(stInRelMeta);
				}

				if (null != node.getMetadata().get("OUT_RELATIONS_KEY")) {
					URI preOutRelMeta = getUri("outRelations", uriCache);
					Value outRelMeta = new LiteralImpl(node.getMetadata().get("OUT_RELATIONS_KEY").toString());
					Statement stOutRelMeta = new StatementImpl(subject, preOutRelMeta, outRelMeta);
					writer.handleStatement(stOutRelMeta);
				}

			} else if (!SystemNodeTypes.DEFINITION_NODE.name().equals(node.getNodeType())
					&& !SystemNodeTypes.ROOT_NODE.name().equals(node.getNodeType())) {
				// Serialize all properties as RDF statements
				for (Entry<String, Object> entry : node.getMetadata().entrySet()) {
					Object val = entry.getValue();
					URI predicate = getUri(entry.getKey(), uriCache);
					Value object = new LiteralImpl(val.toString());
					Statement stMeta = new StatementImpl(subject, predicate, object);
					writer.handleStatement(stMeta);
				}
			}

		}

		for (Relation relation : relations) {
			URI subject = getUri(relation, uriCache);

			if (null != relation.getMetadata()) {
				for (Entry<String, Object> entry : relation.getMetadata().entrySet()) {
					URI relPredicate = getUri(entry.getKey(), uriCache);
					Value relObject = new LiteralImpl(entry.getValue().toString());
					writer.handleStatement(new StatementImpl(subject, relPredicate, relObject));
				}
			}

			URI relEnd = getUri(relation.getEndNodeId(), uriCache);
			URI relPredicate = getUri(PROPERTY_RELATION_END, uriCache);
			writer.handleStatement(new StatementImpl(subject, relPredicate, relEnd));

			URI relFrom = getUri(relation.getStartNodeId(), uriCache);
			relPredicate = getUri(PROPERTY_RELATION_START, uriCache);
			writer.handleStatement(new StatementImpl(subject, relPredicate, relFrom));

			Literal label = new LiteralImpl(relation.getRelationType());
			relPredicate = getUri(PROPERTY_RELATION_TYPE, uriCache);
			writer.handleStatement(new StatementImpl(subject, relPredicate, label));
		}

		writer.endRDF();

		try (OutputStream outputStream = new ByteArrayOutputStream()) {
			outputStream.write(out.toString().getBytes());
			return outputStream;
		}
	}

	/**
	 * Registers additional prefix and URIs to be used in RDF
	 * 
	 * @param prefix
	 *            Prefix (e.g. pcp)
	 * @param uri
	 *            URI of the ontology (e.g.
	 *            http://com.canopusconsulting.com/preceptron)
	 */
	public void addNamespace(String prefix, String uri) {
		namespaceMap.put(prefix, uri);
	}

	/**
	 * Registers the namespace for a property. Since properties are simple
	 * strings, separate namespace registration is required.
	 * 
	 * @param property
	 *            Property name
	 * @param prefix
	 *            Prefix (e.g. pcp)
	 */
	public void addPropertyNamespace(String property, String prefix) {
		propertyNamespaceMap.put(property, prefix);
	}

	private URI getUri(Node node, Map<Object, URI> cache) {

		URI uri = cache.get("NODE_" + node.getIdentifier());
		if (uri == null) {
			String name = node.getIdentifier();
			try {
				new URIImpl(name);
			} catch (IllegalArgumentException e) {
				name = defaultNamespaceUri + "#" + name;
			}
			uri = new URIImpl(name);
			cache.put("NODE_" + node.getIdentifier(), uri);
		}
		return uri;
	}

	/**
	 * Returns the URI for the given property. If the NS was registered, it is
	 * used, Otherwise ther chemilcats on too hee.
	 * 
	 * @param property
	 *            Property for which the prefix is defined
	 * @param cache
	 *            Cache of the node/edge to the URI
	 * @return URI of the object. If the object didnt exist, places it in the
	 *         cache.
	 */
	protected URI getUri(String property, Map<Object, URI> cache) {

		if (StringUtils.equals(property, "Class")) {
			return OWL.CLASS;
		} else if (StringUtils.equals(property, "DataTypeProperty")) {
			return OWL.DATATYPEPROPERTY;
		}

		URI uri = cache.get(property);
		if (uri == null) {
			String name = property;
			String namespace = propertyNamespaceMap.get(property);
			namespace = (namespace == null ? defaultNamespaceUri : namespaceMap.get(namespace));
			uri = new URIImpl(namespace + "#" + name);
			cache.put(property, uri);
		}
		return uri;
	}

	private URI getUri(Relation relation, Map<Object, URI> cache) {

		URI uri = cache.get("RELATION_" + relation.getId());
		if (uri == null) {
			URI startNodeURI = getUri(relation.getStartNodeId(), cache);
			uri = new URIImpl(startNodeURI + "/relation" + relation.getId());
			cache.put("RELATION_" + relation.getId(), uri);
		}
		return uri;
	}

}
