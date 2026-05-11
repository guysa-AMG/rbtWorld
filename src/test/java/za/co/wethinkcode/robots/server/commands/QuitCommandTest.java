package za.co.wethinkcode.robots.server.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import za.co.wethinkcode.robots.server.robot.BaseRobot;
import za.co.wethinkcode.robots.server.world.RobotWorld;

public class QuitCommandTest {

    private RobotWorld world;
    private BaseRobot robot;

    @BeforeEach
    void setup() {
        world = new RobotWorld(11, 11, 5);
        world.addRobot("HAL");
        robot = world.getAllRobots().get("HAL");
    }

    @Test
    void execute_removesRobotFromWorld() {
        new QuitCommand("HAL").execute(world, robot);
        assertFalse(world.getAllRobots().containsKey("HAL"));
    }

    @Test
    void execute_returnsNull() {
        // The contract today: quit returns null instead of a Response.
        // If this changes (it really should — quit deserves an OK ack), update this test.
        assertNull(new QuitCommand("HAL").execute(world, robot));
    }

    @Test
    void getCommandName_isQuit() {
        assertEquals("quit", new QuitCommand("HAL").getCommandName());
    }

    @Test
    void getRobotName_returnsConstructorValue() {
        assertEquals("HAL", new QuitCommand("HAL").getRobotName());
    }
}