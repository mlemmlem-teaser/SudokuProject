import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class SudokuUI extends JFrame {

    JTextField[][] cells;
    JList<String> levelList;
    JPanel centerPanel;
    JPanel boardPanel;
    JButton loginBtn;
    
    // Lưu trữ người dùng hiện tại
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

        // Tạo danh sách level linh hoạt dựa trên số lượng puzzle đọc từ SQL
        String[] levels = new String[Board.puzzles.size()];
        for (int i = 0; i < Board.puzzles.size(); i++) {
            levels[i] = "Level " + (i + 1) + " (" + Board.puzzles.get(i).length + "x" + Board.puzzles.get(i).length + ")";
        }

        levelList = new JList<>(levels);
        levelList.setFont(new Font("Arial", Font.BOLD, 16));
        levelList.setSelectedIndex(0);
        levelList.setSelectionBackground(new Color(100, 150, 255));

        // Bắt sự kiện khi click chọn Level
        levelList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int index = levelList.getSelectedIndex();
                Board.setPuzzle(index);
                drawBoard(Board.board);
            }
        });

        JScrollPane levelPane = new JScrollPane(levelList);
        levelPane.setPreferredSize(new Dimension(160, 0));
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
        checkBtn.setBackground(new Color(76, 175, 80)); // Màu xanh lá cho nổi bật
        checkBtn.setForeground(Color.WHITE);
        bottom.add(checkBtn);

        checkBtn.addActionListener(e -> {
            int size = Board.getSize(); // Lấy kích thước hiện tại (4, 9, hoặc 16)
            int[][] currentData = new int[size][size];
            
            try {
                for (int i = 0; i < size; i++) {
                    for (int j = 0; j < size; j++) {
                        String val = cells[i][j].getText().trim().toUpperCase();
                        if (val.isEmpty()) {
                            currentData[i][j] = 0;
                        } else {
                            // Xử lý cả chữ (A-G cho bảng 16x16) và số (1-9)
                            if (val.length() == 1 && val.charAt(0) >= 'A' && val.charAt(0) <= 'G') {
                                currentData[i][j] = val.charAt(0) - 'A' + 10;
                            } else {
                                currentData[i][j] = Integer.parseInt(val);
                            }
                        }
                    }
                }

                // Cập nhật mảng vào Board và gọi Validator để kiểm tra
                Board.updateBoard(currentData);
                if (Validator.isValid(Board.board)) {
                    JOptionPane.showMessageDialog(this, "Chúc mừng! Bạn đã giải đúng!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Bảng Sudoku chưa đúng hoặc còn ô trống.", "Thông báo", JOptionPane.WARNING_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Dữ liệu nhập vào không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
        // ------------------------------------------

        add(bottom, BorderLayout.SOUTH);
        // ...
        add(bottom, BorderLayout.SOUTH);

        loginBtn.addActionListener(e -> {
            if (loggedInUser == null) openLogin();
            else JOptionPane.showMessageDialog(this, "Bạn đã đăng nhập với tên: " + loggedInUser);
        });

        // Vẽ bảng đầu tiên mặc định
        if (!Board.puzzles.isEmpty()) {
            drawBoard(Board.board);
        }

        setLocationRelativeTo(null);
        setVisible(true);
    }

    // Hàm vẽ lưới Sudoku động (chạy được 4x4, 9x9, 16x16)
    private void drawBoard(int[][] puzzleData) {
        int size = puzzleData.length;
        int boxSize = (int) Math.sqrt(size);

        if (boardPanel != null) {
            centerPanel.remove(boardPanel);
        }

        boardPanel = new JPanel(new GridLayout(size, size));
        boardPanel.setBorder(new LineBorder(Color.BLACK, 3));
        cells = new JTextField[size][size];

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                JTextField cell = new JTextField();
                cell.setHorizontalAlignment(JTextField.CENTER);
                
                // Thu nhỏ font chữ nếu là bảng 16x16 để hiển thị vừa vặn hơn
                cell.setFont(new Font("Arial", Font.BOLD, size == 16 ? 14 : 22));

                int val = puzzleData[i][j];
                if (val != 0) {
                    // Chuyển lại giá trị 10-16 thành A-G cho bảng 16x16
                    if (val > 9) {
                        cell.setText(String.valueOf((char) ('A' + val - 10)));
                    } else {
                        cell.setText(String.valueOf(val));
                    }
                    cell.setEditable(false);
                    cell.setBackground(new Color(230, 230, 230));
                    cell.setForeground(Color.BLACK);
                }

                // Tính toán viền cho từng box (ví dụ: chia khối 2x2 cho bảng 4, 3x3 cho bảng 9, 4x4 cho bảng 16)
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

    // Logic Đăng nhập gọi SQL
    void openLogin() {
        JDialog login = new JDialog(this, "Login", true);
        login.setSize(320, 220);
        login.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(2, 2, 10, 10));
        form.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel gmailLabel = new JLabel("Gmail:");
        JTextField gmailField = new JTextField();

        JLabel passLabel = new JLabel("Password:");
        JPasswordField passField = new JPasswordField();

        form.add(gmailLabel);
        form.add(gmailField);
        form.add(passLabel);
        form.add(passField);

        JButton submit = new JButton("Login");
        submit.setPreferredSize(new Dimension(100, 35));

        JPanel btnPanel = new JPanel();
        btnPanel.add(submit);

        login.add(form, BorderLayout.CENTER);
        login.add(btnPanel, BorderLayout.SOUTH);

        submit.addActionListener(e -> {
            String gmail = gmailField.getText();
            String pass = new String(passField.getPassword());
            
            // Gọi hàm login từ SQL
            String username = SQL.login(gmail, pass);
            
            if (username != null) {
                JOptionPane.showMessageDialog(login, "Đăng nhập thành công! Xin chào " + username);
                loggedInUser = username;
                loginBtn.setText("Hi, " + username);
                login.dispose();
            } else {
                JOptionPane.showMessageDialog(login, "Sai email hoặc mật khẩu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        login.setLocationRelativeTo(this);
        login.setVisible(true);
    }
    
}