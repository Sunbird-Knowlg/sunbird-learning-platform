/**
 * 
 */
package com.ilimi.taxonomy.mgr;

import java.io.File;

import com.ilimi.common.dto.Response;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.taxonomy.mgr.impl.APKMimeTypeMgrImpl;
import com.ilimi.taxonomy.mgr.impl.AssetsMimeTypeMgrImpl;
import com.ilimi.taxonomy.mgr.impl.CollectionMimeTypeMgrImpl;
import com.ilimi.taxonomy.mgr.impl.ECMLMimeTypeMgrImpl;
import com.ilimi.taxonomy.mgr.impl.HTMLMimeTypeMgrImpl;

/**
 * The Interface IMimeTypeManager provide a way to have different implementation
 * for the different MimeType of all the Content Operation. The Interface is
 * implemented by Each MimeType which an EkStep Content Support.
 * 
 * All the operations are being called by the implementation of
 * IContentManager's High Level Operations such as Upload and Publish.
 *
 * @author Azhar
 * 
 * @see ECMLMimeTypeMgrImpl
 * @see HTMLMimeTypeMgrImpl
 * @see APKMimeTypeMgrImpl
 * @see CollectionMimeTypeMgrImpl
 * @see AssetsMimeTypeMgrImpl
 */
public interface IMimeTypeManager {

	/**
	 * The <code>upload</code> method carries the entire task of Content Upload
	 * operation. Other than Preliminary validation it perform all the tasks of
	 * Content Upload. This Method is directly backed by Content Work-flow
	 * Pipeline.
	 * 
	 *
	 * @param node
	 *            the content <code>node</code> for which the content package is
	 *            being uploaded.
	 * @param uploadFile
	 *            the upload <code>file</code> is the content package file.
	 * @param folder
	 *            the <code>folder</code> is the location on the storage space
	 *            where the content should be uploaded.
	 * @return the <code>response</code> object contains the node id as
	 *         <code>node_id</code> and the <code>artifactUrl</code> property
	 *         i.e. the <code>URL</code> where the file is uploaded.
	 */
	public Response upload(Node node, File uploadFile, String folder);

	/**
	 * The <code>publish</code> method is a vital in Content work-flow carries
	 * all the tasks needed for Content Publish Operation. This Method is
	 * directly backed by Content Work-flow Pipeline.
	 *
	 * @param node
	 *            the content <code>node</code> for which the publish operation
	 *            has to be performed.
	 * @return the <code>response</code> object contains node id as
	 *         <code>node_id<c/ode> of the content
	 *         being published and the <code>URL</code> of the <code>ECAR</code>
	 *         file i.e. the value as <code>downloadUrl</code> property of
	 *         content node.
	 */
	public Response publish(Node node);

}
