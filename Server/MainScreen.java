package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.InputProcessor;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;

import java.io.*;

import org.json.*; 

import java.util.ArrayList;
import java.util.List;

import java.util.Iterator;

public class MainScreen implements Screen {

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

    private ServerSocket ss; 

    private List<PrintWriter> clientWriters; 

    private JSONObject jsonResponse; 

    private float timer; 


    public MainScreen(MyGdxGame game, BatchManager batchManager) {
        // init game world        
        this.game = game;
        this.batchManager = batchManager;
        batchManager.initialize();

        this.spriteBatch = batchManager.getBatch();
        this.shapeRenderer = new ShapeRenderer();

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
//      SERVER CODE
//      ===============
        int port = 9806; 
        clientWriters = new ArrayList<>();
        jsonResponse = new JSONObject(); 
        timer =0; 

        // need to init entries for jsonResponse
        JSONObject newlyConnected = new JSONObject();
        JSONObject NewlyDisconnected = new JSONObject();
        JSONObject connected = new JSONObject();
        jsonResponse.put("Connected", connected);
        jsonResponse.put("NewlyConnected", newlyConnected);
        jsonResponse.put("NewlyDisconnected", NewlyDisconnected);

        // Start the server in a new thread
        new Thread(() -> {
            try {
                ss = new ServerSocket(port);
                while (true) {
                    Socket s = ss.accept();
                    handleClient(s);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        // END OF CREATING SERVER 
        // ======================
    }

    private void handleClient(Socket socket) {
        new Thread(() -> {
            try {
                InetSocketAddress inetSocketAddress = (InetSocketAddress) socket.getRemoteSocketAddress();
                int socketId = inetSocketAddress.getPort();
                System.out.println("Client connected: " + socketId);
    
                // Initialize client info
                JSONArray newClientInfo = new JSONArray(); 
                JSONObject clientDetails = new JSONObject();
                clientDetails.put("hp", 20);
                newClientInfo.put(clientDetails);
    
                synchronized (jsonResponse) {
                    jsonResponse.getJSONObject("Connected").put(String.valueOf(socketId), newClientInfo);
                    jsonResponse.getJSONObject("NewlyConnected").put(String.valueOf(socketId), newClientInfo);
                }
    
                // Create PrintWriter for this client and add to list
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                synchronized (clientWriters) {
                    clientWriters.add(out);
                }
    
                // Send the socket ID to the client
                JSONObject socketIdResponse = new JSONObject();
                socketIdResponse.put("socketId", socketId);
                out.println(socketIdResponse.toString());
    
                // Handle communication with client
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    if (inputLine.equals("DISCONNECT")) {
                        handleClientDisconnect(socket);
                        break;
                    }
                    System.out.println("Received from client: " + inputLine);
                }
    
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                handleClientDisconnect(socket);
            }
        }).start();
    }
    
    
    private void handleClientDisconnect(Socket socket) {
        try {
            InetSocketAddress inetSocketAddress = (InetSocketAddress) socket.getRemoteSocketAddress();
            int socketId = inetSocketAddress.getPort();
    
            System.out.println("Handling disconnect for client: " + socketId);
    
            synchronized (jsonResponse) {
                if (jsonResponse.getJSONObject("Connected").has(String.valueOf(socketId))) {
                    JSONArray disconnectedClientInfo = jsonResponse.getJSONObject("Connected").getJSONArray(String.valueOf(socketId));
                    jsonResponse.getJSONObject("Connected").remove(String.valueOf(socketId));
                    jsonResponse.getJSONObject("NewlyDisconnected").put(String.valueOf(socketId), disconnectedClientInfo);
                }
            }
    
            synchronized (clientWriters) {
                clientWriters.removeIf(writer -> {
                    try {
                        writer.println(); 
                        return false;
                    } catch (Exception e) {
                        System.out.println("Removing invalid writer: " + e.getMessage());
                        return true;
                    }
                });
            }
    
            socket.close();
            System.out.println("Client disconnected and socket closed: " + socketId);
    
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    


    @Override
    public void show() {
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
        // useDkstra();
        // useAstar();
        // player.update(delta, floormap.getData());
        // enemy.update(delta, floormap.getData(), pathFinder.parentAstar, pathFinder.aStarFound);
    }  

    @Override
    public void render(float delta) {
        // System.out.println(timer);
        timer += delta;

        if(timer >= 1.0f){
            timer = 0;  
            updateServer(); 
        }

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

        // floormap.draw(spriteBatch, tileSize); 
        // numberTilesRender(); 
        // player.render();
        // enemy.render();
        spriteBatch.end();



        update(delta);
    }// end of render
//  =================

    public void updateServer() {
        System.out.println("updateServer");
        String jsonString;

        synchronized (jsonResponse) {
            jsonString = jsonResponse.toString() + "\n";
        }

        synchronized (clientWriters) {
            for (PrintWriter writer : clientWriters) {
                try {
                    writer.println(jsonString);
                } catch (Exception e) {
                    e.printStackTrace();
                    clientWriters.remove(writer);
                }
            }
        }

        synchronized (jsonResponse) {
            JSONObject newlyConnected = jsonResponse.getJSONObject("NewlyConnected");
            JSONObject newlyDisconnected = jsonResponse.getJSONObject("NewlyDisconnected");

            // Use iterators to safely remove entries
            Iterator<String> connectedKeys = newlyConnected.keys();
            while (connectedKeys.hasNext()) {
                connectedKeys.next();
                connectedKeys.remove();
            }

            Iterator<String> disconnectedKeys = newlyDisconnected.keys();
            while (disconnectedKeys.hasNext()) {
                disconnectedKeys.next();
                disconnectedKeys.remove();
            }
        }
    }

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
        // Cleanup resources here
        shapeRenderer.dispose();
        if (ss != null && !ss.isClosed()) {
            try {
                ss.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // later close other sockets as well 
    }

// =========================================
// NEEDED TO IMPLEMENT THE INPUT PROCCESSOR 
   
//  =======================
//  END OF INPUT PROC STUFF
//  =======================

}
