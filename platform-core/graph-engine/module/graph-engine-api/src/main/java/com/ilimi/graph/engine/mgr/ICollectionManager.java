package com.ilimi.graph.engine.mgr;

import com.ilimi.graph.common.Request;

/**
 * Graph Engine Manager with API for operations on all types of collections -
 * sequences, sets and tags.
 * 
 * @author rayulu
 * 
 */
public interface ICollectionManager {

    /**
     * API to create a new sequence
     * 
     * @request - GRAPH_ID as request context variable.
     * @request - SEQUENCE_ID Unique id of the sequence
     * @request - MEMBERS Ordered list of sequence members
     * @response - STATUS: API call status
     */
    void createSequence(Request request);

    /**
     * API to create a new set. If a criteria is provided, Set is created as a
     * dynamic set. All nodes matching the specified criteria are automatically
     * added to the set.
     * 
     * @request - GRAPH_ID as request context variable.
     * @request - SET_ID Unique id of the set
     * @request - CRITERIA Criteria for the set.
     * @response - STATUS: API call status
     */
    void createSet(Request request);

    /**
     * API to create a new tag. Tag can be created on a node or a on a node
     * property. If the criteria parameter contains property name, then the tag
     * is created as a tag on metadata property. Else, the tag is created as tag
     * on nodes.
     * 
     * @request - GRAPH_ID as request context variable.
     * @request - TAG_ID Unique id of the tag
     * @request - CRITERIA Criteria for the tag.
     * @request - MEMBERS list of tag members
     * @response - STATUS: API call status
     */
    void createTag(Request request);

    /**
     * API to add a member to a collection. If the collection is a dynamic set
     * and if the given member does not fulfill the criteria, this API returns
     * an error.
     * 
     * @request - GRAPH_ID as request context variable.
     * @request - COLLECTION_ID Id of the collection
     * @request - COLLECTION_TYPE collection type
     * @request - MEMBER_ID member to be added to the collection
     * @response - STATUS: API call status
     */
    void addMember(Request request);

    /**
     * API to remove a member from a collection. If the collection is a dynamic
     * set and if the given member fulfill the set criteria, this API returns an
     * error.
     * 
     * @request - GRAPH_ID as request context variable.
     * @request - COLLECTION_ID Id of the collection
     * @request - COLLECTION_TYPE collection type
     * @request - MEMBER_ID member to be removed from the collection
     * @response - STATUS: API call status
     */
    void removeMember(Request request);

    /**
     * API to remove a collection from the graph. All the members of the
     * collection still remain in the graph even after the collection is
     * deleted.
     * 
     * @request - GRAPH_ID as request context variable.
     * @request - COLLECTION_ID Id of the collection to be removed
     * @request - COLLECTION_TYPE collection type
     * @response - STATUS: API call status
     */
    void dropCollection(Request request);

    /**
     * API to get all members of a collection.
     * 
     * @request - GRAPH_ID as request context variable.
     * @request - COLLECTION_ID Id of the collection to be removed
     * @request - COLLECTION_TYPE collection type
     * @response - MEMBERS: List of members
     * @response - STATUS: API call status
     */
    void getCollectionMembers(Request request);

    /**
     * API to add a tag to a node or a node property. If PROPERTY is specified,
     * then the tag is created on the property.
     * 
     * @request - GRAPH_ID as request context variable.
     * @request - TAG_NAME name of the tag to be created
     * @request - NODE_ID id of the node to be tagged
     * @request - PROPERTY name of the property to be tagged
     * @response - STATUS: API call status
     */
    void addTag(Request request);

    /**
     * API to add multiple tags to a node.
     * 
     * @request - GRAPH_ID as request context variable.
     * @request - TAGS list of tags to be added
     * @request - NODE_ID id of the node to be tagged
     * @response - STATUS: API call status
     */
    void addTags(Request request);

}
