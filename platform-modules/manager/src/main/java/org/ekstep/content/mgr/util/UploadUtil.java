package org.ekstep.content.mgr.util;

import org.ekstep.common.Platform;
import org.ekstep.common.dto.Response;
import org.ekstep.content.mgr.impl.UpdateManager;
import org.ekstep.graph.service.common.DACConfigurationConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class UploadUtil {

    @Autowired private UpdateManager updateManager;

    public Response updateMimeType(String contentId, String mimeType) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("mimeType", mimeType);
        map.put("versionKey", Platform.config.getString(DACConfigurationConstants.PASSPORT_KEY_BASE_PROPERTY));
        return this.updateManager.update(contentId, map);
    }

}
