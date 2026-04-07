/**
 * Kiểm tra tính hợp lệ của board Sudoku.
 * Hỗ trợ kích thước 4x4, 9x9, 16x16...
 */
public class Validator {

    public static boolean isValid(int[][] board) {
        if (board == null || board.length == 0) {
            return false;
        }

        int size = board.length;
        int box = (int) Math.sqrt(size);

        if (box * box != size) {
            return false;
        }

        // Không được có ô trống hoặc giá trị ngoài phạm vi.
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                int value = board[i][j];
                if (value == 0 || value < 1 || value > size) {
                    return false;
                }
            }
        }

        // Kiểm tra từng hàng.
        for (int i = 0; i < size; i++) {
            boolean[] seen = new boolean[size + 1];
            for (int j = 0; j < size; j++) {
                int num = board[i][j];
                if (num < 1 || num > size || seen[num]) {
                    return false;
                }
                seen[num] = true;
            }
        }

        // Kiểm tra từng cột.
        for (int j = 0; j < size; j++) {
            boolean[] seen = new boolean[size + 1];
            for (int i = 0; i < size; i++) {
                int num = board[i][j];
                if (num < 1 || num > size || seen[num]) {
                    return false;
                }
                seen[num] = true;
            }
        }

        // Kiểm tra từng khối con.
        for (int boxRow = 0; boxRow < size; boxRow += box) {
            for (int boxCol = 0; boxCol < size; boxCol += box) {
                boolean[] seen = new boolean[size + 1];
                for (int i = 0; i < box; i++) {
                    for (int j = 0; j < box; j++) {
                        int num = board[boxRow + i][boxCol + j];
                        if (num < 1 || num > size || seen[num]) {
                            return false;
                        }
                        seen[num] = true;
                    }
                }
            }
        }

        return true;
    }

    /**
     * Kiểm tra nước đi của 1 ô cụ thể.
     */
    public static boolean isValid(int[][] board, int row, int col, int num) {
        if (board == null || board.length == 0) {
            return false;
        }

        int size = board.length;

        if (num < 1 || num > size) {
            return false;
        }

        for (int i = 0; i < size; i++) {
            if (board[row][i] == num && i != col) {
                return false;
            }
        }

        for (int i = 0; i < size; i++) {
            if (board[i][col] == num && i != row) {
                return false;
            }
        }

        int box = (int) Math.sqrt(size);
        int boxRow = row - row % box;
        int boxCol = col - col % box;

        for (int i = boxRow; i < boxRow + box; i++) {
            for (int j = boxCol; j < boxCol + box; j++) {
                if (board[i][j] == num && (i != row || j != col)) {
                    return false;
                }
            }
        }

        return true;
    }

    public static boolean isSafe(int[][] board, int row, int col, int num) {
        return isValid(board, row, col, num);
    }
}
