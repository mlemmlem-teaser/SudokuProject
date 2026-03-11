import java.util.List;
import java.util.Vector;

public class Board {

    static int[][] board;

    static Vector<int[][]> puzzles = new Vector<>();

    public static void ReadData() {
        
        // List<int[][]> list = SQL.ReadData();
        // puzzles.addAll(list);
    }

    public static void setPuzzle(int index) {
        board = puzzles.get(index);
    }

    public static void setCell(int row, int col, int value) {
        board[row][col] = value;
    }

    public static void clearCell(int row, int col) {
        board[row][col] = 0;
    }

    public static void printBoard() {

        for (int i = 0; i < board.length; i++) {

            for (int j = 0; j < board.length; j++) {

                System.out.print(board[i][j] + " ");
            }

            System.out.println();
        }
    }
}