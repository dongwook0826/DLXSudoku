package sudoku;

public class SizeNotDivisibleException extends Exception {
    public SizeNotDivisibleException(int size, int hsize){
        super(String.format("Box size not dividing sudoku size : %d %% %d = %d",
                            size, hsize, size%hsize));
    }
}
