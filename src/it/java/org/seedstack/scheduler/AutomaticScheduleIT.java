/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.scheduler;

import static org.junit.Assert.fail;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.seedstack.seed.it.SeedITRunner;

@RunWith(SeedITRunner.class)
public class AutomaticScheduleIT {
    private final static String expectedTaskName = "Task4";
    private final static String expectedTriggerName = "Trigger4";
    static CountDownLatch countDownLatch = new CountDownLatch(3);
    static int invocationCount1 = 0;
    static int invocationCount2 = 0;
    static int invocationCount4 = 0;
    static boolean beforeCalled = false;
    static boolean afterCalled = false;
    static boolean onExceptionCalled = false;
    static String actualTaskName;
    static String actualTriggerName;

    @Test
    public void automatically_timed_task() throws Exception {
        if (!countDownLatch.await(10, TimeUnit.SECONDS))
            fail("timeout during automatically timed task wait");

        Assertions.assertThat(beforeCalled).isTrue();
        Assertions.assertThat(afterCalled).isFalse();
        Assertions.assertThat(onExceptionCalled).isTrue();

        // if the test is slow it will execute TimedTask1 multiple times
        Assertions.assertThat(invocationCount1).isGreaterThanOrEqualTo(1);
        Assertions.assertThat(invocationCount2).isGreaterThanOrEqualTo(1);
        Assertions.assertThat(invocationCount4).isGreaterThanOrEqualTo(1);

        Assertions.assertThat(actualTaskName).isEqualTo(expectedTaskName);
        Assertions.assertThat(actualTriggerName).isEqualTo(expectedTriggerName);
    }

}
