// # State: position, direction, shields
package za.co.wethinkcode.robots.server.robot;

public class Robot {
    private int x;
    private int y;
    Direction direction;
    private int shields;

    public enum Direction {
        NORTH, SOUTH, EAST, WEST
    }

    public Robot(int x, int y) {
        this.x = x;
        this.y = y;
        this.direction = Direction.NORTH;
        this.shields = 20;
    }
}