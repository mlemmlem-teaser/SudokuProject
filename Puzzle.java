import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

/**
 * Cửa sổ quản lý puzzle dành riêng cho admin.
 * Cho phép thêm, sửa, xóa level trong bảng levels.
 */
public class Puzzle extends JDialog {

    private final String adminUsername;
    private final Runnable onDataChanged;

    private final DefaultListModel<SQL.PuzzleRecord> model = new DefaultListModel<>();
    private final JList<SQL.PuzzleRecord> list = new JList<>(model);

    private final JTextField sizeField = new JTextField();
    private final JTextField titleField = new JTextField();
    private final JTextField difficultyField = new JTextField();
    private final JTextArea boardArea = new JTextArea(8, 28);

    private final JLabel statusLabel = new JLabel(" ");

    private SQL.PuzzleRecord selectedRecord;

    public static void openManager(JFrame parent, String adminUsername, Runnable onDataChanged) {
        Puzzle dialog = new Puzzle(parent, adminUsername, onDataChanged);
        dialog.setVisible(true);
    }

    private Puzzle(JFrame parent, String adminUsername, Runnable onDataChanged) {
        super(parent, "Puzzle Manager", true);
        this.adminUsername = adminUsername;
        this.onDataChanged = onDataChanged;

        setSize(980, 640);
        setMinimumSize(new Dimension(900, 580));
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(12, 12));
        getContentPane().setBackground(new Color(236, 240, 245));

        if (!SQL.isAdmin(adminUsername)) {
            JOptionPane.showMessageDialog(parent, "Chỉ admin mới được mở phần quản lý puzzle.");
            dispose();
            return;
        }

