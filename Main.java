import javax.swing.SwingUtilities;

/**
 * Điểm khởi động của ứng dụng.
 */
public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Board.ReadData();
            SudokuUI ui = new SudokuUI();
            ui.openLogin();
        });
    }
}
