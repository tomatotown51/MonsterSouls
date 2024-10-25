package com.mycompany.monstersouls;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;

public class GamePanel extends JPanel implements Runnable, KeyListener {
    private Player player;
    private Image backgroundImage;
    private int bgWidth, bgHeight;
    private int leftBoundary = 50;
    private int rightBoundary = 750;
    private int topBoundary = 50;
    private int bottomBoundary = 570;
    private int boundaryLineThickness = 5;
    private ArrayList<Enemy> enemies;

    public GamePanel() {
        setPreferredSize(new Dimension(800, 720));
        setFocusable(true);
        requestFocus();
        addKeyListener(this);

        player = new Player(400, 300);
        enemies = new ArrayList<>();
        enemies.add(new Enemy(200, 200, 100, 1, "resources/skeleton.png"));
        enemies.add(new Enemy(300, 300, 120, 1, "resources/zombie.png"));

        try {
            backgroundImage = ImageIO.read(new File("resources/bg.jpg"));
            bgWidth = backgroundImage.getWidth(null);
            bgHeight = backgroundImage.getHeight(null);
            System.out.println("Image loaded successfully");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Image failed to load");
        }

        Thread gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                g2d.drawImage(backgroundImage, col * bgWidth, row * bgHeight, null);
            }
        }

        drawBoundary(g2d);

        player.draw(g);

        for (Enemy enemy : enemies) {
            enemy.draw(g);
        }
    }

    private void drawBoundary(Graphics2D g2d) {
        g2d.setColor(Color.BLACK);
        g2d.fillRect(leftBoundary - boundaryLineThickness, topBoundary, boundaryLineThickness, bottomBoundary - topBoundary);
        g2d.fillRect(rightBoundary, topBoundary, boundaryLineThickness, bottomBoundary - topBoundary);
        g2d.fillRect(leftBoundary, topBoundary - boundaryLineThickness, rightBoundary - leftBoundary, boundaryLineThickness);
        g2d.fillRect(leftBoundary, bottomBoundary, rightBoundary - leftBoundary, boundaryLineThickness);
    }

    @Override
    public void run() {
        while (true) {
            player.update(leftBoundary, rightBoundary, topBoundary, bottomBoundary);

            for (Enemy enemy : enemies) {
                enemy.update(player);

                if (checkCollision(player, enemy)) {
                    player.takeDamage(10);
                    System.out.println("Player hit by enemy! Remaining health: " + player.getHealth());
                }

                if (player.isAttacking() && checkAttackRange(player, enemy)) {
                    int damage = 0;

                    // Determine attack type and apply damage
                    if (player.isJab()) {
                        damage = 20;
                        System.out.println("Player used Jab!");
                    } else if (player.isSwipe()) {
                        damage = 10;
                        System.out.println("Player used Swipe!");
                    }

                    enemy.takeDamage(damage);
                    System.out.println("Enemy hit by player! Remaining health: " + enemy.getHealth());
                }
            }

            repaint();

            try {
                Thread.sleep(16);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean checkCollision(Player player, Enemy enemy) {
        return Math.abs(player.getX() - enemy.getX()) < 30 && Math.abs(player.getY() - enemy.getY()) < 30;
    }

    private boolean checkAttackRange(Player player, Enemy enemy) {
        return Math.abs(player.getX() - enemy.getX()) < 50 && Math.abs(player.getY() - enemy.getY()) < 50;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        player.handleKeyPress(e);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        player.handleKeyRelease(e);
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Not used
    }
}
