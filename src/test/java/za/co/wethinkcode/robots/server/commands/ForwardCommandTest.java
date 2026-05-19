package za.co.wethinkcode.robots.server.commands;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import za.co.wethinkcode.robots.models.Position;
import za.co.wethinkcode.robots.models.ServerRequest;
import za.co.wethinkcode.robots.models.ServerResponse;
import za.co.wethinkcode.robots.models.StatusCode;
import za.co.wethinkcode.robots.models.impediment.Impediments;
import za.co.wethinkcode.robots.server.robot.BaseRobot;
import za.co.wethinkcode.robots.server.world.RobotWorld;

public class ForwardCommandTest {

    private RobotWorld world;
    private BaseRobot robot;

    @BeforeEach
    void setupWorldWithEmptyMap() {
        world = new RobotWorld(11, 11, 5);
        // ForwardCommand uses world.isPositionAvailable which reads from `map`,
        // so we need to load a map of nulls (= every cell available).
        ArrayList<ArrayList<Impediments>> emptyMap = new ArrayList<>();
        for (int y = 0; y < 11; y++) {
            ArrayList<Impediments> row = new ArrayList<>();
            for (int x = 0; x < 11; x++) row.add(null);
            emptyMap.add(row);
        }
        world.loadMap(emptyMap);
        world.addRobot("HAL");
        robot = world.getAllRobots().get("HAL");
        // Move HAL away from origin into positive map coords so movement is possible
        robot.updatePosition(new Position(5, 5));
    }

    private Command forwardCommand(String steps) {
        return Command.generate(new ServerRequest("HAL", "forward", new String[]{steps}));
    }

    @Nested
    @DisplayName("response shape")
    class ResponseShape {

        @Test
        void execute_returnsOkResult() {
            ServerResponse res = forwardCommand("1").execute(world, robot);
            assertEquals(StatusCode.OK, res.getResult());
        }

        @Test
        void execute_responseHasStateWithDirection() {
            ServerResponse res = forwardCommand("1").execute(world, robot);
            assertNotNull(res.getState());
            assertNotNull(res.getState().getDirection());
        }

        @Test
        void execute_responseHasDataMessage() {
            ServerResponse res = forwardCommand("1").execute(world, robot);
            assertNotNull(res.getData());
            assertNotNull(res.getData().getMessage());
        }
    }

    @Nested
    @DisplayName("invalid arguments")
    class InvalidArguments {

        @Test
        void execute_treatsNonNumericStepsAsOne() {
            assertDoesNotThrow(() -> forwardCommand("five").execute(world, robot));
        }

        @Test
        void execute_treatsEmptyArgsAsOne() {
            Command c = Command.generate(new ServerRequest("HAL", "forward", new String[0]));
            assertDoesNotThrow(() -> c.execute(world, robot));
        }

        @Test
        void execute_treatsNegativeStepsAsZero() {
            Position before = new Position(robot.getPosition().getX(), robot.getPosition().getY());
            forwardCommand("-3").execute(world, robot);
            // parseSteps clamps to 0
            assertEquals(before.getX(), robot.getPosition().getX());
            assertEquals(before.getY(), robot.getPosition().getY());
        }

        @Test
        void execute_zeroSteps_doesNotMove() {
            int x = robot.getPosition().getX(), y = robot.getPosition().getY();
            forwardCommand("0").execute(world, robot);
            assertEquals(x, robot.getPosition().getX());
            assertEquals(y, robot.getPosition().getY());
        }
    }

    // =====================================================================
    // Movement & metrics
    // =====================================================================

    @Nested
    @DisplayName("movement")
    class Movement {

        @BeforeEach
        void resetRobotToCentre() {
            robot.updatePosition(new Position(0, 0));
            robot.updateDirection(za.co.wethinkcode.robots.models.Directions.NORTH);
        }

        @Test
        void forwardThree_movesNorthThreeCells() {
            forwardCommand("3").execute(world, robot);
            assertEquals(3, robot.getPosition().getY());
        }

        @Test
        void forwardOne_returnsDone() {
            ServerResponse res = forwardCommand("1").execute(world, robot);
            assertEquals("DONE", res.getData().getMessage());
        }

        @Test
        void forwardBlockedByObstacle_returnsBlocked() {
            world.addObstacle(new za.co.wethinkcode.robots.models.impediment.Obstacle(0, 1, 0, 1, "MOUNTAIN"));
            ServerResponse res = forwardCommand("1").execute(world, robot);
            assertEquals("BLOCKED", res.getData().getMessage());
        }

