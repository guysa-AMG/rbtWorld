package za.co.wethinkcode.robots.server.world;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import za.co.wethinkcode.robots.models.impediment.Tree;
import za.co.wethinkcode.robots.models.impediment.EmptySpot;
import za.co.wethinkcode.robots.models.impediment.Mountain;
import za.co.wethinkcode.robots.models.impediment.Pit;
import za.co.wethinkcode.robots.models.impediment.Water;
import za.co.wethinkcode.robots.models.impediment.Boundary;

import java.util.List;

public class WorldGeneratorTest {

    @Test
    public void testGenerateFromMapString_mapsToImpediments() {
        String map = "T . M\nP W |"; // two rows, tokens separated by spaces
        RobotWorld world = WorldGenerator.generateFromMapString(map);
        assertNotNull(world);
        List<?> objs = world.getMap();
        // Expect 3 tokens first row + 3 tokens second row = 6
        assertEquals(6, objs.size());
        assertTrue(objs.get(0) instanceof Tree);
        assertTrue(objs.get(1) instanceof EmptySpot);
        assertTrue(objs.get(2) instanceof Mountain);
        assertTrue(objs.get(3) instanceof Pit);
        assertTrue(objs.get(4) instanceof Water);
        assertTrue(objs.get(5) instanceof Boundary);
    }

    @Test
    public void testBuildCreatesObstaclesAndAmmo() {
        RobotWorld world = WorldGenerator.build();
        assertNotNull(world);
        // check a known mountain placed by build()
        assertEquals("MOUNTAIN", world.obstacleTypeAt(-25, 15));
        // build also seeds ammo pickups
        assertNotNull(world.getAmmoPickups());
        assertTrue(world.getAmmoPickups().size() > 0);
    }
}
