import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.util.ArrayList;

public class DisplayPanel extends JPanel {
   private final int WIDTH = 800;
   private final int HEIGHT = 800;
   
   private boolean playerOneTurn;
   private int[][] macroboard;
   private int[][] board;
   
   private int offset = 100;
   private int localBoardSize = 168;
   private int gap = (WIDTH - 2 * offset - 3 * localBoardSize) / 2;
   private int tileSize = localBoardSize / 3;
   
   private int wins = 0, losses = 0, ties = 0;
   
   private Bot bot;
   
   public DisplayPanel() {
      setPreferredSize(new Dimension(WIDTH, HEIGHT));
      setBackground(Color.LIGHT_GRAY);
      
      
      macroboard = getStartingBoard(3, -1);
      board = getStartingBoard(9, 0);
      playerOneTurn = getStartingPlayer();
      
      bot = new Bot();
      
      // Mouse listeners
      addMouseListener(new MouseAdapter() {
         public void mouseClicked(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();
            
            if (playerOneTurn && !isGameOver()) {
               makeMove(x, y);
               
               if (isGameOver()) {
                  clearBoardSelections();
                  System.out.println("You Win");
               }
            }
         }
      });
   }
   
   public void makeMove(int x, int y) {
      int[] clickLocation = getClick(x, y);
      
      int moveX = clickLocation[0];
      int moveY = clickLocation[1];
      
      // Do nothing if the click is not within the board
      if (moveX == -1 || !isInActiveMicroboard(moveX, moveY, macroboard))
         return;
      
      board[moveX][moveY] = 1;
      
      // Make appropritate changes to macroboard
      modifyMacroboard(moveX, moveY);
      playerOneTurn = false;
   }
   
