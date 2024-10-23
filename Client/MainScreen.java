package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.InputProcessor;

import java.net.Socket;
import java.io.IOException;
import java.io.*;

import org.json.*; 


public class MainScreen implements Screen, InputProcessor {

    private final MyGdxGame game;
    private BatchManager batchManager;
    private SpriteBatch spriteBatch;
    private NumberTile[][] numberTiles;
    private NumberTile numbertile; 
    private int cols;
    private int rows;

    private int xMax;
    private int yMax;

    private FloorMap floormap; 
    private ShapeRenderer shapeRenderer;

    private int tileSize;
    PathFinding pathFinder;
    private Player player; 
    private EnemyBasic enemy; 

    private Socket clientSocket;
    private BufferedReader br;
    private Player[] players; 

    private float timeSinceLastUpdate = 0f;
    private final float updateInterval = 0.1f;
    private clientId = -1; 
    
    public MainScreen(MyGdxGame game, BatchManager batchManager) {
        this.game = game;
        this.batchManager = batchManager;
        batchManager.initialize();

        this.spriteBatch = batchManager.getBatch();
        this.shapeRenderer = new ShapeRenderer();
        

        // temp give a max size of 20 later maybe make it dynamic or limit players in server 
        players = new Player[20]; 

        pathFinder = new PathFinding();

        floormap = new FloorMap();
        String filePath = "./maps/map1";
        floormap.initMap(filePath);

        xMax = floormap.getMapXMAX(); 
        yMax = floormap.getMapYMAX();
        tileSize = Gdx.graphics.getHeight()/yMax;

        numberTiles = new NumberTile[xMax][yMax]; 
        for(int i = 0; i < xMax; i++){
            for(int j = 0; j < yMax; j++){
                numberTiles[i][j] = new NumberTile(i, j, tileSize);
            }
        }

//      ===========
//      CLIENT SIDE
//      =====================================
        System.out.println("Client started");

        // Start the client connection in a new thread
        new Thread(() -> {
            String ipAddress = "localhost";
            int port = 9806;
            try {
                clientSocket = new Socket(ipAddress, port);
                br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                 // Receive the initial ID from the server
                 String idResponse = br.readLine();
                 JSONObject idJson = new JSONObject(idResponse);
 
                 if (idJson.has("socketId")) {
                     clientId = idJson.getInt("socketId");
                     System.out.println("Connected to server with client ID: " + clientId);
                 }

                // Start a new thread to listen for server updates
                new Thread(this::listenForServerUpdates).start();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
        // END OF CLIENT SIDE
        // ==================

        player = new Player(0, 0, spriteBatch, tileSize);
        enemy = new EnemyBasic(3, 4, spriteBatch, tileSize);
    }

    public void listenForServerUpdates() {
        try {
            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            
            while (true) { // Continuously listen for updates
                jsonBuilder.setLength(0); // Clear the builder for each new message
                while ((line = br.readLine()) != null) {
                    if (line.isEmpty()) {
                        break; // End of JSON message
                    }
                    jsonBuilder.append(line);
                }
                
                String jsonString = jsonBuilder.toString();
                processServerUpdate(jsonString); // Handle the received JSON update
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void processServerUpdate(String jsonString) {
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
    
            // Handling newly connected players
            if (jsonObject.has("NewlyConnected")) {
                JSONObject newlyConnected = jsonObject.getJSONObject("NewlyConnected");
    
                for (String key : newlyConnected.keySet()) {
                    int playerId = Integer.parseInt(key);
    
                    if (playerId < players.length) {
                        JSONArray playerInfo = newlyConnected.getJSONArray(key);
                        int hp = playerInfo.getJSONObject(0).getInt("hp");
    
                        // Initialize and add the new player to the players array
                        players[playerId] = new Player(0, 0, spriteBatch, tileSize);
                        players[playerId].setHp(hp);
    
                        // Log the new player's HP
                        System.out.println("Player " + playerId + " connected with HP: " + hp);
                    }
                }
            }
    
            // Handling currently connected players
            if (jsonObject.has("Connected")) {
                JSONObject connected = jsonObject.getJSONObject("Connected");
    
                for (String key : connected.keySet()) {
                    int playerId = Integer.parseInt(key);
    
                    if (playerId < players.length && players[playerId] != null) {
                        JSONArray playerInfo = connected.getJSONArray(key);
                        int hp = playerInfo.getJSONObject(0).getInt("hp");
    
                        // Update player HP
                        players[playerId].setHp(hp);
    
                        // Log the player's HP
                        System.out.println("Player " + playerId + " is connected with HP: " + hp);
                    }
                }
            }
    
            // Handling player disconnections
            if (jsonObject.has("Disconnected")) {
                JSONArray disconnected = jsonObject.getJSONArray("Disconnected");
    
                for (int i = 0; i < disconnected.length(); i++) {
                    int playerId = disconnected.getInt(i);
    
                    if (playerId < players.length && players[playerId] != null) {
                        // Remove the player from the players array
                        players[playerId] = null;
    
                        // Log the player's disconnection
                        System.out.println("Player " + playerId + " disconnected.");
                    }
                }
            }
    
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void show() {
        Gdx.input.setInputProcessor(this);
    }


    private void handleInput() {

        // Exit application when "I" key is pressed
        if (Gdx.input.isKeyPressed(Input.Keys.I)) {
            dispose(); // Clean up resources
            Gdx.app.exit(); // Exit the application
        }

        // player movement 
        if(!player.isMoving()){
            if (Gdx.input.isKeyPressed(Input.Keys.W) ) {
                if(player.getFacingDirection() != Player.Direction.UP){
                    player.setTurning(true);
                }
                else{
                    player.setTurning(false);
                }
                player.setFacingDirection(Player.Direction.UP);
                player.setMoving(true);
            }
            if (Gdx.input.isKeyPressed(Input.Keys.S)  ) {
                if(player.getFacingDirection() != Player.Direction.DOWN){
                    player.setTurning(true);
                }
                else{
                    player.setTurning(false);
                }
                player.setFacingDirection(Player.Direction.DOWN);
                player.setMoving(true);
            }
            if (Gdx.input.isKeyPressed(Input.Keys.A) ) {
                if(player.getFacingDirection() != Player.Direction.LEFT){
                    player.setTurning(true);
                }
                else{
                    player.setTurning(false);
                }
                player.setFacingDirection(Player.Direction.LEFT);
                player.setMoving(true);
            }
            if (Gdx.input.isKeyPressed(Input.Keys.D)  ) {
                if(player.getFacingDirection() != Player.Direction.RIGHT){
                    player.setTurning(true);
                }
                else{
                    player.setTurning(false);
                }
                player.setFacingDirection(Player.Direction.RIGHT);
                player.setMoving(true);
            }
        }
    }
    void useDkstra(){
        pathFinder.dikstra(floormap.getData(), floormap.getMapXMAX(), floormap.getMapYMAX(), player.getOldX(), player.getOldY());
        for(int i = 0; i < xMax; i++) {
            for(int j = 0; j < yMax; j++) {
                int val = pathFinder.parentDkstra[(i*yMax) + j]; 
                if(val!= -2 && val != -1){// could be bug check later
                    numberTiles[i][j].update(val, true); 
                } 
                else{
                    numberTiles[i][j].update(-2, false); 
                }
            }

        }
        // for(int i = 0; i < xMax; i++) {
        //     for(int j = 0; j < yMax; j++) {
        //         int val = pathFinder.distanceDkstra[(i*yMax) + j]; 
        //         if(val!= Integer.MAX_VALUE){
        //             numberTiles[i][j].update(val, true); 
        //         } 
        //         else{
        //             numberTiles[i][j].update(Integer.MAX_VALUE, false); 
        //         }
        //     }
        // }
    }


// carefull note because the path uses the parent to up what the goal and start are flipped, could reverse list but this is easier trust; 
    void useAstar(){
        pathFinder.astar(floormap.getData(), xMax, yMax, player.getOldX(), player.getOldY(),  enemy.getOldX(), enemy.getOldY());
        for(int i = 0; i < xMax; i++) {
            for(int j = 0; j < yMax; j++) {
                int val = pathFinder.parentAstar[(i*yMax) + j]; 
                if(val != -1 && val != 2){
                    numberTiles[i][j].update(val, true); 
                } 
                else{
                    numberTiles[i][j].update(-2, false); 
                }
            }
        }

        // for(int i = 0; i < xMax; i++) {
        //     for(int j = 0; j < yMax; j++) {
        //         double val = pathFinder.gscoreAstar[(i*yMax) + j]; 
        //         if(val!= Double.MAX_VALUE){
        //             numberTiles[i][j].update(val, true); 
        //         } 
        //         else{
        //             numberTiles[i][j].update(-1, false); 
        //         }
        //     }
        // }
    }

    private void update(float delta) {
        handleInput();
        // useDkstra();
        useAstar();
        player.update(delta, floormap.getData());
        // enemy.update(delta, floormap.getData(), pathFinder.parentAstar, pathFinder.aStarFound);

        timeSinceLastUpdate += delta;
        if (timeSinceLastUpdate >= updateInterval) {
            sendPlayerPosition();
            timeSinceLastUpdate = 0f;
        }

    }  

    private void sendPlayerPosition() {
        if (clientSocket != null && !clientSocket.isClosed()) {
            try {
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                JSONObject positionUpdate = new JSONObject();
                positionUpdate.put("playerId", 0); // Assuming playerId is 0, change if needed
                positionUpdate.put("x", player.getX());
                positionUpdate.put("y", player.getY());
                
                out.println(positionUpdate.toString());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
 
        // draw grid for debuggin purposes
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        for (float x = 0; x <= Gdx.graphics.getWidth(); x += tileSize) {
            shapeRenderer.line(x, 0, x, Gdx.graphics.getHeight());
        }
        for (float y = 0; y <= Gdx.graphics.getHeight(); y += tileSize) {
            shapeRenderer.line(0, y, Gdx.graphics.getWidth(), y);
        }
        shapeRenderer.end();
        // =================

        spriteBatch.begin();

        floormap.draw(spriteBatch, tileSize); 
        numberTilesRender(); 
        player.render();
        enemy.render();
        spriteBatch.end();

        update(delta);
    }// end of render
//  =================

    public void numberTilesRender(){
        for(int i = 0; i < xMax; i++){
            for(int j = 0; j < yMax; j++){
                numberTiles[i][j].draw(spriteBatch);
            }
        }
    }
    @Override
    public void resize(int width, int height) {
        // Handle screen resizing
    }

    @Override
    public void pause() {
        // Handle game pause, if needed
    }

    @Override
    public void resume() {
        // Handle game resume, if needed
    }

    @Override
    public void hide() {
        // This method is called when the screen is no longer the current screen
    }

    @Override
    public void dispose() {
        // Notify server of disconnect
        if (clientSocket != null && !clientSocket.isClosed()) {
            try {
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                out.println("DISCONNECT"); // Send a disconnect message
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            // Close client socket
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        // Cleanup resources
        shapeRenderer.dispose();
    }
    

// =========================================
// NEEDED TO IMPLEMENT THE INPUT PROCCESSOR 
    @Override
    public boolean keyDown(int keycode) {
        // Handle key down if needed
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        // Handle key up if needed
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        // Handle key typed if needed
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        // Handle touch down if needed
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        // Handle touch up if needed
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        // Handle touch dragged if needed
        return false;
    }

    public boolean touchMoved(int screenX, int screenY) {
        // Handle touch moved if needed
        return false;
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        // Handle touch cancelled if needed
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        // Handle mouse movement if needed
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        // Handle scrolling if needed
        return false;
    }
//  =======================
//  END OF INPUT PROC STUFF
//  =======================

}
