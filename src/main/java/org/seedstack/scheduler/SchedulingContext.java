/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.scheduler;

import java.util.Date;
import java.util.Map;

/**
 * Provides detailed scheduling information for a task.
 */
public interface SchedulingContext {

    /**
     * TaskName is either :
     * <ul>
     * <li>provided through ({@literal @}Scheduled) annotation</li>
     * <li>generated though ({@literal @}ScheduledTaskBuilder) instantiation</li>
     * <li>provided through ({@literal @}ScheduledTaskBuilder) DSL withTaskName method</li>
     * </ul>
     *
     * @return the task name
     */
    String getTaskName();

    /**
     * RequestRecovery notify the Scheduler whether or not the Task should be re-executed if a recovery or fail-over
     * situation is encountered.
     *
     * @return true if recovery should be attempted, false otherwise
     */
    boolean isRequestRecovery();

    /**
     * Whether or not the Task should remain stored after it is
     * orphaned (no Triggers point to it).
     *
     * @return true if the task should be stored durably, false otherwise.
     */
    boolean isStoreDurably();

    /**
     * triggerName is either :
     * <ul>
     * <li>provided through ({@literal @}Scheduled) annotation</li>
     * <li>generated though ({@literal @}ScheduledTaskBuilder) instantiation</li>
     * <li>provided through ({@literal @}ScheduledTaskBuilder) DSL withTriggerName method</li>
     * </ul>
     *
     * @return the trigger name
     */
    String getTriggerName();

    /**
     * The Trigger's priority.  When more than one Trigger have the same
     * fire time, the scheduler will fire the one with the highest priority
     * first.
     *
     * @return the trigger priority
     */
    int getTriggerPriority();

    /**
     * actual fire time of the current Task run
     *
     * @return the fire date
     */
    Date getCurrentFireDate();

    /**
     * fire time of the previous Task run
     *
     * @return the previous fire date
     */
    Date getPreviousFireDate();

    /**
     * fire time of the next Task run
     *
     * @return the next fire date
     */
    Date getNextFireDate();

    /**
     * The amount of time the Task ran for (in milliseconds).
     * The returned value will be -1 until the Task has actually completed (or thrown an exception),
     * and is therefore generally only useful to TaskListeners.
     *
     * @return the runtime task in milliseconds
     */
    long getTaskRuntime();

    /**
     * actual scheduled time of the current Task run
     *
     * @return the scheduled fire date
     */
    Date getScheduledFireDate();

    /**
     * Number of times the Trigger has been Task refired
     *
     * @return the refire count
     */
    int getTriggerRefireCount();

    /**
     * Trigger planned date of final run
     *
     * @return the planned final run date
     */
    Date getTriggerFinalFireDate();

    /**
     * Trigger planned end time
     *
     * @return the planned end date
     */
    Date getTriggerEndDate();

    /**
     * Trigger planned start time
     *
     * @return the planned start date
     */
    Date getTriggerStartDate();

    /**
     * The task data map.
     *
     * @return the data map.
     */
    Map<String, ?> getDataMap();

    /**
     * Allow to access to unwrap the context to an implementation-specific class.
     *
     * @param toClass the implementation-specific class to access to.
     * @param <T>     the implementation-specific type.
     * @return the instance implementing the requested type.
     * @throws org.seedstack.seed.SeedException if the class cannot be unwrapped.
     */
    <T> T unwrap(Class<T> toClass);
}
