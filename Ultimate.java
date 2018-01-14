import java.awt.*;
import javax.swing.*;

public class Ultimate extends JFrame {
   private DisplayPanel display;
   
   public Ultimate() {
      super("Ultimate Tic Tac Toe");
      setLayout(new BorderLayout());
      setDefaultCloseOperation(EXIT_ON_CLOSE);
      setResizable(false);
      
      display = new DisplayPanel();
      
      add(display, BorderLayout.CENTER);
      
      pack();
      
      // Centers the window and sets it to be visible
      setLocationRelativeTo(null);
      setVisible(true);
   }
   
   public void start() {
      //while (true) {
         display.start();
      //}
   }
   
   public static void main(String[] args) {
      Ultimate ult = new Ultimate();
      ult.start();
   }
}