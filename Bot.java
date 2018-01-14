import java.util.ArrayList;
import java.util.Random;

/**
 * Bot class
 * 
 * Handles everything that has to do with the field, such 
 * as storing the current state and performing calculations
 * on the field.
 * 
 * @author Jim van Eeden <jim@starapple.nl>, Joost de Meij <joost@starapple.nl>
 */

public class Bot {
	private final int COLS = 9, ROWS = 9;
   
   // The evaluation function's socre should never exceed MAX_SCORE
   private final int MAX_SCORE = 20000;
   private final int SEARCH_DEPTH = 0;
   private final int INITIAL_ALPHA = -100000, INITIAL_BETA = 100000;
   
   
	private String mLastError = "";
   private Random r;
	
	public Bot() {
      r = new Random();
	}

	public ArrayList<Move> getAvailableMoves(State state) {
	   ArrayList<Move> moves = new ArrayList<Move>();
		int[][] board = state.getBoard();
      int[][] macroboard = state.getMacroboard();
      
		for (int y = 0; y < ROWS; y++) {
            for (int x = 0; x < COLS; x++) {
                if (isInActiveMicroboard(x, y, macroboard) && board[x][y] == 0) {
                    moves.add(new Move(x, y));
                }
            }
        }

		return moves;
	}
   
   //reward for two in a rows in potential three in a rows on macroboard
   public Move getBestMove(ArrayList<Move> legalMoves, int[][] board, int[][] macroboard, int botId, int stdDepth) {
      // If it's the first move return the middle square
      if (isEmpty(board))
         return new Move(4, 4);
      
      // Initialize with the first move
      ArrayList<Move> bestMoves = new ArrayList<Move>();
      
      int score;
      int maxScore = -MAX_SCORE - 20;
      
      
      State possibleState;
      
      //System.err.println("Macroboard: " + toString(macroboard));
      //System.err.println("Number of legal moves: " + legalMoves.size());
      
      boolean freeMove = isFreeMove(macroboard);
      
      int startTime = (int) System.currentTimeMillis();
      int timePassed;
      
      // Find what the ID of the opposite player is
      int oppositePlayer = getOppositePlayer(botId);
      
      for (int i = 0; i < legalMoves.size(); i++) {
         possibleState = generateState(legalMoves.get(i), botId, board, macroboard);
         
         timePassed = (int) System.currentTimeMillis() - startTime;
         
         int depth = stdDepth;
         
         // If we have reached endgame, increase the search depth
         if (isEndGame(possibleState.getBoard(), possibleState.getMacroboard(), 30) && timePassed < 5000) {
            depth += 1;
         }
         else if (timePassed < 5000 && isEndGame(possibleState.getBoard(), possibleState.getMacroboard(), 60)) {
            
         }
         
         score = minimax(possibleState, botId, oppositePlayer, depth, INITIAL_ALPHA, INITIAL_BETA);
         
         //System.err.println(timePassed + " Milliseconds passed");
         
         Move move = legalMoves.get(i);
         //System.err.println("Move " + move.getX() + " " + move.getY() + " #" + i + ": " + score);
         
         if (score > maxScore) {
            maxScore = score;
            // Find the actual function for clearing
            bestMoves.clear();
            bestMoves.add(legalMoves.get(i));
         }
         else if (score == maxScore) {
            bestMoves.add(legalMoves.get(i));
         }
      }
      
      return bestMoves.get(r.nextInt(bestMoves.size()));
   }
   
   // Returns whether or not the player can move anywhere on the board
   public boolean isFreeMove(int[][] macroboard) {
      int count = 0;
      
      // If two or more values are -1
      for (int i = 0; i < 3; i++) {
         for (int j = 0; j < 3; j++) {
            if (macroboard[i][j] == -1 && count == 1) 
               return true;
            else if (macroboard[i][j] == -1)
               count++;
         }
      }
      return false;
   }  
   
