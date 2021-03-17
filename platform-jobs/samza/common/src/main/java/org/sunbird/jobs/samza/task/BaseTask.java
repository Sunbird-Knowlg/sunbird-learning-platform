package org.sunbird.jobs.samza.task;

import org.apache.samza.task.InitableTask;
import org.apache.samza.task.StreamTask;
import org.apache.samza.task.WindowableTask;

/**
 * Base Class for Samza Task
 *
 * @author Kumar Gauraw
 */
public abstract class BaseTask implements StreamTask, InitableTask, WindowableTask {
    //TODO: Provide Common Method Implementation Here.
}
