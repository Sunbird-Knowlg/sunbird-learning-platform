package org.ekstep;

import java.util.List;

import org.ekstep.common.Platform;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.engine.router.ActorBootstrap;
import org.ekstep.GraphUtil;
import org.ekstep.InstructionEventFeeder;

public class App {

	private static final int batchSize = 500;
	private static GraphUtil util = new GraphUtil();
	private static InstructionEventFeeder feeder = new InstructionEventFeeder();
	private static int totalSize = 0;
	
	public static void main(String[] args) {
		if(Platform.config.hasPath("total.size")) {
			totalSize = Platform.config.getInt("total.size");
		}
		process();
		ActorBootstrap.getActorSystem().shutdown();
		
	}
	
	private static void process() {
		boolean found = true;
		int start = 0;
		int count = 0;
		while (found) {
			List<Node> nodes = util.getLiveContentNodes( start, batchSize);
			if (null != nodes && !nodes.isEmpty()) {
				System.out.println(batchSize + " objects are getting synced");
				start += batchSize;
				for(Node node:nodes) {
					try {
						String imgNodeId = node.getIdentifier() + ".img";
						Node imgNode = util.getNode("domain", imgNodeId);
						if(imgNode==null) {
							System.out.println("pushing publish message for id="+node.getIdentifier());
							feeder.push(node, "Public");							
							count++;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					if(totalSize!=0&&totalSize==count) {
						found = false;
						break;						
					}
				}
			} else {
				found = false;
				break;
			}
		}
	}
}
