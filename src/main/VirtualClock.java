package main;

import java.time.*;

public class VirtualClock
{
    private final ZoneId zoneId;

    private ZonedDateTime baseVirtualTime;
    private Instant baseRealTime;

    private double speed = 1.0; // 1.0 = real time, 0 = pause, negative = reversed

    private ZonedDateTime current;

    private boolean realtime = true;
    private boolean paused = false;

    private long tickSeconds = 1;

    public VirtualClock(ZoneId zoneId)
    {
        this.zoneId = zoneId;
        this.current = ZonedDateTime.now(zoneId);
    }

    public ZonedDateTime now()
    {
        return current;
    }

    public LocalTime getLocalTime()
    {
        return current.toLocalTime();
    }

    public LocalDate getLocalDate()
    {
        return current.toLocalDate();
    }

    public void tick()
    {
        if (paused)
            return;

        if (realtime)
            current = ZonedDateTime.now(zoneId);
        else
            current = current.plusSeconds(tickSeconds); // revise - this only ticks once per second; may need from settings
    }

    public void setRealTime(boolean realtime)
    {
        this.realtime = realtime;
    }
    public void setPaused(boolean paused)
    {
        this.paused = paused;
    }
    public boolean getPaused()
    {
        return paused;
    }
    public void setTickSeconds(long seconds)
    {
        this.tickSeconds = seconds;
    }

    public void offsetHours(long hours)
    {
        current = current.plusHours(hours);
    }

    public void offsetMinutes(long minutes)
    {
        current = current.plusMinutes(minutes);
    }

    public void offsetSeconds(long seconds)
    {
        current = current.plusSeconds(seconds);
    }
}
