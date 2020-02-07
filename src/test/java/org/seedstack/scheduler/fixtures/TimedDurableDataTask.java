/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.scheduler.fixtures;

import java.util.Map;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.PersistJobDataAfterExecution;
import org.seedstack.scheduler.SchedulingContext;
import org.seedstack.scheduler.Task;

@DisallowConcurrentExecution
@PersistJobDataAfterExecution
public class TimedDurableDataTask implements Task {

	public static int fireCount = 0;

	@SuppressWarnings("unchecked")
	@Override
	public void execute(SchedulingContext sc) throws Exception {

		Integer count = ((Map<String, Integer>) sc.getDataMap()).getOrDefault("count", 0);
		((Map<String, Integer>) sc.getDataMap()).put("count", count + 1);

		fireCount = ((Map<String, Integer>) sc.getDataMap()).getOrDefault("count", 0);

	}

}
