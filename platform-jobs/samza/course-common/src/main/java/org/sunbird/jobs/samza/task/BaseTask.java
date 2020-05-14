package org.sunbird.jobs.samza.task;

import org.apache.commons.lang3.StringUtils;
import org.apache.samza.config.Config;
import org.apache.samza.system.IncomingMessageEnvelope;
import org.apache.samza.system.OutgoingMessageEnvelope;
import org.apache.samza.system.SystemStream;
import org.apache.samza.task.InitableTask;
import org.apache.samza.task.MessageCollector;
import org.apache.samza.task.StreamTask;
import org.apache.samza.task.TaskContext;
import org.apache.samza.task.TaskCoordinator;
import org.apache.samza.task.WindowableTask;
import org.ekstep.common.Platform;
import org.ekstep.jobs.samza.service.ISamzaService;
import org.ekstep.jobs.samza.service.task.JobMetrics;
import org.ekstep.jobs.samza.util.SamzaCommonParams;
import org.ekstep.telemetry.TelemetryGenerator;
import org.ekstep.telemetry.TelemetryParams;
import org.ekstep.telemetry.handler.Level;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class BaseTask implements StreamTask, InitableTask, WindowableTask {
    protected JobMetrics metrics;
    protected Config config = null;
    protected String eventId = "";
    protected List<String> action = new ArrayList<>();
    protected String jobStartMessage = "";
    protected String jobEndMessage = "";
    protected String jobClass = "";

    protected static String mid = "LP."+ UUID.randomUUID();
    protected static String startJobEventId = "JOB_START";
    protected static String endJobEventId = "JOB_END";
    protected static int MAXITERTIONCOUNT= 2;

    @Override
    public void init(Config config, TaskContext context) throws Exception {
        metrics = new JobMetrics(context, config.get("output.metrics.job.name"), config.get("output.metrics.topic.name"));
        ISamzaService service = initialize();
        service.initialize(config);
        this.config = config;
        this.eventId = "BE_JOB_REQUEST";
    }

    public abstract ISamzaService initialize() throws Exception;

    protected int getMaxIterations() {
        if(Platform.config.hasPath("max.iteration.count.samza.job"))
            return Platform.config.getInt("max.iteration.count.samza.job");
        else
            return MAXITERTIONCOUNT;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void process(IncomingMessageEnvelope envelope, MessageCollector collector, TaskCoordinator coordinator) throws Exception {
        Map<String, Object> message = (Map<String, Object>) envelope.getMessage();
        Map<String, Object> execution = new HashMap<>();
        int maxIterations = getMaxIterations();
        String eid = (String) message.get(SamzaCommonParams.eid.name());
        Map<String, Object> edata = (Map<String, Object>) message.getOrDefault(SamzaCommonParams.edata.name(), new HashMap<String,Object>());
        if(StringUtils.equalsIgnoreCase(this.eventId, eid)) {
            String action = (String) edata.get(SamzaCommonParams.action.name());
            if(this.action.contains(action)) {
                int currentIteration = (int) edata.get(SamzaCommonParams.iteration.name());
                preProcess(message, collector, execution, maxIterations, currentIteration);
                process(message, collector, coordinator);
                postProcess(message, collector, execution, maxIterations, currentIteration);
            } else{
                //Throw exception has to be added.
            }
        } else {
            //Throw exception has to be added.
        }
    }

    public abstract void process(Map<String, Object> message, MessageCollector collector, TaskCoordinator coordinator) throws Exception;

    public void preProcess(Map<String, Object> message, MessageCollector collector, Map<String, Object> execution, int maxIterationCount, int iterationCount) {
        if (isInvalidMessage(message)) {
            String event = generateEvent(Level.ERROR.name(), "Samza job de-serialization error", message);
            collector.send(new OutgoingMessageEnvelope(new SystemStream(SamzaCommonParams.kafka.name(), this.config.get("kafka.topics.backend.telemetry")), event));
        }
        try {
            if(iterationCount <= maxIterationCount) {
                Map<String, Object> jobStartEvent = getJobEvent("JOBSTARTEVENT", message);

                execution.put(SamzaCommonParams.submitted_date.name(), (long)message.get(SamzaCommonParams.ets.name()));
                execution.put(SamzaCommonParams.processing_date.name(), (long)jobStartEvent.get(SamzaCommonParams.ets.name()));
                execution.put(SamzaCommonParams.latency.name(), (long)jobStartEvent.get(SamzaCommonParams.ets.name()) - (long)message.get(SamzaCommonParams.ets.name()));

                pushEvent(jobStartEvent, collector, this.config.get("kafka.topics.backend.telemetry"));
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }


    @SuppressWarnings("unchecked")
    public void postProcess(Map<String, Object> message, MessageCollector collector, Map<String, Object> execution, int maxIterationCount, int iterationCount) throws Exception {
        try {
            if(iterationCount <= maxIterationCount) {
                Map<String, Object> jobEndEvent = getJobEvent("JOBENDEVENT", message);

                execution.put(SamzaCommonParams.completed_date.name(), (long)jobEndEvent.get(SamzaCommonParams.ets.name()));
                execution.put(SamzaCommonParams.execution_time.name(), (long)jobEndEvent.get(SamzaCommonParams.ets.name()) - (long)execution.get(SamzaCommonParams.processing_date.name()));
                Map<String, Object> eks = (Map<String, Object>)((Map<String, Object>)jobEndEvent.get(SamzaCommonParams.edata.name())).get(SamzaCommonParams.eks.name());
                eks.put(SamzaCommonParams.execution.name(), execution);
                //addExecutionTime(jobEndEvent, execution); //Call to add execution time

                pushEvent(jobEndEvent, collector, this.config.get("kafka.topics.backend.telemetry"));
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void pushEvent(Map<String, Object> message, MessageCollector collector, String topicId) throws Exception {
        try {
            //TODO: Fix Event Template for "START" & "END" Event and enable below line for backend telemetry.
            //collector.send(new OutgoingMessageEnvelope(new SystemStream(SamzaCommonParams.kafka.name(), topicId), message));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getJobEvent(String jobEvendID, Map<String, Object> message){

        long unixTime = System.currentTimeMillis();
        Map<String, Object> jobEvent = new HashMap<>();

        jobEvent.put(SamzaCommonParams.ets.name(), unixTime);
        jobEvent.put(SamzaCommonParams.mid.name(), mid);

        Map<String, Object> edata = new HashMap<>();
        Map<String, Object> eks = new HashMap<>();
        eks.put(SamzaCommonParams.ets.name(), message.get(SamzaCommonParams.ets.name()));
        eks.put(SamzaCommonParams.action.name(), ((Map<String, Object>) message.get(SamzaCommonParams.edata.name())).get(SamzaCommonParams.action.name()));
        eks.put(SamzaCommonParams.iteration.name(), ((Map<String, Object>) message.get(SamzaCommonParams.edata.name())).get(SamzaCommonParams.iteration.name()));
        eks.put(SamzaCommonParams.status.name(), ((Map<String, Object>) message.get(SamzaCommonParams.edata.name())).get(SamzaCommonParams.status.name()));
        eks.put(SamzaCommonParams.reqid.name(), message.get(SamzaCommonParams.mid.name()));
        edata.put(SamzaCommonParams.eks.name(), eks);
        edata.put(SamzaCommonParams.level.name(), SamzaCommonParams.INFO.name());
        edata.put(SamzaCommonParams.jobclass.name(), this.jobClass);
        edata.put(SamzaCommonParams.object.name(), message.get("object"));


        if(StringUtils.equalsIgnoreCase(jobEvendID, "JOBSTARTEVENT")) {
            jobEvent.put(SamzaCommonParams.eid.name(), startJobEventId);
            edata.put(SamzaCommonParams.message.name(), this.jobStartMessage);
        }
        else if(StringUtils.equalsIgnoreCase(jobEvendID, "JOBENDEVENT")) {
            jobEvent.put(SamzaCommonParams.eid.name(), endJobEventId);
            edata.put(SamzaCommonParams.message.name(), this.jobEndMessage);
        }

        jobEvent.put(SamzaCommonParams.edata.name(), edata);
        return jobEvent;
    }

    protected boolean isInvalidMessage(Map<String, Object> message) {
        return (message == null || (null != message && message.containsKey("serde")
                && "error".equalsIgnoreCase((String) message.get("serde"))));
    }

    @Override
    public void window(MessageCollector collector, TaskCoordinator coordinator) throws Exception {
        Map<String, Object> event = metrics.collect();
        collector.send(new OutgoingMessageEnvelope(new SystemStream("kafka", metrics.getTopic()), event));
        metrics.clear();
    }

    private String generateEvent(String logLevel, String message, Map<String, Object> data) {
        Map<String, String> context = new HashMap<String, String>();
        context.put(TelemetryParams.ACTOR.name(), "org.ekstep.learning.platform");
        context.put(TelemetryParams.ENV.name(), "content");
        context.put(TelemetryParams.CHANNEL.name(), Platform.config.getString("channel.default"));
        return TelemetryGenerator.log(context, "system", logLevel, message);
    }
}
