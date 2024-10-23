package com.mygdx.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


/*
    Not3s: 
        The map has changed so that the origin 0, 0 from the input file is 
        also the [0][0] of the array and vice versa. 
*/

public class FloorMap {
    private char[][] data;
    private char[][] initialData;
    private int maxRows = 25;
    private int maxColumns = 25;
    public int rows;
    public int cols;
    public int xMax;
    public int yMax;

    private Texture blueTileTexture;

    public FloorMap() {
        blueTileTexture = new Texture("badlogic.jpg");
    }

    public void initMap(String filePath) {
        initialData = new char[maxRows][maxColumns]; 

        try {
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String line;
            int charsRead;
            rows = 0;
            cols = 0;

            // parse first line to know how many columns
            line = br.readLine();
            for (int i = 0; i < line.length(); i++) {
                char currentChar = line.charAt(i);
                if (currentChar == '\r' || currentChar == '\n' || currentChar == ' ')
                    continue;
                initialData[rows][cols] = currentChar;
                cols++;
                System.out.print(currentChar + " ");
            }
            rows++;
            System.out.println();

            // now parse the rest of the rows
            int index = 0;
            while ((line = br.readLine()) != null) {
                for (int i = 0; i < line.length(); i++) {
                    char currentChar = line.charAt(i);
                    if (currentChar == '\r' || currentChar == '\n' || currentChar == ' ')
                        continue;
                    initialData[rows][index] = currentChar;
                    index++;
                    System.out.print(currentChar + " ");
                }
                rows++;
                index = 0;
                System.out.println();
            }

            System.out.println("rows: " + rows);
            System.out.println("columns: " + cols + "\n");
            System.out.println("now printing arrays");

            br.close();

            // create actual map with origin at bottom aka transform table 
            xMax = cols; 
            yMax = rows; 
            data = new char[xMax][yMax];

            for(int i = 0; i < xMax; i++){
                for(int j = 0; j < yMax; j++){
                    data[i][j] = initialData[rows-j-1][i]; 
                }
            }

             
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getMapXMAX(){
        return cols;
    }
    public int getMapYMAX(){
        return rows;
    }

    public char[][] getData(){
        return data;
    }

    public void draw(SpriteBatch batch, int tileSize) {
        for (int i = 0; i<xMax ; i++) {
            for (int j = 0; j < yMax; j++) {
                if (data[i][j] == '-') {
                    batch.draw(blueTileTexture, i * tileSize, j * tileSize, tileSize, tileSize);
                }
            }
        }
    }

    // testing main won't run normally with this will be called in mainscreen or summit idk for now let it be
    //  to run just the files for pathfinding do this 
    //  javac com/mygdx/game/FloorMap.java com/mygdx/game/PathFinding.java
    // java com.mygdx.game.FloorMap
    // public static void main(String[] args) {
    //     FloorMap map = new FloorMap();
    //     String filePath = "./maps/map1";
    //     map.initMap(filePath);
    //     PathFinding pathFinder = new PathFinding();
    //     // pathFinder.dikstra(map.data, map.rows, map.cols);
    //     pathFinder.astar(map.data, map.rows, map.cols);

    // }// end of function main
}// end of class
