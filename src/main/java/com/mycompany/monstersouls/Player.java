package com.mycompany.monstersouls;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.geom.AffineTransform;

public class Player {
    private int x, y;
    private int speed = 2;
    private int rollSpeed = (int) (speed * 1.5);
    private boolean rolling = false;
    private int rollDuration = 200;
    private long rollEndTime = 0;

    private boolean up, down, left, right;

    private Image normalSprite;
    private Image rollSprite;
    private Image[] jabSprites;
    private Image[] swipeSprites;
    private int currentJabFrame = 0;
    private int currentSwipeFrame = 0;

    private long jabCooldown = 250;
    private long swipeCooldown = 500;
    private long lastJabTime = 0;
    private long lastSwipeTime = 0;

    private double rotationAngle = Math.toRadians(90); //INITIALIZE FACING DOWN

    private boolean isJabbing = false;
    private boolean isSwiping = false;
    private String lastDirection = "DOWN";

    public Player(int startX, int startY) { //PLAYER OBJECT
        this.x = startX; //COORDINATES
        this.y = startY;

        try { //TRY LOADING SPRITES
            normalSprite = ImageIO.read(new File("resources/player.png"));
            rollSprite = ImageIO.read(new File("resources/roll.png"));
            //ARRAY OF IMAGES FOR ANIMATION
            jabSprites = new Image[]{
                ImageIO.read(new File("resources/playerAttack1.png")),
                ImageIO.read(new File("resources/player.png")),
                ImageIO.read(new File("resources/playerAttack2.png"))
            };
            
            swipeSprites = new Image[]{ //RAN OUT OF TIME FOR ANIMATION
                ImageIO.read(new File("resources/swipe.png"))
            };
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    
    public int getX()
            
    {
        return x;
    }
    public int getY()
            
    {
        return y;
    }

    //UPDATE METHOD TO ENFORCE BOUNDARY
    public void update(int leftBoundary, int rightBoundary, int topBoundary, int bottomBoundary) {
        
        //ROLLING
        if (rolling) {
            if (System.currentTimeMillis() < rollEndTime) {
                move(lastDirection, rollSpeed, leftBoundary, rightBoundary, topBoundary, bottomBoundary);
            } else {
                rolling = false;
            }
        }
        else {
            if (up && !down && y - speed >= topBoundary) { 
                y -= speed;
                rotationAngle = Math.toRadians(270 + 90); // UP
                lastDirection = "UP";
            } else if (down && !up && y + speed <= bottomBoundary) {
                y += speed;
                rotationAngle = Math.toRadians(90 + 90); // DOWN
                lastDirection = "DOWN";
            } else if (left && !right && x - speed >= leftBoundary) {
                x -= speed;
                rotationAngle = Math.toRadians(180 + 90); // LEFT
                lastDirection = "LEFT";
            } else if (right && !left && x + speed <= rightBoundary) {
                x += speed;
                rotationAngle = Math.toRadians(0 + 90); // RIGHT
                lastDirection = "RIGHT";
            } else if (up && right && y - speed >= topBoundary && x + speed <= rightBoundary) {
                y -= speed;
                x += speed;
                rotationAngle = Math.toRadians(315 + 90); // UP-RIGHT
                lastDirection = "UP-RIGHT";
            } else if (up && left && y - speed >= topBoundary && x - speed >= leftBoundary) {
                y -= speed;
                x -= speed;
                rotationAngle = Math.toRadians(225 + 90); // UP-LEFT
                lastDirection = "UP-LEFT";
            } else if (down && right && y + speed <= bottomBoundary && x + speed <= rightBoundary) {
                y += speed;
                x += speed;
                rotationAngle = Math.toRadians(45 + 90); // DOWN-RIGHT
                lastDirection = "DOWN-RIGHT";
            } else if (down && left && y + speed <= bottomBoundary && x - speed >= leftBoundary) {
                y += speed;
                x -= speed;
                rotationAngle = Math.toRadians(135 + 90); // DOWN-LEFT
                lastDirection = "DOWN-LEFT";
            }
        }

        if (isJabbing) {
            if (System.currentTimeMillis() - lastJabTime < jabCooldown) {
                currentJabFrame = (int) ((System.currentTimeMillis() - lastJabTime) / (jabCooldown / jabSprites.length)) % jabSprites.length;
            } else {
                isJabbing = false;
                currentJabFrame = 0;
            }
        }

        if (isSwiping) { 
            if (System.currentTimeMillis() - lastSwipeTime < swipeCooldown) {
                currentSwipeFrame = (int) ((System.currentTimeMillis() - lastSwipeTime) / (swipeCooldown / swipeSprites.length));
            } else {
                isSwiping = false;
                currentSwipeFrame = 0;
            }
        }
    }

    public void draw(Graphics g) { //SPRITE DRAWING
        Graphics2D g2d = (Graphics2D) g;
        AffineTransform oldTransform = g2d.getTransform();

        Image currentSprite = normalSprite;
        if (rolling) {
            currentSprite = rollSprite;
        } else if (isJabbing) {
            currentSprite = jabSprites[currentJabFrame % jabSprites.length];
        } else if (isSwiping) {
            currentSprite = swipeSprites[currentJabFrame % jabSprites.length];
        }

        AffineTransform transform = new AffineTransform();
        transform.translate(x, y);
        transform.rotate(rotationAngle);
        transform.translate(-currentSprite.getWidth(null) / 2, -currentSprite.getHeight(null) / 2);

        g2d.drawImage(currentSprite, transform, null);
        g2d.setTransform(oldTransform);
    }

    public void handleKeyPress(KeyEvent e) { //HANDLING CONTROLS
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_W || key == KeyEvent.VK_UP) up = true;
        if (key == KeyEvent.VK_S || key == KeyEvent.VK_DOWN) down = true;
        if (key == KeyEvent.VK_A || key == KeyEvent.VK_LEFT) left = true;
        if (key == KeyEvent.VK_D || key == KeyEvent.VK_RIGHT) right = true;
        if (key == KeyEvent.VK_SPACE) roll();
        if (key == KeyEvent.VK_O) performJab();
        if (key == KeyEvent.VK_P) performSwipe();
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

    private void performJab() { //JAB ATTACK
        if (!isJabbing && System.currentTimeMillis() - lastJabTime >= jabCooldown) {
            isJabbing = true;
            lastJabTime = System.currentTimeMillis();
        }
    }

    private void performSwipe() { //SWIPE ATTACK
        if (!isSwiping && System.currentTimeMillis() - lastSwipeTime >= swipeCooldown) {
            isSwiping = true;
            lastSwipeTime = System.currentTimeMillis();
        }
    }

    private void move(String direction, int speed, int leftBoundary, int rightBoundary, int topBoundary, int bottomBoundary) { //PLAYER MOVEMENT LOGIC
        switch (direction) {
            case "UP": 
                y = Math.max(y - speed, topBoundary); 
                break;
            case "DOWN": 
                y = Math.min(y + speed, bottomBoundary); 
                break;
            case "LEFT": 
                x = Math.max(x - speed, leftBoundary); 
                break;
            case "RIGHT": 
                x = Math.min(x + speed, rightBoundary); 
                break;
            case "UP-RIGHT": 
                y = Math.max(y - speed, topBoundary); 
                x = Math.min(x + speed, rightBoundary); 
                break;
            case "UP-LEFT": 
                y = Math.max(y - speed, topBoundary); 
                x = Math.max(x - speed, leftBoundary); 
                break;
            case "DOWN-RIGHT": 
                y = Math.min(y + speed, bottomBoundary); 
                x = Math.min(x + speed, rightBoundary); 
                break;
            case "DOWN-LEFT": 
                y = Math.min(y + speed, bottomBoundary); 
                x = Math.max(x - speed, leftBoundary); 
                break;
        }
    }
}
