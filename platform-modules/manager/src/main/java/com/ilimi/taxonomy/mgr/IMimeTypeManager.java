/**
 * 
 */
package com.ilimi.taxonomy.mgr;

import java.io.File;

import com.ilimi.common.dto.Response;
import com.ilimi.graph.dac.model.Node;

/**
 * The Interface IMimeTypeManager.
 *
 * @author Azhar
 */
public interface IMimeTypeManager {

	public Response upload(Node node, File uploadFile, String folder);
	
	public Response extract(Node node);
	
	public Response publish(Node node);
	
	public Node tuneInputForBundling(Node node);
	
}
