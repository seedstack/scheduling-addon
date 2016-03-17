/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.scheduler;

import org.seedstack.seed.Logging;
import org.slf4j.Logger;

import static org.seedstack.scheduler.ExceptionPolicy.UNSCHEDULE_ALL_TRIGGERS;

@Scheduled(value = "* * * * * ?", taskName = "Task1", triggerName = "Trigger1", exceptionPolicy = UNSCHEDULE_ALL_TRIGGERS)
public class TimedTask1 implements Task {
    @Logging
    Logger logger;

    @Override
    public void execute(SchedulingContext sc) throws Exception {
        logger.info("Executing timed task 1");
        AutomaticScheduleIT.invocationCount1++;
        AutomaticScheduleIT.countDownLatch.countDown();
        throw new Exception("oups");
    }
}
