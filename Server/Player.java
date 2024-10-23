package com.mygdx.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Player {
    public enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    private float x;
    private float y;
    private float velocity = 50f; 
    
    private Direction facingDirection = Direction.UP;
    private boolean isTurning;
    private boolean isMoving; 

    private float targetX; 
    private float targetY; 
    private float oldX; 
    private float oldY; 

    private float tweenTime = .025f;

    private float tileW;
    private float tileH; 

    private Texture blockTexture; 
    private SpriteBatch batch; 

    public Player(float inputX, float inputY, SpriteBatch batch, float tileSize) {
        this.x = inputX * tileSize;
        this.y = inputY * tileSize;
        this.batch = batch;

        blockTexture = new Texture("badlogic.jpg");
        blockTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        tileW = tileSize;
        tileH = tileSize;
        isTurning = false; 
        isMoving = false; 

        oldX = inputX;
        oldY = inputY; 
        setTargetPosition(inputX, inputY); 

    }

    public void setTargetPosition(float targetX, float targetY) {
        this.targetX = targetX * tileW; 
        this.targetY = targetY * tileH;
    }

    public void update(float delta, char[][] tilemap) {
        // move 
        if(isMoving){
            if(isTurning){
                // maybe add animation for turning or a pause that it cant move idk for now false
                isTurning = false;
            }
            else{
                float nextX = oldX; 
                float nextY = oldY; 
                switch (facingDirection) {
                    case UP:
                        nextY = oldY + 1; 
                        break;
                    case DOWN:
                        nextY = oldY - 1;
                        break;
                    case LEFT:
                        nextX = oldX - 1;
                        break;
                    case RIGHT:
                        nextX = oldX + 1;
                        break;
                }
                if(isValidMovement(nextX, nextY, tilemap)){
                    setTargetPosition(nextX, nextY); 
                    float percentComplete = Math.min(1.0f, delta / tweenTime); // Ensure it doesn't exceed 100%
                    x = interpolate(x, targetX, percentComplete);
                    y = interpolate(y, targetY, percentComplete);

                    // Check if the player is close enough to the target position
                    float distanceSquared = (x - targetX) * (x - targetX) + (y - targetY) * (y - targetY);
                    float thresholdSquared = 0.001f * 0.001f; // Adjust the threshold as needed

                    if (distanceSquared < thresholdSquared) {
                        isMoving = false; 
                        oldX = targetX/tileW;
                        oldY = targetY/tileH;
                    }
                }
                else{
                    // cant move so its not moving
                    isMoving = false; 
                }
            }// else not turning 
        }// if moving 
    }
    

    private float interpolate(float start, float end, float percent) {
        return start + percent * (end - start);
    }

    private boolean isValidMovement(float nextX, float nextY, char[][] tilemap) {
        //  so just use .length() idk if its much slower than passing around but for now to just remember i can 
        if(nextX < 0 || nextX >= tilemap.length){
            return false;
        }

        if(nextY < 0 || nextY >= tilemap[0].length){
            return false;
        }

        if(tilemap[(int)nextX][(int)nextY] == '-'){
            return false;
        }

        return true; 
    }

    public void render() {
        batch.draw(blockTexture, x, y, tileW, tileH);
    }

    //  ===================
    //  getters and setters
    //  ===================
    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public int getOldX() {
        return (int)oldX;
    }

    public int getOldY() {
        return (int)oldY;
    }

    public float getVelocity() {
        return velocity;
    }

    public void setVelocity(float velocity) {
        this.velocity = velocity;
    }

    public Direction getFacingDirection() {
        return facingDirection;
    }

    public void setFacingDirection(Direction facingDirection) {
        this.facingDirection = facingDirection;
    }

    public boolean isTurning() {
        return isTurning;
    }

    public void setTurning(boolean turning) {
        isTurning = turning;
    }

    public boolean isMoving() {
        return isMoving;
    }

    public void setMoving(boolean moving) {
        isMoving = moving;
    }
    //  ===================
    //  getters and setters
    //  ===================

}
