import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Puzzle {
    public static int[][] loadPuzzle(String filename, int index) {
        int[][] board = new int[9][9];
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line;
            int currentBoard = 0;
            while (true) {
                int row = 0;
                while (row < 9) {
                    line = reader.readLine();
                    if (line == null) {
                        reader.close();
                        return null; // hết file
                    }
                    if (line.trim().isEmpty()) {
                        continue; // bỏ qua dòng trống
                    }
                    if (currentBoard == index) {
                        for (int j = 0; j < 9; j++) {
                            board[row][j] = line.charAt(j) - '0';
                        }
                    }
                    row++;
                }
                if (currentBoard == index) {
                    reader.close();
                    return board;
                }
                currentBoard++;
            }
        } catch (IOException e) {
            System.out.println("Error reading puzzle file");
        }
        return null;
    }

    public void createNewPuzzles() {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("puzzle.txt", true));
            int[][] board = {
                    { 1, 2, 3, 4, 5, 6, 7, 8, 9 },
                    { 4, 5, 6, 7, 8, 9, 1, 2, 3 },
                    { 7, 8, 9, 1, 2, 3, 4, 5, 6 },
                    { 2, 3, 4, 5, 6, 7, 8, 9, 1 },
                    { 5, 6, 7, 8, 9, 1, 2, 3, 4 },
                    { 8, 9, 1, 2, 3, 4, 5, 6, 7 },
                    { 3, 4, 5, 6, 7, 8, 9, 1, 2 },
                    { 6, 7, 8, 9, 1, 2, 3, 4, 5 },
                    { 9, 1, 2, 3, 4, 5, 6, 7, 8 }
            };
            writer.write("\n");
            writer.newLine();
            for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 9; j++) {
                    writer.write(board[i][j]+"");
                }
                writer.newLine();
            }
            writer.newLine();
            writer.close();
            System.out.println("Added");
        } catch (Exception e) {
            System.out.println("Error writing puzzle file");
        }
    }
}