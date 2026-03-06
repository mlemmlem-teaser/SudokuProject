import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Puzzle {

    public static int[][] loadPuzzle(String filename) {
        int[][] board = new int[9][9];

        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line;

            for (int i = 0; i < 9; i++) {
                line = reader.readLine();
                for (int j = 0; j < 9; j++) {
                    board[i][j] = line.charAt(j) - '0';
                }
            }

            reader.close();
        } catch (IOException e) {
            System.out.println("Error reading puzzle file");
        }

        return board;
    }
}