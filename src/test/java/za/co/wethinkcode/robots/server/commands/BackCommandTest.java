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

public class BackCommandTest {

    private RobotWorld world;
    private BaseRobot robot;

    @BeforeEach
    void setupWorldWithEmptyMap() {
        world = new RobotWorld(11, 11, 5);
        ArrayList<ArrayList<Impediments>> emptyMap = new ArrayList<>();
        for (int y = 0; y < 11; y++) {
            ArrayList<Impediments> row = new ArrayList<>();
            for (int x = 0; x < 11; x++) row.add(null);
            emptyMap.add(row);
        }
        world.loadMap(emptyMap);
        world.addRobot("HAL");
        robot = world.getAllRobots().get("HAL");
        robot.updatePosition(new Position(5, 5));
    }

    private Command backCommand(String steps) {
        return Command.generate(new ServerRequest("HAL", "back", new String[]{steps}));
    }

    @Nested
    @DisplayName("response shape")
    class ResponseShape {

        @Test
        void execute_returnsOkResult() {
            ServerResponse res = backCommand("1").execute(world, robot);
            assertEquals(StatusCode.OK, res.getResult());
        }

        @Test
        void execute_responseHasState() {
            ServerResponse res = backCommand("1").execute(world, robot);
            assertNotNull(res.getState());
        }

        @Test
        void execute_responseHasDataMessage() {
            ServerResponse res = backCommand("1").execute(world, robot);
            assertNotNull(res.getData());
            assertNotNull(res.getData().getMessage());
        }
    }

    @Nested
    @DisplayName("invalid arguments")
    class InvalidArguments {

        @Test
        void execute_treatsNonNumericStepsAsOne() {
            assertDoesNotThrow(() -> backCommand("two").execute(world, robot));
        }

        @Test
        void execute_emptyArgs_doesNotThrow() {
            Command c = Command.generate(new ServerRequest("HAL", "back", new String[0]));
            assertDoesNotThrow(() -> c.execute(world, robot));
        }

        @Test
        void execute_negativeArg_doesNotThrow() {
            assertDoesNotThrow(() -> backCommand("-2").execute(world, robot));
        }

        @Test
        void execute_zeroSteps_doesNotMove() {
            int x = robot.getPosition().getX(), y = robot.getPosition().getY();
            backCommand("0").execute(world, robot);
            assertEquals(x, robot.getPosition().getX());
            assertEquals(y, robot.getPosition().getY());
        }
    }

    @Nested
    @DisplayName("movement")
    class Movement {

        @BeforeEach
        void reset() {
            robot.updatePosition(new Position(0, 0));
            robot.updateDirection(za.co.wethinkcode.robots.models.Directions.NORTH);
        }

        @Test
        void backOneFromNorth_movesSouth() {
            backCommand("1").execute(world, robot);
            assertEquals(-1, robot.getPosition().getY());
        }

        @Test
        void backTwoFromNorth_movesSouthTwo() {
            backCommand("2").execute(world, robot);
            assertEquals(-2, robot.getPosition().getY());
        }

        @Test
        void backFromSouth_movesNorth() {
            robot.updateDirection(za.co.wethinkcode.robots.models.Directions.SOUTH);
            backCommand("2").execute(world, robot);
            assertEquals(2, robot.getPosition().getY());
        }

        @Test
        void backFromEast_movesWest() {
            robot.updateDirection(za.co.wethinkcode.robots.models.Directions.EAST);
            backCommand("3").execute(world, robot);
            assertEquals(-3, robot.getPosition().getX());
        }

        @Test
        void backFromWest_movesEast() {
            robot.updateDirection(za.co.wethinkcode.robots.models.Directions.WEST);
            backCommand("3").execute(world, robot);
            assertEquals(3, robot.getPosition().getX());
        }

        @Test
        void back_succeeds_returnsDone() {
            ServerResponse res = backCommand("1").execute(world, robot);
            assertEquals("DONE", res.getData().getMessage());
        }

