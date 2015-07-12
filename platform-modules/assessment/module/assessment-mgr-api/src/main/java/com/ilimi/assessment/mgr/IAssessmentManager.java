package com.ilimi.assessment.mgr;

import com.ilimi.common.dto.Request;
import com.ilimi.common.dto.Response;

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
     * Some fields like qtiXML, media... will be ignored and the values will be
     * regenerated during save.
     * 
     * @request AssessmentManagerParams.ASSESSMENT_ITEM AssessmentItem -
     *          mandatory
     * @request AssessmentManagerParams.QUESTION_SUBTYPE StringValue - mandatory
     * @request AssessmentManagerParams.COURSE_ID StringValue - mandatory
     * @response AssessmentManagerParams.ASSESSMENT_ITEM_ID StringValue -
     *           Identifier of the saved AssessmentItem. This will be new id if
     *           ASSESSMENT_ITEM does not have identifier specified.
     */
    public Response createAssessmentItem(String taxonomyId, Request request);
    
    public Response updateAssessmentItem(String id, String taxonomyId, Request request);
    
    /**
     * Gets AssessmentItem for the identifier
     * 
     * @request AssessmentManagerParams.ASSESSMENT_ITEM_ID - StringValue - AssessmentItem identifier-
     *          mandatory
     * @response AssessmentManagerParams.ASSESSMENT_ITEM - AssessmentItem 
     *           
     */
    public Response getAssessmentItem(String id, String taxonomyId, String[] ifields);
    
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
    public Response searchAssessmentItems(Request request);
    
    /**
     * Deletes the AssessmentItem for given id.
     * 
     * @request AssessmentManagerParams.ASSESSMENT_ITEM_ID - StringValue -
     *          mandatory
     * @response None
     * 
     */
    public Response deleteAssessmentItem(String id, String taxonomyId);
}
