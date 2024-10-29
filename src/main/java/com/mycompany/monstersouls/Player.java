package com.mycompany.monstersouls;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;

public class Player {
    
    //CONSTRUCTOR VARIABLES
    private int x, y;
    private int health = 100; //INITIAL PLAYER HEALTH
    
    //MOVEMENT VARIABLES
    private int speed = 2;
    private int rollSpeed = (int) (speed * 1.5);
    private boolean rolling = false;
    private int rollDuration = 200;
    private long rollEndTime = 0;
    private boolean up, down, left, right;
    private double rotationAngle = Math.toRadians(90);
    private String lastDirection = "DOWN";
    
    //ATTACK VARIABLES
    private long lastJabTime = 0;
    private long lastSwipeTime = 0;
    private static final long ATTACK_DELAY = 1000;
    private long jabCooldown = 250;
    private long swipeCooldown = 500;
    private boolean isJabbing = false;
    private boolean isSwiping = false;
    
    //HITBOX VARIABLES
    private static final int HITBOX_WIDTH = 32; //PLAYER RECIEVE DAMAGE HITBOX
    private static final int HITBOX_HEIGHT = 32; 
    private static final int ATTACK_HITBOX_WIDTH = 48; //PLAYER ATTACK DAMAGE HITBOX
    private static final int ATTACK_HITBOX_HEIGHT = 48; 
    
    //SPRITES
    private Image normalSprite;
    private Image rollSprite;
    private Image[] jabSprites;
    private Image[] swipeSprites;
    private int currentJabFrame = 0;
    private int currentSwipeFrame = 0;
    
    //OTHER
    private String username = "";

    public Player(int startX, int startY, int health) { //CONSTRUCTOR
        this.x = startX;
        this.y = startY;
        this.health = health;

        //LOADING SPRITES
        normalSprite = SpriteLoader.loadSprite("resources/player.png"); 
        rollSprite = SpriteLoader.loadSprite("resources/roll.png");
        jabSprites = new Image[]{
                SpriteLoader.loadSprite("resources/playerAttack1.png"),
                SpriteLoader.loadSprite("resources/player.png"),
                SpriteLoader.loadSprite("resources/playerAttack2.png")
        };
        swipeSprites = new Image[]{
                SpriteLoader.loadSprite("resources/swipe.png")
        };
        
    }
    
    //UPDATE METHOD
    public void update(int leftBoundary, int rightBoundary, int topBoundary, int bottomBoundary) {
        
        //ROLLING
        if (rolling) {
            if (System.currentTimeMillis() < rollEndTime) { //ROLL TIME
                move(lastDirection, rollSpeed, leftBoundary, rightBoundary, topBoundary, bottomBoundary); //WITHIN BOUNDARY
            } else {
                rolling = false;
            }
        }
        else {
            
            //PLAYER MOVEMENT
            double moveX = 0;
            double moveY = 0;
            
            //DIRECTION HANDLING
            if (up) { moveY -= speed; rotationAngle = Math.toRadians(270 + 90); lastDirection = "UP"; }
            if (down) { moveY += speed; rotationAngle = Math.toRadians(90 + 90); lastDirection = "DOWN"; }
            if (left) { moveX -= speed; rotationAngle = Math.toRadians(180 + 90); lastDirection = "LEFT"; }
            if (right) { moveX += speed; rotationAngle = Math.toRadians(0 + 90); lastDirection = "RIGHT"; }

            //SPEED HANDLING
            double magnitude = Math.sqrt(moveX * moveX + moveY * moveY);
            if (magnitude > 0) { moveX /= magnitude; moveY /= magnitude; }
            x += moveX * speed;
            y += moveY * speed;
            
            //WITHIN BOUNDARY
            x = Math.max(leftBoundary, Math.min(x, rightBoundary));
            y = Math.max(topBoundary, Math.min(y, bottomBoundary));
        }

        //IF JABBING
        if (isJabbing) {
            if (System.currentTimeMillis() - lastJabTime < jabCooldown) { //COOLDOWN
                currentJabFrame = (int) ((System.currentTimeMillis() - lastJabTime) / (jabCooldown / jabSprites.length)) % jabSprites.length; //SPRITE ANIMATION
            } else {
                resetJab(); //RESET JAB
            }
        }
        
        //IF SWIPING
        if (isSwiping) { 
            if (System.currentTimeMillis() - lastSwipeTime < swipeCooldown) { //COOLDOWN
                currentSwipeFrame = (int) ((System.currentTimeMillis() - lastSwipeTime) / (swipeCooldown / swipeSprites.length)); //SPRITE ANIMATION
            } else {
                resetSwipe(); //RESET SWIPE
            }
        }
    }
    
    public void draw(Graphics g) { //DRAWING PLAYER SPRITES
        Graphics2D g2d = (Graphics2D) g;
        AffineTransform oldTransform = g2d.getTransform();

        Image currentSprite = normalSprite; //SETTING SPRITES
        if (rolling) {
            currentSprite = rollSprite; //ROLL SPRITE
        } else if (isJabbing) {
            currentSprite = jabSprites[currentJabFrame % jabSprites.length]; //JAB ANIMATION
        } else if (isSwiping) {
            currentSprite = swipeSprites[currentSwipeFrame % swipeSprites.length]; //SWIPE ANIMATION
        }

        AffineTransform transform = new AffineTransform();
        transform.translate(x, y);
        transform.rotate(rotationAngle);
        transform.translate(-currentSprite.getWidth(null) / 2, -currentSprite.getHeight(null) / 2);

        g2d.drawImage(currentSprite, transform, null);
        g2d.setTransform(oldTransform);

        
        //DRAWING HITBOXES FOR DEBUGGING
        
        //RECEIEVE DMG HITBOX
        g.setColor(new Color(0, 0, 255, 100)); //BLUE
        g.fillRect(x - HITBOX_WIDTH / 2, y - HITBOX_HEIGHT / 2, HITBOX_WIDTH, HITBOX_HEIGHT);

        //ATTACK HTIBOX
        if (isJabbing || isSwiping) {
            g.setColor(new Color(255, 0, 0, 100)); //RED
            g.fillRect(x - ATTACK_HITBOX_WIDTH / 2, y - ATTACK_HITBOX_HEIGHT / 2, ATTACK_HITBOX_WIDTH, ATTACK_HITBOX_HEIGHT);
        }
    }
    
