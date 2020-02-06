package org.seedstack.scheduler.internal;

import org.quartz.PersistJobDataAfterExecution;
import org.seedstack.scheduler.Task;

@PersistJobDataAfterExecution
class PersistentDataTaskDelegateJob extends TaskDelegateJob {

    public PersistentDataTaskDelegateJob(Task task) {
        super(task);
    }

}
