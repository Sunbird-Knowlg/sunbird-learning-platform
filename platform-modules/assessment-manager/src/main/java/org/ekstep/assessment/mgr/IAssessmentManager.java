package org.ekstep.assessment.mgr;

import org.ekstep.assessment.enums.AssessmentAPIParams;
import org.ekstep.common.dto.Request;
import org.ekstep.common.dto.Response;
import org.ekstep.graph.dac.enums.GraphDACParams;


/**
 * AssessmentManager provides Service API to Manage Assessment data - Assessment
 * Items (Questions), Assessment Item Sets, Questionnaire.
 * 
 * @author ravitejagarlapati
 * 
 */

public interface IAssessmentManager {   

    /**
     * Saves AssessmentItem. If identifier is not provided, it will be
     * generated. The identifier of the saved AssessmentItem is returned in
     * response.
     * 
     * 
     * @request {@link AssessmentAPIParams}.assessment_item AssessmentItem -
     *          mandatory
     * 
     * @response {@link GraphDACParams}.node_id String -
     *           Identifier of the saved AssessmentItem. This will be new id if
     *           identifier not specified.
     *           
     */
    public Response createAssessmentItem(String taxonomyId, Request request);
    
    /**
     * Updates AssessmentItem. The identifier of the updated AssessmentItem is returned in
     * response.
     * 
     * @id      AssessmentItem identifier - mandatory
     * @request {@link AssessmentAPIParams}.assessment_item AssessmentItem -
     *          mandatory
     * 
     * @response {@link GraphDACParams}.node_id String -
     *           Identifier of the saved AssessmentItem. This will be new id if
     *           identifier not specified.
     *           
     */
    public Response updateAssessmentItem(String id, String taxonomyId, Request request);
    
    /**
     * Gets AssessmentItem for the identifier
     * 
     * @id       AssessmentItem identifier - mandatory
     * @ifields  AssessmentItems fields to return
     * @response {@link AssessmentAPIParams.assessment_item} - AssessmentItem
     *           
     */
    public Response getAssessmentItem(String id, String taxonomyId, String[] ifields, String[] fields);
    
    /**
     * Searches Assessment Items based on SearchCriteria and the response would
     * have lists of Assessment Items (Partial objects of type AssessmentItemListDTO are returned). SearchCriteria can use any attribute of
     * AssessmentItem. Pagination, Sort information can be provided in SearchCriteria.
     * 
     * @see com.canopus.perceptron.dac.mulgara.dto.SearchCriteria
     * 
     * @request AssessmentManagerParams.SEARCH_CRITERIA - SearchCriteria -
     *          mandatory
     * @response AssessmentManagerParams.ASSESSMENT_ITEM_LIST -
     *           <code>BaseValueObjectList<AssessmentItemListDTO></code>
     * 
     */
    public Response searchAssessmentItems(String taxonomyId, Request request);
    
    /**
     * Deletes the AssessmentItem for given id.
     * 
     * @id       AssessmentItem identifier - mandatory - String
     * @response None
     * 
     */
    public Response deleteAssessmentItem(String id, String taxonomyId);
    
    // Assessment Item Set - START

    /**
     * Creates AssessmentItem Set. If identifier is not provided, it will be
     * generated. The identifier of the saved AssessmentItem is returned in
     * response.
     * 
     * @see org.ekstep.graph.model.collection.Set
     * 
     * @request {@link AssessmentAPIParams}.item_set Set -
     *          mandatory
     * @response AssessmentManagerParams.item_set_id String -
     *           Identifier of the created Object. This will be new id if
     *           input does not have identifier specified.
     */
    public Response createItemSet(String taxonomyId, Request request);
    
    /**
     * Saves AssessmentItem Set. If identifier is not provided, it will be
     * generated. The identifier of the saved AssessmentItem is returned in
     * response.
     * 
     * @see com.canopus.perceptron.dac.mulgara.dto.Set
     * 
     * @request AssessmentManagerParams.ITEM_SET Set -
     *          mandatory
     * @response AssessmentManagerParams.ITEM_SET_ID StringValue -
     *           Identifier of the saved Object. This will be new id if
     *           input does not have identifier specified.
     */
    public Response updateItemSet(String id, String taxonomyId, Request request);

    /**
     * Gets Assessment Item Set for the set identifier
     * 
     * @see com.canopus.perceptron.dac.mulgara.dto.Set
     * 
     * @request AssessmentManagerParams.ITEM_SET_ID - StringValue - 
     *          mandatory
     * @response AssessmentManagerParams.ITEM_SET - Set 
     *           
     */
    public Response getItemSet(String id, String taxonomyId, String[] isfields, boolean expandItems);
    
    /**
     * 
     * 
     */
    public Response searchItemSets(String taxonomyId, Request request);

    /**
     * Deletes the Assessment Item Sets for given list of ids.
     * 
     * @request AssessmentManagerParams.ITEM_SET_IDS - StringValueList -
     *          mandatory
     * @response None
     * 
     */
    public Response deleteItemSet(String id, String taxonomyId);

    // Assessment Item Set - END

}
