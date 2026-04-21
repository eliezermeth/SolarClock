package util;

import util.enums.Terminator;

import java.time.Duration;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Arrays;

public class TerminatorTimes
{
    private ZonedDateTime[] terminators = new ZonedDateTime[3];
    private Terminator startingTerminator;

    /**
     * Removes the first time and shifts all current times forward.  Also flips starting terminator.
     */
    public void increment()
    {
        increment(null);
    }

    /**
     * Removes the first time and shifts all current times forward, then adds new time to end.  Also flips starting
     * terminator.
     * @param next new time to add to the end
     */
    public void increment(ZonedDateTime next)
    {
        terminators[0] = terminators[1];
        terminators[1] = terminators[2];
        terminators[2] = next;

        startingTerminator = (startingTerminator.equals(Terminator.SUNRISE)) ? Terminator.SUNSET : Terminator.SUNRISE;
    }

    /**
     * Get the terminator stored in a certain position.
     * @param i array index of terminator to retrieve
     * @return <code>ZonedDateTime</code> of terminator; null if not set or IndexOutOfBounds
     */
    public ZonedDateTime getTerminator(int i)
    {
        if (i < 0 || i >= 3) return null;
        return terminators[i];
    }

    /**
     * Try to set a terminator at a specific index within the upcoming terminators array.
     * @param i index of terminator to set
     * @param time <code>ZonedDateTime</code> of terminator
     * @return if terminator could be set; failure means index out of bounds
     */
    public boolean setTerminator(int i, ZonedDateTime time)
    {
        if (i < 0 || i >= 3) return false;

        terminators[i] = time;
        return true;
    }

    /**
     * Get the status of the first terminator stored; all other terminators alternate from there.
     * @return util.enums.Terminator of SUNRISE or SUNSET; null if not set
     */
    public Terminator getStartingTerminator()
    {
        return startingTerminator;
    }

    /**
     * Set the status of the first terminator stored.
     * @param t util.enums.Terminator of SUNRISE or SUNSET
     */
    public void setStartingTerminator(Terminator t)
    {
        startingTerminator = t;
    }

    /**
     * Clear all data in class; reset to blank slate.
     */
    public void clear()
    {
        // reset in place to avoid creating new array
        terminators[0] = null;
        terminators[1] = null;
        terminators[2] = null;

        startingTerminator = null;
    }

    /**
     * Get the time span (milliseconds) of a tekufah between the terminators.
     * @param i 0 for first tekufah, 1 for second tekufah
     * @return milliseconds of specified tekufah; -1 if invalid selection
     */
    public long getTekufahSpan(int i)
    {
        if (i < 0 || i >= 2) return -1;

        ZonedDateTime a = terminators[i];
        ZonedDateTime b = terminators[i + 1];

        return Duration.between(a, b).toMillis();
    }

    /**
     * Get the time span (milliseconds) of a shaah (halachic hour) between the terminators.  Fractional loss of
     * remaining as <code>long</code> deemed insignificant.
     * @param i 0 for first tekufah, 1 for second tekufah
     * @return milliseconds of specified tekufah shaah; -1 if invalid selection
     */
    public long getTekufahShaah(int i)
    {
        long res = getTekufahSpan(i);
        return (res != -1) ? res / 12 : -1; // divide into hours if not invalid
    }
}
