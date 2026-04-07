/**
 * Giải Sudoku bằng backtracking.
 */
public class SudokuSolver {

    public static boolean solve(int[][] board) {
        if (board == null || board.length == 0) {
            return false;
        }
        return solveRec(board, 0, 0);
    }

    private static boolean solveRec(int[][] board, int row, int col) {
        int size = board.length;

        if (row == size - 1 && col == size) {
            return true;
        }

        if (col == size) {
            row++;
            col = 0;
        }

        if (board[row][col] != 0) {
            return solveRec(board, row, col + 1);
        }

        for (int num = 1; num <= size; num++) {
            if (Validator.isValid(board, row, col, num)) {
                board[row][col] = num;
                if (solveRec(board, row, col + 1)) {
                    return true;
                }
                board[row][col] = 0;
            }
        }

        return false;
    }
}
