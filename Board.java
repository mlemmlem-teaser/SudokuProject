import java.util.List;
import java.util.Vector;

/**
 * Lớp giữ trạng thái board hiện tại và toàn bộ danh sách puzzle đã nạp từ DB.
 * Mọi màn chơi đều được copy riêng để tránh sửa trực tiếp vào dữ liệu gốc.
 */
public class Board {
    static int[][] board;
    static Vector<int[][]> puzzles = new Vector<>();
    static Vector<Integer> puzzleIds = new Vector<>();
    static int currentIndex = -1;

    public static int getSize() {
        return board == null ? 0 : board.length;
    }

    public static int[][] getBoard() {
        return board;
    }

    public static int getPuzzleCount() {
        return puzzles.size();
    }

    public static int getLevelId(int index) {
        return puzzleIds.get(index);
    }

    public static int[][] getPuzzle(int index) {
        return copyBoard(puzzles.get(index));
    }

    public static void updateBoard(int[][] newValues) {
        board = copyBoard(newValues);
    }

    /**
     * Nạp lại puzzle từ SQL Server.
     */
    public static void ReadData() {
        puzzles.clear();
        puzzleIds.clear();

        List<int[][]> list = SQL.ReadData();
        List<Integer> ids = SQL.ReadLevelIds();

        puzzles.addAll(list);
        puzzleIds.addAll(ids);
    }

    /**
     * Chọn puzzle hiện hành và đổ dữ liệu vào board.
     */
    public static void setPuzzle(int index) {
        currentIndex = index;
        board = copyBoard(puzzles.get(index));
    }

    public static int getCurrentIndex() {
        return currentIndex;
    }

    public static void setCell(int row, int col, int value) {
        if (board != null) {
            board[row][col] = value;
        }
    }

    public static void clearCell(int row, int col) {
        if (board != null) {
            board[row][col] = 0;
        }
    }

    public static void printBoard() {
        if (board == null) {
            System.out.println("Board is null");
            return;
        }

        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                System.out.print(board[i][j] + " ");
            }
            System.out.println();
        }
    }

    /**
     * Copy mảng 2 chiều để tránh thay đổi dữ liệu gốc.
     */
    public static int[][] copyBoard(int[][] source) {
        if (source == null) {
            return null;
        }

        int[][] copy = new int[source.length][source[0].length];
        for (int i = 0; i < source.length; i++) {
            System.arraycopy(source[i], 0, copy[i], 0, source[i].length);
        }
        return copy;
    }
}
