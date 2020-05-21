package sudoku;

import java.util.Random;

public class SudokuGenThread implements Runnable {

    // one thread running, one ToricLinkedList used
    /*
    private static ToricLinkedList dlxSudoku;
     */
    private final SudokuTemplate SDK_TEMPLATE;
    private static int size;
    private static boolean odd;
    private static int halfSize;
    private static int sqSize;
    private static int hsize;
    // private static int vsize;

    private int[][] baseGrid; // solution
    private static int[][] targetBaseGrid;
    private static int[][] targetGrid; // puzzle found closest to target

    private final int searchCnt; // thread-wise search count; init by constructor
    private static int maximaSearchCnt;
    private static int iterationRate;
    private static int difficultyLimit;

    private static int targetDifficulty;
    private static int geometryIndicator;
    private static int[][] usingAreaInd;
    /*
    geometryIndicator)
    1 : asym.
    2 : order-2 rot. sym. <- default
    3 : order-4 rot. sym.
    4 : 1-way hori. reflx. sym.
    5 : 2-way hori. reflx. sym.
    6 : 1-way diag. reflx. sym.
    7 : 2-way diag. reflx. sym.
    8 : full 8-fold sym.
    usingAreaInd used for geom. indic. == 6~8
     */

    private static int difficulty;
    private static int emptyCellCnt;

    private static int cnt = 0;

    public SudokuGenThread(int searchCnt)
            throws SudokuException {
        SDK_TEMPLATE = new SudokuTemplate(size, hsize);
        this.searchCnt = searchCnt;
    }

    public static void changeSettings(int maximaSearchCnt, int iterationRate, int difficultyLimit){
        SudokuGenThread.maximaSearchCnt = maximaSearchCnt;
        SudokuGenThread.iterationRate = iterationRate;
        SudokuGenThread.difficultyLimit = difficultyLimit;
    }

    public static void setGenData(int size, int hsize, int targetDifficulty, int geometryIndicator) {
        // must be called before running threads
        SudokuGenThread.size = size;
        odd = size%2 == 1;
        halfSize = (size+1)/2;
        sqSize = size*size;
        SudokuGenThread.hsize = hsize;
        // vsize = size/hsize;
        SudokuGenThread.targetDifficulty = targetDifficulty > difficultyLimit ? Integer.MAX_VALUE : targetDifficulty;
        SudokuGenThread.geometryIndicator = geometryIndicator;
        difficulty = 0;
        emptyCellCnt = 0;
        cnt = 0;
        switch(geometryIndicator){
            case 6 :
                usingAreaInd = new int[size*(size+1)/2][2];
                for(int r=0, ind=0; r<size; r++){
                    for(int c=0; c<size-r; c++){
                        usingAreaInd[ind++] = new int[]{r,c};
                    }
                }break;
            case 7 :
                usingAreaInd = new int[((size+1)/2)*(size/2+1)][2];
                for(int r=0, ind=0; r<size; r++){
                    int bound = r<size/2 ? r+1 : size-r;
                    for(int c=0; c<bound; c++){
                        usingAreaInd[ind++] = new int[]{r,c};
                    }
                }break;
            case 8 :
                usingAreaInd = new int[halfSize*(halfSize+1)/2][2];
                for(int r=0, ind=0; r<(size+1)/2; r++){
                    for(int c=0; c<=r; c++){
                        usingAreaInd[ind++] = new int[]{r,c};
                    }
                }break;
        }
    }

    public void setBaseGrid(long seed){
        SDK_TEMPLATE.dlxSudoku.randomSearchSolution(new Random(seed));
        baseGrid = SDK_TEMPLATE.solutionInIntArray();
    }

    /*
    // if all gen threads are desired to play under the same baseGrid
    public void setBaseGrid(int[][] baseGrid){
        this.baseGrid = new int[baseGrid.length][];
        for(int i=0; i<baseGrid.length; i++){
            this.baseGrid[i] = baseGrid[i].clone();
        }
    }

    public int[][] getBaseGrid(){
        return baseGrid;
    }*/

