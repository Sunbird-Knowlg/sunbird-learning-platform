package com.ilimi.taxonomy.mgr;

import java.io.File;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;
import com.ilimi.taxonomy.mgr.impl.ContentManagerImpl;

/**
 * The Interface IContentManager is the Contract for the operations that can be
 * perform on Content Node in the Graph. Including all Low (CRUD) Level and
 * high-level operations.
 * 
 * The sub-class implementing these operations should take care of uploading the
 * artifacts or assets to the respective Storage Space.
 * 
 * @author Azhar
 * @see ContentManagerImpl
 */
public interface IContentManager {

	/**
	 * Upload is High level Content Operation uploads the content package over
	 * the storage space and set the <code>artifactUrl</code> of the
	 * <code>node</code>. Logically interns it calls for <code>Extract</code>
	 * operation after uploading the <code>content package</code>.
	 * 
	 * <p>
	 * It is a <code>Pipelined Operation</code> which is accomplished by several
	 * <code>Processors</code> meant for atomic tasks.
	 * 
	 * <p>
	 * A subclass must provide an implementation of this method.
	 *
	 * @param id
	 *            the content <code>identifier</code> for which the content
	 *            package needs to be uploaded.
	 * @param taxonomyId
	 *            the <code>graph id</code> of the content.
	 * @param uploadedFile
	 *            the uploaded file is the <code>zip content package</code>.
	 * @param folder
	 *            the storage space folder where the uploaded content should
	 *            stored.
	 * @return the response contains the node id as <code>node_id</code> for
	 *         which the content is being uploaded.
	 */
	Response upload(String id, String taxonomyId, File uploadedFile, String folder);

	/**
	 * Optimize is High level Content Operation mainly deals with optimizing the
	 * Content and its <code>artifact<code> such as <code>assets<code> and
	 * <code>icon or banner<code> images e.g. compress the images, audio and
	 * video for decrease in size. It also deals with the code level
	 * optimization of content package by removing the unnecessary code.
	 * 
	 * This functionality helps in reducing the size of the content without
	 * compromising the quality.
	 * 
	 * <p> It is a <code>Pipelined Operation</code> which is accomplished by
	 * several <code>Processors</code> meant for atomic tasks.
	 * 
	 * <p>
	 * A subclass must provide an implementation of this method.
	 *
	 * @param taxonomyId
	 *            the <code>graph id</code> of the content.
	 * 
	 * @param contentId
	 *            the content <code>identifier</code> which needs to be publish.
	 * @return the response contains the optimization status as
	 *         <code>optStatus</code>
	 */
	Response optimize(String taxonomyId, String contentId);

	/**
	 * Publish is High level Content Operation mainly deals with the tasks
	 * needed for making any content in <code>LIVE</code> state. It includes the
	 * downloading of all the <code>assets</code> and <code>icons</code> to the
	 * storage space, replace the <code>URLs</code> with relative Urls, set the
	 * <code>body</code> of content object. Finally Creates the
	 * <code>ECAR</code> and upload the package to Storage Space, update the
	 * package version information, sets the <code>downloadUrl</code> property.
	 * 
	 * <p>
	 * It is a <code>Pipelined Operation</code> which is accomplished by several
	 * <code>Processors</code> meant for atomic tasks.
	 * 
	 * <p>
	 * A subclass must provide an implementation of this method.
	 *
	 * @param taxonomyId
	 *            the <code>graph id</code> of the content.
	 * @param contentId
	 *            the content <code>identifier</code> which needs to be publish.
	 * @return the response contains the ECAR <code>URL</code> in its Result Set
	 */
	Response publish(String taxonomyId, String contentId);

	/**
	 * Bundle is a High level Content Operation mainly deals with providing the
	 * Content Bundle for offline usage. It takes a map of Content Identifiers
	 * in the request body as <code>content_identifiers</code> and bundle file
	 * name as <code>file_name</code> Reads all the content id and get the node
	 * meta-data, downloads all the <code>assets</code> and other artifacts
	 * which can downloaded and localize their address in the index file (if
	 * applicable, which is based on mimeType of content). Create a
	 * <code>Manifest.json</code> file which contains all the meta-data along
	 * with the header information. Finally Creates an <code>ECAR</code> and
	 * upload it to Storage Space and <code>return</code> the URL.
	 * 
	 * <p>
	 * It is a <code>Pipelined Operation</code> which is accomplished by several
	 * <code>Processors</code> meant for atomic tasks.
	 * 
	 * <p>
	 * A subclass must provide an implementation of this method.
	 *
	 * @param request
	 *            the request contains the list of content
	 *            <code>identifiers</code> and bundle file name in its
	 *            <code>body</code>.
	 * @param taxonomyId
	 *            the <code>graph id</code> of the content.
	 * @param version
	 *            the <code>version</code> of Content Bundle can be seen in
	 *            <code>Manifest File</code> Header.
	 * @return the response contains the ECAR <code>URL</code> and node id as
	 *         <code>node_id</code> in its Result Set
	 */
	Response bundle(Request request, String taxonomyId, String version);

}
