package src;

import javax.swing.*;
import java.awt.event.ActionListener;

public class TetrisApp extends JFrame {
    Tetris tetris = new Tetris();

    ActionListener NewGameAction = e -> TetrisApp.this.tetris.Initial();
    ActionListener PauseAction = e -> TetrisApp.this.tetris.SetPause(true);
    ActionListener ContinueAction = e -> TetrisApp.this.tetris.SetPause(false);
    ActionListener ExitAction = e -> System.exit(0);
    ActionListener AboutAction = e -> JOptionPane.showMessageDialog(TetrisApp.this, "Tetris Remake Ver 0.1.2", "关于", JOptionPane.WARNING_MESSAGE);
    ActionListener NormalAction = e -> TetrisApp.this.tetris.SetMode(false);
    ActionListener AIAction = e -> TetrisApp.this.tetris.SetMode(false);//未完成，避免操作产生bug始终设置为false

    public TetrisApp() {

        this.add(this.tetris);
        this.tetris.setFocusable(true);

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
        v4Item.addActionListener(this.NormalAction);
        JMenuItem AutoItem = modeMenu.add("Auto");
        AutoItem.addActionListener(this.AIAction);
        JMenu helpMenu = new JMenu("帮助");
        JMenuItem aboutItem = helpMenu.add("关于");
        aboutItem.addActionListener(this.AboutAction);
        menu.add(gameMenu);
        menu.add(modeMenu);
        menu.add(helpMenu);
    }

    static public void main(String... args) {
        TetrisApp tetrisApp = new TetrisApp();
        tetrisApp.setVisible(true);
        System.out.println("***********");
        tetrisApp.tetris.SetPause(true);
    }
}
