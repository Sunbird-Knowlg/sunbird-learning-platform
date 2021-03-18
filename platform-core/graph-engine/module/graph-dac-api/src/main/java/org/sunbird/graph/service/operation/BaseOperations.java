package org.sunbird.graph.service.operation;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.common.dto.Property;

public class BaseOperations {
	
	protected boolean validateRequired(Object... objects) {
        boolean valid = true;
        for (Object baseValueObject : objects) {
            if (null == baseValueObject) {
                valid = false;
                break;
            }
            if (baseValueObject instanceof String) {
                if (StringUtils.isBlank((String) baseValueObject)) {
                    valid = false;
                    break;
                }
            }
            if (baseValueObject instanceof List<?>) {
                List<?> list = (List<?>) baseValueObject;
                if (null == list || list.isEmpty()) {
                    valid = false;
                    break;
                }
            }
            if (baseValueObject instanceof Map<?, ?>) {
                Map<?, ?> map = (Map<?, ?>) baseValueObject;
                if (null == map || map.isEmpty()) {
                    valid = false;
                    break;
                }
            }
            if (baseValueObject instanceof Property) {
                Property property = (Property) baseValueObject;
                if (StringUtils.isBlank(property.getPropertyName())
                        || (null == property.getPropertyValue() && null == property.getDateValue())) {
                    valid = false;
                    break;
                }
            }
        }
        return valid;
    }

}
