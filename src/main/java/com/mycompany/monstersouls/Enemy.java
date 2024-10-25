package com.mycompany.monstersouls;

import java.awt.Graphics;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Enemy {
    private int x, y;
    private int health;
    private int speed;
    private Image sprite;
    private double directionX;
    private double directionY;
    private long lastUpdate;
    private static final long MOVE_DELAY = 1000;
    private int aggressionLevel;

    public Enemy(int x, int y, int health, int speed, String spritePath) {
        this.x = x;
        this.y = y;
        this.health = health;
        this.speed = speed;
        this.aggressionLevel = 5;
        this.lastUpdate = System.currentTimeMillis();

        try {
            this.sprite = ImageIO.read(new File(spritePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void update(Player player) {
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastUpdate >= MOVE_DELAY) {
            directionX = player.getX() - this.x;
            directionY = player.getY() - this.y;

            double magnitude = Math.sqrt(directionX * directionX + directionY * directionY);
            if (magnitude > 0) {
                directionX /= magnitude;
                directionY /= magnitude;
            }

            this.x += directionX * speed;
            this.y += directionY * speed;

            lastUpdate = currentTime;
        }

        if (aggressionLevel > 7) {
            // Implement additional AI behavior if needed
        }
    }

    public void takeDamage(int damage) {
        this.health -= damage;
        if (this.health <= 0) {
            this.health = 0;
            // Handle enemy death if needed
        }
    }

    public void draw(Graphics g) {
        g.drawImage(sprite, x, y, null);
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
