package org.eksep.samza.jobs.test;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.samza.metrics.Counter;
import org.apache.samza.metrics.Metric;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stub for Consumer Metric Stream
 *
 * @author Kumar Gauraw
 */
public class MetricsStreamStub {

    public static final String METRIC_STREAM_NO_EVENT = "{\n" +
            "  \"org.apache.samza.system.kafka.KafkaSystemConsumerMetrics\": {\n" +
            "    \"kafka-test.topic-0-high-watermark\": {\n" +
            "      \"name\": \"kafka-test.topic-0-high-watermark\",\n" +
            "      \"count\": {\n" +
            "        \"value\": 0\n" +
            "      }\n" +
            "    }\n" +
            "  },\n" +
            "  \"org.apache.samza.checkpoint.OffsetManagerMetrics\": {\n" +
            "    \"kafka-test.topic-0-checkpointed-offset\": {\n" +
            "      \"name\": \"kafka-test.topic-0-checkpointed-offset\",\n" +
            "      \"count\": {\n" +
            "        \"value\": 0\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}";

    public static final String METRIC_STREAM_SOME_EVENT_MULTI_PARTITION = "{\n" +
            "  \"org.apache.samza.system.kafka.KafkaSystemConsumerMetrics\": {\n" +
            "    \"kafka-topic1-0-high-watermark\": {\n" +
            "      \"name\": \"kafka-topic1-0-high-watermark\",\n" +
            "      \"count\": {\n" +
            "        \"value\": 10\n" +
            "      }\n" +
            "    },\n" +
            "    \"kafka-topic2-0-high-watermark\": {\n" +
            "      \"name\": \"kafka-topic2-0-high-watermark\",\n" +
            "      \"count\": {\n" +
            "        \"value\": 100\n" +
            "      }\n" +
            "    }\n" +
            "  },\n" +
            "  \"org.apache.samza.checkpoint.OffsetManagerMetrics\": {\n" +
            "    \"kafka-topic1-0-checkpointed-offset\": {\n" +
            "      \"name\": \"kafka-topic1-0-checkpointed-offset\",\n" +
            "      \"count\": {\n" +
            "        \"value\": 5\n" +
            "      }\n" +
            "    },\n" +
            "    \"kafka-topic2-0-checkpointed-offset\": {\n" +
            "      \"name\": \"kafka-topic2-0-checkpointed-offset\",\n" +
            "      \"count\": {\n" +
            "        \"value\": 50\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}";

    public static final String SAMZA_EVENT_STREAM_WITH_SYSTEM_COMMAND = "{\n" +
            "  \"org.apache.samza.system.kafka.KafkaSystemConsumerMetrics\": {\n" +
            "    \"kafka-system.command-0-high-watermark\": {\n" +
            "      \"name\": \"kafka-system.command-0-high-watermark\",\n" +
            "      \"count\": {\n" +
            "        \"value\": 100\n" +
            "      }\n" +
            "    },\n" +
            "    \"kafka-learning.job.request-0-high-watermark\": {\n" +
            "      \"name\": \"kafka-learning.job.request-0-high-watermark\",\n" +
            "      \"count\": {\n" +
            "        \"value\": 200\n" +
            "      }\n" +
            "    }\n" +
            "  },\n" +
            "  \"org.apache.samza.checkpoint.OffsetManagerMetrics\": {\n" +
            "    \"kafka-system.command-0-checkpointed-offset\": {\n" +
            "      \"name\": \"kafka-system.command-0-checkpointed-offset\",\n" +
            "      \"count\": {\n" +
            "        \"value\": 90\n" +
            "      }\n" +
            "    },\n" +
            "    \"kafka-learning.job.request-0-checkpointed-offset\": {\n" +
            "      \"name\": \"kafka-learning.job.request-0-checkpointed-offset\",\n" +
            "      \"count\": {\n" +
            "        \"value\": 100\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}";

    public static final String METRIC_STREAM_SOME_EVENT_SINGLE_TOPIC_MULTI_PARTITION = "{\n" +
            "  \"org.apache.samza.system.kafka.KafkaSystemConsumerMetrics\": {\n" +
            "    \"kafka-topic1-0-high-watermark\": {\n" +
            "      \"name\": \"kafka-topic1-0-high-watermark\",\n" +
            "      \"count\": {\n" +
            "        \"value\": 10\n" +
            "      }\n" +
            "    },\n" +
            "    \"kafka-topic1-1-high-watermark\": {\n" +
            "      \"name\": \"kafka-topic1-1-high-watermark\",\n" +
            "      \"count\": {\n" +
            "        \"value\": 100\n" +
            "      }\n" +
            "    }\n" +
            "  },\n" +
            "  \"org.apache.samza.checkpoint.OffsetManagerMetrics\": {\n" +
            "    \"kafka-topic1-0-checkpointed-offset\": {\n" +
            "      \"name\": \"kafka-topic1-0-checkpointed-offset\",\n" +
            "      \"count\": {\n" +
            "        \"value\": 5\n" +
            "      }\n" +
            "    },\n" +
            "    \"kafka-topic1-1-checkpointed-offset\": {\n" +
            "      \"name\": \"kafka-topic1-1-checkpointed-offset\",\n" +
            "      \"count\": {\n" +
            "        \"value\": 50\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}";


    public static Map<String, ConcurrentHashMap<String, Metric>> getMetricMap(String message) {
        Type type = new TypeToken<Map<String, ConcurrentHashMap<String, Counter>>>() {
        }.getType();
        return (Map<String, ConcurrentHashMap<String, Metric>>) new Gson().fromJson(message, type);
    }

}
