/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.scheduler.internal;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.PersistJobDataAfterExecution;
import org.seedstack.scheduler.Task;
import org.seedstack.seed.SeedException;

class JobDelegateFactory {

    private JobDelegateFactory() {
        // No Instances
    }

    static Job buildJobWrapper(Task task) {
        try {
            return computeDelegateClass(task.getClass()).getConstructor(Task.class).newInstance(task);
        } catch (Exception e) {
            throw SeedException.wrap(e, SchedulerErrorCode.CANNOT_INITIALIZE_TASK);
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
