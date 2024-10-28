package com.mycompany.monstersouls;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Rectangle;
import java.awt.Graphics;
import java.awt.Image;
import java.util.List;

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
    
    private static final long DECISION_DELAY = 200; //TIME BWETEEN AI DESICIONS
    private static final double CHASE_DISTANCE = 500; //DISTANCE BEFORE CHASING PLAYER
    private static final double ATTACK_DISTANCE = 100; //DISTANCE BEFORE ATTACKING PLAYER
    private EnemyState state = EnemyState.IDLE;
    private long lastDecisionTime = 0;
    
    private enum EnemyState {
        IDLE, CHASE, ATTACK, RETREAT
    }
    

    public Enemy(int x, int y, int health, int speed, String spritePath, int id) { //ENEMY CONSTRUCTOR
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

    public void update(Player player, List<Obstacle> obstacles) {
        long currentTime = System.currentTimeMillis();

        //UPDATING ENEMY STATE
        if (currentTime - lastDecisionTime >= DECISION_DELAY) {
            updateState(player);
            lastDecisionTime = currentTime;
        }

        //ACTING BASED ON STATE
        switch (state) {
            case IDLE:
                wander();
                break;
            case CHASE:
                moveTowardsPlayer(player, obstacles);
                break;
            case ATTACK:
                attack(player);
                break;
            case RETREAT:
                retreat(player);
                break;
        }

        //MAKING ENEMY STAY IN BOUNDARY
        stayWithinBoundaries();
        updateHitbox();
    }

    public void draw(Graphics g) {
        g.drawImage(sprite, x - sprite.getWidth(null) / 2, y - sprite.getHeight(null) / 2, null);

        //DRAWING HITBOX
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

    public int getX() {return x; }
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
    
    private void updateState(Player player) {
    double distanceToPlayer = distanceTo(player);

    if (health < 30) {
        state = EnemyState.RETREAT;
    } else if (distanceToPlayer <= ATTACK_DISTANCE) {
        state = EnemyState.ATTACK;
    } else if (distanceToPlayer <= CHASE_DISTANCE) {
        state = EnemyState.CHASE;
    } else {
        state = EnemyState.IDLE;
    }
}

    private void wander() {
        // Simple random movement
        double angle = Math.random() * 2 * Math.PI;
        double newX = x + Math.cos(angle) * speed / 2;
        double newY = y + Math.sin(angle) * speed / 2;
        moveWithinBoundaries(newX, newY);
    }

    private void moveTowardsPlayer(Player player, List<Obstacle> obstacles) {
        double dx = player.getX() - this.x;
        double dy = player.getY() - this.y;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance > 0) {
            dx /= distance;
            dy /= distance;

            // Check for obstacles and adjust movement
            for (Obstacle obstacle : obstacles) {
                if (willCollideWith(obstacle, dx * speed, dy * speed)) {
                    // Simple obstacle avoidance
                    dx += (Math.random() - 0.5) * 0.5;
                    dy += (Math.random() - 0.5) * 0.5;
                    break;
                }
            }

        // Normalize direction vector again after adjustment
        double newDistance = Math.sqrt(dx * dx + dy * dy);
        dx /= newDistance;
        dy /= newDistance;

        double newX = x + dx * speed;
        double newY = y + dy * speed;
        moveWithinBoundaries(newX, newY);
        
        }   
    }

    private void attack(Player player) {
        // Implement attack logic here
        if (canAttack()) {
        // Perform attack
        System.out.println("Enemy attacks player!");
        }
    }

    private void retreat(Player player) {
        double dx = this.x - player.getX();
        double dy = this.y - player.getY();
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance > 0) {
            dx /= distance;
            dy /= distance;
            double newX = x + dx * speed * 0.5; // Retreat at half speed
            double newY = y + dy * speed * 0.5;
            moveWithinBoundaries(newX, newY);
        }
    }

    private double distanceTo(Player player) {
        double dx = player.getX() - this.x;
        double dy = player.getY() - this.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    private boolean willCollideWith(Obstacle obstacle, double dx, double dy) {
        Rectangle futurePosition = new Rectangle(
            (int)(x + dx - hitbox.width / 2),
            (int)(y + dy - hitbox.height / 2),
            hitbox.width,
            hitbox.height
        );
        return futurePosition.intersects(obstacle.getHitbox());
    }

    private void moveWithinBoundaries(double newX, double newY) {
        x = (int) Math.max(GamePanel.getLeftBoundary(), Math.min(newX, GamePanel.getRightBoundary() - hitbox.width));
        y = (int) Math.max(GamePanel.getTopBoundary(), Math.min(newY, GamePanel.getBottomBoundary() - hitbox.height));
    }

    private void stayWithinBoundaries() {
        x = Math.max(GamePanel.getLeftBoundary(), Math.min(x, GamePanel.getRightBoundary() - hitbox.width));
        y = Math.max(GamePanel.getTopBoundary(), Math.min(y, GamePanel.getBottomBoundary() - hitbox.height));
    }
}