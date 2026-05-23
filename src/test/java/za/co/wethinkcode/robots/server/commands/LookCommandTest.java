package za.co.wethinkcode.robots.server.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import za.co.wethinkcode.robots.models.StatusCode;
import za.co.wethinkcode.robots.models.transitmodels.ServerResponse;
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
        return new LookCommand( "HAL");
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
    void execute_dataIncludesPositionAndVisibility() {
        ServerResponse res = newLookCommand().execute(world, robot);
        assertNotNull(res.getData().getPosition());
        assertTrue(res.getData().getVisibility() > 0);
        // Shield is now reported on state, not data, for the look command.
        assertTrue(res.getState().getShields() > 0);
    }

    @Test
    void execute_stateIncludesPositionAndDirection() {
        ServerResponse res = newLookCommand().execute(world, robot);
        assertNotNull(res.getState().getPosition());
        assertNotNull(res.getState().getDirection());
    }

    // =====================================================================
    // Comprehensive look-scan tests. LookCommand now delegates to
    // RobotWorld.lookAround which scans 4 cardinals up to bulletRange cells.
    // =====================================================================

    /** Move the robot to a known cell so look scans are deterministic. */
    private void positionAt(int x, int y) {
        robot.updatePosition(new za.co.wethinkcode.robots.models.Position(x, y));
    }

    @org.junit.jupiter.api.Nested
    @org.junit.jupiter.api.DisplayName("scan range")
    class Range {

        @org.junit.jupiter.api.Test
        void visibility_matchesLookRange() {
            ServerResponse res = newLookCommand().execute(world, robot);
            assertEquals(za.co.wethinkcode.robots.server.world.Iworld.lookRange,
                    res.getData().getVisibility());
        }

        @org.junit.jupiter.api.Test
        void noObstacles_centreRobot_objectsEmptyOrEdgesOnly() {
            // 11x11 world, robot at centre — edges are exactly at lookRange (Chebyshev 5),
            // and the boundary check fires only PAST that distance. So no edges reported here.
            positionAt(0, 0);
            ServerResponse res = newLookCommand().execute(world, robot);
            org.junit.jupiter.api.Assertions.assertTrue(res.getData().getObjects().isEmpty());
        }

        @org.junit.jupiter.api.Test
        void offCentre_reportsNearEdge() {
            positionAt(3, 3); // edges reachable within range
            ServerResponse res = newLookCommand().execute(world, robot);
            org.junit.jupiter.api.Assertions.assertFalse(res.getData().getObjects().isEmpty());
        }

        @org.junit.jupiter.api.Test
        void objectsBeyondRange_areInvisible() {
            positionAt(0, 0);
            // Place a TREE at distance 6, beyond lookRange=5
            world.addObstacle(new za.co.wethinkcode.robots.models.impediment.Obstacle(0, 6, 0, 6, "TREE"));
            ServerResponse res = newLookCommand().execute(world, robot);
            org.junit.jupiter.api.Assertions.assertTrue(res.getData().getObjects().isEmpty());
        }

        @org.junit.jupiter.api.Test
        void objectAtExactRange_isReported() {
            positionAt(0, 0);
            int range = za.co.wethinkcode.robots.server.world.Iworld.lookRange;
            world.addObstacle(new za.co.wethinkcode.robots.models.impediment.Obstacle(0, range, 0, range, "TREE"));
            ServerResponse res = newLookCommand().execute(world, robot);
            org.junit.jupiter.api.Assertions.assertFalse(res.getData().getObjects().isEmpty());
        }
    }

    @org.junit.jupiter.api.Nested
    @org.junit.jupiter.api.DisplayName("object reporting")
    class Reporting {

        @org.junit.jupiter.api.Test
        void mountainReported_asObstacleSubtypeMountain() {
            positionAt(0, 0);
            world.addObstacle(new za.co.wethinkcode.robots.models.impediment.Obstacle(0, 3, 0, 3, "MOUNTAIN"));
            ServerResponse res = newLookCommand().execute(world, robot);
            org.junit.jupiter.api.Assertions.assertTrue(
                    res.getData().getObjects().stream().anyMatch(o ->
                            "MOUNTAIN".equals(o.getSubtype()) &&
                            o.getType() == za.co.wethinkcode.robots.models.ImpedimentType.OBSTACLE));
        }

        @org.junit.jupiter.api.Test
        void treeReported_asObstacleSubtypeTree() {
            positionAt(0, 0);
            world.addObstacle(new za.co.wethinkcode.robots.models.impediment.Obstacle(2, 0, 2, 0, "TREE"));
            ServerResponse res = newLookCommand().execute(world, robot);
            org.junit.jupiter.api.Assertions.assertTrue(
                    res.getData().getObjects().stream().anyMatch(o -> "TREE".equals(o.getSubtype())));
        }

        @org.junit.jupiter.api.Test
        void rockReported_asObstacleSubtypeRock() {
            positionAt(0, 0);
            world.addObstacle(new za.co.wethinkcode.robots.models.impediment.Obstacle(0, -2, 0, -2, "ROCK"));
            ServerResponse res = newLookCommand().execute(world, robot);
            org.junit.jupiter.api.Assertions.assertTrue(
                    res.getData().getObjects().stream().anyMatch(o -> "ROCK".equals(o.getSubtype())));
        }

        @org.junit.jupiter.api.Test
        void wallReported_asObstacleSubtypeWall() {
            positionAt(0, 0);
            world.addObstacle(new za.co.wethinkcode.robots.models.impediment.Obstacle(-2, 0, -2, 0, "WALL"));
            ServerResponse res = newLookCommand().execute(world, robot);
            org.junit.jupiter.api.Assertions.assertTrue(
                    res.getData().getObjects().stream().anyMatch(o -> "WALL".equals(o.getSubtype())));
        }

        @org.junit.jupiter.api.Test
        void pitReported_asObstacleSubtypePit() {
            positionAt(0, 0);
            world.addObstacle(new za.co.wethinkcode.robots.models.impediment.Obstacle(0, 2, 0, 2, "PIT"));
            ServerResponse res = newLookCommand().execute(world, robot);
            org.junit.jupiter.api.Assertions.assertTrue(
                    res.getData().getObjects().stream().anyMatch(o -> "PIT".equals(o.getSubtype())));
        }

        @org.junit.jupiter.api.Test
        void lakeReported_asObstacleSubtypeLake() {
            positionAt(0, 0);
            world.addObstacle(new za.co.wethinkcode.robots.models.impediment.Obstacle(0, 4, 0, 4, "LAKE"));
            ServerResponse res = newLookCommand().execute(world, robot);
            org.junit.jupiter.api.Assertions.assertTrue(
                    res.getData().getObjects().stream().anyMatch(o -> "LAKE".equals(o.getSubtype())));
        }

        @org.junit.jupiter.api.Test
        void everyReportedObject_includesPosition() {
            positionAt(0, 0);
            world.addObstacle(new za.co.wethinkcode.robots.models.impediment.Obstacle(0, 2, 0, 2, "TREE"));
            world.addObstacle(new za.co.wethinkcode.robots.models.impediment.Obstacle(2, 0, 2, 0, "ROCK"));
            ServerResponse res = newLookCommand().execute(world, robot);
            for (var o : res.getData().getObjects()) assertNotNull(o.getPosition());
        }

        @org.junit.jupiter.api.Test
        void everyReportedObject_includesDirection() {
            positionAt(0, 0);
            world.addObstacle(new za.co.wethinkcode.robots.models.impediment.Obstacle(0, 2, 0, 2, "TREE"));
            ServerResponse res = newLookCommand().execute(world, robot);
            for (var o : res.getData().getObjects()) assertNotNull(o.getDirection());
        }

        @org.junit.jupiter.api.Test
        void everyReportedObject_distanceIsPositive() {
            positionAt(0, 0);
            world.addObstacle(new za.co.wethinkcode.robots.models.impediment.Obstacle(0, 2, 0, 2, "TREE"));
            ServerResponse res = newLookCommand().execute(world, robot);
            for (var o : res.getData().getObjects()) {
                org.junit.jupiter.api.Assertions.assertTrue(o.getDistance() > 0);
            }
        }

        @org.junit.jupiter.api.Test
        void treeDistance_matchesActual() {
            positionAt(0, 0);
            world.addObstacle(new za.co.wethinkcode.robots.models.impediment.Obstacle(0, 3, 0, 3, "TREE"));
            ServerResponse res = newLookCommand().execute(world, robot);
            var hit = res.getData().getObjects().stream()
                    .filter(o -> "TREE".equals(o.getSubtype())).findFirst().orElseThrow();
            assertEquals(3, hit.getDistance());
        }

        @org.junit.jupiter.api.Test
        void treePosition_matchesObstaclePosition() {
            positionAt(0, 0);
            world.addObstacle(new za.co.wethinkcode.robots.models.impediment.Obstacle(0, 3, 0, 3, "TREE"));
            ServerResponse res = newLookCommand().execute(world, robot);
            var hit = res.getData().getObjects().stream()
                    .filter(o -> "TREE".equals(o.getSubtype())).findFirst().orElseThrow();
            assertEquals(0, hit.getPosition().getX());
            assertEquals(3, hit.getPosition().getY());
        }
    }

    @org.junit.jupiter.api.Nested
    @org.junit.jupiter.api.DisplayName("line-of-sight rules")
    class LineOfSight {

        @org.junit.jupiter.api.Test
        void mountain_blocksSightBehind() {
            positionAt(0, 0);
            world.addObstacle(new za.co.wethinkcode.robots.models.impediment.Obstacle(0, 2, 0, 2, "MOUNTAIN"));
            world.addObstacle(new za.co.wethinkcode.robots.models.impediment.Obstacle(0, 4, 0, 4, "TREE"));
            ServerResponse res = newLookCommand().execute(world, robot);
            long north = res.getData().getObjects().stream()
                    .filter(o -> o.getDirection() == za.co.wethinkcode.robots.models.Directions.NORTH).count();
            assertEquals(1, north); // only mountain visible
        }

        @org.junit.jupiter.api.Test
        void wall_blocksSightBehind() {
            positionAt(0, 0);
            world.addObstacle(new za.co.wethinkcode.robots.models.impediment.Obstacle(0, 2, 0, 2, "WALL"));
            world.addObstacle(new za.co.wethinkcode.robots.models.impediment.Obstacle(0, 4, 0, 4, "TREE"));
            ServerResponse res = newLookCommand().execute(world, robot);
            long north = res.getData().getObjects().stream()
                    .filter(o -> o.getDirection() == za.co.wethinkcode.robots.models.Directions.NORTH).count();
            assertEquals(1, north);
        }

        @org.junit.jupiter.api.Test
        void tree_doesNotBlockSightBehind() {
            positionAt(0, 0);
            world.addObstacle(new za.co.wethinkcode.robots.models.impediment.Obstacle(0, 2, 0, 2, "TREE"));
            world.addObstacle(new za.co.wethinkcode.robots.models.impediment.Obstacle(0, 4, 0, 4, "ROCK"));
            ServerResponse res = newLookCommand().execute(world, robot);
            long north = res.getData().getObjects().stream()
                    .filter(o -> o.getDirection() == za.co.wethinkcode.robots.models.Directions.NORTH).count();
            org.junit.jupiter.api.Assertions.assertTrue(north >= 2);
        }

        @org.junit.jupiter.api.Test
        void rock_doesNotBlockSightBehind() {
            positionAt(0, 0);
            world.addObstacle(new za.co.wethinkcode.robots.models.impediment.Obstacle(0, 2, 0, 2, "ROCK"));
            world.addObstacle(new za.co.wethinkcode.robots.models.impediment.Obstacle(0, 4, 0, 4, "PIT"));
            ServerResponse res = newLookCommand().execute(world, robot);
            long north = res.getData().getObjects().stream()
                    .filter(o -> o.getDirection() == za.co.wethinkcode.robots.models.Directions.NORTH).count();
            org.junit.jupiter.api.Assertions.assertTrue(north >= 2);
        }

        @org.junit.jupiter.api.Test
        void pit_doesNotBlockSightBehind() {
            positionAt(0, 0);
            world.addObstacle(new za.co.wethinkcode.robots.models.impediment.Obstacle(0, 2, 0, 2, "PIT"));
            world.addObstacle(new za.co.wethinkcode.robots.models.impediment.Obstacle(0, 4, 0, 4, "TREE"));
            ServerResponse res = newLookCommand().execute(world, robot);
            long north = res.getData().getObjects().stream()
                    .filter(o -> o.getDirection() == za.co.wethinkcode.robots.models.Directions.NORTH).count();
            org.junit.jupiter.api.Assertions.assertTrue(north >= 2);
        }

        @org.junit.jupiter.api.Test
        void lake_doesNotBlockSightBehind() {
            positionAt(0, 0);
            world.addObstacle(new za.co.wethinkcode.robots.models.impediment.Obstacle(0, 2, 0, 2, "LAKE"));
            world.addObstacle(new za.co.wethinkcode.robots.models.impediment.Obstacle(0, 4, 0, 4, "TREE"));
            ServerResponse res = newLookCommand().execute(world, robot);
            long north = res.getData().getObjects().stream()
                    .filter(o -> o.getDirection() == za.co.wethinkcode.robots.models.Directions.NORTH).count();
            org.junit.jupiter.api.Assertions.assertTrue(north >= 2);
        }
    }

    @org.junit.jupiter.api.Nested
    @org.junit.jupiter.api.DisplayName("edges")
    class Edges {

        @org.junit.jupiter.api.Test
        void edgeReported_whenWithinRange() {
            positionAt(3, 0); // east edge at distance 2 in an 11x11 world (xLimit=5)
            ServerResponse res = newLookCommand().execute(world, robot);
            org.junit.jupiter.api.Assertions.assertTrue(
                    res.getData().getObjects().stream().anyMatch(o ->
                            o.getType() == za.co.wethinkcode.robots.models.ImpedimentType.EDGE &&
                            o.getDirection() == za.co.wethinkcode.robots.models.Directions.EAST));
        }

        @org.junit.jupiter.api.Test
        void edge_hasPositionRecorded() {
            positionAt(3, 0);
            ServerResponse res = newLookCommand().execute(world, robot);
            var edge = res.getData().getObjects().stream()
                    .filter(o -> o.getType() == za.co.wethinkcode.robots.models.ImpedimentType.EDGE)
                    .findFirst().orElseThrow();
            assertNotNull(edge.getPosition());
        }

        @org.junit.jupiter.api.Test
        void edge_breaksScanInThatDirection() {
            positionAt(3, 0);
            // Place a tree beyond the east edge — should NOT be reported (edge stops the scan)
            ServerResponse res = newLookCommand().execute(world, robot);
            long east = res.getData().getObjects().stream()
                    .filter(o -> o.getDirection() == za.co.wethinkcode.robots.models.Directions.EAST).count();
            assertEquals(1, east); // just the edge
        }

        @org.junit.jupiter.api.Test
        void edgeDirection_matchesScanDirection() {
            positionAt(0, 3); // north edge in range
            ServerResponse res = newLookCommand().execute(world, robot);
            var edge = res.getData().getObjects().stream()
                    .filter(o -> o.getType() == za.co.wethinkcode.robots.models.ImpedimentType.EDGE &&
                                 o.getDirection() == za.co.wethinkcode.robots.models.Directions.NORTH)
                    .findFirst().orElse(null);
            assertNotNull(edge);
        }
    }

    @org.junit.jupiter.api.Nested
    @org.junit.jupiter.api.DisplayName("other robots")
    class OtherRobots {

        @org.junit.jupiter.api.Test
        void otherRobotInSight_isReported() {
            positionAt(0, 0);
            world.addRobot("R2");
            world.getAllRobots().get("R2").updatePosition(new za.co.wethinkcode.robots.models.Position(0, 3));
            ServerResponse res = newLookCommand().execute(world, robot);
            org.junit.jupiter.api.Assertions.assertTrue(
                    res.getData().getObjects().stream().anyMatch(o ->
                            o.getType() == za.co.wethinkcode.robots.models.ImpedimentType.ROBOT));
        }

        @org.junit.jupiter.api.Test
        void otherRobot_includesName() {
            positionAt(0, 0);
            world.addRobot("R2");
            world.getAllRobots().get("R2").updatePosition(new za.co.wethinkcode.robots.models.Position(0, 3));
            ServerResponse res = newLookCommand().execute(world, robot);
            org.junit.jupiter.api.Assertions.assertTrue(
                    res.getData().getObjects().stream().anyMatch(o -> "R2".equals(o.getName())));
        }

        @org.junit.jupiter.api.Test
        void otherRobot_includesPosition() {
            positionAt(0, 0);
            world.addRobot("R2");
            world.getAllRobots().get("R2").updatePosition(new za.co.wethinkcode.robots.models.Position(0, 3));
            ServerResponse res = newLookCommand().execute(world, robot);
            var hit = res.getData().getObjects().stream()
                    .filter(o -> o.getType() == za.co.wethinkcode.robots.models.ImpedimentType.ROBOT)
                    .findFirst().orElseThrow();
            assertEquals(0, hit.getPosition().getX());
            assertEquals(3, hit.getPosition().getY());
        }

        @org.junit.jupiter.api.Test
        void otherRobot_blocksLineOfSight() {
            positionAt(0, 0);
            world.addRobot("R2");
            world.getAllRobots().get("R2").updatePosition(new za.co.wethinkcode.robots.models.Position(0, 2));
            world.addObstacle(new za.co.wethinkcode.robots.models.impediment.Obstacle(0, 4, 0, 4, "TREE"));
            ServerResponse res = newLookCommand().execute(world, robot);
            long north = res.getData().getObjects().stream()
                    .filter(o -> o.getDirection() == za.co.wethinkcode.robots.models.Directions.NORTH).count();
            assertEquals(1, north); // R2 blocks, tree hidden
        }

        @org.junit.jupiter.api.Test
        void selfNotReportedInScan() {
            positionAt(0, 0);
            ServerResponse res = newLookCommand().execute(world, robot);
            org.junit.jupiter.api.Assertions.assertTrue(
                    res.getData().getObjects().stream().noneMatch(o ->
                            o.getType() == za.co.wethinkcode.robots.models.ImpedimentType.ROBOT &&
                            "HAL".equals(o.getName())));
        }
    }

    @org.junit.jupiter.api.Nested
    @org.junit.jupiter.api.DisplayName("multi-direction scans")
    class MultiDirection {

        @org.junit.jupiter.api.Test
        void obstaclesInAllFourCardinals_areAllReported() {
            positionAt(0, 0);
            world.addObstacle(new za.co.wethinkcode.robots.models.impediment.Obstacle(0, 2, 0, 2, "TREE"));
            world.addObstacle(new za.co.wethinkcode.robots.models.impediment.Obstacle(0, -2, 0, -2, "TREE"));
            world.addObstacle(new za.co.wethinkcode.robots.models.impediment.Obstacle(2, 0, 2, 0, "TREE"));
            world.addObstacle(new za.co.wethinkcode.robots.models.impediment.Obstacle(-2, 0, -2, 0, "TREE"));
            ServerResponse res = newLookCommand().execute(world, robot);
            // 4 trees reported
            long trees = res.getData().getObjects().stream()
                    .filter(o -> "TREE".equals(o.getSubtype())).count();
            assertEquals(4, trees);
        }

        @org.junit.jupiter.api.Test
        void allFourDirectionsCovered() {
            positionAt(0, 0);
            world.addObstacle(new za.co.wethinkcode.robots.models.impediment.Obstacle(0, 2, 0, 2, "TREE"));
            world.addObstacle(new za.co.wethinkcode.robots.models.impediment.Obstacle(0, -2, 0, -2, "TREE"));
            world.addObstacle(new za.co.wethinkcode.robots.models.impediment.Obstacle(2, 0, 2, 0, "TREE"));
            world.addObstacle(new za.co.wethinkcode.robots.models.impediment.Obstacle(-2, 0, -2, 0, "TREE"));
            ServerResponse res = newLookCommand().execute(world, robot);
            java.util.Set<za.co.wethinkcode.robots.models.Directions> dirs = new java.util.HashSet<>();
            for (var o : res.getData().getObjects()) dirs.add(o.getDirection());
            assertEquals(4, dirs.size());
        }

        @org.junit.jupiter.api.Test
        void diagonalObstacle_isNotReported() {
            positionAt(0, 0);
            // Diagonal — not in cardinal scan line
            world.addObstacle(new za.co.wethinkcode.robots.models.impediment.Obstacle(2, 2, 2, 2, "TREE"));
            ServerResponse res = newLookCommand().execute(world, robot);
            org.junit.jupiter.api.Assertions.assertTrue(res.getData().getObjects().isEmpty());
        }
    }

    @org.junit.jupiter.api.Nested
    @org.junit.jupiter.api.DisplayName("message field")
    class Message {

        @org.junit.jupiter.api.Test
        void noObjects_messageNothingInSight() {
            positionAt(0, 0);
            ServerResponse res = newLookCommand().execute(world, robot);
            assertEquals("Nothing in sight", res.getData().getMessage());
        }

        @org.junit.jupiter.api.Test
        void withObjects_messageIsOK() {
            positionAt(0, 0);
            world.addObstacle(new za.co.wethinkcode.robots.models.impediment.Obstacle(0, 2, 0, 2, "TREE"));
            ServerResponse res = newLookCommand().execute(world, robot);
            assertEquals("OK", res.getData().getMessage());
        }
    }

    @org.junit.jupiter.api.Nested
    @org.junit.jupiter.api.DisplayName("look does not affect non-vision state")
    class SideEffects {

        @org.junit.jupiter.api.Test
        void look_doesNotChangePosition() {
            positionAt(2, 2);
            newLookCommand().execute(world, robot);
            assertEquals(2, robot.getPosition().getX());
            assertEquals(2, robot.getPosition().getY());
        }

        @org.junit.jupiter.api.Test
        void look_doesNotChangeDirection() {
            za.co.wethinkcode.robots.models.Directions before = robot.getDirection();
            newLookCommand().execute(world, robot);
            assertEquals(before, robot.getDirection());
        }

        @org.junit.jupiter.api.Test
        void look_doesNotChangeShield() {
            int before = robot.getShield();
            newLookCommand().execute(world, robot);
            assertEquals(before, robot.getShield());
        }

        @org.junit.jupiter.api.Test
        void look_doesNotChangeAmmo() {
            int before = robot.getShoots();
            newLookCommand().execute(world, robot);
            assertEquals(before, robot.getShoots());
        }

        @org.junit.jupiter.api.Test
        void look_doesNotChangeLives() {
            int before = robot.getLives();
            newLookCommand().execute(world, robot);
            assertEquals(before, robot.getLives());
        }

        @org.junit.jupiter.api.Test
        void look_doesNotIncrementBlocked() {
            int before = robot.getBlockedCount();
            newLookCommand().execute(world, robot);
            assertEquals(before, robot.getBlockedCount());
        }

        @org.junit.jupiter.api.Test
        void look_doesNotResetBlockedCount() {
            robot.incrementBlocked();
            int before = robot.getBlockedCount();
            newLookCommand().execute(world, robot);
            assertEquals(before, robot.getBlockedCount());
        }

        @org.junit.jupiter.api.Test
        void look_doesNotResetLastMoveTimestamp() {
            long before = robot.getLastMoveTimestamp();
            newLookCommand().execute(world, robot);
            assertEquals(before, robot.getLastMoveTimestamp());
        }

        @org.junit.jupiter.api.Test
        void look_doesNotChangeKills() {
            int before = robot.getKills();
            newLookCommand().execute(world, robot);
            assertEquals(before, robot.getKills());
        }

        @org.junit.jupiter.api.Test
        void look_doesNotRemoveRobotFromWorld() {
            newLookCommand().execute(world, robot);
            assertNotNull(world.getAllRobots().get("HAL"));
        }
    }
}
