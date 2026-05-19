package za.co.wethinkcode.robots.server.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import za.co.wethinkcode.robots.models.ServerRequest;
import za.co.wethinkcode.robots.models.ServerResponse;
import za.co.wethinkcode.robots.server.robot.BaseRobot;
import za.co.wethinkcode.robots.server.world.RobotWorld;

public class StateCommandTest {

    private RobotWorld world;
    private BaseRobot robot;

    @BeforeEach
    void setup() {
        world = new RobotWorld(11, 11, 5);
        world.addRobot("HAL");
        robot = world.getAllRobots().get("HAL");
    }

    private Command stateCommand() {
        return Command.generate(new ServerRequest("HAL", "state", new String[0]));
    }

    @Test
    void execute_returnsResponseWithState() {
        ServerResponse res = stateCommand().execute(world, robot);
        assertNotNull(res);
        assertNotNull(res.getState());
    }

    @Test
    void execute_stateContainsRobotPosition() {
        ServerResponse res = stateCommand().execute(world, robot);
        // Robots now spawn at random safe cells; we just confirm position matches the live robot.
        assertNotNull(res.getState().getPosition());
        assertEquals(robot.getPosition().getX(), res.getState().getPosition().getX());
        assertEquals(robot.getPosition().getY(), res.getState().getPosition().getY());
    }

    @Test
    void execute_stateContainsShields() {
        ServerResponse res = stateCommand().execute(world, robot);
        assertEquals(robot.getShield(), res.getState().getShields());
    }

    @Test
    void execute_stateContainsShots() {
        ServerResponse res = stateCommand().execute(world, robot);
        assertEquals(robot.getShoots(), res.getState().getShots());
    }

    @Test
    void execute_doesNotIncludeData() {
        ServerResponse res = stateCommand().execute(world, robot);
        // StateCommand only sets state, leaves data null
        // (ServerResponse builder does not auto-populate data)
        org.junit.jupiter.api.Assertions.assertNull(res.getData());
    }
}