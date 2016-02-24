/**
 * 
 */
package com.ilimi.taxonomy.mgr;

import com.ilimi.common.dto.Response;
import com.ilimi.graph.dac.model.Node;

/**
 * @author ilimi
 *
 */
public interface IMimeTypeManager {

	public void upload();
	
	public Response extract(Node node);
	
	public void publish();
	
	public void bundle();
	
}
