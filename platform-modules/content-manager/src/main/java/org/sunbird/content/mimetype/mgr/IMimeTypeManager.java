/**
 * 
 */
package org.sunbird.content.mimetype.mgr;

import java.io.File;

import org.sunbird.common.dto.Response;
import org.sunbird.content.mimetype.mgr.impl.APKMimeTypeMgrImpl;
import org.sunbird.content.mimetype.mgr.impl.AssetsMimeTypeMgrImpl;
import org.sunbird.content.mimetype.mgr.impl.CollectionMimeTypeMgrImpl;
import org.sunbird.content.mimetype.mgr.impl.ECMLMimeTypeMgrImpl;
import org.sunbird.content.mimetype.mgr.impl.HTMLMimeTypeMgrImpl;
import org.sunbird.graph.dac.model.Node;


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
	 * @return the <code>response</code> object contains the node id as
	 *         <code>node_id</code> and the <code>artifactUrl</code> property
	 *         i.e. the <code>URL</code> where the file is uploaded.
	 */
	public Response upload(String contentId, Node node, File uploadFile, boolean isAsync);
	
	/**
	 * The <code>upload</code> method carries the entire task of Content Upload
	 * operation using file URL. Other than Preliminary validation it perform all the tasks of
	 * Content Upload. This Method is directly backed by Content Work-flow
	 * Pipeline.
	 * @param contentId 
	 * 
	 *
	 * @param node
	 *            the content <code>node</code> for which the content package is
	 *            being uploaded.
	 * @param fileUrl
	 *            <code>URL</code> of the content package file.
	 * @return the <code>response</code> object contains the node id as
	 *         <code>node_id</code> and the <code>artifactUrl</code> property
	 *         i.e. the <code>URL</code> where the file is uploaded.
	 */
	public Response upload(String contentId, Node node, String fileUrl);

	/**
	 * The <code>publish</code> method is a vital method in Content work-flow
	 * carries all the tasks needed for Content Publish Operation. This Method
	 * is directly backed by Content Work-flow Pipeline.
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
	public Response publish(String contentId, Node node, boolean isAsync);

	/**
	 * The <code>review</code> method is a vital method in Content work-flow
	 * carries all the tasks needed for Content Review Process Operation When is
	 * is sent for review from the Content Author. This Method is directly
	 * backed by Content Work-flow Pipeline.
	 *
	 * @param node
	 *            the content <code>node</code> for which the review operation
	 *            has to be performed.
	 * @return the <code>response</code> object contains node id as
	 *         <code>node_id<c/ode> of the content
	 *         being reviewed and the <code>URL</code> of the <code>ECAR</code>
	 *         file i.e. the value as <code>downloadUrl</code> property of
	 *         content node.
	 */
	public Response review(String contentId, Node node, boolean isAsync);

}
