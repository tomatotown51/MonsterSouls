package com.mycompany.monstersouls;

import java.awt.Rectangle;


public class Obstacle {
    private int x, y, width, height;
    private Rectangle hitbox;

    public Obstacle(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.hitbox = new Rectangle(x, y, width, height);
    }

    public Rectangle getHitbox() {
        return hitbox;
    }
}