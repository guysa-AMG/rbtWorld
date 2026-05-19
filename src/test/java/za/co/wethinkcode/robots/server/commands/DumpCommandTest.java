package za.co.wethinkcode.robots.server.commands;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import za.co.wethinkcode.robots.models.ServerResponse;
import za.co.wethinkcode.robots.server.robot.BaseRobot;
import za.co.wethinkcode.robots.server.world.RobotWorld;

public class DumpCommandTest {

    private RobotWorld world;
    private BaseRobot robot;

    @BeforeEach
    void setup() {
        world = new RobotWorld(11, 11, 5);
        world.addRobot("HAL");
        robot = world.getAllRobots().get("HAL");
    }

    @Test
    void execute_returnsNull() {
        // DumpCommand currently returns null after sending a message to the robot.
        // If this changes to return an OK response, update this test.
        assertTrue(new DumpCommand("HAL").execute(world, robot) instanceof ServerResponse);
    }

    @Test
    void execute_doesNotThrowWithValidRobotAndWorld() {
        assertDoesNotThrow(() -> new DumpCommand("HAL").execute(world, robot));
    }

    @Test
    void getCommandName_isDump() {
        assertEquals("dump", new DumpCommand("HAL").getCommandName());
    }

    @Test
    void getRobotName_returnsConstructorValue() {
        assertEquals("HAL", new DumpCommand("HAL").getRobotName());
    }
}