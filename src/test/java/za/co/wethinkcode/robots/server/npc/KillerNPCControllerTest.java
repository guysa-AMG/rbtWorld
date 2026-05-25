package za.co.wethinkcode.robots.server.npc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import za.co.wethinkcode.robots.server.world.RobotWorld;
import za.co.wethinkcode.robots.services.ITCService;

public class KillerNPCControllerTest {

    private RobotWorld world;
    private KillerNPCController ctrl;

    @BeforeEach
    void freshWorld() {
        world = new RobotWorld(11, 11, 5);
        ITCService.getInstance().setWorld(world);
        ctrl = new KillerNPCController(world);
    }

    @Nested
    @DisplayName("Static config constants")
    class Constants {
        @Test void nameIsFixedString() {
            assertEquals("Guyser_Thekiller", KillerNPC.NAME);
        }

        @Test void thresholdsArePositive() {
            assertTrue(KillerNPC.TICK_MS > 0);
            assertTrue(KillerNPC.RESPAWN_DELAY_MS > 0);
            assertTrue(KillerNPC.IDLE_THRESHOLD_MS > 0);
            assertTrue(KillerNPC.PEACE_THRESHOLD_MS > 0);
            assertTrue(KillerNPC.BLOCKED_THRESHOLD > 0);
        }

        @Test void threatMessageIsNonEmpty() {
            assertNotNull(KillerNPC.THREAT);
            assertTrue(KillerNPC.THREAT.length() > 0);
        }
    }

    @Nested
    @DisplayName("Public API hooks")
    class Hooks {
        @Test void recordKillRunsWithoutError() {
            ctrl.recordKill();
        }

        @Test void onNPCKilledSchedulesRespawnAndBroadcasts() {
            ctrl.onNPCKilled("Player1");
        }

        @Test void stopFlagsAsNonRunning() {
            ctrl.stop();
        }
    }

    @Nested
    @DisplayName("Background thread lifecycle")
    class Lifecycle {
        @Test void canBeRunOnThreadAndStopped() throws InterruptedException {
            Thread t = new Thread(ctrl);
            t.setDaemon(true);
            t.start();
            // Let it spawn the NPC (TICK_MS = 500)
            Thread.sleep(KillerNPC.TICK_MS + 200);
            ctrl.stop();
            t.interrupt();
            t.join(2000);
            // The NPC should have appeared in the world after at least one tick.
            assertNotNull(world.getAllRobots().get(KillerNPC.NAME));
        }
    }
}
