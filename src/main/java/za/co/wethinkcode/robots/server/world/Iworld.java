// # Interface for the world
package za.co.wethinkcode.robots.server.world;
import za.co.wethinkcode.robots.models.ServerResponse;
import java.util.List;

public interface Iworld {

    /**
     * These are the possible results when a robot tries to move.
     */
    enum Direction {
        NORTH, SOUTH, EAST, WEST
    }

    enum UpdateResponse {
        SUCCESS,            // The move worked fine
        HIT_ROBOT,          // Crashed into another player
        HIT_OBSTACLE,       // Crashed into a mountain/lake
        FELL_IN_PIT,        // Robot is dead (fell in a pit)
        OUT_OF_BOUNDS       // Hit the edge of the world
    }

    // Robot Management

    /**
     * Try to put a new robot in the world.
     * Returns true if it worked, false if name is taken or world is full.
     */
    boolean addRobot(String name);

    /**
     * Kick a robot out of the world (e.g., if they quit or die).
     */
    void removeRobot(String name);


    // Movement & Physics

    /**
     * Move a robot forward or backward by X steps.
     * This needs to check the path for obstacles and the world edge.
     */
    UpdateResponse moveRobot(String name, int steps);

    /**
     * Turn the robot 90 degrees left or right.
     */
    void rotateRobot(String name, boolean turnRight);

    /**
     * Check if a specific coordinate is currently blocked by an obstacle.
     */
    boolean isPositionBlocked(int x, int y);

    /**
     * Check if a coordinate is a bottomless pit.
     */
    boolean isPositionInPit(int x, int y);


    //  Combat & Looking

    /**
     * Returns a list of things a robot can see (Robots, Obstacles, or Edges).
     * Only looks in straight lines up to the 'visibility' distance.
     */
    List<Object> look(String name);

    /**
     * Checks if a bullet hit anyone.
     * You give it the starting point, direction, and how far the gun fires.
     */
    boolean checkHit(String shooterName, int bulletDistance);


    // World Info (For the 'dump' command and Config)

    /**
     * Returns the max X and Y limits so we know where the edges are.
     * Remember: (0,0) is the center!
     */
    int getWidth();

    int getHeight();

    /**
     * Gets the current (x, y) and direction for a robot to send back to the client.
     */
    String getRobotState(String name);

    /**
     * Returns all the obstacles in the world (for the server console).
     */
    List<Object> getObstacles();

    /**
     * Executes a command within the world context.
     */
    ServerResponse perform(za.co.wethinkcode.robots.server.commands.Command command);
}