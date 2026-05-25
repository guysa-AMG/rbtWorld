package za.co.wethinkcode.robots.server.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import za.co.wethinkcode.robots.models.Position;
import za.co.wethinkcode.robots.models.impediment.Impediments;

public class WorldGeneratorTest {

    @Nested
    @DisplayName("WorldGenerator.build() — full procedural arena")
    class Build {
        @Test void buildReturnsNonNullWorld() {
            RobotWorld w = WorldGenerator.build();
            assertNotNull(w);
        }

        @Test void buildPlacesObstacles() {
            RobotWorld w = WorldGenerator.build();
            assertTrue(w.getObstacles().size() > 0);
        }

        @Test void buildPlacesAmmoPickups() {
            RobotWorld w = WorldGenerator.build();
            assertFalse(w.getAmmoPickups().isEmpty());
        }

        @Test void buildSetsExpectedDimensions() {
            RobotWorld w = WorldGenerator.build();
            assertEquals(51, w.getWidth());
            assertEquals(31, w.getHeight());
        }
    }

    @Nested
    @DisplayName("generateFromMapString")
    class FromString {
        @Test void simpleMapStringYieldsPopulatedMap() {
            String map = "t r r\n. . .\nm p w";
            RobotWorld w = WorldGenerator.generateFromMapString(map);
            assertNotNull(w);
            assertFalse(w.getMap().isEmpty());
        }

        @Test void boundaryAndEmptyTilesAreRecognised() {
            RobotWorld w = WorldGenerator.generateFromMapString(". | -\n. . .");
            assertNotNull(w.getMap());
        }
    }

    @Nested
    @DisplayName("generateFromMapfile")
    class FromFile {
        @Test void existingResourceReturnsWorld() {
            RobotWorld w = WorldGenerator.generateFromMapfile("worldMapTest.txt");
            assertNotNull(w);
            assertFalse(w.getMap().isEmpty());
        }

        @Test void missingResourceReturnsNull() {
            assertNull(WorldGenerator.generateFromMapfile("no-such-file.txt"));
        }
    }

    @Nested
    @DisplayName("RobotWorld extras: ammo pickup rules")
    class AmmoPickups {
        @Test void cannotAddPickupOnBlockedCell() {
            RobotWorld w = new RobotWorld(11, 11, 5);
            w.addObstacle(new za.co.wethinkcode.robots.models.impediment.Obstacle(1, 1, 1, 1, "MOUNTAIN"));
            assertFalse(w.addAmmoPickup(new Position(1, 1)));
        }

        @Test void cannotAddDuplicatePickup() {
            RobotWorld w = new RobotWorld(11, 11, 5);
            assertTrue(w.addAmmoPickup(new Position(2, 2)));
            assertFalse(w.addAmmoPickup(new Position(2, 2)));
        }

        @Test void nullPickupReturnsFalse() {
            RobotWorld w = new RobotWorld(11, 11, 5);
            assertFalse(w.addAmmoPickup(null));
        }

        @Test void cannotAddPickupOnPit() {
            RobotWorld w = new RobotWorld(11, 11, 5);
            w.addObstacle(new za.co.wethinkcode.robots.models.impediment.Obstacle(0, 0, 0, 0, "PIT"));
            assertFalse(w.addAmmoPickup(new Position(0, 0)));
        }
    }

    @Nested
    @DisplayName("perform() routes unlaunched commands to ErrorCommand")
    class PerformRouting {
        @Test void historyOfCommandsRecordsExecution() {
            RobotWorld w = new RobotWorld(11, 11, 5);
            w.perform(za.co.wethinkcode.robots.server.commands.Command.generate(
                    new za.co.wethinkcode.robots.models.transitmodels.ServerRequest("HAL", "launch", new String[]{"balanced"})));
            assertEquals(1, w.getHistoryOfCommands().size());
        }

        @Test void newSpawnPointReturnsInBoundsPosition() {
            RobotWorld w = new RobotWorld(7, 7, 3);
            Position p = w.newSpawnPoint();
            assertNotNull(p);
            assertTrue(Math.abs(p.getX()) <= 3);
            assertTrue(Math.abs(p.getY()) <= 3);
        }
    }
}
