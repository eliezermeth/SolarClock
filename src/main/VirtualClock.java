package main;

import util.DebugTimeModifications;

import java.time.*;

public class VirtualClock
{
    private final ZoneId zoneId;

    private ZonedDateTime baseVirtualTime;
    private Instant baseRealTime;

    private double speed = 1.0; // 1.0 = real time, 0 = pause, negative = reversed
    private double savedSpeed = 1.0;

    public VirtualClock(ZoneId zoneId)
    {
        this.zoneId = zoneId;
        this.baseVirtualTime = ZonedDateTime.now(zoneId);
        this.baseRealTime = Instant.now();

        // for debugging
        if (DebugTimeModifications.DEBUG)
        {
            // change offset first, so speed is only applied after that change
            if (DebugTimeModifications.OFFSET.enabled)
                offset(DebugTimeModifications.OFFSET.duration);
            if (DebugTimeModifications.SPEED.enabled)
                setSpeed(DebugTimeModifications.SPEED.speed);
        }
    }

    /**
     * Core calculation: virtual = base + (elapsed * speed)
     * @return current {@code ZonedDateTime} of {@code VirtualClock}
     */
    public ZonedDateTime now()
    {
       Instant nowReal = Instant.now();
       long elapsedMillis = Duration.between(baseRealTime, nowReal).toMillis();

       long adjustedMillis = (long) (elapsedMillis * speed);

       return baseVirtualTime.plus(Duration.ofMillis(adjustedMillis));
    }

    public LocalTime getLocalTime()
    {
        return now().toLocalTime();
    }

    public LocalDate getLocalDate()
    {
        return now().toLocalDate();
    }

    // -----------------------------------------------------------------------------

    /**
     * Change speed the clock runs while preserving current virtual time.
     * @param newSpeed 1.0 = real time, 0 = pause, negative = reversed
     */
    public void setSpeed(double newSpeed)
    {
        this.baseVirtualTime = now();
        this.baseRealTime = Instant.now();

        this.speed = newSpeed;
        if (newSpeed != 0.0) // if speed not set to paused, update the saved speed
            this.savedSpeed = speed;
    }

    /**
     * Get the current speed of the clock.
     * @return {@code double} where 1.0 = real time, 0 = pause, negative = reversed
     */
    public double getSpeed()
    {
        return speed;
    }

    /**
     * Gets the saved speed of the clock; useful for when clock is paused and want to see speed clock had been running
     * at.
     * @return {@code double} of speed of clock when not paused; 1.0 = real time, 0 = pause, negative = reversed
     */
    public double getSavedSpeed()
    {
        return savedSpeed;
    }

    /**
     * If the clock is currently paused.
     * @return paused status of clock.
     */
    public boolean isPaused()
    {
        return speed == 0.0;
    }

    /**
     * Pause the running of the clock; the previous speed is saved.
     */
    public void pause()
    {
        setSpeed(0.0);
    }

    /**
     * Resume running the clock at the previous saved speed.
     */
    public void resume()
    {
        setSpeed(savedSpeed);
    }

    // -----------------------------------------------------------------------------

    /**
     * Jump forward/backward in virtual time.
     * @param duration amount to offset the current time of the clock.
     */
    public void offset(Duration duration)
    {
        baseVirtualTime = now().plus(duration);
        baseRealTime = Instant.now();
    }

    /**
     * Set the clock to a specific <code>ZonedDateTime</code>
     * @param dateTime targeted date and time.
     */
    public void setTime(ZonedDateTime dateTime)
    {
        this.baseVirtualTime = dateTime;
        this.baseRealTime = Instant.now();
    }
}