   // Returns a score for a given node
   // Uses alpha beta pruning to reduce the size of the game tree
   public int minimax(State state, int botId, int player, int depth, int alpha, int beta) {
      // Count how many -1 values are given to the opponent
      // Give higher score for lower amount of -1 values
      
      // Transform the 2D arrays into 1D
      int[] macroboard = transformMacroboard(state.getMacroboard());
      
      // Test for terminal conditions
      if (isWin(macroboard, botId)) {
         return MAX_SCORE + depth;
      }
      else if (isLose(macroboard, botId)) {
         return -MAX_SCORE -  depth;
      }
      else if (isTie(macroboard)) {
         return 0;
      }
      else if (depth == 0) {
         return evaluationFunction(state, botId);
      }
      
      // Find the oppositePlayer
      int oppositePlayer = getOppositePlayer(player);
      
      ArrayList<Move> legalMoves = getAvailableMoves(state);
      State possibleState;
      int stateScore;
      
      // Maximizing player
      if (player == botId)
         stateScore = INITIAL_ALPHA;
      else
         stateScore = INITIAL_BETA;
      
      for (int i = 0; i < legalMoves.size(); i++) {
         possibleState = generateState(legalMoves.get(i), player, state.getBoard(), state.getMacroboard());
         
         // We want to maximize the value of the state
         if (player == botId) {
            stateScore = Math.max(stateScore, minimax(possibleState, botId, oppositePlayer, depth - 1, alpha, beta));
            
            alpha = Math.max(alpha, stateScore);
            
            if (beta <= alpha)
               break;
         }
         // The opponent wants to minimze the value of the state
         else {
            stateScore = Math.min(stateScore, minimax(possibleState, botId, oppositePlayer, depth - 1, alpha, beta));
            
            beta = Math.min(beta, stateScore);
            
            if (beta <= alpha)
               break;
         }
      }
      
      // The algorithm sees a tie
      if (Math.abs(stateScore) == INITIAL_BETA) {
         System.err.println("Bug, Depth: " + depth);
         return 0;
      }
         
      return stateScore;
   }
   
   public double localBoardMultiplier(int x, int y) {
   
      // Center: 1.6; Corner: 1.2; Side: .8
      if (x == 0) {
         if (y == 1)
            return .7;
         else
            return 1.2;
      }
      else if (x == 1) {
         // Center
         if (y == 1) 
            return 1.6;
         else if (y == 0)
            return .7;
         else  
            return .8;
      }
      else if (x == 2) {
         if (y == 1)
            return .8;
         else
            return 1.2;
      }
      return 1;
   }
   
   public int evaluationFunction(State state, int botId) {
      int[][] macroboard = state.getMacroboard();
      int[][] board = state.getBoard(); 
       
      int score = 0;
      
       // Find the oppositePlayer
      int oppositePlayer = getOppositePlayer(botId);
      
      for (int i = 0; i < 3; i++) {
         for (int j = 0; j < 3; j++) {
            if (macroboard[i][j] == oppositePlayer)
               score -= 1000 * localBoardMultiplier(i, j);
            else if (macroboard[i][j] == botId)
               score += 1000 * localBoardMultiplier(i, j);
         }
      }
      
      // Check local board and macroboard for 2 in a rows
      
      // Transform macroboard into 1D array
      int[] flatMacroboard = transformMacroboard(macroboard);
      
      // Increase score for my two in a rows
      score += getMacroTwoInARows(flatMacroboard, botId) * 500;
      
      // Decrease score for opponents two in a rows
      score -= getMacroTwoInARows(flatMacroboard, oppositePlayer) * 500;
      
      // Check local boards for two in a rows
      int[] localBoardValues;
      int activeLocalBoard;
      
      for (int i = 0; i < 9; i += 3) {
         for (int j = 0; j < 9; j += 3) {
            localBoardValues = getLocalBoardValues(i, j, board);
            
            // Find the active macroboard
            activeLocalBoard = j + i / 3;
            
            // Give an increased score depending on what square the two in a row is in
            score += getMicroTwoInARows(localBoardValues, flatMacroboard, botId, activeLocalBoard) * 10 * localBoardMultiplier(i / 3, j / 3);
            score -= getMicroTwoInARows(localBoardValues, flatMacroboard, oppositePlayer, activeLocalBoard) * 10 * localBoardMultiplier(i / 3, j / 3);
         }
      }
      
      
      return score;
   }
   
   public int getOppositePlayer(int player) {
      if (player == 1)
         return 2;
      else 
         return 1;
   }
   
   // Takes 9 values and returns how many two in a rows there are
   public int getMacroTwoInARows(int[] board, int player) {
      int counter = 0;
      int[] boardCopy;
      
      for (int i = 0; i < board.length; i++) {
         if (board[i] > 0)
            continue;
         boardCopy = copyValues(board);
         boardCopy[i] = player;
         if (isWin(boardCopy, player)) {
            counter++;
         }
      }
      
      return counter;
   }
   
   public int getMicroTwoInARows(int[] board, int[] macroboard, int player, int activeLocalBoard) {
      int counter = 0;
      int[] boardCopy, macroboardCopy;
      int x, y;
      
      int oppositePlayer = getOppositePlayer(player);
      
      for (int i = 0; i < board.length; i++) {
         if (board[i] > 0)
            continue;
         boardCopy = copyValues(board);
         boardCopy[i] = player;
         if (isWin(boardCopy, player)) {
             // If this localboard could cause a win, increase score counter by more
             macroboardCopy = copyValues(macroboard);
             macroboardCopy[activeLocalBoard] = player;
             if (isWin(macroboardCopy, player)) {
                counter += 10;
             }
             else {
                macroboardCopy = copyValues(macroboard);
                macroboardCopy[activeLocalBoard] = oppositePlayer;
                if (isWin(macroboardCopy, oppositePlayer)) {
                   counter += 10;
                }
                else
                  counter++;
            }
         }
      }
      
      return counter;
   }
   
