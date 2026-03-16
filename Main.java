public class Main {

    public static void main(String[] args) {

        SQL.getConnection();

        Board.ReadData();      
        Board.setPuzzle(0);   

        Board.printBoard();   

        new SudokuUI();       
    }
}