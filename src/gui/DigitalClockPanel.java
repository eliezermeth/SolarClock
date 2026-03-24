package gui;

import com.kosherjava.zmanim.ComplexZmanimCalendar;
import com.kosherjava.zmanim.util.GeoLocation;
import interfaces.UpdatablePanel;
import main.Main;
import util.GeoData;
import util.Regions;

import javax.swing.*;
import java.awt.*;
import java.time.LocalTime;
import java.util.LinkedList;
import java.util.TimeZone;

/**
 * Panel to contain the digital clock and related information.  To be used as a layer of the greater program.
 * Individual cells will be used by other classes to create the smaller digital clock parts.
 */
public class DigitalClockPanel extends JPanel implements UpdatablePanel
{
    private Main clock;
    private boolean
            standardClockEnabled = false,
            halachicClockEnabled = false,
            conversionTableEnabled = false,
            upcomingTimesEnabled = false;
    private JPanel[][] cells; // to hold cells that used by elements
    private JPanel standardClockPanel, halachicClockPanel, conversionTablePanel, upcomingTimesPanel;
    private LinkedList<UpdatablePanel> updatablePanelsList = new LinkedList<>();

    // Elements to be updated
    // Standard clock:
    private JLabel[] standardTimeComponents = new JLabel[3];

    /**
     *
     * @param clock
     */
    public DigitalClockPanel(Main clock)
    {
        this.clock = clock;

        this.setLayout(new GridBagLayout());
        //gridPanel.setPreferredSize(new Dimension(frame.getWidth(), frame.getHeight()));
        //frame.add(gridPanel, BorderLayout.CENTER);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;

        cells = new JPanel[3][3]; // change code to allow for multiple?

        // Create grid normally
        for (int x = 0; x < 3; x++)
            for (int y = 0; y < 3; y++)
            {
                cells[x][y] = new JPanel();
            }

        // Merge W and SW cells
        cells[0][2] = cells[0][1]; // make SW pointer use W cell

        // Set constraints for all cells
        for (int x = 0; x < 3; x++)
            for (int y = 0; y < 3; y++)
            {
                if (y > 0 && cells[x][y] == cells[x][y-1]) // if same as cell above
                    continue;

                gbc.gridx = x; gbc.gridy = y;
                gbc.gridwidth = 1; gbc.gridheight = 1;
                gbc.weightx = 1; gbc.weighty = 1;

                if (y < 2 && cells[x][y] == cells[x][y+1]) // detect vertical span
                    gbc.gridheight = 2;

                this.add(cells[x][y], gbc);
            }

        // Name used cells
        standardClockPanel = cells[0][0];
        halachicClockPanel = cells[2][0];
        conversionTablePanel = cells[0][2];
        upcomingTimesPanel = cells[2][2];

        // for testing
        cells[0][0].setBorder(BorderFactory.createLineBorder(Color.BLACK));
        cells[0][1].setBorder(BorderFactory.createLineBorder(Color.RED));
    }

    private void createStandardClock()
    {
        StandardClockPanel scp = new StandardClockPanel(clock, this, standardClockPanel);
        addUpdatablePanel(scp);
    }

    private void createHalachicClock()
    {
        HalachicClockPanel hcp = new HalachicClockPanel(clock, this, halachicClockPanel);
        addUpdatablePanel(hcp);
    }

    private void createConversionTable()
    {

    }

    private void createUpcomingTimes()
    {

    }

    private void addUpdatablePanel(UpdatablePanel panel)
    {
        updatablePanelsList.add(panel);
    }

    @Override
    public void update()
    {
        for (UpdatablePanel panel : updatablePanelsList)
            panel.update();
    }

    // --------------------------------------------------------------
    // Setters / Getters
    // --------------------------------------------------------------

    // TODO modify so has direct reference to cells
    public void setStandardClockEnabled(boolean enabled)
    {
        this.standardClockEnabled = enabled;

        if (standardClockEnabled && standardClockPanel.getComponentCount() == 0) // create when needed and avoid duplicates
            createStandardClock();

        standardClockPanel.setVisible(standardClockEnabled);

    }
    public boolean isStandardClockEnabled() { return standardClockEnabled; }

    public void setHalachicClockEnabled(boolean enabled)
    {
        this.halachicClockEnabled = enabled;

        if (halachicClockEnabled && halachicClockPanel.getComponentCount() == 0) // create when needed and avoid duplicates
            createHalachicClock();

        halachicClockPanel.setVisible(halachicClockEnabled);
    }
    public boolean isHalachicClockEnabled() { return halachicClockEnabled; }

    public void setConversionTableEnabled(boolean enabled)
    {
        this.conversionTableEnabled = enabled;

        if (conversionTableEnabled && conversionTablePanel.getComponentCount() == 0) // create when needed and avoid duplicates
            createConversionTable();

        conversionTablePanel.setVisible(conversionTableEnabled);
    }
    public boolean isConversionTableEnabled() { return conversionTableEnabled; }

    public void setUpcomingTimesEnabled(boolean enabled)
    {
        this.upcomingTimesEnabled = enabled;

        if (upcomingTimesEnabled && upcomingTimesPanel.getComponentCount() == 0) // create when needed and avoid duplicates
            createUpcomingTimes();

        upcomingTimesPanel.setVisible(upcomingTimesEnabled);
    }
    public boolean isUpcomingTimesEnabled() { return upcomingTimesEnabled; }


    public static void main(String[] args)
    {
        // set up clock
        GeoData location = Regions.getLocation("Pikesville");
        Main clock = new Main(new ComplexZmanimCalendar(
                new GeoLocation(
                        location.getName(), location.getLatitude(), location.getLongitude(),
                        TimeZone.getTimeZone(location.getRegion())
                )
        ));
        // TODO time progression, tekufah flips?

        // Create JFrame (main window of application)
        JFrame frame = new JFrame("JLayeredPane Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 600);
        frame.setLayout(new BorderLayout());

        // Create DigitalClockPanel
        DigitalClockPanel dcp = new DigitalClockPanel(clock);
        //dcp.setPreferredSize(new Dimension(frame.getWidth(), frame.getHeight()));
        frame.add(dcp, BorderLayout.CENTER);

        // set components to visible
        dcp.setStandardClockEnabled(true);
        dcp.setHalachicClockEnabled(true);

        frame.setVisible(true);
    }
}