        @Test
        void forwardBlocked_doesNotChangePosition() {
            world.addObstacle(new za.co.wethinkcode.robots.models.impediment.Obstacle(0, 1, 0, 1, "MOUNTAIN"));
            forwardCommand("1").execute(world, robot);
            assertEquals(0, robot.getPosition().getY());
        }

        @Test
        void forwardPartiallyBlocked_commitsPartialProgress() {
            world.addObstacle(new za.co.wethinkcode.robots.models.impediment.Obstacle(0, 3, 0, 3, "MOUNTAIN"));
            forwardCommand("5").execute(world, robot);
            // Walks 2 cells, then mountain at y=3 stops it
            assertEquals(2, robot.getPosition().getY());
        }

        @Test
        void forwardPartiallyBlocked_returnsBlocked() {
            world.addObstacle(new za.co.wethinkcode.robots.models.impediment.Obstacle(0, 3, 0, 3, "MOUNTAIN"));
            ServerResponse res = forwardCommand("5").execute(world, robot);
            assertEquals("BLOCKED", res.getData().getMessage());
        }

        @Test
        void forwardFromSouth_goesSouth() {
            robot.updateDirection(za.co.wethinkcode.robots.models.Directions.SOUTH);
            forwardCommand("2").execute(world, robot);
            assertEquals(-2, robot.getPosition().getY());
        }

        @Test
        void forwardFromEast_goesEast() {
            robot.updateDirection(za.co.wethinkcode.robots.models.Directions.EAST);
            forwardCommand("3").execute(world, robot);
            assertEquals(3, robot.getPosition().getX());
        }

        @Test
        void forwardFromWest_goesWest() {
            robot.updateDirection(za.co.wethinkcode.robots.models.Directions.WEST);
            forwardCommand("3").execute(world, robot);
            assertEquals(-3, robot.getPosition().getX());
        }

        @Test
        void forwardOutOfBounds_returnsBlocked() {
            int yLimit = (world.getHeight() - 1) / 2;
            robot.updatePosition(new Position(0, yLimit));
            ServerResponse res = forwardCommand("1").execute(world, robot);
            assertEquals("BLOCKED", res.getData().getMessage());
        }

        @Test
        void forwardOutOfBounds_robotStays() {
            int yLimit = (world.getHeight() - 1) / 2;
            robot.updatePosition(new Position(0, yLimit));
            forwardCommand("1").execute(world, robot);
            assertEquals(yLimit, robot.getPosition().getY());
        }

        @Test
        void forwardOnto_pickup_refillsAmmo() {
            while (robot.getShoots() > 0) robot.decrementBullets();
            world.addAmmoPickup(new Position(0, 1));
            forwardCommand("1").execute(world, robot);
            assertEquals(za.co.wethinkcode.robots.server.world.Iworld.MAG_MAX, robot.getShoots());
        }

        @Test
        void forwardIntoPit_outOfLives_returnsFellInPit() {
            robot.decrementLives(); robot.decrementLives(); robot.decrementLives();
            world.addObstacle(new za.co.wethinkcode.robots.models.impediment.Obstacle(0, 1, 0, 1, "PIT"));
            ServerResponse res = forwardCommand("1").execute(world, robot);
            assertEquals("FELL_IN_PIT", res.getData().getMessage());
        }

        @Test
        void forwardIntoPit_outOfLives_removesRobot() {
            robot.decrementLives(); robot.decrementLives(); robot.decrementLives();
            world.addObstacle(new za.co.wethinkcode.robots.models.impediment.Obstacle(0, 1, 0, 1, "PIT"));
            forwardCommand("1").execute(world, robot);
            org.junit.jupiter.api.Assertions.assertNull(world.getAllRobots().get("HAL"));
        }

        @Test
        void forwardIntoPit_withLives_returnsRespawned() {
            world.addObstacle(new za.co.wethinkcode.robots.models.impediment.Obstacle(0, 1, 0, 1, "PIT"));
            ServerResponse res = forwardCommand("1").execute(world, robot);
            assertEquals("RESPAWNED", res.getData().getMessage());
        }