 //CONTROLS
    public void handleKeyPress(KeyEvent e) { //HANDLING KEY PRESS
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_W || key == KeyEvent.VK_UP) up = true;//UP: W || ARROW UP
        if (key == KeyEvent.VK_S || key == KeyEvent.VK_DOWN) down = true;//DOWN: S || ARROW DOWN
        if (key == KeyEvent.VK_A || key == KeyEvent.VK_LEFT) left = true;//LEFT: A || ARROW LEFT
        if (key == KeyEvent.VK_D || key == KeyEvent.VK_RIGHT) right = true;//RIGHT: D || ARROW RIGHT
        if (key == KeyEvent.VK_SPACE) roll();//ROLL: SPACE
        if (key == KeyEvent.VK_O) jab();//JAB: O
        if (key == KeyEvent.VK_P) swipe();//SWIPE: P
    }

    public void handleKeyRelease(KeyEvent e) { //HANDLING KEY RELEASES
        int key = e.getKeyCode();
        
        //STOPPING MOVEMENT WHEN RELEASED
        if (key == KeyEvent.VK_W || key == KeyEvent.VK_UP) up = false;
        if (key == KeyEvent.VK_S || key == KeyEvent.VK_DOWN) down = false;
        if (key == KeyEvent.VK_A || key == KeyEvent.VK_LEFT) left = false;
        if (key == KeyEvent.VK_D || key == KeyEvent.VK_RIGHT) right = false;
    }
    
    //MOVE LOGIC
    private void move(String direction, int speed, int leftBoundary, int rightBoundary, int topBoundary, int bottomBoundary) { 
        switch (direction) { //SWITCH BASED ON DIRECTION
            case "UP": y = Math.max(y - speed, topBoundary); break;
            case "DOWN": y = Math.min(y + speed, bottomBoundary); break;
            case "LEFT": x = Math.max(x - speed, leftBoundary); break;
            case "RIGHT": x = Math.min(x + speed, rightBoundary); break;
        }
    }
    
    //PLAYER TAKE DAMAGE
    public void takeDamage(int damage) { 
        this.health -= damage;
        if (this.health <= 0) {
            this.health = 0;
            handleDeath();
        }
    }
    //PLAYER IS ATTACKING
    public boolean isAttacking() {
        return isJabbing || isSwiping;
    }

    private void roll() { //ROLL
        if (!rolling) {
            rolling = true;
            rollEndTime = System.currentTimeMillis() + rollDuration;
        }
    }

    public void jab() { //ATTACK JAB
        if (canJab()) {
            isJabbing = true;
            lastJabTime = System.currentTimeMillis();
        }
    }

    public void swipe() { //SWIPE JAB
        if (canSwipe()) {
            isSwiping = true;
            lastSwipeTime = System.currentTimeMillis();
        }
    }

    public void resetJab() { //RESETTING JAB
        isJabbing = false;
        currentJabFrame = 0;
    }

    public void resetSwipe() { //RESETTING SWIPE
        isSwiping = false;
        currentSwipeFrame = 0;
    }


    //CHECKING IF CAN JAB
    public boolean canJab() {
        return System.currentTimeMillis() - lastJabTime >= ATTACK_DELAY;
    }
    
    //CHECKING IF CAN SWIPE
    public boolean canSwipe() {
        return System.currentTimeMillis() - lastSwipeTime >= ATTACK_DELAY;
    }
    
    //CHECKING IF PLAYER IS ATTACKING ENEMY HITBOX
    public boolean isAttackingEnemy(Enemy enemy) {
        Rectangle attackHitbox = new Rectangle(//USING HITBOX DIMENSIONS
            x - ATTACK_HITBOX_WIDTH / 2,
            y - ATTACK_HITBOX_HEIGHT / 2,
            ATTACK_HITBOX_WIDTH,
            ATTACK_HITBOX_HEIGHT
        );

        //CHECKING IF HITBOXES INTERSECT
        return attackHitbox.intersects(enemy.getHitbox());
    }
    
    //IS JABBING
    public boolean isJabbing() {
        return isJabbing;
    }
    
    //IS SWIPING
    public boolean isSwiping() {
        return isSwiping;
    }
    
    //IS DEAD
    public boolean isDead() {
        return this.health <= 0;
    }
    //HANDLING DEATH CONSOLE OUTPUT
    private void handleDeath() { 
        System.out.println("Player has died!");
    }
    
    //GETTERS AND SETTERS
    public void setHealth(int health) {
        this.health = health;
    }
    
    public void setPosition( int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    public void setUsername (String username) {
        this.username = username;
    }
    
    public String getUsername () {
        return username;
    }
    
    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }
    public int getHealth() {
        return health;
    }
 
}