import java.io.*;
import java.util.Arrays;
import java.util.Comparator;

public class homework {
    static float timeRemaining;
    static int numOfFruitTypes;
    static int nodeCount = 0;
    static int setLevel;
    static long startTime;

    public static void main(String[] args) {
        homework game = new homework();
        int size;

        String fileName = "input.txt";
        String line;
        startTime = System.currentTimeMillis();


        try {
            FileReader fileReader = new FileReader(fileName);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            line = bufferedReader.readLine();
            size = Integer.parseInt(line);
            line = bufferedReader.readLine();
            numOfFruitTypes = Integer.parseInt(line);
            line = bufferedReader.readLine();
            timeRemaining = Float.parseFloat(line);

            char[][] inputBoard = new char[size][size];

            for (int i = 0; i < size; i++) {
                line = bufferedReader.readLine();
                char[] arr = line.toCharArray();
                for (int j = 0; j < size; j++) {
                    inputBoard[i][j] = arr[j];
                }
            }

            bufferedReader.close();
            game.findNextMove(inputBoard, size);
        }
        catch(Exception ex) {
            System.out.println(
                    "FAIL" + ex.getMessage() );
        }
    }

    void findNextMove(char[][] inputBoard, int size){
        //long startTime = System.currentTimeMillis();
        int leadPoints = 0;
        short maxGroupNum = 0;
        FruitBoard initialFruitBoard = new FruitBoard(inputBoard, size, leadPoints, maxGroupNum);
        initialFruitBoard.groups = initialFruitBoard.computeGroups(size);
        int numGroups = initialFruitBoard.maxGroupNum;


        if (timeRemaining < 10)
            setLevel = 1;
        else if (timeRemaining < 30)
            setLevel = 2;
        else if(timeRemaining < 50){
            if(size > 10 || numGroups > 50)
                setLevel = 2;
            else if (size >= 5)
                setLevel = 4;
            else
                setLevel = 5;
        }

        else if(timeRemaining < 80){
            if(size > 10 && numGroups > 50)
                setLevel = 2;
            else if (size >= 10)
                setLevel = 3;
            else if (size >= 5)
                setLevel = 4;
            else
                setLevel = 5;
        }

        else if(size >= 20)
            setLevel = 3;
        else if(size >= 14)
            setLevel = 4;
        else if (size >= 12)
            setLevel = 5;
        else if (size >= 9)
            setLevel = 6;
        else if (size >= 7)
            setLevel = 7;
        else if (size >= 5)
            setLevel = 8;
        else if (size >= 3)
            setLevel = 10;
        else
            setLevel = 3;

        System.out.println("Level is " + setLevel);
        //setLevel = 4;

        FruitBoard bestMoveBoard = minimax(initialFruitBoard, size,0, 0,  true, Integer.MIN_VALUE, Integer.MAX_VALUE);
        System.out.println("Time taken is " + (System.currentTimeMillis()-startTime));
        printSolution(bestMoveBoard, size);
    }

    private void printSolution(FruitBoard bestMoveBoard, int size){
        try {
            File f = new File("output.txt");
            FileOutputStream fos;
            fos = new FileOutputStream(f);
            PrintWriter pw = new PrintWriter(fos);
            char pickedColumn = (char) (bestMoveBoard.pickedY+65);

            String str = String.valueOf(pickedColumn) + String.valueOf(bestMoveBoard.pickedX+1);
            pw.write(str);
            pw.write("\n");
            for(int i=0; i<size; i++){
                for(int j=0; j<size; j++){
                    pw.write(String.valueOf(bestMoveBoard.board[i][j]));
                }
                pw.write("\n");
            }

            pw.flush();
            fos.close();
            pw.close();
        }

        catch(Exception ex) {
            System.out.println(
                    "FILE WRITE FAIL" );
        }
    }


