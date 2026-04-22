// # Implementation of the 2D world logic
package za.co.wethinkcode.robots.server.world;

import java.util.*;

public class RobotWorld implements IWorld {

    private final int width;
    private final int height;
    private final int visibility;

    private final Map<String, int[]> robotPositions = new HashMap<>();
    private final Map<String, Direction> robotDirections = new HashMap<>();
    private final List<Obstacle> obstacles = new ArrayList<>();

    public RobotWorld(int width, int height, int visibility) {
        this.width = width;
        this.height = height;
        this.visibility = visibility;
    }

    @Override
    public UpdateResponse moveRobot(String name, int steps) {
        int[] currentPos = robotPositions.get(name);
        Direction dir = robotDirections.get(name);
        int multiplier = (steps > 0) ? 1 : -1;

        int nextX = currentPos[0];
        int nextY = currentPos[1];

        // IMPORTANT: We check every single klik (step)
        for (int i = 1; i <= Math.abs(steps); i++) {
            int stepX = nextX;
            int stepY = nextY;

            // EXACT MATH: North is +Y, South is -Y, East is +X, West is -X
            if (dir == Direction.NORTH) stepY += (1 * multiplier);
            else if (dir == Direction.SOUTH) stepY -= (1 * multiplier);
            else if (dir == Direction.EAST) stepX += (1 * multiplier);
            else if (dir == Direction.WEST) stepX -= (1 * multiplier);

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

        // SPEC: Robot only sees in straight lines (N, S, E, W)
        for (Direction lookDir : Direction.values()) {
            for (int dist = 1; dist <= visibility; dist++) {
                int lookX = pos[0];
                int lookY = pos[1];

                if (lookDir == Direction.NORTH) lookY += dist;
                else if (lookDir == Direction.SOUTH) lookY -= dist;
                else if (lookDir == Direction.EAST) lookX += dist;
                else if (lookDir == Direction.WEST) lookX -= dist;

                // Check for Edges first
                if (lookX > (width/2) || lookX < -(width/2) || lookY > (height/2) || lookY < -(height/2)) {
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
        robotDirections.put(name, Direction.NORTH);
        return true;
    }

    @Override public void removeRobot(String name) {
        robotPositions.remove(name);
        robotDirections.remove(name);
    }

    @Override public void rotateRobot(String name, boolean turnRight) {
        Direction current = robotDirections.get(name);
        if (turnRight) {
            robotDirections.put(name, (current == Direction.NORTH) ? Direction.EAST : (current == Direction.EAST) ? Direction.SOUTH : (current == Direction.SOUTH) ? Direction.WEST : Direction.NORTH);
        } else {
            robotDirections.put(name, (current == Direction.NORTH) ? Direction.WEST : (current == Direction.WEST) ? Direction.SOUTH : (current == Direction.SOUTH) ? Direction.EAST : Direction.NORTH);
        }
    }

    @Override public int getWidth() { return width; }
    @Override public int getHeight() { return height; }
    @Override public List<Object> getObstacles() { return new ArrayList<>(obstacles); }
    @Override public String getRobotState(String n) {
        int[] p = robotPositions.get(n);
        return "Position: [" + p[0] + "," + p[1] + "], Direction: " + robotDirections.get(n);
    }
    @Override public boolean checkHit(String s, int d) { return false; }

    /**
     * Exact Obstacle Math: Rectangular coordinates.
     */
    public static class Obstacle {
        private final int x1, y1, x2, y2; // top-left (x1, y1) and bottom-right (x2, y2)
        private final String type;

        public Obstacle(int x1, int y1, int x2, int y2, String type) {
            this.x1 = x1; this.y1 = y1;
            this.x2 = x2; this.y2 = y2;
            this.type = type;
        }

        public boolean isAt(int x, int y) {
            // A point (x,y) is inside if it's between the X boundaries
            // AND between the Y boundaries.
            return (x >= x1 && x <= x2) && (y <= y1 && y >= y2);
        }

        public String getType() { return type; }
    }
}