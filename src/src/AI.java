package src;

import java.awt.*;

public class AI extends Tetris {
    //BlockHeight = 26
    //场地宽度为16, BlockWidth = 16
    public int pieceX, pieceY; // 方块左上角在场地中的坐标
    public boolean[][] piece; // 方块定义，大小为4x4
    public int[] boardInfo;
    public int rotate;    //旋转次数
    public int shift;    //左右平移
    public int evalScore;

    public void AIRunner() throws StackOverflowError {//TODO AI方法， 可能要用到timer进行监听
        while (!IsTouch(NowBlockMap, NowBlockPos)) {
            Point DesPoint;
            switch (AIPlay()) {
                case ShiftRight -> {
                    DesPoint = new Point(NowBlockPos.x, NowBlockPos.y + 1);
                    if (!IsTouch(NowBlockMap, DesPoint)) {
                        NowBlockPos = DesPoint;
                    }
                }
                case ShiftLeft -> {
                    DesPoint = new Point(NowBlockPos.x, NowBlockPos.y - 1);
                    if (!IsTouch(NowBlockMap, DesPoint)) {
                        NowBlockPos = DesPoint;
                    }
                }
                case Drop -> {
                    DesPoint = new Point(NowBlockPos.x, NowBlockPos.y);
                    while (!IsTouch(NowBlockMap, DesPoint)) {
                        DesPoint.y++;
                    }
                    DesPoint.y--;
                    NowBlockPos = DesPoint;
                }
                case Rotate -> {
                    boolean[][] TurnBlock = RotateBlock(NowBlockMap, 1);
                    if (!IsTouch(TurnBlock, NowBlockPos)) {
                        NowBlockMap = TurnBlock;
                    }
                }
            }
        }
    }

    public boolean IsInitial() {
        return pieceX == BlockWidth / 2 && pieceY == BlockHeight - 1 && evalScore == 0;
    }

    public PieceOperator AIPlay() {
        if (IsInitial()) {
            boardInfo = new int[BlockWidth];
            for (int x = 0; x < BlockWidth; x++)
                boardInfo[x] = -1;
            for (int x = 0; x < BlockWidth; x++) {
                for (int y = BlockHeight - 4 - 1; y >= 0; y--) {
                    //最上方四行用来缓存刚刚生成的方块
                    if (!NowBlockMap[y][x])
                        continue;
                    boardInfo[x] = y;
                    break;
                }
            }//获取到了broad中每列最高的方块的纵坐标值
            myPair[] posScore;
            posScore = DiffPos();
            rotate = 0;
            shift = pieceX - posScore[0].pairScore;
            evalScore = posScore[0].pairScore;
            for (int i = 1; i < 4; i++) {
                if (posScore[i].pairScore >= evalScore) {
                    rotate = 1;
                    shift = pieceX - posScore[i].landPoint;
                    evalScore = posScore[i].pairScore;
                }
            }
        }
        if (rotate > 0) {
            rotate--;

            return PieceOperator.Rotate;
        }
        if (shift != 0) {
            if (shift > 0)    //左移
            {
                System.out.println(shift);
                shift--;
                return PieceOperator.ShiftLeft;
            } else    //右移
            {
                System.out.println(shift);
                shift++;
                return PieceOperator.ShiftRight;
            }
        }
        if (evalScore != 0) evalScore = 0;


        return PieceOperator.Drop;
    }

    private myPair[] DiffPos() {
        myPair[] posScore = new myPair[4];
        boolean[][] tmp = new boolean[4][4];
        boolean[][] evalPiece = new boolean[4][4];
        CopyPiece(tmp, piece);
        CopyPiece(evalPiece, piece);
        posScore[0] = eval(evalPiece);//旋转零次
        if (piece[1][1] && piece[1][2] && piece[2][1] && piece[2][2]) {
            for (int i = 1; i < 4; i++)
                posScore[i] = new myPair(-1000000000, -10);
            return posScore;
        }

        RotatePiece(evalPiece, tmp);//旋转一次
        posScore[1] = eval(evalPiece);
        if ((piece[1][0] && piece[1][1] && piece[1][2] && piece[1][3])
                || (piece[1][0] && piece[1][1] && piece[2][1] && piece[2][2])
                || (piece[2][1] && piece[2][2] && piece[1][2] && piece[1][3])) {
            for (int i = 2; i < 4; i++)
                posScore[i] = new myPair(-1000000000, -10);
            return posScore;
        }
        RotatePiece(tmp, piece);
        RotatePiece(evalPiece, tmp);//旋转两次
        posScore[2] = eval(evalPiece);
        CopyPiece(tmp, evalPiece);
        RotatePiece(evalPiece, tmp);//旋转三次
        posScore[3] = eval(evalPiece);

        return posScore;
    }

