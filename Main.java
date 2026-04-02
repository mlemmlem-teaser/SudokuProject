import java.sql.Connection;
import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Connection conn = SQL.getConnection();
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception ignored) {
            }

            Board.ReadData();

            SudokuUI ui = new SudokuUI();
            ui.openLogin();
        });
    }
}
