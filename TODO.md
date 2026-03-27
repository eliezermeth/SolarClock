## ClockBrain
- Potentially change the `Timer` to a `ScheduledExecutorService`.  The 
standard implementation `ScheduledThreadPoolExecutor` supports configurable 
thread pools and robust scheduling policies.  It replaces legacy classes like 
`Timer` and `TimerTask`, offering improved precision and resilience when 
tasks encounter exceptions or long execution times.  Developers typically 
obtain a `ScheduledExecutorService` instance using 
`Executors.newScheduledThreadPool(int corePoolSize)`.  Tasks can be scheduled 
via `scheduleAtFixedRate(task, initialDelay, period, unit)` for periodic 
tasks at a fixed rate.