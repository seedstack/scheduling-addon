/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.scheduler.internal;

import org.seedstack.shed.exception.ErrorCode;

enum SchedulerErrorCode implements ErrorCode {
    EXCEPTION_IN_LISTENER,
    FAILED_TO_INSTANTIATE_TASK,
    IMPOSSIBLE_TO_USE_CRON_AND_TRIGGER,
    MISSING_CRON_EXPRESSION,
    MISSING_TYPE_PARAMETER,
    SCHEDULER_ERROR,
    SCHEDULER_FAILED_TO_START,
    TRIGGER_AND_JOB_NAME_SHOULD_BE_UNIQUE,
    UNABLE_TO_UNWRAP,
    UNRECOGNIZED_TRIGGER
}
