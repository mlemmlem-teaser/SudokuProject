import java.util.List;
import java.util.Vector;

public class Board {
    public static int getSize() {
        return board.length;
    }
public static void updateBoard(int[][] newValues) {
    board = newValues;
}
    static int[][] board;
    // static int[][] board_temp = {
    // {5,3,0,0,7,0,0,0,0},
    // {6,0,0,1,9,5,0,0,0},
    // {0,9,8,0,0,0,0,6,0},
    // {8,0,0,0,6,0,0,0,3},
    // {4,0,0,8,0,3,0,0,1},
    // {7,0,0,0,2,0,0,0,6},
    // {0,6,0,0,0,0,2,8,0},
    // {0,0,0,4,1,9,0,0,5},
    // {0,0,0,0,8,0,0,7,9}
    // }; //sau khi có chức năng đọc từ SQL sẽ xóa board_temp rồi thay thế bằng bảng
    // từ SQL
    // static {
    // board = board_temp;
    // }

    static Vector<int[][]> puzzles = new Vector<>();

    public static void ReadData() {

        List<int[][]> list = SQL.ReadData();
        puzzles.addAll(list);
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