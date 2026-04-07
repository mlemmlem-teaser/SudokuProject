import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Giao diện chính của game Sudoku.
 * Hỗ trợ đăng nhập, đăng ký, chơi game, kiểm tra, solve, và quản lý puzzle cho admin.
 */
public class SudokuUI extends JFrame {

    private JTextField[][] cells;
    private JList<String> levelList;
    private DefaultListModel<String> levelModel;
    private JPanel boardContainer;
    private JPanel boardPanel;

    private JButton loginBtn;
    private JButton registerBtn;
    private JButton logoutBtn;
    private JButton solveBtn;
    private JButton checkBtn;
    private JButton manageBtn;

    private boolean updatingLevelList = false;

    private final JLabel labelTime = new JLabel("00:00");
    private final JLabel labelBest = new JLabel("Best: --:--");
    private final Feature timer = new Feature(labelTime);

    private String loggedInUser = null;
    private String loggedInRole = "user";

    public SudokuUI() {
        setTitle("Sudoku Game");
        setSize(1100, 760);
        setMinimumSize(new Dimension(980, 680));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(14, 14));
        getContentPane().setBackground(new Color(236, 240, 245));

        buildTopPanel();
        buildCenterPanel();
        buildBottomPanel();

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void buildTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(33, 103, 172));
        topPanel.setBorder(new EmptyBorder(14, 18, 14, 18));

        JPanel titleWrap = new JPanel(new BorderLayout());
        titleWrap.setOpaque(false);

        JLabel title = new JLabel("SUDOKU GAME", JLabel.LEFT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 30));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Hãy chơi theo cách của bạn!");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(new Color(230, 240, 250));

        titleWrap.add(title, BorderLayout.NORTH);
        titleWrap.add(subtitle, BorderLayout.SOUTH);
        topPanel.add(titleWrap, BorderLayout.WEST);

        JPanel timerPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        timerPanel.setOpaque(false);

        labelTime.setFont(new Font("Segoe UI", Font.BOLD, 20));
        labelTime.setForeground(Color.WHITE);
        labelBest.setFont(new Font("Segoe UI", Font.BOLD, 16));
        labelBest.setForeground(Color.WHITE);

        timerPanel.add(labelTime);
        timerPanel.add(labelBest);
        topPanel.add(timerPanel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);
    }

    private void buildCenterPanel() {
        JPanel centerPanel = new JPanel(new BorderLayout(14, 14));
        centerPanel.setBackground(new Color(236, 240, 245));
        centerPanel.setBorder(new EmptyBorder(0, 14, 0, 14));

        levelModel = new DefaultListModel<>();
        levelList = new JList<>(levelModel);
        levelList.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        levelList.setSelectionBackground(new Color(52, 152, 219));
        levelList.setSelectionForeground(Color.WHITE);
        levelList.setFixedCellHeight(36);
        levelList.setBorder(new EmptyBorder(8, 8, 8, 8));

        levelList.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel label = new JLabel(value);
            label.setOpaque(true);
            label.setBorder(new EmptyBorder(4, 10, 4, 10));
            label.setFont(new Font("Segoe UI", Font.PLAIN, 15));

            boolean locked = value != null && value.contains("[LOCKED]");
            boolean done = value != null && value.contains("[DONE]");

            if (isSelected) {
                label.setBackground(list.getSelectionBackground());
                label.setForeground(list.getSelectionForeground());
            } else if (locked) {
                label.setBackground(new Color(241, 243, 246));
                label.setForeground(new Color(140, 140, 140));
            } else if (done) {
                label.setBackground(new Color(236, 248, 240));
                label.setForeground(new Color(32, 124, 59));
            } else {
                label.setBackground(Color.WHITE);
                label.setForeground(new Color(55, 55, 55));
            }

            return label;
        });

        levelList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && loggedInUser != null && !updatingLevelList) {
                int index = levelList.getSelectedIndex();
                if (index == -1) {
                    return;
                }

                List<Integer> completed = SQL.getCompletedLevels(loggedInUser);
                int unlockedIndex = getUnlockedIndex(completed);

                if (index > unlockedIndex) {
                    JOptionPane.showMessageDialog(this, "Màn chơi này đang bị khóa. Hãy hoàn thành các màn trước.");
                    updatingLevelList = true;
                    levelList.setSelectedIndex(Math.min(unlockedIndex, Math.max(0, Board.getPuzzleCount() - 1)));
                    updatingLevelList = false;
                    return;
                }

                loadLevel(index);
            }
        });

        JScrollPane levelPane = new JScrollPane(levelList);
        levelPane.setPreferredSize(new Dimension(250, 0));
        levelPane.setBorder(new TitledBorder(new LineBorder(new Color(33, 103, 172), 2), "Levels"));

        JPanel leftContainer = new JPanel(new BorderLayout());
        leftContainer.setBackground(new Color(236, 240, 245));
        leftContainer.add(levelPane, BorderLayout.CENTER);

        boardContainer = new JPanel(new GridBagLayout());
        boardContainer.setBackground(new Color(236, 240, 245));
        showWelcomeCard();

        centerPanel.add(leftContainer, BorderLayout.WEST);
        centerPanel.add(boardContainer, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);
    }

    private void buildBottomPanel() {
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 16));
        bottom.setBackground(new Color(236, 240, 245));

        loginBtn = createButton("Login", new Color(33, 103, 172));
        registerBtn = createButton("Register", new Color(52, 152, 219));
        logoutBtn = createButton("Logout", new Color(120, 120, 120));
        solveBtn = createButton("Solve", new Color(142, 68, 173));
        checkBtn = createButton("Check Board", new Color(46, 160, 90));
        manageBtn = createButton("Manage Puzzle", new Color(211, 84, 0));

        logoutBtn.setVisible(false);
        manageBtn.setVisible(false);

        bottom.add(loginBtn);
        bottom.add(registerBtn);
        bottom.add(logoutBtn);
        bottom.add(solveBtn);
        bottom.add(checkBtn);
        bottom.add(manageBtn);

        loginBtn.addActionListener(e -> {
            if (loggedInUser == null) {
                openLogin();
            } else {
                JOptionPane.showMessageDialog(this, "Bạn đã đăng nhập: " + loggedInUser);
            }
        });

        registerBtn.addActionListener(e -> openRegister());

        logoutBtn.addActionListener(e -> doLogout());

        checkBtn.addActionListener(e -> {
            if (loggedInUser == null) {
                JOptionPane.showMessageDialog(this, "Vui lòng đăng nhập để kiểm tra!");
                return;
            }
            handleCheckBoard();
        });

        solveBtn.addActionListener(e -> {
            if (loggedInUser == null) {
                JOptionPane.showMessageDialog(this, "Vui lòng đăng nhập để dùng Solver!");
                return;
            }
            handleSolveBoard();
        });

        manageBtn.addActionListener(e -> {
            if (loggedInUser == null || !SQL.isAdmin(loggedInUser)) {
                JOptionPane.showMessageDialog(this, "Chỉ admin mới có quyền quản lý puzzle.");
                return;
            }
            Puzzle.openManager(this, loggedInUser, () -> {
                Board.ReadData();
                refreshLevelList();
            });
        });

        add(bottom, BorderLayout.SOUTH);
    }

    private JButton createButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 15));
        button.setPreferredSize(new Dimension(150, 42));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(8, 18, 8, 18));
        return button;
    }

    private void updateLoggedInStateUI() {
        boolean loggedIn = loggedInUser != null;

        loginBtn.setText(loggedIn ? "Hi, " + loggedInUser : "Login");
        logoutBtn.setVisible(loggedIn);
        registerBtn.setVisible(!loggedIn);
        manageBtn.setVisible(loggedIn && SQL.isAdmin(loggedInUser));
    }

    private void doLogout() {
        timer.stop();
        timer.reset();

        loggedInUser = null;
        loggedInRole = "user";

        Board.updateBoard(null);
        Board.currentIndex = -1;

        levelModel.clear();
        labelBest.setText("Best: --:--");
        showWelcomeCard();
        cells = null;

        updateLoggedInStateUI();
        JOptionPane.showMessageDialog(this, "Đã đăng xuất.");
    }

    private void showWelcomeCard() {
        boardContainer.removeAll();

        JPanel welcomeCard = new JPanel(new BorderLayout(0, 10));
        welcomeCard.setBackground(Color.WHITE);
        welcomeCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 228, 236), 1, true),
                new EmptyBorder(22, 22, 22, 22)));

        JLabel welcomeLabel = new JLabel("Vui lòng đăng nhập để bắt đầu trò chơi.");
        welcomeLabel.setFont(new Font("Segoe UI", Font.ITALIC, 16));
        welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        welcomeCard.add(welcomeLabel, BorderLayout.CENTER);

        boardContainer.add(welcomeCard);
        boardContainer.revalidate();
        boardContainer.repaint();
    }

    /**
     * Mở hộp thoại đăng nhập.
     */
    void openLogin() {
        JDialog login = new JDialog(this, "Login", true);
        login.setSize(380, 330);
        login.setLayout(new BorderLayout());
        login.getContentPane().setBackground(Color.WHITE);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(33, 103, 172));
        header.setBorder(new EmptyBorder(14, 16, 14, 16));

        JLabel loginTitle = new JLabel("Đăng nhập", JLabel.LEFT);
        loginTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        loginTitle.setForeground(Color.WHITE);

        JLabel loginSub = new JLabel("Sử dụng gmail và password");
        loginSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        loginSub.setForeground(new Color(230, 240, 250));

        header.add(loginTitle, BorderLayout.NORTH);
        header.add(loginSub, BorderLayout.SOUTH);

        JPanel form = new JPanel(new GridLayout(2, 2, 10, 12));
        form.setBorder(new EmptyBorder(18, 18, 16, 18));
        form.setBackground(Color.WHITE);

        JTextField gmailField = new JTextField();
        JPasswordField passField = new JPasswordField();

        form.add(new JLabel("Gmail:"));
        form.add(gmailField);
        form.add(new JLabel("Password:"));
        form.add(passField);

        JButton submit = createButton("Login", new Color(33, 103, 172));
        JButton goRegister = createButton("Register", new Color(52, 152, 219));

        submit.addActionListener(e -> {
            String username = SQL.login(gmailField.getText(), new String(passField.getPassword()));
            if (username != null) {
                loggedInUser = username;
                loggedInRole = SQL.getUserRole(username);
                updateLoggedInStateUI();
                refreshLevelList();
                login.dispose();
            } else {
                JOptionPane.showMessageDialog(login, "Sai email hoặc mật khẩu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        goRegister.addActionListener(e -> {
            login.dispose();
            openRegister();
        });

        JPanel bp = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        bp.setBackground(Color.WHITE);
        bp.setBorder(new EmptyBorder(0, 16, 14, 16));
        bp.add(goRegister);
        bp.add(submit);

        login.add(header, BorderLayout.NORTH);
        login.add(form, BorderLayout.CENTER);
        login.add(bp, BorderLayout.SOUTH);
        login.setLocationRelativeTo(this);
        login.setVisible(true);
    }

    /**
     * Mở hộp thoại đăng ký tài khoản mới.
     */
    void openRegister() {
        JDialog dialog = new JDialog(this, "Register", true);
        dialog.setSize(420, 360);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(Color.WHITE);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(33, 103, 172));
        header.setBorder(new EmptyBorder(14, 16, 14, 16));

        JLabel title = new JLabel("Đăng ký", JLabel.LEFT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Color.WHITE);

        JLabel sub = new JLabel("Tạo tài khoản user mới");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(new Color(230, 240, 250));

        header.add(title, BorderLayout.NORTH);
        header.add(sub, BorderLayout.SOUTH);

        JPanel form = new JPanel(new GridLayout(3, 2, 10, 12));
        form.setBorder(new EmptyBorder(18, 18, 16, 18));
        form.setBackground(Color.WHITE);

        JTextField gmailField = new JTextField();
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JPasswordField confirmField = new JPasswordField();

        form.add(new JLabel("Gmail:"));
        form.add(gmailField);
        form.add(new JLabel("Username:"));
        form.add(usernameField);
        form.add(new JLabel("Password:"));
        form.add(passwordField);

        JPanel lower = new JPanel(new GridLayout(1, 2, 10, 12));
        lower.setBackground(Color.WHITE);
        lower.add(new JLabel("Confirm:"));
        lower.add(confirmField);

        JButton submit = createButton("Create account", new Color(33, 103, 172));

        submit.addActionListener(e -> {
            String gmail = gmailField.getText().trim();
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            String confirm = new String(confirmField.getPassword());

            if (gmail.isEmpty() || username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng điền đầy đủ thông tin.");
                return;
            }

            if (!password.equals(confirm)) {
                JOptionPane.showMessageDialog(dialog, "Mật khẩu xác nhận không khớp.");
                return;
            }

            if (SQL.signUp(gmail, password, username)) {
                JOptionPane.showMessageDialog(dialog, "Đăng ký thành công. Hãy đăng nhập.");
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "Không thể đăng ký. Gmail hoặc username có thể đã tồn tại.");
            }
        });

        JPanel centerWrap = new JPanel(new BorderLayout(0, 8));
        centerWrap.setBackground(Color.WHITE);
        centerWrap.add(form, BorderLayout.CENTER);
        centerWrap.add(lower, BorderLayout.SOUTH);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        bottom.setBackground(Color.WHITE);
        bottom.setBorder(new EmptyBorder(0, 16, 14, 16));
        bottom.add(submit);

        dialog.add(header, BorderLayout.NORTH);
        dialog.add(centerWrap, BorderLayout.CENTER);
        dialog.add(bottom, BorderLayout.SOUTH);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void loadLevel(int index) {
        if (index < 0 || index >= Board.getPuzzleCount()) {
            return;
        }

        timer.stop();
        timer.reset();

        Board.setPuzzle(index);
        drawBoard(Board.getBoard());

        int userId = SQL.getUserId(loggedInUser);
        int levelId = Board.getLevelId(index);
        int best = SQL.getBestTime(userId, levelId);
        if (best >= 0) {
            labelBest.setText("Best: " + formatTime(best));
        } else {
            labelBest.setText("Best: --:--");
        }

        timer.start();
    }

    private int getUnlockedIndex(List<Integer> completed) {
        if (Board.getPuzzleCount() == 0) {
            return 0;
        }

        Set<Integer> completedSet = new HashSet<>(completed);
        int unlockedIndex = 0;

        for (int i = 0; i < Board.getPuzzleCount(); i++) {
            int levelId = Board.getLevelId(i);
            if (completedSet.contains(levelId)) {
                unlockedIndex = i + 1;
            } else {
                break;
            }
        }

        return Math.min(unlockedIndex, Board.getPuzzleCount() - 1);
    }

    private void refreshLevelList() {
        if (loggedInUser == null) {
            levelModel.clear();
            showWelcomeCard();
            return;
        }

        int previousSelected = levelList.getSelectedIndex();
        List<Integer> completed = SQL.getCompletedLevels(loggedInUser);
        levelModel.clear();

        int unlockedIndex = getUnlockedIndex(completed);
        Set<Integer> completedSet = new HashSet<>(completed);

        updatingLevelList = true;
        for (int i = 0; i < Board.getPuzzleCount(); i++) {
            int levelId = Board.getLevelId(i);
            boolean isDone = completedSet.contains(levelId);
            boolean isLocked = i > unlockedIndex;

            String status = "";
            if (isLocked) {
                status = " [LOCKED]";
            } else if (isDone) {
                status = " [DONE]";
            }

            int[][] puzzle = Board.getPuzzle(i);
            levelModel.addElement("Level " + (i + 1) + " (" + puzzle.length + "x" + puzzle.length + ")" + status);
        }

        if (Board.getPuzzleCount() > 0) {
            int targetIndex = previousSelected;
            if (targetIndex < 0 || targetIndex >= Board.getPuzzleCount()) {
                targetIndex = Math.min(unlockedIndex, Board.getPuzzleCount() - 1);
            }
            levelList.setSelectedIndex(targetIndex);
        }
        updatingLevelList = false;

        if ((Board.getCurrentIndex() < 0 || cells == null) && Board.getPuzzleCount() > 0) {
            int index = levelList.getSelectedIndex();
            if (index >= 0) {
                loadLevel(index);
            }
        }
    }

    private void handleCheckBoard() {
        int size = Board.getSize();
        if (size == 0 || cells == null) {
            JOptionPane.showMessageDialog(this, "Chưa có màn chơi nào được tải.");
            return;
        }

        int[][] currentData = readCurrentBoard(size);

        try {
            Board.updateBoard(currentData);

            if (Validator.isValid(Board.getBoard())) {
                timer.stop();
                int time = timer.getTime();

                int levelIndex = levelList.getSelectedIndex();
                if (levelIndex < 0) {
                    JOptionPane.showMessageDialog(this, "Chưa chọn màn chơi.");
                    return;
                }

                int userId = SQL.getUserId(loggedInUser);
                int levelId = Board.getLevelId(levelIndex);

                SQL.markCompleted(loggedInUser, levelId);
                SQL.saveBestTime(userId, levelId, time);

                int best = SQL.getBestTime(userId, levelId);
                if (best >= 0) {
                    labelBest.setText("Best: " + formatTime(best));
                }

                JOptionPane.showMessageDialog(this, "Chúc mừng! Bạn đã giải đúng!\nTime: " + labelTime.getText());
                refreshLevelList();
            } else {
                JOptionPane.showMessageDialog(this, "Bảng chưa đúng hoặc còn ô trống.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Dữ liệu nhập vào không hợp lệ!");
        }
    }

    private void handleSolveBoard() {
        int size = Board.getSize();
        if (size == 0 || cells == null) {
            JOptionPane.showMessageDialog(this, "Chưa có màn chơi nào được tải.");
            return;
        }

        int userId = SQL.getUserId(loggedInUser);
        if (!SQL.canUseSolve(userId)) {
            long remain = SQL.getSolveCooldownRemainingSeconds(userId);
            JOptionPane.showMessageDialog(this, "Bạn chỉ được dùng Solve mỗi 10 phút một lần.\nHãy đợi thêm " + remain + " giây.");
            return;
        }

        // Đánh dấu ngay khi dùng để ngăn spam Solve.
        SQL.markSolveUsed(userId);

        int[][] currentData = readCurrentBoard(size);
        int[][] solvingBoard = Board.copyBoard(currentData);

        boolean solved = SudokuSolver.solve(solvingBoard);
        if (!solved) {
            JOptionPane.showMessageDialog(this, "Không thể giải bảng này.");
            return;
        }

        Board.updateBoard(solvingBoard);
        applyBoardToUI(solvingBoard);

        timer.stop();

        int levelIndex = levelList.getSelectedIndex();
        if (levelIndex >= 0) {
            int levelId = Board.getLevelId(levelIndex);
            SQL.markCompleted(loggedInUser, levelId);
            SQL.saveBestTime(userId, levelId, timer.getTime());
            int best = SQL.getBestTime(userId, levelId);
            if (best >= 0) {
                labelBest.setText("Best: " + formatTime(best));
            }
        }

        JOptionPane.showMessageDialog(this, "Solver đã điền xong bảng.");
        refreshLevelList();
    }

    private int[][] readCurrentBoard(int size) {
        int[][] currentData = new int[size][size];

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                String val = cells[i][j].getText().trim().toUpperCase();

                if (val.isEmpty()) {
                    currentData[i][j] = 0;
                } else if (val.length() == 1 && val.charAt(0) >= 'A' && val.charAt(0) <= 'G') {
                    currentData[i][j] = val.charAt(0) - 'A' + 10;
                } else {
                    currentData[i][j] = Integer.parseInt(val);
                }
            }
        }

        return currentData;
    }

    private void applyBoardToUI(int[][] puzzleData) {
        for (int i = 0; i < puzzleData.length; i++) {
            for (int j = 0; j < puzzleData.length; j++) {
                JTextField cell = cells[i][j];
                int val = puzzleData[i][j];

                cell.setText(val > 9 ? String.valueOf((char) ('A' + val - 10)) : String.valueOf(val));
                cell.setEditable(false);
                cell.setBackground(new Color(225, 230, 236));
                cell.setForeground(Color.BLACK);
            }
        }
    }

    private String formatTime(int seconds) {
        int mins = seconds / 60;
        int secs = seconds % 60;
        return String.format("%02d:%02d", mins, secs);
    }

    private void drawBoard(int[][] puzzleData) {
        boardContainer.removeAll();

        int size = puzzleData.length;
        int boxSize = (int) Math.sqrt(size);
        int cellSize = size == 16 ? 32 : 52;
        int boardSize = size * cellSize;

        JPanel boardHolder = new JPanel(new GridBagLayout());
        boardHolder.setBackground(new Color(236, 240, 245));

        boardPanel = new JPanel(new GridLayout(size, size, 0, 0));
        boardPanel.setPreferredSize(new Dimension(boardSize, boardSize));
        boardPanel.setMaximumSize(new Dimension(boardSize, boardSize));
        boardPanel.setMinimumSize(new Dimension(boardSize, boardSize));
        boardPanel.setBackground(Color.WHITE);
        boardPanel.setBorder(new CompoundBorder(
                new LineBorder(new Color(26, 47, 77), 3, true),
                new EmptyBorder(0, 0, 0, 0)));

        cells = new JTextField[size][size];

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                JTextField cell = new JTextField();
                int row = i;
                int col = j;

                cell.setHorizontalAlignment(JTextField.CENTER);
                cell.setFont(new Font("Segoe UI", Font.BOLD, size == 16 ? 14 : 22));
                cell.setBorder(createCellBorder(i, j, size, boxSize));
                cell.setPreferredSize(new Dimension(cellSize, cellSize));
                cell.setMinimumSize(new Dimension(cellSize, cellSize));
                cell.setMaximumSize(new Dimension(cellSize, cellSize));

                int val = puzzleData[i][j];
                if (val != 0) {
                    cell.setText(val > 9 ? String.valueOf((char) ('A' + val - 10)) : String.valueOf(val));
                    cell.setEditable(false);
                    cell.setBackground(new Color(225, 230, 236));
                    cell.setForeground(new Color(25, 25, 25));
                } else {
                    cell.setBackground(Color.WHITE);
                    cell.setForeground(new Color(33, 103, 172));

                    cell.addKeyListener(new KeyAdapter() {
                        @Override
                        public void keyReleased(KeyEvent e) {
                            String text = cell.getText().trim().toUpperCase();

                            if (text.isEmpty()) {
                                cell.setBackground(Color.WHITE);
                                return;
                            }

                            try {
                                int num;
                                if (text.length() == 1 && text.charAt(0) >= 'A' && text.charAt(0) <= 'G') {
                                    num = text.charAt(0) - 'A' + 10;
                                } else {
                                    num = Integer.parseInt(text);
                                }

                                int[][] tempBoard = readCurrentBoard(size);
                                tempBoard[row][col] = num;

                                if (Validator.isValid(tempBoard, row, col, num)) {
                                    cell.setBackground(Color.WHITE);
                                } else {
                                    cell.setBackground(new Color(255, 214, 214));
                                }
                            } catch (Exception ex) {
                                cell.setBackground(new Color(255, 214, 214));
                            }
                        }
                    });
                }

                cells[i][j] = cell;
                boardPanel.add(cell);
            }
        }

        boardHolder.add(boardPanel);
        boardContainer.add(boardHolder);
        boardContainer.revalidate();
        boardContainer.repaint();
    }

    private Border createCellBorder(int i, int j, int size, int boxSize) {
        int top = (i % boxSize == 0) ? 3 : 1;
        int left = (j % boxSize == 0) ? 3 : 1;
        int bottom = (i == size - 1) ? 3 : 1;
        int right = (j == size - 1) ? 3 : 1;
        return new MatteBorder(top, left, bottom, right, new Color(26, 47, 77));
    }
}
