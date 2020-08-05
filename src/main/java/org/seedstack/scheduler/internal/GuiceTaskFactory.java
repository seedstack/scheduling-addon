/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.scheduler.internal;

import com.google.inject.Injector;
import javax.inject.Inject;
import org.quartz.Job;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;
import org.seedstack.scheduler.Task;
import org.seedstack.seed.SeedException;

/**
 * This factory instantiates a {@link Job} wrapping a {@link Task}. The task will be initialized
 * with its listeners. A new Job will be created each time the associated trigger will fire.
 */
class GuiceTaskFactory implements JobFactory {

    private final Injector injector;

    @Inject
    GuiceTaskFactory(Injector injector) {
        this.injector = injector;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Job newJob(TriggerFiredBundle bundle, Scheduler scheduler) throws SchedulerException {
        // create new Job
        String taskClassName = bundle.getJobDetail().getKey().getGroup();
        try {
            return JobDelegateFactory
                    .buildJobWrapper(injector.getInstance((Class<? extends Task>) Class.forName(taskClassName)));

        } catch (Exception ex) {
            throw SeedException.wrap(ex, SchedulerErrorCode.FAILED_TO_INSTANTIATE_TASK)
                    .put("taskClass", taskClassName);
        }

    }
}
