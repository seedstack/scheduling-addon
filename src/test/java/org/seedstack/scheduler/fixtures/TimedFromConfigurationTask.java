/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.scheduler.fixtures;

import static org.seedstack.scheduler.ExceptionPolicy.UNSCHEDULE_ALL_TRIGGERS;

import org.seedstack.scheduler.AutomaticScheduleIT;
import org.seedstack.scheduler.Scheduled;
import org.seedstack.scheduler.SchedulingContext;
import org.seedstack.scheduler.Task;
import org.seedstack.seed.Logging;
import org.slf4j.Logger;

@Scheduled(value = "${test.scheduling.cronExpression}", 
           taskName = "${test.scheduling.taskName}", 
           triggerName = "${test.scheduling.triggerName}", 
           exceptionPolicy = UNSCHEDULE_ALL_TRIGGERS)
public class TimedFromConfigurationTask implements Task {

    @Logging
    private Logger logger;

    @Override
    public void execute(SchedulingContext sc) throws Exception {
        logger.info("Executing TimedFromConfigurationTask");
        AutomaticScheduleIT.notifyConfigFiredTaskInvocation(sc);
        AutomaticScheduleIT.countDownLatch.countDown();
    }
}
