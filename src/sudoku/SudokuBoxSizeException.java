package sudoku;

public class SudokuBoxSizeException extends Exception {
    public SudokuBoxSizeException(int hsize){
        super("Box size out of bound : "+hsize);
    }
}
