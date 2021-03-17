package org.sunbird.jobs.samza.test;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.samza.config.Config;
import org.apache.samza.metrics.Counter;
import org.apache.samza.metrics.MetricsRegistry;
import org.apache.samza.system.IncomingMessageEnvelope;
import org.apache.samza.system.SystemStreamPartition;
import org.apache.samza.task.MessageCollector;
import org.apache.samza.task.TaskContext;
import org.apache.samza.task.TaskCoordinator;
import org.sunbird.jobs.samza.service.CompositeSearchIndexerService;
import org.sunbird.jobs.samza.service.util.CompositeSearchIndexer;
import org.sunbird.jobs.samza.service.util.DialCodeIndexer;
import org.sunbird.jobs.samza.service.util.DialCodeMetricsIndexer;
import org.sunbird.jobs.samza.task.CompositeSearchIndexerTask;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.Map;

import static org.junit.Assert.assertEquals;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class ESIndexerTest {

    private MessageCollector collectorMock;
    private TaskContext contextMock;
    private MetricsRegistry metricsRegistry;
    private Counter counter;
    private TaskCoordinator coordinatorMock;
    private IncomingMessageEnvelope envelopeMock;
    private SystemStreamPartition streamMock;
    private Config configMock;
    private CompositeSearchIndexerTask compositeSearchIndexerTask;
    private CompositeSearchIndexerService compositeSearchIndexerService;
    private CompositeSearchIndexer csIndexerMock;
    private DialCodeIndexer dcIndexerMock;
    private DialCodeMetricsIndexer dialCodeMetricsIndexerMock;

    private String dialcodeMetricsValidEvent = "{\"ets\":1543561000015,\"nodeUniqueId\":\"QR1234\",\"transactionData\":{\"properties\":{\"average_scans_per_day\":{\"nv\":2},\"last_scan\":{\"nv\":1541456052000},\"total_dial_scans_global\":{\"nv\":25},\"total_dial_scans_local\":{\"nv\":25},\"first_scan\":{\"nv\":1540469152000}}},\"objectType\":\"\",\"operationType\":\"UPDATE\",\"nodeType\":\"DIALCODE_METRICS\",\"graphId\":\"domain\",\"nodeGraphId\":0}";

    @Before
    public void setUp() throws Exception {

        collectorMock = Mockito.mock(MessageCollector.class);
        contextMock = Mockito.mock(TaskContext.class);
        metricsRegistry = Mockito.mock(MetricsRegistry.class);
        counter = Mockito.mock(Counter.class);
        coordinatorMock = Mockito.mock(TaskCoordinator.class);
        envelopeMock = mock(IncomingMessageEnvelope.class);
        configMock = Mockito.mock(Config.class);

        dialCodeMetricsIndexerMock = Mockito.mock(DialCodeMetricsIndexer.class);
        streamMock = Mockito.mock(SystemStreamPartition.class);

        stub(envelopeMock.getSystemStreamPartition()).toReturn(streamMock);
        stub(metricsRegistry.newCounter(anyString(), anyString())).toReturn(counter);
        stub(contextMock.getMetricsRegistry()).toReturn(metricsRegistry);
        stub(streamMock.getStream()).toReturn("telemetry.with_location");
        stub(configMock.get("task.window.ms")).toReturn("10");
        stub(configMock.get("definitions.update.window.ms")).toReturn("20");

        compositeSearchIndexerService = Mockito.spy(new CompositeSearchIndexerService(csIndexerMock, dcIndexerMock, dialCodeMetricsIndexerMock));
        doNothing().when(compositeSearchIndexerService).initialize(configMock);
        compositeSearchIndexerTask = new CompositeSearchIndexerTask(configMock, contextMock, compositeSearchIndexerService);

    }

    @Test
    @SuppressWarnings("unchecked")
    public void testDialcodeMetricsEventIndexer() throws Exception {
        stub(envelopeMock.getMessage()).toReturn(getEvent(dialcodeMetricsValidEvent));
        ArgumentCaptor<String> stringArgument = ArgumentCaptor.forClass(String.class);
        Class<Map<String, Object>> mapClass = (Class<Map<String, Object>>)(Class) Map.class;
        ArgumentCaptor<Map<String, Object>> mapArgument = ArgumentCaptor.forClass(mapClass);
        compositeSearchIndexerTask.process(envelopeMock, collectorMock, coordinatorMock);
        verify(dialCodeMetricsIndexerMock, times(1))
                .upsertDocument(stringArgument.capture(), mapArgument.capture());
        assertEquals("QR1234", stringArgument.getValue());
        Map<String, Object> message = mapArgument.getValue();
        assertEquals("UPDATE", message.get("operationType"));
        assertEquals("DIALCODE_METRICS", message.get("nodeType"));
    }

    public static Map<String, Object> getEvent(String message) {
        return new Gson().fromJson(message, new TypeToken<Map<String, Object>>() {}.getType());
    }
}
