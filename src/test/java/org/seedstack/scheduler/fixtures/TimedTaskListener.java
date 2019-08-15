/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.scheduler.fixtures;

import java.util.TimeZone;

import javax.inject.Inject;

import org.seedstack.scheduler.AutomaticScheduleIT;
import org.seedstack.scheduler.ScheduledTasks;
import org.seedstack.scheduler.SchedulingContext;
import org.seedstack.scheduler.TaskListener;
import org.seedstack.seed.Logging;
import org.slf4j.Logger;

public class TimedTaskListener implements TaskListener<TimedCronExpressionTask> {
    @Logging
    private Logger logger;

    @Inject
    private ScheduledTasks scheduledTasks;

    @Override
    public void before(SchedulingContext sc) {
        logger.info("Before timed task of Task {} on trigger {}", sc.getTaskName(), sc.getTriggerName());
        AutomaticScheduleIT.notifyBeforeTriggerInvocation();
    }

    @Override
    public void after(SchedulingContext sc) {
        logger.info("After timed task from Task {} on trigger {}", sc.getTaskName(), sc.getTriggerName());
        AutomaticScheduleIT.notifyAfterTriggerInvocation();
    }

    @Override
    public void onException(SchedulingContext sc, Exception e) {
        AutomaticScheduleIT.notifyOnExcetionInvocation();

        logger.info("Rescheduling timed task 2 from task {} on trigger {}", sc.getTaskName(), sc.getTriggerName());
        scheduledTasks.scheduledTask(ListenerFiredTask.class)
                .withTaskName("Task2")
                .withTriggerName("Trigger2")
                .withCronExpression("* * * * * ?")
                .withStoreDurably(false)
                .withRequestRecovery(false)
                .withTimeZone(TimeZone.getDefault())
                .schedule();
    }
}
