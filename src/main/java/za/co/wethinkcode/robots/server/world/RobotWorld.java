// # Implementation of the 2D world logic

package za.co.wethinkcode.robots.server.world;
import za.co.wethinkcode.robots.models.Directions;
import za.co.wethinkcode.robots.models.Position;
import za.co.wethinkcode.robots.models.impediment.EmptySpot;
import za.co.wethinkcode.robots.models.impediment.Impediments;
import za.co.wethinkcode.robots.models.impediment.Obstacle;
import za.co.wethinkcode.robots.models.transitmodels.ServerResponse;
import za.co.wethinkcode.robots.server.commands.Command;
import za.co.wethinkcode.robots.server.commands.ErrorCommand;
import za.co.wethinkcode.robots.server.robot.BaseRobot;
import za.co.wethinkcode.robots.server.robot.SimpleRobot;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RobotWorld extends WorldGenerator  {

    private final java.util.Set<Position> ammoPickups = ConcurrentHashMap.newKeySet();
  


    public RobotWorld(int width, int height, int visibility) {
        super();
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

    @Override
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
        // for (Obstacle obs : obstacles) {
        //     if (!obs.getType().equals("PIT") && obs.isAt(x, y)) return true;
        // }
      boolean inUse=  this.map.stream().anyMatch(imeds -> imeds.getPosition().equals(new Position(x, y))&& !( imeds instanceof EmptySpot));   
        return inUse;
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
    // public Position findSafeSpawn(List<Position> avoid, int minDistance) {
    //     int xLimit = (width - 1) / 2;
    //     int yLimit = (height - 1) / 2;
    //     java.util.Random rnd = new java.util.Random();
    //     for (int attempt = 0; attempt < 500; attempt++) {
    //         int x = rnd.nextInt(2 * xLimit + 1) - xLimit;
    //         int y = rnd.nextInt(2 * yLimit + 1) - yLimit;
    //         if (isPositionBlocked(x, y) || isPositionInPit(x, y)) continue;
    //         if (robotAtCell(x, y, null) != null) continue;
    //         // Avoid pickups and avoid-list cells
    //         if (cellInList(x, y, getAmmoPickups(), 0)) continue;
    //         if (avoid != null && !avoid.isEmpty() && cellInList(x, y, avoid, minDistance)) continue;
    //         return new Position(x, y);
    //     }
    //     // Fallback: relax the minDistance constraint
    //     for (int attempt = 0; attempt < 100; attempt++) {
    //         int x = rnd.nextInt(2 * xLimit + 1) - xLimit;
    //         int y = rnd.nextInt(2 * yLimit + 1) - yLimit;
    //         if (isPositionBlocked(x, y) || isPositionInPit(x, y)) continue;
    //         if (robotAtCell(x, y, null) != null) continue;
    //         if (cellInList(x, y, getAmmoPickups(), 0)) continue;
    //         return new Position(x, y);
    //     }
    //     return new Position(0, 0);
    // }

    // public Position findSafeSpawn() {
    //     return findSafeSpawn(null, 0);
    // }

    private boolean cellInList(int x, int y, List<Position> list, int minDistance) {
        for (Position p : list) {
            int dx = Math.abs(p.getX() - x);
            int dy = Math.abs(p.getY() - y);
            if (Math.max(dx, dy) <= minDistance) return true;
        }
        return false;
    }

    // public void consumePickupAt(int x, int y, BaseRobot robot) {
    //     Position match = null;
    //     for (Position p : this.ammoPickups) {
    //         if (p.getX() == x && p.getY() == y) { match = p; break; }
    //     }
    //     if (match != null) {
    //         this.ammoPickups.remove(match);
    //         robot.refillAmmo();
    //         // Respawn a fresh pickup elsewhere, at least 6 cells away from the picker.
    //         Position fresh = findSafeSpawn(List.of(robot.getPosition()), 6);
    //         addAmmoPickup(fresh);
    //     }
    // }

    // OTHER INTERFACE METHODS
    @Override
     public boolean addRobot(String name,int shield,int shoots) {
        if (robots.containsKey(name)) return false;
    
        List<Position> others = new ArrayList<>();
        for (BaseRobot r : robots.values()) {
            if (r.getPosition() != null) others.add(r.getPosition());
        }
        // Position spawn = findSafeSpawn(others, 8);
        Position spawn = newSpawnPoint();
        BaseRobot robot = BaseRobot.Builder(name, spawn.getX(), spawn.getY(), shield, shoots);
        robots.put(name, robot);
        this.map.remove(getObjectsAtPosition(spawn));
        this.map.add(robot);
        return true;
    }
     public boolean addRobot(String name) {
      return addRobot(name,1,1);
    }
   

    @Override
     public void removeRobot(String name) {
        try{
       robots.remove(name);
       Impediments bots  = this.map.stream()
                                    .filter(imped -> imped instanceof BaseRobot)
                                    .filter(bot -> ((BaseRobot)bot).getName().toLowerCase().equals(name))
                                    .findFirst()
                                    .get();
        Position pos = bots.getPosition();
        this.map.add(new EmptySpot(pos));
        this.map.remove(bots);
       
        }
        catch(NoSuchElementException e){
            
        }
       

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
    public void updateRobot(String name,BaseRobot robot){
        this.robots.put(name, robot);

    }

 
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

    public void swapePosition(Position intenPosition, Position old){
    Impediments obj2 = getObjectsAtPosition(intenPosition);
    Impediments obj1 = getObjectsAtPosition(old);
    if (obj1 == null || obj2 == null) return;
    Position intendedCopy = intenPosition.copy();
    Position oldCopy = old.copy();
    
    obj1.setPosition(intendedCopy);
    obj2.setPosition(oldCopy);
    
    }

	@Override
	public boolean isPositionAvailable(Position intendedPos) {
         if (intendedPos == null || !intendedPos.isIn(0,0,width,height)) return false;

        // int x = intendedPos.getX();
        // int y = intendedPos.getY();
        // int xLimit = (width - 1) / 2;
        // int yLimit = (height - 1) / 2;
        // if (x < -xLimit || x > xLimit || y < -yLimit || y > yLimit) return false;
        // return !isPositionBlocked(x, y);
       Impediments obj = getObjectsAtPosition(intendedPos);
        return obj instanceof EmptySpot ;
    }

    Impediments getObjectsAtPosition(Position pos ){
        if (pos==null){
            return null;
        }
        try{
       return  this.map.stream()
                        .filter(obj->obj.getPosition()
                                        .equals(pos)).findFirst()
                                                     .get();
        }
        catch(NoSuchElementException ex){
            return null;
        }
      
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