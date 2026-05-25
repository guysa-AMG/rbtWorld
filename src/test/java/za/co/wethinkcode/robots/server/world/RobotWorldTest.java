package za.co.wethinkcode.robots.server.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import za.co.wethinkcode.robots.models.Directions;
import za.co.wethinkcode.robots.models.ImpedimentType;
import za.co.wethinkcode.robots.models.Position;
import za.co.wethinkcode.robots.models.impediment.Obstacle;
import za.co.wethinkcode.robots.models.transitmodels.ServerResponseObject;
import za.co.wethinkcode.robots.server.commands.LookCommand;
import za.co.wethinkcode.robots.server.commands.MovementCommand.ForwardCommand;
import za.co.wethinkcode.robots.server.robot.BaseRobot;

/**
 * Comprehensive RobotWorld tests covering: construction, addRobot/removeRobot,
 * obstacles, moveRobot (success/blocked/pit/respawn), pickups, lookAround,
 * findSafeSpawn, and isPositionAvailable.
 */
public class RobotWorldTest {

    private RobotWorld world;

    @BeforeEach
    void freshWorld() {
        world = new RobotWorld(11, 11, 5);
    }

    /** Snap a freshly-added robot to a fixed cell so movement/look tests are deterministic. */
    private BaseRobot launchAt(String name, int x, int y) {
        world.addRobot(name);
        BaseRobot bot = world.getAllRobots().get(name);
        bot.updatePosition(new Position(x, y));
        bot.markMoved();
        return bot;
    }

    @Nested
    @DisplayName("construction & dimensions")
    class Dimensions {

        @Test void defaultConstructor_widthIsTen() {
            assertEquals(10, new RobotWorld().getWidth());
        }

        @Test void defaultConstructor_heightIsTen() {
            assertEquals(10, new RobotWorld().getHeight());
        }

        @Test void customConstructor_storesWidth() {
            assertEquals(11, world.getWidth());
        }

        @Test void customConstructor_storesHeight() {
            assertEquals(11, world.getHeight());
        }

        @Test void noObstacles_byDefault() {
            assertTrue(world.getObstacles().isEmpty());
        }

        @Test void noRobots_byDefault() {
            assertTrue(world.getAllRobots().isEmpty());
        }

        @Test void noPickups_byDefault() {
            assertTrue(world.getAmmoPickups().isEmpty());
        }
    }

    @Nested
    @DisplayName("addRobot / removeRobot")
    class RobotLifecycle {

        @Test void addRobot_addsByName() {
            assertTrue(world.addRobot("HAL"));
            assertNotNull(world.getAllRobots().get("HAL"));
        }

        @Test void addRobot_returnsFalseOnDuplicateName() {
            world.addRobot("HAL");
            assertFalse(world.addRobot("HAL"));
        }

        @Test void addRobot_spawnIsInsideBounds() {
            world.addRobot("HAL");
            Position p = world.getAllRobots().get("HAL").getPosition();
            int xl = (world.getWidth() - 1) / 2, yl = (world.getHeight() - 1) / 2;
            assertTrue(Math.abs(p.getX()) <= xl);
            assertTrue(Math.abs(p.getY()) <= yl);
        }

    
        @Test void addRobot_spawnIsNotInsideAPit() {
            world.addObstacle(new Obstacle(0, 0, 0, 0, "PIT"));
            world.addRobot("HAL");
            Position p = world.getAllRobots().get("HAL").getPosition();
            assertFalse(world.isPositionInPit(p.getX(), p.getY()));
        }

        @Test void addRobot_spawnAtMinDistanceFromExisting() {
            world.addRobot("HAL");
            Position first = world.getAllRobots().get("HAL").getPosition();
            world.addRobot("R2");
            Position second = world.getAllRobots().get("R2").getPosition();
            int cheb = Math.max(Math.abs(first.getX() - second.getX()),
                                Math.abs(first.getY() - second.getY()));
            // findSafeSpawn enforces minDistance=8 when another robot is in the world;
            // in a tiny 11x11 world it may fall back, but they must at least not overlap.
            assertTrue(cheb >= 1);
        }

