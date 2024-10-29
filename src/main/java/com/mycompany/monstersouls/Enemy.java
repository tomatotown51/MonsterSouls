package com.mycompany.monstersouls;

//IMPORTS
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Graphics;
import java.awt.Image;
import java.util.List;


public class Enemy { //HANDLES ENEMIES
    
    //ENEMY CONSTRUCTOR VARIABLES
    private int x, y, health ,speed, id;
    private Image sprite;
    
    //ENEMY MOVEMENT VARIABLES
    private double directionX;
    private double directionY;
    private long lastUpdate;
    private static final long MOVE_DELAY = 1000;
    private int aggressionLevel;
    //ENEMY ATTACK VARIABLES
    private static final long ATTACK_DELAY = 1000;
    private long lastAttackTime;
    private long lastHitTime;
    private static final long HIT_DELAY = 100;
    
    //ENEMY HITBOX VARIABLES
    private Rectangle hitbox;
    
    //ENEMY AI VARIABLES
    private static final long DECISION_DELAY = 100; //TIME BWETEEN AI DESICIONS
    private static final double CHASE_DISTANCE = 300; //DISTANCE BEFORE CHASING PLAYER
    private static final double ATTACK_DISTANCE = 30; //DISTANCE BEFORE ATTACKING PLAYER
    private EnemyState state = EnemyState.IDLE;
    private long lastDecisionTime = 0;
    
    //ENEMY TYPE
    private String type;
    
    //TOGGLE ENEMY HITBOX
    private boolean toggleEnemyHitbox = true;
    
    //ENEMY CONSTRUCTOR
    public Enemy(int x, int y, int health, int speed, String spritePath, int id) {
        //THIS
        this.x = x;
        this.y = y;
        this.health = health;
        this.speed = speed;
        this.aggressionLevel = 5;
        this.lastUpdate = System.currentTimeMillis();
        this.lastAttackTime = 0;
        this.sprite = SpriteLoader.loadSprite(spritePath);
        this.id = id;
        
        //HITBOX
        int hitboxSize = 32;
        this.hitbox = new Rectangle(x - hitboxSize / 2, y - hitboxSize / 2, hitboxSize, hitboxSize);
    }
    
    //DRAWING ENEMIES
    public void draw(Graphics g) {
        //DRAWING ENEMY SPRITE
        g.drawImage(sprite, x - sprite.getWidth(null) / 2, y - sprite.getHeight(null) / 2, null);
        //DRAWING ENEMY HITBOX DEPENDING ON VARIABLE
        if (toggleEnemyHitbox) {
            g.setColor(Color.YELLOW);
            g.drawRect(hitbox.x, hitbox.y, hitbox.width, hitbox.height);
        }

    }
    
//ENEMY UPDATES
    public void update(Player player, List<Obstacle> obstacles) {
        long currentTime = System.currentTimeMillis();

        //ATTACK IF IN ATTACK RANGE
        if (distanceTo(player) <= ATTACK_DISTANCE) {
            if (canAttack()) {
                attack(player);
            }
        }
        
        //ELSE CONTINUE MOVEMENT
        else { 
            if (currentTime - lastDecisionTime >= DECISION_DELAY) { //UPDATE ENEMY STATE
                updateState(player);
                lastDecisionTime = currentTime;
            }
            
            //SWITCH FOR ENEMY STATE
            switch (state) {
                case IDLE: //WANDER IF IDLE
                    wander();
                    break;
                
                case CHASE: //CHASE PLAYER
                    moveTowardsPlayer(player, obstacles);
                    break;

                case RETREAT: //RUN AWAY
                    retreat(player);
                    break;
            }
        }
        
        stayWithinBoundaries(); //MAKING SURE ENEMY STAYS IN BOUNDARY
        updateHitbox(); //UPDATING HITBOX
        
    }
    
    //UPDATING ENEMY HITBOX
    public void updateHitbox() {
        hitbox.setLocation(x - hitbox.width / 2, y - hitbox.height / 2);
    }
    
    
//ATTACK METHODS
    
    //ENEMY ATTACK
    private void attack(Player player) {
    
        player.takeDamage(10); //DEALING DAMAGE TO PLAYER
        
        //PRINT DAMAGE TO CONSOLE FOR DEBUGGING
        System.out.println("Enemy attacks player! Player health: " + player.getHealth());
    }
    
    //CHECKING IF ENEMY CAN ATTACK
    public boolean canAttack() {
        long currentTime = System.currentTimeMillis();
        //CHECKING ATTACK DELAY
        if (currentTime - lastAttackTime >= ATTACK_DELAY) {
            lastAttackTime = currentTime;
            return true;
        }
        return false; //ELSE
    }
    
//RECIEVE DAMAGE METHODS
    
