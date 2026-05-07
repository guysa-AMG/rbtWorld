package za.co.wethinkcode.robots.server;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import za.co.wethinkcode.robots.server.world.Iworld;
import za.co.wethinkcode.robots.server.world.RobotWorld;
import za.co.wethinkcode.robots.services.ITCService;

public class WorldTest{


    @DisplayName("forward and backward test")
    @Nested
    class travelTest{

        @BeforeEach
        void setup(){
            ITCService.getInstance();
            Iworld world = new RobotWorld();
            var map = ITCService.getInstance().parseStringMap("\"   T    \",\"T       \"");
            world.loadMap(map);
            ITCService.getInstance().setWorld(world);
            String data =ITCService.getInstance().doThisCommand("{  \"robot\" : \"mark\",  \"command\" : \"launch\",  \"arguments\" : [ \"strong\" ]}");
           
           
        }

        @DisplayName("Test if Robot can turn and move forward if not obstructed.")
        @Test
        void turnAndForwardunObstructedTest(){
    
           ITCService.getInstance().doThisCommand("{  \"robot\" : \"mark\",  \"command\" : \"turn\",  \"arguments\" : [ \"left\" ]}");
           String res = ITCService.getInstance().doThisCommand("{  \"robot\" : \"mark\",  \"command\" : \"forward\",  \"arguments\" : [ \"2\" ]}");

            assertEquals("{\"result\":\"OK\",\"data\":{\"message\":\"DONE\"},\"state\":{\"position\":{\"x\":2,\"y\":0},\"direction\":\"EAST\",\"shields\":20,\"shots\":0}}",res);

        }



         @DisplayName("Test if Robot can turn and move forward then backward if not obstructed.")
        @Test
        void turnAndbackwardunObstructedTest(){
    
           ITCService.getInstance().doThisCommand("{  \"robot\" : \"mark\",  \"command\" : \"turn\",  \"arguments\" : [ \"left\" ]}");
           ITCService.getInstance().doThisCommand("{  \"robot\" : \"mark\",  \"command\" : \"forward\",  \"arguments\" : [ \"2\" ]}");
          String res = ITCService.getInstance().doThisCommand("{  \"robot\" : \"mark\",  \"command\" : \"back\",  \"arguments\" : [ \"1\" ]}");

            assertEquals("{\"result\":\"OK\",\"data\":{\"message\":\"DONE\"},\"state\":{\"position\":{\"x\":1,\"y\":0},\"direction\":\"EAST\",\"shields\":20,\"shots\":0}}",res);

        }



         @DisplayName("Test if Robot can turn right and move backward into a obstructed.")
        @Test
        void forwardObstructedTest(){
           ITCService.getInstance().doThisCommand("{  \"robot\" : \"mark\",  \"command\" : \"turn\",  \"arguments\" : [ \"right\" ]}");
        
           String res = ITCService.getInstance().doThisCommand("{  \"robot\" : \"mark\",  \"command\" : \"back\",  \"arguments\" : [ \"100\" ]}");

            assertEquals("{\"result\":\"OK\",\"data\":{\"message\":\"BLOCKED\"},\"state\":{\"position\":{\"x\":2,\"y\":0},\"direction\":\"WEST\",\"shields\":20,\"shots\":0}}",res);

        }



         @DisplayName("Test if Robot can turn and move forward into an obstructed.")
        @Test
        void backwardObstructedTest(){
          ITCService.getInstance().doThisCommand("{  \"robot\" : \"mark\",  \"command\" : \"turn\",  \"arguments\" : [ \"left\" ]}");
          String res = ITCService.getInstance().doThisCommand("{  \"robot\" : \"mark\",  \"command\" : \"forward\",  \"arguments\" : [ \"5\" ]}");

            assertEquals("{\"result\":\"OK\",\"data\":{\"message\":\"BLOCKED\"},\"state\":{\"position\":{\"x\":2,\"y\":0},\"direction\":\"EAST\",\"shields\":20,\"shots\":0}}",res);

        }
        
    }
}