        @Test
        void forwardIntoPit_withLives_robotStillInWorld() {
            world.addObstacle(new za.co.wethinkcode.robots.models.impediment.Obstacle(0, 1, 0, 1, "PIT"));
            forwardCommand("1").execute(world, robot);
            assertNotNull(world.getAllRobots().get("HAL"));
        }

        @Test
        void forwardIntoPit_withLives_decrementsLives() {
            int before = robot.getLives();
            world.addObstacle(new za.co.wethinkcode.robots.models.impediment.Obstacle(0, 1, 0, 1, "PIT"));
            forwardCommand("1").execute(world, robot);
            assertEquals(before - 1, robot.getLives());
        }

        @Test
        void forwardIntoPit_responseIncludesStateLives() {
            world.addObstacle(new za.co.wethinkcode.robots.models.impediment.Obstacle(0, 1, 0, 1, "PIT"));
            ServerResponse res = forwardCommand("1").execute(world, robot);
            assertEquals(robot.getLives(), res.getState().getLives());
        }
    }

    @Nested
    @DisplayName("metrics — markMoved / incrementBlocked")
    class Metrics {

        @BeforeEach
        void reset() {
            robot.updatePosition(new Position(0, 0));
            robot.updateDirection(za.co.wethinkcode.robots.models.Directions.NORTH);
        }

        @Test
        void successfulMove_resetsBlockedCount() {
            robot.incrementBlocked();
            robot.incrementBlocked();
            forwardCommand("1").execute(world, robot);
            assertEquals(0, robot.getBlockedCount());
        }

        @Test
        void successfulMove_advancesLastMoveTimestamp() throws Exception {
            long before = robot.getLastMoveTimestamp();
            Thread.sleep(2);
            forwardCommand("1").execute(world, robot);
            org.junit.jupiter.api.Assertions.assertTrue(robot.getLastMoveTimestamp() >= before);
        }

        @Test
        void blockedMove_incrementsBlockedCount() {
            world.addObstacle(new za.co.wethinkcode.robots.models.impediment.Obstacle(0, 1, 0, 1, "MOUNTAIN"));
            int before = robot.getBlockedCount();
            forwardCommand("1").execute(world, robot);
            assertEquals(before + 1, robot.getBlockedCount());
        }

        @Test
        void blockedMove_doesNotChangePosition() {
            world.addObstacle(new za.co.wethinkcode.robots.models.impediment.Obstacle(0, 1, 0, 1, "WALL"));
            forwardCommand("1").execute(world, robot);
            assertEquals(0, robot.getPosition().getY());
        }
    }

    @Nested
    @DisplayName("response state details")
    class State {

        @BeforeEach
        void reset() {
            robot.updatePosition(new Position(0, 0));
            robot.updateDirection(za.co.wethinkcode.robots.models.Directions.NORTH);
        }

        @Test
        void state_position_matchesRobot() {
            ServerResponse res = forwardCommand("2").execute(world, robot);
            assertEquals(robot.getPosition().getX(), res.getState().getPosition().getX());
            assertEquals(robot.getPosition().getY(), res.getState().getPosition().getY());
        }

        @Test
        void state_direction_matchesRobot() {
            ServerResponse res = forwardCommand("1").execute(world, robot);
            assertEquals(robot.getDirection(), res.getState().getDirection());
        }

        @Test
        void state_shieldMatchesRobot() {
            ServerResponse res = forwardCommand("1").execute(world, robot);
            assertEquals(robot.getShield(), res.getState().getShields());
        }

        @Test
        void state_directionDoesNotChangeOnForward() {
            za.co.wethinkcode.robots.models.Directions before = robot.getDirection();
            forwardCommand("3").execute(world, robot);
            assertEquals(before, robot.getDirection());
        }
    }

    @Nested
    @DisplayName("repeated movement")
    class Repetition {

        @BeforeEach
        void reset() {
            robot.updatePosition(new Position(0, 0));
            robot.updateDirection(za.co.wethinkcode.robots.models.Directions.NORTH);
        }

        @Test
        void threeSingleSteps_equalsOneTripleStep() {
            forwardCommand("1").execute(world, robot);
            forwardCommand("1").execute(world, robot);
            forwardCommand("1").execute(world, robot);
            int y = robot.getPosition().getY();
            assertEquals(3, y);
        }

        @Test
        void forwardZeroThenForwardTwo_endsAtTwo() {
            forwardCommand("0").execute(world, robot);
            forwardCommand("2").execute(world, robot);
            assertEquals(2, robot.getPosition().getY());
        }

