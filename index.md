---
title: "Overview"
addon: "Scheduling"
repo: "https://github.com/seedstack/scheduling-addon"
author: "SeedStack"
description: "Provides easy-to-use support for task scheduling through Quartz."
min-version: "15.11+"
backend: true
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
Inject a `ScheduledTaskBuilderFactory` and programmatically define a scheduled task (not necessarily at application
startup) with following DSL:

## Cron expression

    @Inject
    private ScheduledTaskBuilderFactory factory;
    ...
    ScheduledTaskBuilder scheduledTaskBuilder = factory
													.createScheduledTaskBuilder(MyTask.class)
													.withCronExpression("0/2 * * * * ?");
	scheduledTaskBuilder.schedule();	
    
Note: Above cron expression implicitly defines a `Trigger`.

## With a Trigger

When a cron expression can not define the expected triggering conditions, a (Quartz) `Trigger` can be defined.

For example:

    @Inject
    private ScheduledTaskBuilderFactory factory;
    ...
    
    Trigger trigger = TriggerBuilder
		.newTrigger()
		.withIdentity(TriggerKey.triggerKey("myTrigger", "myTriggerGroup"))
		.withSchedule(SimpleScheduleBuilder.simpleSchedule()
                .withIntervalInSeconds(1)
                .repeatForever())
		.startAt(DateBuilder.futureDate(2,DateBuilder.IntervalUnit.SECOND))
		.build();
 	
 	ScheduledTaskBuilder scheduledTaskBuilder = factory
            .createScheduledTaskBuilder(MyTask.class)
            .withTrigger(trigger)
            .withPriority(10);
    scheduledTaskBuilder.schedule();



# Listeners
Create a `Class` implementing `TaskListener` in order to listen to the `Task` execution. The `Task` is bound to the
{{< java "org.seedstack.scheduler.TaskListener" >}} by declaring the `Task` as the `Type` parameter:

    public class MyTaskListener implements TaskListener<MyTask> {
        @Logging
        private Logger logger;

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
            logger.info("Something gets wrong", e);
			
			ScheduledTaskBuilder scheduledTaskBuilder = factory
                    .createScheduledTaskBuilder(MyTask.class);
												
			scheduledTaskBuilder.unschedule(sc.getTriggerName());
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
