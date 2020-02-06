package org.seedstack.scheduler.internal;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.PersistJobDataAfterExecution;
import org.seedstack.scheduler.Task;

class JobDelegateFactory {

    private JobDelegateFactory() {
        // No Instances
    }

    static Job buildJobWrapper(Task task) {

        try {
            return computeDelegateClass(task.getClass()).getConstructor(Task.class).newInstance(task);
        } catch (Exception e) {
            // TODO: Put a proper logger / error handler here
            throw new RuntimeException(e);
        }

    }

    static Class<? extends TaskDelegateJob> computeDelegateClass(Class<? extends Task> taskClazz) {

        boolean nonConcurrentExecution = taskClazz.getAnnotation(DisallowConcurrentExecution.class) != null;
        boolean persistDataAfterExecution = taskClazz.getAnnotation(PersistJobDataAfterExecution.class) != null;

        if (!nonConcurrentExecution && !persistDataAfterExecution) {
            return TaskDelegateJob.class;
        } else if (nonConcurrentExecution && persistDataAfterExecution) {
            return NonConcurrentPersistentDataTaskDelegateJob.class;
        } else if (nonConcurrentExecution && !persistDataAfterExecution) {
            return NonConcurrentTaskDelegateJob.class;
        } else {
            return PersistentDataTaskDelegateJob.class;
        }

    }

}
