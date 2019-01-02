package org.ekstep.taxonomy.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.stream.Collectors.toList;

public class TaxonomyUtil {

    public static <T> Map<T, Integer> getPositionMap(List<T> l) {
        Map<T, Integer> m = new HashMap<>();
        int index = 0;
        for (T e : l) { m.put(e, index++); }
        return m;
    }

    public static <T, R> List<R> convertListAndGetNew(List<T> l) {
        return l.
                stream().
                filter(Objects::nonNull).
                map(e -> (R) e).
                collect(toList());
    }

}
