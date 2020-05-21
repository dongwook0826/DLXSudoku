package com;

import sudoku.*;

import java.util.Scanner;
import java.io.*;
import java.util.concurrent.TimeUnit;

public class SudokuMaker {

    static final char[] CELL_CHARS = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
            'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
            'U', 'V', 'W', 'X', 'Y', 'Z'
    };

    public static void main(String[] args)
            throws SudokuException, IOException, InterruptedException {

        String choiceStr;
        int choice;

        // changeable each time of generating
        int size, hsize, vsize;
        int targetDifficulty;
        int geometryIndicator;

        // changeable only in setting menu
        int maximaSearchCnt = 1500;
        int threadNum = 10;
        int searchCnt = maximaSearchCnt / threadNum; // = 150 in default
        int iterationRate = 200;
        int difficultyLimit = 9;

        final String[] SYMMETRY_LIST = {
                "Go back to size setting",
                "Asymmetry",
                "Rotational symmetry (order 2) <-- default design",
                "Rotational symmetry (order 4)",
                "Vertical reflectional symmetry",
                "Vertical & horizontal reflectional symmetry",
                "Diagonal reflectional symmetry (1-way)",
                "Diagonal reflectional symmetry (2-way)",
                "Full 8-fold symmetry"
        };

        final char[] FORBIDDEN_CHARS = {'\\', '/', ':', '*', '?', '"', '<', '>', '|'};
        char emptyCellChar = '_';

        Scanner sc = new Scanner(System.in);
        BufferedWriter bw = null;

        String fileName = "";

        System.out.println("******************************************");
        System.out.println("*                                        *");
        System.out.println("*       SUDOKU GEN CHALLENGE ver.2       *"); // SUDOKU GEN CHALLENGE ver.2
        System.out.println("*                                        *");
        System.out.println("*            made by. TenDong            *"); // made by. TenDong
        System.out.println("*                                        *");
        System.out.println("******************************************");

        gameLoop : while(true){
            System.out.println("\n ---------- HOME ----------");
            System.out.println("\nWhich task do you want to do?");
            System.out.println("\t(1) Generate a new charming piece of Sudoku");
            System.out.println("\t(2) Analyze and solve a Sudoku you already have");
            System.out.println("\t(3) Change settings");
            System.out.println("\t(0) Exit this program");

            choice = -1;
            while(choice<0){
                System.out.print(">>>>>>>> ");
                choiceStr = sc.nextLine();
                if(choiceStr.length() == 0){
                    System.out.println("Try again : no input");
                    continue;
                }
                choice = (int)choiceStr.charAt(0) - '0';
                switch(choice){
                    case 0 :
                        break gameLoop;
                    case 1 : case 2 : case 3 :
                        break;
                    default :
                        System.out.println("Try again : invalid choice");
                        choice = -1;
                }
            }
            System.out.println();
            switch(choice){
                case 1 :
                    System.out.println("\n ---------- MODE : GENERATOR ----------\n");
                    System.out.printf(" maxima searching count : %4d\n", maximaSearchCnt);
                    System.out.printf(" search thread number   : %4d\n", threadNum);
                    System.out.printf(" iteration rate         : %4d\n", iterationRate);
                    System.out.printf(" difficulty limit       : %4d\n", difficultyLimit);
                    while(true){
                        System.out.println("\nWhat size of Sudoku do you want to generate? (0 for going back)");
                        System.out.println("(possible up to size 25, but you'd better not try it over 16 : CPU overload possible)");
                        inputLoop :
                        while(true){
                            System.out.print(">>>>>>>> ");
                            choiceStr = sc.nextLine();
                            if(choiceStr.length() == 0){
                                System.out.println("Try again : no input");
                                continue;
                            }
                            for(int i=0; i<choiceStr.length(); i++){
                                int n = (int) choiceStr.charAt(i) - '0';
                                if(n<0 || n>9){
                                    System.out.println("Try again : invalid input");
                                    continue inputLoop;
                                }
                            }
                            choice = Integer.parseInt(choiceStr);
                            if(choice == 0){
                                continue gameLoop;
                            }else if(choice > 25){
                                System.out.println("Try again : input out of range");
                            }else break;
                        }size = choice;
                        int adjustedIterationRate = iterationRate * size/9;

                        System.out.println("\nWhat size of the subarea would it be?");
                        System.out.println("(in horizontal way; must divide the size of Sudoku you input)");
                        inputLoop :
                        while(true){
                            System.out.print(">>>>>>>> ");
                            choiceStr = sc.nextLine();
                            if(choiceStr.length() == 0){
                                System.out.println("Try again : no input");
                                continue;
                            }
                            for(int i=0; i<choiceStr.length(); i++){
                                int n = (int) choiceStr.charAt(i) - '0';
                                if(n<0 || n>9){
                                    System.out.println("Try again : invalid input");
                                    continue inputLoop;
                                }
                            }
                            choice = Integer.parseInt(choiceStr);
                            if(choice <= 0 || choice >= size || size % choice != 0){
                                System.out.println("Try again : invalid size value");
                            }else break;
                        }hsize = choice;
                        vsize = size / hsize;

                        System.out.println("\nHow hard may the puzzle be at most?");
                        System.out.printf("(0 to %d for easiest to hardest; larger value for difficulty-limitless search)\n", difficultyLimit);
                        inputLoop :
                        while(true){
                            System.out.print(">>>>>>>> ");
                            choiceStr = sc.nextLine();
                            if(choiceStr.length() == 0){
                                System.out.println("Try again : no input");
                                continue;
                            }
                            for(int i=0; i<choiceStr.length(); i++){
                                int n = (int) choiceStr.charAt(i) - '0';
                                if(n<0 || n>9){
                                    System.out.println("Try again : invalid input");
                                    continue inputLoop;
                                }
                            }
                            choice = Integer.parseInt(choiceStr);
                            break;
                        }targetDifficulty = choice;

                        System.out.println("\nIn which geometry do you want the puzzle to be?");
                        for(int i=1; i<=8; i++){
                            System.out.printf("\t(%d) %s\n", i, SYMMETRY_LIST[i]);
                        }System.out.println("\t(0) Go back to size setting");
                        while(true){
                            System.out.print(">>>>>>>> ");
                            choiceStr = sc.nextLine();
                            if(choiceStr.length() == 0){
                                System.out.println("Try again : no input");
                                continue;
                            }
                            choice = (int) choiceStr.charAt(0) - '0';
                            if(choice > 8 || choice < 0){
                                System.out.println("Try again : invalid input");
                            }else break;
                        }geometryIndicator = choice;

                        // all values assigned
                        System.out.println("\nInput values : ");
                        System.out.printf("whole size       : %d\n", size);
                        System.out.printf("subarea size      : %d by %d\n", hsize, vsize);
                        System.out.printf("target difficulty : %d\n", targetDifficulty);
                        System.out.printf("puzzle symmetry   : (%d) %s\n", geometryIndicator, SYMMETRY_LIST[geometryIndicator]);
                        if(size>=16){
                            System.out.println("!!!!!! CAUTION !!!!!!");
                            System.out.println(" Sudoku of size over 16 may take over 2 minutes for generating.");
                        }
                        System.out.println("Continue generating puzzle with these value settings? (Y/N)");
                        char yesno;
                        while(true){
                            System.out.print(">>>>>>>> ");
                            choiceStr = sc.nextLine();
                            if(choiceStr.length() == 0){
                                System.out.println("Try again : no input");
                                continue;
                            }
                            yesno = choiceStr.charAt(0);
                            if(yesno == 'n' || yesno == 'N' || yesno == 'y' || yesno == 'Y'){
                                break;
                            }else System.out.println("Try again : invalid choice");
                        }

                        if(yesno == 'n' || yesno == 'N'){
                            System.out.println("Quit generating; go back to size choice");
                            continue;
                        }

                        System.out.print("\nGenerating one nice and lovely piece of Sudoku :   0%");

                        long genStart = System.currentTimeMillis();

                        SudokuGenThread.changeSettings(maximaSearchCnt, adjustedIterationRate, difficultyLimit);
                        SudokuGenThread.setGenData(size, hsize, targetDifficulty, geometryIndicator);
                        SudokuGenThread[] genThreads = new SudokuGenThread[threadNum];
                        Thread[] threads = new Thread[threadNum];
                        for(int i=0; i<genThreads.length-1; i++){
                            genThreads[i] = new SudokuGenThread(searchCnt);
                            threads[i] = new Thread(genThreads[i]);
                        }genThreads[genThreads.length-1] = new SudokuGenThread(maximaSearchCnt - searchCnt*(threadNum-1));
                        threads[genThreads.length-1] = new Thread(genThreads[genThreads.length-1]);

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
                        System.out.printf("Difficulty rate : %4.3f\n", difficultyRate);
                        System.out.printf("Generating time : %4.3f sec\n", (genEnd - genStart)/1000.0);

                        System.out.println("\nSave it as txt file? (Y/N)");
                        while(true){
                            System.out.print(">>>>>>>> ");
                            choiceStr = sc.nextLine();
                            if(choiceStr.length() == 0){
                                System.out.println("Try again : no input");
                                continue;
                            }
                            yesno = choiceStr.charAt(0);
                            if(yesno != 'n' && yesno != 'N' && yesno != 'y' && yesno != 'Y')
                                System.out.println("Try again : invalid choice");
                            else break;
                        }

                        if (yesno == 'y' || yesno == 'Y') {
                            System.out.println("\nChoose the output task you want to do");
                            System.out.println("\t(1) Save the puzzle in a new txt file");
                            if(fileName.length() != 0){
                                System.out.printf("\t(2) Save it in the txt file just generated : %s\n", fileName + ".txt");
                            }
                            System.out.println("\t(0) Quit the saving process and go back");
                            while(true){
                                System.out.print(">>>>>>>> ");
                                choiceStr = sc.nextLine();
                                if(choiceStr.length() == 0){
                                    System.out.println("Try again : no input");
                                    continue;
                                }
                                choice = (int) choiceStr.charAt(0) - '0';
                                if(choice >= 0 && (choice <= 1 || (fileName.length() > 0 && choice <= 2))){
                                    break;
                                }else{
                                    System.out.println("Try again : invalid input");
                                }
                            }
                            switch(choice){
                                case 1 :
                                    System.out.println("\nInput the name of text file to save your Sudoku (without extension)");
                                    while (true) {
                                        System.out.print(">>>>>>>> ");
                                        fileName = sc.nextLine();
                                        if (fileName.length() == 0) {
                                            System.out.println("Try again : no input");
                                            continue;
                                        }
                                        boolean fileNameValid = true;
                                        validCheck:
                                        for (int i = 0; i < fileName.length(); i++) {
                                            for (char forbc : FORBIDDEN_CHARS) {
                                                if (fileName.charAt(i) == forbc) {
                                                    fileNameValid = false;
                                                    break validCheck;
                                                }
                                            }
                                        }
                                        if (!fileNameValid) {
                                            System.out.println("Forbidden character included : try again!");
                                            continue;
                                        }
                                        break;
                                    }
                                case 2 :
                                    bw = new BufferedWriter(new FileWriter(fileName + ".txt", true));
                                    bw.write(toString(puzzleGrid, emptyCellChar));
                                    bw.write(String.format(" (%2d, %d, %d, %4.3f)", size, hsize, vsize, difficultyRate));
                                    bw.newLine();
                                    bw.flush();
                                    System.out.printf("\nSave success : %s\n", fileName+".txt");
                            }
                        }

                        System.out.println("\nExpose solution on the shell? (Y/N)");
                        while(true){
                            System.out.print(">>>>>>>> ");
                            choiceStr = sc.nextLine();
                            if(choiceStr.length() == 0){
                                System.out.println("Try again : no input");
                                continue;
                            }
                            yesno = choiceStr.charAt(0);
                            if(yesno != 'n' && yesno != 'N' && yesno != 'y' && yesno != 'Y')
                                System.out.println("Try again : invalid choice");
                            else break;
                        }
                        if(yesno == 'y' || yesno == 'Y'){
                            System.out.println("Solution : ");
                            printBoard(SudokuGenThread.getSolution(), size, hsize, vsize);
                        }
                        TimeUnit.MILLISECONDS.sleep(500);
                    }
                case 2 :
                    System.out.println("\n ---------- MODE : ANALYZER ----------");
                    while(true) {
                        System.out.println("\nWhat size of Sudoku do you have? (possible up to size 25; 0 for going back)");
                        inputLoop:
                        while (true) {
                            System.out.print(">>>>>>>> ");
                            choiceStr = sc.nextLine();
                            if (choiceStr.length() == 0) {
                                System.out.println("Try again : no input");
                                continue;
                            }
                            for (int i = 0; i < choiceStr.length(); i++) {
                                int n = (int) choiceStr.charAt(i) - '0';
                                if (n < 0 || n > 9) {
                                    System.out.println("Try again : invalid input");
                                    continue inputLoop;
                                }
                            }
                            choice = Integer.parseInt(choiceStr);
                            if (choice == 0) {
                                continue gameLoop;
                            } else if (choice > 25) {
                                System.out.println("Try again : input out of range");
                            } else break;
                        }
                        size = choice;

                        System.out.println("\nWhat size of the subarea would it be?");
                        System.out.println("(in horizontal way; must divide the size of Sudoku you input)");
                        inputLoop:
                        while (true) {
                            System.out.print(">>>>>>>> ");
                            choiceStr = sc.nextLine();
                            if (choiceStr.length() == 0) {
                                System.out.println("Try again : no input");
                                continue;
                            }
                            for (int i = 0; i < choiceStr.length(); i++) {
                                int n = (int) choiceStr.charAt(i) - '0';
                                if (n < 0 || n > 9) {
                                    System.out.println("Try again : invalid input");
                                    continue inputLoop;
                                }
                            }
                            choice = Integer.parseInt(choiceStr);
                            if (choice <= 0 || choice >= size || size % choice != 0) {
                                System.out.println("Try again : invalid size value");
                            } else break;
                        }
                        hsize = choice;
                        vsize = size / hsize;

                        System.out.println("\nInput the Sudoku puzzle to be parsed, in one string!");
                        if (size <= 9) {
                            System.out.println("[for size n <=  9, use 1~n for clues and any other character for empty cells]");
                        } else {
                            System.out.println("[for size n >= 10, use 0~n (including alphabets) for clues and any other character for empty cells]");
                        }
                        String rawSudoku;
                        while (true) {
                            System.out.print(">>>>>>>> ");
                            rawSudoku = sc.nextLine();
                            if (rawSudoku.length() != size * size) {
                                System.out.println("Try again : invalid puzzle length");
                            } else break;
                        }

                        int[][] puzzle = new int[size][size];
                        for (int r = 0, i = 0; r < size; r++) {
                            for (int c = 0; c < size; c++) {
                                char cellChar = rawSudoku.charAt(i++);
                                puzzle[r][c] = size <= 9
                                        ? (cellChar > '0' && cellChar <= '9' ? (int) cellChar - '1' : -1)
                                        : Character.isDigit(cellChar) ? (int) cellChar - '0'
                                        : Character.isUpperCase(cellChar) ? (int) cellChar - 'A' + 10 : -1;
                            }
                        }

                        System.out.println("\nParsing complete :");
                        printBoard(puzzle, size, hsize, vsize);
                        System.out.println("\nExpose solution on the shell? (Y/N)");
                        char yesno;
                        while (true) {
                            System.out.print(">>>>>>>> ");
                            choiceStr = sc.nextLine();
                            if (choiceStr.length() == 0) {
                                System.out.println("Try again : no input");
                                continue;
                            }
                            yesno = choiceStr.charAt(0);
                            if (yesno != 'n' && yesno != 'N' && yesno != 'y' && yesno != 'Y')
                                System.out.println("Try again : invalid choice");
                            else break;
                        }
                        if (yesno == 'y' || yesno == 'Y') {
                            long solveStart = System.currentTimeMillis();

                            SudokuTemplate sdkTempl = new SudokuTemplate(size, hsize);
                            int[] sdkInfo = sdkTempl.getPuzzleInfo(puzzle, false);

                            long solveEnd = System.currentTimeMillis();

                            System.out.println("\nSolution :");
                            printBoard(sdkTempl.solutionInIntArray(), size, hsize, vsize);
                            switch (sdkInfo[0]) {
                                case 0:
                                    System.out.println("No solution");
                                    break;
                                case 2:
                                    System.out.println("Multiple solutions");
                                    break;
                                case 1:
                                    System.out.println("Unique solution");
                                    System.out.printf("Difficulty rate : %4.3f\n", sdkInfo[1] + sdkInfo[2] / ((double) size * size));
                                    System.out.printf("Solving time    : %4.3f\n", (solveEnd - solveStart) / 1000.0);
                            }
                        }
                        TimeUnit.MILLISECONDS.sleep(500);
                    }
                case 3 :
                    System.out.println("\n ---------- MODE : SETTING ----------");
                    while(true){
                        System.out.println("\nChoose the factor you want to make a change in");
                        System.out.printf("\t(1) maxima searching count : %4d\n", maximaSearchCnt);
                        System.out.printf("\t(2) search thread number   : %4d\n", threadNum);
                        System.out.printf("\t(3) iteration rate         : %4d\n", iterationRate);
                        System.out.printf("\t(4) difficulty limit       : %4d\n", difficultyLimit);
                        System.out.printf("\t(5) empty cell character   : %c\n", emptyCellChar);
                        System.out.println("\t(0) go back to home menu");
                        while(true){
                            System.out.print(">>>>>>>> ");
                            choiceStr = sc.nextLine();
                            if(choiceStr.length() == 0){
                                System.out.println("Try again : no input");
                                continue;
                            }
                            choice = (int) choiceStr.charAt(0) - '0';
                            if(choice > 5 || choice < 0){
                                System.out.println("Try again : invalid input");
                            }else break;
                        }
                        if(choice == 0) continue gameLoop;

                        switch(choice){
                            case 1 :
                                System.out.println("\nInput the new maxima searching count (1000~1500 recommended; 1~2000 possible)");
                                inputLoop:
                                while (true) {
                                    System.out.print(">>>>>>>> ");
                                    choiceStr = sc.nextLine();
                                    if (choiceStr.length() == 0) {
                                        System.out.println("Try again : no input");
                                        continue;
                                    }
                                    for (int i = 0; i < choiceStr.length(); i++) {
                                        if (!Character.isDigit(choiceStr.charAt(i))) {
                                            System.out.println("Try again : invalid input");
                                            continue inputLoop;
                                        }
                                    }
                                    choice = Integer.parseInt(choiceStr);
                                    if (choice <= 0 || choice > 2000) {
                                        System.out.println("Try again : input out of range");
                                    } else break;
                                }maximaSearchCnt = choice;
                                searchCnt = maximaSearchCnt / threadNum;
                                break;
                            case 2 :
                                System.out.println("\nInput the new number of searching threads (4~50 recommended; 1~[maxima searching cnt] possible)");
                                inputLoop:
                                while (true) {
                                    System.out.print(">>>>>>>> ");
                                    choiceStr = sc.nextLine();
                                    if (choiceStr.length() == 0) {
                                        System.out.println("Try again : no input");
                                        continue;
                                    }
                                    for (int i = 0; i < choiceStr.length(); i++) {
                                        if (!Character.isDigit(choiceStr.charAt(i))) {
                                            System.out.println("Try again : invalid input");
                                            continue inputLoop;
                                        }
                                    }
                                    choice = Integer.parseInt(choiceStr);
                                    if (choice > maximaSearchCnt) {
                                        System.out.println("Try again : thread number larger than maxima search count");
                                    } else if (choice <= 0){
                                        System.out.println("Try again : input out of range");
                                    } else break;
                                }threadNum = choice;
                                searchCnt = maximaSearchCnt / threadNum;
                                break;
                            case 3 :
                                System.out.println("\nInput the new iteration rate (150~200 recommended; 100~300 possible)");
                                System.out.println("[[[ Notice ]]]");
                                System.out.println("  This iteration rate is for generating standard 9*9 Sudoku;");
                                System.out.println("  the iteration number will be automatically adjusted");
                                System.out.println("  proportional to the size of the puzzle you're making");
                                inputLoop:
                                while (true) {
                                    System.out.print(">>>>>>>> ");
                                    choiceStr = sc.nextLine();
                                    if (choiceStr.length() == 0) {
                                        System.out.println("Try again : no input");
                                        continue;
                                    }
                                    for (int i = 0; i < choiceStr.length(); i++) {
                                        if (!Character.isDigit(choiceStr.charAt(i))) {
                                            System.out.println("Try again : invalid input");
                                            continue inputLoop;
                                        }
                                    }
                                    choice = Integer.parseInt(choiceStr);
                                    if (choice < 100 || choice > 300) {
                                        System.out.println("Try again : input out of range");
                                    } else break;
                                }iterationRate = choice;
                                break;
                            case 4 :
                                System.out.println("\nInput the new difficulty limit (9~100 recommended; 0~2147483647 possible)");
                                inputLoop:
                                while (true) {
                                    System.out.print(">>>>>>>> ");
                                    choiceStr = sc.nextLine();
                                    if (choiceStr.length() == 0) {
                                        System.out.println("Try again : no input");
                                        continue;
                                    }
                                    for (int i = 0; i < choiceStr.length(); i++) {
                                        if (!Character.isDigit(choiceStr.charAt(i))) {
                                            System.out.println("Try again : invalid input");
                                            continue inputLoop;
                                        }
                                    }
                                    choice = Integer.parseInt(choiceStr);
                                    if (choice < 0){
                                        System.out.println("Try again : input out of range");
                                    } else break;
                                }difficultyLimit = choice;
                                break;
                            case 5 :
                                System.out.println("\nInput the new empty-cell character (any character but for digit & alphabet is possible; *, _, $, /, etc.)");
                                while (true) {
                                    System.out.print(">>>>>>>> ");
                                    choiceStr = sc.nextLine();
                                    if (choiceStr.length() == 0) {
                                        System.out.println("Try again : no input");
                                    }else if(choiceStr.length() > 1){
                                        System.out.println("Try again : multiple characters input");
                                    }else if (Character.isLetterOrDigit(choiceStr.charAt(0))) {
                                        System.out.println("Try again : invalid input for empty-cell character");
                                    }else break;
                                }emptyCellChar = choiceStr.charAt(0);
                        }
                    }
            }
        }

        sc.close();
        if(bw != null){
            bw.close();
        }
        System.out.println("Seems you need to go bask to work, what a pity...");
        System.out.println("Anyway, thanks for playing!");
        System.out.println("(upcoming with more general Sudoku maker...)");
    }

    public static void printBoard(int[][] board, int size, int hsize, int vsize){
        printBoard(board, size, hsize, vsize, size>9 ? 0 : 1);
    }

    public static void printBoard(int[][] board, int size, int hsize, int vsize, int charRange){
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
                    System.out.print(CELL_CHARS[board[r][c]+charRange]+" ");
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

    public static String toString(int[][] board, char emptyCellChar){
        int charRange = board.length>9 ? 0 : 1;
        StringBuilder sb = new StringBuilder();
        for(int[] row : board){
            for(int c : row){
                if(c<0) sb.append(emptyCellChar);
                else sb.append(CELL_CHARS[c+charRange]);
            }
        }
        return sb.toString();
    }
}