        @Test
        void backBlockedByObstacle_returnsBlocked() {
            world.addObstacle(new za.co.wethinkcode.robots.models.impediment.Obstacle(0, -1, 0, -1, "WALL"));
            ServerResponse res = backCommand("1").execute(world, robot);
            assertEquals("BLOCKED", res.getData().getMessage());
        }

        @Test
        void backBlockedByObstacle_doesNotChangePosition() {
            world.addObstacle(new za.co.wethinkcode.robots.models.impediment.Obstacle(0, -1, 0, -1, "MOUNTAIN"));
            backCommand("1").execute(world, robot);
            assertEquals(0, robot.getPosition().getY());
        }

        @Test
        void backPartiallyBlocked_commitsPartial() {
            world.addObstacle(new za.co.wethinkcode.robots.models.impediment.Obstacle(0, -3, 0, -3, "MOUNTAIN"));
            backCommand("5").execute(world, robot);
            assertEquals(-2, robot.getPosition().getY());
        }

        @Test
        void backOutOfBounds_returnsBlocked() {
            int yLimit = (world.getHeight() - 1) / 2;
            robot.updatePosition(new Position(0, -yLimit));
            ServerResponse res = backCommand("1").execute(world, robot);
            assertEquals("BLOCKED", res.getData().getMessage());
        }

        @Test
        void backDoesNotChangeDirection() {
            za.co.wethinkcode.robots.models.Directions before = robot.getDirection();
            backCommand("2").execute(world, robot);
            assertEquals(before, robot.getDirection());
        }

        @Test
        void back_thenForward_returnsToOrigin() {
            backCommand("3").execute(world, robot);
            Command.generate(new ServerRequest("HAL", "forward", new String[]{"3"}))
                   .execute(world, robot);
            assertEquals(0, robot.getPosition().getY());
        }
    }

    @Nested
    @DisplayName("pit interactions")
    class PitInteractions {

        @BeforeEach
        void reset() {
            robot.updatePosition(new Position(0, 0));
            robot.updateDirection(za.co.wethinkcode.robots.models.Directions.NORTH);
        }

        @Test
        void backIntoPit_outOfLives_returnsFellInPit() {
            robot.decrementLives(); robot.decrementLives(); robot.decrementLives();
            world.addObstacle(new za.co.wethinkcode.robots.models.impediment.Obstacle(0, -1, 0, -1, "PIT"));
            ServerResponse res = backCommand("1").execute(world, robot);
            assertEquals("FELL_IN_PIT", res.getData().getMessage());
        }

        @Test
        void backIntoPit_outOfLives_removesRobot() {
            robot.decrementLives(); robot.decrementLives(); robot.decrementLives();
            world.addObstacle(new za.co.wethinkcode.robots.models.impediment.Obstacle(0, -1, 0, -1, "PIT"));
            backCommand("1").execute(world, robot);
            org.junit.jupiter.api.Assertions.assertNull(world.getAllRobots().get("HAL"));
        }

        @Test
        void backIntoPit_withLives_returnsRespawned() {
            world.addObstacle(new za.co.wethinkcode.robots.models.impediment.Obstacle(0, -1, 0, -1, "PIT"));
            ServerResponse res = backCommand("1").execute(world, robot);
            assertEquals("RESPAWNED", res.getData().getMessage());
        }

        @Test
        void backIntoPit_withLives_decrementsLives() {
            int before = robot.getLives();
            world.addObstacle(new za.co.wethinkcode.robots.models.impediment.Obstacle(0, -1, 0, -1, "PIT"));
            backCommand("1").execute(world, robot);
            assertEquals(before - 1, robot.getLives());
        }

        @Test
        void backIntoPit_withLives_robotStillExists() {
            world.addObstacle(new za.co.wethinkcode.robots.models.impediment.Obstacle(0, -1, 0, -1, "PIT"));
            backCommand("1").execute(world, robot);
            assertNotNull(world.getAllRobots().get("HAL"));
        }
    }

    @Nested
    @DisplayName("metrics")
    class Metrics {

        @BeforeEach
        void reset() {
            robot.updatePosition(new Position(0, 0));
            robot.updateDirection(za.co.wethinkcode.robots.models.Directions.NORTH);
        }

