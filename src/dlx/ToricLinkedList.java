package dlx;

/*
 * dlx algorithm interface for solving general exact cover problems
 * source : https://arxiv.org/pdf/cs/0011047.pdf
 */

import java.util.Arrays;
import java.util.Random;

public class ToricLinkedList {

    public HeaderQNode root;
    private int solutionCnt = 0;
    private QNode[] solution = null;

    public ToricLinkedList(){
        root = new HeaderQNode();

        root.leftQNode = root;
        root.rightQNode = root;
        root.upperQNode = root;
        root.lowerQNode = root;
        root.headerQNode = root;
        root.primary = true;
    }

    public ToricLinkedList(String name){
        root = new HeaderQNode(name, -1, true);

        root.leftQNode = root;
        root.rightQNode = root;
        root.upperQNode = root;
        root.lowerQNode = root;
        root.headerQNode = root;
    }

    public ToricLinkedList(String name, String[] columnNames, boolean[] whichPrimary){
        this(name);
        if(columnNames.length == whichPrimary.length){
            for(int i=0; i<columnNames.length; i++){
                addColumn(columnNames[i], i, whichPrimary[i]);
            }
        }
    }

    public ToricLinkedList(String name, String[] columnNames, boolean[] whichPrimary, boolean[][] constraints){
        this(name, columnNames, whichPrimary);
        for(int i=0; i<constraints.length; i++){
            addRow(i, constraints[i]);
        }
    }

    public void addColumn(String name, int indicator, boolean primary){
        HeaderQNode newCol = new HeaderQNode(name, indicator, primary);

        newCol.leftQNode = root.leftQNode;
        newCol.rightQNode = root;
        newCol.upperQNode = newCol;
        newCol.lowerQNode = newCol;
        newCol.headerQNode = root;

        root.leftQNode.rightQNode = newCol;
        root.leftQNode = newCol;

        root.cnt++;
    }

    public void addRow(int indicator, boolean[] boolNodesRow){
        if(boolNodesRow.length != root.cnt) return;

        QNode first = null;
        HeaderQNode colNd = root.rightQNode;

        for(int i=0; i<boolNodesRow.length; i++, colNd = colNd.rightQNode){
            if(boolNodesRow[i]){
                QNode qnd = new QNode(indicator);
                // System.out.println(indicator);

                qnd.upperQNode = colNd.upperQNode;
                qnd.lowerQNode = colNd;
                qnd.headerQNode = colNd;

                colNd.upperQNode.lowerQNode = qnd;
                colNd.upperQNode = qnd;
                colNd.cnt++;

                if(first == null){
                    qnd.rightQNode = qnd;
                    qnd.leftQNode = qnd;

                    first = qnd;
                }else{
                    qnd.leftQNode = first.leftQNode;
                    qnd.rightQNode = first;

                    first.leftQNode.rightQNode = qnd;
                    first.leftQNode = qnd;
                }
            }
        }// System.out.println();
    }

    public void cover(HeaderQNode colNd){

        colNd.leftQNode.rightQNode = colNd.rightQNode;
        colNd.rightQNode.leftQNode = colNd.leftQNode;

        QNode cnd = colNd.lowerQNode;
        while(cnd != colNd){
            QNode rnd = cnd.rightQNode;
            while(rnd != cnd){
                rnd.upperQNode.lowerQNode = rnd.lowerQNode;
                rnd.lowerQNode.upperQNode = rnd.upperQNode;
                rnd.headerQNode.cnt--;

                rnd = rnd.rightQNode;
            }
            cnd = cnd.lowerQNode;
        }
        root.cnt--;
    }

    public void uncover(HeaderQNode colNd){

        QNode cnd = colNd.upperQNode;
        while(cnd != colNd){
            QNode rnd = cnd.leftQNode;
            while(rnd != cnd){
                rnd.upperQNode.lowerQNode = rnd;
                rnd.lowerQNode.upperQNode = rnd;
                rnd.headerQNode.cnt++;

                rnd = rnd.leftQNode;
            }
            cnd = cnd.upperQNode;
        }
        root.cnt++;

        colNd.leftQNode.rightQNode = colNd;
        colNd.rightQNode.leftQNode = colNd;
    }

    public void testPrint(){
        HeaderQNode cnd = root.rightQNode;
        while(cnd != root){
            System.out.printf("%5s %5b ", cnd.name, cnd.primary);
            QNode qnd = cnd.lowerQNode;
            while(qnd != cnd){
                System.out.printf("%2d ", qnd.indicator);
                qnd = qnd.lowerQNode;
            }
            cnd = cnd.rightQNode;
            System.out.println();
        }
    }

    public void searchSolution(){
        this.searchSolution(Integer.MAX_VALUE-1);
    }

