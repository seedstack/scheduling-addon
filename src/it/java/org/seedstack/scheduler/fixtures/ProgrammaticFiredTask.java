/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.scheduler.fixtures;

import org.quartz.JobExecutionContext;
import org.seedstack.scheduler.ProgrammaticScheduleIT;
import org.seedstack.scheduler.SchedulingContext;
import org.seedstack.scheduler.Task;
import org.seedstack.seed.Logging;
import org.slf4j.Logger;

public class ProgrammaticFiredTask implements Task {
    @Logging
    private Logger logger;

    @Override
    public void execute(SchedulingContext sc) throws Exception {
        logger.info("Executing ProgrammaticFiredTask");
        String testData = (String) sc.getDataMap().get("testData");
        String nativeTestData = sc.unwrap(JobExecutionContext.class)
                .getJobDetail()
                .getJobDataMap()
                .getString("testData");

        ProgrammaticScheduleIT.markProgrammaticInvocation();
        ProgrammaticScheduleIT.setTestData(testData, nativeTestData);
        ProgrammaticScheduleIT.getCountDownLatch().countDown();

    }
}
