package src;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

public class Tetris extends JPanel {

    public static final int BlockSize = 20;
    public static final int BlockWidth = 16;
    public static final int BlockHeight = 26;
    private static final String[] AuthorInfo = {"Producer：", "QiShining", "https://space.bilibili.com/8903675", "https://github.com/Qi-ShanLin", "*方向键控制位移和下落", "*方向上键控制变形", "*空格键控制下落到底"};
    public static int TimeDelay = 1000;
    // 从Shape类中读取7种形状，用于随机生成
    static boolean[][][] Shape = Block_4.Shape;
    // 存放已经固定的方块
    public final boolean[][] BlockMap = new boolean[BlockHeight][BlockWidth];
    //timer
    private final Timer timer;
    // 下落方块的位置,取左上角坐标
    public Point NowBlockPos;
    // 当前的方块矩阵
    public boolean[][] NowBlockMap;
    //是否暂停
    public boolean IsPause = false;
    private boolean IsAuto = true;

    java.awt.event.KeyListener KeyListener = new java.awt.event.KeyListener() {
        @Override
        public void keyPressed(KeyEvent e) {
            if (!IsPause) {
                Point DesPoint;
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_DOWN -> {
                        DesPoint = new Point(Tetris.this.NowBlockPos.x, Tetris.this.NowBlockPos.y + 1);
                        if (!Tetris.this.IsTouch(Tetris.this.NowBlockMap, DesPoint)) {
                            Tetris.this.NowBlockPos = DesPoint;
                        }
                    }
                    case KeyEvent.VK_UP -> {
                        boolean[][] TurnBlock = Tetris.this.RotateBlock(Tetris.this.NowBlockMap, 1);
                        if (!Tetris.this.IsTouch(TurnBlock, Tetris.this.NowBlockPos)) {
                            Tetris.this.NowBlockMap = TurnBlock;
                        }
                    }
                    case KeyEvent.VK_RIGHT -> {
                        DesPoint = new Point(Tetris.this.NowBlockPos.x + 1, Tetris.this.NowBlockPos.y);
                        if (!Tetris.this.IsTouch(Tetris.this.NowBlockMap, DesPoint)) {
                            Tetris.this.NowBlockPos = DesPoint;
                        }
                    }
                    case KeyEvent.VK_LEFT -> {
                        DesPoint = new Point(Tetris.this.NowBlockPos.x - 1, Tetris.this.NowBlockPos.y);
                        if (!Tetris.this.IsTouch(Tetris.this.NowBlockMap, DesPoint)) {
                            Tetris.this.NowBlockPos = DesPoint;
                        }
                    }
                    case KeyEvent.VK_SPACE -> {
                        DesPoint = new Point(Tetris.this.NowBlockPos.x, Tetris.this.NowBlockPos.y);
                        while (!Tetris.this.IsTouch(Tetris.this.NowBlockMap, DesPoint)) {
                            DesPoint.y++;
                        }
                        DesPoint.y--;
                        Tetris.this.NowBlockPos = DesPoint;
                    }
                }
                repaint();
            }
        }//按键监听器

        @Override
        public void keyTyped(KeyEvent e) {
        }

