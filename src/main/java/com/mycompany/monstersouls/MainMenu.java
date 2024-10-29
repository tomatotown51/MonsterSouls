/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.monstersouls;

import java.awt.BorderLayout;
import java.sql.SQLException;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 *
 * @author Thomas PC
 */
public class MainMenu extends JPanel {
    
    //VARIABLES
    private JTextField usernameField;
    private JButton startButton;
    private DatabaseManager dbManager;

    public MainMenu(DatabaseManager dbManager) {
        this.dbManager = dbManager;
        setLayout(new BorderLayout());

        //USERNAME INPUT & PANEL CONTROLS
        usernameField = new JTextField(15);
        startButton = new JButton("Start");
        JButton exitButton = new JButton("Exit");
        JPanel panel = new JPanel();
        panel.add(new JLabel("Enter Username:"));
        panel.add(usernameField);
        panel.add(startButton);
        panel.add(exitButton);

        add(panel, BorderLayout.CENTER);

        //BUTTON LISTENERS
        startButton.addActionListener(e -> handleStartButton());
        exitButton.addActionListener(e -> System.exit(0));
    }

    private void handleStartButton() { //HANDLING START BUTTON
        String username = usernameField.getText().trim();
        

        //INPUT ERROR HANDLING
        
        //CHECKING FOR EMPTY USERNAME
        if (username.isEmpty()) { 
            JOptionPane.showMessageDialog(this, "Please enter a username.");
        }
        //CHECKING FOR INVALID LENGTH
        else if (username.length() < 3 || username.length() > 15) {
            JOptionPane.showMessageDialog(this, "Username must be between 3 and 15 characters.");
        }
        //CHECKING FOR INVALID CHARACTERS
        else if (!username.matches("[a-zA-Z0-9_]+")) { 
            JOptionPane.showMessageDialog(this, "Username must only contain letters, numbers, and underscores.");
        }
        else {
            try {
                
                Player player = dbManager.checkAndLoadPlayer(username); //CHECKING IF USERNAME EXISTS AND LOADING
                ((GameWindow) SwingUtilities.getWindowAncestor(this)).switchToGamePanel(player);
            }
            catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error accessing the database.");
            }
        }
    }

}
