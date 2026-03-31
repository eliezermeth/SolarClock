package gui;

import com.kosherjava.zmanim.ComplexZmanimCalendar;
import main.ClockBrain;

import javax.swing.*;
import java.util.Date;

public class UpcomingTimesPanel
{
    private ClockBrain clock;
    private ComplexZmanimCalendar czc;
    private JPanel panel;

    /**
     * Constructor.
     * @param panel
     */
    public UpcomingTimesPanel(JPanel panel)
    {
        this.clock = ClockBrain.getInstance();
        czc = clock.getComplexZmanimCalendar();
        this.panel = panel;
    }

    public static void main(String[] args)
    {
        ClockBrain clock = ClockBrain.getInstance();
        ComplexZmanimCalendar czc = clock.getComplexZmanimCalendar();

        // matching from myzmanim.com; times appear to be ~10 second earlier due to location
        printTimeGroup("Dawn: 72 minutes at 16.1 degrees", czc.getAlos16Point1Degrees());
        printTimeGroup("Earliest talis & tefillin (sun is 10.2 degrees below horizon)", czc.getMisheyakir10Point2Degrees());
        printTimeGroup("Sunrise", czc.getSunrise());
        printTimeGroup("SZK\"Sh'ma (M\"A): (72 minutes at) 16.1 degrees", czc.getSofZmanShmaMGA16Point1Degrees());
        printTimeGroup("SZK\"Sh'ma  (Gra & Baal HaTanya)", czc.getSofZmanShmaGRA());
        // check below here
        printTimeGroup("SZ\"Tfila (Gra & Ball HaTanya)", czc.getSofZmanTfilaGRA());
        printTimeGroup("Midday", czc.getChatzos());
        printTimeGroup("Earliest mincha (lechumra)", czc.getMinchaGedola());
        printTimeGroup("Plag Ha\"Mincha (Gra & Baal HaTanya)", czc.getPlagHamincha());
        printTimeGroup("Sunset", czc.getSunset());
        printTimeGroup("Nightfall - 3 stars emerge (36 minutes as degrees)", czc.getTzais());
        printTimeGroup("Nightfall - 72 minutes", czc.getTzais72());
    }

    public static void printTimeGroup(String name, Date time)
    {
        System.out.println(name);
        System.out.println(time);
        System.out.println();
    }
}
