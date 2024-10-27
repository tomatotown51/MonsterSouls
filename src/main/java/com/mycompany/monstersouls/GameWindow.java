package com.mycompany.monstersouls;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Dimension;

/**
 *
 * @author Thomas PC
 */
public class GameWindow extends JFrame {
    
    public GameWindow() {
        setTitle("MonsterSouls");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        
        GamePanel gamePanel = new GamePanel();
        add(gamePanel);
        
        pack();
        setLocationRelativeTo(null);
        setVisible(true);    
    }
    
    public static void main(String[] args) {
        new GameWindow();
    }
    
}