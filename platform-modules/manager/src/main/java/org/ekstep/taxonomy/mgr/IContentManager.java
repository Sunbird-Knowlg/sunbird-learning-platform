package org.ekstep.taxonomy.mgr;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;

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
	 * @param uploadedFile
	 *            the uploaded file is the <code>zip content package</code>.
	 * @return the response contains the node id as <code>node_id</code> for
	 *         which the content is being uploaded.
	 */
	Response upload(String id, File uploadedFile, String mimeType);

	/**
	 * Upload is High level Content Operation to set the
	 * <code>artifactUrl</code> of the <code>node</code>.
	 * 
	 * 
	 * <p>
	 * A subclass must provide an implementation of this method.
	 *
	 * @param id
	 *            the content <code>identifier</code> for which the content
	 *            package needs to be uploaded.
	 * @param fileUrl
	 *            the file URL is the <code>zip content package path</code>.
	 * @return the response contains the node id as <code>node_id</code> for
	 *         which the content is being uploaded.
	 */
	Response upload(String id, String fileUrl, String mimeType);

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
	 * @param contentId
	 *            the content <code>identifier</code> which needs to be publish.
	 * @return the response contains the optimization status as
	 *         <code>optStatus</code>
	 */
	Response optimize(String contentId);

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
	 * @param contentId
	 *            the content <code>identifier</code> which needs to be publish.
	 * @param requestMap
	 *            the map of request params
	 * @return the response contains the ECAR <code>URL</code> in its Result Set
	 */
	Response publish(String contentId, Map<String, Object> requestMap);

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
	 * @param version
	 *            the <code>version</code> of Content Bundle can be seen in
	 *            <code>Manifest File</code> Header.
	 * @return the response contains the ECAR <code>URL</code> and node id as
	 *         <code>node_id</code> in its Result Set
	 */
	Response bundle(Request request, String version);

	/**
	 * Review is High level Content Operation mainly deals with the tasks needed
	 * for making any content in <code>Review</code> state. It includes the
	 * validation of Content based on its type.
	 * 
	 * <p>
	 * It is a <code>Pipelined Operation</code> which is accomplished by several
	 * <code>Processors</code> meant for atomic tasks.
	 * 
	 * <p>
	 * A subclass must provide an implementation of this method.
	 *
	 * @param contentId
	 *            the content <code>identifier</code> which needs to be review.
	 * @param requestMap
	 *            the map of request params
	 * @return the response contains the Node <code>identifier</code> in its
	 *         Result Set.
	 * @throws Exception
	 */
	Response review(String contentId, Request request) throws Exception;

	/**
	 * This method returns the full hierarchy of a content. The "Sequence
	 * Membership" relation is traversed to compute the hierarchy of the
	 * content.
	 * 
	 * A subclass must provide an implementation of this method.
	 *
	 * @param contentId
	 *            the content <code>identifier</code> whose hierarchy needs to
	 *            be returned
	 * @param mode
	 *            if edit, returns the hierarchy of the Draft version, else
	 *            returns the hierarchy of the Live version. If Live version
	 *            does not exist, Draft version is returned
	 * @return the response contains the hierarchy of the <code>content</code>
	 *         in its Result Set.
	 */
	Response getHierarchy(String contentId, String mode, List<String> fields) throws Exception;

	/**
	 * This method returns the content.
	 * 
	 * A subclass must provide an implementation of this method.
	 * 
	 * @param contentId
	 *            the content <code>identifier</code> whose hierarchy needs to
	 *            be returned
	 * @param mode
	 *            if edit, returns the content's Draft version, else returns the
	 *            content's Live version. If Live version does not exist, Draft
	 *            version is returned
	 * @param fields
	 *            TODO
	 *
	 * @return the response contains the <code>content</code> in its Result Set.
	 */
	Response find(String contentId, String mode, List<String> fields);

	/**
	 * @param map
	 * @return
	 * @throws Exception
	 */
	Response create(Map<String, Object> map, String channelId) throws Exception;

	/**
	 * @param contentId
	 * @param map
	 * @return
	 * @throws Exception
	 */
	Response update(String contentId, Map<String, Object> map) throws Exception;

	/**
	 * @param contentId
	 * @param fileName
	 * @param type
	 * @return
	 */
	Response preSignedURL(String contentId, String fileName, String type);

	/**
	 * @param data
	 * @return
	 */
	Response updateHierarchy(Map<String, Object> data);

	/**
	 * @param contentId
	 * @param map
	 * @return
	 * @throws Exception
	 */
	Response updateAllContents(String contentId, Map<String, Object> map) throws Exception;

	/**
	 * @param channelId
	 * @param reqObj
	 * @return
	 * @throws Exception
	 */
	Response linkDialCode(String channelId, Object reqObj) throws Exception;

	/**
	 * @param contentId
	 * @param requestMap
	 * @param mode
	 * @return
	 */
	Response copyContent(String contentId, Map<String, Object> requestMap, String mode);

    /**
     * @param contentId
     * @return
     */
    Response retire(String contentId);
    
    /**
     * @param contentId
     * @return
     * @throws Exception
     */
    Response acceptFlag(String contentId) throws Exception;

    Response syncHierarchy(String identifier);
    
    /**
     * @param contentId
     * @param channelId
     * @param reqObj
     * @return
     * @throws Exception
     */
    Response reserveDialCode(String contentId, String channelId, Map<String, Object> reqMap) throws Exception;

	/**
	 *
	 * @param contentId
	 * @param channelId
	 * @return
	 * @throws Exception
	 */
	Response releaseDialcodes(String contentId, String channelId) throws Exception;

	Response getContentHierarchy(String contentId, String mode, List<String> fields) throws Exception;
}