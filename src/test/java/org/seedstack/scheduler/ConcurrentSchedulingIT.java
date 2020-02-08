/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.scheduler;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.TriggerBuilder;
import org.seedstack.scheduler.fixtures.NonConcurrentTask;
import org.seedstack.scheduler.fixtures.TimedDurableDataTask;
import org.seedstack.scheduler.fixtures.TimedNonDurableDataTask;
import org.seedstack.seed.testing.junit4.SeedITRunner;

@RunWith(SeedITRunner.class)
public class ConcurrentSchedulingIT {

    @Inject
    private ScheduledTasks scheduledTasks;

    /***
     * SCENARIO
     * 
     * Trigger should have been fired 9 times.
     * <p>
     * The task has a 2 second delay, so it will miss 4 Triggers (Odd Seconds)
     * </p>
     * Due to Misfire policy, missed triggers will be ignored and won't be fired
     */
    @Test
    public void testNonConcurrentExecutions() throws Exception {

        NonConcurrentTask.executionCount.set(0);

        Assertions.assertThat(NonConcurrentTask.executionCount.get()).isEqualTo(0);

        scheduledTasks.scheduledTask(NonConcurrentTask.class)
                .withTrigger(TriggerBuilder.newTrigger().startNow().withSchedule(SimpleScheduleBuilder
                        .repeatSecondlyForTotalCount(9).withMisfireHandlingInstructionNextWithRemainingCount()).build())
                .schedule();

        TimeUnit.SECONDS.sleep(9);

        Assertions.assertThat(NonConcurrentTask.executionCount.get()).isEqualTo(5);

    }

    @Test
    public void testPersistentData() throws Exception {
        TimedDurableDataTask.fireCount = 0;
        TimedNonDurableDataTask.fireCount = 0;

        Map<String, Integer> data = new HashMap<>();

        data.put("count", 0);
        scheduledTasks.scheduledTask(TimedDurableDataTask.class)
                .withTrigger(TriggerBuilder.newTrigger().startNow()
                        .withSchedule(SimpleScheduleBuilder.repeatSecondlyForTotalCount(5)).build())
                .withDataMap(new HashMap<>(data)).schedule();
        scheduledTasks.scheduledTask(TimedNonDurableDataTask.class)
                .withTrigger(TriggerBuilder.newTrigger().startNow()
                        .withSchedule(SimpleScheduleBuilder.repeatSecondlyForTotalCount(5)).build())
                .withDataMap(new HashMap<>(data)).schedule();

        TimeUnit.SECONDS.sleep(5);

        Assertions.assertThat(TimedNonDurableDataTask.fireCount).isEqualTo(1);
        Assertions.assertThat(TimedDurableDataTask.fireCount).isEqualTo(5);

    }

}