        buildUI();
        loadPuzzles();
    }

    /**
     * Tạo giao diện quản lý puzzle.
     */
    private void buildUI() {
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(new Color(33, 103, 172));
        top.setBorder(new EmptyBorder(14, 16, 14, 16));

        JLabel title = new JLabel("Puzzle Manager");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(Color.WHITE);

        JLabel sub = new JLabel("Thêm, sửa, xóa puzzle chỉ dành cho admin");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(new Color(230, 240, 250));

        JPanel titleWrap = new JPanel(new BorderLayout());
        titleWrap.setOpaque(false);
        titleWrap.add(title, BorderLayout.NORTH);
        titleWrap.add(sub, BorderLayout.SOUTH);

        top.add(titleWrap, BorderLayout.WEST);
        add(top, BorderLayout.NORTH);

        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        list.setFixedCellHeight(34);
        list.setCellRenderer((jList, value, index, isSelected, cellHasFocus) -> {
            JLabel label = new JLabel(formatRecord(value));
            label.setOpaque(true);
            label.setBorder(new EmptyBorder(4, 10, 4, 10));
            label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            label.setBackground(isSelected ? jList.getSelectionBackground() : Color.WHITE);
            label.setForeground(isSelected ? jList.getSelectionForeground() : new Color(45, 45, 45));
            return label;
        });

        list.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                SQL.PuzzleRecord rec = list.getSelectedValue();
                if (rec != null) {
                    selectedRecord = rec;
                    fillForm(rec);
                }
            }
        });

        JScrollPane listPane = new JScrollPane(list);
        listPane.setPreferredSize(new Dimension(310, 0));
        listPane.setBorder(BorderFactory.createTitledBorder("Danh sách puzzle"));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(14, 14, 14, 14));

        boardArea.setLineWrap(true);
        boardArea.setWrapStyleWord(true);
        boardArea.setFont(new Font("Consolas", Font.PLAIN, 14));

        int y = 0;
        y = addRow(form, y, "Size:", sizeField);
        y = addRow(form, y, "Title:", titleField);
        y = addRow(form, y, "Difficulty:", difficultyField);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = y;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.insets = new Insets(8, 6, 6, 8);
        form.add(new JLabel("Board data:"), c);

        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = y;
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(8, 0, 6, 0);
        form.add(new JScrollPane(boardArea), c);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttons.setOpaque(false);

        JButton addBtn = createButton("Add", new Color(46, 160, 90));
        JButton updateBtn = createButton("Update", new Color(33, 103, 172));
        JButton deleteBtn = createButton("Delete", new Color(192, 57, 43));
        JButton refreshBtn = createButton("Refresh", new Color(142, 68, 173));
        JButton clearBtn = createButton("Clear", new Color(120, 120, 120));

        buttons.add(addBtn);
        buttons.add(updateBtn);
        buttons.add(deleteBtn);
        buttons.add(refreshBtn);
        buttons.add(clearBtn);

        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = y + 1;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(12, 0, 0, 0);
        form.add(buttons, c);

        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = y + 2;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(8, 0, 0, 0);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        statusLabel.setForeground(new Color(90, 90, 90));
        form.add(statusLabel, c);

        add(listPane, BorderLayout.WEST);
        add(form, BorderLayout.CENTER);

        addBtn.addActionListener(e -> addPuzzle());
        updateBtn.addActionListener(e -> updatePuzzle());
        deleteBtn.addActionListener(e -> deletePuzzle());
        refreshBtn.addActionListener(e -> loadPuzzles());
        clearBtn.addActionListener(e -> clearForm());
    }

    private JButton createButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(8, 14, 8, 14));
        return button;
    }

    private int addRow(JPanel form, int y, String label, JComponent field) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = y;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(8, 6, 6, 8);
        form.add(new JLabel(label), c);

        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = y;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(8, 0, 6, 0);
        form.add(field, c);

        return y + 1;
    }

    /**
     * Nạp lại danh sách puzzle.
     */
    private void loadPuzzles() {
        model.clear();
        List<SQL.PuzzleRecord> records = SQL.readPuzzleRecords();
        for (SQL.PuzzleRecord record : records) {
            model.addElement(record);
        }

        if (!model.isEmpty()) {
            list.setSelectedIndex(0);
        } else {
            selectedRecord = null;
            clearForm();
        }

        statusLabel.setText("Đã nạp " + model.size() + " puzzle.");
    }

    private String formatRecord(SQL.PuzzleRecord record) {
        if (record == null) {
            return "";
        }

        String title = record.title == null || record.title.trim().isEmpty() ? "(no title)" : record.title.trim();
        String diff = record.difficulty == null || record.difficulty.trim().isEmpty() ? "Unknown" : record.difficulty.trim();
        return "#" + record.levelId + " | " + title + " | " + record.size + "x" + record.size + " | " + diff;
    }

    private void fillForm(SQL.PuzzleRecord record) {
        sizeField.setText(String.valueOf(record.size));
        titleField.setText(record.title == null ? "" : record.title);
        difficultyField.setText(record.difficulty == null ? "" : record.difficulty);
        boardArea.setText(record.boardData == null ? "" : record.boardData);
        statusLabel.setText("Đang chỉnh sửa puzzle #" + record.levelId);
    }

    private void clearForm() {
        selectedRecord = null;
        list.clearSelection();
        sizeField.setText("");
        titleField.setText("");
        difficultyField.setText("");
        boardArea.setText("");
        statusLabel.setText(" ");
    }

    /**
     * Chuẩn hóa dữ liệu board trước khi lưu.
     */
    private String normalizeBoard(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.replaceAll("\\s+", "").toUpperCase();
    }

    private boolean isPerfectSquare(int size) {
        int root = (int) Math.sqrt(size);
        return root * root == size;
    }

    private boolean validateBoardData(int size, String boardData) {
        if (size <= 0 || !isPerfectSquare(size)) {
            return false;
        }
        if (boardData == null) {
            return false;
        }

        String normalized = normalizeBoard(boardData);
        if (normalized.length() != size * size) {
            return false;
        }

        for (int i = 0; i < normalized.length(); i++) {
            char c = normalized.charAt(i);
            boolean ok = (c >= '0' && c <= '9') || (c >= 'A' && c <= 'G');
            if (!ok) {
                return false;
            }
        }

        return true;
    }

    private void addPuzzle() {
        if (!SQL.isAdmin(adminUsername)) {
            JOptionPane.showMessageDialog(this, "Chỉ admin mới có quyền thêm puzzle.");
            return;
        }

        try {
            int size = Integer.parseInt(sizeField.getText().trim());
            String title = titleField.getText().trim();
            String difficulty = difficultyField.getText().trim();
            String boardData = normalizeBoard(boardArea.getText());

            if (!validateBoardData(size, boardData)) {
                JOptionPane.showMessageDialog(this, "Size phải là số chính phương (4, 9, 16...) và board_data phải đúng độ dài size*size.");
                return;
            }

            int levelId = SQL.getNextLevelId();
            int createdBy = SQL.getUserId(adminUsername);

            String sql = "INSERT INTO levels(level_id, size, board_data, title, difficulty, created_by) VALUES (?, ?, ?, ?, ?, ?)";
            try (Connection conn = SQL.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setInt(1, levelId);
                ps.setInt(2, size);
                ps.setString(3, boardData);
                ps.setString(4, title.isEmpty() ? null : title);
                ps.setString(5, difficulty.isEmpty() ? null : difficulty);
                if (createdBy == -1) {
                    ps.setNull(6, java.sql.Types.INTEGER);
                } else {
                    ps.setInt(6, createdBy);
                }

                ps.executeUpdate();
            }

            afterDataChanged("Đã thêm puzzle mới.");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Size không hợp lệ.");
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Không thể thêm puzzle.");
        }
    }

    private void updatePuzzle() {
        if (!SQL.isAdmin(adminUsername)) {
            JOptionPane.showMessageDialog(this, "Chỉ admin mới có quyền sửa puzzle.");
            return;
        }

        if (selectedRecord == null) {
            JOptionPane.showMessageDialog(this, "Hãy chọn một puzzle trước.");
            return;
        }

        try {
            int size = Integer.parseInt(sizeField.getText().trim());
            String title = titleField.getText().trim();
            String difficulty = difficultyField.getText().trim();
            String boardData = normalizeBoard(boardArea.getText());

            if (!validateBoardData(size, boardData)) {
                JOptionPane.showMessageDialog(this, "Size phải là số chính phương và board_data phải đúng.");
                return;
            }

            String sql = "UPDATE levels SET size = ?, board_data = ?, title = ?, difficulty = ? WHERE level_id = ?";
            try (Connection conn = SQL.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setInt(1, size);
                ps.setString(2, boardData);
                ps.setString(3, title.isEmpty() ? null : title);
                ps.setString(4, difficulty.isEmpty() ? null : difficulty);
                ps.setInt(5, selectedRecord.levelId);
                ps.executeUpdate();
            }

            afterDataChanged("Đã cập nhật puzzle #" + selectedRecord.levelId + ".");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Size không hợp lệ.");
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Không thể cập nhật puzzle.");
        }
    }

    private void deletePuzzle() {
        if (!SQL.isAdmin(adminUsername)) {
            JOptionPane.showMessageDialog(this, "Chỉ admin mới có quyền xóa puzzle.");
            return;
        }

        if (selectedRecord == null) {
            JOptionPane.showMessageDialog(this, "Hãy chọn một puzzle trước.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Xóa puzzle #" + selectedRecord.levelId + "?\nDữ liệu progress và time liên quan sẽ bị xóa theo FK CASCADE.",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        String sql = "DELETE FROM levels WHERE level_id = ?";
        try (Connection conn = SQL.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, selectedRecord.levelId);
            ps.executeUpdate();

            afterDataChanged("Đã xóa puzzle #" + selectedRecord.levelId + ".");
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Không thể xóa puzzle.");
        }
    }

    private void afterDataChanged(String message) {
        statusLabel.setText(message);
        loadPuzzles();

        if (onDataChanged != null) {
            onDataChanged.run();
        }
    }
}
