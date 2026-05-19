// # Test state changes (e.g., turning left)
package za.co.wethinkcode.robots.server;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import za.co.wethinkcode.robots.server.world.Iworld;
import za.co.wethinkcode.robots.server.world.RobotWorld;
import za.co.wethinkcode.robots.services.ITCService;

class InterThreadCommunicationTest {

    @BeforeEach
    void starter(){
        ITCService.getInstance();
         Iworld world = new RobotWorld();
        ITCService.getInstance().setWorld(world);
    }

    @DisplayName("Luanched based Test")
    @Nested
    class LaunchTest{
        

        @Test
        void testOtherCommandBeforeLaunch(){
            // Responses are now decorated with pickups/robots snapshots — check the key fields
            // instead of an exact JSON match.
            String res = ITCService.getInstance().doThisCommand("{\"robot\":\"mark\", \"command\":\"Forward\", \"arguments\":[\"10\"]}");
            assertTrue(res.contains("\"result\":\"ERROR\""));
            assertTrue(res.contains("robot mark has not been launched"));
        }

    }


      @DisplayName("Move based Test")
    @Nested
    class MovementTest{

    }


}