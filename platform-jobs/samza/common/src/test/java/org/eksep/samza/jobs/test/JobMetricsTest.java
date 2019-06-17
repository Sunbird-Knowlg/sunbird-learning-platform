package org.eksep.samza.jobs.test;

import org.apache.samza.Partition;
import org.apache.samza.metrics.Counter;
import org.apache.samza.metrics.Metric;
import org.apache.samza.metrics.MetricsRegistry;
import org.apache.samza.system.SystemStreamPartition;
import org.apache.samza.task.TaskContext;
import org.ekstep.jobs.samza.service.task.JobMetrics;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;
import static org.mockito.Mockito.when;
import static org.junit.Assert.assertEquals;

/**
 * JobMetrics Test for Consumer Lag Computation
 *
 * @author Kumar Gauraw
 */
public class JobMetricsTest  {

    private TaskContext contextMock;
    private JobMetrics jobMetricsMock;

    @Before
    public void setUp() {
        contextMock = mock(TaskContext.class);
        MetricsRegistry metricsRegistry = mock(MetricsRegistry.class);
        Counter counter = mock(Counter.class);
        stub(metricsRegistry.newCounter(anyString(), anyString())).toReturn(counter);
        stub(contextMock.getMetricsRegistry()).toReturn(metricsRegistry);
    }

    @Test
    public void testConsumerLagWithEventProcessed() {

        jobMetricsMock = new JobMetrics(contextMock);

        Set<SystemStreamPartition> systemStreamPartitions = new HashSet<>();
        SystemStreamPartition systemStreamTopic1 = new SystemStreamPartition("kafka", "topic1", new Partition(0));
        SystemStreamPartition systemStreamTopic2 = new SystemStreamPartition("kafka", "topic2", new Partition(0));
        systemStreamPartitions.add(systemStreamTopic1);
        systemStreamPartitions.add(systemStreamTopic2);

        jobMetricsMock.setOffset(systemStreamTopic1, "5");
        jobMetricsMock.setOffset(systemStreamTopic2, "10");

        Map<String, ConcurrentHashMap<String, Metric>> concurrentHashMap = MetricsStreamStub.getMetricMap(MetricsStreamStub.METRIC_STREAM_SOME_EVENT);

        when(contextMock.getSystemStreamPartitions()).thenReturn(systemStreamPartitions);
        long consumer_lag = jobMetricsMock.computeConsumerLag(concurrentHashMap);
        assertEquals(13, consumer_lag);

    }

    @Test
    public void testConsumerLagWithNoEventProcessed() {

        jobMetricsMock = new JobMetrics(contextMock);

        Set<SystemStreamPartition> systemStreamPartitions = new HashSet<>();
        SystemStreamPartition systemStreamPartition = new SystemStreamPartition("kafka", "test.topic", new Partition(0));
        systemStreamPartitions.add(systemStreamPartition);

        Map<String, ConcurrentHashMap<String, Metric>> concurrentHashMap = MetricsStreamStub.getMetricMap(MetricsStreamStub.METRIC_STREAM_NO_EVENT);

        when(contextMock.getSystemStreamPartitions()).thenReturn(systemStreamPartitions);
        long consumer_lag = jobMetricsMock.computeConsumerLag(concurrentHashMap);
        assertEquals(0, consumer_lag);

    }

    @Test
    public void testConsumerLagWithSystemCommandStream() {
        jobMetricsMock = new JobMetrics(contextMock);
        Set<SystemStreamPartition> systemStreamPartitions = new HashSet<>();

        SystemStreamPartition streamSysCommand = new SystemStreamPartition("kafka", "system.command", new Partition(0));
        SystemStreamPartition streamJobReq = new SystemStreamPartition("kafka", "learning.job.request", new Partition(0));
        systemStreamPartitions.add(streamSysCommand);
        systemStreamPartitions.add(streamJobReq);

        jobMetricsMock.setOffset(streamSysCommand, "3");
        jobMetricsMock.setOffset(streamJobReq, "8");

        Map<String, ConcurrentHashMap<String, Metric>> concurrentHashMap = MetricsStreamStub.getMetricMap(MetricsStreamStub.SAMZA_EVENT_STREAM_WITH_SYSTEM_COMMAND);

        when(contextMock.getSystemStreamPartitions()).thenReturn(systemStreamPartitions);
        long consumer_lag = jobMetricsMock.computeConsumerLag(concurrentHashMap);
        System.out.println("consumer_lag :"+consumer_lag);
        //Before Ignoring system.command stream
        //Assert.assertEquals(15, consumer_lag);

        //After Ignoring system.command stream
        assertEquals(11, consumer_lag);

    }
}
