import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SQL {

    public static Connection getConnection() {
        Connection conn = null;
        try {
            String url = "jdbc:sqlserver://localhost:1433;databaseName=SudokuManager;integratedSecurity=true;encrypt=false;trustServerCertificate=true";
            conn = DriverManager.getConnection(url);
            System.out.println("Connect SQL Server success (Windows Auth)");
        } catch (Exception e) {
            System.err.println("Lỗi kết nối: " + e.getMessage());
        }
        return conn;
    }

    public static List<int[][]> ReadData() {
        List<int[][]> puzzles = new ArrayList<>();
        String query = "SELECT level_id, size, board_data FROM levels ORDER BY level_id";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                int size = rs.getInt("size");
                String board = rs.getString("board_data");
                int[][] sudoku = new int[size][size];
                int index = 0;

                for (int i = 0; i < size; i++) {
                    for (int j = 0; j < size; j++) {
                        char c = board.charAt(index++);
                        sudoku[i][j] = decodeCell(c);
                    }
                }

                puzzles.add(sudoku);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return puzzles;
    }

    public static List<Integer> ReadLevelIds() {
        List<Integer> ids = new ArrayList<>();
        String query = "SELECT level_id FROM levels ORDER BY level_id";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                ids.add(rs.getInt("level_id"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ids;
    }

    public static String login(String gmail, String password) {
        String sql = "SELECT username FROM users WHERE gmail = ? AND password = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, gmail);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("username");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static int getUserId(String username) {
        String sql = "SELECT user_id FROM users WHERE username = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("user_id");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }

    public static List<Integer> getCompletedLevels(String username) {
        List<Integer> completedIds = new ArrayList<>();
        String query = "SELECT p.level_id FROM progress p " +
                "JOIN users u ON p.user_id = u.user_id " +
                "WHERE u.username = ? AND p.completed = 1 " +
                "ORDER BY p.level_id";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    completedIds.add(rs.getInt("level_id"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return completedIds;
    }

    public static void markCompleted(String username, int levelId) {
        int userId = getUserId(username);
        if (userId == -1) {
            return;
        }

        String update = "UPDATE progress SET completed = 1 WHERE user_id = ? AND level_id = ?";
        String insert = "INSERT INTO progress(user_id, level_id, completed) VALUES (?, ?, 1)";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(update)) {

            ps.setInt(1, userId);
            ps.setInt(2, levelId);

            int rows = ps.executeUpdate();
            if (rows == 0) {
                try (PreparedStatement ps2 = conn.prepareStatement(insert)) {
                    ps2.setInt(1, userId);
                    ps2.setInt(2, levelId);
                    ps2.executeUpdate();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void insertTime(String name, String level, int time) {
        String sql = "INSERT INTO SudokuTime(player_name, level, time_seconds) VALUES (?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, level);
            ps.setInt(3, time);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int getBestTime(String level) {
        String sql = "SELECT MIN(time_seconds) FROM SudokuTime WHERE level = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, level);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int value = rs.getInt(1);
                    if (rs.wasNull()) {
                        return -1;
                    }
                    return value;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }

    private static int decodeCell(char c) {
        if (c >= '1' && c <= '9') {
            return c - '0';
        }
        if (c >= 'A' && c <= 'G') {
            return c - 'A' + 10;
        }
        if (c >= 'a' && c <= 'g') {
            return c - 'a' + 10;
        }
        return 0;
    }
}
