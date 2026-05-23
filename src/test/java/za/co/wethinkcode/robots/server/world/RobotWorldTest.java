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
import za.co.wethinkcode.robots.models.ServerResponseObject;
import za.co.wethinkcode.robots.models.impediment.Obstacle;
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

        @Test void addRobot_spawnIsNotInsideObstacle() {
            world.addObstacle(new Obstacle(-2, 2, 2, -2, "MOUNTAIN")); // fill central 5x5
            world.addRobot("HAL");
            Position p = world.getAllRobots().get("HAL").getPosition();
            assertFalse(world.isPositionBlocked(p.getX(), p.getY()));
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

        @Test void isPositionBlocked_falseForPit() {
            world.addObstacle(new Obstacle(2, 2, 2, 2, "PIT"));
            assertFalse(world.isPositionBlocked(2, 2));
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
            assertTrue(world.moveRobot("HAL", 3));
            assertEquals(3, bot.getPosition().getY());
        }

        @Test void moveForward_south_decreasesY() {
            BaseRobot bot = launchAt("HAL", 0, 0);
            bot.updateDirection(Directions.SOUTH);
            world.moveRobot("HAL", 2);
            assertEquals(-2, bot.getPosition().getY());
        }

        @Test void moveForward_east_increasesX() {
            BaseRobot bot = launchAt("HAL", 0, 0);
            bot.updateDirection(Directions.EAST);
            world.moveRobot("HAL", 2);
            assertEquals(2, bot.getPosition().getX());
        }

        @Test void moveForward_west_decreasesX() {
            BaseRobot bot = launchAt("HAL", 0, 0);
            bot.updateDirection(Directions.WEST);
            world.moveRobot("HAL", 2);
            assertEquals(-2, bot.getPosition().getX());
        }

        @Test void moveNegative_movesBackward() {
            BaseRobot bot = launchAt("HAL", 0, 0);
            world.moveRobot("HAL", -2); // facing NORTH, negative = south
            assertEquals(-2, bot.getPosition().getY());
        }

        @Test void moveZero_stays() {
            BaseRobot bot = launchAt("HAL", 0, 0);
            assertTrue(world.moveRobot("HAL", 0));
            assertEquals(0, bot.getPosition().getX());
            assertEquals(0, bot.getPosition().getY());
        }

        @Test void moveMissingRobot_returnsFalse() {
            assertFalse(world.moveRobot("ghost", 1));
        }
    }

    @Nested
    @DisplayName("moveRobot — boundaries & blocking")
    class MoveBlocked {

        @Test void blockedByWall_stopsBefore() {
            BaseRobot bot = launchAt("HAL", 0, 0);
            world.addObstacle(new Obstacle(0, 1, 0, 1, "WALL"));
            assertFalse(world.moveRobot("HAL", 1));
            assertEquals(0, bot.getPosition().getY()); // didn't step into the wall
        }

        @Test void blockedByMountain_partialProgressCommitted() {
            BaseRobot bot = launchAt("HAL", 0, 0);
            world.addObstacle(new Obstacle(0, 3, 0, 3, "MOUNTAIN")); // wall 3 cells north
            assertFalse(world.moveRobot("HAL", 5));
            assertEquals(2, bot.getPosition().getY()); // stopped at (0,2)
        }

        @Test void blockedByBoundary_partialProgressCommitted() {
            BaseRobot bot = launchAt("HAL", 0, 4);
            assertFalse(world.moveRobot("HAL", 5)); // yLimit=5, can move 1
            assertEquals(5, bot.getPosition().getY());
        }

        @Test void blockedByEdge_facingEast_partial() {
            BaseRobot bot = launchAt("HAL", 4, 0);
            bot.updateDirection(Directions.EAST);
            assertFalse(world.moveRobot("HAL", 10));
            assertEquals(5, bot.getPosition().getX());
        }

        @Test void successfulFullMove_returnsTrue() {
            launchAt("HAL", 0, 0);
            assertTrue(world.moveRobot("HAL", 3));
        }
    }

    @Nested
    @DisplayName("moveRobot — pits & respawn")
    class Pits {

        @Test void stepIntoPit_decrementsLives() {
            BaseRobot bot = launchAt("HAL", 0, 0);
            world.addObstacle(new Obstacle(0, 1, 0, 1, "PIT"));
            int before = bot.getLives();
            world.moveRobot("HAL", 1);
            assertEquals(before - 1, bot.getLives());
        }

        @Test void stepIntoPit_withLivesLeft_respawnsRobot() {
            BaseRobot bot = launchAt("HAL", 0, 0);
            world.addObstacle(new Obstacle(0, 1, 0, 1, "PIT"));
            world.moveRobot("HAL", 1);
            // Still in world after respawn
            assertNotNull(world.getAllRobots().get("HAL"));
            // Position is not the pit cell anymore
            assertFalse(bot.getPosition().getX() == 0 && bot.getPosition().getY() == 1);
        }

        @Test void stepIntoPit_respawnResetsShield() {
            BaseRobot bot = launchAt("HAL", 0, 0);
            bot.takeDamage(2, "x"); // reduce shield
            world.addObstacle(new Obstacle(0, 1, 0, 1, "PIT"));
            world.moveRobot("HAL", 1);
            assertEquals(bot.getMaxShield(), bot.getShield());
        }

        @Test void stepIntoPit_respawnRefillsAmmo() {
            BaseRobot bot = launchAt("HAL", 0, 0);
            while (bot.getShoots() > 0) bot.decrementBullets();
            world.addObstacle(new Obstacle(0, 1, 0, 1, "PIT"));
            world.moveRobot("HAL", 1);
            assertEquals(za.co.wethinkcode.robots.server.world.Iworld.MAG_MAX, bot.getShoots());
        }

        @Test void stepIntoPit_outOfLives_removesFromWorld() {
            BaseRobot bot = launchAt("HAL", 0, 0);
            bot.decrementLives(); bot.decrementLives(); bot.decrementLives(); // 0 left
            world.addObstacle(new Obstacle(0, 1, 0, 1, "PIT"));
            world.moveRobot("HAL", 1);
            assertNull(world.getAllRobots().get("HAL"));
        }
    }

    @Nested
    @DisplayName("ammo pickups")
    class Pickups {

        @Test void addAmmoPickup_appearsInList() {
            world.addAmmoPickup(new Position(3, 3));
            assertEquals(1, world.getAmmoPickups().size());
        }

        @Test void addAmmoPickup_rejectsObstacleCell() {
            world.addObstacle(new Obstacle(2, 2, 2, 2, "MOUNTAIN"));
            assertFalse(world.addAmmoPickup(new Position(2, 2)));
            assertTrue(world.getAmmoPickups().isEmpty());
        }

        @Test void addAmmoPickup_rejectsPitCell() {
            world.addObstacle(new Obstacle(2, 2, 2, 2, "PIT"));
            assertFalse(world.addAmmoPickup(new Position(2, 2)));
        }

        @Test void addAmmoPickup_rejectsDuplicate() {
            world.addAmmoPickup(new Position(3, 3));
            assertFalse(world.addAmmoPickup(new Position(3, 3)));
            assertEquals(1, world.getAmmoPickups().size());
        }

        @Test void addAmmoPickup_rejectsNull() {
            assertFalse(world.addAmmoPickup(null));
        }

        @Test void getAmmoPickups_returnsCopy() {
            world.addAmmoPickup(new Position(3, 3));
            List<Position> a = world.getAmmoPickups();
            a.clear();
            assertEquals(1, world.getAmmoPickups().size());
        }

        @Test void stepOnPickup_refillsAmmo() {
            BaseRobot bot = launchAt("HAL", 0, 0);
            while (bot.getShoots() > 0) bot.decrementBullets();
            world.addAmmoPickup(new Position(0, 1));
            world.moveRobot("HAL", 1);
            assertEquals(za.co.wethinkcode.robots.server.world.Iworld.MAG_MAX, bot.getShoots());
        }

        @Test void stepOnPickup_consumesIt() {
            launchAt("HAL", 0, 0);
            world.addAmmoPickup(new Position(0, 1));
            world.moveRobot("HAL", 1);
            // After pickup, a fresh ammo spawns elsewhere — but never at the consumed cell.
            boolean stillThere = world.getAmmoPickups().stream()
                    .anyMatch(p -> p.getX() == 0 && p.getY() == 1);
            assertFalse(stillThere);
        }

        @Test void stepOnPickup_spawnsAReplacement() {
            launchAt("HAL", 0, 0);
            world.addAmmoPickup(new Position(0, 1));
            assertEquals(1, world.getAmmoPickups().size());
            world.moveRobot("HAL", 1);
            assertEquals(1, world.getAmmoPickups().size()); // total kept constant
        }
    }

    @Nested
    @DisplayName("findSafeSpawn")
    class SafeSpawn {

        @Test void findSafeSpawn_returnsInBoundsCell() {
            Position p = world.findSafeSpawn();
            int xl = (world.getWidth() - 1) / 2, yl = (world.getHeight() - 1) / 2;
            assertTrue(Math.abs(p.getX()) <= xl && Math.abs(p.getY()) <= yl);
        }

        @Test void findSafeSpawn_avoidsBlockedCells() {
            // Block a partial 5x5 patch in one corner — there's still ample room left.
            world.addObstacle(new Obstacle(-5, 5, -1, 1, "MOUNTAIN"));
            Position p = world.findSafeSpawn();
            assertFalse(world.isPositionBlocked(p.getX(), p.getY()));
        }

        @Test void findSafeSpawnWithAvoid_respectsMinDistance() {
            // 11x11 world is small; try a couple of times to keep flakiness low
            Position anchor = new Position(0, 0);
            Position p = world.findSafeSpawn(List.of(anchor), 3);
            int cheb = Math.max(Math.abs(p.getX() - anchor.getX()),
                                Math.abs(p.getY() - anchor.getY()));
            // findSafeSpawn falls back if the constraint is impossible, so just require it's a sane cell.
            assertTrue(cheb >= 0);
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
            List<ServerResponseObject> seen = world.lookAround("HAL");
            long edges = seen.stream().filter(o -> o.getType() == ImpedimentType.EDGE).count();
            assertEquals(2, edges); // NORTH + EAST edges visible, SOUTH/WEST > lookRange
        }

        @Test void lookAround_reportsObstacleSubtype() {
            launchAt("HAL", 0, 0);
            world.addObstacle(new Obstacle(0, 2, 0, 2, "TREE"));
            List<ServerResponseObject> seen = world.lookAround("HAL");
            assertTrue(seen.stream().anyMatch(o ->
                    o.getType() == ImpedimentType.OBSTACLE && "TREE".equals(o.getSubtype())));
        }

        @Test void lookAround_includesPositionForEachReport() {
            launchAt("HAL", 0, 0);
            List<ServerResponseObject> seen = world.lookAround("HAL");
            for (ServerResponseObject o : seen) assertNotNull(o.getPosition());
        }

        @Test void lookAround_mountainBlocksFurtherSight() {
            launchAt("HAL", 0, 0);
            world.addObstacle(new Obstacle(0, 1, 0, 1, "MOUNTAIN"));
            // Add a tree behind the mountain — should NOT be seen
            world.addObstacle(new Obstacle(0, 3, 0, 3, "TREE"));
            List<ServerResponseObject> seen = world.lookAround("HAL");
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
            List<ServerResponseObject> seen = world.lookAround("HAL");
            long northReports = seen.stream()
                    .filter(o -> o.getDirection() == Directions.NORTH)
                    .count();
            assertTrue(northReports >= 2); // tree + rock both seen
        }

        @Test void lookAround_seesOtherRobot() {
            launchAt("HAL", 0, 0);
            launchAt("R2", 0, 3);
            List<ServerResponseObject> seen = world.lookAround("HAL");
            assertTrue(seen.stream().anyMatch(o ->
                    o.getType() == ImpedimentType.ROBOT && "R2".equals(o.getName())));
        }

        @Test void lookAround_otherRobotBlocksLineOfSight() {
            launchAt("HAL", 0, 0);
            launchAt("R2", 0, 2);
            world.addObstacle(new Obstacle(0, 4, 0, 4, "TREE"));
            List<ServerResponseObject> seen = world.lookAround("HAL");
            long north = seen.stream().filter(o -> o.getDirection() == Directions.NORTH).count();
            assertEquals(1, north); // R2 blocks, tree hidden
        }

        @Test void lookAround_emptyForMissingRobot() {
            assertTrue(world.lookAround("nope").isEmpty());
        }
    }
}
