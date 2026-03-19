import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SQL {

    // Hàm kết nối SQL Server
    public static Connection getConnection() {
        Connection conn = null;
        try {
            // Thêm trustServerCertificate=true để bỏ qua kiểm tra chứng chỉ bảo mật nếu cần
            String url = "jdbc:sqlserver://localhost:1433;databaseName=SudokuManager;integratedSecurity=true;encrypt=false;trustServerCertificate=true";

            // Không cần truyền user/password khi dùng integratedSecurity
            conn = DriverManager.getConnection(url);
            System.out.println("Connect SQL Server success (Windows Auth)");
        } catch (Exception e) {
            System.err.println("Lỗi kết nối: " + e.getMessage());
            // e.printStackTrace(); // Bật cái này nếu muốn xem chi tiết lỗi
        }
        return conn;
    }

    // Hàm đọc dữ liệu Sudoku từ database
    public static List<int[][]> ReadData() {
        List<int[][]> puzzles = new ArrayList<>();
        try {
            Connection conn = getConnection();
            String query = "SELECT size, board_data FROM levels";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                int size = rs.getInt("size");
                String board = rs.getString("board_data");
                int[][] sudoku = new int[size][size];
                int index = 0;
                for (int i = 0; i < size; i++) {
                    for (int j = 0; j < size; j++) {
                        char c = board.charAt(index++);
                        if (c >= '1' && c <= '9') {
                            sudoku[i][j] = c - '0';
                        } else if (c >= 'A' && c <= 'G') {
                            sudoku[i][j] = c - 'A' + 10;
                        } else {
                            sudoku[i][j] = 0;
                        }
                    }
                }
                puzzles.add(sudoku);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return puzzles;
    }

    public static String login(String gmail, String password) {
        try {
            Connection conn = getConnection();
            String query = "SELECT username FROM users WHERE gmail = ? AND password = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, gmail);
            pstmt.setString(2, password);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("username"); // Trả về tên người dùng nếu đúng
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // Trả về null nếu sai email/password
    }

    public static List<Integer> getCompletedLevels(String username) {
        List<Integer> completedIds = new ArrayList<>();
        try {
            Connection conn = getConnection();
            // Truy vấn lấy level_id mà user đã hoàn thành (completed = 1)
            String query = "SELECT p.level_id FROM progress p " +
                    "JOIN users u ON p.user_id = u.user_id " +
                    "WHERE u.username = ? AND p.completed = 1";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                completedIds.add(rs.getInt("level_id"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return completedIds;
    }
}