package com.mycompany.monstersouls;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;

public class Player {
    private int x, y;
    private int speed = 2;
    private int rollSpeed = (int) (speed * 1.5);
    private boolean rolling = false;
    private int rollDuration = 200;
    private long rollEndTime = 0;
    private int health = 100;
    private long lastJabTime = 0;
    private long lastSwipeTime = 0;
    private static final long ATTACK_DELAY = 1000;

    private boolean up, down, left, right;

    private Image normalSprite;
    private Image rollSprite;
    private Image[] jabSprites;
    private Image[] swipeSprites;
    private int currentJabFrame = 0;
    private int currentSwipeFrame = 0;

    private long jabCooldown = 250;
    private long swipeCooldown = 500;
    private double rotationAngle = Math.toRadians(90); // Initializes facing down

    private boolean isJabbing = false;
    private boolean isSwiping = false;
    private String lastDirection = "DOWN";

    public Player(int startX, int startY) {
        this.x = startX;
        this.y = startY;

        // Load all necessary sprites using SpriteLoader
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
    
    public void takeDamage(int damage) {
        this.health -= damage;
        if (this.health <= 0) {
            this.health = 0;
            // Handle player death if needed
        }
    }

    public boolean isAttacking() {
        return isJabbing || isSwiping;
    }

    public int getX() { return x; }
    public int getY() { return y; }

    public void update(int leftBoundary, int rightBoundary, int topBoundary, int bottomBoundary) {
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

            if (moveX != 0 || moveY != 0) {
                if (moveX > 0 && moveY > 0) { rotationAngle = Math.toRadians(45 + 90); lastDirection = "DOWN-RIGHT"; }
                else if (moveX > 0 && moveY < 0) { rotationAngle = Math.toRadians(315 + 90); lastDirection = "UP-RIGHT"; }
                else if (moveX < 0 && moveY > 0) { rotationAngle = Math.toRadians(135 + 90); lastDirection = "DOWN-LEFT"; }
                else if (moveX < 0 && moveY < 0) { rotationAngle = Math.toRadians(225 + 90); lastDirection = "UP-LEFT"; }
            }
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

    public void draw(Graphics g) {
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
    }

    public void handleKeyPress(KeyEvent e) {
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

    private void roll() {
        if (!rolling) {
            rolling = true;
            rollEndTime = System.currentTimeMillis() + rollDuration;
        }
    }

    public void jab() {
        if (canJab()) {
            isJabbing = true;
            lastJabTime = System.currentTimeMillis();
        }
    }

    public void swipe() {
        if (canSwipe()) {
            isSwiping = true;
            lastSwipeTime = System.currentTimeMillis();
        }
    }

    public void resetJab() {
        isJabbing = false;
        currentJabFrame = 0;
    }

    public void resetSwipe() {
        isSwiping = false;
        currentSwipeFrame = 0;
    }

    private void move(String direction, int speed, int leftBoundary, int rightBoundary, int topBoundary, int bottomBoundary) {
        switch (direction) {
            case "UP": y = Math.max(y - speed, topBoundary); break;
            case "DOWN": y = Math.min(y + speed, bottomBoundary); break;
            case "LEFT": x = Math.max(x - speed, leftBoundary); break;
            case "RIGHT": x = Math.min(x + speed, rightBoundary); break;
            case "UP-RIGHT": y = Math.max(y - speed, topBoundary); x = Math.min(x + speed, rightBoundary); break;
            case "UP-LEFT": y = Math.max(y - speed, topBoundary); x = Math.max(x - speed, leftBoundary); break;
            case "DOWN-RIGHT": y = Math.min(y + speed, bottomBoundary); x = Math.min(x + speed, rightBoundary); break;
            case "DOWN-LEFT": y = Math.min(y + speed, bottomBoundary); x = Math.max(x - speed, leftBoundary); break;
        }
    }
    
    public int getSpeed(){
        return speed;
    }
    
    public String getDirection(){
        return lastDirection;
    }
    
    public int getHealth() {
        return health;
    }
    
    public boolean canJab() {
        return System.currentTimeMillis() - lastJabTime >= ATTACK_DELAY;
    }
    
    public boolean canSwipe() {
        return System.currentTimeMillis() - lastSwipeTime >= ATTACK_DELAY;
    }
    
    public boolean isJabbing() {
    return isJabbing;
}

    public boolean isSwiping() {
        return isSwiping;
}

    
}
