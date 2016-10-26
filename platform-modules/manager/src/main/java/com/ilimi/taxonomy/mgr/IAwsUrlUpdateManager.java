package com.ilimi.taxonomy.mgr;

import com.ilimi.common.dto.Response;

/**
 * IAwsUrlUpdateManager provides an interface for implementing any operations
 * related to s3 specific relocation changes
 * 
 * The sub-class implementing these operations should take care of updating the aws 
 * URLs accurately so that after these artifacts in s3 can be accessed without any glitches
 * 
 * @author Jyotsna
 * @see AwsUrlUpdateManagerImpl
 */
public interface IAwsUrlUpdateManager {

	/**
	 * This operation fetches all nodes of a particular object type present in the graph and 
	 * looks for any property which has an AWS url configured and updates the url
	 * with the modified bucket and region. This helps in accessing the objects once they are
	 * relocated to a different region or bucket.
	 * 
	 * A subclass must provide an implementation of this method.
	 *
	 * @param objectType
	 *            objectType of the node.
	 * 
	 * @param graphId
	 *            graphId of the node
	 * @param apiId
	 * 			  apiId of the api
	 * @return the response contains the list of identifiers of the failed nodes 
	 * if there are any
	 */
	Response updateNodesWithUrl(String objectType, String graphId, String apiId);

}