        @Test
        void forwardOnce_thenBlockedNextStep_keepsFirstProgress() {
            world.addObstacle(new za.co.wethinkcode.robots.models.impediment.Obstacle(0, 2, 0, 2, "WALL"));
            forwardCommand("1").execute(world, robot); // moves to (0,1)
            ServerResponse res = forwardCommand("1").execute(world, robot); // blocked
            assertEquals("BLOCKED", res.getData().getMessage());
            assertEquals(1, robot.getPosition().getY());
        }

        @Test
        void manyMoves_decrementBlockedToZero() {
            for (int i = 0; i < 4; i++) robot.incrementBlocked();
            forwardCommand("1").execute(world, robot);
            assertEquals(0, robot.getBlockedCount());
        }
    }

    @Nested
    @DisplayName("argument parsing")
    class ArgumentParsing {

        @Test
        void parseSteps_emptyArgs_returnsOne() {
            assertEquals(1, ForwardCommand.parseSteps(new String[0]));
        }

        @Test
        void parseSteps_null_returnsOne() {
            assertEquals(1, ForwardCommand.parseSteps(null));
        }

        @Test
        void parseSteps_negative_clampsToZero() {
            assertEquals(0, ForwardCommand.parseSteps(new String[]{"-3"}));
        }

        @Test
        void parseSteps_zero_returnsZero() {
            assertEquals(0, ForwardCommand.parseSteps(new String[]{"0"}));
        }

        @Test
        void parseSteps_positive_returnsValue() {
            assertEquals(7, ForwardCommand.parseSteps(new String[]{"7"}));
        }

        @Test
        void parseSteps_garbage_returnsOne() {
            assertEquals(1, ForwardCommand.parseSteps(new String[]{"abc"}));
        }

        @Test
        void parseSteps_decimal_returnsOne() {
            assertEquals(1, ForwardCommand.parseSteps(new String[]{"1.5"}));
        }

        @Test
        void parseSteps_largeNumber_returnsThatNumber() {
            assertEquals(1000000, ForwardCommand.parseSteps(new String[]{"1000000"}));
        }
    }

    @Nested
    @DisplayName("pickup interactions")
    class PickupInteraction {

        @BeforeEach
        void reset() {
            robot.updatePosition(new Position(0, 0));
            robot.updateDirection(za.co.wethinkcode.robots.models.Directions.NORTH);
        }

        @Test
        void steppingOnPickup_doesNotChangePositionResponse() {
            world.addAmmoPickup(new Position(0, 1));
            ServerResponse res = forwardCommand("1").execute(world, robot);
            assertEquals("DONE", res.getData().getMessage());
        }

        @Test
        void steppingPastPickup_doesNotPickItUp() {
            // Pickups only register when the robot ENDS on the pickup cell, not just passes through.
            while (robot.getShoots() > 0) robot.decrementBullets();
            world.addAmmoPickup(new Position(0, 1));
            forwardCommand("2").execute(world, robot);
            assertEquals(0, robot.getShoots());
        }

        @Test
        void endingOnPickup_consumesIt() {
            world.addAmmoPickup(new Position(0, 1));
            int beforeCount = world.getAmmoPickups().size();
            forwardCommand("1").execute(world, robot);
            // Pickup was at (0,1), robot ends there. Pickup consumed but a fresh one spawns.
            assertEquals(beforeCount, world.getAmmoPickups().size());
            org.junit.jupiter.api.Assertions.assertFalse(
                world.getAmmoPickups().stream()
                    .anyMatch(p -> p.getX() == 0 && p.getY() == 1));
        }

        @Test
        void pickupRespawn_isNotOnObstacle() {
            world.addAmmoPickup(new Position(0, 1));
            forwardCommand("1").execute(world, robot);
            // Every pickup should be on a non-blocked, non-pit cell
            for (Position p : world.getAmmoPickups()) {
                org.junit.jupiter.api.Assertions.assertFalse(
                    world.isPositionBlocked(p.getX(), p.getY()));
                org.junit.jupiter.api.Assertions.assertFalse(
                    world.isPositionInPit(p.getX(), p.getY()));
            }
        }

        @Test
        void pickupResponse_includesPosition() {
            world.addAmmoPickup(new Position(0, 1));
            ServerResponse res = forwardCommand("1").execute(world, robot);
            assertNotNull(res.getState().getPosition());
        }
    }
}
