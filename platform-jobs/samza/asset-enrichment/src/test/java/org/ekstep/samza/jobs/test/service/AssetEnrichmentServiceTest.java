package org.ekstep.samza.jobs.test.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import org.apache.samza.config.Config;
import org.apache.samza.metrics.Counter;
import org.apache.samza.metrics.MetricsRegistry;
import org.apache.samza.system.IncomingMessageEnvelope;
import org.apache.samza.system.SystemStreamPartition;
import org.apache.samza.task.MessageCollector;
import org.apache.samza.task.TaskContext;
import org.apache.samza.task.TaskCoordinator;
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
    VideoStreamingJobRequest jobRequest;
    ControllerUtil controllerUtil;
    AssetEnrichmentTask assetEnrichmentTask;
    private static final String SUCCESS_TOPIC = "telemetry.with_location";
    private static final String FAILED_TOPIC = "telemetry.failed";
    private static final String MALFORMED_TOPIC = "telemetry.malformed";


    @BeforeClass
    public static void initialize() throws InterruptedException {
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
        jobRequest = new VideoStreamingJobRequest();
        controllerUtil = new ControllerUtil();

        stub(configMock.get("output.success.topic.name", SUCCESS_TOPIC)).toReturn(SUCCESS_TOPIC);
        stub(configMock.get("output.failed.topic.name", FAILED_TOPIC)).toReturn(FAILED_TOPIC);
        stub(configMock.get("output.malformed.topic.name", MALFORMED_TOPIC)).toReturn(MALFORMED_TOPIC);
        stub(configMock.get("channel.default","in.ekstep")).toReturn("in.ekstep");
        stub(configMock.get("cassandra.lp.connection","localhost:9042")).toReturn("localhost:9042");

        stub(envelopeMock.getSystemStreamPartition()).toReturn(streamMock);
        stub(metricsRegistry.newCounter(anyString(), anyString())).toReturn(counter);
        stub(contextMock.getMetricsRegistry()).toReturn(metricsRegistry);
        stub(streamMock.getStream()).toReturn("telemetry.with_location");
        stub(configMock.get("task.window.ms")).toReturn("10");
        stub(configMock.get("definitions.update.window.ms")).toReturn("20");

        assetEnrichmentService = Mockito.spy(new AssetEnrichmentService(controllerUtil,jobRequest));
        doNothing().when(assetEnrichmentService).initialize(configMock);
        assetEnrichmentTask = new AssetEnrichmentTask(configMock, contextMock);

    }

    @After
    public void tearDown() throws Exception {

    }

    @AfterClass
    public static void destroy() throws InterruptedException {

    }

    @Test
    public void pushStreamingUrlSuccessTest() {
        stub(envelopeMock.getMessage()).toReturn(MessageEvents.getMap(MessageEvents.SUCCESS_UPLOAD_STREAMURL_EVENT));
        try{
            assetEnrichmentTask.process(envelopeMock, collectorMock, coordinatorMock);
        } catch (Exception e){
            e.printStackTrace();
        }


    }

    @Test
    public void pushStreamUrlSuccess() {
        Node expectedNode = controllerUtil.getNode("domain","do_112732688163201024117");
        String videoUrl = (String) expectedNode.getMetadata().get(AssetEnrichmentEnums.artifactUrl.name());
        assetEnrichmentService.pushStreamingUrlRequest(expectedNode, videoUrl);
    }


}
