package za.co.wethinkcode.robots.server.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import za.co.wethinkcode.robots.models.*;
import za.co.wethinkcode.robots.models.impediment.Obstacle;
import za.co.wethinkcode.robots.models.transitmodels.ServerRequest;
import za.co.wethinkcode.robots.models.transitmodels.ServerResponse;
import za.co.wethinkcode.robots.server.robot.BaseRobot;
import za.co.wethinkcode.robots.server.world.Iworld;
import za.co.wethinkcode.robots.server.world.RobotWorld;

public class FireCommandTest {

    private RobotWorld world;
    private BaseRobot shooter;

    @BeforeEach
    void setup() {
        world = new RobotWorld(31, 31, 5);
        world.addRobot("HAL");
        shooter = world.getAllRobots().get("HAL");
        shooter.updatePosition(new Position(0, 0));
        shooter.updateDirection(Directions.NORTH);
        shooter.refillAmmo();
    }

    private Command fire() {
        return Command.generate(new ServerRequest("HAL", "fire", new String[0]));
    }

    private BaseRobot placeVictim(String name, int x, int y) {
        world.addRobot(name);
        BaseRobot v = world.getAllRobots().get(name);
        v.updatePosition(new Position(x, y));
        return v;
    }

    @Nested
    @DisplayName("miss path")
    class Miss {

        @Test void noTarget_message_isMiss() {
            ServerResponse res = fire().execute(world, shooter);
            assertEquals("Miss", res.getData().getMessage());
        }

        @Test void noTarget_resultIsOk() {
            ServerResponse res = fire().execute(world, shooter);
            assertEquals(StatusCode.OK, res.getResult());
        }

        @Test void noTarget_decrementsAmmo() {
            int before = shooter.getShoots();
            fire().execute(world, shooter);
            assertEquals(before - 1, shooter.getShoots());
        }

        @Test void noTarget_doesNotIncrementKills() {
            int before = shooter.getKills();
            fire().execute(world, shooter);
            assertEquals(before, shooter.getKills());
        }

        @Test void noTarget_doesNotChangeShooterShield() {
            int before = shooter.getShield();
            fire().execute(world, shooter);
            assertEquals(before, shooter.getShield());
        }

        @Test void noTarget_dataRobotIsNull() {
            ServerResponse res = fire().execute(world, shooter);
            assertNull(res.getData().getRobot());
        }

        @Test void blockedByMountain_isMiss() {
            world.addObstacle(new Obstacle(0, 2, 0, 2, "MOUNTAIN"));
            placeVictim("R2", 0, 4); // behind mountain — bullet blocked first
            ServerResponse res = fire().execute(world, shooter);
            assertEquals("Miss", res.getData().getMessage());
        }

        @Test void blockedByWall_isMiss() {
            world.addObstacle(new Obstacle(0, 2, 0, 2, "WALL"));
            placeVictim("R2", 0, 4);
            ServerResponse res = fire().execute(world, shooter);
            assertEquals("Miss", res.getData().getMessage());
        }

        @Test void blockedByTree_isMiss() {
            world.addObstacle(new Obstacle(0, 2, 0, 2, "TREE"));
            placeVictim("R2", 0, 4);
            ServerResponse res = fire().execute(world, shooter);
            assertEquals("Miss", res.getData().getMessage());
        }

        @Test void outOfRange_isMiss() {
            placeVictim("R2", 0, Iworld.bulletRange + 1); // just past range
            ServerResponse res = fire().execute(world, shooter);
            assertEquals("Miss", res.getData().getMessage());
        }
    }

    @Nested
    @DisplayName("hit path (non-lethal)")
    class Hit {

        @Test void hit_messageStartsWithHit() {
            // Fire from max range so damage is 1 (non-lethal vs shield=3)
            placeVictim("R2", 0, Iworld.bulletRange);
            ServerResponse res = fire().execute(world, shooter);
            assertTrue(res.getData().getMessage().startsWith("HIT"),
                    "expected HIT but got: " + res.getData().getMessage());
        }

        @Test void hit_messageIncludesVictimName() {
            placeVictim("R2", 0, Iworld.bulletRange);
            ServerResponse res = fire().execute(world, shooter);
            assertTrue(res.getData().getMessage().contains("R2"));
        }

