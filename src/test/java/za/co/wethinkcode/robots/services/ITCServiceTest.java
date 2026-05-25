package za.co.wethinkcode.robots.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import za.co.wethinkcode.robots.models.StatusCode;
import za.co.wethinkcode.robots.models.transitmodels.ServerResponse;
import za.co.wethinkcode.robots.models.transitmodels.ServerResponseData;
import za.co.wethinkcode.robots.server.world.RobotWorld;

public class ITCServiceTest {

    @BeforeEach
    void prepareWorld() {
        ITCService.getInstance().setWorld(new RobotWorld(11, 11, 5));
    }

    @Nested
    @DisplayName("Singleton wiring")
    class Singleton {
        @Test void getInstanceAlwaysReturnsSameInstance() {
            assertSame(ITCService.getInstance(), ITCService.getInstance());
        }

        @Test void setWorldPersistsAcrossCalls() {
            RobotWorld w = new RobotWorld(5, 5, 3);
            ITCService.getInstance().setWorld(w);
            assertSame(w, ITCService.getInstance().getWorld());
        }
    }

    @Nested
    @DisplayName("Client registration & subscribers")
    class ClientReg {
        @Test void registerThenUnregisterIsSafe() {
            StringWriter sw = new StringWriter();
            ITCService.getInstance().registerClient("Bob", new PrintWriter(sw));
            ITCService.getInstance().unregisterClient("Bob");
        }

        @Test void registerNullNameIsNoop() {
            ITCService.getInstance().registerClient(null, new PrintWriter(new StringWriter()));
        }

        @Test void registerNullWriterIsNoop() {
            ITCService.getInstance().registerClient("X", null);
        }

        @Test void subscribeThenUnsubscribeIsNoop() {
            ITCService.getInstance().subscribe("S1");
            ITCService.getInstance().unsubscribe("S1");
        }

        @Test void subscribeNullIsSafe() {
            ITCService.getInstance().subscribe(null);
            ITCService.getInstance().unsubscribe(null);
        }
    }

    @Nested
    @DisplayName("doThisCommand routing")
    class Routing {
        @Test void shutdownReturnsOffSentinel() {
            String res = ITCService.getInstance().doThisCommand(
                    "{\"robot\":\"X\",\"command\":\"shutdown\",\"arguments\":[]}");
            assertEquals("off", res);
        }

        @Test void quitReturnsOffSentinel() {
            String res = ITCService.getInstance().doThisCommand(
                    "{\"robot\":\"X\",\"command\":\"quit\",\"arguments\":[]}");
            assertEquals("off", res);
        }

        @Test void launchCommandRoutesThroughWorld() {
            String res = ITCService.getInstance().doThisCommand(
                    "{\"robot\":\"HAL\",\"command\":\"launch\",\"arguments\":[\"balanced\"]}");
            assertNotNull(res);
            assertTrue(res.contains("\"result\":\"OK\""));
        }

        @Test void preLaunchCommandReturnsErrorResponse() {
            String res = ITCService.getInstance().doThisCommand(
                    "{\"robot\":\"ghost\",\"command\":\"look\",\"arguments\":[]}");
            assertTrue(res.contains("\"result\":\"ERROR\""));
            assertTrue(res.contains("not been launched"));
        }

        @Test void subscribeReturnsOkResponse() {
            String res = ITCService.getInstance().doThisCommand(
                    "{\"robot\":\"sub\",\"command\":\"subscribe\",\"arguments\":[]}");
            assertTrue(res.contains("Subscribed"));
        }

        @Test void unsubscribeReturnsOkResponse() {
            String res = ITCService.getInstance().doThisCommand(
                    "{\"robot\":\"sub\",\"command\":\"unsubscribe\",\"arguments\":[]}");
            assertTrue(res.contains("Unsubscribed"));
        }

        @Test void doThisCommandUnrestrictedAllowsDump() {
            ITCService.getInstance().doThisCommand(
                    "{\"robot\":\"HAL\",\"command\":\"launch\",\"arguments\":[\"balanced\"]}");
            String res = ITCService.getInstance().doThisCommandUnRestricted(
                    "{\"robot\":\"HAL\",\"command\":\"dump\",\"arguments\":[]}");
            assertTrue(res.contains("\"result\":\"OK\""));
            assertTrue(res.contains("Robot Dump"));
        }
    }

    @Nested
    @DisplayName("Broadcasts")
    class Broadcasts {
        @Test void broadcastEventWithNullDoesNotThrow() {
            ITCService.getInstance().broadcastEvent(null);
        }

        @Test void broadcastEventSendsToRegisteredWriters() {
            StringWriter sw = new StringWriter();
            ITCService.getInstance().registerClient("Bob", new PrintWriter(sw, true));
            ServerResponse evt = ServerResponse.builder()
                    .result(StatusCode.OK)
                    .data(ServerResponseData.builder().message("hello").build())
                    .build();
            ITCService.getInstance().broadcastEvent(evt);
            assertTrue(sw.toString().contains("hello"));
            ITCService.getInstance().unregisterClient("Bob");
        }

        @Test void pushEventNullsAreSafe() {
            ITCService.getInstance().pushEvent(null, null);
            ITCService.getInstance().pushEvent("nobody", null);
        }

        @Test void pushEventDeliversToRegisteredClient() {
            StringWriter sw = new StringWriter();
            ITCService.getInstance().registerClient("Alice", new PrintWriter(sw, true));
            ServerResponse evt = ServerResponse.builder()
                    .result(StatusCode.OK)
                    .data(ServerResponseData.builder().message("ping").build())
                    .build();
            ITCService.getInstance().pushEvent("Alice", evt);
            assertTrue(sw.toString().contains("ping"));
            ITCService.getInstance().unregisterClient("Alice");
        }

        @Test void broadcastWorldStateWithoutSubscribersIsNoop() {
            ITCService.getInstance().broadcastWorldState();
        }

        @Test void broadcastWorldStateNotifiesSubscriber() {
            StringWriter sw = new StringWriter();
            ITCService.getInstance().registerClient("Subby", new PrintWriter(sw, true));
            ITCService.getInstance().subscribe("Subby");
            ITCService.getInstance().broadcastWorldState();
            assertTrue(sw.toString().contains("world_state"));
            ITCService.getInstance().unsubscribe("Subby");
            ITCService.getInstance().unregisterClient("Subby");
        }
    }

    @Nested
    @DisplayName("KillerController setter/getter")
    class KillerControllerWiring {
        @Test void setAndGetReturnsSameInstance() {
            za.co.wethinkcode.robots.server.npc.KillerNPCController c =
                    new za.co.wethinkcode.robots.server.npc.KillerNPCController(new RobotWorld(5, 5, 3));
            ITCService.getInstance().setKillerController(c);
            assertSame(c, ITCService.getInstance().getKillerController());
            ITCService.getInstance().setKillerController(null);
        }
    }
}
