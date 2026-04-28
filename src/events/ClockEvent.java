package events;

import java.time.ZonedDateTime;

public class ClockEvent implements Comparable<ClockEvent>
{
    public enum State
    {
        PENDING,
        TRIGGERED
    }

    private final String id;
    private final String title;
    private final String description;

    private final ZonedDateTime time;
    private final String source;
    private final boolean enabled;

    private State state = State.PENDING;

    public ClockEvent(String id, String title, String description, ZonedDateTime time, String source, boolean enabled)
    {
        this.id = id;
        this.title = title;
        this.description = description;
        this.time = time;
        this.source = source;
        this.enabled = enabled;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public ZonedDateTime getTime() { return time; }
    public String getSource() { return source; }
    public boolean isEnabled() { return enabled; }

    public State getState() { return state; }

    public void markTriggered() { this.state = State.TRIGGERED; }
    public boolean isTriggered() { return state == State.TRIGGERED; }

    @Override
    public int compareTo(ClockEvent other)
    {
        int cmp =  this.time.compareTo(other.time);

        if (cmp != 0) return cmp;

        return this.id.compareTo(other.id);
    }

    @Override
    public String toString()
    {
        return id + " @ " + time + " [" + state + "]";
    }
}