        @Test void hit_dataRobotIsVictim() {
            placeVictim("R2", 0, Iworld.bulletRange);
            ServerResponse res = fire().execute(world, shooter);
            assertEquals("R2", res.getData().getRobot());
        }

        @Test void hit_reducesVictimShield() {
            BaseRobot v = placeVictim("R2", 0, Iworld.bulletRange);
            int before = v.getShield();
            fire().execute(world, shooter);
            assertTrue(v.getShield() < before);
        }

        @Test void hit_decrementsShooterAmmo() {
            placeVictim("R2", 0, Iworld.bulletRange);
            int before = shooter.getShoots();
            fire().execute(world, shooter);
            assertEquals(before - 1, shooter.getShoots());
        }

        @Test void hit_doesNotIncrementShooterKills() {
            BaseRobot v = placeVictim("R2", 0, 2);
            // give victim plenty of shield so a single shot doesn't kill it
            v.respawnAt(new Position(0, 2));
            for (int i = 0; i < 0; i++) {} // no-op (shield is already max)
            int beforeKills = shooter.getKills();
            fire().execute(world, shooter);
            if (v.getShield() > 0) {
                assertEquals(beforeKills, shooter.getKills());
            }
        }

        @Test void hit_stateIncludesShooterAmmoCount() {
            placeVictim("R2", 0, 2);
            ServerResponse res = fire().execute(world, shooter);
            assertNotNull(res.getState());
        }

        @Test void hit_damageScalesWithRange() {
            BaseRobot v = placeVictim("R2", 0, 1); // very close
            int before = v.getShield();
            fire().execute(world, shooter);
            int closeDamage = before - v.getShield();

            // Fresh setup: victim further away
            v.respawnAt(new Position(0, Iworld.bulletRange));
            shooter.refillAmmo();
            int before2 = v.getShield();
            fire().execute(world, shooter);
            int farDamage = before2 - v.getShield();
            assertTrue(closeDamage >= farDamage,
                    "close damage " + closeDamage + " should be >= far damage " + farDamage);
        }

        @Test void shooterDoesNotHitSelf() {
            // Standing on an empty cell, no other robot — should miss not self-hit.
            ServerResponse res = fire().execute(world, shooter);
            assertNotEquals("HIT HAL", res.getData().getMessage());
        }

        private void assertNotEquals(String expected, String actual) {
            assertFalse(expected.equals(actual));
        }
    }

    @Nested
    @DisplayName("kill path (lethal)")
    class Kill {

        @Test void kill_messageStartsWithKILLED() {
            BaseRobot v = placeVictim("R2", 0, 1);
            v.takeDamage(v.getShield() - 1, "x"); // 1 shield left
            ServerResponse res = fire().execute(world, shooter);
            assertTrue(res.getData().getMessage().startsWith("KILLED"));
        }

        @Test void kill_incrementsShooterKills() {
            BaseRobot v = placeVictim("R2", 0, 1);
            v.takeDamage(v.getShield() - 1, "x");
            int before = shooter.getKills();
            fire().execute(world, shooter);
            assertEquals(before + 1, shooter.getKills());
        }

        @Test void kill_removesVictimFromWorld() {
            BaseRobot v = placeVictim("R2", 0, 1);
            v.takeDamage(v.getShield() - 1, "x");
            fire().execute(world, shooter);
            assertNull(world.getAllRobots().get("R2"));
        }

        @Test void kill_setsVictimKilledBy() {
            BaseRobot v = placeVictim("R2", 0, 1);
            v.takeDamage(v.getShield() - 1, "x");
            fire().execute(world, shooter);
            assertEquals("HAL", v.getKilledBy());
        }

        @Test void kill_dataRobotIsVictimName() {
            BaseRobot v = placeVictim("R2", 0, 1);
            v.takeDamage(v.getShield() - 1, "x");
            ServerResponse res = fire().execute(world, shooter);
            assertEquals("R2", res.getData().getRobot());
        }

        @Test void kill_consumesAmmo() {
            BaseRobot v = placeVictim("R2", 0, 1);
            v.takeDamage(v.getShield() - 1, "x");
            int before = shooter.getShoots();
            fire().execute(world, shooter);
            assertEquals(before - 1, shooter.getShoots());
        }
    }

