package com.test;

import dlx.*;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

// test for general case : size 9-by-9 standard sudoku gen

public class GenTestMain {
    public static void main(String[] args){

        Scanner sc = new Scanner(System.in);

        System.out.print("Input the size of sudoku you want to generate(in one positive integer, up to 16)\n>>> ");
        int SIZE = sc.nextInt();
        while(SIZE<=0 || SIZE>25){
            System.out.printf("size out of range(%d) : try again\n>>> ", SIZE);
            SIZE = sc.nextInt();
        }

        System.out.print("Input the horizontal size of boxes(must divide previous size input)\n>>> ");
        int HSIZE = sc.nextInt(); // must divide SIZE
        while(HSIZE<=0 || HSIZE>=SIZE || SIZE%HSIZE != 0){
            System.out.printf("invalid hsize(%d) : try again\n>>> ", HSIZE);
            HSIZE = sc.nextInt();
        }

        int VSIZE = SIZE/HSIZE; // = 3

        long genStart = System.currentTimeMillis();

        String name = "Test problem : Sudoku Board Gen";
        String[] colNames = new String[SIZE*SIZE*4];

        for(int r=0; r<SIZE; r++){
            for(int c=0; c<SIZE; c++){
                colNames[SIZE*r+c] = String.format("(%d,%d) filled", r, c);
            }
        }
        for(int n=1; n<=SIZE; n++){
            for(int a=0; a<SIZE; a++){
                colNames[SIZE*SIZE + SIZE*(n-1) + a] = String.format("%d in row %d", n, a);
                colNames[SIZE*SIZE*2 + SIZE*(n-1) + a] = String.format("%d in col %d", n, a);
                colNames[SIZE*SIZE*3 + SIZE*(n-1) + a] = String.format("%d in box %d", n, a);
            }
        }

        boolean[] whichPrimary = new boolean[colNames.length];
        Arrays.fill(whichPrimary, true);

        ToricLinkedList sudoku = new ToricLinkedList(name, colNames, whichPrimary);

        for(int n=0, indic=0; n<SIZE; n++){
            for(int r=0; r<SIZE; r++){
                for(int c=0; c<SIZE; c++){
                    boolean[] constraint = new boolean[colNames.length];
                    constraint[SIZE*r + c] = true;
                    constraint[SIZE*SIZE + SIZE*n + r] = true;
                    constraint[SIZE*SIZE*2 + SIZE*n + c] = true;
                    constraint[SIZE*SIZE*3 + SIZE*n + VSIZE*(r/VSIZE) + c/HSIZE] = true;
                    // System.out.println(indic);
                    sudoku.addRow(indic++, constraint);
                }
            }
        } // sudoku initialization complete

        // sudoku.testPrint();

        long solveStart = System.currentTimeMillis();
        Random rand = new Random(solveStart);

        sudoku.randomSearchSolution(rand);
        QNode[] board = sudoku.getSolution();
        /*
        sudoku.printSolution();
        System.out.println();
         */
        long solveEnd = System.currentTimeMillis();
        printBoard(board, SIZE, HSIZE, VSIZE);

        System.out.printf("Generating : %4.3f sec\n", (solveStart - genStart)/1000.0);
        System.out.printf("Solving : %4.3f sec\n", (solveEnd - solveStart)/1000.0);
        // sc.close();
    }

    public static void printBoard(QNode[] board, int size, int hsize, int vsize){
        printBoard(board, size, hsize, vsize, size>9 ? 0 : 1);
    }

    public static void printBoard(QNode[] board, int size, int hsize, int vsize, int charRange){

        char[] cellChars = new char[36];
        for(int i=0; i<10; i++){
            cellChars[i] = (char)(i+'0');
        }for(int i=0; i<26; i++){
            cellChars[i+10] = (char)(i+'A');
        }

        char[][] boardArray = new char[size][size];
        for(QNode cell : board){
            int ind = cell.indicator;
            boardArray[(ind%(size*size))/size][ind%size] = cellChars[ind/(size*size)+charRange];
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
                if(boardArray[r][c] == 0){
                    System.out.print("  ");
                }else{
                    System.out.print(boardArray[r][c]+" ");
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
