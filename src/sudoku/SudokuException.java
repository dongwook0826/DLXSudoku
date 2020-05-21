package sudoku;

public class SudokuException extends Exception {
    public SudokuException(int size, boolean isSize){
        super(isSize
                ? ("Sudoku size out of bound : "+size)
                : ("Box size out of bound : "+size)
        );
    }

    public SudokuException(int size, int hsize){
        super(String.format("Box size not dividing sudoku size : %d %% %d = %d",
                size, hsize, size%hsize));
    }
}
