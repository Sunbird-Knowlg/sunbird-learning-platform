package org.ekstep.taxonomy.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ekstep.common.enums.TaxonomyErrorCodes;
import org.ekstep.common.exception.ServerException;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

public class JsonUtils {

    private static ObjectMapper mapper = new ObjectMapper();

    public static <T> T toObject(Object o, Class<T> clazz) {
        return mapper.convertValue(o, clazz);
    }

    public static <T, R> List<R> toListObject(List<T> l, Class<R> clazz) {
        return l.
                stream().
                filter(Objects::nonNull).
                map(m -> toObject(m, clazz)).
                collect(toList());
    }

    public static <T> Optional<Object> mapToObject(Object o, Class<T> clazz) {
        return Optional.ofNullable(o).
                map(a -> {
                    if (a instanceof List) {
                        List l = (List) a;
                        if (!l.isEmpty()) {
                            return Optional.ofNullable(l.get(0)).
                                    map(e -> {
                                        if (e instanceof Map) {
                                            List<Map> lm = (List<Map>) l;
                                            return JsonUtils.<Map, T>toListObject(lm, clazz);
                                        }
                                        return null;
                                    }).
                                    filter(list -> !list.isEmpty()).
                                    orElseGet(null);
                        }
                    }
                    return null;
                });
    }

    public static <T> List<T> mapToListObject(Object obj, Class<T> clazz,
                                          Function<List<T>, List<T>> c) {
        return mapToObject(obj, clazz).
                map(o -> (List<T>)o).
                map(e -> c.apply(e)).
                orElse(null);
    }

    public static <T> T deepCopy(T o, Class<T> clazz) {
        try {
            return mapper.readValue(mapper.writeValueAsString(o), clazz);
        } catch (IOException e) {
            e.printStackTrace();
            throw new ServerException(TaxonomyErrorCodes.SYSTEM_ERROR.name(),
                    "Something went wrong while processing the request");
        }
    }

    public static String serialize(Object o) {
        try {
            return mapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new ServerException(TaxonomyErrorCodes.SYSTEM_ERROR.name(),
                    "Something went wrong while processing the request");
        }
    }

}
