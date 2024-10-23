package com.mygdx.game;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Align;

public class NumberTile {

    private double number = -1;
    private float x, y; 
    private BitmapFont font;
    private int tileSize; 
    private boolean showTile = true; 

    public NumberTile(float x, float y, int tileSize) {
        this.x = x;
        this.y = y;
        this.tileSize = tileSize;

        font = new BitmapFont();
    }

    public void update(double number, boolean appear){
        this.number = number;
        showTile = appear;
    }

    public void draw(SpriteBatch batch) {

        // question y do i need to shift the y tile on this is it casuse of the batch else it truncates like?
        if(showTile)
            font.draw(batch, String.valueOf(number), (x * tileSize), (y * tileSize) + tileSize);
    }
    
}
