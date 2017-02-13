---
title: "Scheduling"
repo: "https://github.com/seedstack/scheduling-addon"
author: Pierre THIROUIN
description: "Provides easy-to-use support for task scheduling through Quartz."
tags:
    - "scheduling"
    - "task"
    - "quartz"
    - "cron"
    - "timer"
zones:
    - Addons
menu:
    AddonScheduling:
        weight: 10
---

Scheduling add-on provides a simple API to schedule task in Seed. 

{{< dependency g="org.seedstack.addons.scheduling" a="scheduling" >}}

# Declarative API

Create a `Class` implementing {{< java "org.seedstack.scheduler.Task" >}} and add a
{{< java "org.seedstack.scheduler.Scheduled" "@" >}} annotation with a cron expression.<br>
Your task will be detected and scheduled according to the annotation content at Seed startup:

    @Scheduled("0/2 * * * * ?")
    public class MyTask implements Task {

        @Override
        public void execute() throws Exception {
            return calculateSomething();
        }
    }

As shown in above snippet, the default "value" attribute of `@Scheduled` is used for cron expression. <br>
If any other attribute is required, the annotation becomes for instance :
	
	@Scheduled(value = "0/2 * * * * ?", taskName = "TASK1", exceptionPolicy = UNSCHEDULE_ALL_TRIGGERS)

`exceptionPolicy` defines the behaviour on `Task`'s exception. Refer to `@Scheduled` JavaDoc for all its attributes.
Refer to [Quartz Documentation](http://quartz-scheduler.org/generated/2.2.1/html/qs-all/#page/Quartz_Scheduler_Documentation_Set%2Fco-trg_crontriggers.html%23) for cron expression details.

# Programmatic API
Inject the `ScheduledTasks` interface and programmatically define a scheduled task (not necessarily at application
startup) with the following DSL:

## Cron expression

    @Inject
    private ScheduledTasks scheduledTasks;
    
    ...
    
    scheduledTasks.scheduledTask(MyTask.class)
        .withTaskName("usefulTask")
	    .withCronExpression("0/2 * * * * ?")
	    .schedule();

{{% callout info %}}
The above cron expression implicitly defines a `Trigger` that will fire accordingly.
{{% /callout %}}

## With a Trigger

When a cron expression is not enough to define the expected triggering conditions, a Quartz `Trigger` can be defined:

    @Inject
    private ScheduledTasks scheduledTasks;
    
    ...
    
    Trigger trigger = TriggerBuilder.newTrigger()
	    .withIdentity(TriggerKey.triggerKey("myTrigger", "myTriggerGroup"))
	    .withSchedule(SimpleScheduleBuilder.simpleSchedule()
		    .withIntervalInSeconds(1)
            .repeatForever())
	    .startAt(DateBuilder.futureDate(2,DateBuilder.IntervalUnit.SECOND))
	    .build();
 	
    scheduledTasks.scheduledTask(MyTask.class)
            .withTrigger(trigger)
            .withPriority(10)
            .schedule();

# Listeners
Create a `Class` implementing `TaskListener` in order to listen to the `Task` execution. The `Task` is bound to the
{{< java "org.seedstack.scheduler.TaskListener" >}} by declaring the `Task` as the `Type` parameter:

    public class MyTaskListener implements TaskListener<MyTask> {
        @Logging
        private Logger logger;

        @Inject
        private ScheduledTasks scheduledTasks;

        @Override
        public void before(SchedulingContext schedulingContext) {
            logger.info("Before MyTask");
        }

        @Override
        public void after(SchedulingContext schedulingContext) {
            logger.info("After MyTask");
        }

        @Override
        public void onException(SchedulingContext schedulingContext, Exception e) {
            logger.info("Something has gone wrong, unscheduling", e);
			scheduledTasks
			    .scheduledTask(MyTask.class)
			    .unschedule(schedulingContext.getTriggerName());
        }
    }

{{% callout tips %}}
**Keep Code In Listeners Concise And Efficient.** Performing large amounts of work is discouraged, as the thread that
would be executing the job (or completing the trigger and moving on to firing another job, etc.) will be tied up
within the listener.
{{% /callout %}}

{{% callout warning %}}
**Handle Exceptions.** Every listener method should contain a try-catch block that handles all possible exceptions. If
a listener throws an exception, it may cause other listeners not to be notified and/or prevent the execution of
the job, etc.
{{% /callout %}}

# Exception handling

When exception occurs during the task execution, you can choose to unschedule the Task or refire it immediately. You just
have add an ExceptionPolicy to the Scheduled annotation.

    @Scheduled(value = "0/2 * * * * ?", exceptionPolicy = UNSCHEDULE_ALL_TRIGGERS)

ExceptionPolicy can take the following values:

* `REFIRE_IMMEDIATELY`: Immediately re-execute the task. This option **SHOULD BE USED VERY CAREFULLY** as the `Task`
will be fired indefinitely until successful or the application crashes.

* `UNSCHEDULE_FIRING_TRIGGER`: Unschedule the `Trigger` firing the `Task`. This option is convenient when a `Task`
fails due to a specific trigger.

* `UNSCHEDULE_ALL_TRIGGERS`: Unschedule all triggers associated to the `Task`.

* `NONE`: Do nothing. Default value.

You can also choose to handle exception by yourself with a TaskListener. It will be possible to use the
`UNSCHEDULE_ALL_TRIGGERS` option and then reschedule the Task

```java
public class MyTaskListener implements TaskListener<MyTask> {

    @Inject
    private ScheduledTasks scheduledTasks;

    @Override
    public void onException(SchedulingContext schedulingContext, Exception e) {
        logger.info("Something has gone wrong");
        try {
            // Fix the problem

            // Reschedule
            scheduledTasks
                .scheduledTask(TimedTask.class)
                .withTriggerName(schedulingContext.getTriggerName());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
```

{{% callout info %}}
`Exception` handling in a `TaskListener` is called asynchronously in order to be sure to apply the `Task`'s
exception policy. Be careful in your implementation as it is impossible to know whether the `Task`'s `exceptionPolicy`
or `TaskListener`'s `onException()` method is called first.
{{% /callout %}}
