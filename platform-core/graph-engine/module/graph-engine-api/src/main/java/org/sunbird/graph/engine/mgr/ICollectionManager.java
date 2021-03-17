package org.sunbird.graph.engine.mgr;

import org.sunbird.common.dto.Request;

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
    
    void getSet(Request request);
    
    void updateSet(Request request);
    
    void getSetCardinality(Request request);

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

	void addMembers(Request request);

}