    public void searchSolution(int maxCnt){
        // optimal branching
        // solution & solutionCnt will be overwritten as ---
        // first-found solution /&/ 0-1-2 int switch for no-unique-multiple solution(s)

        // print every possible solution for exact cover problem
        /*
        System.out.println(root.name);
        System.out.println();
         */

        solutionCnt = 0;
        QNode[] stack = new QNode[root.cnt];
        int depth = 0;
        boolean currentlyValid = true;

        search : while(depth>=0){
            // System.out.printf("depth %d check\n", depth);
            if(currentlyValid){ // proceed
                HeaderQNode colNd = root;
                do{
                    colNd = colNd.rightQNode;
                }while(!colNd.primary);
                if(colNd == root){ // solution found
                    solutionCnt++;
                    if(solutionCnt==1){
                        solution = Arrays.copyOf(stack, depth);
                    }
                    if(solutionCnt >= maxCnt){
                        // System.out.println("too many solutions searched");
                        break;
                    }
                    /*
                    if(solutionCnt <= maxPrint){
                        System.out.printf("solution found : cnt %d\n", solutionCnt);
                        System.out.print(qnsfm.qNodeStackFormat(stack, depth));
                        System.out.println();
                    }*/

                    depth--;
                    currentlyValid = false;
                    continue;
                }

                HeaderQNode minColNd = colNd;
                do{
                    // System.out.printf("\tcol %s : %d ", colNd.name, colNd.cnt);
                    if(colNd.cnt == 0){
                        // branch not valid
                        // System.out.printf(": branch invalid\n");
                        depth--;
                        currentlyValid = false;
                        continue search;
                    }
                    if(minColNd.cnt > colNd.cnt){
                        // System.out.printf("-> min");
                        minColNd = colNd;
                    }
                    // System.out.println();
                    do{
                        colNd = colNd.rightQNode;
                    }while(!colNd.primary);
                }while(colNd != root);
                cover(minColNd);
                // System.out.println("\tcover complete");

                stack[depth] = minColNd.lowerQNode;
                QNode qnd = stack[depth].rightQNode;
                while(qnd != stack[depth]){
                    cover(qnd.headerQNode);
                    qnd = qnd.rightQNode;
                }
                depth++;
            }else{ // backtrack & change branch
                QNode qnd = stack[depth].leftQNode;
                while(qnd != stack[depth]){
                    uncover(qnd.headerQNode);
                    qnd = qnd.leftQNode;
                }

                HeaderQNode colNd = stack[depth].headerQNode;
                stack[depth] = stack[depth].lowerQNode;
                if(stack[depth] == colNd){
                    uncover(colNd);
                    stack[depth--] = null;
                    continue;
                }
                qnd = stack[depth].rightQNode;
                while(qnd != stack[depth]){
                    cover(qnd.headerQNode);
                    qnd = qnd.rightQNode;
                }
                depth++;
                currentlyValid = true;
            }
        }

        // System.out.printf("total solution count : %d\n", solutionCnt);
    }

    public void randomSearchSolution(Random rand) {
        // random branching
        // solution & solutionCnt will be overwritten as ---
        // randomly found solution /&/ (always) 1

        solutionCnt = 0;
        QNode[] stack = new QNode[root.cnt];
        int depth = 0;
        boolean currentlyValid = true;

        search :
        while(depth >= 0){

            // System.out.println("depth "+depth);
            if(currentlyValid){ // proceed
                HeaderQNode colNd = root;
                do{
                    colNd = colNd.rightQNode;
                }while(!colNd.primary);
                if(colNd == root){ // solution found; return point
                    solutionCnt++;
                    solution = Arrays.copyOf(stack, depth);
                    return;
                }

                int minCnt = Integer.MAX_VALUE; // minimum val. of colNd.cnt
                int minNum = 0; // num. of column nodes, of which colNd.cnt == minCnt
                do{
                    if(colNd.cnt == 0){
                        // branch not valid
                        depth--;
                        currentlyValid = false;
                        continue search;
                    }
                    if(colNd.cnt < minCnt){
                        minCnt = colNd.cnt;
                        minNum = 1;
                    }else if(colNd.cnt == minCnt){
                        minNum++;
                    }
                    do{
                        colNd = colNd.rightQNode;
                    }while(!colNd.primary);
                }while(colNd != root);

                int choice = rand.nextInt(minNum);
                colNd = root;
                for(int i=0; i<=choice; i++){
                    do{
                        colNd = colNd.rightQNode;
                    }while(colNd.cnt > minCnt || !colNd.primary);
                }
                cover(colNd);
                stack[depth] = colNd.lowerQNode;
                QNode qnd = stack[depth].rightQNode;
                while(qnd != stack[depth]){
                    cover(qnd.headerQNode);
                    qnd = qnd.rightQNode;
                }
                depth++;
            }else{ // backtrack & change branch
                QNode qnd = stack[depth].leftQNode;
                while(qnd != stack[depth]){
                    uncover(qnd.headerQNode);
                    qnd = qnd.leftQNode;
                }

                HeaderQNode colNd = stack[depth].headerQNode;
                stack[depth] = stack[depth].lowerQNode;
                if(stack[depth] == colNd){ // last QNode choice was wrong
                    uncover(colNd);
                    stack[depth--] = null;
                    continue;
                }

                qnd = stack[depth].rightQNode;
                while(qnd != stack[depth]){ // proceed to next possible branch
                    cover(qnd.headerQNode);
                    qnd = qnd.rightQNode;
                }
                depth++;
                currentlyValid = true;
            }
        }
    }

    public int getSolutionCnt(){
        return solutionCnt;
    }

    public QNode[] getSolution(){
        return solution;
    }

    public void printSolution(){ // default print
        if(solution == null) return;

        for (QNode oriNd : solution) {
            QNode solNd = oriNd;
            String tmp = solNd.indicator + " -> ";
            do {
                tmp = tmp.concat(solNd.headerQNode.name + " ");
                solNd = solNd.rightQNode;
            } while (solNd != oriNd);
            System.out.println(tmp);
        }
    }
    /*
    public static class SolutionFormatter implements QNodeStackFormatter {
        @Override
        public String qNodeStackFormat(QNode[] stack, int depth){
            // System.out.println("stack length : "+stack.length);
            String sfm = "";
            for(int d=0; d<depth; d++){
                QNode solNd = stack[d];
                sfm = sfm.concat(solNd.indicator + " -> ");
                do{
                    sfm = sfm.concat(solNd.headerQNode.name + " ");
                    solNd = solNd.rightQNode;
                }while(solNd != stack[d]);
                sfm = sfm.concat("\n");
            }
            return sfm;
        }
    }
     */
}

