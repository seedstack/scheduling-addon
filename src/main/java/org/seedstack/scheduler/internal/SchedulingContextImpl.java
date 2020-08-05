/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.scheduler.internal;

import java.util.Date;
import java.util.Map;
import org.quartz.JobExecutionContext;
import org.seedstack.scheduler.SchedulingContext;
import org.seedstack.seed.SeedException;

class SchedulingContextImpl implements SchedulingContext {

    private final JobExecutionContext jobExecutionContext;

    // Task

    /**
     * The Task name, must be unique within the group.
     */
    private final String taskName;

    /**
     * Instructs the Scheduler whether or not the Task should
     * be re-executed if a recovery or fail-over situation is
     * encountered.
     */
    private final boolean requestRecovery;

    /**
     * Whether or not the Task should remain stored after it is
     * orphaned (no Triggers point to it).
     */
    private final boolean storeDurably;

    // Trigger

    /**
     * The Trigger name, must be unique within the group.
     */
    private final String triggerName;

    /**
     * The Trigger's priority.  When more than one Trigger have the same
     * fire time, the scheduler will fire the one with the highest priority
     * first.
     */
    private final int triggerPriority;

    /**
     * actual fire time of the current Task run
     */
    private final Date currentFireDate;
    /**
     * fire time of the previous Task run
     */
    private final Date previousFireDate;
    /**
     * fire time of the next Task run
     */
    private final Date nextFireDate;

    /**
     * The amount of time the job ran for (in milliseconds).
     * The returned value will be -1 until the Task has actually completed (or thrown an exception),
     * and is therefore generally only useful to TaskListeners.
     */
    private final long taskRuntime;

    /**
     * actual scheduled time of the current Task run
     */
    private final Date scheduledFireDate;

    /**
     * Number of times the Trigger has been Task refired
     */
    private final int triggerRefireCount;

    /**
     * Trigger planned date of final run
     */
    private final Date triggerFinalFireDate;

    /**
     * Trigger planned end time
     */
    private final Date triggerEndDate;

    /**
     * Trigger planned start time
     */
    private final Date triggerStartDate;

    /**
     * The task data map.
     */
    private final Map<String, ?> dataMap;

    SchedulingContextImpl(JobExecutionContext context) {
        jobExecutionContext = context;
        taskName = context.getJobDetail().getKey().getName();
        storeDurably = context.getJobDetail().isDurable();
        requestRecovery = context.getJobDetail().requestsRecovery();
        dataMap = context.getJobDetail().getJobDataMap();

        scheduledFireDate = context.getScheduledFireTime();
        currentFireDate = context.getFireTime();
        previousFireDate = context.getPreviousFireTime();
        nextFireDate = context.getNextFireTime();

        taskRuntime = context.getJobRunTime();

        triggerRefireCount = context.getRefireCount();
        triggerEndDate = context.getTrigger().getEndTime();
        triggerFinalFireDate = context.getTrigger().getFinalFireTime();
        triggerName = context.getTrigger().getKey().getName();
        triggerStartDate = context.getTrigger().getStartTime();
        triggerPriority = context.getTrigger().getPriority();

    }

    @Override
    public String getTaskName() {
        return taskName;
    }

    @Override
    public boolean isRequestRecovery() {
        return requestRecovery;
    }

    @Override
    public boolean isStoreDurably() {
        return storeDurably;
    }

    @Override
    public String getTriggerName() {
        return triggerName;
    }

    @Override
    public int getTriggerPriority() {
        return triggerPriority;
    }

    @Override
    public Date getCurrentFireDate() {
        return currentFireDate;
    }

    @Override
    public Date getPreviousFireDate() {
        return previousFireDate;
    }

    @Override
    public Date getNextFireDate() {
        return nextFireDate;
    }

    @Override
    public long getTaskRuntime() {
        return taskRuntime;
    }

    @Override
    public Date getScheduledFireDate() {
        return scheduledFireDate;
    }

    @Override
    public int getTriggerRefireCount() {
        return triggerRefireCount;
    }

    @Override
    public Date getTriggerFinalFireDate() {
        return triggerFinalFireDate;
    }

    @Override
    public Date getTriggerEndDate() {
        return triggerEndDate;
    }

    @Override
    public Date getTriggerStartDate() {
        return triggerStartDate;
    }

    @Override
    public Map<String, ?> getDataMap() {
        return dataMap;
    }

    @Override
    public <T> T unwrap(Class<T> toClass) {
        if (toClass.isAssignableFrom(jobExecutionContext.getClass())) {
            return toClass.cast(jobExecutionContext);
        }
        throw SeedException.createNew(SchedulerErrorCode.UNABLE_TO_UNWRAP)
                .put("class", toClass);
    }
}
