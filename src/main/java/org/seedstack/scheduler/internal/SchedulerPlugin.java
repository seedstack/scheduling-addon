/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.scheduler.internal;

import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.Context;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.api.plugin.request.ClasspathScanRequest;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.seedstack.scheduler.Scheduled;
import org.seedstack.scheduler.ScheduledTasks;
import org.seedstack.scheduler.Task;
import org.seedstack.scheduler.TaskListener;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.core.internal.AbstractSeedPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
import java.util.function.Predicate;

import static org.seedstack.shed.reflect.ClassPredicates.classImplements;

/**
 * Plugin for Quartz scheduler integration.
 */
@SuppressWarnings("rawtypes")
public class SchedulerPlugin extends AbstractSeedPlugin {
    private static final Logger LOGGER = LoggerFactory.getLogger(SchedulerPlugin.class);
    @Inject
    private static DelegateJobListener delegateJobListener;
    @Inject
    private static GuiceTaskFactory guiceTaskFactory;
    @Inject
    private static ScheduledTasks scheduledTasks;
    private Predicate<Class<?>> specificationForJobs;
    private Predicate<Class<?>> specificationForJobListeners;
    private Collection<Class<?>> jobClasses;
    private Multimap<Class<? extends Task>, Class<? extends TaskListener<? extends Task>>> jobListenerMap = ArrayListMultimap.create();
    private Scheduler scheduler;

    @Override
    public String name() {
        return "scheduler";
    }

    @Override
    public Collection<ClasspathScanRequest> classpathScanRequests() {
        specificationForJobs = classImplements(Task.class);
        specificationForJobListeners = classImplements(TaskListener.class);
        return classpathScanRequestBuilder()
                .predicate(specificationForJobs)
                .predicate(specificationForJobListeners)
                .build();
    }

    @Override
    @SuppressWarnings("unchecked")
    public InitState initialize(InitContext initContext) {
        Map<Predicate<Class<?>>, Collection<Class<?>>> scannedTypesByPredicate = initContext.scannedTypesByPredicate();

        // Associates - scan for nativeUnitModule
        jobClasses = scannedTypesByPredicate.get(specificationForJobs);

        Collection<Class<?>> listenerClasses = scannedTypesByPredicate.get(specificationForJobListeners);
        for (Class<?> listenerClass : listenerClasses) {
            if (TaskListener.class.isAssignableFrom(listenerClass)) {
                // Get the type of Job to listen
                Type typeVariable = getParametrizedTypeOfJobListener(listenerClass);
                if (typeVariable != null && Task.class.isAssignableFrom((Class<?>) typeVariable)) {
                    // bind the Task to the listener
                    jobListenerMap.put((Class<? extends Task>) typeVariable,
                            (Class<? extends TaskListener<? extends Task>>) listenerClass);
                }
            }
        }

        // Initialises the scheduler and adds jobs
        StdSchedulerFactory schedulerFactory = new StdSchedulerFactory();
        try {
            this.scheduler = schedulerFactory.getScheduler();
        } catch (Exception e) {
            throw SeedException.wrap(e, SchedulerErrorCode.SCHEDULER_ERROR);
        }

        return InitState.INITIALIZED;
    }

    /**
     * Returns the type parameter of the TaskListener interface.
     *
     * @param listenerClass class to check
     * @return type which extends Task
     * @throws SeedException if no parameter type is present
     */
    private Type getParametrizedTypeOfJobListener(Class<?> listenerClass) {
        Type[] interfaces = listenerClass.getGenericInterfaces();
        Type[] typeParameters = null;
        for (Type anInterface : interfaces) {
            // Checks if the class get parameters
            if (!(anInterface instanceof ParameterizedType)) {
                continue;
            }
            // Gets rawType to check if the interface is TaskListener
            Class interfaceClass = (Class) ((ParameterizedType) anInterface).getRawType();
            if (TaskListener.class.isAssignableFrom(interfaceClass)) {
                typeParameters = ((ParameterizedType) anInterface).getActualTypeArguments();
                break;
            }
        }
        if (typeParameters == null || typeParameters.length == 0) {
            throw SeedException.createNew(SchedulerErrorCode.MISSING_TYPE_PARAMETER)
                    .put("class", listenerClass);
        }

        return typeParameters[0];
    }

    @Override
    public Object nativeUnitModule() {
        return new SchedulerModule(jobClasses, scheduler, jobListenerMap);
    }

    @Override
    public void start(Context context) {
        super.start(context);

        try {
            // Configure scheduler
            scheduler.setJobFactory(guiceTaskFactory);
            scheduler.getListenerManager().addJobListener(delegateJobListener);

            // Schedule declarative tasks (@Scheduled)
            scheduleAnnotatedTasks();

            // Start scheduler
            scheduler.start();
        } catch (Exception e) {
            throw SeedException.wrap(e, SchedulerErrorCode.SCHEDULER_FAILED_TO_START);
        }
    }

    @SuppressWarnings("unchecked")
    private void scheduleAnnotatedTasks() {
        try {
            for (Class<?> candidateClass : jobClasses) {
                if (Task.class.isAssignableFrom(candidateClass)) {
                    Scheduled annotation = candidateClass.getAnnotation(Scheduled.class);
                    if (annotation != null && !Strings.isNullOrEmpty(annotation.value())) {
                        Class<? extends Task> taskClass = (Class<? extends Task>) candidateClass;
                        scheduledTasks.scheduledTask(taskClass).schedule();
                    }
                }
            }
        } catch (Exception e) {
            throw SeedException.wrap(e, SchedulerErrorCode.SCHEDULER_ERROR);
        }
    }

    @Override
    public void stop() {
        try {
            if (this.scheduler != null) {
                this.scheduler.shutdown();
            }
        } catch (SchedulerException e) {
            LOGGER.warn("Quartz scheduler failed to shutdown properly", e);
        }
        super.stop();
    }
}
