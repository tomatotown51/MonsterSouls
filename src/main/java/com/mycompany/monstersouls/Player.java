/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.monstersouls;

/**
 *
 * @author Thomas PC
 */
import java.awt.Graphics;
import java.awt.Color;
import java.awt.event.KeyEvent;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;

public class Player {
    private int x, y; // Player's position in the game world
    private int speed = 5; // Movement speed

    // Movement flags
    private boolean up, down, left, right;

    public Player(int startX, int startY) {
        this.x = startX;
        this.y = startY;
    }

    public void update() {
        // Update player's position based on key input
        if (up && !down) y -= speed;
        if (down && !up) y += speed;
        if (left && !right) x -= speed;
        if (right && !left) x += speed;
    }

    public void draw(Graphics g) {
        // Draw the player at the center of the screen
        g.setColor(Color.RED);
        g.fillRect(400 - 25, 300 - 25, 50, 50); // Centered 50x50 square
    }

    // Handle key presses
    public void handleKeyPress(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_W || key == KeyEvent.VK_UP) {
            up = true;
        }
        if (key == KeyEvent.VK_S || key == KeyEvent.VK_DOWN) {
            down = true;
        }
        if (key == KeyEvent.VK_A || key == KeyEvent.VK_LEFT) {
            left = true;
        }
        if (key == KeyEvent.VK_D || key == KeyEvent.VK_RIGHT) {
            right = true;
        }
    }

    // Handle key releases
    public void handleKeyRelease(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_W || key == KeyEvent.VK_UP) {
            up = false;
        }
        if (key == KeyEvent.VK_S || key == KeyEvent.VK_DOWN) {
            down = false;
        }
        if (key == KeyEvent.VK_A || key == KeyEvent.VK_LEFT) {
            left = false;
        }
        if (key == KeyEvent.VK_D || key == KeyEvent.VK_RIGHT) {
            right = false;
        }
    }

    // Getter for player's x-coordinate
    public int getX() {
        return x;
    }

    // Getter for player's y-coordinate
    public int getY() {
        return y;
    }
}

