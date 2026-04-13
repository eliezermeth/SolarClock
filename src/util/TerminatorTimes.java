package util;

import util.enums.Terminator;

import java.time.LocalTime;
import java.util.Arrays;

public class TerminatorTimes
{
    private LocalTime[] terminatorTimes = new LocalTime[3];
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
     * @param time new time to add to the end
     */
    public void increment(LocalTime time)
    {
        terminatorTimes[0] = terminatorTimes[1];
        terminatorTimes[1] = terminatorTimes[2];
        terminatorTimes[2] = time;

        startingTerminator = (startingTerminator.equals(Terminator.SUNRISE)) ? Terminator.SUNSET : Terminator.SUNRISE;
    }

    /**
     * Get the terminator stored in a certain position.
     * @param i array index of terminator to retrieve
     * @return LocalTime of terminator; null if not set or IndexOutOfBounds
     */
    public LocalTime getTerminator(int i)
    {
        try
        {
            return terminatorTimes[i];
        }
        catch (IndexOutOfBoundsException e)
        {
            return null;
        }
    }

    /**
     * Try to set a terminator at a specific index within the upcoming terminators array.
     * @param i index of terminator to set
     * @param time LocalTime of terminator
     * @return if terminator could be set; failure means index out of bounds
     */
    public boolean setTerminator(int i, LocalTime time)
    {
        try
        {
            terminatorTimes[i] = time;
            return true;
        }
        catch (IndexOutOfBoundsException e)
        {
            return false;
        }
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
        Arrays.fill(terminatorTimes, null); // reset in place to avoid creating new array
        startingTerminator = null;
    }

    /**
     * Get the time span (milliseconds) of a tekufah between the terminators.
     * @param i 0 for first tekufah, 1 for second tekufah
     * @return milliseconds of specified tekufah; -1 if invalid selection
     */
    public long getTekufahSpan(int i)
    {
        try
        {
            return TimeUtil.calculateMillisBetween(terminatorTimes[i], terminatorTimes[i + 1]);
        }
        catch (IndexOutOfBoundsException e)
        {
            return -1;
        }
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
