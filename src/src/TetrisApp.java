package src;

import javax.swing.*;
import java.awt.event.ActionListener;

public class TetrisApp extends JFrame {
    boolean aiMode = false;
    Tetris tetris = new Tetris();
    //TODO 生成AI模式对象

    public TetrisApp() {
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(600, 600);
        this.setTitle("Tetris");
        this.setResizable(false);

        JMenuBar menu = new JMenuBar();
        this.setJMenuBar(menu);
        JMenu gameMenu = new JMenu("游戏");
        JMenuItem newGameItem = gameMenu.add("新游戏");
        newGameItem.addActionListener(this.NewGameAction);
        JMenuItem pauseItem = gameMenu.add("暂停");
        pauseItem.addActionListener(this.PauseAction);
        JMenuItem continueItem = gameMenu.add("继续");
        continueItem.addActionListener(this.ContinueAction);
        JMenuItem exitItem = gameMenu.add("退出");
        exitItem.addActionListener(this.ExitAction);
        JMenu modeMenu = new JMenu("模式");
        JMenuItem v4Item = modeMenu.add("正常");
        v4Item.addActionListener(this.v4Action);
        JMenuItem AutoItem = modeMenu.add("Auto");
        AutoItem.addActionListener(this.AIAction);
        JMenu helpMenu = new JMenu("帮助");
        JMenuItem aboutItem = helpMenu.add("关于");
        aboutItem.addActionListener(this.AboutAction);
        menu.add(gameMenu);
        menu.add(modeMenu);
        menu.add(helpMenu);
        if (aiMode){
            //TODO 让aiMode运行
//            this.add(this.ai);
//            this.ai.setFocusable(true);
//            this.ai.AIPlayer();
        }
        else {
            this.add(this.tetris);
            this.tetris.setFocusable(true);
        }
    }

    static public void main(String... args) {
        TetrisApp tetrisApp = new TetrisApp();
        tetrisApp.setVisible(true);
        System.out.println("***********");
    }

    ActionListener NewGameAction = e -> TetrisApp.this.tetris.Initial();

    ActionListener PauseAction = e -> TetrisApp.this.tetris.SetPause(true);

    ActionListener ContinueAction = e -> TetrisApp.this.tetris.SetPause(false);

    ActionListener ExitAction = e -> System.exit(0);

    ActionListener AboutAction = e -> JOptionPane.showMessageDialog(TetrisApp.this, "Tetris Remake Ver 0.1.0", "关于", JOptionPane.WARNING_MESSAGE);

    ActionListener v4Action = e -> aiMode = false;

    ActionListener AIAction = e -> aiMode = true;
}