   public void modifyMacroboard(int x, int y) {
      // Make appropriate changes to the macroboard value that the move is within
      // Find the local board that the move is within
      
      int[] localBoardValues = getLocalBoardValues(x, y);
     
      if (isWin(localBoardValues, 1)) {
         macroboard[x / 3][y / 3] = 1;
      }
      else if (isLose(localBoardValues, 1)) {
         // Find the opposite player
         macroboard[x / 3][y / 3] = 2;
      }
      else if (isTie(localBoardValues)) {
         macroboard[x / 3][y / 3] = 3;
      }
  
      //Set the new -1 values (legal localBoards)
      
      // Get the positions within the local board
      x = x % 3;
      y = y % 3;
      
      // If this localboard has already been won
      if (macroboard[x][y] != -1 && macroboard[x][y] != 0) {
         // Set all 0s to -1s
         for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
               if (macroboard[i][j] == 0)
                  macroboard[i][j] = -1;
            }
         }
      }
      else {
         // Set all -1s to 0s
         // Set the local board to -1
         for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
               if (macroboard[i][j] == -1)
                  macroboard[i][j] = 0;
            }
         }
         macroboard[x][y] = -1;
      }
   }

   public int[] getLocalBoardValues(int x, int y) {
      int[] localBoardValues = new int[9];
      
      int startX = x - x % 3;
      int startY = y - y % 3;
      
      for (int i = 0; i < 3; i++) {
         for (int j = 0; j < 3; j++) {
            x = startX + j;
            y = startY + i;
            localBoardValues[i * 3 + j] = board[x][y];
         }
      }
      return localBoardValues;
   }

   // Takes in an int[] of 9 values and checks for win
   public static boolean isWin(int[] boardValues, int player) {
      boolean win = true;
      
      // i = rows, j = columns
      
      // Check for wins within rows
      for (int i = 0; i < 3; i++) {
         win = true;
         for (int j = 0; j < 3; j++) {
            if (boardValues[i * 3 + j] != player) {
               win = false;
               break;
            }
         }
         if (win)
            return true;
      }
           
      // Check for wins within columns
      for (int j = 0; j < 3; j++) {
         win = true;
         for (int i = 0; i < 3; i++) {
            if (boardValues[i * 3 + j] != player) {
               win = false;
               break;
            }
         }
         if (win)
            return true;
      }
      
      // Check for wins within diagonals
      if ((boardValues[0] == player && boardValues[4] == player && boardValues[8] == player) ||
       (boardValues[2] == player && boardValues[4] == player && boardValues[6] == player))
         return true;
      
      return false;
   }
   
   public boolean isLose(int[] boardValues, int player) {
      // Call the isWin method from the opponent's perspective
      if (player == 1) 
         return isWin(boardValues, 2);
      else
         return isWin(boardValues, 1);
   }
	
   // Called after testing for both isWin and isLose
   public boolean isTie(int[] boardValues) {
      for (int i = 0; i < boardValues.length; i++) {
         if (boardValues[i] == 0 || boardValues[i] == -1)
            return false;
      }
      return true;
   }
   
   // Returns the tile within the board that the click is contained in
   public int[] getClick(int x, int y) {
      for (int i = 0; i < 9; i++) {
         for (int j = 0; j < 9; j++) {
            int startX = offset + (i / 3) * (localBoardSize + gap) + (i % 3) * tileSize; 
            int startY = offset + (j / 3) * (localBoardSize + gap) + (j % 3) * tileSize;
            
            if (x > startX && y > startY && x < startX + tileSize && y < startY + tileSize)
               return new int[]{i, j};
         }
      }
      
      // If the click is not within a tile, return -1 values
      return new int[]{-1, -1};
   }
   
   public Boolean isInActiveMicroboard(int x, int y, int[][] macroboard) {
	    return macroboard[(int) x/3][(int) y/3] == -1;
	}
   
   public boolean getStartingPlayer() {
      if (Math.random() > .5)
         return false;
      return true;
   }
   
   public int[][] getStartingBoard(int length, int val) {
      int[][] board = new int[length][length];
      
      for (int i = 0; i < board.length; i++) {
         for (int j = 0; j < board[0].length; j++) {
            board[i][j] = val;
         }
      }
      return board;
   }
   
   public void paint(Graphics g) {
      super.paint(g);
      
      Graphics2D g2 = (Graphics2D) g;
      
      drawPieces(board, macroboard, g, g2);
      drawBoard(g, g2);
      
      g.setColor(Color.WHITE);
      g.setFont(new Font("TimesRoman", Font.PLAIN, 30)); 
      
      if (isGameOver()) {
         int[] tMacro = transformMacroboard(macroboard);
         if (isWin(tMacro, 1)) {
            g.drawString("You Win", 350, 50);
         }
         else if (isWin(tMacro, 2)) {
            g.drawString("You Lost", 345, 50);
         }
         else {
            g.drawString("You Tied", 345, 50);
         }
      }
      else {
         if (playerOneTurn) {
            g.drawString("Player Turn", 330, 50);
         }
         else
            g.drawString("Bot Turn", 345, 50);
      }
   }
   
   public void drawBoard(Graphics g, Graphics2D g2) {
      for (int i = 0; i < 3; i++) {
         for (int j = 0; j < 3; j++) {
            int startX = offset + j * (localBoardSize + gap);
            int startY = offset + i * (localBoardSize + gap);
            
            g.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(1));
            
            if (macroboard[j][i] == 1) {
               g2.setStroke(new BasicStroke(3));
               g.setColor(Color.RED);
               
            }
            else if (macroboard[j][i] == 2) {
               g2.setStroke(new BasicStroke(3));
               g.setColor(Color.BLUE);

            }
            else if (macroboard[j][i] == -1) {
               g.setColor(Color.WHITE);
            }
            
            // Draw 4 vertical lines and 4 horizontal lines
            for (int k = 0; k < 4; k++) {
               int step = (int) (k / 3.0 * localBoardSize);
               g.drawLine(startX + step, startY, startX + step, startY  + localBoardSize);
               g.drawLine(startX, startY  + step, startX + localBoardSize, startY + step);               
            }
         }
      }
   }
   
   public void drawPieces(int[][] board, int[][] macroboard, Graphics g, Graphics2D g2) {
      // Board
      
      for (int i = 0; i < 9; i++) {
         for (int j = 0; j < 9; j++) {
            int startX = offset + (j / 3) * (localBoardSize + gap) + (j % 3) * tileSize; 
            int startY = offset + (i / 3) * (localBoardSize + gap) + (i % 3) * tileSize;    
                    
            // Draw circle
            if (board[j][i] == 1) {
               g.setColor(Color.RED);
               g.fillOval(startX + tileSize / 8, startY + tileSize / 8, tileSize * 3 / 4, tileSize * 3 / 4);
               g.setColor(Color.LIGHT_GRAY);
               g.fillOval(startX + tileSize / 4, startY + tileSize / 4, tileSize / 2, tileSize / 2);
            }
            
            // Draw X
            else if (board[j][i] == 2) {
               g.setColor(Color.BLUE);
               
               g2.setStroke(new BasicStroke(8));
               g2.draw(new Line2D.Float(startX + tileSize / 4, startY + tileSize / 4, startX + tileSize * 3 / 4, startY + tileSize * 3 / 4));
               g2.draw(new Line2D.Float(startX + tileSize / 4, startY + tileSize * 3 / 4, startX + tileSize * 3 / 4, startY + tileSize * 1 / 4));
               g2.setStroke(new BasicStroke(1));
            }
         }
      }
   }
   
   public String convertToString(int[][] board) {
      String s = "";
      for (int i = 0; i < board.length; i++) {
         for (int j = 0; j < board[0].length; j++) {
            s += board[j][i];
         }
      }
      
      return s;
   }
   
   public boolean isGameOver() {
      int[] tMacro = transformMacroboard(macroboard);
      return isWin(tMacro, 1) || isWin(tMacro, 2) || isTie(tMacro);
   }
   
   public int[] transformMacroboard(int[][] oldMacroboard) {
      int[] newMacroboard = new int[9];
      
      for (int i = 0; i < 3; i++) {
         for (int j = 0; j < 3; j++) {
            newMacroboard[i * 3 + j] = oldMacroboard[i][j];
         }
      }
      return newMacroboard;
   }
   
   public void clearBoardSelections() {
      for (int i = 0; i < 3; i++) {
         for (int j = 0; j < 3; j++) {
            if (macroboard[i][j] == -1)
               macroboard[i][j] = 0;
         }
      }
   }
   
   public void start() {
      int playerNum;
      
      while (wins + ties + losses < 1000) {
         if (playerOneTurn)
            playerNum = 1;
         else
            playerNum = 2;   
            
         State currentState = new State(board, macroboard);
         ArrayList<Move> moves = bot.getAvailableMoves(currentState);
         Move bestMove;
         boolean endGame = bot.isEndGame(currentState.getBoard(), currentState.getMacroboard(), 60);
         
         if (!endGame) {
            if (playerNum == 1)
               bestMove = bot.getBestMove(moves, currentState.getBoard(), currentState.getMacroboard(), playerNum, 4);
            else
               bestMove = bot.getBestMove(moves, currentState.getBoard(), currentState.getMacroboard(), playerNum, 5);
         }
         else {
            if (playerNum == 1)
               bestMove = bot.getBestMove(moves, currentState.getBoard(), currentState.getMacroboard(), playerNum, 5);
            else
               bestMove = bot.getBestMove(moves, currentState.getBoard(), currentState.getMacroboard(), playerNum, 4);
         }
         
         int moveX = bestMove.getX();
         int moveY = bestMove.getY();
         
         board[moveX][moveY] = playerNum;
         modifyMacroboard(moveX, moveY);
         playerOneTurn = !playerOneTurn;
         
         repaint();
         
         if (isGameOver()) {
            int[] tMacro = transformMacroboard(macroboard);
            if (isWin(tMacro, 1)) {
               wins++;
               //System.out.println("Win");
            }
            else if (isWin(tMacro, 2)) {
               losses++;
               //System.out.println("Loss");
            }
            else {
               ties++;
               //System.out.println("Tie");
            }
            macroboard = getStartingBoard(3, -1);
            board = getStartingBoard(9, 0);
            playerOneTurn = getStartingPlayer();
            //clearBoardSelections();
         }
      }
      System.out.println(wins + " Wins");
      System.out.println(losses + " Losses");
      System.out.println(ties + " Ties");
      
   }
}