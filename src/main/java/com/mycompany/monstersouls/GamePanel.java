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
import java.util.Random;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

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
    private long lastEnemySpawnTime = 0;
    private static final long ENEMY_SPAWN_DELAY = 10000; //10 SECONDS
    private int score = 0;
    private Rectangle resetButtonBounds = new Rectangle(290, 350, 120, 50);
    private int initialX = 400;
    private int initialY = 300;
    private boolean isGameOver = false;
    private boolean isPaused = false; // New variable to track pause state

    public GamePanel() {
        setPreferredSize(new Dimension(800, 720));
        setFocusable(true);
        requestFocus();
        addKeyListener(this);
        
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (player.isDead() && resetButtonBounds.contains(e.getPoint())) {
                    resetGame(); 
                }
            }
        });

        player = new Player(initialX, initialY);
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

    @Override //PAINT COMPONENT
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        if (player.isDead()) {
            displayGameOver(g);
            drawResetButton(g);
            return;
        }

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
        
        drawHP(g);
        drawScore(g);
        drawControls(g);

        if (isPaused) { // Display PAUSED message if the game is paused
            g.setFont(new Font("Arial", Font.BOLD, 60));
            g.setColor(Color.RED);
            g.drawString("PAUSED", getWidth() / 2 - 100, getHeight() / 2);
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
            if (!isGameOver && !isPaused) { // Skip updates if paused or game over
                long currentTime = System.currentTimeMillis();

                player.update(leftBoundary, rightBoundary, topBoundary, bottomBoundary);

                if (currentTime - lastEnemySpawnTime >= ENEMY_SPAWN_DELAY) {
                    spawnEnemy();
                    lastEnemySpawnTime = currentTime;
                }

                ArrayList<Enemy> enemiesToRemove = new ArrayList<>();
                for (Enemy enemy : enemies) {
                    enemy.update(player);

                    if (enemy.isDead()) {
                        enemiesToRemove.add(enemy);
                        score++;
                    } else if (checkCollision(player, enemy) && enemy.canAttack()) {
                        player.takeDamage(10);
                        System.out.println("Player hit by enemy! Remaining health: " + player.getHealth());
                    }

                    if (player.isJabbing() && checkAttackRange(player, enemy) && enemy.canBeHit()) {
                        int damage = 20;
                        enemy.takeDamage(damage);
                        System.out.println("Enemy hit by player jab! Remaining health: " + enemy.getHealth());
                    } else if (player.isSwiping() && checkAttackRange(player, enemy) && enemy.canBeHit()) {
                        int damage = 10;
                        enemy.takeDamage(damage);
                        System.out.println("Enemy hit by player swipe! Remaining health: " + enemy.getHealth());
                    }
                }
                enemies.removeAll(enemiesToRemove);

                repaint();
            }

            try {
                Thread.sleep(16); //UPDATING EVERY FRAME
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
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE && !isGameOver) { // Toggle pause with ESC if game not over
            isPaused = !isPaused;
            repaint();
        } else {
            player.handleKeyPress(e);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        player.handleKeyRelease(e);
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Not used
    }
    
    public void spawnEnemy() {
        Random rand = new Random();
        int x = rand.nextInt(rightBoundary - leftBoundary) + leftBoundary;
        int y = rand.nextInt(bottomBoundary - topBoundary) + topBoundary;
        enemies.add(new Enemy(x, y, 100, 1,"resources/goblin.png"));
    }
    
    private void drawHP(Graphics g) {
        g.setColor(Color.RED);
        g.fillRect(leftBoundary, bottomBoundary + 10, player.getHealth() * 2, 20);

        g.setColor(Color.BLACK);
        g.drawRect(leftBoundary, bottomBoundary + 10, 200, 20);  //HP BAR
        g.drawString("HP: " + player.getHealth(), leftBoundary + 5, bottomBoundary + 25);
    }
    
    private void drawScore(Graphics g) {
        g.setColor(Color.YELLOW);
        g.setFont(new Font("Arial", Font.PLAIN, 16)); 
        g.drawString("Score: " + score, leftBoundary, bottomBoundary + 55);
    }
    
    private void displayGameOver(Graphics g) {
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 40));
        g.drawString("Game Over", 250, 300);
        g.drawString("SCORE:" + score, 250, 350);
    }

    private void drawResetButton(Graphics g) {
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(resetButtonBounds.x, resetButtonBounds.y, resetButtonBounds.width, resetButtonBounds.height);
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("RESTART", resetButtonBounds.x + 20, resetButtonBounds.y + 30);
    }
    
    public void resetGame() {
        player.setHealth(100);
        player.setPosition(initialX, initialY);

        isGameOver = false;
        score = 0;

        enemies.clear();
        enemies.add(new Enemy(200, 200, 100, 1, "resources/skeleton.png"));
        enemies.add(new Enemy(300, 300, 120, 1, "resources/zombie.png"));
    
        lastEnemySpawnTime = System.currentTimeMillis();

        repaint();
    }

    public void checkPlayerDeath() {
        if (player.isDead()) {
            isGameOver = true;
        }
    }
    
    private void drawControls(Graphics g) {
        g.setFont(new Font("SansSerif", Font.PLAIN, 14));
        g.setColor(Color.YELLOW);
        int startX = 20;
        int startY = getHeight() - 60;

        g.drawString("Controls:", startX + 550, startY - 65);
        g.drawString("WASD/ARROW KEYS = MOVE", startX + 550, startY - 45);
        g.drawString("O = JAB ATTACK", startX + 550, startY - 25);
        g.drawString("P = SWIPE ATTACK", startX + 550, startY - 5);
        g.drawString("SPACE = ROLL", startX + 550, startY + 15);
        g.drawString("ESC = PAUSE", startX + 550, startY + 35); // Added pause control
    }
}
