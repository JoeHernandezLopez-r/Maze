package com.mygdx.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class EnemyBasic {
    public enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    private float x;
    private float y;
    private float velocity = 50f; 
    
    private Direction facingDirection = Direction.DOWN;
    private boolean isTurning;
    private boolean isMoving; 

    private float targetX; 
    private float targetY; 
    private float oldX; 
    private float oldY; 

    private float tweenTime = .05f;

    private float tileW;
    private float tileH; 

    private Texture blockTexture; 
    private SpriteBatch batch; 

    public EnemyBasic(float inputX, float inputY, SpriteBatch batch, float tileSize) {
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

    // note this works only for dkstra sep func later for Astar or pass a flag makes more sense imo
    public void update(float delta, char[][] tilemap, int[] parentsPath, boolean isFound) {
        if(!isFound){
            return; 
        }
        if(!isMoving()){
            // get parent from enemy xy pos
            int cols = tilemap[0].length; 
            // oldX and oldY are floats btu always ints too lazy change so cast to int instead
            int nextPos = parentsPath[(((int)oldX)*cols) + ((int)oldY)]; 

            // a star position end for dkstra its diff 
            if(nextPos == -1){
                return; 
            }

            // get nextPos x and y
            int nextXpos = nextPos/cols; 
            int nextYpos = nextPos%cols; 

            if ((nextYpos - oldY) == 1) {
                if(facingDirection != Direction.UP){
                    isTurning = true;
                }
                else{
                    isTurning = false;
                }
                facingDirection = Direction.UP;
                isMoving = true;
            }
            if ((nextYpos - oldY) == -1) {
                if(facingDirection != Direction.DOWN){
                    isTurning = true;
                }
                else{
                    isTurning = false;
                }
                facingDirection = Direction.DOWN;
                isMoving = true;
            }
            if ((nextXpos - oldX) == 1) {
                if(facingDirection != Direction.RIGHT){
                    isTurning = true;
                }
                else{
                    isTurning = false;
                }
                facingDirection = Direction.RIGHT;
                isMoving = true;
            }
            if ((nextXpos - oldX) == -1) {
                if(facingDirection != Direction.LEFT){
                    isTurning = true;
                }
                else{
                    isTurning = false;
                }
                facingDirection = Direction.LEFT;
                isMoving = true;
            }

            // if same position 
            if ((nextXpos - oldX) == 0 && (nextYpos - oldY) == 0) {
                return; 
            }

        }


        // move 
        if(isMoving){
            if(isTurning){
                // maybe add animation for turning or a pause that it cant move idk for now false
                isTurning = false;
            }
            else{
                // =================================================================================================
                // TECHINCALY SPEAKING THE BOUND CHECK PLUYS FACING ISVALID NOT REQUIRED MIGHT YEET OUT LATER 
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

                // moaybe only remove above and keep below so that certiain tiles cannnot be entered makes more sense and would cleaner look ing 
                // so shal only remove top  portion on second thought keep so that the math looks cleaner rather than condense it all up on the
                // sim input portion it will look uglier imo even if it saves like five lines /_\

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

                // END OF THE PART THAT ALOT OF LOGIC COULD BE REMOVED
                //  =================================================
            }// else not turning 
        }// if moving 
    }
    

    private float interpolate(float start, float end, float percent) {
        return start + percent * (end - start);
    }

//  techincally doesnt need to exist cause it will follow path but who nkows might need it for later _/\_
    private boolean isValidMovement(float nextX, float nextY, char[][] tilemap) {
        //  so just use .length() idk if its much slower than passing around but for now to just remember i can 
        if(nextX < 0 || nextX >= tilemap.length){
            return false;
        }

        if(nextY < 0 || nextY >= tilemap[0].length){
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
