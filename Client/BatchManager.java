package com.mygdx.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class BatchManager {

    private SpriteBatch batch;
    private Texture img;
    private BitmapFont font;  // Add BitmapFont variable

    public void initialize() {
        batch = new SpriteBatch();
        img = new Texture("badlogic.jpg");
        font = new BitmapFont();  // Initialize the BitmapFont
        // Load other assets here
    }

    public SpriteBatch getBatch() {
        return batch;
    }

    public BitmapFont getFont() {
        return font;
    }

    public void dispose() {
        batch.dispose();
        img.dispose();
        font.dispose();  // Dispose of the BitmapFont
        // Dispose other assets here
    }
}
