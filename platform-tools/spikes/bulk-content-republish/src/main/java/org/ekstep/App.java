package org.ekstep;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.ekstep.common.Platform;
import org.ekstep.common.dto.Response;
import org.ekstep.common.exception.ResourceNotFoundException;
import org.ekstep.graph.dac.enums.GraphDACParams;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.dac.model.SearchCriteria;
import org.ekstep.graph.engine.router.ActorBootstrap;


public class App {

	private static GraphUtil util = new GraphUtil();
	private static InstructionEventFeeder feeder = new InstructionEventFeeder();
	private static int totalSize = 0;
	private final static int defaultBatchSize = 500;
	public static SearchCriteria sc = null;

	public static void main(String[] args) throws Exception {
		System.out.println("I am in main..");
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
			File file = getFile(true);
			FileWriter writer = new FileWriter(file, true);
			Set<String> processedObjects = getProcessedObject(file);
			boolean found = true;
			int maxContentCount=0;
			List<String> processed_ids = new ArrayList<String>();
			int start = sc.getStartPosition();
			int count = 0;
			int counter = 0;
			int maxTimeGap = Platform.config.getInt("max.time.gap");
			//check and change result size to set it to minimum batches
			if (sc.getResultSize() == 0)
				sc.setResultSize(defaultBatchSize);
			else {
				totalSize = sc.getResultSize();
				if(sc.getResultSize()>defaultBatchSize)
					sc.setResultSize(defaultBatchSize);
			}
			if(Platform.config.hasPath("max.content.count"))
				maxContentCount = Platform.config.getInt("max.content.count");
			System.out.println("maxContentCount***:"+ maxContentCount);
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
							if (imgNode == null && !processedObjects.contains(node.getIdentifier())) {
								processed_ids.add(node.getIdentifier());
								feeder.push(node, "Public");
								writeToFile(writer, node);
								counter++;
								System.out.println("counter*****: "+ counter);
								System.out.println("Sleep Starts: " + System.currentTimeMillis());
								Thread.sleep(maxTimeGap);
								System.out.println("Sleep Ends: " + System.currentTimeMillis());
							}
						}catch(InterruptedException e) {
							e.printStackTrace();
						}catch (Exception e) {
							e.printStackTrace();
						}
						if(maxContentCount!=0 && counter==maxContentCount) {
							System.out.println("******counter*****: "+ counter);
							found = false;
							break;
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
		File file = getFile(false);
		List<String> notPublishedContent = readBatch(file);
		
		if(null == notPublishedContent || notPublishedContent.isEmpty()) {
			System.out.println("****************** All contents got published ******************");
		}else {
			System.out.println("****************** There are total " + notPublishedContent.size() + " contents not got published ******************");
			System.out.println(notPublishedContent.toString());
		}
		
	}
	
	private static List<String> readBatch(File file) throws IOException {
		Map<String, Integer> resultMap = new HashMap<String, Integer>();   
		String line = null;
		BufferedReader bufferedReader = null;
		List<String> notPublishedContent = null;
		try {
			bufferedReader = new BufferedReader(new FileReader(file));
			while((line = bufferedReader.readLine()) != null) {
				String[] arr = line.split(",");
				resultMap.put(arr[0], Integer.parseInt(arr[1]));
			}
			return addUnpublishedContent(resultMap);
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			if(null != bufferedReader)
				bufferedReader.close();
			ActorBootstrap.getActorSystem().shutdown();
		}
		return notPublishedContent;
	}
	
	private static List<String> addUnpublishedContent(Map<String, Integer> map) {
		List<String> contentList = new ArrayList<String>(map.keySet());
		List<Node> nodes = null;
		List<String> notPublishedContent = new ArrayList();
		int batchSize = 50;
		while(!contentList.isEmpty()) {
			List<String> sublist;
			if(contentList.size()>batchSize)
				sublist = contentList.subList(0, batchSize);
			else
				sublist = contentList.subList(0, contentList.size());
			if(!sublist.isEmpty()) {
				Response response = util.getDataNodes("domain", sublist);
				if (StringUtils.equalsIgnoreCase("failed", response.getParams().getStatus()))
					throw new ResourceNotFoundException("NODES_NOT_FOUND", "Nodes not found: domain");
				else {
					nodes = (List<Node>) response.get(GraphDACParams.node_list.name());
				}
			}
			
			if(null != nodes && !nodes.isEmpty()) {
				for(Node node : nodes) {
					Double pkgVersion = getDoubleValue(node.getMetadata().get("pkgVersion"));
					int republishedContentPkgVer = pkgVersion.intValue();
					int beforeRepublishedContentPkgVer = map.get(node.getIdentifier());
					if(!(beforeRepublishedContentPkgVer<republishedContentPkgVer)) {
						notPublishedContent.add(node.getIdentifier());
					}
				}
			}
			contentList.removeIf(x -> sublist.contains(x));
		}
		return notPublishedContent;
	}
	
	private static File getFile(boolean fileToCreate) throws IOException {
		String path = Platform.config.getString("validation.filePath");
		File file = new File(path);
		
		if(!file.exists() && !fileToCreate) {
			System.out.println("File: "+ file.getName() + " not exists.");
			throw new FileNotFoundException();
		}
		if(!file.exists() && fileToCreate) {
			file.getParentFile().mkdirs();
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
	private static void writeToFile(FileWriter writer, Node node) throws IOException {
		writer.write(node.getIdentifier());
		writer.write(",");
		Double d = getDoubleValue(node.getMetadata().get("pkgVersion"));
		String a = String.valueOf(d.intValue());
		writer.write(a);
		writer.write("\r\n");
	}
	private static Set<String> getProcessedObject(File file){
		Set<String> objects = new HashSet<String>();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(file));
			String line = br.readLine();
			while(null != line) {
				String[] attributes = line.split(",");
				objects.add(attributes[0]);
				System.out.println("Added: " + attributes[0]);
				line = br.readLine();
			}
			return objects;
		}catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			if(null != br) {
				try {
					br.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		return objects;
	}
}
