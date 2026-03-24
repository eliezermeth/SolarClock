import javax.swing.*;
import java.awt.*;

public class SandboxGuiOne
{
    public static void main(String[] args)
    {
        // Create JFrame (main window of application)
        JFrame frame = new JFrame("JLayeredPane Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 500);

        // Create JLayeredPane to manage layering of components
        JLayeredPane layeredPane = new JLayeredPane();
        frame.add(layeredPane); // add JLayeredPane to JFrame

        // Create three colored panels to add to layered pane
        JPanel panel1 = createColoredPanel(Color.RED, 100, 100, 200, 200);
        JPanel panel2 = createColoredPanel(Color.GREEN, 150, 150, 200, 200);
        JPanel panel3 = createColoredPanel(Color.BLUE, 200, 200, 200, 200);

        // Add panels to layered pane with different layer values.
        // Layers determine stacking order of the panels.
        layeredPane.add(panel1, Integer.valueOf(0));
        layeredPane.add(panel2, Integer.valueOf(1));
        layeredPane.add(panel3, Integer.valueOf(2));

        frame.setVisible(true);
    }

    private static JPanel createColoredPanel(Color color, int x, int y, int width, int height)
    {
        // Create colored JPanel with specific color and position
        JPanel panel = new JPanel();
        panel.setBackground(color);
        panel.setBounds(x, y, width, height);
        return panel;
    }
}
