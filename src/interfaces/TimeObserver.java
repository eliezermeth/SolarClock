package interfaces;

import java.time.LocalTime;

/**
 * Interface for when the time of a clock should be updated.
 */
public interface TimeObserver
{

    /**
     * Update the time within a class.  May also be used to repaint the panel.
     * @param time
     */
    void updateTime(LocalTime time);
}