        @Test
        void successfulBack_resetsBlockedCount() {
            robot.incrementBlocked();
            robot.incrementBlocked();
            backCommand("1").execute(world, robot);
            assertEquals(0, robot.getBlockedCount());
        }

        @Test
        void blockedBack_incrementsBlockedCount() {
            world.addObstacle(new za.co.wethinkcode.robots.models.impediment.Obstacle(0, -1, 0, -1, "WALL"));
            int before = robot.getBlockedCount();
            backCommand("1").execute(world, robot);
            assertEquals(before + 1, robot.getBlockedCount());
        }

        @Test
        void successfulBack_advancesLastMoveTimestamp() throws Exception {
            long before = robot.getLastMoveTimestamp();
            Thread.sleep(2);
            backCommand("1").execute(world, robot);
            org.junit.jupiter.api.Assertions.assertTrue(robot.getLastMoveTimestamp() >= before);
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
            ServerResponse res = backCommand("2").execute(world, robot);
            assertEquals(robot.getPosition().getX(), res.getState().getPosition().getX());
            assertEquals(robot.getPosition().getY(), res.getState().getPosition().getY());
        }

        @Test
        void state_directionMatchesRobot() {
            ServerResponse res = backCommand("1").execute(world, robot);
            assertEquals(robot.getDirection(), res.getState().getDirection());
        }

        @Test
        void state_shieldMatchesRobot() {
            ServerResponse res = backCommand("1").execute(world, robot);
            assertEquals(robot.getShield(), res.getState().getShields());
        }

        @Test
        void state_pitDeath_includesLives() {
            world.addObstacle(new za.co.wethinkcode.robots.models.impediment.Obstacle(0, -1, 0, -1, "PIT"));
            ServerResponse res = backCommand("1").execute(world, robot);
            assertEquals(robot.getLives(), res.getState().getShields());
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
        void threeSingleBacks_equalsTripleBack() {
            backCommand("1").execute(world, robot);
            backCommand("1").execute(world, robot);
            backCommand("1").execute(world, robot);
            assertEquals(-3, robot.getPosition().getY());
        }

        @Test
        void backThenBlocked_keepsFirstProgress() {
            world.addObstacle(new za.co.wethinkcode.robots.models.impediment.Obstacle(0, -2, 0, -2, "WALL"));
            backCommand("1").execute(world, robot);
            ServerResponse res = backCommand("1").execute(world, robot);
            assertEquals("BLOCKED", res.getData().getMessage());
            assertEquals(-1, robot.getPosition().getY());
        }

        @Test
        void zeroBack_thenBackTwo_endsAtNegativeTwo() {
            backCommand("0").execute(world, robot);
            backCommand("2").execute(world, robot);
            assertEquals(-2, robot.getPosition().getY());
        }

        @Test
        void backIsSymmetricWithForward() {
            // Walk forward 3, back 3 → at origin
            Command.generate(new ServerRequest("HAL", "forward", new String[]{"3"})).execute(world, robot);
            backCommand("3").execute(world, robot);
            assertEquals(0, robot.getPosition().getY());
        }
    }

    @Nested
    @DisplayName("pickups")
    class Pickups {

        @BeforeEach
        void reset() {
            robot.updatePosition(new Position(0, 0));
            robot.updateDirection(za.co.wethinkcode.robots.models.Directions.NORTH);
        }

        @Test
        void backOntoPickup_refillsAmmo() {
            while (robot.getShoots() > 0) robot.decrementBullets();
            world.addAmmoPickup(new Position(0, -1));
            backCommand("1").execute(world, robot);
            assertEquals(za.co.wethinkcode.robots.server.world.Iworld.MAG_MAX, robot.getShoots());
        }

        @Test
        void backOntoPickup_responseIsDone() {
            world.addAmmoPickup(new Position(0, -1));
            ServerResponse res = backCommand("1").execute(world, robot);
            assertEquals("DONE", res.getData().getMessage());
        }

