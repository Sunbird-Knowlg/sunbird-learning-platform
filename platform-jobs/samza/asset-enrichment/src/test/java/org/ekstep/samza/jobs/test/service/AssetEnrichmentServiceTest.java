package org.ekstep.samza.jobs.test.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import org.apache.samza.config.Config;
import org.apache.samza.metrics.Counter;
import org.apache.samza.metrics.MetricsRegistry;
import org.apache.samza.system.IncomingMessageEnvelope;
import org.apache.samza.system.SystemStreamPartition;
import org.apache.samza.task.MessageCollector;
import org.apache.samza.task.TaskContext;
import org.apache.samza.task.TaskCoordinator;
import org.ekstep.cassandra.connector.util.CassandraConnector;
import org.ekstep.graph.dac.model.Node;
import org.ekstep.jobs.samza.service.AssetEnrichmentService;
import org.ekstep.jobs.samza.task.AssetEnrichmentTask;
import org.ekstep.jobs.samza.util.AssetEnrichmentEnums;
import org.ekstep.learning.contentstore.VideoStreamingJobRequest;
import org.ekstep.learning.util.ControllerUtil;
import org.junit.*;
import org.mockito.Mockito;

public class AssetEnrichmentServiceTest {
    private MessageCollector collectorMock;
    private TaskContext contextMock;
    private MetricsRegistry metricsRegistry;
    private Counter counter;
    private TaskCoordinator coordinatorMock;
    private IncomingMessageEnvelope envelopeMock;
    private Config configMock;
    private SystemStreamPartition streamMock;
    private AssetEnrichmentService assetEnrichmentServiceMock;
    private AssetEnrichmentService assetEnrichmentService;
    private VideoStreamingJobRequest jobRequest;
    private ControllerUtil controllerUtil;
    private AssetEnrichmentTask assetEnrichmentTask;

    private static Session session;


    @BeforeClass
    public static void initialize() throws InterruptedException {
        session = CassandraConnector.getSession("lp");
    }

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws Exception {
        collectorMock = Mockito.mock(MessageCollector.class);
        contextMock = Mockito.mock(TaskContext.class);
        metricsRegistry = Mockito.mock(org.apache.samza.metrics.MetricsRegistry.class);
        counter = Mockito.mock(org.apache.samza.metrics.Counter.class);
        coordinatorMock = Mockito.mock(TaskCoordinator.class);
        envelopeMock = mock(IncomingMessageEnvelope.class);
        configMock = Mockito.mock(Config.class);
        streamMock = mock(SystemStreamPartition.class);
        jobRequest = mock(VideoStreamingJobRequest.class);
        controllerUtil = new ControllerUtil();

        stub(envelopeMock.getSystemStreamPartition()).toReturn(streamMock);
        stub(metricsRegistry.newCounter(anyString(), anyString())).toReturn(counter);
        stub(contextMock.getMetricsRegistry()).toReturn(metricsRegistry);
        stub(streamMock.getStream()).toReturn("telemetry.with_location");
        stub(configMock.get("task.window.ms")).toReturn("10");
        stub(configMock.get("definitions.update.window.ms")).toReturn("20");

        assetEnrichmentService = Mockito.spy(new AssetEnrichmentService(controllerUtil, jobRequest));
        doNothing().when(assetEnrichmentService).initialize(configMock);
        assetEnrichmentTask = new AssetEnrichmentTask(configMock, contextMock);

    }

    @After
    public void tearDown() throws Exception {

    }

    @AfterClass
    public static void destroy() throws InterruptedException {

    }

    @Ignore
    @Test
    public void pushStreamUrlSuccess() {
        Node expectedNode = controllerUtil.getNode("domain", "do_112732688163201024117");
        assertEquals(expectedNode.getMetadata().get("status"), "Live");
        String videoUrl = (String) expectedNode.getMetadata().get(AssetEnrichmentEnums.artifactUrl.name());
        assetEnrichmentService.pushStreamingUrlRequest(expectedNode, videoUrl);
        ResultSet result = session.execute("SELECT * FROM platform_db.job_request WHERE request_id = '" + expectedNode.getIdentifier() + "_1.0' ALLOW FILTERING;");
        Row row = result.one();
        assertNotNull(row);
        assertEquals(row.get("location", String.class), (String) expectedNode.getMetadata().get("artifactUrl"));
    }

    @Ignore
    @Test
    public void pushStreamUrlFailure() {
        Node expectedNode = controllerUtil.getNode("domain", "do_11273262441237708812");
        assertEquals(expectedNode.getMetadata().get("status"), "Processing");
        String videoUrl = (String) expectedNode.getMetadata().get(AssetEnrichmentEnums.artifactUrl.name());
        assetEnrichmentService.pushStreamingUrlRequest(expectedNode, videoUrl);
        ResultSet result = session.execute("SELECT * FROM platform_db.job_request WHERE request_id = '" + expectedNode.getIdentifier() + "_1.0' ALLOW FILTERING;");
        assertNull(result.one());

    }


}
