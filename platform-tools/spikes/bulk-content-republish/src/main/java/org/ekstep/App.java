package org.ekstep;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.ekstep.common.Platform;
import org.ekstep.common.dto.Response;
import org.ekstep.common.exception.ResourceNotFoundException;
import org.ekstep.graph.dac.enums.GraphDACParams;
import org.ekstep.graph.dac.model.Filter;
import org.ekstep.graph.dac.model.MetadataCriterion;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.dac.model.SearchCriteria;
import org.ekstep.graph.engine.router.ActorBootstrap;

import javassist.expr.Instanceof;

public class App {

	private static GraphUtil util = new GraphUtil();
	private static InstructionEventFeeder feeder = new InstructionEventFeeder();
	private static int totalSize = 0;
	private final static int defaultBatchSize = 500;
	public static SearchCriteria sc = null;

	public static void main(String[] args) throws Exception {
		
		if (args.length==0) {
			throw new Exception("First argument is mandatory. It should be 'republish' or 'validate'.");
		}
		
		if(StringUtils.equalsIgnoreCase(args[0], "republish")) {
			String inputArr[] = Arrays.copyOfRange(args, 1, args.length);
			rePublish(inputArr);
		}else if(StringUtils.equalsIgnoreCase(args[0], "validate")) {
			validate();
		}
		
	}
	
	
	private static void rePublish(String[] args) throws Exception {
		int startPostition = 0;
		int resultSize = 0;

		if (args.length>0&&StringUtils.isNotBlank(args[0]))
			try {
				startPostition = Integer.parseInt(args[0]);
			} catch (Exception e) {
				new Exception("args[0] should be startPoisitoin and it should be Integer value");
			}

		if (args.length>1&&StringUtils.isNotBlank(args[1]))
			try {
				resultSize = Integer.parseInt(args[1]);
			} catch (Exception e) {
				new Exception("args[1] should be resultSize and it should be Integer value");
			}

		if (Platform.config.hasPath("search_criteria")) {
			Map<String, Object> search_criteria = (Map<String, Object>) Platform.config.getAnyRef("search_criteria");
			ObjectMapper mapper = new ObjectMapper();
			sc = mapper.convertValue(search_criteria, SearchCriteria.class);

			if (startPostition != 0)
				sc.setStartPosition(startPostition);

			if (resultSize != 0)
				sc.setResultSize(resultSize);

			System.out.println("Search Criteria="+mapper.convertValue(sc, Map.class));

		} else {
			throw new Exception("missing search_criteria in application.conf");
		}
		process();
		ActorBootstrap.getActorSystem().shutdown();
	}

	private static void process() {
		try{
			String path = "publishedContent.csv";
			FileWriter writer = new FileWriter(path, true);
			boolean found = true;
			List<String> processed_ids = new ArrayList<String>();
			int start = sc.getStartPosition();
			int count = 0;
			//check and change result size to set it to minimum batches
			if (sc.getResultSize() == 0)
				sc.setResultSize(defaultBatchSize);
			else {
				totalSize = sc.getResultSize();
				if(sc.getResultSize()>defaultBatchSize)
					sc.setResultSize(defaultBatchSize);
			}

			while (found) {
				if (totalSize != 0 && totalSize == count) {
					System.out.println("Skipping remaining as it reaches result size(" + totalSize + ") to process");
					found = false;
					break;
				}
				List<Node> nodes = null;
				try {
					if (start != 0 && sc.getStartPosition() != start)
						sc.setStartPosition(start);
					nodes = util.getLiveContentNodes(sc);
				} catch (Exception e) {
					System.out.println("error while fetching neo4j records for Live content offset=" + start + ",batchSize="
							+ sc.getResultSize());
					start += sc.getResultSize();
					continue;
				}

				if (null != nodes && !nodes.isEmpty()) {
					System.out.println(nodes.size() + " objects are getting republished, offset=" + sc.getStartPosition());
					start += sc.getResultSize();
					for (Node node : nodes) {
						count++;
						
						try {
							String imgNodeId = node.getIdentifier() + ".img";
							Node imgNode = util.getNode("domain", imgNodeId);
							if (imgNode == null) {
								processed_ids.add(node.getIdentifier());
								feeder.push(node, "Public");
								writer.write(node.getIdentifier());
								writer.write(",");
								Double d = getDoubleValue(node.getMetadata().get("pkgVersion"));
								String a = String.valueOf(d.intValue());
								writer.write(a);
								writer.write("\r\n");
							}
						} catch (Exception e) {
							e.printStackTrace();
						}

					}
				} else {
					found = false;
					break;
				}
			}
			
			System.out.println(processed_ids.size()+" content events got pushed for republishing");
			System.out.println("processed_ids="+processed_ids);
			writer.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	private static void validate() throws Exception{
		File file = getFile();
		List<String> notPublishedContent = new ArrayList<String>();
		
		try {
			
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
			int count = 0;
			Map<String, Integer> map = new HashMap<String, Integer>();
			while ((line = br.readLine()) != null) {
				String[] arr = line.split(",");
				map.put(arr[0], Integer.parseInt(arr[1]));
				count++;
				if(count%50==0) {
					addUnpublishedContent(map, notPublishedContent);
				}
			}
			if(!map.isEmpty()) {
				addUnpublishedContent(map, notPublishedContent);
			}
			br.close();
			if(notPublishedContent.isEmpty()) {
				System.out.println("****************** All contents got published ******************");
			}else {
				System.out.println("****************** There are total " + notPublishedContent.size() + " contents not got published ******************");
				System.out.println(notPublishedContent.toString());
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void addUnpublishedContent(Map<String, Integer> map, List<String> notPublishedContent) {
		List<String> contentList = new ArrayList<String>(map.keySet());
		List<Node> nodes = new ArrayList<Node>();
		
		Response response = util.getDataNodes("domain", contentList);
		if (StringUtils.equalsIgnoreCase("failed", response.getParams().getStatus()))
			throw new ResourceNotFoundException("NODES_NOT_FOUND", "Nodes not found: domain");
		else {
			nodes = (List<Node>) response.get(GraphDACParams.node_list.name());
			
		}
		
		for(Node node : nodes) {
			int republishedContentPkgVer = ((Double)node.getMetadata().get("pkgVersion")).intValue();
			int beforeRepublishedContentPkgVer = map.get(node.getIdentifier());
			if(!(beforeRepublishedContentPkgVer<republishedContentPkgVer)) {
				notPublishedContent.add(node.getIdentifier());
			}
		}
		map.clear();
	}
	
	private static File getFile() throws FileNotFoundException {
		String path = "publishedContent.csv";
		File file = new File(path);
		
		if(!file.exists()) {
			System.out.println("File: "+ file.getName() + " not exists.");
			throw new FileNotFoundException();
		}
		return file;
	}
	
	private static Double getDoubleValue(Object obj) {
		Number n = getNumericValue(obj);
		if (null == n)
			return 0.0;
		return n.doubleValue();
	}
	
	private static Number getNumericValue(Object obj) {
		try {
			return (Number) obj;
		} catch (Exception e) {
			return 0;
		}
	}
}