        @Test
        void backPastPickup_doesNotPickup() {
            while (robot.getShoots() > 0) robot.decrementBullets();
            world.addAmmoPickup(new Position(0, -1));
            backCommand("2").execute(world, robot);
            assertEquals(0, robot.getShoots());
        }
    }

    @Nested
    @DisplayName("direction & position edge cases")
    class EdgeCases {

        @BeforeEach
        void reset() {
            robot.updatePosition(new Position(0, 0));
            robot.updateDirection(za.co.wethinkcode.robots.models.Directions.NORTH);
        }

        @Test
        void backByBigNumber_stopsAtWorldEdge() {
            ServerResponse res = backCommand("1000").execute(world, robot);
            assertEquals("BLOCKED", res.getData().getMessage());
            int yLimit = (world.getHeight() - 1) / 2;
            assertEquals(-yLimit, robot.getPosition().getY());
        }

        @Test
        void backAtEdge_returnsBlocked() {
            int yLimit = (world.getHeight() - 1) / 2;
            robot.updatePosition(new Position(0, -yLimit));
            ServerResponse res = backCommand("1").execute(world, robot);
            assertEquals("BLOCKED", res.getData().getMessage());
        }

        @Test
        void backOneStep_facingEast_movesWestOne() {
            robot.updateDirection(za.co.wethinkcode.robots.models.Directions.EAST);
            backCommand("1").execute(world, robot);
            assertEquals(-1, robot.getPosition().getX());
        }

        @Test
        void backOneStep_facingWest_movesEastOne() {
            robot.updateDirection(za.co.wethinkcode.robots.models.Directions.WEST);
            backCommand("1").execute(world, robot);
            assertEquals(1, robot.getPosition().getX());
        }

        @Test
        void backDoesNotChangeShield() {
            int before = robot.getShield();
            backCommand("2").execute(world, robot);
            assertEquals(before, robot.getShield());
        }

        @Test
        void backDoesNotChangeAmmo() {
            int before = robot.getShoots();
            backCommand("2").execute(world, robot);
            assertEquals(before, robot.getShoots());
        }

        @Test
        void backDoesNotChangeKills() {
            int before = robot.getKills();
            backCommand("2").execute(world, robot);
            assertEquals(before, robot.getKills());
        }

        @Test
        void backDoesNotChangeLives_whenNotInPit() {
            int before = robot.getLives();
            backCommand("2").execute(world, robot);
            assertEquals(before, robot.getLives());
        }

        @Test
        void state_existsForRespawn() {
            world.addObstacle(new za.co.wethinkcode.robots.models.impediment.Obstacle(0, -1, 0, -1, "PIT"));
            ServerResponse res = backCommand("1").execute(world, robot);
            assertNotNull(res.getState());
        }

        @Test
        void backWithExtraArgs_usesFirstArgOnly() {
            Command c = Command.generate(new ServerRequest("HAL", "back",
                    new String[]{"2", "ignored", "garbage"}));
            c.execute(world, robot);
            assertEquals(-2, robot.getPosition().getY());
        }

        @Test
        void back_returnsOk_evenWhenBlocked() {
            world.addObstacle(new za.co.wethinkcode.robots.models.impediment.Obstacle(0, -1, 0, -1, "MOUNTAIN"));
            ServerResponse res = backCommand("1").execute(world, robot);
            assertEquals(StatusCode.OK, res.getResult());
        }

        @Test
        void back_intoMountainAt_distanceOne_isPureBlock() {
            world.addObstacle(new za.co.wethinkcode.robots.models.impediment.Obstacle(0, -1, 0, -1, "MOUNTAIN"));
            backCommand("3").execute(world, robot);
            assertEquals(0, robot.getPosition().getY());
        }

        @Test
        void back_intoMountainAt_distanceTwo_partialOne() {
            world.addObstacle(new za.co.wethinkcode.robots.models.impediment.Obstacle(0, -2, 0, -2, "MOUNTAIN"));
            backCommand("5").execute(world, robot);
            assertEquals(-1, robot.getPosition().getY());
        }
    }
}
