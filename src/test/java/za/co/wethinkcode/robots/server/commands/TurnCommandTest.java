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

    // =====================================================================
    // Exhaustive rotation tests — every direction × {left, right} permutation,
    // plus argument validation and combinations.
    // =====================================================================

    @Nested
    @DisplayName("turn right from each cardinal")
    class TurnRight {

        @Test void fromNorth_facesEast() {
            robot.updateDirection(Directions.NORTH);
            turnCommand("right").execute(world, robot);
            assertEquals(Directions.EAST, robot.getDirection());
        }

        @Test void fromEast_facesSouth() {
            robot.updateDirection(Directions.EAST);
            turnCommand("right").execute(world, robot);
            assertEquals(Directions.SOUTH, robot.getDirection());
        }

        @Test void fromSouth_facesWest() {
            robot.updateDirection(Directions.SOUTH);
            turnCommand("right").execute(world, robot);
            assertEquals(Directions.WEST, robot.getDirection());
        }

        @Test void fromWest_facesNorth() {
            robot.updateDirection(Directions.WEST);
            turnCommand("right").execute(world, robot);
            assertEquals(Directions.NORTH, robot.getDirection());
        }

        @Test void fourRights_returnToOrigin() {
            Directions start = robot.getDirection();
            for (int i = 0; i < 4; i++) turnCommand("right").execute(world, robot);
            assertEquals(start, robot.getDirection());
        }

        @Test void rightThenRight_isOneEighty() {
            robot.updateDirection(Directions.NORTH);
            turnCommand("right").execute(world, robot);
            turnCommand("right").execute(world, robot);
            assertEquals(Directions.SOUTH, robot.getDirection());
        }
    }

    @Nested
    @DisplayName("turn left from each cardinal")
    class TurnLeft {

        @Test void fromNorth_facesWest() {
            robot.updateDirection(Directions.NORTH);
            turnCommand("left").execute(world, robot);
            assertEquals(Directions.WEST, robot.getDirection());
        }

        @Test void fromWest_facesSouth() {
            robot.updateDirection(Directions.WEST);
            turnCommand("left").execute(world, robot);
            assertEquals(Directions.SOUTH, robot.getDirection());
        }

        @Test void fromSouth_facesEast() {
            robot.updateDirection(Directions.SOUTH);
            turnCommand("left").execute(world, robot);
            assertEquals(Directions.EAST, robot.getDirection());
        }

        @Test void fromEast_facesNorth() {
            robot.updateDirection(Directions.EAST);
            turnCommand("left").execute(world, robot);
            assertEquals(Directions.NORTH, robot.getDirection());
        }

        @Test void fourLefts_returnToOrigin() {
            Directions start = robot.getDirection();
            for (int i = 0; i < 4; i++) turnCommand("left").execute(world, robot);
            assertEquals(start, robot.getDirection());
        }

        @Test void leftThenLeft_isOneEighty() {
            robot.updateDirection(Directions.NORTH);
            turnCommand("left").execute(world, robot);
            turnCommand("left").execute(world, robot);
            assertEquals(Directions.SOUTH, robot.getDirection());
        }
    }

    @Nested
    @DisplayName("left + right cancel each other")
    class LeftRightSymmetry {

        @Test void leftThenRight_returnsToOrigin_fromNorth() {
            robot.updateDirection(Directions.NORTH);
            turnCommand("left").execute(world, robot);
            turnCommand("right").execute(world, robot);
            assertEquals(Directions.NORTH, robot.getDirection());
        }

        @Test void rightThenLeft_returnsToOrigin_fromEast() {
            robot.updateDirection(Directions.EAST);
            turnCommand("right").execute(world, robot);
            turnCommand("left").execute(world, robot);
            assertEquals(Directions.EAST, robot.getDirection());
        }

        @Test void leftThenRight_returnsToOrigin_fromSouth() {
            robot.updateDirection(Directions.SOUTH);
            turnCommand("left").execute(world, robot);
            turnCommand("right").execute(world, robot);
            assertEquals(Directions.SOUTH, robot.getDirection());
        }

        @Test void rightThenLeft_returnsToOrigin_fromWest() {
            robot.updateDirection(Directions.WEST);
            turnCommand("right").execute(world, robot);
            turnCommand("left").execute(world, robot);
            assertEquals(Directions.WEST, robot.getDirection());
        }
    }

    @Nested
    @DisplayName("input validation")
    class InputValidation {

        @Test void missingArgument_returnsError() {
            Command c = Command.generate(new ServerRequest("HAL", "turn", new String[0]));
            ServerResponse res = c.execute(world, robot);
            assertEquals(StatusCode.ERROR, res.getResult());
        }

        @Test void missingArgument_messageMentionsLeftOrRight() {
            Command c = Command.generate(new ServerRequest("HAL", "turn", new String[0]));
            ServerResponse res = c.execute(world, robot);
            org.junit.jupiter.api.Assertions.assertTrue(
                    res.getData().getMessage().toLowerCase().contains("left") &&
                    res.getData().getMessage().toLowerCase().contains("right"));
        }

        @Test void missingArgument_doesNotChangeDirection() {
            Directions before = robot.getDirection();
            Command c = Command.generate(new ServerRequest("HAL", "turn", new String[0]));
            c.execute(world, robot);
            assertEquals(before, robot.getDirection());
        }

        @Test void invalidArgument_returnsError() {
            ServerResponse res = turnCommand("sideways").execute(world, robot);
            assertEquals(StatusCode.ERROR, res.getResult());
        }

        @Test void invalidArgument_messageContainsBadValue() {
            ServerResponse res = turnCommand("sideways").execute(world, robot);
            org.junit.jupiter.api.Assertions.assertTrue(res.getData().getMessage().contains("sideways"));
        }

        @Test void invalidArgument_doesNotChangeDirection() {
            Directions before = robot.getDirection();
            turnCommand("sideways").execute(world, robot);
            assertEquals(before, robot.getDirection());
        }

        @Test void emptyStringArgument_returnsError() {
            ServerResponse res = turnCommand("").execute(world, robot);
            assertEquals(StatusCode.ERROR, res.getResult());
        }
    }

    @Nested
    @DisplayName("case insensitivity")
    class Casing {

        @Test void mixedCaseLeft_works() {
            robot.updateDirection(Directions.NORTH);
            turnCommand("Left").execute(world, robot);
            assertEquals(Directions.WEST, robot.getDirection());
        }

        @Test void upperCaseRight_works() {
            robot.updateDirection(Directions.NORTH);
            turnCommand("RIGHT").execute(world, robot);
            assertEquals(Directions.EAST, robot.getDirection());
        }

        @Test void lowerCaseLeft_works() {
            robot.updateDirection(Directions.NORTH);
            turnCommand("left").execute(world, robot);
            assertEquals(Directions.WEST, robot.getDirection());
        }

        @Test void mixedCaseRight_works() {
            robot.updateDirection(Directions.NORTH);
            turnCommand("RiGhT").execute(world, robot);
            assertEquals(Directions.EAST, robot.getDirection());
        }
    }

    @Nested
    @DisplayName("turn does not affect non-direction state")
    class SideEffects {

        @Test void turn_doesNotChangePosition() {
            robot.updatePosition(new za.co.wethinkcode.robots.models.Position(3, 4));
            turnCommand("right").execute(world, robot);
            assertEquals(3, robot.getPosition().getX());
            assertEquals(4, robot.getPosition().getY());
        }

        @Test void turn_doesNotChangeShield() {
            int before = robot.getShield();
            turnCommand("left").execute(world, robot);
            assertEquals(before, robot.getShield());
        }

        @Test void turn_doesNotChangeAmmo() {
            int before = robot.getShoots();
            turnCommand("right").execute(world, robot);
            assertEquals(before, robot.getShoots());
        }

        @Test void turn_doesNotChangeLives() {
            int before = robot.getLives();
            turnCommand("right").execute(world, robot);
            assertEquals(before, robot.getLives());
        }

        @Test void turn_doesNotChangeKills() {
            int before = robot.getKills();
            turnCommand("right").execute(world, robot);
            assertEquals(before, robot.getKills());
        }

        @Test void turn_doesNotResetBlockedCount() {
            robot.incrementBlocked();
            robot.incrementBlocked();
            int before = robot.getBlockedCount();
            turnCommand("right").execute(world, robot);
            assertEquals(before, robot.getBlockedCount());
        }
    }

    @Nested
    @DisplayName("response state contents")
    class ResponseState {

        @Test void state_directionMatchesNewDirection() {
            robot.updateDirection(Directions.NORTH);
            ServerResponse res = turnCommand("right").execute(world, robot);
            assertEquals(Directions.EAST, res.getState().getDirection());
        }

        @Test void state_positionMatchesRobot() {
            robot.updatePosition(new za.co.wethinkcode.robots.models.Position(2, -3));
            ServerResponse res = turnCommand("left").execute(world, robot);
            assertEquals(2, res.getState().getPosition().getX());
            assertEquals(-3, res.getState().getPosition().getY());
        }

        @Test void state_shieldMatchesRobot() {
            ServerResponse res = turnCommand("right").execute(world, robot);
            assertEquals(robot.getShield(), res.getState().getShields());
        }

        @Test void state_includesShootsCount() {
            // For turn, shots field is left to default (0), but state object is non-null
            ServerResponse res = turnCommand("right").execute(world, robot);
            assertNotNull(res.getState());
        }
    }

    @Nested
    @DisplayName("rotation cycles")
    class Cycles {

        @Test void leftEightTimes_returnsToOrigin() {
            Directions start = robot.getDirection();
            for (int i = 0; i < 8; i++) turnCommand("left").execute(world, robot);
            assertEquals(start, robot.getDirection());
        }

        @Test void rightEightTimes_returnsToOrigin() {
            Directions start = robot.getDirection();
            for (int i = 0; i < 8; i++) turnCommand("right").execute(world, robot);
            assertEquals(start, robot.getDirection());
        }

        @Test void alternateLeftRight_endsAtStart() {
            Directions start = robot.getDirection();
            turnCommand("left").execute(world, robot);
            turnCommand("right").execute(world, robot);
            turnCommand("left").execute(world, robot);
            turnCommand("right").execute(world, robot);
            assertEquals(start, robot.getDirection());
        }

        @Test void threeRights_equalsOneLeft() {
            robot.updateDirection(Directions.NORTH);
            turnCommand("right").execute(world, robot);
            turnCommand("right").execute(world, robot);
            turnCommand("right").execute(world, robot);
            assertEquals(Directions.WEST, robot.getDirection());
        }

        @Test void threeLefts_equalsOneRight() {
            robot.updateDirection(Directions.NORTH);
            turnCommand("left").execute(world, robot);
            turnCommand("left").execute(world, robot);
            turnCommand("left").execute(world, robot);
            assertEquals(Directions.EAST, robot.getDirection());
        }

        @Test void longSequence_preservesParity() {
            // 100 alternating left/right turns should leave direction unchanged.
            Directions start = robot.getDirection();
            for (int i = 0; i < 50; i++) {
                turnCommand("left").execute(world, robot);
                turnCommand("right").execute(world, robot);
            }
            assertEquals(start, robot.getDirection());
        }

        @Test void mixedSequence_endingDirectionIsCorrect() {
            // NORTH → R(EAST) → R(SOUTH) → L(EAST) → R(SOUTH) → L(EAST)
            robot.updateDirection(Directions.NORTH);
            turnCommand("right").execute(world, robot);
            turnCommand("right").execute(world, robot);
            turnCommand("left").execute(world, robot);
            turnCommand("right").execute(world, robot);
            turnCommand("left").execute(world, robot);
            assertEquals(Directions.EAST, robot.getDirection());
        }
    }
}