    //ENEMY TAKING DAMAGE
    public void takeDamage(int damage) {
        this.health -= damage;
        if (this.health <= 0) {
            this.health = 0;
        }
    }
    
    //CHECKING IF ENEMY CAN BE HIT
    public boolean canBeHit() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastHitTime >= HIT_DELAY) {
            lastHitTime = currentTime;
            return true;
        }
        return false;
    }
    
 
    
//ENEMY AI METHODS
    
    //ENEMY STATES
    private enum EnemyState {
        IDLE, CHASE, RETREAT
    }
    
    //UPDATING ENEMY STATE
    private void updateState(Player player) {
        double distanceToPlayer = distanceTo(player);

        if (health < 30) {
            state = EnemyState.RETREAT;
        } else if (distanceToPlayer <= CHASE_DISTANCE) {
            state = EnemyState.CHASE;
        } else {
            state = EnemyState.IDLE;
        }
    }
    
    
 //ENEMY BEHAVIOURS   
    
    //ENEMY MOVE TOWARDS PLAYER
    private void moveTowardsPlayer(Player player, List<Obstacle> obstacles) {
        double dx = player.getX() - this.x; //GETTING PLAYER LOCATION
        double dy = player.getY() - this.y;
        double distance = Math.sqrt(dx * dx + dy * dy); //DETERMENING DISTANCE FROM ENEMY TO PLAYER

        if (distance > 0) { //IF DISTANCE BIGGER THAN 0
            dx /= distance;
            dy /= distance;

            //CHECKING FOR OBSTACLES
            for (Obstacle obstacle : obstacles) {
                if (willCollideWith(obstacle, dx * speed, dy * speed)) {
                    //TRYING TO GO AROUND
                    dx += (Math.random() - 0.5) * 0.5;
                    dy += (Math.random() - 0.5) * 0.5;
                    break;
                }
            }

        //NORMALIZE DIRECTION VECTOR
        double newDistance = Math.sqrt(dx * dx + dy * dy);
        dx /= newDistance;
        dy /= newDistance;
        
        //NEW X & Y
        double newX = x + dx * speed;
        double newY = y + dy * speed;
        moveWithinBoundaries(newX, newY); //WITHIN BOUNDARIES
        
        }   
    }
 

    //ENEMY WANDER
    private void wander() {
        //RANDOM MOVEMENT
        double angle = Math.random() * 2 * Math.PI;
        double newX = x + Math.cos(angle) * speed / 2;
        double newY = y + Math.sin(angle) * speed / 2;
        moveWithinBoundaries(newX, newY); //WITHIN BOUNDARIES
    }
    

    //ENEMY RETREAT
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

    //CHECKING COLLISIONS
    private boolean willCollideWith(Obstacle obstacle, double dx, double dy) {
        
        Rectangle futurePosition = new Rectangle( //NEW RECTANGLE
            (int)(x + dx - hitbox.width / 2),
            (int)(y + dy - hitbox.height / 2),
            hitbox.width, //HITBOX SIZE
            hitbox.height
        );
        return futurePosition.intersects(obstacle.getHitbox());
    }
    
    //KEEP ENEMY MOVEMENT IN MAP BOUNDARY
    private void moveWithinBoundaries(double newX, double newY) {
        x = (int) Math.max(GamePanel.getLeftBoundary(), Math.min(newX, GamePanel.getRightBoundary() - hitbox.width));
        y = (int) Math.max(GamePanel.getTopBoundary(), Math.min(newY, GamePanel.getBottomBoundary() - hitbox.height));
    }

    //MAKING SURE NEW ENEMY COORDINATES ARE IN BOUNDARIES
    private void stayWithinBoundaries() {
        x = Math.max(GamePanel.getLeftBoundary(), Math.min(x, GamePanel.getRightBoundary() - hitbox.width));
        y = Math.max(GamePanel.getTopBoundary(), Math.min(y, GamePanel.getBottomBoundary() - hitbox.height));
    }
    
    //GETTING DISTANCE TO PLAYER
    private double distanceTo(Player player) {
        double dx = player.getX() - this.x;
        double dy = player.getY() - this.y;
        return Math.sqrt(dx * dx + dy * dy);
    }
    
//GETTERS AND SETTERS
    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }
    public int getHealth() {
        return health;
    }
    public boolean isDead() {
        return this.health <= 0; }
    public Rectangle getHitbox() { return hitbox;
    }
    public int getSpeed()
    { return speed;
    }
    public int getId() {
        return id;
    }
    public String getType() {
        return type;
    }
    private void setType(){
        this.type = type;
    }
    
}