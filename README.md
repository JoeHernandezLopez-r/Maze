# Maze

This libgdx gradle project only has the src files to make it less bloated. The project itself consists of 
    - Server which runs first and listens for clients
    - Clients which enable user to control their character
    - Connection with sockets and json is used to communicate between the server and the client(s)
    - Server keeps track of all clients connection
    - Clients are told what other cliets joins and leave 
    - Pathfinding Astar and Dijkstra's implementation 
    - Collision detection 
    - Map which can be generated with text file "x" is floor and "-" is a wall must be a rectangle


While the server shares across all clients who join and leave, it does not track their position(s),and because of this, the enemy does not know who to target. Tracking a player's position could be implemented by using the sockets id as IDs, which the server already keeps track of to know what to free when a client exits and only requires the that the clients position be added in what information which is sent to server and that is then added to the announce all players. For choosing who to target the server could choose the closest player and if it is several enemies it would simply be best to make an array of those enemies and iterate through them and then see what player is closest to them for a simple enemy AI using pathfinding which is already implemented. 

The bulk of the boilerplate is done, and I will continue with another project in C++ similar to this but learn raycasting. Due to this, I will not proceed to finetune this and leave the project as is. This project is mainly a demo to understand better how games and networking work. My goal with the C++ is 2.5d implementaion and adding more mechanics and detail essentially building up from my knowledge of this project. 