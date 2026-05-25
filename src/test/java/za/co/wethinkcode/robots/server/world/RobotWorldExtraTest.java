package za.co.wethinkcode.robots.server.world;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import za.co.wethinkcode.robots.models.Position;
import za.co.wethinkcode.robots.models.impediment.Pit;
import za.co.wethinkcode.robots.server.robot.SimpleRobot;
import za.co.wethinkcode.robots.server.robot.BaseRobot;

public class RobotWorldExtraTest {

    @Test
    public void testAddAmmoPickupNullAndBounds() {
        RobotWorld w = new RobotWorld(5,5,3);
        assertFalse(w.addAmmoPickup(null));
        // outside bounds
        assertFalse(w.isPositionAvailable(new Position(100,100)));
    }

    @Test
    public void testSwapPositionPitRemovesRobot() {
        RobotWorld w = new RobotWorld(5,5,3);
        SimpleRobot r = (SimpleRobot) BaseRobot.Builder("a",0,0,1,1,0);
        r.updatePosition(new Position(0,0));
        w.getAllRobots().put("a", r);
        // Replace the map with a modifiable copy containing the robot at (0,0)
        java.util.List<Object> newMap = new java.util.ArrayList<>(w.getMap());
        var existing = w.getObjectsAtPosition(new Position(0,0));
        newMap.remove(existing);
        newMap.add(r);
        w.loadMap((java.util.List) newMap);

        Pit pit = new Pit(new Position(1,1));
        // place pit into the world map
        newMap.remove(w.getObjectsAtPosition(new Position(1,1)));
        newMap.add(pit);
        w.loadMap((java.util.List) newMap);

        String res = w.swapePosition(new Position(1,1), new Position(0,0));
        assertEquals("HOLE", res);
        assertNull(w.getAllRobots().get("a"));
    }
}