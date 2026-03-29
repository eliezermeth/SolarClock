package gui;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Panel to contain the digital clock and related information.  To be used as a layer of the greater program.
 * Individual cells will be used by other classes to create the smaller digital clock parts.
 */
public class DigitalClockPanel extends JPanel
{
    private boolean
            standardClockEnabled = false,
            halachicClockEnabled = false,
            conversionTableEnabled = false,
            upcomingTimesEnabled = false;
    private final JPanel
            standardClockPanel = new JPanel(),
            halachicClockPanel = new JPanel(),
            conversionTablePanel = new JPanel(),
            upcomingTimesPanel = new JPanel();

    // Grid size; can be changed to allow different scales
    private final int cols = 15;
    private final int rows = 10;

    private final ArrayList<GridRegion> regions = new ArrayList<>();
    private boolean[][] occupied; // tracks which cells are already taken

    /**
     *
     */
    public DigitalClockPanel()
    {
        buildLayout();
        buildRegions(); // user-defined regions
        validateRegions();
        fillEmptyRegions(); // optional
        applyRegions();
        debugBorders(); // optional
    }

    /**
     * Layout setup.
     */
    private void buildLayout()
    {
        GridBagLayout gbl = new GridBagLayout();

        gbl.rowWeights = new double[rows];
        gbl.columnWeights = new double[cols];
        gbl.rowHeights = new int[rows];
        gbl.columnWidths = new int[cols];

        Arrays.fill(gbl.rowWeights, 1.0);
        Arrays.fill(gbl.columnWeights, 1.0);

        this.setLayout(gbl);
    }

    /**
     * Build needed regions of the layout, using wrapper panels.
     */
    private void buildRegions()
    {
        regions.clear();

        // add clock regions
        regions.add(new GridRegion(1, 1, 3, 2, buildZeroingWrapper(standardClockPanel)));
        regions.add(new GridRegion(11, 1, 3, 2, buildZeroingWrapper(halachicClockPanel)));

        // taller regions
        regions.add(new GridRegion(1, 5, 3, 4, new JPanel())); // upcomingTimesPanel
        regions.add(new GridRegion(11, 5, 3, 4, new JPanel())); // conversionTablePanel
    }

    /**
     * Build a wrapper panel for another panel.  The wrapper will not influence <code>GridBagLayout</code>'s expanding
     * feature to increase the size the panel takes, but will remain within its preset bounds.
     * @param panel <code>JPanel</code> to be placed inside wrapper
     * @return wrapper <code>JPanel</code>
     */
    private JPanel buildZeroingWrapper(JPanel panel)
    {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(panel, BorderLayout.CENTER);

        // set minimum/preferred size to 0, 0 to prevent it from expanding the GridBagLayout
        wrapper.setPreferredSize(new Dimension(0, 0));
        wrapper.setMinimumSize(new Dimension(0, 0));

        return wrapper;
    }

    /**
     * Validate no regions overlap or go out of bounds, and mark occupied cells.
     */
    private void validateRegions()
    {
        occupied = new boolean[cols][rows];

        for (GridRegion r : regions)
        {
            for (int x = r.x; x < r.x + r.width; x++)
            {
                for (int y = r.y; y < r.y + r.height; y++)
                {
                    if (x >= cols || y >= rows)
                        throw new IllegalStateException("Region out of bounds: (" + x + "," + y + ")");

                    if (occupied[x][y])
                        throw new IllegalStateException("Overlapping region at: (" + x + "," + y + ")");

                    occupied[x][y] = true;
                }
            }
        }
    }

    /**
     * For debugging; adds regions to the unused sections of the grid.
     */
    private void fillEmptyRegions()
    {
        for (int x = 0; x < cols; x++)
            for (int y = 0; y < rows; y++)
                if (!occupied[x][y]) // if cell is not occupied, create 1x1 region
                {
                    GridRegion filler = new GridRegion(x, y, 1, 1, new JPanel());
                    regions.add(filler);
                    occupied[x][y] = true; // mark slot as currently occupied
                }
    }

    /**
     * Add regions to panel.
     */
    private void applyRegions()
    {
        for (GridRegion r : regions)
        {
            GridBagConstraints gbc = new GridBagConstraints();

            gbc.gridx = r.x; gbc.gridy = r.y;
            gbc.gridwidth = r.width; gbc.gridheight = r.height;

            gbc.weightx = 1.0; gbc.weighty = 1.0;
            gbc.fill = GridBagConstraints.BOTH;

            this.add(r.panel, gbc);
        }
    }

    /**
     * For debugging; adds a border around each region.
     */
    private void debugBorders()
    {
        for (GridRegion r : regions)
            r.panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
    }

    /// --------------------------------------------------------------
    // Component Creation
    // --------------------------------------------------------------

    private void createStandardClock()
    {
        new StandardClockPanel(this, standardClockPanel);
    }

    private void createHalachicClock()
    {
        new HalachicClockPanel( this, halachicClockPanel);
    }

    private void createConversionTable()
    {

    }

    private void createUpcomingTimes()
    {

    }

    // --------------------------------------------------------------
    // Setters / Getters
    // --------------------------------------------------------------

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
        // Create JFrame (main window of application)
        JFrame frame = new JFrame("JLayeredPane Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 600);
        frame.setLayout(new BorderLayout());

        // Create DigitalClockPanel
        DigitalClockPanel dcp = new DigitalClockPanel();
        //dcp.setPreferredSize(new Dimension(frame.getWidth(), frame.getHeight()));
        frame.add(dcp, BorderLayout.CENTER);

        // set components to visible
        dcp.setStandardClockEnabled(true);
        dcp.setHalachicClockEnabled(true);

        frame.setVisible(true);
    }
}
