package sudoku;

import dlx.*;
import java.util.Arrays;

public class SudokuTemplate {

    protected ToricLinkedList dlxSudoku;
    protected int size;
    protected int sqSize;
    protected int hsize;
    protected int vsize;
    protected int[][] solution;

    /*
    validity checker will be implemented in main class
     */

    public SudokuTemplate()
            throws SudokuException {
        this(9, 3);
    }

    public SudokuTemplate(int size, int hsize)
            throws SudokuException {

        if(size <= 0 || size > 25){
            throw new SudokuException(size, true);
        }else if(hsize <= 0 || hsize >= size){
            throw new SudokuException(hsize, false);
        }else if(size % hsize != 0){
            throw new SudokuException(size, hsize);
        }

        this.size = size;
        this.hsize = hsize;
        this.sqSize = size*size;

        vsize = size / hsize;
        solution = new int[size][size];

        initializeDLX();
    }

    public void initializeDLX(){
        String name = String.format("Sudoku template : size %d, box %d:%d", size, hsize, vsize);
        String[] colNames = new String[size*size*4];

        for(int r=0; r<size; r++){
            for(int c=0; c<size; c++){
                colNames[size*r+c] = String.format("(%d,%d) filled", r, c);
            }
        }
        for(int n=0; n<size; n++){
            for(int a=0; a<size; a++){
                colNames[sqSize + size*n + a] = String.format("%d in row %d", n, a);
                colNames[sqSize*2 + size*n + a] = String.format("%d in col %d", n, a);
                colNames[sqSize*3 + size*n + a] = String.format("%d in box %d", n, a);
            }
        }

        boolean[] whichPrimary = new boolean[colNames.length];
        Arrays.fill(whichPrimary, true);

        dlxSudoku = new ToricLinkedList(name, colNames, whichPrimary);

        for(int n=0, indic=0; n<size; n++){
            for(int r=0; r<size; r++){
                for(int c=0; c<size; c++){
                    boolean[] constraint = new boolean[colNames.length];
                    constraint[size*r + c] = true;
                    constraint[sqSize + size*n + r] = true;
                    constraint[sqSize*2 + size*n + c] = true;
                    constraint[sqSize*3 + size*n + vsize*(r/vsize) + c/hsize] = true;
                    // System.out.println(indic);
                    dlxSudoku.addRow(indic++, constraint);
                }
            }
        } // dlxSudoku initialization complete
    }

    public int[] getPuzzleInfo(int[][] puzzle, boolean recycled){
        // each entry >= 0 for clue given,
        //            == -1 for empty cell
        // SudokuGenThread 안에서만 쓸거면 validity check는 성능상 무쓸모
        // 디버깅 완료시 아래 블록과 "Invalid puzzle" 체킹블록 주석 처리
        /* // for debugging
        if(puzzle.length != size){
            System.out.printf("Puzzle size not matching : expected %d, passed %d\n", size, puzzle.length);
            return new int[3];
        }*/
        HeaderQNode cellRepNd = dlxSudoku.root;
        QNode[] clues = new QNode[sqSize];
        int cluesNum = 0;
        // cover clues
        for(int r=0; r<size; r++){
            for(int c=0; c<size; c++){
                cellRepNd = cellRepNd.rightQNode;
                if(puzzle[r][c] < 0) continue;
                dlxSudoku.cover(cellRepNd);
                QNode numNd = cellRepNd.lowerQNode;
                while(numNd.indicator/sqSize != puzzle[r][c]){
                    numNd = numNd.lowerQNode;
                    /* // for debugging
                    if(numNd == cellRepNd){
                        System.out.printf("Invalid puzzle : (%d,%d) %d\n", r,c,puzzle[r][c]);
                        return new int[3];
                    }*/
                }clues[cluesNum++] = numNd;
                // System.out.println(numNd.indicator);
                QNode qnd = numNd.rightQNode;
                while(qnd != numNd){
                    dlxSudoku.cover(qnd.headerQNode);
                    qnd = qnd.rightQNode;
                }
            }
        }
        int emptyCellsNum = sqSize-cluesNum;
        dlxSudoku.searchSolution();
        if(recycled){
            while (cluesNum > 0) {
                QNode qnd = clues[--cluesNum].leftQNode;
                while (qnd != clues[cluesNum]) {
                    dlxSudoku.uncover(qnd.headerQNode);
                    qnd = qnd.leftQNode;
                }
                dlxSudoku.uncover(clues[cluesNum].headerQNode);
            }
        }else{
            solution = puzzle;
        }
        return new int[]{dlxSudoku.getSolutionCnt(), dlxSudoku.getBranchDiff(), emptyCellsNum};
    }

    public int[][] solutionInIntArray(){
        int sqSize = size*size;
        for(QNode cell : dlxSudoku.getSolution()){
            int ind = cell.indicator;
            solution[(ind%sqSize)/size][ind%size] = ind/(size*size);
        }
        return solution;
    }

    public void testPrint(){
        dlxSudoku.testPrint();
    }
}