    private int[][] searchwiseBestGrid;
    private int searchwiseBestDifficulty;
    private int searchwiseBestEmptyCellCnt;

    private void generatePuzzleGrid(long seed){

        Random rand = new Random(seed);

        int[][] tempGrid = new int[size][size];
        int[] tempPuzzleInfo = new int[3];
        // {solutionCnt, branchDiff, emptyCellsCnt}

        int[][] currentBestGrid = new int[size][size];
        int[] currentBestPuzzleInfo = new int[3];

        for(int i=0; i<baseGrid.length; i++){
            tempGrid[i] = baseGrid[i].clone();
            currentBestGrid[i] = baseGrid[i].clone();
        }

        boolean eraseSwitch = true;

        for(int iteration=0; iteration<iterationRate; iteration++){
            switch(geometryIndicator){
                case 1 : // asym.
                    if(eraseSwitch){
                        for(int i=0; i<3; i++){
                            int tgInd = rand.nextInt(sqSize - tempPuzzleInfo[2] - i);
                            int ind=-1;
                            for(int j=0; j<=tgInd; j++){
                                do{
                                    ind++;
                                }while(tempGrid[ind/size][ind%size] < 0);
                            }tempGrid[ind/size][ind%size] = -1;
                        }
                    }else{
                        for(int i=0; i<2; i++){
                            int tgInd = rand.nextInt(tempPuzzleInfo[2] - i);
                            int ind=-1;
                            for(int j=0; j<=tgInd; j++){
                                do{
                                    ind++;
                                }while(tempGrid[ind/size][ind%size] >= 0);
                            }tempGrid[ind/size][ind%size] = baseGrid[ind/size][ind%size];
                        }
                    }break;
                case 3 : // order-4 rot. sym.
                    if(eraseSwitch){
                        int tgInd = rand.nextInt((sqSize+3-tempPuzzleInfo[2])/4);
                        int ind=-1;
                        for(int j=0; j<=tgInd; j++){
                            do{
                                ind++;
                            }while(tempGrid[ind/halfSize][halfSize - ind%halfSize - 1] < 0);
                        }int rind = ind/halfSize, cind = halfSize - ind%halfSize - 1;
                        tempGrid[rind][cind] = -1;
                        tempGrid[size-cind-1][rind] = -1;
                        tempGrid[size-rind-1][size-cind-1] = -1;
                        tempGrid[cind][size-rind-1] = -1;
                    }else{
                        int tgInd = rand.nextInt((tempPuzzleInfo[2]+3)/4);
                        int ind=-1;
                        for(int j=0; j<=tgInd; j++){
                            do{
                                ind++;
                            }while(tempGrid[ind/halfSize][halfSize - ind%halfSize - 1] >= 0);
                        }int rind = ind/halfSize, cind = halfSize - ind%halfSize - 1;
                        tempGrid[rind][cind] = baseGrid[rind][cind];
                        tempGrid[size-cind-1][rind] = baseGrid[size-cind-1][rind];
                        tempGrid[size-rind-1][size-cind-1] = baseGrid[size-rind-1][size-cind-1];
                        tempGrid[cind][size-rind-1] = baseGrid[cind][size-rind-1];
                    }break;
                case 4 : // 1-way hori. reflx. sym.
                    int axisEmpty = 0;
                    if(odd)
                        for(int[] row : tempGrid)
                            if(row[size/2] < 0) axisEmpty++;
                    if(eraseSwitch){
                        int tgInd = rand.nextInt(size*halfSize - (tempPuzzleInfo[2]+axisEmpty)/2);
                        int ind=-1;
                        for(int j=0; j<=tgInd; j++){
                            do{
                                ind++;
                            }while(tempGrid[ind/halfSize][ind%halfSize] < 0);
                        }int rind = ind/halfSize, cind = ind%halfSize;
                        tempGrid[rind][cind] = -1;
                        tempGrid[rind][size-cind-1] = -1;
                    }else{
                        int tgInd = rand.nextInt((tempPuzzleInfo[2]+axisEmpty)/2);
                        int ind=-1;
                        for(int j=0; j<=tgInd; j++){
                            do{
                                ind++;
                            }while(tempGrid[ind/halfSize][ind%halfSize] >= 0);
                        }int rind = ind/halfSize, cind = ind%halfSize;
                        tempGrid[rind][cind] = baseGrid[rind][cind];
                        tempGrid[rind][size-cind-1] = baseGrid[rind][size-cind-1];
                    }break;
                case 5 : // 2-way hori. reflx. sym.
                    axisEmpty = 0;
                    if(odd){
                        for(int i=0; i<size; i++){
                            if(tempGrid[i][halfSize-1] < 0) axisEmpty++;
                            if(tempGrid[halfSize-1][i] < 0) axisEmpty++;
                        }
                    }
                    if(eraseSwitch){
                        int tgInd = rand.nextInt(halfSize*halfSize - (tempPuzzleInfo[2]+axisEmpty+1)/4);
                        int ind=-1;
                        for(int j=0; j<=tgInd; j++){
                            do{
                                ind++;
                            }while(tempGrid[ind/halfSize][ind%halfSize] < 0);
                        }int rind = ind/halfSize, cind = ind%halfSize;
                        tempGrid[rind][cind] = -1;
                        tempGrid[rind][size-cind-1] = -1;
                        tempGrid[size-rind-1][cind] = -1;
                        tempGrid[size-rind-1][size-cind-1] = -1;
                    }else{
                        int tgInd = rand.nextInt((tempPuzzleInfo[2]+axisEmpty+1)/4);
                        int ind=-1;
                        for(int j=0; j<=tgInd; j++){
                            do{
                                ind++;
                            }while(tempGrid[ind/halfSize][ind%halfSize] >= 0);
                        }int rind = ind/halfSize, cind = ind%halfSize;
                        tempGrid[rind][cind] = baseGrid[rind][cind];
                        tempGrid[rind][size-cind-1] = baseGrid[rind][size-cind-1];
                        tempGrid[size-rind-1][cind] = baseGrid[size-rind-1][cind];
                        tempGrid[size-rind-1][size-cind-1] = baseGrid[size-rind-1][size-cind-1];
                    }break;
                case 6 : // 1-way diag. reflx. sym.
                    axisEmpty = 0;
                    for(int i=0; i<size; i++){
                        if(tempGrid[i][size-i-1] < 0) axisEmpty++;
                    }
                    if(eraseSwitch){
                        int tgInd = rand.nextInt(usingAreaInd.length - (tempPuzzleInfo[2] + axisEmpty)/2);
                        int ind=-1;
                        for(int j=0; j<=tgInd; j++){
                            do{
                                ind++;
                            }while(tempGrid[usingAreaInd[ind][0]][usingAreaInd[ind][1]] < 0);
                        }int rind = usingAreaInd[ind][0], cind = usingAreaInd[ind][1];
                        tempGrid[rind][cind] = -1;
                        tempGrid[size-cind-1][size-rind-1] = -1;
                    }else{
                        int tgInd = rand.nextInt((tempPuzzleInfo[2] + axisEmpty)/2);
                        int ind=-1;
                        for(int j=0; j<=tgInd; j++){
                            do{
                                ind++;
                            }while(tempGrid[usingAreaInd[ind][0]][usingAreaInd[ind][1]] >= 0);
                        }int rind = usingAreaInd[ind][0], cind = usingAreaInd[ind][1];
                        tempGrid[rind][cind] = baseGrid[rind][cind];
                        tempGrid[size-cind-1][size-rind-1] = baseGrid[size-cind-1][size-rind-1];
                    }break;
                case 7 : // 2-way diag. reflx. sym.
                    axisEmpty = 0;
                    for(int i=0; i<size; i++){
                        if(tempGrid[i][size-i-1] < 0) axisEmpty++;
                        if(tempGrid[i][i] < 0) axisEmpty++;
                    }
                    if(eraseSwitch){
                        int tgInd = rand.nextInt(usingAreaInd.length - (tempPuzzleInfo[2]+axisEmpty+1)/4);
                        int ind=-1;
                        for(int j=0; j<=tgInd; j++){
                            do{
                                ind++;
                            }while(tempGrid[usingAreaInd[ind][0]][usingAreaInd[ind][1]] < 0);
                        }int rind = usingAreaInd[ind][0], cind = usingAreaInd[ind][1];
                        tempGrid[rind][cind] = -1;
                        tempGrid[size-cind-1][size-rind-1] = -1;
                        tempGrid[cind][rind] = -1;
                        tempGrid[size-rind-1][size-cind-1] = -1;
                    }else{
                        int tgInd = rand.nextInt((tempPuzzleInfo[2]+axisEmpty+1)/4);
                        int ind=-1;
                        for(int j=0; j<=tgInd; j++){
                            do{
                                ind++;
                            }while(tempGrid[usingAreaInd[ind][0]][usingAreaInd[ind][1]] >= 0);
                        }int rind = usingAreaInd[ind][0], cind = usingAreaInd[ind][1];
                        tempGrid[rind][cind] = baseGrid[rind][cind];
                        tempGrid[size-cind-1][size-rind-1] = baseGrid[size-cind-1][size-rind-1];
                        tempGrid[cind][rind] = baseGrid[cind][rind];
                        tempGrid[size-rind-1][size-cind-1] = baseGrid[size-rind-1][size-cind-1];
                    }break;
                case 8 : // full 8-fold sym.
                    axisEmpty = 0;
                    for(int i=0; i<halfSize; i++)
                        if(tempGrid[i][i] < 0) axisEmpty++;
                    if(odd)
                        for(int i=0; i<halfSize; i++)
                            if(tempGrid[halfSize-1][i] < 0) axisEmpty++;
                    if(eraseSwitch){
                        int tgInd = rand.nextInt(usingAreaInd.length - (tempPuzzleInfo[2]+axisEmpty*4)/8);
                        int ind=-1;
                        for(int j=0; j<=tgInd; j++){
                            do{
                                ind++;
                            }while(tempGrid[usingAreaInd[ind][0]][usingAreaInd[ind][1]] < 0);
                        }int rind = usingAreaInd[ind][0], cind = usingAreaInd[ind][1];
                        tempGrid[rind][cind] = -1;
                        tempGrid[size-cind-1][size-rind-1] = -1;
                        tempGrid[cind][rind] = -1;
                        tempGrid[size-rind-1][size-cind-1] = -1;
                        tempGrid[size-rind-1][cind] = -1;
                        tempGrid[cind][size-rind-1] = -1;
                        tempGrid[size-cind-1][rind] = -1;
                        tempGrid[rind][size-cind-1] = -1;
                    }else{
                        int tgInd = rand.nextInt((tempPuzzleInfo[2]+axisEmpty*4)/8);
                        int ind=-1;
                        for(int j=0; j<=tgInd; j++){
                            do{
                                ind++;
                            }while(tempGrid[usingAreaInd[ind][0]][usingAreaInd[ind][1]] >= 0);
                        }int rind = usingAreaInd[ind][0], cind = usingAreaInd[ind][1];
                        tempGrid[rind][cind] = baseGrid[rind][cind];
                        tempGrid[size-cind-1][size-rind-1] = baseGrid[size-cind-1][size-rind-1];
                        tempGrid[cind][rind] = baseGrid[cind][rind];
                        tempGrid[size-rind-1][size-cind-1] = baseGrid[size-rind-1][size-cind-1];
                        tempGrid[size-rind-1][cind] = baseGrid[size-rind-1][cind];
                        tempGrid[cind][size-rind-1] = baseGrid[cind][size-rind-1];
                        tempGrid[size-cind-1][rind] = baseGrid[size-cind-1][rind];
                        tempGrid[rind][size-cind-1] = baseGrid[rind][size-cind-1];
                    }break;
                default : // order-2 rot. sym. <- default
                    if(eraseSwitch){
                        int tgInd = rand.nextInt((sqSize+1-tempPuzzleInfo[2])/2);
                        int ind=-1;
                        for(int j=0; j<=tgInd; j++){
                            do{
                                ind++;
                            }while(tempGrid[ind/size][ind%size] < 0);
                        }int rind = ind/size, cind = ind%size;
                        tempGrid[rind][cind] = -1;
                        tempGrid[size-rind-1][size-cind-1] = -1;
                    }else{
                        int tgInd = rand.nextInt((tempPuzzleInfo[2]+1)/2);
                        int ind=-1;
                        for(int j=0; j<=tgInd; j++){
                            do{
                                ind++;
                            }while(tempGrid[ind/size][ind%size] >= 0);
                        }int rind = ind/size, cind = ind%size;
                        tempGrid[rind][cind] = baseGrid[rind][cind];
                        tempGrid[size-rind-1][size-cind-1] = baseGrid[size-rind-1][size-cind-1];
                    }
            }

            tempPuzzleInfo = SDK_TEMPLATE.getPuzzleInfo(tempGrid, true);
            if(tempPuzzleInfo[0] > 1 || tempPuzzleInfo[1] > targetDifficulty){
                eraseSwitch = false;
                continue;
            }eraseSwitch = true;

            if(tempPuzzleInfo[1] > currentBestPuzzleInfo[1]
            || (tempPuzzleInfo[1] == currentBestPuzzleInfo[1]
                    && tempPuzzleInfo[2] > currentBestPuzzleInfo[2])){
                for(int i=0; i<size; i++){
                    currentBestGrid[i] = tempGrid[i].clone();
                }
                currentBestPuzzleInfo = tempPuzzleInfo.clone();
            }
        }
        searchwiseBestGrid = currentBestGrid;
        searchwiseBestDifficulty = currentBestPuzzleInfo[1];
        searchwiseBestEmptyCellCnt = currentBestPuzzleInfo[2];
    }