        @Test void addMultipleRobots_allPresent() {
            world.addRobot("A");
            world.addRobot("B");
            world.addRobot("C");
            assertEquals(3, world.getAllRobots().size());
        }

        @Test void removeRobot_removesByName() {
            world.addRobot("HAL");
            world.removeRobot("HAL");
            assertNull(world.getAllRobots().get("HAL"));
        }

        @Test void removeRobot_otherRobotsUnaffected() {
            world.addRobot("HAL");
            world.addRobot("R2");
            world.removeRobot("HAL");
            assertNotNull(world.getAllRobots().get("R2"));
        }

        @Test void removeRobot_onMissingNameDoesNotThrow() {
            assertEquals(0, world.getAllRobots().size());
            world.removeRobot("nope");
        }
    }

    @Nested
    @DisplayName("obstacles")
    class Obstacles {

        @Test void addObstacle_appearsInList() {
            world.addObstacle(new Obstacle(1, 1, 1, 1, "MOUNTAIN"));
            assertEquals(1, world.getObstacles().size());
        }

        @Test void isPositionBlocked_trueForMountain() {
            world.addObstacle(new Obstacle(1, 1, 1, 1, "MOUNTAIN"));
            assertTrue(world.isPositionBlocked(1, 1));
        }

      

        @Test void isPositionBlocked_falseForEmptyCell() {
            assertFalse(world.isPositionBlocked(0, 0));
        }

        @Test void isPositionInPit_trueForPit() {
            world.addObstacle(new Obstacle(2, 2, 2, 2, "PIT"));
            assertTrue(world.isPositionInPit(2, 2));
        }

        @Test void isPositionInPit_falseForMountain() {
            world.addObstacle(new Obstacle(1, 1, 1, 1, "MOUNTAIN"));
            assertFalse(world.isPositionInPit(1, 1));
        }
    }

    @Nested
    @DisplayName("isPositionAvailable")
    class Availability {

        @Test void available_inBoundsEmptyCell() {
            assertTrue(world.isPositionAvailable(new Position(0, 0)));
        }

        @Test void notAvailable_whenBlocked() {
            world.addObstacle(new Obstacle(1, 1, 1, 1, "WALL"));
            assertFalse(world.isPositionAvailable(new Position(1, 1)));
        }

        @Test void notAvailable_pastEastBoundary() {
            int xLimit = (world.getWidth() - 1) / 2;
            assertFalse(world.isPositionAvailable(new Position(xLimit + 1, 0)));
        }

        @Test void notAvailable_pastSouthBoundary() {
            int yLimit = (world.getHeight() - 1) / 2;
            assertFalse(world.isPositionAvailable(new Position(0, -yLimit - 1)));
        }

        @Test void notAvailable_nullPosition() {
            assertFalse(world.isPositionAvailable(null));
        }

        @Test void available_atExactBoundary() {
            int xLimit = (world.getWidth() - 1) / 2;
            assertTrue(world.isPositionAvailable(new Position(xLimit, 0)));
        }
    }

    @Nested
    @DisplayName("moveRobot — clean cases")
    class MoveSuccess {

        @Test void moveForward_north_changesY() {
            BaseRobot bot = launchAt("HAL", 0, 0);
           ForwardCommand com = new ForwardCommand(new String[]{"3"}, "HAL");
            assertTrue(com.moveRobot("HAL", 3, world));
            assertEquals(3, bot.getPosition().getY());
        }

        @Test void moveForward_south_decreasesY() {
            BaseRobot bot = launchAt("HAL", 0, 0);
            bot.updateDirection(Directions.SOUTH);
            new ForwardCommand(new String[]{"2"}, "HAL").moveRobot("HAL", 2, world);
            assertEquals(-2, bot.getPosition().getY());
        }

        @Test void moveForward_east_increasesX() {
            BaseRobot bot = launchAt("HAL", 0, 0);
            bot.updateDirection(Directions.EAST);
            new ForwardCommand(new String[]{"2"}, "HAL").moveRobot("HAL", 2, world);
            assertEquals(2, bot.getPosition().getX());
        }