    private FruitBoard minimax(FruitBoard currBoard, int size, int level, int leadPoints, boolean aiPlayer, int alpha, int beta){
        FruitBoard bestBoard = new FruitBoard();
        nodeCount++;

        if (currBoard.isTerminalState(size)|| level == setLevel){
            currBoard.leadPoints = leadPoints;
            return currBoard;
        }

        for(int i=1; i <= currBoard.maxGroupNum; i++){
            int leadPointsToPass = leadPoints;
            FruitBoard newBoard = new FruitBoard(currBoard.board, size, currBoard.leadPoints, currBoard. maxGroupNum);
            newBoard.maxGroupNum = 0;
            newBoard.groups = newBoard.computeGroups(size);


            //FindBestGroup function returns in the order of biggest to smallest groups. Can serve as a heuristic to improve Alpha Beta Pruning

            Integer[] groupOrder = newBoard.findBestGroup(newBoard.maxGroupNum, size);

            //calling based on the sorted order
            int numberOfPickedFruits = newBoard.pickFruitGroup(groupOrder[i-1]+1, size);
            int points = (numberOfPickedFruits*numberOfPickedFruits);

            if(aiPlayer){
                leadPointsToPass += points;
                FruitBoard max = minimax(newBoard, size, level+1, leadPointsToPass,false, alpha, beta);
                if(max.leadPoints > alpha){
                    alpha = max.leadPoints;
                    bestBoard = newBoard;
                }
            }

            else {
                leadPointsToPass -= points;
                FruitBoard min = minimax(newBoard, size, level+1, leadPointsToPass,true, alpha, beta);
                if(min.leadPoints < beta){
                    beta = min.leadPoints;
                    bestBoard = newBoard;
                }
            }

            if(alpha >= beta){
                break;
            }
        }
        if(aiPlayer)
            bestBoard.leadPoints = alpha;
        else
            bestBoard.leadPoints = beta;
        return bestBoard;
    }

    class FruitBoard {
        char[][] board;
        short[][] groups;
        int leadPoints;
        int pickedX, pickedY;
        short maxGroupNum;

        FruitBoard(){

        }

        FruitBoard(char[][] oldBoard, int size, int points, short group){
            board = new char[size][size];
            groups = new short[size][size];
            for(int i=0; i<size; i++)
                for(int j=0; j<size; j++)
                    board[i][j] = oldBoard[i][j];
            leadPoints = points;
            maxGroupNum = group;
            pickedX = 0;
            pickedY = 0;
        }

        //pick a Group, and compute points, new groups, new board
        protected int pickFruitGroup(int pickGroup, int size){
            for(int currRow = 0; currRow < size; currRow++){
                for(int currColumn = 0; currColumn < size; currColumn++){
                    if(groups[currRow][currColumn] == pickGroup){
                        pickedX = currRow;
                        pickedY = currColumn;
                        return pickAllFruits(size, currRow, currColumn, pickGroup);
                    }
                }
            }
            return 0;
        }

        protected int pickAllFruits(int size, int startRow, int startColumn, int pickGroup){
            int numOfFruits = 0;
            for(int i = startRow; i< size ; i++){
                for(int j = 0; j < size; j++){
                    if(groups[i][j] == pickGroup){
                        board[i][j] = '*';
                        numOfFruits++;
                    }
                }
            }

            // call function to move all *'s to the top
            resetBoard(size);

            // reset groups, and create new groups.
            for(int i=0; i<size; i++){
                for(int j=0; j<size; j++){
                    groups[i][j] = 0;
                }
            }

            maxGroupNum = 0;
            groups = computeGroups(size);
            return numOfFruits;
        }

        protected void resetBoard(int size){
            for(int i=0; i<size; i++){
                for(int j=0; j<size; j++){
                    int checkRow = i;
                    if(board[checkRow][j] == '*' && checkRow > 0){
                        while(checkRow>0 && board[checkRow-1][j] != '*'){
                            if(board[checkRow-1][j] != '*'){
                                board[checkRow][j] = board[checkRow-1][j];
                                board[checkRow-1][j] = '*';
                                checkRow--;
                            }
                        }
                    /*if(currBoard.board[checkRow-1][j] != '*' && checkRow>0){
                        currBoard.board[checkRow][j] = currBoard.board[checkRow-1][j];
                        currBoard.board[checkRow-1][j] = '*';
                    }*/
                    }
                }
            }
        }

        protected boolean isTerminalState(int size){
            for(int currRow = 0; currRow<size; currRow++){
                for(int currColumn = 0; currColumn<size; currColumn++){
                    if (board[currRow][currColumn] >= '0' && board[currRow][currColumn] <='9')
                        return false;
                }
            }
            return true;
        }

