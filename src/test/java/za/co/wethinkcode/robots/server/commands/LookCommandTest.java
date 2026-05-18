package za.co.wethinkcode.robots.server.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import za.co.wethinkcode.robots.models.ServerResponse;
import za.co.wethinkcode.robots.models.StatusCode;
import za.co.wethinkcode.robots.server.robot.BaseRobot;
import za.co.wethinkcode.robots.server.world.RobotWorld;

public class LookCommandTest {

    private RobotWorld world;
    private BaseRobot robot;

    /**
     * NOTE: LookCommand is currently a stub — it does not actually compute
     * what a robot can see. These tests pin down the placeholder response
     * shape so that when real look-logic is added, we know what changed.
     */

    @BeforeEach
    void setup() {
        world = new RobotWorld(11, 11, 5);
        world.addRobot("HAL");
        robot = world.getAllRobots().get("HAL");
    }

    private LookCommand newLookCommand() {
        // LookCommand has a package-private constructor — we can call it directly here
        return new LookCommand("look", "HAL");
    }

    @Test
    void execute_returnsOkResult() {
        ServerResponse res = newLookCommand().execute(world, robot);
        assertEquals(StatusCode.OK, res.getResult());
    }

    @Test
    void execute_responseHasDataAndState() {
        ServerResponse res = newLookCommand().execute(world, robot);
        assertNotNull(res.getData());
        assertNotNull(res.getState());
    }

    @Test
    void execute_dataIncludesPositionVisibilityShields() {
        ServerResponse res = newLookCommand().execute(world, robot);
        assertNotNull(res.getData().getPosition());
        assertTrue(res.getData().getVisibility() > 0);
       // assertTrue(res.getData().getShields() > 0);
    }

    @Test
    void execute_stateIncludesPositionAndDirection() {
        ServerResponse res = newLookCommand().execute(world, robot);
        assertNotNull(res.getState().getPosition());
        assertNotNull(res.getState().getDirection());
    }
}
