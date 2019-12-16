/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.scheduler.internal;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.spi.TriggerFiredBundle;
import org.seedstack.seed.SeedException;

import com.google.inject.Injector;

public class GuiceTaskFactoryTest {

    private GuiceTaskFactory underTest;

    private Injector injector;

    @Before
    public void setUp() throws Exception {
        injector = Mockito.mock(Injector.class);
        underTest = new GuiceTaskFactory(injector);
    }

    @Test
    public void testNewJob() throws Exception {
        TriggerFiredBundle bundle = Mockito.mock(TriggerFiredBundle.class);
        JobDetail detail = Mockito.mock(JobDetail.class);
        Mockito.when(detail.getKey()).thenReturn(new JobKey("key", "org.seedstack.notAClass"));
        Mockito.when(bundle.getJobDetail()).thenReturn(detail);
        try {
            underTest.newJob(bundle, null);
            Assert.fail("No exception was thrown");
        } catch (SeedException ex) {
            Assertions.assertThat(ex.getDescription()).contains("Failed to instantiate the task class 'org.seedstack.notAClass'.");
        }
    }

}
