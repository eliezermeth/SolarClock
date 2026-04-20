## ClockBrain
- Potentially change the `Timer` to a `ScheduledExecutorService`.  The 
standard implementation `ScheduledThreadPoolExecutor` supports configurable 
thread pools and robust scheduling policies.  It replaces legacy classes like 
`Timer` and `TimerTask`, offering improved precision and resilience when 
tasks encounter exceptions or long execution times.  Developers typically 
obtain a `ScheduledExecutorService` instance using 
`Executors.newScheduledThreadPool(int corePoolSize)`.  Tasks can be scheduled 
via `scheduleAtFixedRate(task, initialDelay, period, unit)` for periodic 


## GridRegionPanel
- Add methods to allow querying of current regions and remove regions.

## AnalogClockPanel
- Add new text display method - `bufferedText` where it is displaced from
its exact position to avoid collisions with the circle, and `staticText` 
where the exact position (or centered) is given.
- Determine what hour tick marks to use - calculated from sunrise-sunset, 
or from the shaos zmanios (alos-tzeis?).
- Nightly hour marks are not properly spaced during night.

##
- TimeScheduler may not deal well with the debugging portion due to offset
time


## General
- Does not calculate properly when the clock is changed (e.g. between 
standard and daylight savings time).