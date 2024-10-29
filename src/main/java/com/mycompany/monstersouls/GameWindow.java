package com.mycompany.monstersouls;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Dimension;
import java.sql.SQLException;
import javax.swing.JOptionPane;

/**
 *
 * @author Thomas PC
 */
public class GameWindow extends JFrame {
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
            System.exit(1); // Exit if database connection fails
        }

        add(mainMenu);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void switchToGamePanel(Player player) {
    remove(mainMenu);                // Remove the main menu panel
    gamePanel = new GamePanel();     // Initialize GamePanel using the default constructor
    gamePanel.setPlayer(player);     // Set the player
    gamePanel.setDatabaseManager(dbManager); // Set the DatabaseManager
    
    add(gamePanel);                  // Add GamePanel to the frame
    gamePanel.setFocusable(true);    // Ensure GamePanel can capture key events
    gamePanel.requestFocusInWindow(); // Request focus to ensure it captures key events

    pack();                          // Resize the window to fit the new panel's preferred size
    revalidate();                    // Refresh layout
    repaint();                       // Redraw frame
}


}

