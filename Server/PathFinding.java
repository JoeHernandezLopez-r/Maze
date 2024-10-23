package com.mygdx.game;

import java.util.PriorityQueue;
import java.util.Arrays;


// change later to be 2d arrays ithink it would be better for clarity altho rn idc

class Node {
    int position;
    double cost;

    public Node(int position, double cost) {
        this.position = position;
        this.cost = cost;
    }
}

public class PathFinding {
    // idc about protection 

    /*
        Notes: Default vals
            public int parentDkstra[]; = -1
            public double distanceDkstra[]; = Integer.MAX_VALUE;
            public int parentAstar[]; = -1
            public double gscoreAstar[]; = Double.MAX_VALUE

    */
    public int parentDkstra[];
    public int distanceDkstra[];
    public int parentAstar[];
    public double gscoreAstar[]; 
    public boolean aStarFound;
    public boolean dkstraFound;

    public void positionTOxy(int[] xy, int position, int cols) {
        xy[0] = position/cols; 
        xy[1] = position%cols; 
    }
    
    public int xyTOposition(int x, int y, int cols) {
        int position = (x*cols) + y; 
        return position; 
    }

    public int getNeighborPosition(int currPosition, int xChange, int yChange, int cols) {
        int position = currPosition + (xChange*cols) + yChange; 
        return position; 
    }

    public int getCost(int x, int y, char[][] map2d){
        if(map2d[x][y] == 'X' || map2d[x][y] == 'x') 
            return 1; 
        return 2; 
    }

    public Node findNodeByPosition(PriorityQueue<Node> priorityQueue, int position) {
        for (Node node : priorityQueue) {
            if (node.position == position) 
                return node;
        }
        return null; 
    }

    public void decreasePriority(PriorityQueue<Node> priorityQueue, int position, int newCost) {
        Node node = findNodeByPosition(priorityQueue, position);
    
        if (node != null) {
            priorityQueue.remove(node);
            node.cost = newCost;
            priorityQueue.add(node);
        } else {
            // Handle the case when the node is not found
            System.out.println("Node not found for position: " + position);
        }
    }
    

    public void updateNeighborDikstra(int currPosition, int neighborPosition, int nx, int ny, int[] distance, int[] parent, PriorityQueue<Node> priorityQueue, char[][] map2d){
        // // idk if works check alter to add this in for wall checking 
        // if(map2d[nx][ny] == '-'){
        //     return; 
        // }

        int totalDistance = distance[currPosition] + getCost(nx, ny, map2d); 
        if (totalDistance < distance[neighborPosition]){
            distance[neighborPosition] = totalDistance;
            parent[neighborPosition] = currPosition;
            decreasePriority(priorityQueue, neighborPosition,totalDistance);
        }
    }

    public double hscore(int position, int cols){
        int x = position/cols;
        int y = position%cols;
        int cSquared = (x*x) + (y*y);
        return Math.sqrt(cSquared);
    }

    public void updateNeighborAstar(int currPosition, int neighborPosition, int nx, int ny, double[] gscore, double[] fscore, int[] parent, PriorityQueue<Node> priorityQueue, char[][] map2d, int cols){
        // check if valid move later replace with func as obstacles etc become more complex
        if(map2d[nx][ny] == '-'){
            return; 
        }
        
        double tentativeGscore = gscore[currPosition] + getCost(nx, ny, map2d); 
        if (tentativeGscore < gscore[neighborPosition]){
            gscore[neighborPosition] = tentativeGscore;
            parent[neighborPosition] = currPosition;
            fscore[neighborPosition] = tentativeGscore + hscore(neighborPosition, cols); 

            Node node = findNodeByPosition(priorityQueue, neighborPosition); 
            if(node == null){
                priorityQueue.add(new Node(neighborPosition, fscore[neighborPosition])); 
            }
        }
    }

    public void showAstarPathParents(char[][] map2d, int rows, int cols, int[] parents, int goalX, int goalY){
        int position = xyTOposition(goalX, goalY, cols);
        int[] coordinates = new int[2];

        System.out.println(goalX + " " + goalY); 
        while(parents[position] != -1){
            positionTOxy(coordinates, position, cols);
            map2d[coordinates[0]][coordinates[1]] = '*';
            position = parents[position]; 
        }

        for(int i = 0; i < rows; i++){
            for (int j = 0; j < cols; j++){
                System.out.print(map2d[i][j] + " ");
            }
            System.out.println(); 
        }

        for (int i = 0; i < rows; i++) {
            for(int j=0; j < cols; j++){
                System.out.print(parentAstar[(i*cols) + j] + " ");
            }
            System.out.println(); 
        }
        System.out.println(); 

    }

    // public void showAstarPathParents(char[][] map2d, int rows, int cols, int[] parents, double[] gscore){
        
    // }