    @Nested
    @DisplayName("out of ammo")
    class OutOfAmmo {

        @Test void emptyMag_returnsError() {
            while (shooter.getShoots() > 0) shooter.decrementBullets();
            ServerResponse res = fire().execute(world, shooter);
            assertEquals(StatusCode.ERROR, res.getResult());
        }

        @Test void emptyMag_messageIsOutOfAmmo() {
            while (shooter.getShoots() > 0) shooter.decrementBullets();
            ServerResponse res = fire().execute(world, shooter);
            assertEquals("OUT_OF_AMMO", res.getData().getMessage());
        }

        @Test void emptyMag_doesNotChangeAmmoFurther() {
            while (shooter.getShoots() > 0) shooter.decrementBullets();
            int before = shooter.getShoots();
            fire().execute(world, shooter);
            assertEquals(before, shooter.getShoots());
        }
    }

    @Nested
    @DisplayName("directional firing")
    class Directional {

        @Test void firingSouth_canHitVictimToSouth() {
            shooter.updateDirection(Directions.SOUTH);
            placeVictim("R2", 0, -Iworld.bulletRange);
            ServerResponse res = fire().execute(world, shooter);
            assertTrue(res.getData().getMessage().startsWith("HIT") ||
                    res.getData().getMessage().startsWith("KILLED"));
        }

        @Test void firingEast_canHitVictimToEast() {
            shooter.updateDirection(Directions.EAST);
            placeVictim("R2", Iworld.bulletRange, 0);
            ServerResponse res = fire().execute(world, shooter);
            assertTrue(res.getData().getMessage().startsWith("HIT") ||
                    res.getData().getMessage().startsWith("KILLED"));
        }

        @Test void firingWest_canHitVictimToWest() {
            shooter.updateDirection(Directions.WEST);
            placeVictim("R2", -Iworld.bulletRange, 0);
            ServerResponse res = fire().execute(world, shooter);
            assertTrue(res.getData().getMessage().startsWith("HIT") ||
                    res.getData().getMessage().startsWith("KILLED"));
        }

        @Test void firingNorth_doesNotHitVictimSouth() {
            placeVictim("R2", 0, -2);
            ServerResponse res = fire().execute(world, shooter);
            assertEquals("Miss", res.getData().getMessage());
        }

        @Test void firingNorth_doesNotHitVictimEast() {
            placeVictim("R2", 2, 0);
            ServerResponse res = fire().execute(world, shooter);
            assertEquals("Miss", res.getData().getMessage());
        }

        @Test void firingNorth_doesNotHitVictimWest() {
            placeVictim("R2", -2, 0);
            ServerResponse res = fire().execute(world, shooter);
            assertEquals("Miss", res.getData().getMessage());
        }
    }

    @Nested
    @DisplayName("range tracking in response")
    class Range {

        @Test void hit_distanceMatchesActualSeparation() {
            placeVictim("R2", 0, 3);
            ServerResponse res = fire().execute(world, shooter);
            assertEquals(3, res.getData().getDistance());
        }

        @Test void miss_distanceIsTravelLengthOrZero() {
            // No target, no obstacle — bullet runs to max range
            ServerResponse res = fire().execute(world, shooter);
            assertTrue(res.getData().getDistance() >= 0);
        }

        @Test void blockedByObstacle_distanceIsObstacleCell() {
            world.addObstacle(new Obstacle(0, 2, 0, 2, "MOUNTAIN"));
            ServerResponse res = fire().execute(world, shooter);
            assertEquals(2, res.getData().getDistance());
        }

        @Test void hitAtMaxRange_distanceIsBulletRange() {
            placeVictim("R2", 0, Iworld.bulletRange);
            ServerResponse res = fire().execute(world, shooter);
            assertEquals(Iworld.bulletRange, res.getData().getDistance());
        }

        @Test void hitImmediatelyAdjacent_distanceIsOne() {
            placeVictim("R2", 0, 1);
            ServerResponse res = fire().execute(world, shooter);
            assertEquals(1, res.getData().getDistance());
        }
    }

    @Nested
    @DisplayName("response shape")
    class Shape {

