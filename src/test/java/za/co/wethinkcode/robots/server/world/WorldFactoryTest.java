package za.co.wethinkcode.robots.server.world;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import za.co.wethinkcode.robots.models.impediment.Impediments;

public class WorldFactoryTest {
    
    @Nested
    @DisplayName("world factory method for mapped world")
    class MapWorld{
       
        @Test
         void testNonEmptyMap(){
            
           RobotWorld world= RobotWorld.generateFromMapfile("worldMapTest.txt");
            List<Impediments> objs= world.getMap();
        
            assertTrue(objs.size()>1); 
            assertNotNull(objs.get(1));

        }

    }
}
