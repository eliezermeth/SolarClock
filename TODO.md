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
- Integrate current hour-tick mark flags from Settings into program.
- Add new text display method - `bufferedText` where it is displaced from
its exact position to avoid collisions with the circle, and `staticText` 
where the exact position (or centered) is given.
- Determine what hour tick marks to use - calculated from sunrise-sunset, 
or from the shaos zmanios.