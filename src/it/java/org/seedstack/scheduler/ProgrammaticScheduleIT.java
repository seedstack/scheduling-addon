/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.scheduler;

import static org.junit.Assert.fail;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;
import static org.quartz.TriggerKey.triggerKey;

import com.google.common.collect.ImmutableMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.Trigger;
import org.seedstack.seed.it.SeedITRunner;

@RunWith(SeedITRunner.class)
public class ProgrammaticScheduleIT {
    static String testData;
    static String nativeTestData;
    static CountDownLatch countDownLatch = new CountDownLatch(1);
    static int invocationCount3 = 0;

    @Inject
    private ScheduledTasks scheduledTasks;

    @Test
    public void programmatically_timed_task() throws Exception {
        Trigger trigger = newTrigger()
                .withIdentity(triggerKey("Trigger3", TimedTask3.class.getName()))
                .withSchedule(simpleSchedule().withIntervalInSeconds(1))
                .build();

        scheduledTasks.scheduledTask(TimedTask3.class)
                .withTaskName("Task3")
                .withTrigger(trigger)
                .withPriority(10)
                .withDataMap(ImmutableMap.of("testData", "testValue"))
                .schedule();

        if (!countDownLatch.await(10, TimeUnit.SECONDS))
            fail("timeout during programatically timed task wait");

        scheduledTasks.scheduledTask(TimedTask3.class).unschedule("Trigger3");

        Assertions.assertThat(invocationCount3).isEqualTo(1);
        Assertions.assertThat(testData).isEqualTo("testValue");
        Assertions.assertThat(nativeTestData).isEqualTo("testValue");
    }
}
