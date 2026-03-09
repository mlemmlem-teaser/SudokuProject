public class Board {
    static int[][] board;
    public static void printPuzzle(String[] args) {
        board = Puzzle.loadPuzzle("puzzle.txt",2);
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                System.out.print(board[i][j] + " ");
            }
            System.out.println();
        }
    }
    // sửa giá trị 1 ô
    public static void setCell(int row, int col, int value) {
        board[row][col] = value;
    }
    // xóa giá trị 1 ô
    public static void clearCell(int row, int col) {
        board[row][col] = 0;
    }
    // in lại bảng sau khi sửa
    public static void printBoard() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                System.out.print(board[i][j] + " ");
            }
            System.out.println();
        }
    }
}