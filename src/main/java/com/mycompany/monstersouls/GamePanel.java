package com.mycompany.monstersouls;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class GamePanel extends JPanel implements Runnable, KeyListener {
    private Player player;
    private Image backgroundImage;
    private int bgWidth, bgHeight;
    private int leftBoundary = 50;
    private int rightBoundary = 750;
    private int topBoundary = 50;
    private int bottomBoundary = 570;
    private int boundaryLineThickness = 5;

    public GamePanel() {
        setPreferredSize(new Dimension(800, 720)); //SET RESOLUTION
        setFocusable(true);
        requestFocus();
        addKeyListener(this);

        player = new Player(400, 300);

        try {
            backgroundImage = ImageIO.read(new File("resources/bg.jpg")); //LOADING BACKGROUND
            bgWidth = backgroundImage.getWidth(null);
            bgHeight = backgroundImage.getHeight(null);
            System.out.println("Image loaded successfully");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Image failed to load");
        }

        Thread gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    protected void paintComponent(Graphics g) { //PAINT COMPONENT
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        for (int row = 0; row < 3; row++) { //REPEATING BACKGROUND IN A GRID
            for (int col = 0; col < 3; col++) {
                g2d.drawImage(backgroundImage, col * bgWidth, row * bgHeight, null);
            }
        }

        drawBoundary(g2d); //DRAWING BOUNDARY

        player.draw(g); //DRAWING PLAYER
    }

    private void drawBoundary(Graphics2D g2d) {
        g2d.setColor(Color.BLACK); //COLOUR
        g2d.fillRect(leftBoundary - boundaryLineThickness, topBoundary, boundaryLineThickness, bottomBoundary - topBoundary); //LEFT
        g2d.fillRect(rightBoundary, topBoundary, boundaryLineThickness, bottomBoundary - topBoundary); //RIGHT
        g2d.fillRect(leftBoundary, topBoundary - boundaryLineThickness, rightBoundary - leftBoundary, boundaryLineThickness); //TOP
        g2d.fillRect(leftBoundary, bottomBoundary, rightBoundary - leftBoundary, boundaryLineThickness); //BOTTOM
    }

    @Override
    public void run() {
        while (true) {
            player.update(leftBoundary, rightBoundary, topBoundary, bottomBoundary);
            repaint();

            try {
                Thread.sleep(16); //APPROX 60 FRAMES PER SECOND
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        player.handleKeyPress(e);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        player.handleKeyRelease(e);
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }
}
