package za.co.wethinkcode.robots.server.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import za.co.wethinkcode.robots.models.ServerRequest;
import za.co.wethinkcode.robots.models.ServerResponse;
import za.co.wethinkcode.robots.models.StatusCode;
import za.co.wethinkcode.robots.server.robot.BaseRobot;
import za.co.wethinkcode.robots.server.world.RobotWorld;

public class RobotsCommandTest {

    private RobotWorld world;
    private BaseRobot robot;

    @BeforeEach
    void setup() {
        world = new RobotWorld(11, 11, 5);
        world.addRobot("HAL");
        robot = world.getAllRobots().get("HAL");
    }

    private Command robotsCommand(String robotName) {
        return Command.generate(new ServerRequest(robotName, "robots", new String[0]));
    }

    @Test
    void execute_returnsOkResult() {
        ServerResponse res = robotsCommand("HAL").execute(world, robot);
        assertEquals(StatusCode.OK, res.getResult());
    }

    @Test
    void execute_messagePrefixedWithRobotsConnected() {
        ServerResponse res = robotsCommand("HAL").execute(world, robot);
        assertNotNull(res.getData());
        assertNotNull(res.getData().getMessage());
        assertTrue(res.getData().getMessage().startsWith("Robots Connected are :"),
                "Got: " + res.getData().getMessage());
    }

    @Test
    void execute_messageIncludesAllRobotNames() {
        world.addRobot("R2");
        ServerResponse res = robotsCommand("HAL").execute(world, robot);
        String msg = res.getData().getMessage();
        assertTrue(msg.contains("HAL"), "Expected message to contain HAL: " + msg);
        assertTrue(msg.contains("R2"),  "Expected message to contain R2: " + msg);
    }

    @Test
    void execute_addsRobotIfNotPresent() {
        // Calling robots command for a name not yet in world should add them
        // (this is the actual current behavior, weird but documented).
        ServerResponse res = robotsCommand("Mark").execute(world, robot);
        assertEquals(StatusCode.OK, res.getResult());
        // Mark is now in the world map
        assertTrue(world.getAllRobots().containsKey("Mark"));
    }
}