    private myPair eval(boolean[][] evalPiece) {
        myPair[] scoreList = new myPair[BlockWidth + 1];
        for (int i = -2; i < BlockWidth - 2; i++) {
            int newY = NowBlockPos.y;
            boolean deployable = Deployable_1(evalPiece, i, newY);//判断是否出界
            if (!deployable) {
                myPair tmp = new myPair(-1000000000, -10);
                scoreList[i + 2] = tmp;
                continue;
            }
            boolean found = false;
            for (int x = 0; x < 4; x++) {
                for (int y = 0; y < 4; y++) {
                    if (evalPiece[y][x]) {
                        found = true;
                        break;
                    }
                }
                if (found)
                    break;
            }
            //下落过程中，同时填充进新的false
            newY = BlockHeight - 4 - 1;
            deployable = Deployable_2(evalPiece, i, newY);//newX变量与i相等
            while (deployable) {
                newY--;
                deployable = Deployable_2(evalPiece, i, newY);
            }
            newY++;
            boolean[][] newBoardMap = new boolean[BlockHeight][BlockWidth];
            CopyBoard(newBoardMap, NowBlockMap);
            FillBroad(evalPiece, i, newY, newBoardMap);
            Grade grade = GetScore(evalPiece, newBoardMap, newY, i);
            myPair tmp = new myPair(grade.sum, i);
            scoreList[i + 2] = tmp;
        }

        myPair maxPair = new myPair(-1000000000, -10);//选取max值返回
        for (int i = 0; i <= BlockWidth; i++) {
            if (scoreList[i].pairScore > maxPair.pairScore) maxPair = scoreList[i];
            else if (scoreList[i].pairScore == maxPair.pairScore) {
                int p1 = getPriority(scoreList[i].landPoint);
                int p2 = getPriority(maxPair.landPoint);
                if (p1 > p2) maxPair = scoreList[i];
            }
        }
        return maxPair;
    }

    private int getPriority(int landPoint) {
        int priority = (landPoint - pieceX) * 400;
        if (priority > 0)//右移
            return priority;
        else if (priority < 0)//左移
            return 10 - priority;
        else
            return 0;
    }

    private Grade GetScore(boolean[][] evalPiece, boolean[][] newBoardMap, int newY, int newX) {
        int landingHeight = GetLandingHeight(newY, evalPiece);
        int erodedPieceCellsMetric = GetErodedPieceCellsMetric(newBoardMap, evalPiece, newY, newX);
        int RowTransitions = GetRowTransitions(newBoardMap);
        int ColTransitions = GetColTransitions(newBoardMap);
        int BuriedHoles = GetBuriedHoles(newBoardMap);
        int Wells = GetWells(newBoardMap);

        double sum = 100 * (-4.500158825082766 * landingHeight
                + 3.4181268101392694 * erodedPieceCellsMetric
                - 3.2178882868487753 * RowTransitions
                - 9.348695305445199 * ColTransitions
                - 7.899265427351652 * BuriedHoles
                - 3.3855972247263626 * Wells);

        return new Grade((int) sum, landingHeight, erodedPieceCellsMetric, RowTransitions, ColTransitions, BuriedHoles, Wells);
    }

