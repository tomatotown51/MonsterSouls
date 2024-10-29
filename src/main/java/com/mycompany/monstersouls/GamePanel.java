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
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

public class GamePanel extends JPanel implements Runnable, KeyListener {
    private Player player;
    private Image backgroundImage;
    private int bgWidth, bgHeight;
    private static int leftBoundary = 50;
    private static int rightBoundary = 750;
    private static int topBoundary = 50;
    private static int bottomBoundary = 570;
    private int boundaryLineThickness = 5;
    private ArrayList<Enemy> enemies;
    private long lastEnemySpawnTime = 0;
    private static final long ENEMY_SPAWN_DELAY = 10000; // 10 SECONDS
    private int score = 0;
    private Rectangle resetButtonBounds = new Rectangle(290, 350, 120, 50);
    private int initialX = 400;
    private int initialY = 300;
    private boolean isGameOver = false;
    private boolean isPaused = false; // New variable to track pause state
    private DatabaseManager dbManager;
    private Connection connection;
    private int enemyID = 1;
    private int highScore = 0;


    public GamePanel() {
        player = new Player(initialX, initialY, 100); //int int int
        dbManager = new DatabaseManager(connection);
        
        try { //CONNECT TO DATABASE
            connection = DatabaseManager.createConnection();
            dbManager = new DatabaseManager(connection);
            System.out.println("Connected successfully to Database.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        try { //LOAD HIGHSCORE 
            highScore = dbManager.loadHighScore(player.getUsername());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        setPreferredSize(new Dimension(800, 720)); //WINDOW SIZEsetP
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

        enemies = new ArrayList<>();
        Random rand = new Random();
        int initialEnemies = 3;
        
        for (int i = 0; i<initialEnemies; i++) { 
            int randomX = rand.nextInt(rightBoundary - leftBoundary) + leftBoundary; //GENERATING RANDOM COORDS
            int randomY = rand.nextInt(bottomBoundary - topBoundary) + topBoundary;
            int enemyType = rand.nextInt(2); //GENERATING RANDOM ENEMY TYPE
            switch (enemyType) { //ENEMY(X, Y, HEALTH, SPEED, SPRITEPATH, ENEMYID++ (FOR SCORE)
            case 0: 
                enemies.add(new Enemy(randomX, randomY, 75, 1,"resources/skeleton.png", enemyID++));//SKELETON
                break;
            case 1:
                enemies.add(new Enemy(randomX, randomY, 100, 1,"resources/zombie.png", enemyID++));//ZOMBIE
                break;
            case 2:
                enemies.add(new Enemy(randomX, randomY, 50, 2,"resources/goblin.png", enemyID++)); //GOBLIN
                break;
            default:
                throw new IllegalArgumentException("Unexpected random number when generating initial enemy: " + enemyType);
            }
        }
        
        
        
        
        
        
           

        try {
            backgroundImage = ImageIO.read(new File("resources/bg.jpg")); //LOADING BACKGROUND
            bgWidth = backgroundImage.getWidth(null);
            bgHeight = backgroundImage.getHeight(null);
            System.out.println("Image loaded successfully"); //DEBUGGING
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Image failed to load");
        }

        Thread gameThread = new Thread(this); //NEW THREAD
        gameThread.start();
    }

    @Override // PAINT COMPONENT
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

        if (isPaused) { //DISPLAY PAUSED TEXT
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
    List<Obstacle> obstacles = new ArrayList<>(); // Initialize this with your game's obstacles

    while (true) {
        if (!isGameOver && !isPaused) {
            long currentTime = System.currentTimeMillis();

            player.update(leftBoundary, rightBoundary, topBoundary, bottomBoundary);

            if (currentTime - lastEnemySpawnTime >= ENEMY_SPAWN_DELAY) {
                spawnEnemy();
                lastEnemySpawnTime = currentTime;
            }

            ArrayList<Enemy> enemiesToRemove = new ArrayList<>();
            for (Enemy enemy : enemies) {
                enemy.update(player, obstacles); // Updated to include obstacles

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
            checkPlayerDeath();
            repaint();
        }
        
        try {
            Thread.sleep(16); // UPDATING EVERY FRAME
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
        dbManager = new DatabaseManager(connection);
       
if (e.getKeyCode() == KeyEvent.VK_ESCAPE && !isGameOver) { //PAUSE
    isPaused = !isPaused;
    if (isPaused) {
        try {
            dbManager.savePlayerData(player);    // SAVING PLAYER DATA EVERY PAUSE
            dbManager.backupEnemies(enemies);    // SAVING ENEMY DATA EVERY PAUSE
            dbManager.saveHighScore(player.getUsername(), score);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
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
    }

    public void spawnEnemy() { //SPWANING ENEMIES RANDOMLY
        Random rand = new Random();
        int x = rand.nextInt(rightBoundary - leftBoundary) + leftBoundary; //SPAWN RANGE
        int y = rand.nextInt(bottomBoundary - topBoundary) + topBoundary;
        int randomNumber = rand.nextInt(2);
        
        switch (randomNumber) {
            case 0: //X, Y, HEALTH, SPEED, SPRITEPATH, ENEMYID++ (SCORE)
                enemies.add(new Enemy(x, y, 50, 3,"resources/goblin.png", enemyID++)); //GOBLIN
                break;
            case 1:
                enemies.add(new Enemy(x, y, 75, 2,"resources/skeleton.png", enemyID++));//SKELETON
                break;
            case 2:
                enemies.add(new Enemy(x, y, 100, 1,"resources/zombie.png", enemyID++));//ZOMBIE
                break;
            default:
                throw new IllegalArgumentException("Unexpected random number when generating enemy: " + randomNumber);
        }
    }

    private void drawHP(Graphics g) { //DRAW HP
        g.setColor(Color.RED);
        g.fillRect(leftBoundary, bottomBoundary + 10, player.getHealth() * 2, 20);

        g.setColor(Color.BLACK);
        g.drawRect(leftBoundary, bottomBoundary + 10, 200, 20);  // HP BAR
        g.drawString("HP: " + player.getHealth(), leftBoundary + 5, bottomBoundary + 25);
    }

    private void drawScore(Graphics g) {
        g.setColor(Color.YELLOW);
        g.setFont(new Font("Arial", Font.PLAIN, 16)); 
        g.drawString("Score: " + score, leftBoundary, bottomBoundary + 55);
        g.drawString("High Score: " + highScore, leftBoundary, bottomBoundary + 75); // Display high score below
    }

    private void displayGameOver(Graphics g) {
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 40));
        g.drawString("Game Over", 250, 300);
        g.drawString("SCORE: " + score, 250, 350);
        g.drawString("HIGH SCORE: " + highScore, 250, 400); // Add high score
    }

    private void drawResetButton(Graphics g) { // Restart button
    resetButtonBounds.setLocation(250, 420); // Position reset button below high score
    g.setColor(Color.LIGHT_GRAY);
    g.fillRect(resetButtonBounds.x, resetButtonBounds.y, resetButtonBounds.width, resetButtonBounds.height);
    g.setColor(Color.BLACK);
    g.setFont(new Font("Arial", Font.BOLD, 20));
    g.drawString("RESTART", resetButtonBounds.x + 20, resetButtonBounds.y + 30);
}

    public void resetGame() { //RESET GAME
        try {
            dbManager.savePlayerData(player); //SAVE PLAYER DATA BEFORE RESET
        } catch (SQLException ex) {
            ex.printStackTrace(); //ERROR PRINTING
        }
        player.setHealth(100);
        player.setPosition(initialX, initialY);

        isGameOver = false;
        score = 0;

        enemies.clear();
        enemyID = 1;
        enemies = new ArrayList<>();
        Random rand = new Random();
        int initialEnemies = 3;
        for (int i = 0; i<initialEnemies; i++) { 
            int randomX = rand.nextInt(rightBoundary - leftBoundary) + leftBoundary; //GENERATING RANDOM COORDS
            int randomY = rand.nextInt(bottomBoundary - topBoundary) + topBoundary;
            int enemyType = rand.nextInt(2); //GENERATING RANDOM ENEMY TYPE
            switch (enemyType) { //ENEMY(X, Y, HEALTH, SPEED, SPRITEPATH, ENEMYID++ (FOR SCORE)
            case 0: 
                enemies.add(new Enemy(randomX, randomY, 75, 1,"resources/skeleton.png", enemyID++));//SKELETON
                break;
            case 1:
                enemies.add(new Enemy(randomX, randomY, 100, 1,"resources/zombie.png", enemyID++));//ZOMBIE
                break;
            case 2:
                enemies.add(new Enemy(randomX, randomY, 50, 2,"resources/goblin.png", enemyID++)); //GOBLIN
                break;
            default:
                throw new IllegalArgumentException("Unexpected random number when generating initial enemy: " + enemyType);
            }
        }

        try {
            highScore = dbManager.loadHighScore(player.getUsername()); // Reload high score on reset
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        lastEnemySpawnTime = System.currentTimeMillis();

        repaint();
    }

    public void checkPlayerDeath() {
    if (player.isDead()) {
        isGameOver = true;
        
        try {
            dbManager.savePlayerData(player); // Save player data
            // Save high score if the current score is a new record
            if (score > highScore) {
                dbManager.saveHighScore(player.getUsername(), score);
                highScore = score; // Update local high score variable
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}


    private void drawControls(Graphics g) { //DRAWING CONTROLS
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

    public void closeConnection() { //CLSOE CONNECTION
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void backupEnemies() { //BACKUP ENEMIES
        try {
            dbManager.backupEnemies(enemies);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    
    public static int getLeftBoundary() { return leftBoundary; }
    public static int getRightBoundary() { return rightBoundary; }
    public static int getTopBoundary() { return topBoundary; }
    public static int getBottomBoundary() { return bottomBoundary; }
    public void setPlayer(Player player) {
        this.player = player;
    }
    public void setDatabaseManager(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

}
