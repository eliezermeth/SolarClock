package util;

import main.ClockBrain;

/**
 * Holds an ordered list of previous and upcoming zmanim based on those set by ZmanimOptions.  Holds values for current
 * day (24-hour time period) and next day; auto-updates.
 */
public class UpcomingZmanim
{
    private ClockBrain cb;

    public UpcomingZmanim()
    {
        cb = ClockBrain.getInstance();
    }
}