    private void FillBroad(boolean[][] evalPiece, int newX, int newY, boolean[][] newBoardMap) {
        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 4; y++) {
                if (!evalPiece[y][x])
                    continue;
                newBoardMap[newY - y][newX + x] = evalPiece[y][x];
            }
        }
        for (int y = 0; y < BlockHeight - 4; y++) {
            for (int x = 0; x < BlockWidth; x++) {
                if (!newBoardMap[y][x]) {
                    break;
                }
            }
        }
    }

    private boolean Deployable_1(boolean[][] evalPiece, int newX, int newY) {
        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 4; y++) {
                if (!evalPiece[y][x]) continue;
                if (newX + x < 0 || newX + x >= BlockWidth
                        || newY - y < 0 || newY - y >= BlockHeight) {
                    return false;
                }
            }

        }
        return true;
    }

    private boolean Deployable_2(boolean[][] evalPiece, int newX, int newY) {
        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 4; y++) {
                if (!evalPiece[y][x]) continue;
                if (newX + x < 0 || newX + x >= BlockWidth
                        || newY - y < 0 || newY - y >= BlockHeight
                        || NowBlockMap[newY - y][newX + x]) {
                    return false;
                }
            }
        }
        return true;
    }

    private void CopyPiece(boolean[][] tmp, boolean[][] piece) {
        for (int i = 0; i < 4; i++) {
            System.arraycopy(piece[i], 0, tmp[i], 0, 4);
        }
    }

    private void CopyBoard(boolean[][] tmp, boolean[][] piece) {
        for (int i = 0; i < BlockWidth; i++) {
            for (int j = 0; j < BlockHeight; j++) {
                tmp[j][i] = piece[j][i];
            }
        }
    }

    public void RotatePiece(boolean[][] des, boolean[][] src) {
        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++)
                des[i][j] = src[j][3 - i];
    }

    public int GetWells(boolean[][] newBoard) {
        int wellPoints = 0;
        for (int x = 0; x < BlockWidth; x++) {
            int wellDep = 0;    //井深
            for (int y = BlockHeight - 4 - 1; y >= 0; y--) {
                if (x == 0) {
                    if (!newBoard[y][x] && newBoard[y][x + 1]) {
                        wellDep++;
                    } else if (newBoard[y][x]) {
                        wellPoints += wellDep * (wellDep + 1) / 2;
                        wellDep = 0;
                    }
                } else if (x == BlockWidth - 1) {
                    if (!newBoard[y][x] && newBoard[y][x - 1]) {
                        wellDep++;
                    } else if (newBoard[y][x]) {
                        wellPoints += wellDep * (wellDep + 1) / 2;
                        wellDep = 0;
                    }
                } else {
                    if (!newBoard[y][x] && newBoard[y][x - 1] && newBoard[y][x + 1]) {
                        wellDep++;
                    } else if (newBoard[y][x]) {
                        wellPoints += wellDep * (wellDep + 1) / 2;
                        wellDep = 0;
                    }
                }
            }
            wellPoints += wellDep * (wellDep + 1) / 2;
        }
        return wellPoints;
    }

    public int GetBuriedHoles(boolean[][] newBoard) {
        int holes = 0;
        for (int x = 0; x < BlockWidth; x++) {
            int tmp = 0;
            boolean upFilled = false;
            for (int y = BlockHeight - 4 - 1; y >= 0; y--) {
                //顶上未封
                if ((!upFilled) && newBoard[y][x]) {
                    upFilled = true;
                    continue;
                }
                //顶部已封
                if (upFilled && (!newBoard[y][x])) {
                    tmp++;
                }
            }
            holes += tmp;
        }
        return holes;
    }

    public int GetColTransitions(boolean[][] newBoard) {
        int colTransitions = 0;
        for (int x = 0; x < BlockWidth; x++) {
            boolean last = true;
            boolean now;
            int tmp = 0;
            for (int y = 0; y < BlockHeight - 4; y++) {
                now = newBoard[y][x];
                if (now != last)
                    tmp++;
                last = now;
            }
            colTransitions += tmp;
        }
        return colTransitions;
    }

    public int GetRowTransitions(boolean[][] newBoard) {
        //获取highest piece的纵坐标
        int highest = -1;
        for (int y = BlockHeight - 4 - 1; y >= 0; y--) {
            for (int x = 0; x < BlockWidth; x++) {
                if (newBoard[y][x]) {
                    highest = y;
                    break;
                }
            }
            if (highest >= 0)
                break;
        }

        //计算row_transitions
        int rowTransitions = 0;
        for (int y = 0; y <= highest; y++) {
            int tmp = 0;
            boolean last = true;    //认为边界都是有方块填充的
            boolean now;
            for (int x = 0; x < BlockWidth; x++) {
                now = newBoard[y][x];
                if (last != now) {
                    tmp++;
                }
                last = now;
            }
            if (!newBoard[y][BlockWidth - 1])
                tmp++;
            rowTransitions += tmp;
        }
        return rowTransitions;
    }

    public int GetLandingHeight(int newY, boolean[][] evalPiece) {
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 4; x++) {
                if (evalPiece[y][x])
                    return newY - y;
            }
        }
        return 0;
    }

    public int GetErodedPieceCellsMetric(boolean[][] newBoard, boolean[][] evalPiece, int new_piece_y, int new_piece_x) {
        int eliminated = 0;
        int usefulBlocks = 0;
        for (int y = 0; y < BlockHeight - 4; y++) {
            boolean full = true;
            for (int x = 0; x < BlockWidth; x++) {
                if (!newBoard[y][x]) {
                    full = false;
                    break;
                }
            }
            if (full) {
                for (int x = 0; x < 4; x++) {
                    if (evalPiece[new_piece_y - y][x])
                        usefulBlocks++;
                }
                eliminated++;
                //y--;
            }
        }
        return (eliminated * usefulBlocks);
    }

    public enum PieceOperator {
        ShiftLeft,
        ShiftRight,
        Rotate,
        Drop
    }//执行动作的枚举选项：左移、右移、变形、直接下落

    public static class myPair {
        public int pairScore;
        public int landPoint;

        public myPair(int a, int b) {
            pairScore = a;
            landPoint = b;
        }
    }
}
