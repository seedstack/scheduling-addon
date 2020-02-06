package org.seedstack.scheduler;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.TriggerBuilder;
import org.seedstack.scheduler.fixtures.NonConcurrentTask;
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
                .withTrigger(TriggerBuilder.newTrigger().startNow()
                        .withSchedule(SimpleScheduleBuilder.repeatSecondlyForTotalCount(9)
                                .withMisfireHandlingInstructionNextWithRemainingCount())
                        .build())
                .schedule();

        TimeUnit.SECONDS.sleep(9);

        Assertions.assertThat(NonConcurrentTask.executionCount.get()).isEqualTo(5);

    }

}