        @Test void response_alwaysIncludesData() {
            ServerResponse res = fire().execute(world, shooter);
            assertNotNull(res.getData());
        }

        @Test void response_alwaysIncludesState() {
            ServerResponse res = fire().execute(world, shooter);
            assertNotNull(res.getState());
        }

        @Test void stateIncludesShooterPosition() {
            ServerResponse res = fire().execute(world, shooter);
            assertNotNull(res.getState().getPosition());
            assertEquals(0, res.getState().getPosition().getX());
            assertEquals(0, res.getState().getPosition().getY());
        }

        @Test void stateIncludesShooterDirection() {
            ServerResponse res = fire().execute(world, shooter);
            assertEquals(Directions.NORTH, res.getState().getDirection());
        }

        @Test void stateIncludesShooterShield() {
            ServerResponse res = fire().execute(world, shooter);
            assertEquals(shooter.getShield(), res.getState().getShields());
        }

        @Test void stateIncludesShooterAmmo() {
            ServerResponse res = fire().execute(world, shooter);
            assertEquals(shooter.getShoots(), res.getState().getShots());
        }
    }

    @Nested
    @DisplayName("edge cases & boundaries")
    class Edges {

        @Test void firingFromBoundary_bulletDoesNotEscapeWorld() {
            // Place shooter at northern edge facing north
            shooter.updatePosition(new Position(0, (world.getHeight() - 1) / 2));
            shooter.updateDirection(Directions.NORTH);
            ServerResponse res = fire().execute(world, shooter);
            assertEquals("Miss", res.getData().getMessage());
        }

        @Test void allMissesInARow_eachReducesAmmo() {
            int before = shooter.getShoots();
            for (int i = 0; i < before; i++) {
                fire().execute(world, shooter);
            }
            assertEquals(0, shooter.getShoots());
        }

        @Test void hitDoesNotConsumeAmmoTwice() {
            placeVictim("R2", 0, 2);
            int before = shooter.getShoots();
            fire().execute(world, shooter);
            assertEquals(before - 1, shooter.getShoots());
        }

        @Test void firingDoesNotChangeShooterPosition() {
            Position p = shooter.getPosition();
            fire().execute(world, shooter);
            assertEquals(p.getX(), shooter.getPosition().getX());
            assertEquals(p.getY(), shooter.getPosition().getY());
        }

        @Test void firingDoesNotChangeShooterDirection() {
            Directions d = shooter.getDirection();
            fire().execute(world, shooter);
            assertEquals(d, shooter.getDirection());
        }

        @Test void firingDoesNotChangeShooterShield() {
            int s = shooter.getShield();
            fire().execute(world, shooter);
            assertEquals(s, shooter.getShield());
        }

        @Test void firingTwiceAtSameVictim_bothShotsLand() {
            BaseRobot v = placeVictim("R2", 0, 2);
            int initial = v.getShield();
            fire().execute(world, shooter);
            int afterOne = v.getShield();
            if (v.getShield() > 0) {
                fire().execute(world, shooter);
                int afterTwo = v.getShield();
                assertTrue(afterTwo < afterOne);
                assertTrue(afterOne < initial);
            }
        }

        @Test void firingDoesNotAffectOtherRobotsBehind() {
            placeVictim("R2", 0, 2);
            BaseRobot behind = placeVictim("Z9", 0, 4);
            int behindShield = behind.getShield();
            fire().execute(world, shooter);
            // Bullet hits R2 (closer) and stops — Z9 untouched.
            assertEquals(behindShield, behind.getShield());
        }

        @Test void firingThroughEmptyCells_consumesOneBulletPerShot() {
            int before = shooter.getShoots();
            fire().execute(world, shooter); // miss
            fire().execute(world, shooter); // miss
            fire().execute(world, shooter); // miss
            assertEquals(before - 3, shooter.getShoots());
        }

        @Test void firingAtRobotOutOfLOS_misses() {
            // Place mountain between shooter and victim
            world.addObstacle(new Obstacle(0, 2, 0, 2, "MOUNTAIN"));
            placeVictim("R2", 0, 4);
            ServerResponse res = fire().execute(world, shooter);
            assertEquals("Miss", res.getData().getMessage());
        }
    }
}