        //Compute the different groups and keep to be used later
        protected short[][] computeGroups(int size){
            for(int currRow = 0; currRow < size; currRow++){
                for(int currColumn = 0; currColumn < size; currColumn++){
                    //if the current element is not * and if there are no groups assigned to it already, then create a group
                    if(board[currRow][currColumn] != '*' && groups[currRow][currColumn] == 0){
                        //commented to add new group function
                        groups[currRow][currColumn] = ++maxGroupNum;
                        short assignGroupNum = maxGroupNum;
                        //createGroupNew(size, currRow, currColumn, assignGroupNum, board[currRow][currColumn]);
                        createGroup(size, currRow, currColumn, assignGroupNum);

                    }
                    else if (board[currRow][currColumn] != '*'){
                        createGroupNew(size, currRow, currColumn, groups[currRow][currColumn], board[currRow][currColumn]);
                    }
                }
            }
            return groups;
        }

        //Create a Group for the found new element, if any
        protected FruitBoard createGroup(int size, int startRow, int startColumn, short groupNum){
            for(int i = startRow+1; i < size ; i++){
                if((board[i][startColumn] == board[startRow][startColumn]) && groups[i][startColumn] ==0) {
                    groups[i][startColumn] = groupNum;
                    createGroup(size, i, startColumn, groupNum);
                } else {
                    break;
                }
            }

            for(int i = startRow-1; i>=0; i--){
                if((board[i][startColumn] == board[startRow][startColumn]) && groups[i][startColumn] ==0){
                    groups[i][startColumn] = groupNum;
                    createGroup(size, i, startColumn, groupNum);
                } else {
                    break;
                }
            }

            for(int j = startColumn+1; j < size; j++){
                if((board[startRow][j] == board[startRow][startColumn]) && groups[startRow][j] ==0){
                    groups[startRow][j] = groupNum;
                    createGroup(size, startRow, j, groupNum);
                } else {
                    break;
                }
            }

            for(int j = startColumn-1; j>=0; j--){
                if((board[startRow][j] == board[startRow][startColumn]) && groups[startRow][j] ==0){
                    groups[startRow][j] = groupNum;
                    createGroup(size, startRow, j, groupNum);
                } else {
                    break;
                }
            }

            return this;
        }

        protected void createGroupNew(int size, int startRow, int startColumn, short groupNum, int fruitType){
            if(startRow < 0 || startRow >= size || startColumn < 0 || startColumn >= size)
                return;
            if(board[startRow][startColumn] == fruitType && groups[startRow][startColumn] == 0){
                groups[startRow][startColumn] = groupNum;
                createGroupNew(size, startRow+1, startColumn, groupNum, fruitType);
                createGroupNew(size, startRow-1, startColumn, groupNum, fruitType);
                createGroupNew(size, startRow, startColumn+1, groupNum, fruitType);
                createGroupNew(size, startRow, startColumn-1, groupNum, fruitType);
            }
            else
                return;
        }


        protected Integer[] findBestGroup(int totalGroups, int size){
            Integer[] groupSizes = new Integer[totalGroups];
            for(int i=0; i<totalGroups; i++)
                groupSizes[i] = 0;
            for(int i=0; i<size; i++){
                for(int j=0; j<size; j++){
                    if(groups[i][j] !=0){
                        groupSizes[groups[i][j]-1] +=1;
                    }
                }
            }

            GroupArrayComparator comparator = new GroupArrayComparator(groupSizes);
            Integer[] indices = comparator.createIndexArray();
            Arrays.sort(indices, comparator.reversed());
            return indices;
        }
    }

    class GroupArrayComparator implements Comparator<Integer>
    {
        private final Integer[] array;

        public GroupArrayComparator(Integer[] array)
        {
            this.array = array;
        }

        public Integer[] createIndexArray()
        {
            Integer[] indices = new Integer[array.length];
            for (int i = 0; i < array.length; i++)
            {
                indices[i] = i; // Autoboxing
            }
            return indices;
        }

        @Override
        public int compare(Integer index1, Integer index2)
        {
            return array[index1].compareTo(array[index2]);
        }
    }
}