// # Test state changes (e.g., turning left)
package za.co.wethinkcode.robots.server;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
           
            String res = ITCService.getInstance().doThisCommand("{\"robot\":\"mark\", \"command\":\"Forward\", \"arguments\":[\"10\"]}");
            assertEquals(res, "{\"result\":\"ERROR\",\"data\":{\"message\":\"robot mark has not been launched\"}}");
        }

    }


      @DisplayName("Move based Test")
    @Nested
    class MovementTest{

    }


}