        @Override
        public void keyReleased(KeyEvent e) {
        }
    };
    // 分数
    private int Score = 0;
    // 屏幕中显示的下一个方块矩阵
    private boolean[][] NextBlockMap;
    /**
     * 范围[0,28) 7种，每种有4种旋转状态
     */
    private int NextBlockState;
    private int NowBlockState;
    // 定时器监听
    ActionListener TimerListener = arg0 -> {
        if (Tetris.this.IsTouch(Tetris.this.NowBlockMap, new Point(Tetris.this.NowBlockPos.x, Tetris.this.NowBlockPos.y + 1))) {
            if (Tetris.this.FixBlock()) {
                Tetris.this.Score += Tetris.this.ClearLines() * 10;
                Tetris.this.getNextBlock();
            } else {
                JOptionPane.showMessageDialog(Tetris.this.getParent(), "GAME OVER");
                Tetris.this.Initial();
            }
        } else {
            Tetris.this.NowBlockPos.y++;
        }
        Tetris.this.repaint();
    };

    public Tetris() {//TODO 实例ai方法
        this.Initial();
        timer = new Timer(Tetris.TimeDelay, this.TimerListener);
        timer.start();
        this.addKeyListener(this.KeyListener);
        System.out.println("PLAYER MODE SET");
        if (IsAuto) {
            System.out.println("AUTO MODE SET");
        }
    }

    /**
     * 新的方块落下时的初始化
     */
    private void getNextBlock() {
        // 将已经生成好的下一次方块赋给当前方块
        this.NowBlockState = this.NextBlockState;
        this.NowBlockMap = this.NextBlockMap;
        // 再次生成下一次方块
        this.NextBlockState = this.CreateNewBlockState();
        this.NextBlockMap = this.getBlockMap(NextBlockState);
        // 计算方块位置
        this.NowBlockPos = this.CalNewBlockInitPos();
    }

    /**
     * 判断正在下落的方块和墙、已经固定的方块是否有接触
     * @return 下一时点是否会发生碰撞
     */
    boolean IsTouch(boolean[][] NextBlockMap, Point NextBlockPos) {
        for (int i = 0; i < NextBlockMap.length; i++) {
            for (int j = 0; j < NextBlockMap[i].length; j++) {
                if (NextBlockMap[i][j]) {
                    if (NextBlockPos.y + i >= Tetris.BlockHeight || NextBlockPos.x + j < 0 || NextBlockPos.x + j >= Tetris.BlockWidth) {
                        return true;
                    } else {
                        if (NextBlockPos.y + i >= 0) {
                            if (this.BlockMap[NextBlockPos.y + i][NextBlockPos.x + j]) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
    /**
     * 固定方块到地图
     */
    private boolean FixBlock() {
        for (int i = 0; i < this.NowBlockMap.length; i++) {
            for (int j = 0; j < this.NowBlockMap[i].length; j++) {
                if (this.NowBlockMap[i][j])
                    if (this.NowBlockPos.y + i < 0) return false;
                    else this.BlockMap[this.NowBlockPos.y + i][this.NowBlockPos.x + j] = this.NowBlockMap[i][j];
            }
        }
        return true;
    }
    /**
     * 计算新创建的方块的初始位置,方便修改界面的时候保持居中下落
     * @return 返回坐标
     */
    private Point CalNewBlockInitPos() {
        return new Point(Tetris.BlockWidth / 2 - this.NowBlockMap[0].length / 2, -this.NowBlockMap.length);
    }
    /**
     * 游戏的初始化，在切换模式和游戏开始时调用
     */
    public void Initial() {
        //清空Map
        for (boolean[] booleans : this.BlockMap) {
            Arrays.fill(booleans, false);
        }
        //清空分数
        this.Score = 0;
        // 初始化第一次生成的方块和下一次生成的方块
        this.NowBlockState = this.CreateNewBlockState();
        this.NowBlockMap = this.getBlockMap(this.NowBlockState);
        this.NextBlockState = this.CreateNewBlockState();
        this.NextBlockMap = this.getBlockMap(this.NextBlockState);
        // 计算方块位置
        this.NowBlockPos = this.CalNewBlockInitPos();
        this.repaint();
    }
    public void SetMode(boolean flag){
        this.IsAuto = flag;
        if (this.IsAuto) this.timer.start();
    }
    public void SetPause(boolean flag) {
        this.IsPause = flag;
        if (this.IsPause) {
            this.timer.stop();
        } else {
            this.timer.restart();
        }
        this.repaint();
    }
    /**
     * 随机生成新方块状态
     * @return 调用随机类用来生成新方块种类，并且最终能够输出到Terminal
     */
    private int CreateNewBlockState() {
        int Sum = Tetris.Shape.length * 4;
        return ThreadLocalRandom.current().nextInt(0, 1000) % Sum;
    }

    /**
     *
     * @param BlockState 为一随机int[0,28)
     * @return 二维的方块数组
     */
    private boolean[][] getBlockMap(int BlockState) {
        int Shape = BlockState / 4;
        int Arc = BlockState % 4;
        System.out.println(BlockState + "," + Shape + "," + Arc);
        return this.RotateBlock(Tetris.Shape[Shape], Arc);
    }

    /**
     * 几种方块的旋转生成方法
     * @return 旋转后的方块，用于初始化
     */
    boolean[][] RotateBlock(boolean[][] shape, int time) {
        if (time == 0) {
            return shape;
        }
        int height = shape.length;
        int width = shape[0].length;
        boolean[][] ResultMap = new boolean[height][width];
        int tmpH = height - 1, tmpW = 0;
        for (int i = 0; i < height && tmpW < width; i++) {
            for (int j = 0; j < width && tmpH > -1; j++) {
                ResultMap[i][j] = shape[tmpH][tmpW];
                tmpH--;
            }
            tmpH = height - 1;
            tmpW++;
        }
        for (int i = 1; i < time; i++) {
            ResultMap = RotateBlock(ResultMap, 0);
        }
        return ResultMap;
    }

    /**
     * 绘制游戏界面
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        for (int i = 0; i < Tetris.BlockHeight + 1; i++) {
            g.drawRect(0, i * Tetris.BlockSize, Tetris.BlockSize, Tetris.BlockSize);
            g.drawRect((Tetris.BlockWidth + 1) * Tetris.BlockSize, i * Tetris.BlockSize, Tetris.BlockSize,
                    Tetris.BlockSize);
        }
        for (int i = 0; i < Tetris.BlockWidth; i++) {
            g.drawRect((1 + i) * Tetris.BlockSize, Tetris.BlockHeight * Tetris.BlockSize, Tetris.BlockSize,
                    Tetris.BlockSize);
        }// 绘制墙体

        for (int i = 0; i < this.NowBlockMap.length; i++) {
            for (int j = 0; j < this.NowBlockMap[i].length; j++) {
                if (this.NowBlockMap[i][j])
                    g.fillRect((1 + this.NowBlockPos.x + j) * Tetris.BlockSize, (this.NowBlockPos.y + i) * Tetris.BlockSize,
                            Tetris.BlockSize, Tetris.BlockSize);
            }
        }// 绘制当前方块

        for (int i = 0; i < Tetris.BlockHeight; i++) {
            for (int j = 0; j < Tetris.BlockWidth; j++) {
                if (this.BlockMap[i][j])
                    g.fillRect(Tetris.BlockSize + j * Tetris.BlockSize, i * Tetris.BlockSize, Tetris.BlockSize,
                            Tetris.BlockSize);
            }
        }// 绘制已经固定好的方块

        for (int i = 0; i < this.NextBlockMap.length; i++) {
            for (int j = 0; j < this.NextBlockMap[i].length; j++) {
                if (this.NextBlockMap[i][j])
                    g.fillRect(440 + j * Tetris.BlockSize, 80 + i * Tetris.BlockSize, Tetris.BlockSize, Tetris.BlockSize);
            }
        }//绘制下一个方块

        g.drawString("游戏分数:" + this.Score, 365, 20);
        g.drawString("下一个方块:", 365, 35);
        for (int i = 0; i < Tetris.AuthorInfo.length; i++) {
            g.drawString(Tetris.AuthorInfo[i], 380, 200 + i * 20);
        }// 绘制分数信息、作者介绍信息，中间空出高度放置下一个方块的大小

        if (this.IsPause) {
            g.setColor(Color.white);
            g.fillRect(165, 200, 50, 20);
            g.setColor(Color.black);
            g.drawRect(165, 200, 50, 20);
            g.drawString("PAUSE", 170, 216);
        }//绘制暂停
    }

    /**
     * 清除成行的方块并累计行数
     * @return 清除的行数
     */
    private int ClearLines() {
        int lines = 0;
        for (int i = 0; i < this.BlockMap.length; i++) {
            boolean IsLine = true;
            for (int j = 0; j < this.BlockMap[i].length; j++) {
                if (!this.BlockMap[i][j]) {
                    IsLine = false;
                    break;
                }
            }
            if (IsLine) {
                System.arraycopy(this.BlockMap, 0, this.BlockMap, 1, i);
                this.BlockMap[0] = new boolean[Tetris.BlockWidth];
                lines++;
            }
        }
        return lines;
    }
}