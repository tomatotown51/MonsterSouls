package com.mycompany.monstersouls;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class SpriteLoader {

    /**
     * Loads an image from the resources folder.
     * 
     * @param path The relative path to the image file within the resources folder.
     * @return The loaded Image, or null if the image fails to load.
     */
    public static Image loadSprite(String path) {
        try {
            return ImageIO.read(new File(path));
        } catch (IOException e) {
            System.err.println("Error loading sprite at path: " + path);
            e.printStackTrace();
            return null;
        }
    }
}