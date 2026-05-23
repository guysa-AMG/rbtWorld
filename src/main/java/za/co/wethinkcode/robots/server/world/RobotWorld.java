// # Implementation of the 2D world logic

package za.co.wethinkcode.robots.server.world;
import za.co.wethinkcode.robots.models.Directions;
import za.co.wethinkcode.robots.models.ImpedimentType;
import za.co.wethinkcode.robots.models.Position;
import za.co.wethinkcode.robots.models.impediment.EmptySpot;
import za.co.wethinkcode.robots.models.impediment.Impediments;
import za.co.wethinkcode.robots.models.impediment.Obstacle;
import za.co.wethinkcode.robots.models.transitmodels.ServerResponse;
import za.co.wethinkcode.robots.models.transitmodels.ServerResponseObject;
import za.co.wethinkcode.robots.server.commands.Command;
import za.co.wethinkcode.robots.server.commands.ErrorCommand;
import za.co.wethinkcode.robots.server.robot.BaseRobot;

import java.util.*;

public class RobotWorld extends WorldGenerator {

    private final int width;
    private final int height;
    private final int visibility;
    private final List<Obstacle> obstacles;
    private List<Impediments> map;
    private ArrayList<Position> emptySpots;
    private final Map<String, BaseRobot> robots = new HashMap<>();
    private final java.util.Set<Position> ammoPickups = java.util.concurrent.ConcurrentHashMap.newKeySet();
    private List<Command> historyOfCommands;


    public RobotWorld(int width, int height, int visibility) {
        this.width = width;
        this.height = height;
        this.visibility = visibility;
        this.map=new ArrayList<>();
        this.obstacles= new ArrayList<Obstacle>();
        this.historyOfCommands=new ArrayList<>();
        
    }

