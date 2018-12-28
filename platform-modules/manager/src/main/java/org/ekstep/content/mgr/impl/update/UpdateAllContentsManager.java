package org.ekstep.content.mgr.impl.update;

import org.ekstep.common.dto.Response;
import org.ekstep.taxonomy.mgr.impl.DummyBaseContentManager;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class UpdateAllContentsManager extends DummyBaseContentManager {

    public Response updateAllContents(String originalId, Map<String, Object> map) throws Exception {
        return super.updateAllContents(originalId, map);
    }

}