    public void dikstra(char[][] map2d, int rows, int cols, int userStartX, int userStartY) {
        // turn inputs to make 0,0 origin bottom left and set position
        int start = xyTOposition(userStartX, userStartY, cols); 
        // init distance and parent and pq
        distanceDkstra = new int[rows*cols]; 
        parentDkstra = new int[rows*cols];
        PriorityQueue<Node> priorityQueue = new PriorityQueue<>((e1, e2) -> Double.compare(e1.cost, e2.cost));

        // set cost of source to 0
        // also wat do to parent it doesnt have one anyway just following wiki 
        distanceDkstra[start] = 0;
        parentDkstra[start] = start; 
        
        // add all to pq 
        for(int i = 0; i < distanceDkstra.length; i++){
            if(i != start){
                distanceDkstra[i] = Integer.MAX_VALUE;
                parentDkstra[i] = -1; 
            }
            priorityQueue.add(new Node(i, distanceDkstra[i]));
        }

        // while pq not empty 
        while(!priorityQueue.isEmpty()){
            // pop min
            Node currNode = priorityQueue.poll();
            int currPosition = currNode.position; 
            int[] coordinates = new int[2];
            positionTOxy(coordinates, currPosition, cols); 
            int x = coordinates[0]; 
            int y = coordinates[1]; 
            int neighborPosition;
            // search each nieghbor

            // check up
            if(y+1 < cols){
                neighborPosition = getNeighborPosition(currPosition, 0, 1, cols); 
                updateNeighborDikstra(currPosition, neighborPosition, x, y+1, distanceDkstra,  parentDkstra, priorityQueue, map2d); 
            }
            // check down
            if(y-1 >= 0){
                neighborPosition = getNeighborPosition(currPosition, 0, -1, cols); 
                updateNeighborDikstra(currPosition, neighborPosition, x, y-1, distanceDkstra, parentDkstra, priorityQueue, map2d); 
            } 
            // check right
            if(x+1 < rows){
                neighborPosition = getNeighborPosition(currPosition, 1, 0, cols); 
                updateNeighborDikstra(currPosition, neighborPosition, x+1, y, distanceDkstra, parentDkstra, priorityQueue, map2d); 
            }
            // check left
            if(x-1 >= 0){
                neighborPosition = getNeighborPosition(currPosition, -1, 0, cols); 
                updateNeighborDikstra(currPosition, neighborPosition, x-1, y, distanceDkstra,  parentDkstra, priorityQueue, map2d);  
            }
        }// end of while loop



    }// dkstra


    /*
        Notes: 
            f(n) = g(n) + h(n)
            fscore: cost of both hueirsitc and gscore
            hueristic: cost of node to end
            gscore: cost of node from start

            fill faster than for loop
    */

    // astar
    void astar(char[][] map2d, int rows, int cols,int userStartX,int userStartY,int userGoalX,int userGoalY){
        aStarFound = false;

        // start + end 
        int start = xyTOposition(userStartX, userStartY , cols); 
        int xgoal = userGoalX;
        int ygoal = userGoalY;

        // init astar scores
        PriorityQueue<Node> priorityQueue = new PriorityQueue<>((e1, e2) -> Double.compare(e1.cost, e2.cost));
        parentAstar = new int[rows*cols];
        gscoreAstar = new double[rows*cols];
        double[] fscore = new double[rows*cols];

        Arrays.fill(parentAstar, -1);

        Arrays.fill(gscoreAstar, Double.MAX_VALUE);
        gscoreAstar[start] = 0; 

        Arrays.fill(fscore, Double.MAX_VALUE);
        fscore[start] = hscore(start, cols); 

        priorityQueue.add(new Node(start, fscore[start])); 

        // a star memoing algorithm
        while(!priorityQueue.isEmpty()) {
            Node currNode = priorityQueue.poll();
            int currPosition = currNode.position; 
            int[] coordinates = new int[2];
            positionTOxy(coordinates, currPosition, cols); 

            if(coordinates[0] == xgoal && coordinates[1] == ygoal){
                // System.out.println("Found coordinates");
                aStarFound = true;

                // showAstarPathParents(map2d, rows, cols, parentAstar, xgoal, ygoal);
                
                return; 
            }

            // FOR EACH NIEGHBOR
            int x = coordinates[0]; 
            int y = coordinates[1]; 
            int neighborPosition;

            // check up
            if(y+1 < cols){
                neighborPosition = getNeighborPosition(currPosition, 0, 1, cols); 
                updateNeighborAstar(currPosition, neighborPosition, x, y+1, gscoreAstar, fscore,  parentAstar, priorityQueue, map2d, cols); 
            }
            // check down
            if(y-1 >= 0){
                neighborPosition = getNeighborPosition(currPosition, 0, -1, cols); 
                updateNeighborAstar(currPosition, neighborPosition, x, y-1, gscoreAstar, fscore,  parentAstar, priorityQueue, map2d, cols); 
            } 
            // check right
            if(x+1 < rows){
                neighborPosition = getNeighborPosition(currPosition, 1, 0, cols); 
                updateNeighborAstar(currPosition, neighborPosition, x+1, y, gscoreAstar, fscore,  parentAstar, priorityQueue, map2d, cols); 
            }
            // check left
            if(x-1 >= 0){
                neighborPosition = getNeighborPosition(currPosition, -1, 0, cols); 
                updateNeighborAstar(currPosition, neighborPosition, x-1, y, gscoreAstar, fscore,  parentAstar, priorityQueue, map2d, cols); 
            }
        }// end of while loop


    }
}
