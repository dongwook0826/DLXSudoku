package test;

import sudoku.*;
import java.util.Scanner;

public class CompleteGenTestMain {
    public static void main(String[] args) throws SudokuException {

        int size, hsize, vsize;
        int maximaSearchCnt = 1500;
        int threadNum = 10;
        int searchCnt = maximaSearchCnt / threadNum;
        int iterationRate = 200;
        int targetDifficulty;
        int geometryIndicator;

        Scanner sc = new Scanner(System.in);

        System.out.print("Input the size of sudoku you want to generate(in one positive integer <= 25)\n>>> ");
        size = sc.nextInt();
        while(size <= 0 || size > 25){
            System.out.printf("size out of range(%d) : try again\n>>> ", size);
            size = sc.nextInt();
        }

        iterationRate = iterationRate * size*size / 81;

        System.out.print("Input the horizontal size of boxes(must divide previous size input)\n>>> ");
        hsize = sc.nextInt();
        while(hsize <= 0 || hsize >= size || size%hsize != 0){
            System.out.printf("invalid hsize(%d) : try again\n>>> ", hsize);
            hsize = sc.nextInt();
        }
        vsize = size/hsize;

        System.out.print("Input the difficulty target of the puzzle to be generated(0 to 9; 9 for unlimited diff)\n>>> ");
        targetDifficulty = sc.nextInt();
        while(targetDifficulty<0){
            System.out.printf("negative difficulty(%d) : try again\n>>> ", targetDifficulty);
            targetDifficulty = sc.nextInt();
        }

        System.out.println("Input the symmetry indicator for the puzzle");
        System.out.println("\t(1) Asymmetry");
        System.out.println("\t(2) Rotational symmetry (order 2) <-- default design");
        System.out.println("\t(3) Rotational symmetry (order 4)");
        System.out.println("\t(4) Vertical reflectional symmetry");
        System.out.println("\t(5) Vertical & horizontal reflectional symmetry");
        System.out.println("\t(6) Diagonal reflectional symmetry (1-way)");
        System.out.println("\t(7) Diagonal reflectional symmetry (2-way)");
        System.out.println("\t(8) Full 8-fold symmetry");
        System.out.print(">>> ");
        geometryIndicator = sc.nextInt();
        if(geometryIndicator <= 0 || geometryIndicator > 8) geometryIndicator = 2;

        System.out.print("Generating one nice and lovely piece of sudoku :   0%");

        long genStart = System.currentTimeMillis();

        SudokuGenThread.changeSettings(maximaSearchCnt, iterationRate, 9);
        SudokuGenThread.setGenData(size, hsize, targetDifficulty, geometryIndicator);
        SudokuGenThread[] genThreads = new SudokuGenThread[threadNum];
        Thread[] threads = new Thread[threadNum];
        for(int i=0; i<genThreads.length-1; i++){
            genThreads[i] = new SudokuGenThread(searchCnt);
            threads[i] = new Thread(genThreads[i]);
        }genThreads[genThreads.length-1] = new SudokuGenThread(maximaSearchCnt - searchCnt*(threadNum-1));
        threads[genThreads.length-1] = new Thread(genThreads[genThreads.length-1]);

        /*
        genThreads[0].setBaseGrid(System.nanoTime());
        // printBoard(genThreads[0].getBaseGrid(), size, hsize, vsize);
        for(int i=1; i<genThreads.length; i++){
            genThreads[i].setBaseGrid(genThreads[0].getBaseGrid());
        }*/
        for (SudokuGenThread genThread : genThreads) {
            genThread.setBaseGrid(System.nanoTime());
        }

        for(int i=0; i<genThreads.length; i++){
            threads[i].start();
        }try{
            for(Thread thr : threads){
                thr.join();
            }
        }catch(InterruptedException | NullPointerException e){
            e.printStackTrace();
        }

        double difficultyRate = SudokuGenThread.getBestDifficultyRate();
        int[][] puzzleGrid = SudokuGenThread.getBestPuzzle();

        long genEnd = System.currentTimeMillis();
        System.out.println();

        printBoard(puzzleGrid, size, hsize, vsize);
        System.out.printf("difficulty rate : %4.3f\n", difficultyRate);
        System.out.printf("generating time : %4.3f sec\n", (genEnd - genStart)/1000.0);
        System.out.println("Please enjoy!");

        sc.close();
    }

    public static void printBoard(int[][] board, int size, int hsize, int vsize){
        printBoard(board, size, hsize, vsize, size>9 ? 0 : 1);
    }

    public static void printBoard(int[][] board, int size, int hsize, int vsize, int charRange){
        char[] cellChars = new char[36];
        for(int i=0; i<10; i++){
            cellChars[i] = (char)(i+'0');
        }for(int i=0; i<26; i++){
            cellChars[i+10] = (char)(i+'A');
        }

        StringBuilder top = new StringBuilder("┏");
        StringBuilder mid = new StringBuilder("┣");
        StringBuilder sep = new StringBuilder("┠");
        StringBuilder bot = new StringBuilder("┗");
        for(int c=0; c<size; c++){
            top.append("━━━");
            mid.append("━━━");
            bot.append("━━━");
            sep.append("───");
            if(c == size-1){
                top.append("┓");
                mid.append("┫");
                bot.append("┛");
                sep.append("┨");
            }else if((c+1)%hsize == 0){
                top.append("┳");
                mid.append("╋");
                bot.append("┻");
                sep.append("╂");
            }else{
                top.append("┯");
                mid.append("┿");
                bot.append("┷");
                sep.append("┼");
            }
        }

        System.out.println(top);
        for(int r=0; r<size; r++){
            for(int c=0; c<size; c++){
                if(c%hsize == 0){
                    System.out.print("┃ ");
                }else{
                    System.out.print("│ ");
                }
                if(board[r][c] < 0){
                    System.out.print("  ");
                }else{
                    System.out.print(cellChars[board[r][c]+charRange]+" ");
                }
            }System.out.println("┃");
            if(r == size-1){
                System.out.println(bot);
            }else if((r+1)%vsize == 0){
                System.out.println(mid);
            }else{
                System.out.println(sep);
            }
        }
    }
}
