package com.mycompany.monstersouls;

import java.awt.Graphics;
import java.awt.Image;

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
    private static final long ATTACK_DELAY = 1000; // ENEMY
    private long lastAttackTime; // ENEMY
    private long lastHitTime; // PLAYER
    private static final long HIT_DELAY = 100; // PLAYER

    public Enemy(int x, int y, int health, int speed, String spritePath) {
        this.x = x;
        this.y = y;
        this.health = health;
        this.speed = speed;
        this.aggressionLevel = 5;
        this.lastUpdate = System.currentTimeMillis();
        this.lastAttackTime = 0;  // Initialize to allow first attack
        this.sprite = SpriteLoader.loadSprite(spritePath);
    }

    public boolean canAttack() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastAttackTime >= ATTACK_DELAY) {
            lastAttackTime = currentTime;
            return true;
        }
        return false;
    }

    public void takeDamage(int damage) {
        this.health -= damage;
        if (this.health <= 0) {
            this.health = 0;
            // Handle enemy death if needed
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
    }

    public void draw(Graphics g) {
        g.drawImage(sprite, x, y, null);
    }
    
    public boolean canBeHit() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastHitTime >= HIT_DELAY) {
            lastHitTime = currentTime;
            return true;
        }
        return false;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getHealth() { return health; }
    public boolean isDead() { return this.health <= 0; }
}
