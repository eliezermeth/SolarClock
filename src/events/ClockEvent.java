package events;

import util.enums.Zman;

import java.time.ZonedDateTime;

public class ClockEvent implements Comparable<ClockEvent>
{
    public enum State
    {
        PENDING,
        TRIGGERED
    }

    private final Zman zman;

    private final ZonedDateTime time;
    private final boolean enabled;

    private State state = State.PENDING;

    public ClockEvent(Zman zman, ZonedDateTime time, boolean enabled)
    {
        this.zman = zman;
        this.time = time;
        this.enabled = enabled;
    }

    public Zman getZman() { return zman; }
    public String getTitle() { return zman.getTitle(); }
    public String getDescription() { return zman.getDescription(); }
    public ZonedDateTime getTime() { return time; }
    public boolean isEnabled() { return enabled; }

    public State getState() { return state; }

    public void markTriggered() { this.state = State.TRIGGERED; }
    public boolean isTriggered() { return state == State.TRIGGERED; }

    @Override
    public int compareTo(ClockEvent other)
    {
        int cmp =  this.time.compareTo(other.time);

        if (cmp != 0) return cmp;

        return this.zman.compareTo(other.zman);
    }

    @Override
    public String toString()
    {
        return zman + " @ " + time + " [" + state + "]";
    }
}