    public RobotWorld() {
        this(10,10,7);
        this.emptySpots=new ArrayList<>();
    }
    public List<Impediments> getMap(){
        return Collections.unmodifiableList(this.map);
    }
    public void loadMap(List<Impediments> map ){
        
        this.map=map;
    }
    public void setEmptySlots(ArrayList<Position> empties){
        this.emptySpots=empties;
    }
    @Override
    public boolean moveRobot(String name, int steps) {
        BaseRobot robot = robots.get(name);
        if (robot == null) return false;
        Position currentPos = robot.getPosition();
        Directions dir = robot.getDirection();

        if (currentPos == null || dir == null) return false;
        if (steps == 0) return true;

        int multiplier = (steps > 0) ? 1 : -1;
        int nextX = currentPos.getX();
        int nextY = currentPos.getY();
        boolean fullyMoved = true;

        int xLimit = (width - 1) / 2;
        int yLimit = (height - 1) / 2;

        for (int i = 1; i <= Math.abs(steps); i++) {
            int stepX = nextX;
            int stepY = nextY;

            if (dir == Directions.NORTH) stepY += multiplier;
            else if (dir == Directions.SOUTH) stepY -= multiplier;
            else if (dir == Directions.EAST) stepX += multiplier;
            else if (dir == Directions.WEST) stepX -= multiplier;

            if (stepX > xLimit || stepX < -xLimit || stepY > yLimit || stepY < -yLimit) {
                fullyMoved = false;
                break;
            }

            if (isPositionBlocked(stepX, stepY)) {
                fullyMoved = false;
                break;
            }

            if (isPositionInPit(stepX, stepY)) {
                robot.updatePosition(new Position(stepX, stepY));
                int remaining = robot.decrementLives();
                if (remaining > 0) {
                    Position spawn = findSafeSpawn();
                    robot.respawnAt(spawn);
                    updateRobot(name, robot);
                } else {
                    removeRobot(name);
                }
                return true;
            }

            nextX = stepX;
            nextY = stepY;
        }
        robot.updatePosition(new Position(nextX, nextY));
        consumePickupAt(nextX, nextY, robot);
        updateRobot(name, robot);
        return fullyMoved;
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

    /**
     * Structured scan in 4 cardinals up to {@link Iworld#lookRange}.
     * Reports each visible obstacle/robot with its position.
     * MOUNTAIN/WALL block line-of-sight; PIT/LAKE/TREE/ROCK are visible but the scan continues past them.
     */
    

     public BaseRobot robotAtCell(int x, int y, BaseRobot self) {
        for (BaseRobot r : robots.values()) {
            if (r == self) continue;
            Position p = r.getPosition();
            if (p != null && p.getX() == x && p.getY() == y) return r;
        }
        return null;
    }

    public String obstacleTypeAt(int x, int y) {
        for (Obstacle obs : obstacles) {
            if (obs.isAt(x, y)) return obs.getType();
        }
        return null;
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

    public void addObstacle(Obstacle obstacle) { this.obstacles.add(obstacle); }

    /**
     * Add a pickup ONLY if the cell is free of obstacles/pits and not already a pickup.
     * Returns true if the pickup was placed.
     */
    public boolean addAmmoPickup(Position p) {
        if (p == null) return false;
        if (isPositionBlocked(p.getX(), p.getY())) return false;
        if (isPositionInPit(p.getX(), p.getY())) return false;
        for (Position existing : this.ammoPickups) {
            if (existing.getX() == p.getX() && existing.getY() == p.getY()) return false;
        }
        this.ammoPickups.add(p);
        return true;
    }

    public List<Position> getAmmoPickups() {
        List<Position> copy = new ArrayList<>(this.ammoPickups.size());
        for (Position p : this.ammoPickups) copy.add(p.copy());
        return copy;
    }

    /** Find a safe spawn cell: in-bounds, not blocked, not a pit, not occupied, optionally far from {@code avoid}. */
    public Position findSafeSpawn(List<Position> avoid, int minDistance) {
        int xLimit = (width - 1) / 2;
        int yLimit = (height - 1) / 2;
        java.util.Random rnd = new java.util.Random();
        for (int attempt = 0; attempt < 500; attempt++) {
            int x = rnd.nextInt(2 * xLimit + 1) - xLimit;
            int y = rnd.nextInt(2 * yLimit + 1) - yLimit;
            if (isPositionBlocked(x, y) || isPositionInPit(x, y)) continue;
            if (robotAtCell(x, y, null) != null) continue;
            // Avoid pickups and avoid-list cells
            if (cellInList(x, y, getAmmoPickups(), 0)) continue;
            if (avoid != null && !avoid.isEmpty() && cellInList(x, y, avoid, minDistance)) continue;
            return new Position(x, y);
        }
        // Fallback: relax the minDistance constraint
        for (int attempt = 0; attempt < 100; attempt++) {
            int x = rnd.nextInt(2 * xLimit + 1) - xLimit;
            int y = rnd.nextInt(2 * yLimit + 1) - yLimit;
            if (isPositionBlocked(x, y) || isPositionInPit(x, y)) continue;
            if (robotAtCell(x, y, null) != null) continue;
            if (cellInList(x, y, getAmmoPickups(), 0)) continue;
            return new Position(x, y);
        }
        return new Position(0, 0);
    }

    public Position findSafeSpawn() {
        return findSafeSpawn(null, 0);
    }

    private boolean cellInList(int x, int y, List<Position> list, int minDistance) {
        for (Position p : list) {
            int dx = Math.abs(p.getX() - x);
            int dy = Math.abs(p.getY() - y);
            if (Math.max(dx, dy) <= minDistance) return true;
        }
        return false;
    }

    private void consumePickupAt(int x, int y, BaseRobot robot) {
        Position match = null;
        for (Position p : this.ammoPickups) {
            if (p.getX() == x && p.getY() == y) { match = p; break; }
        }
        if (match != null) {
            this.ammoPickups.remove(match);
            robot.refillAmmo();
            // Respawn a fresh pickup elsewhere, at least 6 cells away from the picker.
            Position fresh = findSafeSpawn(List.of(robot.getPosition()), 6);
            addAmmoPickup(fresh);
        }
    }

    // OTHER INTERFACE METHODS
    @Override
     public boolean addRobot(String name) {
        if (robots.containsKey(name)) return false;
        List<Position> others = new ArrayList<>();
        for (BaseRobot r : robots.values()) {
            if (r.getPosition() != null) others.add(r.getPosition());
        }
        Position spawn = findSafeSpawn(others, 8);
        BaseRobot robot = BaseRobot.Builder(name, spawn.getX(), spawn.getY(), 3, 2);
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
    @Override public List<Obstacle> getObstacles() { return new ArrayList<>(obstacles); }

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

        BaseRobot robot = this.robots.get(command.getRobotName());
        if (robot == null && !"launch".equals(command.getCommandName())&& command.restricted()) {
            command = new ErrorCommand("robot " + command.getRobotName() + " has not been launched", command.getRobotName());
        }
        this.historyOfCommands.add(command);
        return command.execute(this, robot);
    }

  

    @Override
    public Map<String, BaseRobot> getAllRobots() {
       return this.robots;
    }
    
    @Override
    public Position newSpawnPoint() {
    var filtered= this.map.stream()
             .filter(obj->obj instanceof EmptySpot)
             .toList();
    if (filtered.size()>0)
    {Random rdm = new Random();
    int randomIndex = rdm.nextInt(filtered.size());
    return filtered.get(randomIndex).getPosition();
}
     return null;
    }

	@Override
	public boolean moveRobot(String name, Position IntendedPosition) {
        return true;

    }

	@Override
	public boolean isPositionAvailable(Position intendedPos) {
        if (intendedPos == null) return false;
        int x = intendedPos.getX();
        int y = intendedPos.getY();
        int xLimit = (width - 1) / 2;
        int yLimit = (height - 1) / 2;
        if (x < -xLimit || x > xLimit || y < -yLimit || y > yLimit) return false;
        return !isPositionBlocked(x, y);
    }

    Impediments getObjectsAtPosition(Position pos ){
        if (pos==null){
            return null;
        }
       return  this.map.stream()
                        .filter(obj->obj.getPosition()
                                        .equals(pos)).findFirst()
                                                     .get();
      
    }
    @Override
    public BaseRobot getFireable(BaseRobot rbt) {
     Impediments botInSight;

     Directions direction = rbt.getDirection();
     Position bulletPosition = rbt.getPosition();
      switch (direction) {

        case SOUTH -> {
            do {
               bulletPosition.incrementY(); 

            } while (isPositionAvailable(bulletPosition)|| bulletPosition.getStraightDistance(rbt.getPosition())<=Iworld.visibleDistance);
            if (( botInSight = getObjectsAtPosition(bulletPosition)) instanceof BaseRobot){
                return (BaseRobot) botInSight;
            }
            
           }
        case NORTH -> {
                bulletPosition = rbt.getPosition();
                do {
                bulletPosition.decrementY(); 

                } while (isPositionAvailable(bulletPosition)&& bulletPosition.getStraightDistance(rbt.getPosition())<=Iworld.visibleDistance);
                if (( botInSight = getObjectsAtPosition(bulletPosition)) instanceof BaseRobot){
                    return (BaseRobot) botInSight;
                }
           }
        case EAST -> {
             bulletPosition = rbt.getPosition();
            do {
               bulletPosition.incrementX(); 

            } while (isPositionAvailable(bulletPosition)|| bulletPosition.getStraightDistance(rbt.getPosition())<=Iworld.visibleDistance);
            if (( botInSight = getObjectsAtPosition(bulletPosition)) instanceof BaseRobot){
                return (BaseRobot) botInSight;
            }
        }

        case WEST -> {
             bulletPosition = rbt.getPosition();
            do {
               bulletPosition.decrementX(); 

            } while (isPositionAvailable(bulletPosition)|| bulletPosition.getStraightDistance(rbt.getPosition())<=Iworld.visibleDistance);
            if (( botInSight = getObjectsAtPosition(bulletPosition)) instanceof BaseRobot){
                return (BaseRobot) botInSight;
            }
            else{
                return null;
            }
        
         }
        default  -> {return null;}

     }
      return null;

    }

    @Override
    public List<Command> getHistoryOfCommands() {
        return this.historyOfCommands;
    }

   

  

  
}