   public String singleToString(int[] numbers) {
      String s = "";
      for (int i = 0; i < numbers.length; i++) {
         s += numbers[i];
      }
      return s;
   }
   
   public int[] copyValues(int[] board) {
      int[] newBoard = new int[board.length];
      
      for (int i = 0; i < board.length; i++) {
         newBoard[i] = board[i];
      }
      return newBoard;
   }
   
   public boolean isEndGame(int[][] board, int[][] macroboard, int numSpotsEmpty) {
      int count = 0;
      
      // Count how many squares are open on the regular board not counting macroboard > 0
      int[] localBoardValues;
      
      for (int i = 0; i < 3; i++) {
         for (int j = 0; j < 3; j++) {
            if (macroboard[i][j] <= 0) {
               localBoardValues = getLocalBoardValues(i * 3, j * 3, board);
               for (int k = 0; k < localBoardValues.length; k++) {
                  if (localBoardValues[k] == 0)
                     count++;
               }
            }
         }
      }
      
      // If less than n spots are empty
      return count < numSpotsEmpty;
   }
   
   public boolean multipleLocalBoardsAvailable(int[][] board) {
      int count = 0;
      
      // If there are at least two, return true
      for (int i = 0; i < 3; i++) {
         for (int j = 0; j < 3; j++) {
            if (board[i][j] == -1 && count == 1)
               return true;
            else if (board[i][j] == -1)
               count++; 
         }
      }
      return false;
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
   
   // Player is 1 for me and 2 for opponent
   public State generateState(Move move, int player, int[][] board, int[][] macroboard) {
      int[][] boardCopy = makeCopy(board);
      int[][] macroboardCopy = makeCopy(macroboard);
      
      // Change the board
      boardCopy[move.getX()][move.getY()] = player;
      
      //Change the macroboard
      macroboardCopy = modifyMacroboard(move, player, macroboardCopy, boardCopy);
      
      return new State(boardCopy, macroboardCopy);
   }
   
   public int[][] makeCopy(int[][] matrix) {
      int [][] myInt = new int[matrix.length][];
      for(int i = 0; i < matrix.length; i++)
         myInt[i] = matrix[i].clone();
      return myInt;
   }
   
   public int[][] modifyMacroboard(Move move,int player, int[][] macroboard, int[][] board) {
      // Make appropriate changes to the macroboard value that the move is within
      // Find the local board that the move is within
      int x = move.getX();
      int y = move.getY();
      
      int[] localBoardValues = getLocalBoardValues(x, y, board);
     
      if (isWin(localBoardValues, player)) {
         macroboard[x / 3][y / 3] = player;
      }
      else if (isLose(localBoardValues, player)) {
         // Find the opposite player
         if (player == 1) 
            macroboard[x / 3][y / 3] = 2;
         else
            macroboard[x / 3][y / 3] = 1;
      }
      else if (isTie(localBoardValues)) {
         macroboard[x / 3][y / 3] = 3;
      }
  
      //Set the new -1 values (legal localBoards)
      
      // Get the positions within the local board
      x = move.getX() % 3;
      y = move.getY() % 3;
      
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
      return macroboard;
   }
   
   public int[] getLocalBoardValues(int x, int y, int[][] board) {
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
   
	public Boolean isInActiveMicroboard(int x, int y, int[][] macroboard) {
	    return macroboard[(int) x/3][(int) y/3] == -1;
	}
	
	/**
	 * Returns reason why addMove returns false
	 * @param args : 
	 * @return : reason why addMove returns false
	 */
	public String getLastError() {
		return mLastError;
	}

	
	/**
	 * Creates comma separated String with player ids for the microboards.
	 * @param args : 
	 * @return : String with player names for every cell, or 'empty' when cell is empty.
	 */
	public String toString(int[][] board) {
		String r = "";
		int counter = 0;
		for (int y = 0; y < board.length; y++) {
			for (int x = 0; x < board[0].length; x++) {
				if (counter > 0) {
					r += ",";
				}
				r += board[x][y];
				counter++;
			}
		}
		return r;
	}
	
	/**
	 * Checks whether the field is full
	 * @param args : 
	 * @return : Returns true when field is full, otherwise returns false.
	 */
	public boolean isFull(int[][] board) {
		for (int x = 0; x < board.length; x++)
		  for (int y = 0; y < board[0].length; y++)
		    if (board[x][y] == 0)
		      return false; // At least one cell is not filled
		// All cells are filled
		return true;
	}
	
	public int getNrColumns() {
		return COLS;
	}
	
	public int getNrRows() {
		return ROWS;
	}

	public boolean isEmpty(int[][] board) {
		for (int x = 0; x < COLS; x++) {
			  for (int y = 0; y < ROWS; y++) {
				  if (board[x][y] > 0) {
					  return false;
				  }
			  }
		}
		return true;
	}
}