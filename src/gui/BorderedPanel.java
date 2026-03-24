package gui;

import javax.swing.*;
import java.awt.*;

/**
 * Temp placeholder class to hold a bordered panel; intended to be replaced with ResizingPanel or the like.
 */
public class BorderedPanel extends JPanel
{
    public BorderedPanel()
    {
        this.setBorder(BorderFactory.createLineBorder(Color.black));
    }

    @Override
    public void setSize(Dimension d)
    {
        this.setSize((int) d.getWidth(), (int) d.getHeight());
    }

    @Override
    public void setSize (int width, int height)
    {
        super.setSize(width, height);
    }

    /**
     * Testing
     * @param args
     */
    public static void main(String[] args)
    {
        // Create JFrame (main window of application)
        JFrame frame = new JFrame("JLayeredPane Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 600);
        frame.setLayout(new BorderLayout());

//        // Create JLayeredPane to manage layering of components
//        JLayeredPane layeredPane = new JLayeredPane();
//        layeredPane.setLayout(null);
//        frame.add(layeredPane);

        // Layout
        JPanel gridPanel = new JPanel(new GridBagLayout());
        gridPanel.setPreferredSize(new Dimension(frame.getWidth(), frame.getHeight()));
        frame.add(gridPanel, BorderLayout.CENTER);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;

//        // component listener to make gridpanel always match layered pane size
//        layeredPane.addComponentListener(new java.awt.event.ComponentAdapter() {
//            @Override
//            public void componentResized(java.awt.event.ComponentEvent evt) {
//                gridPanel.setSize(layeredPane.getSize());
//            }
//        });

        JPanel[][] cells = new JPanel[3][3];
        // Create grid normally
        for (int x = 0; x < 3; x++)
            for (int y = 0; y < 3; y++)
            {
                cells[x][y] = new JPanel();
            }
        // Merge W and SW cells
        JPanel westPanel = new JPanel();
        cells[0][1] = westPanel;
        cells[0][2] = westPanel;
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

                gridPanel.add(cells[x][y], gbc);
            }

        cells[0][0].setBorder(BorderFactory.createLineBorder(Color.BLACK));
        cells[0][1].setBorder(BorderFactory.createLineBorder(Color.RED));

        frame.setVisible(true);
    }
}
