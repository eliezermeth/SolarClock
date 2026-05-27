package interfaces;

import util.enums.QuarterDayMark;

/**
 * Provides an update at midnight, sunrise, midday, and sunset.
 */
public interface QuarterDayObserver
{
    /**
     * Called when a quarter-day point (midnight, sunrise, midday, or sunset) has just occurred.
     * @param mark the period that has just occurred
     */
    void updateQuarterDay(QuarterDayMark mark);
}
