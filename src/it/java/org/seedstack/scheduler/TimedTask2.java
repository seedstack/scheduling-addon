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


public class TimedTask2 implements Task {
    @Logging
    Logger logger;

    @Override
    public void execute(SchedulingContext sc) throws Exception {
        logger.info("Executing timed task 2");
        AutomaticScheduleIT.invocationCount2++;
        AutomaticScheduleIT.countDownLatch.countDown();
    }
}
