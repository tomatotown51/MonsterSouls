package com.mycompany.monstersouls;

//IMPORTS
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
import java.util.List;

public class GamePanel extends JPanel implements Runnable, KeyListener { //GAME PANEL & GAME LOGIC
    
    //GAME VARIABLES
    private int score = 0;
    private Rectangle resetButtonBounds = new Rectangle(290, 350, 120, 50);   
    private boolean isGameOver = false;
    private boolean isPaused = false;
    private int enemyID = 1;
    private int highScore = 0;
    
    //PLAYER VARIABLES
    private Player player;
    private int initialX = 400;
    private int initialY = 300;
    
    //ENEMY VARIABLES
    private ArrayList<Enemy> enemies;
    private long lastEnemySpawnTime = 0;
    private static final long ENEMY_SPAWN_DELAY = 10000; // 10 SECONDS
    
    //BOUNDARY VARIABLES
    private static int leftBoundary = 50;
    private static int rightBoundary = 750;
    private static int topBoundary = 50;
    private static int bottomBoundary = 570;
    private int boundaryLineThickness = 5;

    //DATABASE VARIABLES
    private DatabaseManager dbManager;
    private Connection connection;
    
    //BACKGROUND VARIABLES
    private Image backgroundImage;
    private int bgWidth, bgHeight;


