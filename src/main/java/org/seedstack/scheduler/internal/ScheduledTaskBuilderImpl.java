/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.scheduler.internal;

import static java.util.TimeZone.getDefault;
import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import org.apache.commons.lang.StringUtils;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobKey;
import org.quartz.ObjectAlreadyExistsException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.seedstack.scheduler.Scheduled;
import org.seedstack.scheduler.ScheduledTaskBuilder;
import org.seedstack.scheduler.Task;
import org.seedstack.seed.Application;
import org.seedstack.seed.SeedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DSL to produce {@code Job} and add to a {@code Scheduler}, and associate the related {@code Trigger} with it.
 */
class ScheduledTaskBuilderImpl implements ScheduledTaskBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledTaskBuilderImpl.class);

    private static final String DEFAULT = "DEFAULT";

    private final Scheduler scheduler;

    /**
     * The type of the {@code Job} to be executed.
     */
    private final Class<? extends Job> jobClass;

    /**
     * Instructs the {@code Scheduler} whether or not the {@code Job} should be re-executed if a {@code recovery} or
     * {@code fail-over} situation is encountered.
     */
    private boolean requestRecovery;

    /**
     * Whether or not the {@code Job} should remain stored after it is orphaned (no {@code Trigger}s point to it).
     */
    private boolean storeDurably;

    /**
     * The {@code Trigger} name, must be unique within the group.
     */
    private String triggerName;

    /**
     * The {@code Trigger} group.
     */
    private String triggerGroup;

    /**
     * The cron expression to base the schedule on.
     */
    private String cronExpression;

    /**
     * The time zone for which the {@code cronExpression} of this {@code CronTrigger} will be resolved.
     */
    private TimeZone timeZone = getDefault();

    /**
     * The {@code Trigger}'s priority. When more than one {@code Trigger} have the same fire time, the scheduler will fire the one
     * with the highest priority first.
     */
    private int priority;

    /**
     * The {@code JobKey} to beb used to schedule the {@code Job}
     */
    private JobKey jobKey;

    /**
     * The {@code Trigger} to be used to schedule the {@code Job}
     */
    private Trigger trigger;

    /**
     * Indicates whether the job's trigger should be updated if it is already existing when being scheduled (which is typically
     * the case with a persistent {@link org.quartz.spi.JobStore}.
     */
    private boolean updateExistingTrigger;

    /**
     * The data map passed to the task
     */
    private Map<String, ?> dataMap;

    private String jobGroup;

    private String jobName;

    private Class<? extends Task> taskClass;

    ScheduledTaskBuilderImpl(final Class<? extends Task> taskClass, Scheduler scheduler, Application application) {

        this.jobClass = JobDelegateFactory.computeDelegateClass(taskClass);
        this.scheduler = scheduler;
        this.taskClass = taskClass;

        Scheduled annotation = taskClass.getAnnotation(Scheduled.class);

        // the group associated to a trigger or a job is the taskClass
        this.jobGroup = taskClass.getName();
        this.triggerGroup = taskClass.getName();

        if (annotation != null) {
            // when annotation is not empty
            // if present, the name associated to a trigger or a job is retrieved
            // else, it is generated but can still be provided
            // with DSL withTriggerName() / withTaskName() methods
            this.cronExpression = application.substituteWithConfiguration(annotation.value());
            this.jobName = DEFAULT.equals(annotation.taskName()) ? UUID.randomUUID()
                    .toString() : application.substituteWithConfiguration(annotation.taskName());
            this.triggerName = DEFAULT.equals(annotation.triggerName()) ? UUID.randomUUID()
                    .toString() : application.substituteWithConfiguration(annotation.triggerName());
            this.timeZone = !DEFAULT.equals(annotation.timeZoneId()) ? TimeZone.getTimeZone(annotation.timeZoneId())
                    : getDefault();
            this.requestRecovery = annotation.requestRecovery();
            this.priority = annotation.priority();
            this.storeDurably = annotation.storeDurably();
        } else {
            // when no annotation is provided
            // the name associated to a trigger or a job can be generated or
            // provided with DSL withTriggerName() / withTaskName() methods
            this.jobName = UUID.randomUUID().toString();
            this.triggerName = UUID.randomUUID().toString();
        }
    }

    @Override
    public ScheduledTaskBuilder withRequestRecovery(boolean requestRecovery) {
        this.requestRecovery = requestRecovery;
        return this;
    }

    @Override
    public ScheduledTaskBuilder withStoreDurably(boolean storeDurably) {
        this.storeDurably = storeDurably;
        return this;
    }

    @Override
    public ScheduledTaskBuilder withTriggerName(String triggerName) {
        this.triggerName = triggerName;
        return this;
    }

    @Override
    public ScheduledTaskBuilder withCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
        return this;
    }

    @Override
    public ScheduledTaskBuilder withTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
        return this;
    }

    @Override
    public ScheduledTaskBuilder withPriority(int priority) {
        this.priority = priority;
        return this;
    }

    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    @Override
    public ScheduledTaskBuilder withTrigger(Trigger trigger) {
        this.trigger = trigger;
        return this;
    }

    @Override
    public ScheduledTaskBuilder withTaskName(String taskName) {
        this.jobName = taskName;
        return this;
    }

    @Override
    public ScheduledTaskBuilder withDataMap(Map<String, ?> dataMap) {
        this.dataMap = dataMap;
        return this;
    }

    @Override
    public ScheduledTaskBuilder updateExistingTrigger() {
        this.updateExistingTrigger = true;
        return this;
    }

    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    @Override
    public void schedule() {

        if (StringUtils.isBlank(cronExpression) && trigger == null) {
            throw SeedException.createNew(SchedulerErrorCode.MISSING_CRON_EXPRESSION).put("class", jobClass.getName());
        }
        if (StringUtils.isNotBlank(cronExpression) && trigger != null) {
            throw SeedException.createNew(SchedulerErrorCode.IMPOSSIBLE_TO_USE_CRON_AND_TRIGGER)
                    .put("class", jobClass.getName());
        }

        try {
            if (updateExistingTrigger && scheduler.checkExists(getTriggerKey())) {
                scheduler.unscheduleJob(getTriggerKey());
            }
        } catch (SchedulerException e) {
            throw SeedException.wrap(e, SchedulerErrorCode.SCHEDULER_ERROR);
        }

        try {
            JobBuilder jobBuilder = newJob(jobClass)
                    .withIdentity(getJobKey())
                    .requestRecovery(requestRecovery)
                    .storeDurably(storeDurably);

            if (dataMap != null) {
                jobBuilder.usingJobData(new JobDataMap(dataMap));
            }

            scheduler.scheduleJob(jobBuilder.build(), getTrigger());

            if (cronExpression != null) {
                LOGGER.info("Scheduled {} task with cron {}", taskClass.getCanonicalName(), cronExpression);
            } else {
                LOGGER.info("Scheduled {} task with custom trigger {}", taskClass.getCanonicalName(), trigger.getKey());
            }
        } catch (ObjectAlreadyExistsException e) {
            throw SeedException.wrap(e, SchedulerErrorCode.TRIGGER_AND_JOB_NAME_SHOULD_BE_UNIQUE);
        } catch (Exception e) {
            throw SeedException.wrap(e, SchedulerErrorCode.SCHEDULER_ERROR);
        }
    }

    @Override
    public void reschedule(String triggerName) {
        this.reschedule(buildTriggerKey(triggerName));
    }

    @Override
    public void reschedule(String triggerName, String triggerGroup) {
        this.reschedule(TriggerKey.triggerKey(triggerName, triggerGroup));
    }

    @Override
    public void unschedule(String triggerName) {
        this.unschedule(buildTriggerKey(triggerName));
    }

    @Override
    public void unschedule(String triggerName, String triggerGroup) {
        this.unschedule(TriggerKey.triggerKey(triggerName, triggerGroup));

    }

    Trigger getTrigger() {
        return (trigger == null) ? newTrigger()
                .withIdentity(getTriggerKey())
                .withSchedule(cronSchedule(cronExpression)
                        .inTimeZone(timeZone))
                .withPriority(priority)
                .build()
                : trigger;
    }

    JobKey getJobKey() {
        if (jobKey == null) {
            jobKey = JobKey.jobKey(jobName, jobGroup);
        }
        return jobKey;
    }

    TriggerKey getTriggerKey() {
        if (trigger == null) {
            return TriggerKey.triggerKey(triggerName, triggerGroup);
        }
        // If trigger object is present, we use it instead of a generic TriggerKey
        return trigger.getKey();

    }

    private TriggerKey buildTriggerKey(String name) {
        return new TriggerKey(name, taskClass.getName());
    }

    private void reschedule(TriggerKey tk) {
        try {
            if (!scheduler.checkExists(tk)) {
                throw SeedException.createNew(SchedulerErrorCode.UNRECOGNIZED_TRIGGER)
                        .put("triggerName", tk.getName()).put("triggerGroup", tk.getGroup());
            }
            if (scheduler.rescheduleJob(tk, getTrigger()) == null) {
                throw SeedException.createNew(SchedulerErrorCode.UNRECOGNIZED_TRIGGER)
                        .put("triggerName", tk.getName()).put("triggerGroup", tk.getGroup());
            }
            LOGGER.info("Recheduled {} task with trigger {}", taskClass.getCanonicalName(), tk);
        } catch (SchedulerException e) {
            throw SeedException.wrap(e, SchedulerErrorCode.SCHEDULER_ERROR);
        }
    }

    private void unschedule(TriggerKey tk) {
        try {
            if (scheduler.checkExists(tk)) {
                scheduler.unscheduleJob(tk);
                LOGGER.info("Unscheduled {} task with trigger {}", taskClass.getCanonicalName(), tk);
            }
        } catch (SchedulerException e) {
            throw SeedException.wrap(e, SchedulerErrorCode.SCHEDULER_ERROR);
        }
    }

}
