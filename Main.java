public class Main {

    public static void main(String[] args) {
        // Board.printPuzzle(args);

        // Board.setCell(0, 0, 5);
        // System.out.println("|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||");
        // Board.printBoard();

        // Board.clearCell(0, 0);
        // System.out.println("|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||");
        // Board.printBoard();

        // Puzzle p = new Puzzle();
        // p.createNewPuzzles();

        // if (Validator.isValid(Board.board)) {
        //     System.out.println("Valid");
        // } else {
        //     System.out.println("Invalid");
        // }
        
        Board.board = Puzzle.loadPuzzle("puzzle.txt",2);
        new UI();
    }
}