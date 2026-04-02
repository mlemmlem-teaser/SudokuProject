import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Puzzle {
    public static int[][] loadPuzzle(String filename, int index) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            int[][] board = new int[9][9];
            String line;
            int currentBoard = -1;
            int row = 0;

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    if (row == 9) {
                        row = 0;
                        currentBoard++;
                    }
                    continue;
                }

                if (row == 0) {
                    currentBoard++;
                }

                if (currentBoard == index) {
                    for (int j = 0; j < 9 && j < line.length(); j++) {
                        char ch = line.charAt(j);
                        board[row][j] = ch >= '0' && ch <= '9' ? ch - '0' : 0;
                    }
                }

                row++;
                if (row == 9) {
                    if (currentBoard == index) {
                        return board;
                    }
                    row = 0;
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading puzzle file");
        }
        return null;
    }

    public void createNewPuzzles() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("puzzle.txt", true))) {
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

            writer.newLine();
            for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 9; j++) {
                    writer.write(board[i][j] + "");
                }
                writer.newLine();
            }
            writer.newLine();
            System.out.println("Added");
        } catch (Exception e) {
            System.out.println("Error writing puzzle file");
        }
    }
}
