// # Interface for the world
package za.co.wethinkcode.robots.server.world;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import za.co.wethinkcode.robots.models.Position;
import za.co.wethinkcode.robots.models.ServerResponse;
import za.co.wethinkcode.robots.models.impediment.Impediments;
import za.co.wethinkcode.robots.server.commands.Command;
import za.co.wethinkcode.robots.server.robot.BaseRobot;

public interface Iworld {
    public final static float reloadTime=5f;
    public final static float repairTime=3f;
    public final static int visibleDistance=3;
    public final static int MAG_MAX=5;
    public final static int bulletRange=5;
    public final static int lookRange=bulletRange;
    
   

    
    public Map<String,BaseRobot> getAllRobots();
    
  

    // Robot Management

    /**
     * Try to put a new robot in the world.
     * Returns true if it worked, false if name is taken or world is full.
     */

    abstract BaseRobot getFireable(BaseRobot rbt);

    boolean addRobot(String name);
    boolean moveRobot(String name,int step);
    /**
     * Kick a robot out of the world (e.g., if they quit or die).
     */
    void removeRobot(String name);
    boolean isPositionBlocked(int x, int y);
    
    void loadMap(ArrayList<ArrayList<Impediments>> map );
    // Movement & Physics

    /**
     * Move a robot forward or backward by X steps.
     * This needs to check the path for obstacles and the world edge.
     */
    boolean moveRobot(String name, Position IntendedPosition);

    /**
     * Turn the robot 90 degrees left or right.
     */
    void rotateRobot(String name, boolean turnRight);

    /**
     * Check if a specific coordinate is currently blocked by an obstacle.
     */
    boolean isPositionAvailable(Position intendedPos);

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

   abstract Position newSpawnPoint();

    /**
     * Returns all the obstacles in the world (for the server console).
     */
    List<Object> getObstacles();

    /**
     * Executes a command within the world context.
     */
    ServerResponse perform(za.co.wethinkcode.robots.server.commands.Command command);
}