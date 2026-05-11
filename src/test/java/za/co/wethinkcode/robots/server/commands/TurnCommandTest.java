package za.co.wethinkcode.robots.server.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import za.co.wethinkcode.robots.models.Directions;
import za.co.wethinkcode.robots.models.ServerRequest;
import za.co.wethinkcode.robots.models.ServerResponse;
import za.co.wethinkcode.robots.models.StatusCode;
import za.co.wethinkcode.robots.server.robot.BaseRobot;
import za.co.wethinkcode.robots.server.world.RobotWorld;

public class TurnCommandTest {

    private RobotWorld world;
    private BaseRobot robot;

    @BeforeEach
    void setup() {
        world = new RobotWorld(11, 11, 5);
        world.addRobot("HAL");
        robot = world.getAllRobots().get("HAL");
    }

    private Command turnCommand(String direction) {
        return Command.generate(new ServerRequest("HAL", "turn", new String[]{direction}));
    }

    @Nested
    @DisplayName("response shape")
    class ResponseShape {

        @Test
        void execute_returnsOkResult() {
            ServerResponse res = turnCommand("right").execute(world, robot);
            assertEquals(StatusCode.OK, res.getResult());
        }

        @Test
        void execute_messageIsDone() {
            ServerResponse res = turnCommand("left").execute(world, robot);
            assertNotNull(res.getData());
            assertEquals("DONE", res.getData().getMessage());
        }

        @Test
        void execute_responseStateContainsNewDirection() {
            ServerResponse res = turnCommand("left").execute(world, robot);
            assertNotNull(res.getState());
            assertNotNull(res.getState().getDirection());
        }
    }

    @Nested
    @DisplayName("turning changes the robot's direction")
    class DirectionChange {

        @Test
        void execute_turnLeft_fromNorth_changesDirection() {
            // Start direction is NORTH
            assertEquals(Directions.NORTH, robot.getDirection());
            turnCommand("left").execute(world, robot);
            // After "left" turn, direction should change away from NORTH
            assertNotEquals(Directions.NORTH, robot.getDirection());
        }

        @Test
        void execute_turnRight_fromNorth_changesDirection() {
            assertEquals(Directions.NORTH, robot.getDirection());
            turnCommand("right").execute(world, robot);
            assertNotEquals(Directions.NORTH, robot.getDirection());
        }

        @Test
        void execute_unknownDirection_doesNotChangeFacing() {
            Directions before = robot.getDirection();
            turnCommand("backwards").execute(world, robot);
            assertEquals(before, robot.getDirection());
        }
    }
}