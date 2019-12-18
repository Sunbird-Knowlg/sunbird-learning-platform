package org.ekstep.assessment.dto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;
import org.ekstep.common.dto.AbstractSearchCriteria;
import org.ekstep.graph.dac.model.Filter;
import org.ekstep.graph.dac.model.MetadataCriterion;
import org.ekstep.graph.dac.model.RelationCriterion;
import org.ekstep.graph.dac.model.SearchCriteria;

public class ItemSearchCriteria extends AbstractSearchCriteria {

    @Override
    public SearchCriteria getSearchCriteria() {
        SearchCriteria sc = getSearchCriteria("AssessmentItem");
        return sc;
    }
    
    public static void main(String args[]) throws Exception {
        ItemSearchCriteria criteria = new ItemSearchCriteria();
        List<Filter> filters = new ArrayList<Filter>();
        filters.add(new Filter("prop1", "value1"));
        filters.add(new Filter("prop2", "value2"));
        MetadataCriterion metadata = MetadataCriterion.create(filters);
        metadata.addFilter(new Filter("prop5", "value5"));
        MetadataCriterion metaList1 = MetadataCriterion.create(Arrays.asList(new Filter("prop3", "value3")));
        metadata.addMetadata(metaList1);
        MetadataCriterion metaList2 = MetadataCriterion.create(Arrays.asList(new Filter("prop4", "value4")));
        metadata.addMetadata(metaList2);
        criteria.setMetadata(metadata);
        
        RelationCriterion rc1 = new RelationCriterion("associatedTo", "Concept");
        rc1.addMetadata(MetadataCriterion.create(Arrays.asList(new Filter("name", "valueX"))));
        RelationCriterion rc11 = new RelationCriterion("associatedTo", "Game");
        criteria.setRelations(Arrays.asList(rc1, rc11));
        
        ObjectMapper mapper = new ObjectMapper();
        String str = mapper.writeValueAsString(criteria);
        System.out.println(str);
        System.out.println(criteria.getSearchCriteria().getQuery());
        System.out.println("============================");
//        String str1 = "{\"metadata\":{\"filters\":[{\"property\":\"prop1\",\"value\":\"value1\",\"operator\":\"=\"},{\"property\":\"prop2\",\"value\":\"value2\",\"operator\":\"=\"},{\"property\":\"prop5\",\"value\":\"value5\",\"operator\":\"=\"}],\"op\":\"AND\",\"metadata\":[{\"filters\":[{\"property\":\"prop3\",\"value\":\"value3\",\"operator\":\"=\"}],\"op\":\"AND\"},{\"filters\":[{\"property\":\"prop4\",\"value\":\"value4\",\"operator\":\"=\"}],\"op\":\"AND\"}]},\"relations\":[{\"name\":\"associatedTo\",\"objectType\":\"Concept\",\"op\":\"AND\",\"optional\":false},{\"name\":\"associatedTo\",\"objectType\":\"Game\",\"op\":\"AND\",\"optional\":false}]}";
        String str1 = "{\"metadata\":{\"filters\":[{\"property\":\"prop1\",\"value\":\"value1\",\"operator\":\"=\"},{\"property\":\"prop2\",\"value\":\"value2\",\"operator\":\"=\"},{\"property\":\"prop5\",\"value\":\"value5\",\"operator\":\"=\"}],\"op\":\"AND\",\"metadata\":[{\"filters\":[{\"property\":\"prop3\",\"value\":\"value3\",\"operator\":\"=\"}],\"op\":\"AND\"},{\"filters\":[{\"property\":\"prop4\",\"value\":\"value4\",\"operator\":\"=\"}],\"op\":\"AND\"}]},\"relations\":[{\"name\":\"\",\"objectType\":\"Concept\",\"op\":\"AND\",\"optional\":false},{\"name\":\"associatedTo\",\"objectType\":\"Game\",\"op\":\"AND\",\"optional\":false}]}";
        ItemSearchCriteria c = mapper.readValue(str1, ItemSearchCriteria.class);
        System.out.println(c);
        System.out.println(c.getSearchCriteria().getQuery());
        System.out.println(new ItemSearchCriteria().getSearchCriteria().getQuery());
    }
}