    public GamePanel() { //GAMEPANEL METHOD
        player = new Player(initialX, initialY, 100); //NEW PLAYER (INT, INT, INT)
        dbManager = new DatabaseManager(connection); //NEW DB CONNECTION
        
        try { //TRY CONNECT TO DATABASE
            connection = DatabaseManager.createConnection();
            dbManager = new DatabaseManager(connection);
            System.out.println("Connected successfully to Database.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        try { //TRY LOAD HIGHSCORE 
            highScore = dbManager.loadHighScore(player.getUsername());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        //WINDOW PROPERTIES
        setPreferredSize(new Dimension(800, 720)); //WINDOW SIZE
        setFocusable(true);
        requestFocus();
        addKeyListener(this);

        addMouseListener(new MouseAdapter() { //MOUSE LISTENER
            @Override
            public void mouseClicked(MouseEvent e) {
                if (player.isDead() && resetButtonBounds.contains(e.getPoint())) {
                    resetGame(); 
                }
            }
        });
        
        //INITIAL ENEMY SPAWNING
        enemies = new ArrayList<>();
        Random rand = new Random();
        int initialEnemies = 3;
        
        //GENERATING RANDOM ENEMT TYPE IN RANDOM COORDINATES WITHIN BOUNDARIES
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
                throw new IllegalArgumentException("Unexpected random number when generating initial enemy: " + enemyType); //ERROR HANDLING
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
    
    
    // PAINT COMPONENT
    @Override 
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        if (player.isDead()) { //DRAWING GAMEOVER SCREEN & RESET BUTTON
            displayGameOver(g);
            drawResetButton(g);
            return;
        }

        for (int row = 0; row < 3; row++) { //DRAWING BACKGROUND IN GRID
            for (int col = 0; col < 3; col++) {
                g2d.drawImage(backgroundImage, col * bgWidth, row * bgHeight, null);
            }
        }
        
        //DRAWING MAP BOUNDARY
        drawBoundary(g2d);
        
        //DRAWING PLAYER
        player.draw(g);

        //DRAWING EACH ENEMY
        for (Enemy enemy : enemies) {
            enemy.draw(g);
        }
        
        //DRAWING HUD
        drawHP(g);
        drawScore(g);
        drawControls(g);

        if (isPaused) { //DISPLAY PAUSED TEXT
            g.setFont(new Font("Arial", Font.BOLD, 60));
            g.setColor(Color.RED);
            g.drawString("PAUSED", getWidth() / 2 - 100, getHeight() / 2);
        }
    }
    
    //DRAWING BOUNDARY
    private void drawBoundary(Graphics2D g2d) {
        g2d.setColor(Color.BLACK);
        g2d.fillRect(leftBoundary - boundaryLineThickness, topBoundary, boundaryLineThickness, bottomBoundary - topBoundary);
        g2d.fillRect(rightBoundary, topBoundary, boundaryLineThickness, bottomBoundary - topBoundary);
        g2d.fillRect(leftBoundary, topBoundary - boundaryLineThickness, rightBoundary - leftBoundary, boundaryLineThickness);
        g2d.fillRect(leftBoundary, bottomBoundary, rightBoundary - leftBoundary, boundaryLineThickness);
    }

    //RUN METHOD
    @Override
    public void run() {
        List<Obstacle> obstacles = new ArrayList<>(); //OBSTACLES LIST

        while (true) {
            if (!isGameOver && !isPaused) { //WHILE PLAYER IS ALIVE AND GAME ISNT PAUSED
                
                long currentTime = System.currentTimeMillis();
                player.update(leftBoundary, rightBoundary, topBoundary, bottomBoundary); //UPDATING PLAYER WITHIN BOUNDARIES

                if (currentTime - lastEnemySpawnTime >= ENEMY_SPAWN_DELAY) { //ENEMY SPAWN DELAY
                    spawnEnemy();
                    lastEnemySpawnTime = currentTime;
                }

                ArrayList<Enemy> enemiesToRemove = new ArrayList<>(); //CREATING LIST OF ENEMIES TO DELETE
                for (Enemy enemy : enemies) {
                    enemy.update(player, obstacles);

                    if (enemy.isDead()) {
                        enemiesToRemove.add(enemy); //ADDING ENEMY TO LIST IF DEAD
                        score++; // + 1 SCORE
                    }
                    
                    else if (checkCollision(player, enemy) && enemy.canAttack()) { //IF PLAYER AND ENEMY ARE COLLIDING, DAMAGE PLAYER BY 10
                    player.takeDamage(10);
                    System.out.println("Player hit by enemy! Remaining health: " + player.getHealth()); //CONSOLE OUTPUT FOR DEBUGGING
                }
                    
                //ENEMY TAKING DAMAGE FROM JAB
                if (player.isJabbing() && checkAttackRange(player, enemy) && enemy.canBeHit()) {
                    int damage = 20;
                    enemy.takeDamage(damage);
                    System.out.println("Enemy hit by player jab! Remaining health: " + enemy.getHealth()); //CONSOLE OUTPUT FOR DEBUGGING
                }
                //ENEMY TAKING DAMAGE FROM SWIPE
                else if (player.isSwiping() && checkAttackRange(player, enemy) && enemy.canBeHit()) {
                    int damage = 10;
                    enemy.takeDamage(damage);
                    System.out.println("Enemy hit by player swipe! Remaining health: " + enemy.getHealth()); //CONSOLE OUTPUT FOR DEBUGGING
                }
            }
            enemies.removeAll(enemiesToRemove); //REMOVE DEAD ENEMIES
            checkPlayerDeath(); //CHECK IF PLAYER IS STILL ALIVE
            repaint(); //REPAINT GAME PANEL
            }
        
            //LIMITING FRAMERATE TO 60 FPS
            try {
                Thread.sleep(16);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    
    //CHECKING FOR COLLISIONS
    private boolean checkCollision(Player player, Enemy enemy) {
        return Math.abs(player.getX() - enemy.getX()) < 30 && Math.abs(player.getY() - enemy.getY()) < 30;
    }

    //CHECKING ATTACK RANGE
    private boolean checkAttackRange(Player player, Enemy enemy) {
        return Math.abs(player.getX() - enemy.getX()) < 50 && Math.abs(player.getY() - enemy.getY()) < 50;
    }

    
    @Override
    public void keyPressed(KeyEvent e) { 
        dbManager = new DatabaseManager(connection); //NEW DB
       
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE && !isGameOver) { //PAUSE GAME
            isPaused = !isPaused;
            if (isPaused) {
                try {
                    dbManager.savePlayerData(player);//SAVING PLAYER DATA EVERY PAUSE
                    dbManager.backupEnemies(enemies);//SAVING ENEMY DATA EVERY PAUSE
                    dbManager.saveHighScore(player.getUsername(), score);//SAVING PLAYER HIGHSCORE EVERY PAUSE
                }
                catch (SQLException ex) {
                    ex.printStackTrace();
                    }
            }
            repaint(); //REPAINTING
        }
        else {
            player.handleKeyPress(e);
        }
    }

    @Override //KEY RELEASED
    public void keyReleased(KeyEvent e) {
        player.handleKeyRelease(e);
    }

    @Override //KEY TYPED
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
    //DRAW HP BAR
    private void drawHP(Graphics g) { 
        g.setColor(Color.RED);
        g.fillRect(leftBoundary, bottomBoundary + 10, player.getHealth() * 2, 20);

        g.setColor(Color.BLACK);
        g.drawRect(leftBoundary, bottomBoundary + 10, 200, 20);  //DRAWING HP BAR
        g.drawString("HP: " + player.getHealth(), leftBoundary + 5, bottomBoundary + 25); //DRAWING "HP" STRING
    }
    
    //DRAW SCORE
    private void drawScore(Graphics g) {
        g.setColor(Color.YELLOW);
        g.setFont(new Font("Arial", Font.PLAIN, 16)); 
        g.drawString("Score: " + score, leftBoundary, bottomBoundary + 55);
        g.drawString("High Score: " + highScore, leftBoundary, bottomBoundary + 75); //DRAWING SCORE
    }

    //DRAW GAME OVER
    private void displayGameOver(Graphics g) {
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 40));
        
        g.drawString("Game Over", 250, 300); //DRAWING "GAME OVER"
        g.drawString("SCORE: " + score, 250, 350); //DRAWING SCORE
        g.drawString("HIGH SCORE: " + highScore, 250, 400); //DRAWING HIGHSCORE
    }
    
    //DRAW RESET BUTTON
    private void drawResetButton(Graphics g) {
        resetButtonBounds.setLocation(250, 420); //DRAWING RESTART BUTTON
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(resetButtonBounds.x, resetButtonBounds.y, resetButtonBounds.width, resetButtonBounds.height);
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("RESTART", resetButtonBounds.x + 20, resetButtonBounds.y + 30);
    }
    
    //RESET GAME
    public void resetGame() { 
        try {
            dbManager.savePlayerData(player); //SAVE PLAYER DATA BEFORE RESET
        } catch (SQLException ex) {
            ex.printStackTrace(); //ERROR PRINTING
        }
        //SETTING INITIAL PLAYER VALUES
        player.setHealth(100);
        player.setPosition(initialX, initialY);
        
        //SETTING GAMEOVER TO FALSE
        isGameOver = false;
        
        //RESETTING SCORE
        score = 0;

        //CLEARING ENEMIES
        enemies.clear();
        
        //RESETTING ENEMYID
        enemyID = 1;
        
        //SPAWNING RANDOM ENEMIES
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
            highScore = dbManager.loadHighScore(player.getUsername()); //RELOAD HIGHSCORE ON RESET
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        //RESET LAST ENEMY SPAWN TIME
        lastEnemySpawnTime = System.currentTimeMillis();

        repaint(); 
    }

    //CHECKING FOR PLAYER DEATH
    public void checkPlayerDeath() {
        if (player.isDead()) {
            isGameOver = true; //GAMEOVER
            try {
                dbManager.savePlayerData(player); //SAVE PLATER DATA
                
                //SAVING HIGH SCORE IF NEW RECORD
                if (score > highScore) {
                    dbManager.saveHighScore(player.getUsername(), score);
                    highScore = score; //UPDATING LOCAL SCORE
                }
            }
            catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    //DRAWING CONTROLS ONTO HUD
    private void drawControls(Graphics g) { 
        g.setFont(new Font("SansSerif", Font.PLAIN, 14));
        g.setColor(Color.YELLOW);
        
        //POSITION
        int startX = 20;
        int startY = getHeight() - 60;
        
        //DRAWING CONTROLS
        g.drawString("Controls:", startX + 550, startY - 65);
        g.drawString("WASD/ARROW KEYS = MOVE", startX + 550, startY - 45);
        g.drawString("O = JAB ATTACK", startX + 550, startY - 25);
        g.drawString("P = SWIPE ATTACK", startX + 550, startY - 5);
        g.drawString("SPACE = ROLL", startX + 550, startY + 15);
        g.drawString("ESC = PAUSE", startX + 550, startY + 35);
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
    
    
    //GETTERS AND SETTERS
    public static int getLeftBoundary() {
        return leftBoundary;
    }
    public static int getRightBoundary() {
        return rightBoundary;
    }
    public static int getTopBoundary() {
        return topBoundary;
    }
    public static int getBottomBoundary() {
        return bottomBoundary;
    }
    public void setPlayer(Player player) {
        this.player = player;
    }
    public void setDatabaseManager(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }
}
