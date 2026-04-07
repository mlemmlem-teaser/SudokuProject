import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp trung gian làm việc với SQL Server.
 * Tất cả thao tác đọc/ghi dữ liệu đều đi qua đây để UI và logic không phải viết SQL trực tiếp.
 */
public class SQL {

    private static final String URL =
            "jdbc:sqlserver://localhost:1433;databaseName=SudokuManager;integratedSecurity=true;encrypt=false;trustServerCertificate=true";

    private static final int ADMIN_ROLE_ID = 1;
    private static final int USER_ROLE_ID = 2;
    private static final long SOLVE_COOLDOWN_MILLIS = 10L * 60L * 1000L;

    /**
     * Tạo kết nối tới SQL Server bằng Windows Authentication.
     */
    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL);
        } catch (Exception e) {
            System.err.println("Lỗi kết nối SQL Server: " + e.getMessage());
            return null;
        }
    }

    /**
     * Đọc toàn bộ puzzle đã lưu trong bảng levels.
     */
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
                        sudoku[i][j] = decodeCell(board.charAt(index++));
                    }
                }

                puzzles.add(sudoku);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return puzzles;
    }

    /**
     * Lấy danh sách level_id theo đúng thứ tự hiển thị.
     */
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

    /**
     * Đăng nhập: trả về username nếu gmail + password hợp lệ.
     * Project hiện tại vẫn giữ password dạng plain text để tương thích dữ liệu cũ.
     */
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

    /**
     * Đăng ký tài khoản mới với role mặc định là user.
     */
    public static boolean signUp(String gmail, String password, String username) {
        if (gmail == null || password == null || username == null) {
            return false;
        }

        gmail = gmail.trim();
        password = password.trim();
        username = username.trim();

        if (gmail.isEmpty() || password.isEmpty() || username.isEmpty()) {
            return false;
        }

        if (isGmailTaken(gmail) || isUsernameTaken(username)) {
            return false;
        }

        String sql = "INSERT INTO users(user_id, gmail, password, username, role_id, last_solve_at) VALUES (?, ?, ?, ?, ?, NULL)";
        int newUserId = getNextUserId();

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, newUserId);
            pstmt.setString(2, gmail);
            pstmt.setString(3, password);
            pstmt.setString(4, username);
            pstmt.setInt(5, USER_ROLE_ID);

            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Kiểm tra gmail đã tồn tại hay chưa.
     */
    public static boolean isGmailTaken(String gmail) {
        String sql = "SELECT 1 FROM users WHERE gmail = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, gmail);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    /**
     * Kiểm tra username đã tồn tại hay chưa.
     */
    public static boolean isUsernameTaken(String username) {
        String sql = "SELECT 1 FROM users WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    /**
     * Lấy user_id từ username.
     */
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

    /**
     * Lấy role_name (admin/user) của username.
     */
    public static String getUserRole(String username) {
        String sql = "SELECT r.role_name FROM users u JOIN roles r ON u.role_id = r.role_id WHERE u.username = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("role_name");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "user";
    }

    /**
     * Kiểm tra một username có phải admin hay không.
     */
    public static boolean isAdmin(String username) {
        return "admin".equalsIgnoreCase(getUserRole(username));
    }

    /**
     * Lấy danh sách level đã hoàn thành của user.
     */
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

    /**
     * Đánh dấu level đã hoàn thành cho user.
     */
    public static void markCompleted(String username, int levelId) {
        int userId = getUserId(username);
        if (userId == -1) {
            return;
        }

        String update = "UPDATE progress SET completed = 1, completed_at = GETDATE() WHERE user_id = ? AND level_id = ?";
        String insert = "INSERT INTO progress(user_id, level_id, completed, completed_at) VALUES (?, ?, 1, GETDATE())";

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

    /**
     * Kiểm tra user có được phép dùng Solve ở thời điểm hiện tại hay không.
     */
    public static boolean canUseSolve(int userId) {
        Timestamp last = getLastSolveTime(userId);
        if (last == null) {
            return true;
        }

        long elapsed = System.currentTimeMillis() - last.getTime();
        return elapsed >= SOLVE_COOLDOWN_MILLIS;
    }

    /**
     * Trả về số giây còn lại trước khi user được dùng Solve tiếp.
     */
    public static long getSolveCooldownRemainingSeconds(int userId) {
        Timestamp last = getLastSolveTime(userId);
        if (last == null) {
            return 0;
        }

        long elapsed = System.currentTimeMillis() - last.getTime();
        long remain = SOLVE_COOLDOWN_MILLIS - elapsed;
        return Math.max(0, remain / 1000);
    }

    /**
     * Lưu lại mốc thời gian dùng Solve gần nhất của user.
     */
    public static void markSolveUsed(int userId) {
        String sql = "UPDATE users SET last_solve_at = GETDATE() WHERE user_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Đọc thời điểm dùng Solve gần nhất.
     */
    public static Timestamp getLastSolveTime(int userId) {
        String sql = "SELECT last_solve_at FROM users WHERE user_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getTimestamp("last_solve_at");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Lưu best time theo từng user và từng level.
     * Nếu đã có best time tốt hơn thì giữ nguyên.
     */
    public static void saveBestTime(int userId, int levelId, int timeSeconds) {
        int currentBest = getBestTime(userId, levelId);

        if (currentBest != -1 && currentBest <= timeSeconds) {
            return;
        }

        String update = "UPDATE SudokuTime SET best_seconds = ?, updated_at = GETDATE() WHERE user_id = ? AND level_id = ?";
        String insert = "INSERT INTO SudokuTime(user_id, level_id, best_seconds, updated_at) VALUES (?, ?, ?, GETDATE())";

        try (Connection conn = getConnection()) {
            if (currentBest == -1) {
                try (PreparedStatement ps = conn.prepareStatement(insert)) {
                    ps.setInt(1, userId);
                    ps.setInt(2, levelId);
                    ps.setInt(3, timeSeconds);
                    ps.executeUpdate();
                }
            } else {
                try (PreparedStatement ps = conn.prepareStatement(update)) {
                    ps.setInt(1, timeSeconds);
                    ps.setInt(2, userId);
                    ps.setInt(3, levelId);
                    ps.executeUpdate();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Lấy best time của một user cho một level.
     */
    public static int getBestTime(int userId, int levelId) {
        String sql = "SELECT best_seconds FROM SudokuTime WHERE user_id = ? AND level_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, levelId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("best_seconds");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }

    /**
     * Hàm cũ giữ lại để tương thích.
     * Name -> username, level -> dạng "Level X".
     */
    public static void insertTime(String name, String level, int time) {
        int userId = getUserId(name);
        int levelId = resolveLevelId(level);

        if (userId == -1 || levelId == -1) {
            return;
        }

        saveBestTime(userId, levelId, time);
    }

    /**
     * Hàm cũ giữ lại để tương thích.
     * Trả về best time chung của level theo thứ tự hiển thị.
     */
    public static int getBestTime(String level) {
        int levelId = resolveLevelId(level);
        if (levelId == -1) {
            return -1;
        }

        String sql = "SELECT MIN(best_seconds) FROM SudokuTime WHERE level_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, levelId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int value = rs.getInt(1);
                    return rs.wasNull() ? -1 : value;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }

    /**
     * Chuyển "Level X" thành level_id thực tế trong DB.
     * Cách này giữ tương thích với code cũ đang dùng tên level theo số thứ tự.
     */
    public static int resolveLevelId(String levelLabel) {
        if (levelLabel == null) {
            return -1;
        }

        String digits = levelLabel.replaceAll("\\D+", "");
        if (digits.isEmpty()) {
            return -1;
        }

        int ordinal;
        try {
            ordinal = Integer.parseInt(digits);
        } catch (Exception e) {
            return -1;
        }

        String sql = "SELECT level_id FROM (" +
                "SELECT level_id, ROW_NUMBER() OVER (ORDER BY level_id) AS rn FROM levels" +
                ") t WHERE rn = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ordinal);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("level_id");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }

    /**
     * Sinh user_id mới.
     */
    private static int getNextUserId() {
        String sql = "SELECT ISNULL(MAX(user_id), 60000) + 1 AS next_id FROM users";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt("next_id");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 60001;
    }

    /**
     * Sinh level_id mới.
     */
    public static int getNextLevelId() {
        String sql = "SELECT ISNULL(MAX(level_id), 10000) + 1 AS next_id FROM levels";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt("next_id");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 10001;
    }

    /**
     * Đọc toàn bộ puzzle dạng bảng để dùng cho màn admin.
     */
    public static List<PuzzleRecord> readPuzzleRecords() {
        List<PuzzleRecord> list = new ArrayList<>();
        String sql = "SELECT level_id, size, board_data, title, difficulty, created_by, created_at FROM levels ORDER BY level_id";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                PuzzleRecord record = new PuzzleRecord();
                record.levelId = rs.getInt("level_id");
                record.size = rs.getInt("size");
                record.boardData = rs.getString("board_data");
                record.title = rs.getString("title");
                record.difficulty = rs.getString("difficulty");
                record.createdBy = rs.getInt("created_by");
                if (rs.wasNull()) {
                    record.createdBy = -1;
                }
                record.createdAt = rs.getTimestamp("created_at");
                list.add(record);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    /**
     * Bản ghi puzzle dùng cho màn quản trị.
     */
    public static class PuzzleRecord {
        public int levelId;
        public int size;
        public String boardData;
        public String title;
        public String difficulty;
        public int createdBy;
        public Timestamp createdAt;
    }

    /**
     * Chuyển đổi 1 ký tự sang giá trị ô Sudoku.
     */
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
