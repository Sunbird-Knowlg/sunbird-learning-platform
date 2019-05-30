package org.ekstep.jobs.samza.task;

import org.apache.samza.system.IncomingMessageEnvelope;
import org.apache.samza.system.SystemStreamPartition;
import org.apache.samza.task.InitableTask;
import org.apache.samza.task.StreamTask;
import org.apache.samza.task.WindowableTask;
import org.ekstep.jobs.samza.service.task.JobMetrics;

/**
 * Base Class for Samza Task
 *
 * @author Kumar Gauraw
 */
public abstract class BaseTask implements StreamTask, InitableTask, WindowableTask {

    /**
     *
     * @param envelope
     * @return
     */
    public SystemStreamPartition getSystemStreamPartition(IncomingMessageEnvelope envelope) {
        return envelope.getSystemStreamPartition();
    }

    /**
     *
     * @param envelope
     * @return
     */
    public String getOffset(IncomingMessageEnvelope envelope) {
        return envelope.getOffset();
    }

    /**
     *
     * @param systemStreamPartition
     * @param offset
     * @param metrics
     */
    public void setMetricsOffset(SystemStreamPartition systemStreamPartition, String offset, JobMetrics metrics) {
        metrics.setOffset(systemStreamPartition, offset);
    }
}
