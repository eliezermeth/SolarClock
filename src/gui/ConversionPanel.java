package gui;

import main.ClockBrain;
import util.SolarTimes;
import util.TimeConverter;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class ConversionPanel
{
    private final JPanel panel;

    private TimeConverter timeConverter;
    private final JLabel[][] table = new JLabel[6][2];

    public ConversionPanel(JPanel panel)
    {
        this.panel = panel;

        ClockBrain clock = ClockBrain.getInstance();
        ZonedDateTime now = clock.getCurrentDateTime();
        SolarTimes solarTimes = clock.getSolarTimes();
        // as of now, all tekufos are the 12-hour variety; will need to allow change
        timeConverter = new TimeConverter(solarTimes.getTekufahStart(now), solarTimes.getTekufahEnd(now), 12);
        // TODO refresh converter quarterly

        panel.setLayout(new GridLayout(6, 2)); // but layout already set?

        Border border = BorderFactory.createLineBorder(Color.GRAY);

        for (int row = 0; row < table.length; row++)
        {
            for (int col = 0; col < table[row].length; col++)
            {
                table[row][col] = new JLabel("", SwingConstants.CENTER);
                table[row][col].setBorder(border);
                panel.add(table[row][col]);
            }
        }

        setHalachicTimeOnLeft();
    }

    public void setStandardTimeOnLeft()
    {
        clearLabels();

        table[0][0].setText("Standard");
        table[0][1].setText("Halachic");

        table[1][0].setText("");
        table[1][1].setText("");

        table[2][0].setText("1 hour");
        table[2][1].setText("");

        table[3][0].setText("1 minute");
        table[3][1].setText("");

        table[4][0].setText("1 second");
        table[4][1].setText("");

        table[5][0].setText("");
        table[5][1].setText("");
    }

    public void setHalachicTimeOnLeft()
    {
        clearLabels();

        table[0][0].setText("Halachic");
        table[0][1].setText("Standard");

        table[1][0].setText("Tekufah");
        table[1][1].setText(timeConverter.getDuration().toString());

        table[2][0].setText("1 hour");
        table[2][1].setText(timeConverter.getHalachicHourLength().toString());

        table[3][0].setText("1 minute");
        table[3][1].setText(timeConverter.getHalachicMinuteLength().toString());

        table[4][0].setText("1 second");
        table[4][1].setText(timeConverter.getHalachicSecondLength().toString());

        table[5][0].setText("1 cheilek");
        table[5][1].setText(timeConverter.getHalachicCheilekLength().toString());
    }

    /**
     * Clear the text in all {@link JLabel}s of the table.
     */
    private void clearLabels()
    {
        for (JLabel[] jLabels : table)
            for (JLabel jLabel : jLabels)
                jLabel.setText("");
    }

    /**
     * Changes a {@link Duration} into a {@link String} of a more human-readable format.  The formatting can work with
     * the following segments: Hours, minutes, seconds, and fractions of seconds. An example of the requested format is
     * {@code H:MM:SS.n}.  The highest unit displayed (be it hours, minutes, or seconds) can have any number of units;
     * after that, the display will standardize to two units for each of the remainder to be displayed.  The fractional
     * units ({@code n}) is different; the exact rule is explained below.
     * <p>
     * Sections (between hours, minutes, and seconds) must always be separated by a semicolon ({@code :}).  The
     * separation between seconds and fractional parts is delineated by a dot/period ({@code .}).  The highest time
     * section it will print is to the left of the first semicolon; after that, all remaining sections must be fully
     * specified.
     * <p>
     * The highest section it can print is hours.  The number of {@code H}s is the minimum field width used for the
     * display; as such, {@code HH:} will cause a minimum field width of two (which may cause sub-ten hours to be
     * displayed as {@code 03:...}).  However, if this section should only display if there is at least one unit, then
     * {@code :} should be used.  If there do exist units, then it will be formatted with no leading zeros.
     * <p>
     * The next possible section is minutes.  This follows the same rules as does hours; however, if hours are a certain
     * display ({@code H:} used), then there must be two units for minutes {@code MM:}).  If hours are an uncertain
     * display ({@code :} was used), then there are three options: {@code MM} if there should be two digits used for
     * minutes, {@code M} if this may be the highest displayed (and should display) and will use a variable-width
     * minimum field length, or {@code :} if it should only display if it is a non-zero number.
     * <p>
     * The next possible section is seconds ({@code S}); the same rules apply as for minutes.
     * <p>
     * The decimal point ({@code .}) is not required after seconds; however, if it was used, remaining fractional points
     * may be displayed.  Fractional parts ({@code n}) operate on a different rule.  The character {@code n} is not
     * input; rather, a {@code 1} will specify that one decimal place should always be displayed, {@code 2} will
     * display the hundredths, up to the maximum {@code 9}, for nanoseconds, the smallest unit stored by
     * {@link Duration}.  A {@code +} may be appended after the {@code n} to allow the field to increase to include all
     * units stored, up to nanoseconds.  If the decimal point ({@code .}) is used but no {@code n} is entered, it will
     * only display the dot if fractional points exist, and will use the value stored as the entire field (with no
     * leading zeros; thus, {@code .100 = .1}).  A {@code 0} may be used to force the dot to display without a number;
     * {@code 0+} forces the display and shows existing fractional points.
     * <p>
     * If the {@code format} leads with {@code :}, it must then specify the next unit (minutes or seconds) to be
     * displayed (e.g. {@code :MM}, use {@code ::} (which covers all fields of hours, minutes, and seconds), or
     * {@code :.}, where the decimal point determines the {@code :} to be {@code minutes:seconds}.
     *
     * @param duration the {@link Duration} to be formatted to a {@link String}
     * @param format the format for {@link Duration}, following the above rules
     * @return {@link String}-formatted {@link Duration}
     */
    private String prettyDuration(Duration duration, String format)
    {
        Instant instant = Instant.EPOCH.plus(duration);
        ZonedDateTime zdt = instant.atZone(ZoneOffset.UTC);
        String formatted = DateTimeFormatter.ofPattern(format).format(zdt);

        return null;
    }

    public static void main(String[] args)
    {
        // Create JFrame (main window of application)
        JFrame frame = new JFrame("ConversionPanel Test");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(300, 600);
        frame.setLayout(new BorderLayout());
        JPanel panel = new JPanel(new BorderLayout());
        new ConversionPanel(panel);
        frame.add(panel, BorderLayout.CENTER);
        frame.setVisible(true);
    }
}
