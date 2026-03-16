public class Validator {

    public static boolean isValid(int[][] board) {

        int size = board.length;
        int box = (int)Math.sqrt(size);

        // empty cell
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (board[i][j] == 0) return false;
            }
        }

        // row
        for (int i = 0; i < size; i++) {

            boolean[] seen = new boolean[size + 1];

            for (int j = 0; j < size; j++) {

                int num = board[i][j];

                if (seen[num]) return false;

                seen[num] = true;
            }
        }

        // column
        for (int j = 0; j < size; j++) {

            boolean[] seen = new boolean[size + 1];

            for (int i = 0; i < size; i++) {

                int num = board[i][j];

                if (seen[num]) return false;

                seen[num] = true;
            }
        }

        // box
        for (int boxRow = 0; boxRow < size; boxRow += box) {
            for (int boxCol = 0; boxCol < size; boxCol += box) {

                boolean[] seen = new boolean[size + 1];

                for (int i = 0; i < box; i++) {
                    for (int j = 0; j < box; j++) {

                        int num = board[boxRow + i][boxCol + j];

                        if (seen[num]) return false;

                        seen[num] = true;
                    }
                }
            }
        }

        return true;
    }
}