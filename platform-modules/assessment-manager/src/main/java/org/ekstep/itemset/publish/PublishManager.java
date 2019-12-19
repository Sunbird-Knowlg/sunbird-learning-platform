package org.ekstep.itemset.publish;

import org.apache.commons.collections.CollectionUtils;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.itemset.handler.QuestionPaperGenerator;
import org.ekstep.learning.util.ControllerUtil;

import java.util.List;

public class PublishManager {
    private static final ControllerUtil controllerUtil = new ControllerUtil();
    private static final String TAXONOMY_ID = "domain";

    public static String publish(List<Node> itemSets) {
        if (CollectionUtils.isNotEmpty(itemSets)) {
            Node itemSet = controllerUtil.getNode(TAXONOMY_ID, itemSets.get(0).getIdentifier());
            String previewUrl = QuestionPaperGenerator.generateQuestionPaper(itemSet);
            itemSet.getMetadata().put("previewUrl", previewUrl);
            controllerUtil.updateNode(itemSet);
            return previewUrl;
        }
        return null;
    }
}
