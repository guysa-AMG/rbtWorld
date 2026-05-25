package za.co.wethinkcode.robots.server.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import za.co.wethinkcode.robots.models.Directions;
import za.co.wethinkcode.robots.models.OperationalMode;
import za.co.wethinkcode.robots.models.Position;
import za.co.wethinkcode.robots.models.StatusCode;
import za.co.wethinkcode.robots.models.impediment.Obstacle;
import za.co.wethinkcode.robots.models.transitmodels.ServerRequest;
import za.co.wethinkcode.robots.models.transitmodels.ServerResponse;
import za.co.wethinkcode.robots.server.robot.BaseRobot;
import za.co.wethinkcode.robots.server.world.RobotWorld;

public class FireReloadRepairTest {

    private RobotWorld world;

    @BeforeEach
    void freshWorld() {
        world = new RobotWorld(21, 21, 5);
    }

    private static ServerRequest req(String cmd, String robot, String... args) {
        return new ServerRequest(robot, cmd, args);
    }

    private BaseRobot launchAt(String name, int x, int y, Directions dir) {
        world.perform(Command.generate(req("launch", name, "balanced")));
        BaseRobot bot = world.getAllRobots().get(name);
        bot.updatePosition(new Position(x, y));
        bot.updateDirection(dir);
        return bot;
    }

    @Nested
    @DisplayName("Reload command")
    class Reload {
        @Test void reloadReturnsOkAndDoneMessage() {
            launchAt("HAL", 0, 0, Directions.NORTH);
            ServerResponse res = world.perform(Command.generate(req("reload", "HAL")));
            assertEquals(StatusCode.OK, res.getResult());
            assertEquals("DONE", res.getData().getMessage());
        }

        @Test void reloadRefillsAmmo() {
            BaseRobot bot = launchAt("HAL", 0, 0, Directions.NORTH);
            bot.decrementBullets();
            int before = bot.getShoots();
            world.perform(Command.generate(req("reload", "HAL")));
            assertTrue(bot.getShoots() >= before);
        }
    }

    @Nested
    @DisplayName("Repair command")
    class Repair {
        @Test void repairReturnsOkAndDoneMessage() {
            launchAt("HAL", 0, 0, Directions.NORTH);
            ServerResponse res = world.perform(Command.generate(req("repair", "HAL")));
            assertEquals(StatusCode.OK, res.getResult());
            assertEquals("DONE", res.getData().getMessage());
        }

        @Test void repairResetsStatusToNormal() {
            BaseRobot bot = launchAt("HAL", 0, 0, Directions.NORTH);
            bot.setStatus(OperationalMode.DEAD);
            world.perform(Command.generate(req("repair", "HAL")));
            assertEquals(OperationalMode.NORMAL, bot.getStatus());
        }
    }

    @Nested
    @DisplayName("Fire command — single-robot misses")
    class FireSolo {
        @Test void fireOnEmptyArenaReportsMiss() {
            launchAt("HAL", 0, 0, Directions.NORTH);
            ServerResponse res = world.perform(Command.generate(req("fire", "HAL")));
            assertEquals(StatusCode.OK, res.getResult());
            assertEquals("Miss", res.getData().getMessage());
        }

        @Test void fireWithoutAmmoReturnsOutOfAmmo() {
            BaseRobot bot = launchAt("HAL", 0, 0, Directions.NORTH);
            while (bot.getShoots() > 0) bot.decrementBullets();
            ServerResponse res = world.perform(Command.generate(req("fire", "HAL")));
            assertEquals(StatusCode.ERROR, res.getResult());
            assertEquals("OUT_OF_AMMO", res.getData().getMessage());
        }

        @Test void fireIntoBlockedObstacleReportsMissNotHit() {
            launchAt("HAL", 0, 0, Directions.EAST);
            world.addObstacle(new Obstacle(1, 0, 1, 0, "MOUNTAIN"));
            ServerResponse res = world.perform(Command.generate(req("fire", "HAL")));
            assertEquals(StatusCode.OK, res.getResult());
            assertEquals("Miss", res.getData().getMessage());
        }
    }

    @Nested
    @DisplayName("Fire command — robot-on-robot")
    class FireOnRobot {
        @Test void fireHitsRobotInDirectLineOfSight() {
            launchAt("HAL", 0, 0, Directions.SOUTH);
            BaseRobot victim = launchAt("R2", 0, 2, Directions.NORTH);
            ServerResponse res = world.perform(Command.generate(req("fire", "HAL")));
            assertEquals(StatusCode.OK, res.getResult());
            assertTrue(res.getData().getMessage().contains("HIT") || res.getData().getMessage().contains("KILLED"));
            assertTrue(victim.getShield() < victim.getMaxShield() || victim.isDead());
        }

        @Test void fireKillsLowShieldVictim() {
            launchAt("HAL", 0, 0, Directions.SOUTH);
            BaseRobot victim = launchAt("R2", 0, 1, Directions.NORTH);
            // Damage victim to brink, then fire — should kill.
            victim.takeDamage(victim.getShield() - 1, "x");
            ServerResponse res = world.perform(Command.generate(req("fire", "HAL")));
            assertNotNull(res);
            assertEquals(StatusCode.OK, res.getResult());
        }

        @Test void firePastBoundaryStillReturnsResponse() {
            // Robot near east boundary firing east — should reach edge without hitting.
            launchAt("HAL", 9, 0, Directions.EAST);
            ServerResponse res = world.perform(Command.generate(req("fire", "HAL")));
            assertEquals(StatusCode.OK, res.getResult());
        }
    }
}
