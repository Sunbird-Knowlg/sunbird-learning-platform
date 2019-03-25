package org.ekstep.jobs.samza.task;

import com.google.gson.Gson;
import org.apache.samza.config.Config;
import org.apache.samza.metrics.Counter;
import org.apache.samza.metrics.MetricsRegistry;
import org.apache.samza.system.IncomingMessageEnvelope;
import org.apache.samza.task.MessageCollector;
import org.apache.samza.task.TaskContext;
import org.apache.samza.task.TaskCoordinator;
import org.ekstep.jobs.samza.fixtures.EventFixture;
import org.ekstep.jobs.samza.service.ContentAutoTaggingService;
import org.ekstep.jobs.samza.service.task.JobMetrics;
import org.ekstep.jobs.samza.util.DaggitAPIResponse;
import org.ekstep.jobs.samza.util.DaggitServiceClient;
import org.junit.Before;
import org.mockito.Mockito;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class ContentAutoTaggingTaskTest {

    private MessageCollector collectorMock;
    private TaskContext contextMock;
    private MetricsRegistry metricsRegistry;
    private Counter counter;
    private TaskCoordinator coordinatorMock;
    private IncomingMessageEnvelope envelopeMock;
    private Config configMock;
    private DaggitServiceClient apiClientMock;
    private ContentAutoTaggingTask contentAutoTaggingTask;
    private ContentAutoTaggingService contentAutoTaggingService;


    @Before
    public void setUp() throws Exception {

        collectorMock = mock(MessageCollector.class);
        contextMock = Mockito.mock(TaskContext.class);
        metricsRegistry = Mockito.mock(MetricsRegistry.class);
        counter = Mockito.mock(Counter.class);
        coordinatorMock = mock(TaskCoordinator.class);
        envelopeMock = mock(IncomingMessageEnvelope.class);
        configMock = Mockito.mock(Config.class);
        apiClientMock = Mockito.mock(DaggitServiceClient.class);
        // streamMock = Mockito.mock(SystemStreamPartition.class);

        // stub(envelopeMock.getSystemStreamPartition()).toReturn(streamMock);
        stub(metricsRegistry.newCounter(anyString(), anyString())).toReturn(counter);
        stub(contextMock.getMetricsRegistry()).toReturn(metricsRegistry);

        contentAutoTaggingService = mock(ContentAutoTaggingService.class);
        contentAutoTaggingTask = new ContentAutoTaggingTask(configMock, contextMock, apiClientMock, contentAutoTaggingService);

    }

    @Test
    public void shouldCallDaggitAPIForCreateEvent() throws Exception {
        stub(envelopeMock.getMessage()).toReturn(EventFixture.GRAPH_CREATE_EVENT);
        String response = "{\"id\":\"api.daggit.submit\",\"ver\":\"v1\",\"ts\":\"2019-03-11 06:40:12:316+0000\",\"params\":{\"resmsgid\":null,\"msgid\":\"\",\"err\":null,\"status\":\"success\",\"errmsg\":null},\"responseCode\":\"OK\",\"result\":{\"status\":200,\"experiment_name\":\"content_tagging20190314-013529\",\"estimate_time\":\"\"}}";
        stub(apiClientMock.submit("do_21272272665784320011044")).toReturn(new Gson().fromJson(response, DaggitAPIResponse.class));
        contentAutoTaggingTask.process(envelopeMock, collectorMock, coordinatorMock);
//        System.out.println(contentAutoTaggingTask.metrics.collect());
    }

    @Test
    public void shouldNotCallDaggitAPIForUpdateEvent() throws Exception {
        stub(envelopeMock.getMessage()).toReturn(EventFixture.GRAPH_UPDATE_EVENT);
        contentAutoTaggingTask.process(envelopeMock, collectorMock, coordinatorMock);
//        System.out.println(contentAutoTaggingTask.metrics.collect());
    }

}
