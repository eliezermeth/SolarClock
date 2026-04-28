package interfaces;

import events.ClockEvent;

/**
 * Interface for when a zman event view should be updated, due to the event changing.
 */
public interface ZmanEventObserver
{
    /**
     * Update the zman event viewed, due to the upcoming event changing.
     * @param event the new upcoming event
     */
    void updateZmanEvent(ClockEvent event);
}
