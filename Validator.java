public class Validator {

    public static boolean isValid(int[][] board) {

        //empty cell
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (board[i][j] == 0) {
                    return false;
                }
            }
        }

        //row
        for (int i = 0; i < 9; i++) {
            boolean[] seen = new boolean[10];
            for (int j = 0; j < 9; j++) {
                int num = board[i][j];
                if (seen[num]) return false;
                seen[num] = true;
            }
        }

        //column
        for (int j = 0; j < 9; j++) {
            boolean[] seen = new boolean[10];
            for (int i = 0; i < 9; i++) {
                int num = board[i][j];
                if (seen[num]) return false;
                seen[num] = true;
            }
        }

        //3x3
        for (int boxRow = 0; boxRow < 9; boxRow += 3) {
            for (int boxCol = 0; boxCol < 9; boxCol += 3) {

                boolean[] seen = new boolean[10];

                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {

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
