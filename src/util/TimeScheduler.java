package util;

import main.VirtualClock;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Schedule a task to be run at a specific time.
 * TODO DEPRECIATED -- to be removed as soon as possible
 */
public class TimeScheduler
{
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final VirtualClock clock;

    public TimeScheduler(VirtualClock clock)
    {
        this.clock = clock;
    }


    /**
     * Run a task at a specific time.
     * @param time LocalTime of when to run the task; will be adjusted to proper day.
     * @param task Task to be run.
     */
    public void schedule(LocalTime time, Runnable task)
    {
        long delay = calculateDelay(time);

        scheduler.schedule(task::run, delay, TimeUnit.MILLISECONDS);
    }

    /**
     * Run a repeating task.  First time starts at <code>firstTime</code>, then get next time via calls to the method
     * given to <code>nextTimeSupplier</code>.
     * @param firstTime Time to run task (for first time).
     * @param task Task to be run repeatedly.
     * @param nexTimeSupplier Method to get time for next running.
     */
    public void scheduleRepeat(LocalTime firstTime, Runnable task, Supplier<LocalTime> nexTimeSupplier)
    {
        scheduler.schedule(() -> {
            task.run();

            LocalTime nextTime = nexTimeSupplier.get();
            if (nextTime != null) // protect against null
                scheduleRepeat(nextTime, task, nexTimeSupplier);
        }, calculateDelay(firstTime), TimeUnit.MILLISECONDS);
    }

    /**
     * Calculate the delay (in milliseconds) to execute a task at a specific time, from now.
     * @param targetTime LocalTime of task to be run.
     * @return milliseconds from now until <code>targetTime</code>
     */
    private long calculateDelay(LocalTime targetTime)
    {
        LocalDateTime now = clock.now().toLocalDateTime();
        // following two lines for debugging purposes
        if (DebugTimeModifications.DEBUG && DebugTimeModifications.OFFSET.enabled)
        {
            now = now.plusSeconds(DebugTimeModifications.OFFSET.duration.toSeconds());
        }

        LocalDateTime target = now.with(targetTime);

        if (target.isBefore(now))
            target = target.plusDays(1); // if target is before now, make tomorrow

        return Duration.between(now, target).toMillis();
    }

    /**
     * Kill scheduler.
     */
    public void shutdown()
    {
        scheduler.shutdown();
    }

    /**
     * For testing.
     * @param args
     */
    public static void main(String[] args)
    {
//        TimeScheduler scheduler = new TimeScheduler();
//        scheduler.schedule(LocalTime.now().plusSeconds(10), () -> {
//            System.out.println("Task 1");
//        });
//        scheduler.schedule(LocalTime.now().plusSeconds(15), () -> {
//            System.out.println("Task 2");
//        });
//        scheduler.shutdown();
    }
}
