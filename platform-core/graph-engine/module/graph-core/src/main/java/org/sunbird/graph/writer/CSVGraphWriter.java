package org.sunbird.graph.writer;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.StringUtils;
import org.sunbird.graph.dac.enums.SystemNodeTypes;
import org.sunbird.graph.dac.model.Node;
import org.sunbird.graph.dac.model.Relation;
import org.sunbird.graph.reader.CSVGraphReader;

public class CSVGraphWriter implements GraphWriter {

	private static final String NEW_LINE_SEPARATOR = "\n";
	private List<Node> nodes;

	public CSVGraphWriter(List<Node> nodes, List<Relation> relations) {
		this.nodes = nodes;
	}

	@Override
	public OutputStream getData() throws Exception {
		List<String> headers = new ArrayList<String>();
		headers.add(CSVGraphReader.PROPERTY_ID);
		headers.add(CSVGraphReader.PROPERTY_OBJECT_TYPE);
		headers.add(CSVGraphReader.PROPERTY_TAGS);
		List<String[]> dataRows = new ArrayList<String[]>();
		List<Map<String, String>> nodeMaps = new ArrayList<Map<String, String>>();
		if (null != nodes) {
			for (Node node : nodes) {
				if (SystemNodeTypes.DATA_NODE.name().equalsIgnoreCase(node.getNodeType())) {
					Map<String, String> nodeMap = getNode(node, headers);
					nodeMaps.add(nodeMap);
				}
			}
		}
		for (Map<String, String> nodeMap : nodeMaps) {
			List<String> nodeData = new ArrayList<String>();
			for (String header : headers) {
				if (StringUtils.isNotBlank(nodeMap.get(header))) {
					nodeData.add(nodeMap.get(header).replaceAll("<", "&lt;").replaceAll(">", "&gt;")
							.replaceAll("\n", "").replaceAll("\r", ""));
				} else {
					nodeData.add("");
				}
			}
			dataRows.add(nodeData.toArray(new String[nodeData.size()]));
		}
		List<String[]> allRows = new ArrayList<String[]>();
		allRows.add(headers.toArray(new String[headers.size()]));
		allRows.addAll(dataRows);
		CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator(NEW_LINE_SEPARATOR);
		try (OutputStream outputStream = new ByteArrayOutputStream();
				OutputStreamWriter osWriter = new OutputStreamWriter(outputStream);
				CSVPrinter writer = new CSVPrinter(osWriter, csvFileFormat)) {
			writer.printRecords(allRows);
			return outputStream;
		}
	}

	public Map<String, String> getNode(Node node, List<String> headers) {
		Map<String, String> nodeMap = new HashMap<String, String>();
		getKeys(node.getMetadata(), headers);
		Map<String, List<String>> relMap = new HashMap<String, List<String>>();
		if (null != node.getOutRelations() && !node.getOutRelations().isEmpty()) {
			for (Relation rel : node.getOutRelations()) {
				String relHeader = CSVGraphReader.REL_HEADER_START_WITH + rel.getRelationType();
				if (!headers.contains(relHeader)) {
					headers.add(relHeader);
				}
				List<String> relatedNodes = relMap.get(relHeader);
				if (null == relatedNodes) {
					relatedNodes = new ArrayList<String>();
					relMap.put(relHeader, relatedNodes);
				}
				relatedNodes.add(rel.getEndNodeId());
			}
		}
		if (null != node.getMetadata() && !node.getMetadata().isEmpty()) {
			for (Entry<String, Object> entry : node.getMetadata().entrySet()) {
				nodeMap.put(entry.getKey(), getStringValue(entry.getValue()));
			}
		}
		for (Entry<String, List<String>> entry : relMap.entrySet()) {
			List<String> rels = entry.getValue();
			String rel = stringify(rels, ",");
			nodeMap.put(entry.getKey(), rel);
		}
		nodeMap.put(CSVGraphReader.PROPERTY_ID, node.getIdentifier());
		nodeMap.put(CSVGraphReader.PROPERTY_OBJECT_TYPE, node.getObjectType());
		return nodeMap;
	}

	private void getKeys(Map<String, Object> metadata, List<String> headers) {
		if (null != metadata && !metadata.isEmpty()) {
			for (String key : metadata.keySet()) {
				if (!headers.contains(key)) {
					headers.add(key);
				}
			}
		}
	}

	@SuppressWarnings("rawtypes")
	private String getStringValue(Object value) {
		if (null != value) {
			if (value instanceof String[]) {
				List list = Arrays.asList((String[]) value);
				return stringify(list, CSVGraphReader.LIST_STR_DELIMITER);
			} else if (value instanceof List) {
				List list = (List) value;
				return stringify(list, CSVGraphReader.LIST_STR_DELIMITER);
			} else {
				return value.toString();
			}
		}
		return "";
	}

	@SuppressWarnings("rawtypes")
	private String stringify(List list, String delim) {
		if (null != list && !list.isEmpty()) {
			String str = "";
			for (int i = 0; i < list.size(); i++) {
				str += list.get(i);
				if (i < list.size() - 1)
					str += delim;
			}
			return str;
		}
		return "";
	}
}
