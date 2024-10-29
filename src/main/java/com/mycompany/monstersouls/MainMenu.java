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
    private JTextField usernameField;
    private JButton startButton;
    private DatabaseManager dbManager;

    public MainMenu(DatabaseManager dbManager) {
        this.dbManager = dbManager;
        setLayout(new BorderLayout());

        // Username input and button setup
        usernameField = new JTextField(15);
        startButton = new JButton("Start");
        JButton exitButton = new JButton("Exit");

        // Add components to the panel
        JPanel panel = new JPanel();
        panel.add(new JLabel("Enter Username:"));
        panel.add(usernameField);
        panel.add(startButton);
        panel.add(exitButton);

        add(panel, BorderLayout.CENTER);

        // Button listeners
        startButton.addActionListener(e -> handleStartButton());
        exitButton.addActionListener(e -> System.exit(0));
    }

    private void handleStartButton() {
    String username = usernameField.getText().trim();
    if (!username.isEmpty()) {
        try {
            Player player = dbManager.checkAndLoadPlayer(username);
            ((GameWindow) SwingUtilities.getWindowAncestor(this)).switchToGamePanel(player);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error accessing the database.");
        }
    } else {
        JOptionPane.showMessageDialog(this, "Please enter a username.");
    }
}
}
