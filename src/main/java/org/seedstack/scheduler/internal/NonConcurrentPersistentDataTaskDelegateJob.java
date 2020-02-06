package org.seedstack.scheduler.internal;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.PersistJobDataAfterExecution;
import org.seedstack.scheduler.Task;

@DisallowConcurrentExecution
@PersistJobDataAfterExecution
class NonConcurrentPersistentDataTaskDelegateJob extends TaskDelegateJob {

    public NonConcurrentPersistentDataTaskDelegateJob(Task task) {
        super(task);
    }

}
