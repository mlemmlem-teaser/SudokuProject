import javax.swing.*;
import java.awt.*;

public class UI extends JFrame {
    JTextField[][] cells = new JTextField[9][9];
    public UI() {
        setTitle("Sudoku");
        setSize(500, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        JPanel grid = new JPanel();
        grid.setLayout(new GridLayout(9, 9));
        int[][] board = Board.board;
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                JTextField cell = new JTextField();
                cell.setHorizontalAlignment(JTextField.CENTER);
                cell.setFont(new Font("Arial", Font.BOLD, 20));

                if (board[i][j] != 0) {
                    cell.setText(board[i][j] + "");
                }
                cells[i][j] = cell;
                grid.add(cell);
            }
        }
        add(grid, BorderLayout.CENTER);
        JPanel buttons = new JPanel();
        JButton check = new JButton("Check");
        JButton reload = new JButton("Reload");
        buttons.add(check);
        buttons.add(reload);
        add(buttons, BorderLayout.SOUTH);
        // check sudoku
        check.addActionListener(e -> {
            int[][] newBoard = new int[9][9];
            try {
                for (int i = 0; i < 9; i++) {
                    for (int j = 0; j < 9; j++) {

                        String text = cells[i][j].getText();

                        if (text.isEmpty()) {
                            board[i][j] = 0;
                        } else {
                            board[i][j] = Integer.parseInt(text);
                        }
                    }
                }
                if (Validator.isValid(board)) {
                    JOptionPane.showMessageDialog(this, "Valid Sudoku!");
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid Sudoku!");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid input!");
            }
        });
        // reload puzzle
        reload.addActionListener(e -> {
            int[][] newBoard = Puzzle.loadPuzzle("puzzle.txt", 0);
            for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 9; j++) {

                    if (board[i][j] == 0) {
                        cells[i][j].setText("");
                    } else {
                        cells[i][j].setText(board[i][j] + "");
                    }
                }
            }
        });
        setVisible(true);
    }
}