package org.ekstep;

import java.util.List;

import org.ekstep.graph.dac.model.Node;
import org.ekstep.graph.engine.router.ActorBootstrap;

public class App {

	private static final int batchSize = 1000;
	private static GraphUtil util = new GraphUtil();
	private static InstructionEventFeeder feeder = new InstructionEventFeeder();
			
	public static void main(String[] args) {
		process();
		ActorBootstrap.getActorSystem().shutdown();
	}
	
	private static void process() {
		boolean found = true;
		int start = 0;
		while (found) {
			List<Node> nodes = util.getLiveContentNodes( start, batchSize);
			if (null != nodes && !nodes.isEmpty()) {
				System.out.println(batchSize + " objects are getting synced");
				start += batchSize;
				for(Node node:nodes)
					try {
						feeder.push(node, "public");
					} catch (Exception e) {
						e.printStackTrace();
					}
			} else {
				found = false;
				break;
			}
		}
	}
}
