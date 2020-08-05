/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.scheduler.internal;

import com.google.common.collect.Multimap;
import com.google.inject.PrivateModule;
import com.google.inject.multibindings.MapBinder;
import java.util.Collection;
import java.util.Map.Entry;

import org.quartz.Scheduler;
import org.seedstack.scheduler.ScheduledTasks;
import org.seedstack.scheduler.Task;
import org.seedstack.scheduler.TaskListener;

class SchedulerModule extends PrivateModule {
    private final Collection<Class<?>> taskClasses;
    private final Multimap<Class<? extends Task>, Class<? extends TaskListener<? extends Task>>> taskListenerMap;
    private final Scheduler scheduler;

    SchedulerModule(Collection<Class<?>> taskClasses, Scheduler scheduler,
            Multimap<Class<? extends Task>, Class<? extends TaskListener<? extends Task>>> jobListenerMap) {
        this.taskClasses = taskClasses;
        this.scheduler = scheduler;
        this.taskListenerMap = jobListenerMap;
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected void configure() {
        bind(GuiceTaskFactory.class);
        bind(ScheduledTasks.class).to(ScheduledTasksImpl.class);
        bind(Scheduler.class).toInstance(scheduler);
        bind(DelegateJobListener.class);

        for (Class<?> taskClass : taskClasses) {
            bind(taskClass);
        }

        MapBinder<String, TaskListener> mapBinder = MapBinder.newMapBinder(binder(), String.class, TaskListener.class);
        mapBinder.permitDuplicates();

        for (Entry<Class<? extends Task>, Class<? extends TaskListener<? extends Task>>> taskListenerEntry : taskListenerMap
                .entries()) {
            mapBinder.addBinding(taskListenerEntry.getKey().getCanonicalName()).to(taskListenerEntry.getValue());
        }

        requestStaticInjection(SchedulerPlugin.class);
        expose(ScheduledTasks.class);
    }
}
