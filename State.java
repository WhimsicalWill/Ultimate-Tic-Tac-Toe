public class State {
   private int[][] board;
   private int[][] macroboard;
   
   public State(int[][] board, int[][] macroboard) {
      this.board = board;
      this.macroboard = macroboard;
   }
   
   public int[][] getBoard() {
      return board;
   }
   
   public int[][] getMacroboard() {
      return macroboard;
   }
}