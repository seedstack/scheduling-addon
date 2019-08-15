/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.scheduler;

/**
 * This factory create a {@link ScheduledTaskBuilder} with the given {@link Task}.
 * <pre>
 *     {@literal @}Inject
 *     private ScheduledTasks scheduledTasks;
 *     ...
 *     scheduledTasks.scheduledTask(MyTask.class).cron("* * 0/1 * * ?").schedule();
 * </pre>
 * <p>
 * This class should be injected.
 *
 * @see ScheduledTaskBuilder
 */
public interface ScheduledTasks {

    /**
     * Create a ScheduledTaskBuilder.
     *
     * @param taskClass Task class to schedule
     * @return ScheduledTaskBuilder
     */
    ScheduledTaskBuilder scheduledTask(Class<? extends Task> taskClass);
}
