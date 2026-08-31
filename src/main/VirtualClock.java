package main;

import util.debug.DebugTimeModifications;

import java.time.*;
import java.time.temporal.TemporalAmount;

public class VirtualClock
{
    private final ZoneId zoneId;

    private ZonedDateTime baseVirtualTime;
    private Instant baseRealTime;

    private double speed = 1.0; // 1.0 = real time, 0 = pause, negative = reversed
    private double savedSpeed = 1.0;

    private TemporalAmount increment = null;

    /**
     * Create a virtual clock within a specific {@link ZoneId}.  This clock is running upon creation.
     * @param zoneId {@link ZoneId} for clock
     */
    public VirtualClock(ZoneId zoneId)
    {
        this(zoneId, true);
    }

    /**
     * Create a virtual clock within a specific {@link ZoneId}.  This clock can be paused upon creation.
     * @param zoneId {@link ZoneId} for clock
     * @param run if clock is running; {@code false} to pause the clock upon creation
     */
    public VirtualClock(ZoneId zoneId, boolean run)
    {
        this.zoneId = zoneId;

        ZonedDateTime initialTime = ZonedDateTime.now(zoneId);

        // for debugging
        if (DebugTimeModifications.DEBUG)
        {
            /*
             * ZdtOffset has precedence over TimeOffset.
             *
             * ZdtOffset establishes the complete initial ZonedDateTime, including its zone/offset.
             * TODO check if this can create conflicts if other places have the ZonedDateTime set in another zone
             */
            if (DebugTimeModifications.ZdtOffset.isEnabled())
                initialTime = DebugTimeModifications.ZdtOffset.get();
            else if (DebugTimeModifications.TimeOffset.isEnabled())
                initialTime = initialTime.plus(DebugTimeModifications.TimeOffset.get());

            /*
             * Increment has precedence over Speed.
             */
            if (DebugTimeModifications.Increment.isEnabled())
                setIncrement(DebugTimeModifications.Increment.get());
            else if (DebugTimeModifications.Speed.isEnabled())
                setSpeed(DebugTimeModifications.Speed.get());
        }

        setTime(initialTime);

        if (!run) speed = 0; // manually set to avoid time lag when calling pause()
    }

    /**
     * Core calculation for normal speed-based operation:
     * virtual = base + (elapsed * speed)
     * When increment mode is enabled, the virtual time is advanced only by explicit calls to {@link update()}.
     *
     * @return current {@code ZonedDateTime} of {@code VirtualClock}
     */
    public ZonedDateTime now()
    {
        if (increment != null)
            return baseVirtualTime;

       Instant nowReal = Instant.now();
       long elapsedMillis = Duration.between(baseRealTime, nowReal).toMillis();

       long adjustedMillis = (long) (elapsedMillis * speed);

       return baseVirtualTime.plus(Duration.ofMillis(adjustedMillis));
    }

    /**
     * Update the virtual clock.
     * In increment mode, advances the clock by exactly one increment.
     * In normal speed mode, there is nothing to do because the current virtual time is calculated from real elapsed
     * by {@link #now()}.
     */
    public void update()
    {
        if (increment == null) return;

        baseVirtualTime = baseVirtualTime.plus(increment);
        baseRealTime = Instant.now();
    }

    /**
     * Change the time by a set amount.
     *
     * @param amount {@link TemporalAmount} to change clock
     */
    public void step(TemporalAmount amount)
    {
        setBaseTime(now().plus(amount));
    }

    /**
     * Set the internal components of the clock to a new base time.
     *
     * @param time {@link ZonedDateTime} for virtual time
     */
    private void setBaseTime(ZonedDateTime time)
    {
        baseVirtualTime = time;
        baseRealTime = Instant.now();
    }

    // -----------------------------------------------------------------------------

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
     * Speed mode is disabled when an increment is configured.
     *
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
     * Enable or disable increment mode.
     * A non-null increment enables increment mode and therefore takes precedence over SPEED.
     *
     * @param newIncrement increment to apply on every update; {@code null} disables increment mode
     */
    public void setIncrement(TemporalAmount newIncrement)
    {
        this.baseVirtualTime = now();
        this.baseRealTime = Instant.now();

        this.increment = newIncrement;
    }

    /**
     * Get the configured increment of the clock (how far the clock advances each step).
     *
     * @return the increment, or {@code null} when increment mode is disabled
     */
    public TemporalAmount getIncrement()
    {
        return increment;
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
     * Set the clock to a specific {@link ZonedDateTime}.
     * Care should be taken when using this method.
     *
     * @param dateTime targeted date and time
     */
    public void setTime(ZonedDateTime dateTime)
    {
        setBaseTime(dateTime);
    }
}