    private static final Object PROGRESS_KEY = new Object();

    private void progress(){
        synchronized (PROGRESS_KEY) {
            cnt++;
            if(cnt % (maximaSearchCnt / 100) == 0){
                System.out.print("\b\b\b\b");
                System.out.printf("%3d%%", cnt/(maximaSearchCnt/100)); // works like progress bar
            }
        }
    }

    private static final Object OVERWRITE_KEY = new Object();

    private void overwrite(int[][] grid, int diff, int empty){
        synchronized (OVERWRITE_KEY) {
            if(difficulty < diff
            || (difficulty == diff && emptyCellCnt < empty)){
                difficulty = diff;
                emptyCellCnt = empty;
                targetBaseGrid = baseGrid;
                targetGrid = grid;
            }
        }
    }

    @Override
    public void run() {
        int[][] threadBestGrid = baseGrid;
        int threadBestDifficulty = 0;
        int threadBestEmptyCellCnt = 0;
        for(int search = 1; search <= searchCnt; search++){
            generatePuzzleGrid(System.nanoTime());
            if(threadBestDifficulty < searchwiseBestDifficulty
            || (threadBestDifficulty == searchwiseBestDifficulty
                    && threadBestEmptyCellCnt < searchwiseBestEmptyCellCnt)){
                threadBestGrid = searchwiseBestGrid;
                threadBestDifficulty = searchwiseBestDifficulty;
                threadBestEmptyCellCnt = searchwiseBestEmptyCellCnt;
            }progress();
        }overwrite(threadBestGrid, threadBestDifficulty, threadBestEmptyCellCnt);
    }

    public static int[][] getBestPuzzle(){
        return targetGrid;
    }

    public static int[][] getSolution(){
        return targetBaseGrid;
    }

    public static double getBestDifficultyRate(){
        return difficulty + emptyCellCnt/(double)sqSize;
    }
}
