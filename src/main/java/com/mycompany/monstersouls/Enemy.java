package com.mycompany.monstersouls;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;

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
    private static final long ATTACK_DELAY = 1000;
    private long lastAttackTime;
    private long lastHitTime;
    private static final long HIT_DELAY = 100;
    private Rectangle hitbox;
    private int id;
    private String type;
    

    public Enemy(int x, int y, int health, int speed, String spritePath, int id) {
        this.x = x;
        this.y = y;
        this.health = health;
        this.speed = speed;
        this.aggressionLevel = 5;
        this.lastUpdate = System.currentTimeMillis();
        this.lastAttackTime = 0;
        this.sprite = SpriteLoader.loadSprite(spritePath);
        this.id = id;

        int hitboxSize = 32;
        this.hitbox = new Rectangle(x - hitboxSize / 2, y - hitboxSize / 2, hitboxSize, hitboxSize);
    }

    public void updateHitbox() {
        hitbox.setLocation(x - hitbox.width / 2, y - hitbox.height / 2);
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
            updateHitbox();
        }
    }

    public void draw(Graphics g) {
        g.drawImage(sprite, x - sprite.getWidth(null) / 2, y - sprite.getHeight(null) / 2, null);

        // Draw yellow hitbox
        g.setColor(Color.YELLOW);
        g.drawRect(hitbox.x, hitbox.y, hitbox.width, hitbox.height);
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
    public Rectangle getHitbox() { return hitbox; }
    public int getSpeed() { return speed; }
    public int getId() { return id; }
    public String getType() {return type; }
    
    private void setType(){
        this.type = type;
    }
}