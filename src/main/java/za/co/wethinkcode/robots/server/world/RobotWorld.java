// # Implementation of the 2D world logic

package za.co.wethinkcode.robots.server.world;
import za.co.wethinkcode.robots.models.Directions;
import za.co.wethinkcode.robots.models.Position;
import za.co.wethinkcode.robots.models.ServerResponse;
import za.co.wethinkcode.robots.models.impediment.Impediments;
import za.co.wethinkcode.robots.models.impediment.Obstacle;
import za.co.wethinkcode.robots.server.commands.Command;
import za.co.wethinkcode.robots.server.commands.ErrorCommand;
import za.co.wethinkcode.robots.server.robot.BaseRobot;

import java.util.*;

public class RobotWorld implements Iworld {

    private final int width;
    private final int height;
    private final int visibility;
    private final List<Obstacle> obstacles;
    private ArrayList<ArrayList<Impediments>> map;
    private ArrayList<Position> emptySpots;
    private final Map<String, BaseRobot> robots = new HashMap<>();

    public RobotWorld(int width, int height, int visibility) {
        this.width = width;
        this.height = height;
        this.visibility = visibility;
        this.obstacles= new ArrayList<Obstacle>();
        
    }

    public RobotWorld() {
        this(10,10,7);
        this.emptySpots=new ArrayList<>();
    }
    public void loadMap(ArrayList<ArrayList<Impediments>> map ){
        
        this.map=map;
    }
    public void setEmptySlots(ArrayList<Position> empties){
        this.emptySpots=empties;
    }
    @Override
    public boolean moveRobot(String name, int steps) {
        BaseRobot robot = robots.get(name);
        Position currentPos = robot.getPosition();
        Directions dir = robot.getDirection();

        if (currentPos == null || dir == null) return false;

        int multiplier = (steps > 0) ? 1 : -1;
        int nextX = currentPos.getX();
        int nextY = currentPos.getY();

        // IMPORTANT: We check every single klik (step)
        for (int i = 1; i <= Math.abs(steps); i++) {
            int stepX = nextX;
            int stepY = nextY;

            // EXACT MATH: North is +Y, South is -Y, East is +X, West is -X
            if (dir == Directions.NORTH) stepY += multiplier;
            else if (dir == Directions.SOUTH) stepY -= multiplier;
            else if (dir == Directions.EAST) stepX += multiplier;
            else if (dir == Directions.WEST) stepX -= multiplier;

            // Updated Boundary check for an odd-sized world
            int xLimit = (width - 1) / 2;
            int yLimit = (height - 1) / 2;

            if (stepX > xLimit || stepX < -xLimit || stepY > yLimit || stepY < -yLimit) {
                return false;
            }

            // OBSTACLE MATH: Check if this klik is inside a mountain or lake
            if (isPositionBlocked(stepX, stepY)) {
                return false;
            }

            // PIT MATH: If they hit a pit, they are removed from the world
            if (isPositionInPit(stepX, stepY)) {
                removeRobot(name);
                return true;
            }

            nextX = stepX;
            nextY = stepY;
        }
        Position newPosition = new Position(nextX, nextY);
        robot.updatePosition(newPosition);
        updateRobot(name, robot);
        return true;
    }

    @Override
    public List<Object> look(String name) {
        List<Object> results = new ArrayList<>();
        BaseRobot robot = this.robots.get(name);

        Position pos = robot.getPosition();
        if (pos == null) return results;

        // SPEC: Robot only sees in straight lines (N, S, E, W)
        for (Directions lookDir : Directions.values()) {
            for (int dist = 1; dist <= visibility; dist++) {
                int lookX = pos.getX();
                int lookY = pos.getY();

                if (lookDir == Directions.NORTH) lookY += dist;
                else if (lookDir == Directions.SOUTH) lookY -= dist;
                else if (lookDir == Directions.EAST) lookX += dist;
                else if (lookDir == Directions.WEST) lookX -= dist;

                // Check for Edges first (using the odd rectangle math)
                if (Math.abs(lookX) > (width - 1) / 2 || Math.abs(lookY) > (height - 1) / 2) {
                    results.add(formatSeen("EDGE", lookDir, dist));
                    break;
                }

                // Check for Obstacles
                boolean blockedView = false;
                for (Obstacle obs : obstacles) {
                    if (obs.isAt(lookX, lookY)) {
                        results.add(formatSeen(obs.getType(), lookDir, dist));
                        // MOUNTAIN blocks view, others don't
                        if (obs.getType().equals("MOUNTAIN")) blockedView = true;
                    }
                }
                if (blockedView) break;
            }
        }
        return results;
    }

