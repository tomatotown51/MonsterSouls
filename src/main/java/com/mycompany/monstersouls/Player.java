package com.mycompany.monstersouls;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;

public class Player {
    private int x, y;
    private int speed = 2;
    private int rollSpeed = (int) (speed * 1.5);
    private boolean rolling = false;
    private int rollDuration = 200;
    private long rollEndTime = 0;
    private int health = 100; //INITIAL PLAYER HEALTH
    private long lastJabTime = 0;
    private long lastSwipeTime = 0;
    private static final long ATTACK_DELAY = 1000;
    private static final int HITBOX_WIDTH = 32; //PLAYER RECIEVE DAMAGE HITBOX
    private static final int HITBOX_HEIGHT = 32; 
    private static final int ATTACK_HITBOX_WIDTH = 48; //PLAYER ATTACK DAMAGE HITBOX
    private static final int ATTACK_HITBOX_HEIGHT = 48; 
    private String username = "";

    private boolean up, down, left, right;
    private Image normalSprite;
    private Image rollSprite;
    private Image[] jabSprites;
    private Image[] swipeSprites;
    private int currentJabFrame = 0;
    private int currentSwipeFrame = 0;

    private long jabCooldown = 250;
    private long swipeCooldown = 500;
    private double rotationAngle = Math.toRadians(90);
    private boolean isJabbing = false;
    private boolean isSwiping = false;
    private String lastDirection = "DOWN";
    

    public Player(int startX, int startY, int health) {
        this.x = startX;
        this.y = startY;
        this.health = health;
        this.username = username;

        normalSprite = SpriteLoader.loadSprite("resources/player.png"); //LOADING SPRITES
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

    public void takeDamage(int damage) { //TAKING DAMAGE
        this.health -= damage;
        if (this.health <= 0) {
            this.health = 0;
            handleDeath();
        }
    }

    public boolean isAttacking() {
        return isJabbing || isSwiping;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getHealth() { return health; }

    public boolean isJabbing() { return isJabbing; }
    public boolean isSwiping() { return isSwiping; }

    public void update(int leftBoundary, int rightBoundary, int topBoundary, int bottomBoundary) { //UPDATE METHOD
        if (rolling) {
            if (System.currentTimeMillis() < rollEndTime) {
                move(lastDirection, rollSpeed, leftBoundary, rightBoundary, topBoundary, bottomBoundary);
            } else {
                rolling = false;
            }
        } else {
            double moveX = 0;
            double moveY = 0;

            if (up) { moveY -= speed; rotationAngle = Math.toRadians(270 + 90); lastDirection = "UP"; }
            if (down) { moveY += speed; rotationAngle = Math.toRadians(90 + 90); lastDirection = "DOWN"; }
            if (left) { moveX -= speed; rotationAngle = Math.toRadians(180 + 90); lastDirection = "LEFT"; }
            if (right) { moveX += speed; rotationAngle = Math.toRadians(0 + 90); lastDirection = "RIGHT"; }

            double magnitude = Math.sqrt(moveX * moveX + moveY * moveY);
            if (magnitude > 0) { moveX /= magnitude; moveY /= magnitude; }

            x += moveX * speed;
            y += moveY * speed;

            x = Math.max(leftBoundary, Math.min(x, rightBoundary));
            y = Math.max(topBoundary, Math.min(y, bottomBoundary));
        }

        if (isJabbing) {
            if (System.currentTimeMillis() - lastJabTime < jabCooldown) {
                currentJabFrame = (int) ((System.currentTimeMillis() - lastJabTime) / (jabCooldown / jabSprites.length)) % jabSprites.length;
            } else {
                resetJab();
            }
        }

        if (isSwiping) {
            if (System.currentTimeMillis() - lastSwipeTime < swipeCooldown) {
                currentSwipeFrame = (int) ((System.currentTimeMillis() - lastSwipeTime) / (swipeCooldown / swipeSprites.length));
            } else {
                resetSwipe();
            }
        }
    }

    public void draw(Graphics g) { //DRAWING PLAYER SPRITES
        Graphics2D g2d = (Graphics2D) g;
        AffineTransform oldTransform = g2d.getTransform();

        Image currentSprite = normalSprite;
        if (rolling) {
            currentSprite = rollSprite;
        } else if (isJabbing) {
            currentSprite = jabSprites[currentJabFrame % jabSprites.length];
        } else if (isSwiping) {
            currentSprite = swipeSprites[currentSwipeFrame % swipeSprites.length];
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

    public void handleKeyPress(KeyEvent e) { //HANDLING CONTROLS
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_W || key == KeyEvent.VK_UP) up = true;
        if (key == KeyEvent.VK_S || key == KeyEvent.VK_DOWN) down = true;
        if (key == KeyEvent.VK_A || key == KeyEvent.VK_LEFT) left = true;
        if (key == KeyEvent.VK_D || key == KeyEvent.VK_RIGHT) right = true;
        if (key == KeyEvent.VK_SPACE) roll();
        if (key == KeyEvent.VK_O) jab();
        if (key == KeyEvent.VK_P) swipe();
    }

    public void handleKeyRelease(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_W || key == KeyEvent.VK_UP) up = false;
        if (key == KeyEvent.VK_S || key == KeyEvent.VK_DOWN) down = false;
        if (key == KeyEvent.VK_A || key == KeyEvent.VK_LEFT) left = false;
        if (key == KeyEvent.VK_D || key == KeyEvent.VK_RIGHT) right = false;
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

    private void move(String direction, int speed, int leftBoundary, int rightBoundary, int topBoundary, int bottomBoundary) { //MOVE LOGIC
        switch (direction) {
            case "UP": y = Math.max(y - speed, topBoundary); break;
            case "DOWN": y = Math.min(y + speed, bottomBoundary); break;
            case "LEFT": x = Math.max(x - speed, leftBoundary); break;
            case "RIGHT": x = Math.min(x + speed, rightBoundary); break;
        }
    }

    public boolean canJab() {
        return System.currentTimeMillis() - lastJabTime >= ATTACK_DELAY;
    }

    public boolean canSwipe() {
        return System.currentTimeMillis() - lastSwipeTime >= ATTACK_DELAY;
    }

    public boolean isAttackingEnemy(Enemy enemy) {
        //USING HITBOX DIMENSIONS
        Rectangle attackHitbox = new Rectangle(
            x - ATTACK_HITBOX_WIDTH / 2,
            y - ATTACK_HITBOX_HEIGHT / 2,
            ATTACK_HITBOX_WIDTH,
            ATTACK_HITBOX_HEIGHT
        );

        //CHECKING IF HITBOXES INTERSECT
        return attackHitbox.intersects(enemy.getHitbox());
    }
    
    public boolean isDead() {
        return this.health <= 0;
    }
    
    private void handleDeath() { //RIP
        System.out.println("Player has died!");
    }
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
    
    
}