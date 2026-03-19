public class Main {

    public static void main(String[] args) {
    SQL.getConnection();
    Board.ReadData();
    
    SudokuUI ui = new SudokuUI();
    ui.openLogin(); // Ép người dùng login ngay khi mở app
}
}