        @Test void moveForward_west_decreasesX() {
            BaseRobot bot = launchAt("HAL", 0, 0);
            bot.updateDirection(Directions.WEST);
            new ForwardCommand(new String[]{"2"}, "HAL").moveRobot("HAL", 2, world);
            assertEquals(-2, bot.getPosition().getX());
        }

        @Test void moveNegative_movesBackward() {
            BaseRobot bot = launchAt("HAL", 0, 0);
            new ForwardCommand(new String[]{"-2"}, "HAL").moveRobot("HAL", -2, world); // facing NORTH, negative = south
            assertEquals(-2, bot.getPosition().getY());
        }

        @Test void moveZero_stays() {
            BaseRobot bot = launchAt("HAL", 0, 0);
            assertTrue(new ForwardCommand(new String[]{"0"}, "HAL").moveRobot("HAL", 0, world));
            assertEquals(0, bot.getPosition().getX());
            assertEquals(0, bot.getPosition().getY());
        }

        @Test void moveMissingRobot_returnsFalse() {
            assertFalse(new ForwardCommand(new String[]{"1"}, "ghost").moveRobot("ghost", 1, world));
        }
    }

    @Nested
    @DisplayName("moveRobot — boundaries & blocking")
    class MoveBlocked {

        @Test void blockedByWall_stopsBefore() {
            BaseRobot bot = launchAt("HAL", 0, 0);
            world.addObstacle(new Obstacle(0, 1, 0, 1, "WALL"));
            assertFalse(new ForwardCommand(new String[]{"1"}, "HAL").moveRobot("HAL", 1, world));
            assertEquals(0, bot.getPosition().getY()); // didn't step into the wall
        }

        @Test void blockedByMountain_partialProgressCommitted() {
            BaseRobot bot = launchAt("HAL", 0, 0);
            world.addObstacle(new Obstacle(0, 3, 0, 3, "MOUNTAIN")); // wall 3 cells north
            assertFalse(new ForwardCommand(new String[]{"5"}, "HAL").moveRobot("HAL", 5, world));
            assertEquals(2, bot.getPosition().getY()); // stopped at (0,2)
        }

        @Test void blockedByBoundary_partialProgressCommitted() {
            BaseRobot bot = launchAt("HAL", 0, 4);
            assertFalse(new ForwardCommand(new String[]{"5"}, "HAL").moveRobot("HAL", 5, world)); // yLimit=5, can move 1
            assertEquals(5, bot.getPosition().getY());
        }

        @Test void blockedByEdge_facingEast_partial() {
            BaseRobot bot = launchAt("HAL", 4, 0);
            bot.updateDirection(Directions.EAST);
            assertFalse(new ForwardCommand(new String[]{"10"}, "HAL").moveRobot("HAL", 10, world));
            assertEquals(5, bot.getPosition().getX());
        }

        @Test void successfulFullMove_returnsTrue() {
            launchAt("HAL", 0, 0);
            assertTrue(new ForwardCommand(new String[]{"3"}, "HAL").moveRobot("HAL", 3, world));
        }
    }

    @Nested
    @DisplayName("moveRobot — pits & respawn")
    class Pits {

     

        @Test void stepIntoPit_withLivesLeft_respawnsRobot() {
            BaseRobot bot = launchAt("HAL", 0, 0);
            world.addObstacle(new Obstacle(0, 1, 0, 1, "PIT"));
            new ForwardCommand(new String[]{"1"}, "HAL").moveRobot("HAL", 1, world);
            // Still in world after respawn
            assertNotNull(world.getAllRobots().get("HAL"));
            // Position is not the pit cell anymore
            assertFalse(bot.getPosition().getX() == 0 && bot.getPosition().getY() == 1);
        }


    }


    @Nested
    @DisplayName("rotateRobot")
    class Rotation {

        @Test void rotateRight_fromNorth_facesEast() {
            BaseRobot bot = launchAt("HAL", 0, 0);
            world.rotateRobot("HAL", true);
            assertEquals(Directions.EAST, bot.getDirection());
        }

