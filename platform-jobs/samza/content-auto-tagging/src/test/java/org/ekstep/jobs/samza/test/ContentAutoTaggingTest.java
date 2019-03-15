package org.ekstep.jobs.samza.test;

import org.apache.samza.config.Config;
import org.apache.samza.metrics.Counter;
import org.apache.samza.metrics.MetricsRegistry;
import org.apache.samza.system.IncomingMessageEnvelope;
import org.apache.samza.task.MessageCollector;
import org.apache.samza.task.TaskContext;
import org.apache.samza.task.TaskCoordinator;
import org.ekstep.jobs.samza.service.ContentAutoTaggingService;
import org.ekstep.jobs.samza.task.ContentAutoTaggingTask;
import org.junit.Before;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class ContentAutoTaggingTest {

    private MessageCollector collectorMock;
    private TaskContext contextMock;
    private MetricsRegistry metricsRegistry;
    private Counter counter;
    private TaskCoordinator coordinatorMock;
    private IncomingMessageEnvelope envelopeMock;
    private Config configMock;

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
        // streamMock = Mockito.mock(SystemStreamPartition.class);

        stub(metricsRegistry.newCounter(anyString(), anyString())).toReturn(counter);
        stub(contextMock.getMetricsRegistry()).toReturn(metricsRegistry);

        // stub(envelopeMock.getSystemStreamPartition()).toReturn(streamMock);
        stub(metricsRegistry.newCounter(anyString(), anyString())).toReturn(counter);
        stub(contextMock.getMetricsRegistry()).toReturn(metricsRegistry);

        contentAutoTaggingService = mock(ContentAutoTaggingService.class);
        contentAutoTaggingTask = new ContentAutoTaggingTask(configMock, contextMock, contentAutoTaggingService);

    }

}
