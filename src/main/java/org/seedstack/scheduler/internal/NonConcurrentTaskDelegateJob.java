package org.seedstack.scheduler.internal;

import org.quartz.DisallowConcurrentExecution;
import org.seedstack.scheduler.Task;

@DisallowConcurrentExecution
class NonConcurrentTaskDelegateJob extends TaskDelegateJob {

    public NonConcurrentTaskDelegateJob(Task task) {
        super(task);
    }

}