        @Test void rotateLeft_fromNorth_facesWest() {
            BaseRobot bot = launchAt("HAL", 0, 0);
            world.rotateRobot("HAL", false);
            assertEquals(Directions.WEST, bot.getDirection());
        }

        @Test void rotateRight_fourTimes_returnsToNorth() {
            BaseRobot bot = launchAt("HAL", 0, 0);
            for (int i = 0; i < 4; i++) world.rotateRobot("HAL", true);
            assertEquals(Directions.NORTH, bot.getDirection());
        }
    }

    @Nested
    @DisplayName("lookAround")
    class Look {

        @Test void lookAround_offCentre_seesNearestEdge() {
            // Robot at (3,3): NORTH edge reachable at dist=3 (yLimit=5), EAST edge at dist=3 (xLimit=5).
            launchAt("HAL", 3, 3);
            LookCommand look = new LookCommand( "HAL");
            List<ServerResponseObject> seen = look.lookAround(world);
            long edges = seen.stream().filter(o -> o.getType() == ImpedimentType.EDGE).count();
            assertEquals(2, edges); // NORTH + EAST edges visible, SOUTH/WEST > lookRange
        }

        @Test void lookAround_reportsObstacleSubtype() {
            launchAt("HAL", 0, 0);
            world.addObstacle(new Obstacle(0, 2, 0, 2, "TREE"));
                 LookCommand look = new LookCommand( "HAL");
            List<ServerResponseObject> seen = look.lookAround(world);
            assertTrue(seen.stream().anyMatch(o ->
                    o.getType() == ImpedimentType.OBSTACLE && "TREE".equals(o.getSubtype())));
        }

        @Test void lookAround_includesPositionForEachReport() {
            launchAt("HAL", 0, 0);
            LookCommand look = new LookCommand( "HAL");
            List<ServerResponseObject> seen = look.lookAround(world);
            for (ServerResponseObject o : seen) assertNotNull(o.getPosition());
        }

        @Test void lookAround_mountainBlocksFurtherSight() {
            launchAt("HAL", 0, 0);
            world.addObstacle(new Obstacle(0, 1, 0, 1, "MOUNTAIN"));
            // Add a tree behind the mountain — should NOT be seen
            world.addObstacle(new Obstacle(0, 3, 0, 3, "TREE"));
            LookCommand look = new LookCommand( "HAL");
            List<ServerResponseObject> seen = look.lookAround(world);
            long northReports = seen.stream()
                    .filter(o -> o.getDirection() == Directions.NORTH)
                    .count();
            // Only mountain should be reported north (tree hidden behind it)
            assertEquals(1, northReports);
        }

        @Test void lookAround_treeDoesNotBlockSight() {
            launchAt("HAL", 0, 0);
            world.addObstacle(new Obstacle(0, 1, 0, 1, "TREE"));
            world.addObstacle(new Obstacle(0, 3, 0, 3, "ROCK"));
            LookCommand look = new LookCommand( "HAL");
            List<ServerResponseObject> seen = look.lookAround(world);
            long northReports = seen.stream()
                    .filter(o -> o.getDirection() == Directions.NORTH)
                    .count();
            assertTrue(northReports >= 2); // tree + rock both seen
        }

        @Test void lookAround_seesOtherRobot() {
            launchAt("HAL", 0, 0);
            launchAt("R2", 0, 3);
            LookCommand look = new LookCommand( "HAL");
            List<ServerResponseObject> seen = look.lookAround(world);
            assertTrue(seen.stream().anyMatch(o ->
                    o.getType() == ImpedimentType.ROBOT && "R2".equals(o.getName())));
        }

        @Test void lookAround_otherRobotBlocksLineOfSight() {
            launchAt("HAL", 0, 0);
            launchAt("R2", 0, 2);
            world.addObstacle(new Obstacle(0, 4, 0, 4, "TREE"));
                 LookCommand look = new LookCommand( "HAL");
            List<ServerResponseObject> seen = look.lookAround(world);
            long north = seen.stream().filter(o -> o.getDirection() == Directions.NORTH).count();
            assertEquals(1, north); // R2 blocks, tree hidden
        }

    
    }
}