    // Helper method to fix the "Cannot resolve method formatSeen" error
    private Map<String, Object> formatSeen(String type, Directions dir, int dist) {
        Map<String, Object> map = new HashMap<>();
        map.put("direction", dir.toString());
        map.put("type", type);
        map.put("distance", dist);
        return map;
    }

    @Override
    public boolean isPositionBlocked(int x, int y) {
        for (Obstacle obs : obstacles) {
            if (!obs.getType().equals("PIT") && obs.isAt(x, y)) return true;
        }
        return false;
    }

    @Override
    public boolean isPositionInPit(int x, int y) {
        for (Obstacle obs : obstacles) {
            if (obs.getType().equals("PIT") && obs.isAt(x, y)) return true;
        }
        return false;
    }

    // OTHER INTERFACE METHODS
    @Override
     public boolean addRobot(String name) {
        if (robots.containsKey(name)) return false;
        BaseRobot robot = BaseRobot.Builder(name, 0, 0, 3, 2);
        robots.put(name, robot);
    
        return true;
    }

    @Override
     public void removeRobot(String name) {
        robots.remove(name);

    }

    @Override 
    public void rotateRobot(String name, boolean turnRight) {
        BaseRobot robot =robots.get(name);
        Directions current = robot.getDirection();
        if (current == null) return;

        Directions next;
        if (turnRight) {
            next = (current == Directions.NORTH) ? Directions.EAST :
                    (current == Directions.EAST) ? Directions.SOUTH :
                    (current == Directions.SOUTH) ? Directions.WEST : Directions.NORTH;
        } else {
            next = (current == Directions.NORTH) ? Directions.WEST :
                    (current == Directions.WEST) ? Directions.SOUTH :
                    (current == Directions.SOUTH) ? Directions.EAST : Directions.NORTH;
        }
        robot.updateDirection(next);
        updateRobot(name, robot);
    }
    private void updateRobot(String name,BaseRobot robot){
        this.robots.put(name, robot);

    }

    @Override public int getWidth() { return width; }
    @Override public int getHeight() { return height; }
    @Override public List<Object> getObstacles() { return new ArrayList<>(obstacles); }

    @Override public String getRobotState(String n) {
        BaseRobot robot = this.robots.get(n);
        Position p = robot.getPosition();
        if (robot == null) return "Robot not found";
        return "Position: [" + p.getX() + "," + p.getY() + "], Direction: " + robot.getDirection();
    }

    @Override public boolean checkHit(String s, int d) { return false; }

    /**
     * Exact Obstacle Math: Rectangular coordinates.
     */
  

    @Override
    public ServerResponse perform(Command command) {
        // You need to actually execute the command here
        // For now, to pass the build, you can return the command's result
        
        BaseRobot robot = this.robots.get(command.getRobotName());
        if (robot==null && command.getCommandName()!="launch"){
                command = new ErrorCommand("robot "+command.getRobotName()+" has not been launched",command.getRobotName());
        }
        return command.execute(this,robot);
    }

  

    @Override
    public Map<String, BaseRobot> getAllRobots() {
       return this.robots;
    }
    
    @Override
    public Position newSpawnPoint() {
     for(int i =0;i<this.map.size();i++){
        var subArr = this.map.get(i);
        for(int j =0;j<subArr.size();j++){
            if (this.map.get(i).get(j)==null){
                return new Position(i, j);
            }
        
     }

     }
     return null;
    }

	@Override
	public boolean moveRobot(String name, Position IntendedPosition) {
        return true;

    }

	@Override
	public boolean isPositionAvailable(Position intendedPos) {
	int x=intendedPos.getX();
    int y = intendedPos.getY();
    if (map.get(x).get(y)==null){
        return true;
    }
    else{
        return false;
    }
    }

  

  
}