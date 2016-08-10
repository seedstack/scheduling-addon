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

/**
 * @author tuan.docao@ext.mpsa.com
 */
@Scheduled(value = "${org.seedstack.addons.scheduling.cron.expression}", taskName = "${org.seedstack.addons.scheduling.task.name}", triggerName = "${org.seedstack.addons.scheduling.trigger.name}", exceptionPolicy = UNSCHEDULE_ALL_TRIGGERS)
public class TimedTask4 implements Task {

    @Logging
    private Logger logger;

    @Override
    public void execute(SchedulingContext sc) throws Exception {
        logger.info("Executing timed task 4");
        AutomaticScheduleIT.invocationCount4++;
        AutomaticScheduleIT.actualTaskName = sc.getTaskName();
        AutomaticScheduleIT.actualTriggerName = sc.getTriggerName();
        AutomaticScheduleIT.countDownLatch.countDown();

    }
}
