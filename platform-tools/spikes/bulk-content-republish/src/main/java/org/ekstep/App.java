package org.ekstep;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.ekstep.common.Platform;
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
	}
}
