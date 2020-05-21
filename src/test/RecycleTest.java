package test;

import sudoku.*;

public class RecycleTest {
    public static void main(String[] args) throws SudokuException {
        SudokuTemplate template = new SudokuTemplate();

        int[][] puzzle1 = {
                {8,0,0,0,0,0,0,0,0},
                {0,0,3,6,0,0,0,0,0},
                {0,7,0,0,9,0,2,0,0},
                {0,5,0,0,0,7,0,0,0},
                {0,0,0,0,4,5,7,0,0},
                {0,0,0,1,0,0,0,3,0},
                {0,0,1,0,0,0,0,6,8},
                {0,0,8,5,0,0,0,1,0},
                {0,9,0,0,0,0,4,0,0}
        };
        int[][] puzzle2 = {
                {0,0,0,0,0,0,0,0,0},
                {0,0,0,0,0,3,0,8,5},
                {0,0,1,0,2,0,0,0,0},
                {0,0,0,5,0,7,0,0,0},
                {0,0,4,0,0,0,1,0,0},
                {0,9,0,0,0,0,0,0,0},
                {5,0,0,0,0,0,0,7,3},
                {0,0,2,0,1,0,0,0,0},
                {0,0,0,0,4,0,0,0,9}
        };
        for(int r=0; r<9; r++){
            for(int c=0; c<9; c++){
                puzzle1[r][c]-=1;
                puzzle2[r][c]-=1;
            }
        }

        template.testPrint();
        System.out.println("----------------------------------------------------");
        int[] puzzle1Info = template.getPuzzleInfo(puzzle1, true);
        // template.testPrint();
        System.out.println("----------------------------------------------------");
        System.out.println("puzzle 1 diff : "+puzzle1Info[1]);
        System.out.println("----------------------------------------------------");
        int[] puzzle2Info = template.getPuzzleInfo(puzzle2, true);
        // template.testPrint();
        System.out.println("----------------------------------------------------");
        System.out.println("puzzle 2 diff : "+puzzle2Info[1]);
    }
}
