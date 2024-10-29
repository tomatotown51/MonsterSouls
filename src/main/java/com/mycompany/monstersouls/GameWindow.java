package com.mycompany.monstersouls;

//IMPORTS
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Dimension;
import java.sql.SQLException;
import javax.swing.JOptionPane;


public class GameWindow extends JFrame {
    
    //VARIABLES
    private MainMenu mainMenu;
    private GamePanel gamePanel;
    private DatabaseManager dbManager;

    public GameWindow() {
        setTitle("MonsterSouls");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        try {
            dbManager = new DatabaseManager(DatabaseManager.createConnection());
            mainMenu = new MainMenu(dbManager);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to connect to the database.");
            System.exit(1); //CLOSE IF DATABASE FAILS
        }

        add(mainMenu); //ADDING MAIN MANU TO FRAME
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        
    }
    
    //SWITCHING TO ACTUAL GAME
    public void switchToGamePanel(Player player) {
        System.out.println("Switching to GamePanel...");
        
        remove(mainMenu);//GETTING RID OF MAIN MENU FROM FRAME

        gamePanel = new GamePanel();//NEW GAMEPANEL
    
        //SETTING PLAYER AND DATABASE
        gamePanel.setPlayer(player);     
        gamePanel.setDatabaseManager(dbManager); 
    
        //ADDING GAMEPANEL TO FRAME
        add(gamePanel);
        revalidate();
        pack();
        repaint();
        gamePanel.setFocusable(true);
        gamePanel.requestFocusInWindow();

        
        //RESIZE WINDOW
        
        
        System.out.println("GamePanel switch complete.");
    }
}

