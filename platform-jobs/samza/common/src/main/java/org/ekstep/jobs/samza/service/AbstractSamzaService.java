package org.ekstep.jobs.samza.service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.samza.system.OutgoingMessageEnvelope;
import org.apache.samza.system.SystemStream;
import org.apache.samza.task.MessageCollector;
import org.ekstep.jobs.samza.util.SamzaCommonParams;

import com.ilimi.common.Platform;
import com.ilimi.common.logger.LoggerEnum;
import com.ilimi.common.logger.PlatformLogger;

public abstract class AbstractSamzaService implements ISamzaService {
	
	private String eventId = "BE_JOB_REQUEST";
	protected String jobType = "";
	protected String jobStartMessage = "";
	protected String jobEndMessage = "";
    protected String jobClass = "";
    
    private static String mid = "LP."+System.currentTimeMillis()+"."+UUID.randomUUID();
    private static String startJobEventId = "JOB_START";
	private static String endJobEventId = "JOB_END";
	protected static int MAXITERTIONCOUNT= 2;
	
	private SystemStream backendStream = null;
	
	private SystemStream getBackendSystemStream() {
		if (null == this.backendStream) {
			this.backendStream = new SystemStream(SamzaCommonParams.kafka.name(), Platform.config.getString("backend_telemetry_topic"));
		}
		return this.backendStream;
	}
	
	protected void preProcess(Map<String, Object> message, MessageCollector collector) throws Exception {
		String eid = (String) message.get(SamzaCommonParams.eid.name());
		if(StringUtils.equalsIgnoreCase(this.eventId, eid)) {
			@SuppressWarnings("unchecked")
			Map<String, Object> edata = (Map<String, Object>) message.get(SamzaCommonParams.edata.name());
			String requestedJobType = (String) edata.get(SamzaCommonParams.action.name());
			if(StringUtils.equalsIgnoreCase(this.jobType, requestedJobType)) {
				int currentIteration = (int) edata.get(SamzaCommonParams.iteration.name());
				System.out.println("Before preprocess...");

				if (isInvalidMessage(message)) {
					String event = generateEvent(LoggerEnum.ERROR.name(), "Samza job de-serialization error", message);
					collector.send(new OutgoingMessageEnvelope(getBackendSystemStream(), event));
					// throw exception and catch.
				}
				
				if(currentIteration <= getMaxIterations()) {
					Map<String, Object> jobStartEvent = getJobEvent("JOBSTARTEVENT", message);
					System.out.println("Inside preProcess Before pushEvent: " + message.toString());
					pushEvent(jobStartEvent, collector, getBackendSystemStream());
				}
				
				System.out.println("Preprocess completed...");
//				this.processMessage(message, metrics, collector);
//				System.out.println("process completed...");
//				postProcess(message, collector, jobStartTime, maxIterationCount, iterationCount);
//				System.out.println("Postprocess completed...");
			} else {
				System.out.println("message jobType is invalid: " + jobType + " :: expected:" + this.jobType);
			}
		} else {
			System.out.println("message event id is invalid: " + eid + " :: expected:" + this.eventId);
		}
	}
	
	@SuppressWarnings("unchecked")
	protected void postProcess(Map<String, Object> message, MessageCollector collector, long jobStartTime, int maxIterations, int currentIteration) throws Exception {
		if(currentIteration <= maxIterations) {
			Map<String, Object> jobEndEvent = getJobEvent("JOBENDEVENT", message);
			addExecutionTime(jobEndEvent, jobStartTime); //Call to add execution time
			System.out.println("Inside postProcess Before pushEvent: " + message.toString());
			pushEvent(jobEndEvent, collector, getBackendSystemStream());
		}
		
		Map<String, Object> edata = (Map<String, Object>) message.get(SamzaCommonParams.edata.name());
		String status = (String) edata.get(SamzaCommonParams.status.name());
		if(StringUtils.equalsIgnoreCase(status, SamzaCommonParams.FAILED.name()) && currentIteration < maxIterations) {
			edata.put(SamzaCommonParams.iteration.name(), currentIteration + 1);
			collector.send(new OutgoingMessageEnvelope(new SystemStream(SamzaCommonParams.kafka.name(), Platform.config.getString("failed_event_topic")), message));
		}
	}
	
	@SuppressWarnings("unchecked")
	private void addExecutionTime(Map<String, Object> jobEndEvent, long jobStartTime) {
		Map<String, Object> eks = (Map<String, Object>)((Map<String, Object>)jobEndEvent.get(SamzaCommonParams.edata.name())).get(SamzaCommonParams.eks.name());
		Map<String, Object> execution = new HashMap<>();
		execution.put(SamzaCommonParams.submitted_date.name(), eks.get(SamzaCommonParams.ets.name()));
		execution.put(SamzaCommonParams.processing_date.name(), jobStartTime);
		execution.put(SamzaCommonParams.completed_date.name(), jobEndEvent.get(SamzaCommonParams.ets.name()));
		execution.put(SamzaCommonParams.latency.name(), (long)eks.get(SamzaCommonParams.ets.name())-jobStartTime);
		execution.put(SamzaCommonParams.execution_time.name(), jobStartTime-(long)jobEndEvent.get(SamzaCommonParams.ets.name()));
		eks.put(SamzaCommonParams.execution.name(), execution);
		
	}
	
	protected boolean isInvalidMessage(Map<String, Object> message) {
		return (message == null || (null != message && message.containsKey("serde")
				&& "error".equalsIgnoreCase((String) message.get("serde"))));
	}
    
	private String generateEvent(String logLevel, String message, Map<String, Object> data) {
		String event = PlatformLogger.getBELog(logLevel, message, data, null);
		return event;
	}
	
	private void pushEvent(Map<String, Object> message, MessageCollector collector, SystemStream stream) throws Exception {
		try {
			collector.send(new OutgoingMessageEnvelope(stream, message));
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
	
	protected int getMaxIterations() {
		int maxIteration = MAXITERTIONCOUNT;
		if(Platform.config.hasPath("max.iteration.count.samza.job")) 
			maxIteration = Platform.config.getInt("max.iteration.count.samza.job");
		return maxIteration;
	}
	
}
