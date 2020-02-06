/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.scheduler.fixtures;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.quartz.DisallowConcurrentExecution;
import org.seedstack.scheduler.SchedulingContext;
import org.seedstack.scheduler.Task;

@DisallowConcurrentExecution
public class NonConcurrentTask implements Task {

    public static AtomicInteger executionCount = new AtomicInteger();

    @Override
    public void execute(SchedulingContext sc) throws Exception {
        executionCount.incrementAndGet();
        TimeUnit.SECONDS.sleep(2);
    }

}
