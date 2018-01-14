import javax.swing.*;
import java.awt.*;

public class ControlPanel extends JPanel {
   private final int WIDTH = 300;
   private final int HEIGHT = 800;
   
   private DisplayPanel display;
   
   public ControlPanel(DisplayPanel display) {
      setPreferredSize(new Dimension(WIDTH, HEIGHT));
      setBackground(Color.LIGHT_GRAY);
      
      this.display = display;
   }
   
   public void paint(Graphics g) {
      super.paint(g);
      
   }
   
   public void update() {
      repaint();
   }
}