package sudoku;

public class InvalidSudokuSizeException extends Exception {
    public InvalidSudokuSizeException(int size){
        super("Sudoku size out of bound : "+size);
    }
}
