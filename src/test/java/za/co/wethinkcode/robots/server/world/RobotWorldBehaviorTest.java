package za.co.wethinkcode.robots.server.world;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import za.co.wethinkcode.robots.models.Position;
import za.co.wethinkcode.robots.models.impediment.EmptySpot;
import za.co.wethinkcode.robots.models.impediment.Pit;
import za.co.wethinkcode.robots.server.robot.BaseRobot;

public class RobotWorldBehaviorTest {

    @Test
    public void testSwapIntoPitRemovesRobot() {
        RobotWorld world = new RobotWorld(3,1,3);

        // Create a simple map: robot at (0,0) and Pit at (1,0)
        BaseRobot robot = BaseRobot.Builder("rob", 0, 0, 1, 1,0);
        List<za.co.wethinkcode.robots.models.impediment.Impediments> map = new ArrayList<>();
        map.add(robot);
        map.add(new Pit(new Position(1,0)));
        world.loadMap(map);
        // Register robot in world index
        world.getAllRobots().put("rob", robot);
        // Swap into pit
        String fell = world.swapePosition(new Position(1,0), new Position(0,0));
        assertNotNull(fell);
        assertTrue(fell.equalsIgnoreCase("HOLE") || fell.equalsIgnoreCase("PIT") || fell.length() > 0);
        assertFalse(world.getAllRobots().containsKey("rob"));
    }

    @Test
    public void testRotateRobotChangesDirection() {
        RobotWorld world = new RobotWorld(5,5,3);
        BaseRobot r = BaseRobot.Builder("alice", 0,0,3,3,0);
        world.getAllRobots().put("alice", r);
        r.updateDirection(za.co.wethinkcode.robots.models.Directions.NORTH);
        world.rotateRobot("alice", true); // turn right -> EAST
        assertEquals(za.co.wethinkcode.robots.models.Directions.EAST, r.getDirection());
        String state = world.getRobotState("alice");
        assertTrue(state.contains("Position") && state.contains("Direction"));
    }
}
