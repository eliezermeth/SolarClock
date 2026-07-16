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
- Add lines to cover small gaps between different arc sections.

## ClockEventManager
- The lines similar to `if (ClockEvent == null)` may cause problem in the
future because certain events may not occur every day, and the event 
should still be added tested for the next-day calculation.

##
- TimeScheduler may not deal well with the debugging portion due to offset
time


## General
- Does not calculate properly when the clock is changed (e.g. between 
standard and daylight savings time).
- Add section for where between alos and sunrise and same for night (new
color?).
- Certain elements (such as `ClockEvents`) may break if clock is run in 
reverse.
- Rewrite sections in `README`, `AnalogClockPanel`, etc. to accommodate 
for new changes to sunrise/sunset calculations.
- At the current moment, AnalogClockPanel (and HalachicTimes) are using 
uniform periods within segments (that is, it calculates midday at the 
middle time between sunrise and sunset), and not the astronomical times. 
Perhaps split into UNIFORM_SUNDIAL and ASTRONOMICAL_SUNDIAL (where the sun 
being at its zenith is at the top of the clock).

- A time offset to the next [day? / period?] causes the following error:
```
Exception in thread "main" java.util.NoSuchElementException
  at java.base/java.util.ArrayList.getFirst(ArrayList.java:439)
  at events.IndexedSet.peek(IndexedSet.java:47)
  at events.ClockEventManager.initialize(ClockEventManager.java:83)
  at events.ClockEventManager.<init>(ClockEventManager.java:39)
  at main.ClockBrain.<init>(ClockBrain.java:64)
  at main.ClockBrain.getInstance(ClockBrain.java:78)
  at gui.AnalogClockPanel.<init>(AnalogClockPanel.java:71)
  at main.Main.main(Main.java:52)
```
Likely due to the `ClockEventManager` not having a properly modified date.
Need to follow the offset and creation.



gui.ZmanClockGUI:
- Class Javadoc:
    - update terms used
    - update description
- Make labels no longer overlap other elements
- make current time hand (and all others) no longer overlap other elements (i.e. bounding circle)
- currently uses getSunrise/Sunset for delineations; change?
    - higher-order functions?
    - other time for delineation?
- when the colored sections (and zmanim) rotate
- all: when repaint triggers; for one or all