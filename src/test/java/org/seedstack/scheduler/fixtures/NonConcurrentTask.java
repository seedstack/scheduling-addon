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
