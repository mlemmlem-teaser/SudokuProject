import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class SudokuUI extends JFrame {

    JTextField[][] cells;
    JList<String> levelList;
    DefaultListModel<String> levelModel; // Sử dụng Model để có thể cập nhật danh sách
    JPanel centerPanel;
    JPanel boardPanel;
    JButton loginBtn;
    
    String loggedInUser = null;

    public SudokuUI() {
        setTitle("Sudoku Game");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JLabel title = new JLabel("SUDOKU GAME", JLabel.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setBorder(new EmptyBorder(10, 0, 10, 0));
        add(title, BorderLayout.NORTH);

        centerPanel = new JPanel();
        add(centerPanel, BorderLayout.CENTER);

        // Khởi tạo ListModel trống ban đầu
        levelModel = new DefaultListModel<>();
        levelList = new JList<>(levelModel);
        levelList.setFont(new Font("Arial", Font.BOLD, 16));
        levelList.setSelectionBackground(new Color(100, 150, 255));

        // Sự kiện chọn Level (Có kiểm tra khóa)
        levelList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && loggedInUser != null) {
                int index = levelList.getSelectedIndex();
                if (index == -1) return;

                List<Integer> completed = SQL.getCompletedLevels(loggedInUser);
                // Nếu chọn màn vượt quá số màn đã hoàn thành -> Khóa
                if (index > completed.size()) {
                    JOptionPane.showMessageDialog(this, "Màn chơi này đang bị khóa! Hãy hoàn thành các màn trước.");
                    levelList.setSelectedIndex(completed.size()); 
                    return;
                }
                
                Board.setPuzzle(index);
                drawBoard(Board.board);
            }
        });

        JScrollPane levelPane = new JScrollPane(levelList);
        levelPane.setPreferredSize(new Dimension(200, 0));
        levelPane.setBorder(new TitledBorder("Levels"));
        add(levelPane, BorderLayout.WEST);

        JPanel bottom = new JPanel();
        loginBtn = new JButton("Login");
        loginBtn.setFont(new Font("Arial", Font.BOLD, 16));
        loginBtn.setPreferredSize(new Dimension(150, 40));
        bottom.add(loginBtn);

        JButton checkBtn = new JButton("Check Board");
        checkBtn.setFont(new Font("Arial", Font.BOLD, 16));
        checkBtn.setPreferredSize(new Dimension(150, 40));
        checkBtn.setBackground(new Color(76, 175, 80));
        checkBtn.setForeground(Color.WHITE);
        bottom.add(checkBtn);

        checkBtn.addActionListener(e -> {
            if (loggedInUser == null) {
                JOptionPane.showMessageDialog(this, "Vui lòng đăng nhập để kiểm tra!");
                return;
            }
            handleCheckBoard();
        });

        add(bottom, BorderLayout.SOUTH);

        loginBtn.addActionListener(e -> {
            if (loggedInUser == null) openLogin();
            else JOptionPane.showMessageDialog(this, "Bạn đã đăng nhập: " + loggedInUser);
        });

        // Không vẽ board và không hiện level cho đến khi login
        centerPanel.add(new JLabel("Vui lòng đăng nhập để bắt đầu trò chơi."));

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void refreshLevelList() {
        if (loggedInUser == null) return;

        List<Integer> completed = SQL.getCompletedLevels(loggedInUser);
        levelModel.clear();
        
        int maxUnlockedIndex = completed.size(); 

        for (int i = 0; i < Board.puzzles.size(); i++) {
            String status = (i <= maxUnlockedIndex) ? "" : " [LOCKED]";
            levelModel.addElement("Level " + (i + 1) + " (" + Board.puzzles.get(i).length + "x" + Board.puzzles.get(i).length + ")" + status);
        }
        
        // Mặc định chọn màn mới nhất chưa hoàn thành
        levelList.setSelectedIndex(Math.min(maxUnlockedIndex, Board.puzzles.size() - 1));
    }

    private void handleCheckBoard() {
        int size = Board.getSize();
        int[][] currentData = new int[size][size];
        try {
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    String val = cells[i][j].getText().trim().toUpperCase();
                    if (val.isEmpty()) currentData[i][j] = 0;
                    else {
                        if (val.length() == 1 && val.charAt(0) >= 'A' && val.charAt(0) <= 'G') 
                            currentData[i][j] = val.charAt(0) - 'A' + 10;
                        else currentData[i][j] = Integer.parseInt(val);
                    }
                }
            }
            Board.updateBoard(currentData);
            if (Validator.isValid(Board.board)) {
                JOptionPane.showMessageDialog(this, "Chúc mừng! Bạn đã giải đúng!");
                // Ở đây bạn nên gọi thêm SQL.updateProgress(...) để lưu vào DB
                refreshLevelList(); 
            } else {
                JOptionPane.showMessageDialog(this, "Bảng chưa đúng hoặc còn ô trống.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Dữ liệu nhập vào không hợp lệ!");
        }
    }

    private void drawBoard(int[][] puzzleData) {
        centerPanel.removeAll(); // Xóa label thông báo ban đầu
        int size = puzzleData.length;
        int boxSize = (int) Math.sqrt(size);

        boardPanel = new JPanel(new GridLayout(size, size));
        boardPanel.setBorder(new LineBorder(Color.BLACK, 3));
        cells = new JTextField[size][size];

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                JTextField cell = new JTextField();
                cell.setHorizontalAlignment(JTextField.CENTER);
                cell.setFont(new Font("Arial", Font.BOLD, size == 16 ? 14 : 22));

                int val = puzzleData[i][j];
                if (val != 0) {
                    cell.setText(val > 9 ? String.valueOf((char) ('A' + val - 10)) : String.valueOf(val));
                    cell.setEditable(false);
                    cell.setBackground(new Color(230, 230, 230));
                }

                int top = (i % boxSize == 0) ? 2 : 1;
                int left = (j % boxSize == 0) ? 2 : 1;
                int bottom = (i == size - 1) ? 2 : 1;
                int right = (j == size - 1) ? 2 : 1;
                cell.setBorder(new MatteBorder(top, left, bottom, right, Color.BLACK));
                cells[i][j] = cell;
                boardPanel.add(cell);
            }
        }
        centerPanel.add(boardPanel);
        centerPanel.revalidate();
        centerPanel.repaint();
    }

    void openLogin() {
        JDialog login = new JDialog(this, "Login", true);
        login.setSize(320, 220);
        login.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(2, 2, 10, 10));
        form.setBorder(new EmptyBorder(20, 20, 20, 20));

        JTextField gmailField = new JTextField();
        JPasswordField passField = new JPasswordField();

        form.add(new JLabel("Gmail:")); form.add(gmailField);
        form.add(new JLabel("Password:")); form.add(passField);

        JButton submit = new JButton("Login");
        submit.addActionListener(e -> {
            String username = SQL.login(gmailField.getText(), new String(passField.getPassword()));
            if (username != null) {
                loggedInUser = username;
                loginBtn.setText("Hi, " + username);
                refreshLevelList(); // Tải danh sách màn chơi khi login thành công
                login.dispose();
            } else {
                JOptionPane.showMessageDialog(login, "Sai email hoặc mật khẩu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        login.add(form, BorderLayout.CENTER);
        JPanel bp = new JPanel(); bp.add(submit);
        login.add(bp, BorderLayout.SOUTH);
        login.setLocationRelativeTo(this);
        login.setVisible(true);
    }
}