# Version 3.2.0 (2019-12-19)

* [chg] Updated for SeedStack 19.11
* [chg] Updated Quartz to 2.3.2

# Version 3.1.1 (2018-08-22)

* [new] Adds the capability to reschedule/unschedule a task by its trigger key and group.
* [brk] withTrigger now receives Trigger instead of Object 

# Version 3.1.0 (2018-04-28)

* [new] Add method to specify task data. 
* [new] Add method on `SchedulingContext` to access the underlying Quartz implementation context. 
* [chg] Updated Quartz to 2.3.0 which is now pulled transitively (scope was `provided` before). 
* [brk] Remove method to un-schedule task by its trigger key (was Quartz specific). 

# Version 3.0.0 (2017-01-03)

* [new] Add a method to un-schedule a task by its trigger key. 
* [brk] Update to new configuration system.

# Version 2.2.0 (2016-08-10)

* [new] Add the ability to substitute `@Scheduling` attributes `value`, `taskName` and `triggerName` with configuration values via `${}` placeholder.

# Version 2.1.1 (2016-04-26)

* [chg] Update for SeedStack 16.4.

# Version 2.1.0 (2015-11-26)

* [chg] Refactored as an add-on and updated to work with Seed 2.1.0+

# Version 2.0.0 (2015-07-30)

* [new] Initial Open-Source release.
