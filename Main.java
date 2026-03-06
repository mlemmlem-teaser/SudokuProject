public class Main {

    public static void main(String[] args) {

        int[][] board = Puzzle.loadPuzzle("puzzle.txt");

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                System.out.print(board[i][j] + " ");
            }
            System.out.println();
        }
    }
}