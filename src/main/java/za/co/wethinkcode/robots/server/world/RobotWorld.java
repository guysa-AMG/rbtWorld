// # Implementation of the 2D world logic

package za.co.wethinkcode.robots.server.world;
import za.co.wethinkcode.robots.models.ServerResponse;
import za.co.wethinkcode.robots.server.commands.Command;



import java.util.*;

public class RobotWorld implements Iworld {

    private final int width;
    private final int height;
    private final int visibility;

    private final Map<String, int[]> robotPositions = new HashMap<>();
    private final Map<String, Iworld.Direction> robotDirections = new HashMap<>();
    private final List<Obstacle> obstacles = new ArrayList<>();

    public RobotWorld(int width, int height, int visibility) {
        this.width = width;
        this.height = height;
        this.visibility = visibility;
    }

    @Override
    public UpdateResponse moveRobot(String name, int steps) {
        int[] currentPos = robotPositions.get(name);
        Iworld.Direction dir = robotDirections.get(name);

        if (currentPos == null || dir == null) return UpdateResponse.OUT_OF_BOUNDS;

        int multiplier = (steps > 0) ? 1 : -1;
        int nextX = currentPos[0];
        int nextY = currentPos[1];

        // IMPORTANT: We check every single klik (step)
        for (int i = 1; i <= Math.abs(steps); i++) {
            int stepX = nextX;
            int stepY = nextY;

            // EXACT MATH: North is +Y, South is -Y, East is +X, West is -X
            if (dir == Iworld.Direction.NORTH) stepY += multiplier;
            else if (dir == Iworld.Direction.SOUTH) stepY -= multiplier;
            else if (dir == Iworld.Direction.EAST) stepX += multiplier;
            else if (dir == Iworld.Direction.WEST) stepX -= multiplier;

            // Updated Boundary check for an odd-sized world
            int xLimit = (width - 1) / 2;
            int yLimit = (height - 1) / 2;

            if (stepX > xLimit || stepX < -xLimit || stepY > yLimit || stepY < -yLimit) {
                return UpdateResponse.OUT_OF_BOUNDS;
            }

            // OBSTACLE MATH: Check if this klik is inside a mountain or lake
            if (isPositionBlocked(stepX, stepY)) {
                return UpdateResponse.HIT_OBSTACLE;
            }

            // PIT MATH: If they hit a pit, they are removed from the world
            if (isPositionInPit(stepX, stepY)) {
                removeRobot(name);
                return UpdateResponse.FELL_IN_PIT;
            }

            nextX = stepX;
            nextY = stepY;
        }

        robotPositions.put(name, new int[]{nextX, nextY});
        return UpdateResponse.SUCCESS;
    }

    @Override
    public List<Object> look(String name) {
        List<Object> results = new ArrayList<>();
        int[] pos = robotPositions.get(name);
        if (pos == null) return results;

        // SPEC: Robot only sees in straight lines (N, S, E, W)
        for (Iworld.Direction lookDir : Iworld.Direction.values()) {
            for (int dist = 1; dist <= visibility; dist++) {
                int lookX = pos[0];
                int lookY = pos[1];

                if (lookDir == Iworld.Direction.NORTH) lookY += dist;
                else if (lookDir == Iworld.Direction.SOUTH) lookY -= dist;
                else if (lookDir == Iworld.Direction.EAST) lookX += dist;
                else if (lookDir == Iworld.Direction.WEST) lookX -= dist;

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
    private Map<String, Object> formatSeen(String type, Iworld.Direction dir, int dist) {
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
    @Override public boolean addRobot(String name) {
        if (robotPositions.containsKey(name)) return false;
        robotPositions.put(name, new int[]{0, 0});
        robotDirections.put(name, Iworld.Direction.NORTH);
        return true;
    }

    @Override public void removeRobot(String name) {
        robotPositions.remove(name);
        robotDirections.remove(name);
    }

    @Override public void rotateRobot(String name, boolean turnRight) {
        Iworld.Direction current = robotDirections.get(name);
        if (current == null) return;

        Iworld.Direction next;
        if (turnRight) {
            next = (current == Iworld.Direction.NORTH) ? Iworld.Direction.EAST :
                    (current == Iworld.Direction.EAST) ? Iworld.Direction.SOUTH :
                    (current == Iworld.Direction.SOUTH) ? Iworld.Direction.WEST : Iworld.Direction.NORTH;
        } else {
            next = (current == Iworld.Direction.NORTH) ? Iworld.Direction.WEST :
                    (current == Iworld.Direction.WEST) ? Iworld.Direction.SOUTH :
                    (current == Iworld.Direction.SOUTH) ? Iworld.Direction.EAST : Iworld.Direction.NORTH;
        }
        robotDirections.put(name, next);
    }

    @Override public int getWidth() { return width; }
    @Override public int getHeight() { return height; }
    @Override public List<Object> getObstacles() { return new ArrayList<>(obstacles); }

    @Override public String getRobotState(String n) {
        int[] p = robotPositions.get(n);
        if (p == null) return "Robot not found";
        return "Position: [" + p[0] + "," + p[1] + "], Direction: " + robotDirections.get(n);
    }

    @Override public boolean checkHit(String s, int d) { return false; }

    /**
     * Exact Obstacle Math: Rectangular coordinates.
     */
    public static class Obstacle {
        private final int x1, y1, x2, y2;
        private final String type;

        public Obstacle(int x1, int y1, int x2, int y2, String type) {
            this.x1 = x1; this.y1 = y1;
            this.x2 = x2; this.y2 = y2;
            this.type = type;
        }

        public boolean isAt(int x, int y) {
            return (x >= x1 && x <= x2) && (y <= y1 && y >= y2);
        }

        public String getType() { return type; }
    }


    @Override
    public ServerResponse perform(Command command) {
        // You need to actually execute the command here
        // For now, to pass the build, you can return the command's result
        return command.execute(this);
    }
}